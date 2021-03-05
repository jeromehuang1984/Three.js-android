package edu.three.core;

public class InstancedInterleavedBuffer extends InterleavedBuffer {
    public int meshPerAttribute = 1;

    public InstancedInterleavedBuffer(float[] array, int stride, int meshPerAttribute) {
        super(array, stride);
        this.meshPerAttribute = meshPerAttribute;
    }
}
