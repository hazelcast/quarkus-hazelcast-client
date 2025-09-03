package io.quarkus.hazelcast.client.deployment;

import com.hazelcast.client.cache.impl.HazelcastClientCachingProvider;
import com.hazelcast.client.config.ClientFlakeIdGeneratorConfig;
import com.hazelcast.client.config.ClientReliableTopicConfig;
import com.hazelcast.client.impl.ClientExtension;
import com.hazelcast.client.impl.protocol.task.topic.TopicAddMessageListenerMessageTask;
import com.hazelcast.client.impl.proxy.ClientCardinalityEstimatorProxy;
import com.hazelcast.client.impl.proxy.ClientClusterProxy;
import com.hazelcast.client.impl.proxy.ClientDurableExecutorServiceProxy;
import com.hazelcast.client.impl.proxy.ClientExecutorServiceProxy;
import com.hazelcast.client.impl.proxy.ClientFlakeIdGeneratorProxy;
import com.hazelcast.client.impl.proxy.ClientListProxy;
import com.hazelcast.client.impl.proxy.ClientMapProxy;
import com.hazelcast.client.impl.proxy.ClientMultiMapProxy;
import com.hazelcast.client.impl.proxy.ClientPNCounterProxy;
import com.hazelcast.client.impl.proxy.ClientQueueProxy;
import com.hazelcast.client.impl.proxy.ClientReliableMessageRunner;
import com.hazelcast.client.impl.proxy.ClientReliableTopicProxy;
import com.hazelcast.client.impl.proxy.ClientReplicatedMapProxy;
import com.hazelcast.client.impl.proxy.ClientRingbufferProxy;
import com.hazelcast.client.impl.proxy.ClientScheduledExecutorProxy;
import com.hazelcast.client.impl.proxy.ClientScheduledFutureProxy;
import com.hazelcast.client.impl.proxy.ClientSetProxy;
import com.hazelcast.client.impl.proxy.ClientTopicProxy;
import com.hazelcast.client.impl.spi.ClientProxyFactory;
import com.hazelcast.client.util.RandomLB;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.collection.ItemListener;
import com.hazelcast.config.replacer.EncryptionReplacer;
import com.hazelcast.config.replacer.PropertyReplacer;
import com.hazelcast.config.replacer.spi.ConfigReplacer;
import com.hazelcast.core.EntryListener;
import com.hazelcast.internal.diagnostics.EventQueuePlugin;
import com.hazelcast.internal.diagnostics.OverloadedConnectionsPlugin;
import com.hazelcast.internal.networking.nio.NioThread;
import com.hazelcast.internal.util.ICMPHelper;
import com.hazelcast.internal.util.RandomPicker;
import com.hazelcast.internal.util.ThreadLocalRandomProvider;
import com.hazelcast.internal.util.executor.StripedExecutor;
import com.hazelcast.map.listener.MapListener;
import com.hazelcast.nio.SocketInterceptor;
import com.hazelcast.nio.serialization.DataSerializable;
import com.hazelcast.nio.serialization.DataSerializableFactory;
import com.hazelcast.nio.serialization.PortableFactory;
import com.hazelcast.nio.serialization.Serializer;
import com.hazelcast.partition.MigrationListener;
import com.hazelcast.query.extractor.ValueExtractor;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import com.hazelcast.topic.MessageListener;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.HotDeploymentWatchedFileBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveHierarchyIgnoreWarningBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeReinitializedClassBuildItem;
import io.quarkus.deployment.util.ServiceUtil;
import io.quarkus.hazelcast.client.runtime.HazelcastClientBytecodeRecorder;
import io.quarkus.hazelcast.client.runtime.HazelcastClientConfig;
import io.quarkus.hazelcast.client.runtime.HazelcastClientProducer;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.io.IOException;

import static io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem.builder;

class HazelcastClientProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem("hazelcast-client");
    }

    @BuildStep
    void enableSSL(BuildProducer<ExtensionSslNativeSupportBuildItem> ssl) {
        ssl.produce(new ExtensionSslNativeSupportBuildItem("hazelcast-client"));
    }

    @BuildStep
    void registerServiceProviders(BuildProducer<GeneratedResourceBuildItem> generatedResources,
                                  BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
                                  BuildProducer<NativeImageResourceBuildItem> resources) throws IOException {
        registerServiceProviders(DiscoveryStrategyFactory.class, resources, reflectiveClasses, generatedResources);
        registerServiceProviders(ClientExtension.class, resources, reflectiveClasses, generatedResources);
    }

    @BuildStep
    void setup(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.unremovableOf(HazelcastClientProducer.class));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    HazelcastClientConfiguredBuildItem resolveClientProperties(HazelcastClientBytecodeRecorder recorder) {
        recorder.configureRuntimeProperties();
        return new HazelcastClientConfiguredBuildItem();
    }

    @BuildStep
    void registerConfigurationFiles(
            BuildProducer<NativeImageResourceBuildItem> resources,
            BuildProducer<HotDeploymentWatchedFileBuildItem> watchedFiles) {
        resources.produce(new NativeImageResourceBuildItem(
                "hazelcast-client.yml",
                "hazelcast-client-default.xml",
                "hazelcast-client.yaml",
                "hazelcast-client.xml"));

        watchedFiles.produce(new HotDeploymentWatchedFileBuildItem("hazelcast-client.yml"));
        watchedFiles.produce(new HotDeploymentWatchedFileBuildItem("hazelcast-client.yaml"));
        watchedFiles.produce(new HotDeploymentWatchedFileBuildItem("hazelcast-client.xml"));
    }

    @BuildStep
    void registerReflectivelyCreatedClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses) {
        reflectiveClasses.produce(builder(HazelcastClientCachingProvider.class).build());
    }

    @BuildStep
    void registerCustomImplementationClasses(BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
            BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings) {

        registerTypeHierarchy(reflectiveClassHierarchies, ignoreWarnings,
                SocketInterceptor.class,
                MembershipListener.class,
                MigrationListener.class,
                EntryListener.class,
                MessageListener.class,
                ItemListener.class,
                MapListener.class,
                ValueExtractor.class,
                com.hazelcast.client.impl.ClientExtension.class,
                ClientProxyFactory.class);
    }

    @BuildStep
    void registerCustomConfigReplacerClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
            BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings) {

        registerTypeHierarchy(reflectiveClassHierarchies, ignoreWarnings, ConfigReplacer.class);
        reflectiveClasses.produce(builder(EncryptionReplacer.class, PropertyReplacer.class).build());
    }

    void registerServiceProviders(Class<?> klass, BuildProducer<NativeImageResourceBuildItem> resources, BuildProducer<ReflectiveClassBuildItem> reflectiveClasses, BuildProducer<GeneratedResourceBuildItem> generatedResources) throws IOException {
        String service = "META-INF/services/" + klass.getName();

        for (String impl : ServiceUtil.classNamesNamedIn(Thread.currentThread().getContextClassLoader(), service)) {
            reflectiveClasses.produce(builder(impl).build());
        }

        resources.produce(new NativeImageResourceBuildItem(service));
    }

    @BuildStep
    void registerCustomCredentialFactories(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
            BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings) {
        registerTypeHierarchy(
                reflectiveClassHierarchies, ignoreWarnings,
                com.hazelcast.security.ICredentialsFactory.class);

        reflectiveClasses.produce(builder(com.hazelcast.config.security.StaticCredentialsFactory.class).build());
    }

    @BuildStep
    void registerDataStructureProxies(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
                                      BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
                                      BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings) {
        reflectiveClasses.produce(builder(ClientSetProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientQueueProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientCardinalityEstimatorProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientClusterProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientDurableExecutorServiceProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientExecutorServiceProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientFlakeIdGeneratorProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientListProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientMapProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientMultiMapProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientPNCounterProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientQueueProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientReliableMessageRunner.class).methods().build());
        reflectiveClasses.produce(builder(ClientReliableTopicProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientReplicatedMapProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientRingbufferProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientScheduledExecutorProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientScheduledFutureProxy.class).methods().build());
        reflectiveClasses.produce(builder(ClientTopicProxy.class).methods().build());

        // created reflectively by com.hazelcast.internal.config.ConfigUtils#getConfig
        reflectiveClasses.produce(builder(ClientReliableTopicConfig.class).methods().build());
        reflectiveClasses.produce(builder(ClientFlakeIdGeneratorConfig.class).methods().build());

        registerTypeHierarchy(reflectiveClassHierarchies, ignoreWarnings, RuntimeException.class);
    }

    @BuildStep
    void registerSSLUtilities(BuildProducer<ReflectiveClassBuildItem> reflectiveClasses,
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
            BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings) {

        registerTypeHierarchy(
                reflectiveClassHierarchies, ignoreWarnings,
                com.hazelcast.nio.ssl.SSLContextFactory.class);
    }

    @BuildStep
    void registerUserImplementationsOfSerializableUtilities(
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveClassHierarchies,
            BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings) {
        registerTypeHierarchy(reflectiveClassHierarchies, ignoreWarnings,
                DataSerializable.class,
                DataSerializableFactory.class,
                PortableFactory.class,
                Serializer.class);
    }

    @BuildStep
    void registerICMPHelper(BuildProducer<NativeImageResourceBuildItem> resources,
            BuildProducer<RuntimeReinitializedClassBuildItem> reinitializedClasses) {
        resources.produce(new NativeImageResourceBuildItem(
                "lib/linux-x86/libicmp_helper.so",
                "lib/linux-x86_64/libicmp_helper.so"));
        reinitializedClasses.produce(new RuntimeReinitializedClassBuildItem(ICMPHelper.class.getName()));
    }

    @BuildStep
    void initializeRandomHolderAtRuntime(BuildProducer<RuntimeInitializedClassBuildItem> runtimeInitializedClasses) {
        try {
            Class.forName("com.hazelcast.client.impl.connection.nio.WaitStrategy");
            runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem("com.hazelcast.client.impl.connection.nio.WaitStrategy"));
        } catch (Throwable e) {
            runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem("com.hazelcast.client.impl.connection.tcp.WaitStrategy"));
        }
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(TopicAddMessageListenerMessageTask.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(ClientDurableExecutorServiceProxy.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(ClientExecutorServiceProxy.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(RandomLB.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(EventQueuePlugin.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(OverloadedConnectionsPlugin.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(NioThread.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(StripedExecutor.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(ThreadLocalRandomProvider.class.getName()));
        runtimeInitializedClasses.produce(new RuntimeInitializedClassBuildItem(RandomPicker.class.getName()));
    }

    @BuildStep
    void registerXMLParsingUtilities(BuildProducer<NativeImageResourceBuildItem> resources) {
        resources.produce(new NativeImageResourceBuildItem("hazelcast-client-config-5.0.xsd"));
        resources.produce(new NativeImageResourceBuildItem("hazelcast-client-config-5.1.xsd"));
        resources.produce(new NativeImageResourceBuildItem("hazelcast-client-config-5.2.xsd"));
    }

    private static void registerTypeHierarchy(
            BuildProducer<ReflectiveHierarchyBuildItem> reflectiveHierarchyClass,
            BuildProducer<ReflectiveHierarchyIgnoreWarningBuildItem> ignoreWarnings,
            Class<?>... classNames) {

        for (Class<?> klass : classNames) {
            DotName simpleName = DotName.createSimple(klass.getName());

            reflectiveHierarchyClass.produce(
              ReflectiveHierarchyBuildItem.builder(Type.create(simpleName, Type.Kind.CLASS)).build());

            ignoreWarnings.produce(
              new ReflectiveHierarchyIgnoreWarningBuildItem(simpleName));
        }
    }
}
