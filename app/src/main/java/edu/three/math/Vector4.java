package edu.three.math;

public class Vector4 {
    public double x,y,z,w;

    public int[] toIntArray() {
        int[] ret = new int[4];
        ret[0] = (int) x;
        ret[1] = (int) y;
        ret[2] = (int) z;
        ret[3] = (int) w;
        return ret;
    }

    public double[] toArray(double[] array, int offset) {
        if (array == null) {
            array = new double[4];
        }
        array[offset] = x;
        array[offset + 1] = y;
        array[offset + 2] = z;
        array[offset + 3] = w;
        return array;
    }

    public float[] toArrayF(float[] array, int offset) {
        if (array == null) {
            array = new float[3];
        }
        array[offset] = (float)x;
        array[offset + 1] = (float)y;
        array[offset + 2] = (float)z;
        array[offset + 3] = (float)w;
        return array;
    }

    public Vector4() {}

    public Vector4(double x, double y, double z, double w) {
        set(x, y, z, w);
    }

    public Vector4 set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4 divideScalar(double scalar) {
        return multiplyScalar( 1 / scalar );
    }

    public Vector4 multiplyScalar(double scalar) {
        x *= scalar;
        y *= scalar;
        z *= scalar;
        w *= scalar;

        return this;
    }

    public double manhattanLength() {
        return Math.abs( x ) + Math.abs( y ) + Math.abs( z ) + Math.abs( w );
    }

    public double length() {
        return  Math.sqrt( x * x + y * y + z * z + w * w );
    }

    public double lengthSq() {
        return x * x + y * y + z * z + w * w;
    }

    public Vector4 normalize() {
        double len = length();
        len = len == 0 ? 1 : len;
        return this.divideScalar( len );
    }

    public Vector4 floor() {
        x =  Math.floor( x );
        y =  Math.floor( y );
        z =  Math.floor( z );
        w =  Math.floor( w );

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
