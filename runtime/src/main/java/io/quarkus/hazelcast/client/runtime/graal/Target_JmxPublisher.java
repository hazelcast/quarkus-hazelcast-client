package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.metrics.jmx.JmxPublisher;
import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.Set;

@TargetClass(JmxPublisher.class)
public final class Target_JmxPublisher {

    @Alias
    private volatile boolean isShutdown;

    @Alias
    private String domainPrefix;

    @Alias
    private MBeanServer platformMBeanServer;

    @Alias
    private String instanceNameEscaped;

    @Substitute
    public void shutdown() {
        isShutdown = true;
        try {
            // unregister the MBeans registered by this JmxPublisher
            // the mBeans map can't be used since it is not thread-safe
            // and is meant to be used by the publisher thread only
            ObjectName name = new ObjectName(domainPrefix + "*" + ":instance=" + instanceNameEscaped + ",type=Metrics,*");
            if (platformMBeanServer == null) {
                return;
            }
            Set<ObjectName> objectNames = platformMBeanServer.queryNames(name, null);
            for (ObjectName bean : objectNames) {
                unregisterMBeanIgnoreError(bean);
            }
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("Exception when unregistering JMX beans", e);
        }
    }

    @Alias
    private void unregisterMBeanIgnoreError(ObjectName objectName) {
    }
}
