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

