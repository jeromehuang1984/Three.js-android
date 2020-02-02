package edu.three.renderers.gl;

import android.opengl.GLES30;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Event;
import edu.three.core.EventDispatcher;
import edu.three.core.Geometry;
import edu.three.core.IListener;
import edu.three.core.Object3D;

public class GLGeometries {
    GLAttributes attributes;
    GLInfo info;
    HashMap<Long, BufferGeometry> geometries = new HashMap<>();
    HashMap<Long, BufferAttribute> wireframeAttributes = new HashMap<>();

    public GLGeometries(GLAttributes glAttributes, GLInfo glInfo) {
        attributes = glAttributes;
        info = glInfo;
    }

    public IListener onGeometryDispose = new IListener() {
        @Override
        public void onEvent(Event event) {
            BufferGeometry geometry = (BufferGeometry) event.target;
            BufferGeometry bufferGeometry = geometries.get(geometry.id);
            if (bufferGeometry.getIndex() != null) {
                attributes.remove(bufferGeometry.getIndex());
            }
            for (BufferAttribute attribute : bufferGeometry.getNonNullAttributes()) {
                attributes.remove(attribute);
            }

            geometry.removeEventListener("dispose", onGeometryDispose);
            geometries.remove(geometry.id);

            BufferAttribute attribute = wireframeAttributes.get(bufferGeometry.id);
            if (attribute != null) {
                attributes.remove(attribute);
                wireframeAttributes.remove(bufferGeometry.id);
            }

            info.geometries --;
        }
    };

    public BufferGeometry get(Object3D object, EventDispatcher geometry) {
        long id = -1;
        if (geometry instanceof Geometry) {
            id = ((Geometry) geometry).id;
        } else if (geometry instanceof BufferGeometry) {
            id = ((BufferGeometry) geometry).id;
        } else {
            return null;
        }

        BufferGeometry bufferGeometry = geometries.get(id);
        if (bufferGeometry != null) {
            return bufferGeometry;
        }
        geometry.addEventListener("dispose", onGeometryDispose);

        if (geometry instanceof BufferGeometry) {
            bufferGeometry = (BufferGeometry) geometry;
        } else if (geometry instanceof Geometry) {
            bufferGeometry = new BufferGeometry().setFromObject(object);
        }

        geometries.put(id, bufferGeometry);
        info.geometries ++;
        return bufferGeometry;
    }

    public void update(BufferGeometry geometry) {
        BufferAttribute index = geometry.getIndex();
        ArrayList<BufferAttribute> geometryAttributes = geometry.getNonNullAttributes();
        if (index != null) {
            attributes.update(index, GLES30.GL_ELEMENT_ARRAY_BUFFER);
        }

        for (BufferAttribute attribute : geometryAttributes) {
            attributes.update(attribute, GLES30.GL_ARRAY_BUFFER);
        }

        // morph targets
        for (BufferAttribute attribute : geometry.morphAttributes) {
            attributes.update(attribute, GLES30.GL_ARRAY_BUFFER);
        }
    }

    public BufferAttribute getWireframeAttribute(BufferGeometry geometry) {
        BufferAttribute attribute = wireframeAttributes.get(geometry.id);
        if (attribute != null) {
            return attribute;
        }

        BufferAttribute geometryIndex = geometry.getIndex();
        int[] indices;
        int iCount = 0;
        if (geometryIndex != null) {
            int[] array = geometryIndex.arrayInt;
            indices = new int[array.length * 2];
            for (int i = 0; i < array.length; i+= 3) {
                int a = array[i];
                int b = array[i + 1];
                int c = array[i + 2];
                indices[iCount++] = a; indices[iCount++] = b; indices[iCount++] = b;
                indices[iCount++] = c; indices[iCount++] = c; indices[iCount++] = a;
            }
        } else {
            int arrayLen = geometry.position.arrayFloat.length;
            indices = new int[2 * (arrayLen - 3)];
            for (int i = 0; i < arrayLen / 3 -1; i += 3) {
                int a = i;
                int b = i + 1;
                int c = i + 2;
                indices[iCount++] = a; indices[iCount++] = b; indices[iCount++] = b;
                indices[iCount++] = c; indices[iCount++] = c; indices[iCount++] = a;
            }
        }
        attribute = new BufferAttribute().setArray(indices).setItemSize(1);
        attributes.update(attribute, GLES30.GL_ELEMENT_ARRAY_BUFFER);
        wireframeAttributes.put(geometry.id, attribute);
        return attribute;
    }
}
