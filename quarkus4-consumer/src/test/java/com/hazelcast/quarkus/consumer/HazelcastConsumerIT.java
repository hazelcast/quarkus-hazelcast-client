package com.hazelcast.quarkus.consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;

@QuarkusMainIntegrationTest
@QuarkusTestResource(LocalHazelcastMember.class)
class HazelcastConsumerIT {
    @Test
    @Launch
    void writesAndReadsUsingThePackagedExtension(LaunchResult result) {
        assertTrue(result.getOutputStream().contains("Hazelcast round trip: hello"), result::getOutput);
    }
}
