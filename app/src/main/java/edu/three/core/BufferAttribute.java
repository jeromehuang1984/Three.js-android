package edu.three.core;

import edu.three.math.Color;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class BufferAttribute {
    public String name = "";

    public int[] arrayInt;
    public float[] arrayFloat;
    public static int TYPE_FLOAT = 1;
    public static int TYPE_INT = 2;
    protected int bufferType;

    private int arrLen;
    protected int itemSize = 1;
    private int count;
    public boolean normalized = false;

    private boolean dynamic = false;
    public int updateRangeOffset = 0;
    public int updateRangeCount = -1;

    private int version = 0;

    public void setNeedsUpdate(boolean value) {
        if (value) {
            version++;
        }
    }

    public int getVersion() {
        return version;
    }

    public BufferAttribute() {
    }

    public BufferAttribute(float[] array, int itemSize) {
        setArray(array);
        setItemSize(itemSize);
    }

    public BufferAttribute(int[] array, int itemSize) {
        setArray(array);
        setItemSize(itemSize);
    }

    public BufferAttribute setName(String name) {
        this.name = name;
        return this;
    }

    public BufferAttribute setArray(float[] array) {
        bufferType = TYPE_FLOAT;
        arrLen = array.length;
        count = arrLen / itemSize;
        arrayFloat = array;
        return this;
    }

    public BufferAttribute setArray(int[] array) {
        bufferType = TYPE_INT;
        arrLen = array.length;
        count = arrLen / itemSize;
        arrayInt = array;
        return this;
    }

    public BufferAttribute setDynamic(Boolean value) {
        dynamic = value;
        return this;
    }

    public boolean getDynamic() {
        return dynamic;
    }

    public BufferAttribute copy(BufferAttribute source) {
        name = source.name;
        bufferType = source.bufferType;
        if (bufferType == TYPE_INT) {
//            arrayInt = source.arrayInt;
            if (source.arrayInt != null) {
                arrayInt = new int[source.arrayInt.length];
                System.arraycopy(source.arrayInt, 0, arrayInt, 0, arrayInt.length);
            }
        } else if (bufferType == TYPE_FLOAT) {
//            arrayFloat = source.arrayFloat;
            if (source.arrayFloat != null) {
                arrayFloat = new float[source.arrayFloat.length];
                System.arraycopy(source.arrayFloat, 0, arrayFloat, 0, arrayFloat.length);
            }
        }
        itemSize = source.itemSize;
        count = source.count;
        normalized = source.normalized;

        dynamic = source.dynamic;

        return this;
    }

    public BufferAttribute clone() {
        return new BufferAttribute().copy(this);
    }

    //max int java is 2^31
    public BufferAttribute copyAt(int index1, BufferAttribute attribute, int index2) {
        int indexA = index1 * itemSize;
        int indexB = index2 * attribute.itemSize;
        for (int i = 0; i < itemSize; i++) {
            if (bufferType == TYPE_INT) {
                arrayInt[indexA + i] = attribute.arrayInt[indexB + i];
            } else {
                arrayFloat[indexA + i] = attribute.arrayFloat[indexB + i];
            }
        }
        return this;
    }

    public BufferAttribute copyArray(int[] array) {
        return copyArray(array, 0);
    }
    public BufferAttribute copyArray(int[] array, int offset) {
        bufferType = TYPE_INT;
        arrayInt = new int[array.length];
        count = array.length / itemSize;
        System.arraycopy(array, offset, arrayInt, 0, array.length);
        return this;
    }

    public BufferAttribute setItemSize(int itemSize) {
        this.itemSize = itemSize;
        count = arrLen / itemSize;
        return this;
    }

    public int getItemSize() {
        return itemSize;
    }

    public BufferAttribute copyArray(float[] array) {
        return copyArray(array, 0);
    }
    public BufferAttribute copyArray(float[] array, int offset) {
        bufferType = TYPE_FLOAT;
        arrayFloat = new float[array.length];
        count = array.length / itemSize;
        System.arraycopy(array, offset, arrayFloat, 0, array.length);
        return this;
    }

    public int getCount() {
        return count;
    }

    public int getBufferType() {
        return bufferType;
    }

    public BufferAttribute copyVector2sArray(Vector2[] vectors) {
        int offset = 0;
        bufferType = TYPE_FLOAT;
        arrayFloat = new float[vectors.length * 2];
        for (int i = 0; i < vectors.length; i++) {
            Vector2 vector = vectors[i];
            arrayFloat[offset++] = vector.x;
            arrayFloat[offset++] = vector.y;
        }
        return this;
    }

    public BufferAttribute copyVector3sArray(Vector3[] vectors) {
        int offset = 0;
        bufferType = TYPE_FLOAT;
        arrayFloat = new float[vectors.length * 3];
        for (int i = 0; i < vectors.length; i++) {
            Vector3 vector = vectors[i];
            arrayFloat[offset++] = vector.x;
            arrayFloat[offset++] = vector.y;
            arrayFloat[offset++] = vector.z;
        }
        return this;
    }

    public BufferAttribute copyColorsArray(Color[] colors) {
        int offset = 0;
        bufferType = TYPE_FLOAT;
        arrayFloat = new float[colors.length * 3];
        for (int i = 0; i < colors.length; i++) {
            Color color = colors[i];
            arrayFloat[offset++] = color.r;
            arrayFloat[offset++] = color.g;
            arrayFloat[offset++] = color.b;
        }
        return this;
    }

    public float getX(int index) {
        if (bufferType == TYPE_FLOAT) {
            return arrayFloat[index * itemSize];
        } else {
            return arrayInt[index * itemSize];
        }
    }

    public BufferAttribute setX(int index, float x) {
        arrayFloat[index * itemSize] = x;
        return this;
    }

    public float getY(int index) {
        if (bufferType == TYPE_FLOAT) {
            return arrayFloat[index * itemSize + 1];
        } else {
            return arrayInt[index * itemSize + 1];
        }
    }

    public BufferAttribute setY(int index, float y) {
        arrayFloat[index * itemSize + 1] = y;
        return this;
    }

    public float getZ(int index) {
        if (bufferType == TYPE_FLOAT) {
            return arrayFloat[index * itemSize + 2];
        } else {
            return arrayInt[index * itemSize + 2];
        }
    }

    public BufferAttribute setZ(int index, float z) {
        arrayFloat[index * itemSize + 2] = z;
        return this;
    }

    public float getW(int index) {
        if (bufferType == TYPE_FLOAT) {
            return arrayFloat[index * itemSize + 3];
        } else {
            return arrayInt[index * itemSize + 3];
        }
    }

    public BufferAttribute setW(int index, float w) {
        arrayFloat[index * itemSize + 3] = w;
        return this;
    }

    public BufferAttribute setXY(int index, float x, float y) {
        int ind = index * itemSize;
        arrayFloat[ind] = x;
        arrayFloat[ind + 1] = y;
        return this;
    }

    public BufferAttribute setXYZ(int index, float x, float y, float z) {
        int ind = index * itemSize;
        arrayFloat[ind] = x;
        arrayFloat[ind + 1] = y;
        arrayFloat[ind + 2] = z;
        return this;
    }

    public BufferAttribute setXYZW(int index, float x, float y, float z, float w) {
        int ind = index * itemSize;
        arrayFloat[ind] = x;
        arrayFloat[ind + 1] = y;
        arrayFloat[ind + 2] = z;
        arrayFloat[ind + 3] = w;
        return this;
    }

    public BufferAttribute setFrom(Vector3[] vector3s) {
        if (itemSize != 3 || count != vector3s.length || bufferType != TYPE_FLOAT) {
            arrayFloat = new float[vector3s.length * 3];
            itemSize = 3;
            count = vector3s.length;
            bufferType = TYPE_FLOAT;
        }
        for (int i = 0; i < vector3s.length; i++) {
            arrayFloat[i*3] = vector3s[i].x;
            arrayFloat[i*3 + 1] = vector3s[i].y;
            arrayFloat[i*3 + 2] = vector3s[i].z;
        }
        return this;
    }
}
