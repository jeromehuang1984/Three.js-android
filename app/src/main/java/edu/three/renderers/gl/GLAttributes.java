package edu.three.renderers.gl;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.WeakHashMap;

import edu.three.core.BufferAttribute;

public class GLAttributes {
    WeakHashMap<BufferAttribute, BufferItem> buffers = new WeakHashMap<>();

    private BufferItem createBuffer(BufferAttribute attribute, int bufferType) {
        int glType = GLES30.GL_SHORT, bytesPerElement = 2;
        int usage = attribute.getDynamic() ? GLES30.GL_DYNAMIC_DRAW :GLES30.GL_STATIC_DRAW;

        Buffer buffer = null;
        int attrType = attribute.getBufferType();
        if (attrType == BufferAttribute.TYPE_FLOAT) {
            glType = GLES30.GL_FLOAT;
            bytesPerElement = 4;
            buffer = ByteBuffer
                    .allocateDirect(attribute.arrayFloat.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer()
                    .put(attribute.arrayFloat);
            buffer.position(0);
        } else if (attrType == BufferAttribute.TYPE_INT) {
            glType = GLES30.GL_UNSIGNED_INT;
            bytesPerElement = 4;
            buffer = ByteBuffer
                    .allocateDirect(attribute.arrayInt.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asIntBuffer()
                    .put(attribute.arrayInt);
            buffer.position(0);
        }

        int[] buff = new int[1];
        GLES30.glGenBuffers(1, buff, 0);
        GLES30.glBindBuffer(bufferType, buff[0]);
        GLES30.glBufferData(bufferType, buffer.capacity() * bytesPerElement, buffer, usage);

        return new BufferItem(buffer, glType, bytesPerElement, attribute.getVersion(), buff, usage);
    }

    private void updateBuffer(BufferItem buff, BufferAttribute attribute, int bufferType) {
        int rangeOffset = attribute.updateRangeOffset;
        int rangeCount = attribute.updateRangeCount;
        int byesPer = buff.bytesPerElement;
        GLES30.glBindBuffer(bufferType, buff.bufferLoc[0]);
        buff.update(attribute);
        if (!attribute.getDynamic()) {
            GLES30.glBufferData(bufferType, buff.buffer.capacity() * byesPer,
                    buff.buffer, GLES30.GL_STATIC_DRAW);
        } else if (rangeCount < 0) {
            // Not using update ranges
            GLES30.glBufferSubData(bufferType, 0, buff.buffer.capacity() * byesPer, buff.buffer);
        } else if (rangeCount == 0) {
            Log.e(getClass().getSimpleName(),
    "THREE.WebGLObjects.updateBuffer: dynamic THREE.BufferAttribute marked as needsUpdate but updateRange.count is 0, ensure you are using set methods or updating manually.");
        } else {
            int size = attribute.updateRangeCount *byesPer;
            GLES30.glBufferSubData(bufferType, rangeOffset * byesPer, size, buff.buffer);
            attribute.updateRangeCount = - 1; // reset range
        }
    }

    public BufferItem get(BufferAttribute attribute) {
        return buffers.get(attribute);
    }

    public void remove(BufferAttribute attribute) {
        BufferItem data = buffers.get(attribute);
        if (data != null) {
            GLES30.glDeleteBuffers(1, data.bufferLoc, 0);
            buffers.remove(data);
        }
    }

    public void update(BufferAttribute attribute, int bufferType) {
        BufferItem data = buffers.get(attribute);
        if (data == null) {
            buffers.put(attribute, createBuffer(attribute, bufferType));
        } else if (data.version < attribute.getVersion()) {
            updateBuffer(data, attribute, bufferType);
            data.version = attribute.getVersion();
        }
    }

    public static class BufferItem {
        public Buffer buffer;
        public int type;
        public int bytesPerElement;
        public int version;
        public int[] bufferLoc;
        public int usage;

        public BufferItem(Buffer buffer, int type, int bytesPerElement, int version, int[] bufferLoc, int usage) {
            this.buffer = buffer;
            this.type = type;
            this.bytesPerElement = bytesPerElement;
            this.version = version;
            this.bufferLoc = bufferLoc;
            this.usage = usage;
        }

        void update(BufferAttribute attribute) {
            buffer.position(0);
            if (buffer instanceof FloatBuffer) {
                ((FloatBuffer) buffer).put(attribute.arrayFloat);
            } else {
                ((IntBuffer) buffer).put(attribute.arrayInt);
            }
            buffer.position(0);
        }
    }
}
