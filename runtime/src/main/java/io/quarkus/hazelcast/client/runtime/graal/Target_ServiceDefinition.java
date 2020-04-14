package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.util.ServiceLoader;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import java.util.function.BooleanSupplier;

import static com.hazelcast.internal.util.Preconditions.isNotNull;

@Substitute
@TargetClass(className = "com.hazelcast.internal.util.ServiceLoader", innerClass = "ServiceDefinition")
final class Target_ServiceDefinition {
    private final String className;
    private final ClassLoader classLoader;

    public Target_ServiceDefinition(String className, ClassLoader classLoader) {
        this.className = isNotNull(className, "className");
        this.classLoader = isNotNull(classLoader, "classLoader");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Target_ServiceDefinition that = (Target_ServiceDefinition) o;
        if (!classLoader.equals(that.classLoader)) {
            return false;
        }
        return className.equals(that.className);
    }

    @Override
    public int hashCode() {
        int result = className.hashCode();
        result = 31 * result + classLoader.hashCode();
        return result;
    }
}
