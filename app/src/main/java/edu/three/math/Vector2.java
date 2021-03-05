package edu.three.math;

import edu.three.core.BufferAttribute;

public class Vector2 {
  public double x, y;

  public Vector2() {

  }

  public Vector2(double x, double y) {
    set(x, y);
  }

  public Vector2 set(double x, double y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public double get(char field) {
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

  public boolean equals(Vector2 v) {
    return ( v.x == x ) && ( v.y == y );
  }

  public Vector2 fromBufferAttribute(BufferAttribute attribute, int index) {
    x = attribute.getX(index);
    y = attribute.getY(index);

    return this;
  }

  public double distanceTo(Vector2 v) {
    return Math.sqrt(distanceToSquared(v));
  }

  public double distanceToSquared(Vector2 v) {
    double dx = x - v.x, dy = y - v.y;
    return dx * dx + dy * dy;
  }

  public double manhattanDistanceTo(Vector2 v) {
    return Math.abs( x - v.x ) + Math.abs( y - v.y );
  }

  public Vector2 add(Vector2 v) {
    return addVectors(this, v);
  }

  public Vector2 addVectors(Vector2 a, Vector2 b) {
    this.x = a.x + b.x;
    this.y = a.y + b.y;
    return this;
  }

  public Vector2 addScalar(double scalar) {
    this.x += scalar;
    this.y += scalar;
    return this;
  }

  public Vector2 multiply(Vector2 v) {
    this.x *= v.x;
    this.y *= v.y;
    return this;
  }

  public Vector2 addScaledVector(Vector2 v, double s) {
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

  public double lengthSq() {
    return x * x + y * y;
  }

  public double length() {
    return (double) Math.sqrt(lengthSq());
  }

  public Vector2 multiplyScalar(double scalar) {
    this.x *= scalar;
    this.y *= scalar;
    return this;
  }

  public Vector2 divideScalar(double scalar) {
    return multiplyScalar(1 / scalar);
  }

  public Vector2 applyMatrix3(Matrix3 m) {
    double x = this.x, y = this.y;
    double[] e = m.te;

    this.x = e[0] * x + e[3] * y + e[6];
    this.y = e[1] * x + e[4] * y + e[7];

    return this;
  }

  public Vector2 min(Vector2 v) {
    x = Math.min(x, v.x);
    y = Math.min(y, v.y);
    return this;
  }

  public Vector2 max(Vector2 v) {
    x = Math.max(x, v.x);
    y = Math.max(y, v.y);
    return this;
  }

  public Vector2 clamp(Vector2 min, Vector2 max) {
    // assumes min < max, componentwise
    x = Math.max(min.x, Math.min(max.x, x));
    y = Math.max(min.y, Math.min(max.y, y));

    return this;
  }

  public Vector2 normalize() {
    double len = length();
    if (len == 0) len = 1;
    return divideScalar(len);
  }

  public double dot(Vector2 v) {
    return x * v.x + y * v.y;
  }

  public Vector2 setLength(double length) {
    return normalize().multiplyScalar(length);
  }

  public String toString() {
    return x + " " + y;
  }

  public Vector2 floor() {
    x = (double) Math.floor(x);
    y = (double) Math.floor(y);

    return this;
  }

  public double[] toArray(double[] array, int offset) {
    if (array == null) {
      array = new double[2];
    }
    array[offset] = x;
    array[offset + 1] = y;
    return array;
  }

  public float[] toArrayF(float[] array, int offset) {
    if (array == null) {
      array = new float[2];
    }
    array[offset] = (float)x;
    array[offset + 1] = (float)y;
    return array;
  }

  public int[] toIntArray() {
    int[] ret = new int[2];
    ret[0] = (int) x;
    ret[1] = (int) y;
    return ret;
  }

  public double angleTo(Vector2 vector) {
    double angle1 = MathTool.getAngle(vector.x, vector.y);
    double angle2 = MathTool.getAngle(x, y);
    double target = angle1 - angle2;
    if (target > 180) {
      target -= 360;
    } else if (target < -180) {
      target += 360;
    }
    return target;
  }
}
