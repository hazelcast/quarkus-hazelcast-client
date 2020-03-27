package io.quarkus.it.hazelcast.client;

import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

class PortableWrapperFactory implements PortableFactory {

    static final int FACTORY_ID = 1;

    @Override
    public Portable create(int classId) {
        if (PortableWrapper.CLASS_ID == classId) {
            return new PortableWrapper();
        } else {
            return null;
        }
    }
}
