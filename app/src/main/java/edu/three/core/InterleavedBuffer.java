package edu.three.core;

import java.util.Arrays;

public class InterleavedBuffer extends BufferAttribute {
    public int stride;
    public int count;
    boolean dynamic = false;

    public InterleavedBuffer() {}

    public InterleavedBuffer(float[] array, int stride) {
        this.arrayFloat = array;
        this.stride = stride;
        if (array != null) {
            count = array.length / stride;
        }
        bufferType = TYPE_FLOAT;
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
        this.arrayFloat = array;
        count = array != null ? array.length / stride : 0;
        return this;
    }

    public InterleavedBuffer copy(InterleavedBuffer source) {
        arrayFloat = Arrays.copyOf(source.arrayFloat, source.arrayFloat.length);
        this.count = source.count;
        this.stride = source.stride;
        this.dynamic = source.dynamic;

        return this;
    }

    public InterleavedBuffer copyAt(int index1, InterleavedBuffer attribute, int index2) {
        index1 *= stride;
        index2 *= attribute.stride;
        for ( int i = 0; i < stride; i ++ ) {
            arrayFloat[ index1 + i ] = attribute.arrayFloat[ index2 + i ];
        }

        return this;
    }

    public InterleavedBuffer set(float[] value, int offset) {
        arrayFloat = Arrays.copyOfRange(value, offset, value.length);
        return this;
    }

    public InterleavedBuffer clone() {
        return new InterleavedBuffer().copy(this);
    }
}
