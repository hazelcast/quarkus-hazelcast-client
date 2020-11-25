package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.util.ExceptionUtil;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.util.function.BooleanSupplier;

@TargetClass(value = ExceptionUtil.class, onlyWith = Target_ExceptionUtil.IsHazelcast41.class)
public final class Target_ExceptionUtil {

    @Substitute
    public static <T extends Throwable> T tryCreateExceptionWithMessageAndCause(Class<? extends Throwable> exceptionClass, String message, Throwable cause) {
        try {
            return (T) exceptionClass.getConstructor(String.class, Throwable.class)
              .newInstance(message, cause);
        } catch (Throwable e1) {
            try {
                return (T) exceptionClass.getConstructor(Throwable.class).newInstance(cause);
            } catch (Throwable e2) {
                try {
                    T result = (T) exceptionClass.getConstructor(String.class).newInstance(message);
                    result.initCause(cause);
                    return result;
                } catch (Throwable e3) {
                    try {
                        T result = (T) exceptionClass.getConstructor().newInstance();
                        result.initCause(cause);
                        return result;
                    } catch (Throwable e4) {
                        return null;
                    }
                }
            }
        }
    }

    public static final class IsHazelcast41 implements BooleanSupplier {

        @Override
        public boolean getAsBoolean() {
            try {
                ExceptionUtil.class
                  .getDeclaredMethod("tryCreateExceptionWithMessageAndCause", Class.class, String.class, Throwable.class);
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }
    }
}
