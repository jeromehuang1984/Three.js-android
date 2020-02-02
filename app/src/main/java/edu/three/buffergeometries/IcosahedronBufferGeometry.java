package edu.three.buffergeometries;

public class IcosahedronBufferGeometry extends PolyhedronBufferGeometry {
    float t = ( 1 + (float) Math.sqrt( 5 ) ) / 2;
    float[] vertices = new float[] {
            - 1, t, 0, 	1, t, 0, 	- 1, - t, 0, 	1, - t, 0,
            0, - 1, t, 	0, 1, t,	0, - 1, - t, 	0, 1, - t,
            t, 0, - 1, 	t, 0, 1, 	- t, 0, - 1, 	- t, 0, 1
    };
    int[] indices = new int[] {
            0, 11, 5, 	0, 5, 1, 	0, 1, 7, 	0, 7, 10, 	0, 10, 11,
            1, 5, 9, 	5, 11, 4,	11, 10, 2,	10, 7, 6,	7, 1, 8,
            3, 9, 4, 	3, 4, 2,	3, 2, 6,	3, 6, 8,	3, 8, 9,
            4, 9, 5, 	2, 4, 11,	6, 2, 10,	8, 6, 7,	9, 8, 1
    };

    public IcosahedronBufferGeometry() {
        this(null, null);
    }
    public IcosahedronBufferGeometry(Float radius, Integer detail) {
        set(vertices, indices, radius, detail);
    }
}
