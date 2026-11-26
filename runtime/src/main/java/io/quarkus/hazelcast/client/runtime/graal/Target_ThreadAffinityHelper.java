package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.util.ThreadAffinityHelper;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.util.BitSet;

/**
 * Reports CPU thread affinity as unavailable in native images, so configuring it fails with Hazelcast's own message.
 * <p>
 * {@code ThreadAffinityHelper} prefers a library it bundles for Linux x86_64 and falls back to
 * {@code net.openhft.affinity.Affinity}, an optional dependency. Its static initializer runs during the image build, so
 * the image builder only drops the fallback when that library loads on the build machine. On any other platform,
 * such as Linux arm64, the build stops with an unresolved type error. The bundled library isn't linked into the image,
 * so it can't be used at run time on x86_64 either.
 */
@TargetClass(ThreadAffinityHelper.class)
public final class Target_ThreadAffinityHelper {

    @Substitute
    static boolean isAffinityAvailable() {
        return false;
    }

    @Substitute
    public static BitSet getAffinity() {
        return new BitSet();
    }

    @Substitute
    public static void setAffinity(BitSet mask) {
        // Nothing to apply: ThreadAffinity rejects any non-empty CPU list when affinity is unavailable
    }
}
