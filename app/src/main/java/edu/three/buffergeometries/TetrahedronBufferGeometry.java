package edu.three.buffergeometries;

public class TetrahedronBufferGeometry extends PolyhedronBufferGeometry {
    float[] vertices = new float[]{
            1, 1, 1, -1, -1, 1, -1, 1, -1, 1, -1, -1
    };
    int[] indices = new int[]{
            2, 1, 0, 0, 3, 2, 1, 3, 0, 2, 3, 1
    };

    public TetrahedronBufferGeometry() {
        this(null, null);
    }

    public TetrahedronBufferGeometry(Float radius, Integer detail) {
        set(vertices, indices, radius, detail);
    }
}
