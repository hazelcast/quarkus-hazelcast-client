package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.config.MetricsJmxConfig;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(MetricsJmxConfig.class)
public final class Target_MetricsJmxConfig {

    @Substitute
    public boolean isEnabled() {
        return false;
    }
}
