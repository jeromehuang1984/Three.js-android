package edu.three.math;

public class Vector4 {
    public float x,y,z,w;

    public int[] toIntArray() {
        int[] ret = new int[4];
        ret[0] = (int) x;
        ret[1] = (int) y;
        ret[2] = (int) z;
        ret[3] = (int) w;
        return ret;
    }

    public float[] toArray(float[] array, int offset) {
        if (array == null) {
            array = new float[4];
        }
        array[offset] = x;
        array[offset + 1] = y;
        array[offset + 2] = z;
        array[offset + 3] = w;
        return array;
    }

    public Vector4() {}

    public Vector4(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    public Vector4 set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4 divideScalar(float scalar) {
        return multiplyScalar( 1 / scalar );
    }

    public Vector4 multiplyScalar(float scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;

        return this;
    }

    public float manhattanLength() {
        return Math.abs( x ) + Math.abs( y ) + Math.abs( z ) + Math.abs( w );
    }

    public float length() {
        return (float) Math.sqrt( x * x + y * y + z * z + w * w );
    }

    public float lengthSq() {
        return x * x + y * y + z * z + w * w;
    }

    public Vector4 normalize() {
        float len = length();
        len = len == 0 ? 1 : len;
        return this.divideScalar( len );
    }

    public Vector4 floor() {
        x = (float) Math.floor( x );
        y = (float) Math.floor( y );
        z = (float) Math.floor( z );
        w = (float) Math.floor( w );

        return this;
    }

    public Vector4 copy(Vector4 v) {
        return set(v.x, v.y, v.z, v.w);
    }

    public boolean equals(Vector4 v) {
        return ( ( v.x == x ) && ( v.y == y )
                && ( v.z == z ) && (v.w == w) );
    }
}
