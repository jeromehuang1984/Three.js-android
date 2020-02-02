package edu.three.math;

import edu.three.core.BufferAttribute;

public class Vector2 {
    public float x,y;

    public Vector2() {

    }
    public Vector2(float x, float y) {
        set(x, y);
    }

    public Vector2 set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public float get(char field) {
        if ('x' == field) {
            return x;
        } else if ('y' == field) {
            return y;
        }
        return 0;
    }

    public Vector2 negate() {
        x = -x;
        y = -y;
        return this;
    }

    public Vector2 copy(Vector2 v) {
        x = v.x;
        y = v.y;
        return this;
    }
    public Vector2 clone() {
        return new Vector2(x, y);
    }

    public Vector2 fromBufferAttribute(BufferAttribute attribute, int index) {
        x = attribute.getX( index );
        y = attribute.getY( index );

        return this;
    }

    public Vector2 add(Vector2 v) {
        return addVectors(this, v);
    }
    public Vector2 addVectors(Vector2 a, Vector2 b) {
        this.x = a.x + b.x;
        this.y = a.y + b.y;
        return this;
    }

    public Vector2 addScalar(float scalar) {
        this.x += scalar;
        this.y += scalar;
        return this;
    }

    public Vector2 multiply(Vector2 v) {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    public Vector2 addScaledVector(Vector2 v, float s) {
        this.x += v.x * s;
        this.y += v.y * s;

        return this;
    }

    public Vector2 sub(Vector2 v) {
        return subVectors(this, v);
    }
    public Vector2 subVectors(Vector2 a, Vector2 b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        return this;
    }
    public float lengthSq() {
        return x * x + y * y;
    }
    public float length() {
        return (float)Math.sqrt(lengthSq());
    }

    public Vector2 multiplyScalar(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }
    public Vector2 divideScalar(float scalar) {
        return multiplyScalar(1 / scalar);
    }

    public Vector2 applyMatrix3(Matrix3 m) {
        float x = this.x, y = this.y;
        float[] e = m.te;

        this.x = e[ 0 ] * x + e[ 3 ] * y + e[ 6 ];
        this.y = e[ 1 ] * x + e[ 4 ] * y + e[ 7 ];

        return this;
    }

    public Vector2 min(Vector2 v) {
        x = Math.min(x, v.x);
        y = Math.min(y, v.y);
        return this;
    }

    public Vector2 max(Vector2 v) {
        x = Math.max( x, v.x );
        y = Math.max( y, v.y );
        return this;
    }

    public Vector2 clamp(Vector2 min, Vector2 max) {
        // assumes min < max, componentwise
        x = Math.max( min.x, Math.min( max.x, x ) );
        y = Math.max( min.y, Math.min( max.y, y ) );

        return this;
    }
    public Vector2 normalize() {
        float len = length();
        if (len == 0) len = 1;
        return divideScalar(len);
    }

    public Vector2 setLength(float length) {
        return normalize().multiplyScalar(length);
    }

    public String toString() {
        return x + " " + y;
    }

    public Vector2 floor() {
        x = (float) Math.floor( x );
        y = (float) Math.floor( y );

        return this;
    }

    public float[] toArray(float[] array, int offset) {
        if (array == null) {
            array = new float[2];
        }
        array[offset] = x;
        array[offset + 1] = y;
        return array;
    }

    public int[] toIntArray() {
        int[] ret = new int[2];
        ret[0] = (int) x;
        ret[1] = (int) y;
        return ret;
    }
}
