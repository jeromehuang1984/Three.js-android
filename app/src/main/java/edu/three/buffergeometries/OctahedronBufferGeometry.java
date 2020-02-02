package edu.three.buffergeometries;

public class OctahedronBufferGeometry extends PolyhedronBufferGeometry {
    float[] vertices = new float[] {
            1, 0, 0, 	- 1, 0, 0,	0, 1, 0,
            0, - 1, 0, 	0, 0, 1,	0, 0, - 1
    };
    int[] indices = new int[] {
            0, 2, 4,	0, 4, 3,	0, 3, 5,
            0, 5, 2,	1, 2, 5,	1, 5, 3,
            1, 3, 4,	1, 4, 2
    };

    public OctahedronBufferGeometry() {
        this(null, null);
    }
    public OctahedronBufferGeometry(Float radius, Integer detail) {
        set(vertices, indices, radius, detail);
    }
}
