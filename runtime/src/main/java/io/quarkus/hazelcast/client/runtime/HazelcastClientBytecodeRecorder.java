package io.quarkus.hazelcast.client.runtime;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class HazelcastClientBytecodeRecorder {
    private final RuntimeValue<HazelcastClientConfig> hazelcastClientConfig;

    public HazelcastClientBytecodeRecorder(RuntimeValue<HazelcastClientConfig> hazelcastClientConfig) {
        this.hazelcastClientConfig = hazelcastClientConfig;
    }

    public void configureRuntimeProperties() {
        HazelcastClientProducer producer = Arc.container().instance(HazelcastClientProducer.class).get();
        producer.injectConfig(hazelcastClientConfig.getValue());
    }
}
