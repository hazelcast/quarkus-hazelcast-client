package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.metrics.jmx.JmxPublisher;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(JmxPublisher.class)
public final class Target_JmxPublisher {

    @Alias
    private volatile boolean isShutdown;

    @Substitute
    public Target_JmxPublisher(String instanceName, String domainPrefix) {
    }

    @Substitute
    public void shutdown() {
        isShutdown = true;
    }
}
