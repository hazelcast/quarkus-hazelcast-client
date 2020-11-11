package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.spi.impl.AbstractInvocationFuture;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(AbstractInvocationFuture.class)
public final class Target_AbstractInvocationFuture {

    @Substitute
    private static <T extends Throwable> T tryWrapInSameClass(T cause) {
        Class<? extends Throwable> exceptionClass = cause.getClass();

        try {
            return (T) exceptionClass.getConstructor(String.class, Throwable.class)
              .newInstance(cause.getMessage(), cause);
        } catch (Throwable e) {
        }

        try {
            return (T) exceptionClass.getConstructor(Throwable.class).newInstance(cause);
        } catch (Throwable e) {
        }

        try {
            T result = (T) exceptionClass.getConstructor(String.class).newInstance(cause.getMessage());
            result.initCause(cause);
            return result;
        } catch (Throwable e) {
        }
        return null;
    }
}
