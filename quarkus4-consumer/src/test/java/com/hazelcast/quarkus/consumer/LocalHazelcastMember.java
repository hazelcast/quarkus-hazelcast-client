package com.hazelcast.quarkus.consumer;

import java.util.Map;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class LocalHazelcastMember implements QuarkusTestResourceLifecycleManager {
    private HazelcastInstance member;

    @Override
    public Map<String, String> start() {
        String clusterName = "quarkus4-consumer-" + UUID.randomUUID();
        Config config = new Config().setClusterName(clusterName);
        config.setProperty("hazelcast.socket.server.bind.any", "false");

        NetworkConfig network = config.getNetworkConfig().setPort(0).setPortAutoIncrement(false);
        network.getInterfaces().setEnabled(true).addInterface("127.0.0.1");
        network.getJoin().getAutoDetectionConfig().setEnabled(false);

        member = Hazelcast.newHazelcastInstance(config);
        int port = member.getCluster().getLocalMember().getAddress().getPort();
        return Map.of(
                "quarkus.hazelcast-client.cluster-name", clusterName,
                "quarkus.hazelcast-client.cluster-members", "127.0.0.1:" + port,
                "hz-client.connection-strategy.connection-retry.cluster-connect-timeout-millis", "10000");
    }

    @Override
    public void stop() {
        if (member != null) {
            member.shutdown();
        }
    }
}
