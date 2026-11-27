package com.hazelcast.quarkus.consumer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.hazelcast.HazelcastServerTestResource;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;

@QuarkusMainIntegrationTest
@QuarkusTestResource(value = HazelcastServerTestResource.class, initArgs = @ResourceArg(name = "isolated", value = "true"))
class HazelcastConsumerIT {
    @Test
    @Launch
    void writesAndReadsUsingThePackagedExtension(LaunchResult result) {
        assertTrue(result.getOutputStream().contains("Hazelcast round trip: hello"), result::getOutput);
    }
}
