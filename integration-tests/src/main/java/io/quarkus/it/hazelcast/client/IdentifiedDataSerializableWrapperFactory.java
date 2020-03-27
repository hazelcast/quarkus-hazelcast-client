package io.quarkus.it.hazelcast.client;

import com.hazelcast.nio.serialization.IdentifiedDataSerializable;

class IdentifiedDataSerializableWrapperFactory implements com.hazelcast.nio.serialization.DataSerializableFactory {

    static final int FACTORY_ID = 42;

    @Override
    public IdentifiedDataSerializable create(int typeId) {
        if (typeId == IdentifiedDataSerializableWrapper.CLASS_ID) {
            return new IdentifiedDataSerializableWrapper();
        }

        throw new IllegalArgumentException(typeId + " unknown");
    }
}
