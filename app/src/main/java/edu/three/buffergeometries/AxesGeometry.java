package edu.three.buffergeometries;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;

public class AxesGeometry extends BufferGeometry {

    private float size;
    public  AxesGeometry(float size) {
        this.size = size;
        float[] vertices = new float[] {
                0, 0, 0,	size, 0, 0,
                0, 0, 0,	0, size, 0,
                0, 0, 0,	0, 0, size
        };

        position = new BufferAttribute().setArray(vertices).setItemSize(3);
        addGroup(0, 2, 0);
        addGroup(2, 2, 1);
        addGroup(4, 2, 2);
    }
}
