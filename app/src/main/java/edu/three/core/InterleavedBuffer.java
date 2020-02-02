package edu.three.core;

import java.util.Arrays;

public class InterleavedBuffer {
    public float[] array;
    public int stride;
    public int count;
    boolean dynamic = false;
    public int updateRangeOffset = 0;
    public int updateRangeCount = -1;
    int version = 0;
    int arrLen = 0;

    public InterleavedBuffer() {}

    public InterleavedBuffer(float[] array, int stride) {
        this.array = array;
        this.stride = stride;
        if (array != null) {
            count = array.length;
        }
    }

    public void setNeedsUpdate(boolean value) {
        if (value) {
            version++;
        }
    }

    public int getVersion() {
        return version;
    }

    public InterleavedBuffer setDynamic(Boolean value) {
        dynamic = value;
        return this;
    }

    public boolean getDynamic() {
        return dynamic;
    }

    public InterleavedBuffer setArray(float[] array) {
        this.array = array;
        count = array != null ? array.length / stride : 0;
        return this;
    }

    public InterleavedBuffer copy(InterleavedBuffer source) {
        array = Arrays.copyOf(source.array, source.array.length);
        this.count = source.count;
        this.stride = source.stride;
        this.dynamic = source.dynamic;

        return this;
    }

    public InterleavedBuffer copyAt(int index1, InterleavedBuffer attribute, int index2) {
        index1 *= stride;
        index2 *= attribute.stride;
        for ( int i = 0; i < stride; i ++ ) {
            array[ index1 + i ] = attribute.array[ index2 + i ];
        }

        return this;
    }

    public InterleavedBuffer set(float[] value, int offset) {
        array = Arrays.copyOfRange(value, offset, value.length);
        return this;
    }

    public InterleavedBuffer clone() {
        return new InterleavedBuffer().copy(this);
    }
}
