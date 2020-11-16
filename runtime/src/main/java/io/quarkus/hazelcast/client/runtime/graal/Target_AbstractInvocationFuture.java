package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.spi.impl.AbstractInvocationFuture;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.util.function.BooleanSupplier;

@TargetClass(value = AbstractInvocationFuture.class, onlyWith = Target_AbstractInvocationFuture.IsHazelcast403.class)
public final class Target_AbstractInvocationFuture {

    @Substitute
    private static <T extends Throwable> T tryWrapInSameClass(T cause) {
        Class<? extends Throwable> exceptionClass = cause.getClass();

        try {
            return (T) exceptionClass.getConstructor(String.class, Throwable.class)
              .newInstance(cause.getMessage(), cause);
        } catch (Throwable e) {
            try {
                return (T) exceptionClass.getConstructor(Throwable.class).newInstance(cause);
            } catch (Throwable e2) {
                try {
                    T result = (T) exceptionClass.getConstructor(String.class).newInstance(cause.getMessage());
                    result.initCause(cause);
                    return result;
                } catch (Throwable e3) {
                    return null;
                }
            }
        }
    }

    public static final class IsHazelcast403 implements BooleanSupplier {

        @Override
        public boolean getAsBoolean() {
            try {
                AbstractInvocationFuture.class.getDeclaredMethod("tryWrapInSameClass", Throwable.class);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    }
}
