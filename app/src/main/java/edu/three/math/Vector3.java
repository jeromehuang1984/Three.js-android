package edu.three.math;

import edu.three.cameras.Camera;
import edu.three.core.BufferAttribute;

public class Vector3 {
    public double x,y,z;

    public Vector3() {

    }
    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vector3 setScalar(double scalar) {
        this.x = scalar;
        this.y = scalar;
        this.z = scalar;
        return this;
    }
    public Vector3 addScalar(double scalar) {
        this.x += scalar;
        this.y += scalar;
        this.z += scalar;
        return this;
    }
    public Vector3 add(Vector3 v) {
        return addVectors(this, v);
    }
    public Vector3 addVectors(Vector3 a, Vector3 b) {
        this.x = a.x + b.x;
        this.y = a.y + b.y;
        this.z = a.z + b.z;
        return this;
    }
    public Vector3 sub(Vector3 v) {
        return subVectors(this, v);
    }
    public Vector3 subVectors(Vector3 a, Vector3 b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        this.z = a.z - b.z;
        return this;
    }
    public Vector3 multiply(Vector3 v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        return this;
    }
    public Vector3 multiplyScalar(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        return this;
    }
    public Vector3 divideScalar(double scalar) {
        return multiplyScalar(1 / scalar);
    }
    public Vector3 normalize() {
        double len = length();
        if (len == 0) len = 1;
        return divideScalar(len);
    }
    public Vector3 round() {
        x = Math.round(x);
        y = Math.round(y);
        z = Math.round(z);
        return this;
    }
    public Vector3 setLength(double length) {
        return normalize().multiplyScalar(length);
    }
    public Vector3 multiplyVectors(Vector3 a, Vector3 b) {
        this.x = a.x * b.x;
        this.y = a.y * b.y;
        this.z = a.z * b.z;
        return this;
    }
    public Vector3 lerp(Vector3 v, double alpha) {
        x += (v.x - x) * alpha;
        y += (v.y - y) * alpha;
        z += (v.z - z) * alpha;
        return this;
    }
    public double dot(Vector3 v) {
        return x * v.x + y * v.y + z * v.z;
    }
    public Vector3 crossVectors(Vector3 a, Vector3 b) {
        double ax = a.x, ay = a.y, az = a.z;
        double bx = b.x, by = b.y, bz = b.z;
        x = ay * bz - az * by;
        y = az * bx - ax * bz;
        z = ax * by - ay * bx;
        return this;
    }
    public Vector3 cross(Vector3 v) {
        return crossVectors(this, v);
    }
    public double distanceToSquared(Vector3 v) {
        double dx = x - v.x, dy = y - v.y, dz = z - v.z;
        return dx * dx + dy * dy + dz * dz;
    }
    public double distanceTo(Vector3 v) {
        return Math.sqrt(distanceToSquared(v));
    }
    public double manhattanDistanceTo(Vector3 v) {
        return Math.abs( x - v.x ) + Math.abs( y - v.y ) + Math.abs( z - v.z );
    }
    public double lengthSq() {
        return x * x + y * y + z * z;
    }
    public double length() {
        return (double)Math.sqrt(lengthSq());
    }
    public Vector3 negate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }
    public Vector3 set(double vx, double vy, double vz) {
        x = vx;
        y = vy;
        z = vz;
        return this;
    }
    public Vector3 copy(Vector3 v) {
        x = v.x;
        y = v.y;
        z = v.z;
        return this;
    }
    public Vector3 clone() {
        return new Vector3(x, y, z);
    }
    public Vector3 applyQuaternion(Quaternion q) {
        double x = this.x, y = this.y, z = this.z;
        double qx = q.x, qy = q.y, qz = q.z, qw = q.w;

        // calculate quat * vector

        double ix = qw * x + qy * z - qz * y;
        double iy = qw * y + qz * x - qx * z;
        double iz = qw * z + qx * y - qy * x;
        double iw = - qx * x - qy * y - qz * z;

        // calculate result * inverse quat

        this.x = ix * qw + iw * - qx + iy * - qz - iz * - qy;
        this.y = iy * qw + iw * - qy + iz * - qx - ix * - qz;
        this.z = iz * qw + iw * - qz + ix * - qy - iy * - qx;

        return this;
    }

    public Vector3 min(Vector3 v) {
        x = Math.min(x, v.x);
        y = Math.min(y, v.y);
        z = Math.min(z, v.z);
        return this;
    }

    public Vector3 max(Vector3 v) {
        x = Math.max(x, v.x);
        y = Math.max(y, v.y);
        z = Math.max(z, v.z);
        return this;
    }

    public Vector3 applyMatrix4(Matrix4 m) {
        double x0 = this.x, y0 = this.y, z0 = this.z;
        double[] e = m.te;
        double w = 1 / ( e[ 3 ] * x0 + e[ 7 ] * y + e[ 11 ] * z + e[ 15 ] );

        x = ( e[ 0 ] * x0 + e[ 4 ] * y0 + e[ 8 ] * z0 + e[ 12 ] ) * w;
        y = ( e[ 1 ] * x0 + e[ 5 ] * y0 + e[ 9 ] * z0 + e[ 13 ] ) * w;
        z = ( e[ 2 ] * x0 + e[ 6 ] * y0 + e[ 10 ] * z0 + e[ 14 ] ) * w;
        return this;
    }

    public Vector3 applyMatrix3(Matrix3 m) {
        double x = this.x, y = this.y, z = this.z;
        double[] e = m.te;

        this.x = e[ 0 ] * x + e[ 3 ] * y + e[ 6 ] * z;
        this.y = e[ 1 ] * x + e[ 4 ] * y + e[ 7 ] * z;
        this.z = e[ 2 ] * x + e[ 5 ] * y + e[ 8 ] * z;

        return this;
    }
    //addScaledVector
    public Vector3 addScaledVector(Vector3 v, double s) {
        return addScale(v, s);
    }
    public Vector3 addScale(Vector3 v, double s) {
        this.x += v.x * s;
        this.y += v.y * s;
        this.z += v.z * s;

        return this;
    }

    public Vector3 transformDirection(Matrix4 m) {
        // input: THREE.Matrix4 affine matrix
        // vector interpreted as a direction
        double[] e = m.te;
        double x0 = this.x, y0 = this.y, z0 = this.z;

        this.x = e[ 0 ] * x0 + e[ 4 ] * y0 + e[ 8 ] * z0;
        this.y = e[ 1 ] * x0 + e[ 5 ] * y0 + e[ 9 ] * z0;
        this.z = e[ 2 ] * x0 + e[ 6 ] * y0 + e[ 10 ] * z0;

        return normalize();
    }

    public Vector2 to() {
        return new Vector2(x, y);
    }

    public Vector3 from(Vector2 v) {
        x = v.x;
        y = v.y;
        return this;
    }

    public Vector3 setFromMatrixColumn(Matrix4 m, int index) {
        return fromArray(m.te, index * 4);
    }

    public Vector3 setFromMatrixPosition(Matrix4 m) {
        x = m.te[12];
        y = m.te[13];
        z = m.te[14];
        return this;
    }

    public Vector3 setFromMatrixScale(Matrix4 m) {
        double sx = this.setFromMatrixColumn( m, 0 ).length();
        double sy = this.setFromMatrixColumn( m, 1 ).length();
        double sz = this.setFromMatrixColumn( m, 2 ).length();

        this.x = sx;
        this.y = sy;
        this.z = sz;

        return this;
    }

    public Vector3 setFromSpherical(Spherical s) {
        return setFromSphericalCoords(s.radius, s.phi, s.theta);
    }

    public Vector3 setFromSphericalCoords(double radius, double phi, double theta) {
        double sinPhiRadius = (double)Math.sin(phi) * radius;
        this.x = sinPhiRadius * (double)Math.sin( theta );
        this.y = (double)Math.cos( phi ) * radius;
        this.z = sinPhiRadius * (double)Math.cos( theta );

        return this;
    }
    public Vector3 setFromSpherical2(Spherical2 s) {
        double sinPhiRadius = (double)Math.sin(s.phi) * s.radius;
        this.x = sinPhiRadius * (double)Math.sin( s.theta );
        this.z = (double)Math.cos( s.phi ) * s.radius;
        this.y = sinPhiRadius * (double)Math.cos( s.theta );
        return this;
    }

    public Vector3 fromBufferAttribute(BufferAttribute attribute, int index) {
        x = attribute.getX(index);
        y = attribute.getY(index);
        z = attribute.getZ(index);
        return this;
    }

    public Vector3 fromArray(double[] array) {
        return fromArray(array, 0);
    }
    public Vector3 fromArray(double[] array, int offset) {
        x = array[ offset ];
        y = array[ offset + 1 ];
        z = array[ offset + 2 ];
        return this;
    }

    public Vector3 fromArray(float[] array) {
        return fromArray(array, 0);
    }
    public Vector3 fromArray(float[] array, int offset) {
        x = array[ offset ];
        y = array[ offset + 1 ];
        z = array[ offset + 2 ];
        return this;
    }

    public double get(char field) {
        if ('x' == field) {
            return x;
        } else if ('y' == field) {
            return y;
        } else if ('z' == field) {
            return z;
        }
        return 0;
    }

    public void setField(char field, double n) {
        if ('x' == field) {
            x = n;
        } else if ('y' == field) {
            y = n;
        } else if ('z' == field) {
            z = n;
        }
    }

    public Vector3 project(Camera camera) {
        return applyMatrix4(camera.matrixWorldInverse).applyMatrix4(camera.projectionMatrix);
    }

    public Vector3 unproject(Camera camera) {
        return applyMatrix4(camera.projectionMatrixInverse).applyMatrix4(camera.getWorldMatrix());
    }

    // reflect incident vector off plane orthogonal to normal
    // normal is assumed to have unit length
    public Vector3 reflect(Vector3 normal) {
        Vector3 v1 = new Vector3();
        return sub(v1.copy(normal).multiplyScalar(2 * dot(normal)));
    }

    public double angleTo(Vector3 v) {
        double theta = dot(v) / ( length() * v.length() );
        // clamp, to handle numerical problems
        return (double) Math.acos( MathTool.clamp( theta, - 1, 1 ) );
    }

    public String toString() {
        return x + " " + y + " " + z;
    }

    public boolean equals(Vector3 v) {
        return ( ( v.x == x ) && ( v.y == y ) && ( v.z == z ) );
    }

    public double[] toArray(double[] array, int offset) {
        if (array == null) {
            array = new double[3];
        }
        array[offset] = x;
        array[offset + 1] = y;
        array[offset + 2] = z;
        return array;
    }

    public float[] toArrayF(float[] array, int offset) {
        if (array == null) {
            array = new float[3];
        }
        array[offset] = (float)x;
        array[offset + 1] = (float)y;
        array[offset + 2] = (float)z;
        return array;
    }

    public int[] toIntArray() {
        int[] ret = new int[3];
        ret[0] = (int) x;
        ret[1] = (int) y;
        ret[2] = (int) z;
        return ret;
    }

//    public String toFixed(int num) {}
}
