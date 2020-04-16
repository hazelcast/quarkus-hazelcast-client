package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.logging.ILogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.hazelcast.internal.nio.IOUtil.closeResource;

final class ServiceLoaderUtils {
    private ServiceLoaderUtils() {
    }

    /*
    Expanded version of {@link com.hazelcast.internal.util.ServiceLoader#parse(ServiceLoader.URLDefinition)
    that's additionally parameterized with {@link ILogger} }
     */
    static Set<Target_ServiceDefinition> parse(URL url, ClassLoader classLoader, ILogger logger) {
        try {
            Set<Target_ServiceDefinition> names = new HashSet<>();
            BufferedReader r = null;
            try {
                r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    int comment = line.indexOf('#');
                    if (comment >= 0) {
                        line = line.substring(0, comment);
                    }
                    String name = line.trim();
                    if (name.length() == 0) {
                        continue;
                    }
                    names.add(new Target_ServiceDefinition(name, classLoader));
                }
            } finally {
                closeResource(r);
            }
            return names;
        } catch (Exception e) {
            logger.severe(e);
        }
        return Collections.emptySet();
    }

    static ClassLoader resolveClassloader(ClassLoader classLoader) {
        return classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }
}
