package edu.three.renderers.gl;

import java.util.HashMap;

import edu.three.core.BufferGeometry;
import edu.three.core.EventDispatcher;
import edu.three.core.Geometry;
import edu.three.core.Object3D;
import edu.three.objects.Mesh;

public class GLObjects {
    GLGeometries geometries;
    GLInfo info;
    HashMap<Long, Integer> updateList = new HashMap<>();

    public GLObjects(GLGeometries glGeometries, GLInfo glInfo) {
        geometries = glGeometries;
        info = glInfo;
    }

    public BufferGeometry update(Object3D object) {
        int frame = info.frame;
        EventDispatcher geometry = object.geometry;
        BufferGeometry bufferGeometry = geometries.get(object, geometry);

        // Update once per frame
        if (!updateList.containsKey(bufferGeometry.id) || updateList.get(bufferGeometry.id) != frame) {
            if (geometry instanceof Geometry) {
                bufferGeometry.updateFromObject(object);
            }
            geometries.update(bufferGeometry);
            updateList.put(bufferGeometry.id, frame);
        }
        return bufferGeometry;
    }

    public void dispose() {
        updateList.clear();
    }
}
