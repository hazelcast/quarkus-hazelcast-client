package com.hazelcast.quarkus.consumer;

import java.util.UUID;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

@QuarkusMain
public class HazelcastConsumer implements QuarkusApplication {
    @Inject
    HazelcastInstance hazelcast;

    @Override
    public int run(String... args) {
        IMap<String, String> map = hazelcast.getMap("quarkus4-consumer");
        String key = UUID.randomUUID().toString();
        String value = "hello";
        try {
            map.put(key, value);
            String result = map.get(key);
            if (!value.equals(result)) {
                throw new IllegalStateException("Expected to read back " + value + " but got " + result);
            }
            System.out.printf("Hazelcast round trip: %s", result);
            return 0;
        } finally {
            map.delete(key);
        }
    }
}
