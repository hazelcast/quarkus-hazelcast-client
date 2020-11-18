package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.util.ThreadAffinityHelper;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(ThreadAffinityHelper.class)
public final class Target_ThreadAffinityHelper {

    @Substitute
    static boolean isAffinityAvailable() {
        return false;
    }
}
