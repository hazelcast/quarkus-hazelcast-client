package io.quarkus.test.hazelcast;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class HazelcastServerTestResource implements QuarkusTestResourceLifecycleManager {

    private boolean isolated;
    private volatile HazelcastInstance member;

    @Override
    public void init(Map<String, String> initArgs) {
        isolated = Boolean.parseBoolean(initArgs.get("isolated"));
    }

    @Override
    public Map<String, String> start() {
        Config config = new Config();
        NetworkConfig network = config.getNetworkConfig();
        network.getJoin().getAutoDetectionConfig().setEnabled(false);
        if (isolated) {
            config.setClusterName("hazelcast-test-" + UUID.randomUUID());
            config.setProperty("hazelcast.socket.server.bind.any", "false");
            network.setPort(0).setPortAutoIncrement(false);
            network.getInterfaces().setEnabled(true).addInterface("127.0.0.1");
        }

        member = Hazelcast.newHazelcastInstance(config);
        if (!isolated) {
            return Collections.emptyMap();
        }

        int port = member.getCluster().getLocalMember().getAddress().getPort();
        return Map.of(
                "quarkus.hazelcast-client.cluster-name", config.getClusterName(),
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
