package edu.three.core;

public class InstancedBufferAttribute extends BufferAttribute {
    public int meshPerAttribute;

    public InstancedBufferAttribute() {}

    public InstancedBufferAttribute(float[] array, int itemSize, boolean normalized, int meshPerAttribute) {
        arrayFloat = array;
        this.itemSize = itemSize;
        this.normalized = normalized;
        if (meshPerAttribute > 0) {
            this.meshPerAttribute = meshPerAttribute;
        } else {
            meshPerAttribute = 1;
        }
    }
}
