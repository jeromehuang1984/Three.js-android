package edu.three.core;

public class InterleavedBufferAttribute extends BufferAttribute {
    public InterleavedBuffer data;
    public int offset;

    public InterleavedBufferAttribute() {
    }

    public InterleavedBufferAttribute(InterleavedBuffer interleavedBuffer, int itemSize, int offset) {
        this(interleavedBuffer, itemSize, offset, false);
    }
    public InterleavedBufferAttribute(InterleavedBuffer interleavedBuffer, int itemSize, int offset, boolean normalized) {
        data = interleavedBuffer;
        arrayFloat = data.arrayFloat;
        this.itemSize = itemSize;
        this.offset = offset;
        this.normalized = normalized;
        bufferType = TYPE_FLOAT;
    }


    public int getCount() {
        return data.count;
    }

    public float[] getArray() {
        return data.arrayFloat;
    }

    public float getX(int index) {
        return arrayFloat[index * data.stride + offset];
    }

    public BufferAttribute setX(int index, float x) {
        arrayFloat[index * data.stride + offset] = x;
        return this;
    }

    public float getY(int index) {
        return arrayFloat[index * data.stride + offset + 1];
    }

    public BufferAttribute setY(int index, float y) {
        arrayFloat[index * data.stride + offset + 1] = y;
        return this;
    }

    public float getZ(int index) {
        return arrayFloat[index * data.stride + offset + 2];
    }

    public BufferAttribute setZ(int index, float z) {
        arrayFloat[index * data.stride + offset + 2] = z;
        return this;
    }

    public float getW(int index) {
        return arrayFloat[index * data.stride + offset + 3];
    }

    public BufferAttribute setW(int index, float w) {
        arrayFloat[index * data.stride + offset + 3] = w;
        return this;
    }

    public BufferAttribute setXY(int index, float x, float y) {
        int ind = index * data.stride + offset;
        arrayFloat[ind] = x;
        arrayFloat[ind + 1] = y;
        return this;
    }

    public BufferAttribute setXYZ(int index, float x, float y, float z) {
        int ind = index * data.stride + offset;
        arrayFloat[ind] = x;
        arrayFloat[ind + 1] = y;
        arrayFloat[ind + 2] = z;
        return this;
    }

    public BufferAttribute setXYZW(int index, float x, float y, float z, float w) {
        int ind = index * data.stride + offset;
        arrayFloat[ind] = x;
        arrayFloat[ind + 1] = y;
        arrayFloat[ind + 2] = z;
        arrayFloat[ind + 3] = w;
        return this;
    }
}
