package io.quarkus.hazelcast.client.runtime.graal;

import com.hazelcast.internal.memory.impl.UnsafeUtil;
import com.hazelcast.internal.nio.Bits;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import sun.misc.Unsafe;

import static com.hazelcast.internal.util.QuickMath.normalize;

@TargetClass(UnsafeUtil.class)
public final class Target_UnsafeUtil {

    @Substitute
    private static void checkUnsafeInstance(Unsafe unsafe) {
        byte[] buffer = new byte[(int) (long) unsafe.arrayBaseOffset(byte[].class) + (2 * Bits.LONG_SIZE_IN_BYTES)];
        unsafe.putByte(buffer, unsafe.arrayBaseOffset(byte[].class), (byte) 0x00);
        unsafe.putBoolean(buffer, unsafe.arrayBaseOffset(byte[].class), false);
        unsafe.putChar(buffer, normalize((long) unsafe.arrayBaseOffset(byte[].class), Bits.CHAR_SIZE_IN_BYTES), '0');
        unsafe.putShort(buffer, normalize((long) unsafe.arrayBaseOffset(byte[].class), Bits.SHORT_SIZE_IN_BYTES), (short) 1);
        unsafe.putInt(buffer, normalize((long) unsafe.arrayBaseOffset(byte[].class), Bits.INT_SIZE_IN_BYTES), 2);
        unsafe.putFloat(buffer, normalize((long) unsafe.arrayBaseOffset(byte[].class), Bits.FLOAT_SIZE_IN_BYTES), 3f);
        unsafe.putLong(buffer, normalize((long) unsafe.arrayBaseOffset(byte[].class), Bits.LONG_SIZE_IN_BYTES), 4L);
        unsafe.putDouble(buffer, normalize((long) unsafe.arrayBaseOffset(byte[].class), Bits.DOUBLE_SIZE_IN_BYTES), 5d);
        unsafe.copyMemory(new byte[buffer.length], unsafe.arrayBaseOffset(byte[].class), buffer, unsafe.arrayBaseOffset(byte[].class), buffer.length);
    }
}
