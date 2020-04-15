package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.util.ServiceLoader;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static io.quarkus.hazelcast.client.runtime.graal.ServiceLoaderUtils.parse;

@TargetClass(ServiceLoader.class)
public final class Target_ServiceLoader {

    @Alias
    private static ILogger LOGGER;

    /*
     * Hazelcast SPI relies on the classpath presence of multiple _META-INF/services/com.foo.Bar_ files (for example, _com.hazelcast.spi.discovery.multicast.DiscoveryStrategyFactory_).
     *
     * However, the internal implementation of ServiceLoader doesn't play along with GraalVM's native images
     * since Substrate doesn't really feature a _normal_ classpath nor classloading but a mere _simulation_ of these.
     *
     * URLs to classpath resources located in different jars normally look like:
     *
     * jar:file:/work/application/lib/com.hazelcast.hazelcast-4.0.1.jar!/META-INF/services/com.hazelcast.spi.discovery.DiscoveryStrategyFactory
     * jar:file:/work/application/lib/com.hazelcast.hazelcast-kubernetes-2.0.1.jar!/META-INF/services/com.hazelcast.spi.discovery.DiscoveryStrategyFactory
     *
     *
     * but under Substrate, they become _resources_ and share the same URL (but `URL#openStream` leads to different content) which requires special handling:
     *
     * resource:META-INF/services/com.hazelcast.spi.discovery.DiscoveryStrategyFactory
     * resource:META-INF/services/com.hazelcast.spi.discovery.DiscoveryStrategyFactory
     *
     *
     * In order to make it work under GraalVM, we need to give up extra URL processing and don't enforce uniqueness for URLs, but for their content instead.
     */
    @Substitute
    private static Set<Target_ServiceDefinition> getServiceDefinitions(String factoryId, ClassLoader classLoader) {

        Set<Target_ServiceDefinition> services = new HashSet<>();

        try {
            Enumeration<URL> resources = classLoader.getResources("META-INF/services/" + factoryId);

            while (resources.hasMoreElements()) {
                services.addAll(parse(resources.nextElement(), classLoader, LOGGER));
            }
        } catch (IOException e) {
            LOGGER.severe(e);
        }

        if (services.isEmpty()) {
            Logger.getLogger(ServiceLoader.class).finest(
              "Service loader could not load 'META-INF/services/" + factoryId + "'. It may be empty or does not exist.");
        }
        return services;
    }
}

