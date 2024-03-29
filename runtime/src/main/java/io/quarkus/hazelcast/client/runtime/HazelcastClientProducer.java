package io.quarkus.hazelcast.client.runtime;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.arc.DefaultBean;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import static com.hazelcast.client.HazelcastClient.newHazelcastClient;

@ApplicationScoped
public class HazelcastClientProducer {
    HazelcastClientConfig config;

    @Produces
    @Singleton
    @DefaultBean
    public HazelcastInstance hazelcastClientInstance() {
        return newHazelcastClient(new HazelcastConfigurationResolver()
                .resolveClientConfig(config));
    }

    @PreDestroy
    public void destroy() {
        HazelcastClient.shutdownAll();
    }

    public void injectConfig(HazelcastClientConfig config) {
        this.config = config;
    }
}
