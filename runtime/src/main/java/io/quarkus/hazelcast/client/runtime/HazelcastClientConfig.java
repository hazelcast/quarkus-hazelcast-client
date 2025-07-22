package io.quarkus.hazelcast.client.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@ConfigMapping(prefix = "quarkus.hazelcast-client")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface HazelcastClientConfig {

    /**
     * Hazelcast Cluster members
     */
    Optional<List<String>> clusterMembers();

    /**
     * Hazelcast client labels
     */
    Optional<List<String>> labels();

    /**
     * Hazelcast Cluster group name
     */
    Optional<String> clusterName();

    /**
     * Outbound ports
     */
    Optional<List<Integer>> outboundPorts();

    /**
     * Outbound port definitions
     */
    Optional<List<String>> outboundPortDefinitions();

    /**
     * Connection timeout
     */
    OptionalInt connectionTimeout();
}
