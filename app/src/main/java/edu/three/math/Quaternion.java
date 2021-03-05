package edu.three.math;

import edu.three.core.IUpdate;

public class Quaternion {
    double x,y,z,w;
    IUpdate _onChangeCallback = null;

    void update() {
        if (_onChangeCallback != null)
            _onChangeCallback.updated();
    }

    public Quaternion() {
        w = 1;
    }
    public Quaternion(double x, double y, double z, double w) {
        set(x, y, z, w);
    }
    public Quaternion copy(Quaternion quat) {
        x = quat.x;
        y = quat.y;
        z = quat.z;
        w = quat.w;
        update();
        return this;
    }
    public Quaternion set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        update();
        return this;
    }
    public Quaternion clone() {
        return new Quaternion(x, y, z, w);
    }
    public Quaternion setFromAxisAngle(Vector3 axis, double angle) {
        double halfAngle = angle / 2, s = Math.sin(halfAngle);
        x = axis.x * s;
        y = axis.y * s;
        z = axis.z * s;
        w = Math.cos(halfAngle);
        update();
        return this;
    }

    public double dot(Quaternion quat) {
        return x * quat.x + y * quat.y + z * quat.z + w * quat.w;
    }

    public Quaternion conjugate() {
        x *= -1;
        y *= -1;
        z *= -1;
        update();
        return this;
    }

    public Quaternion inverse() {
        return conjugate();
    }

    public double lengthSq() {
        return x * x + y * y + z * z + w * w;
    }
    public double length() {
        return Math.sqrt(lengthSq());
    }
    public Quaternion normalize() {
        double len = length();
        if (len == 0) {
            x = y = z = 0;
            w = 1;
        } else {
            len = 1 / len;
            x *= len;
            y *= len;
            z *= len;
            w *= len;
        }
        return this;
    }

    public Quaternion setFromEuler(Euler euler) {
        return setFromEuler(euler, false);
    }

    public Quaternion setFromEuler(Euler euler, boolean update) {
        double x = euler._x, y = euler._y, z = euler._z;
        String order = euler._order;

        // http://www.mathworks.com/matlabcentral/fileexchange/
        // 	20696-function-to-convert-between-dcm-euler-angles-quaternions-and-euler-vectors/
        //	content/SpinCalc.m
        double c1 =  Math.cos( x / 2 );
        double c2 =  Math.cos( y / 2 );
        double c3 =  Math.cos( z / 2 );

        double s1 =  Math.sin( x / 2 );
        double s2 =  Math.sin( y / 2 );
        double s3 =  Math.sin( z / 2 );
        switch (order) {
            case "XYZ":
                this.x = s1 * c2 * c3 + c1 * s2 * s3;
                this.y = c1 * s2 * c3 - s1 * c2 * s3;
                this.z = c1 * c2 * s3 + s1 * s2 * c3;
                this.w = c1 * c2 * c3 - s1 * s2 * s3;
                break;
            case "YXZ":
                this.x = s1 * c2 * c3 + c1 * s2 * s3;
                this.y = c1 * s2 * c3 - s1 * c2 * s3;
                this.z = c1 * c2 * s3 - s1 * s2 * c3;
                this.w = c1 * c2 * c3 + s1 * s2 * s3;
                break;

            case "ZXY":
                this.x = s1 * c2 * c3 - c1 * s2 * s3;
                this.y = c1 * s2 * c3 + s1 * c2 * s3;
                this.z = c1 * c2 * s3 + s1 * s2 * c3;
                this.w = c1 * c2 * c3 - s1 * s2 * s3;
                break;

            case "ZYX":
                this.x = s1 * c2 * c3 - c1 * s2 * s3;
                this.y = c1 * s2 * c3 + s1 * c2 * s3;
                this.z = c1 * c2 * s3 - s1 * s2 * c3;
                this.w = c1 * c2 * c3 + s1 * s2 * s3;
                break;

            case "YZX":
                this.x = s1 * c2 * c3 + c1 * s2 * s3;
                this.y = c1 * s2 * c3 + s1 * c2 * s3;
                this.z = c1 * c2 * s3 - s1 * s2 * c3;
                this.w = c1 * c2 * c3 - s1 * s2 * s3;
                break;

            case "XZY":
                this.x = s1 * c2 * c3 - c1 * s2 * s3;
                this.y = c1 * s2 * c3 - s1 * c2 * s3;
                this.z = c1 * c2 * s3 + s1 * s2 * c3;
                this.w = c1 * c2 * c3 + s1 * s2 * s3;
                break;
            default:
                break;
        }
        if (update)
            update();
        return this;
    }

    public Quaternion setFromRotationMatrix(Matrix4 m) {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)

        double[] te = m.te;
        double m11 = te[ 0 ], m12 = te[ 4 ], m13 = te[ 8 ],
                m21 = te[ 1 ], m22 = te[ 5 ], m23 = te[ 9 ],
                m31 = te[ 2 ], m32 = te[ 6 ], m33 = te[ 10 ],
                trace = m11 + m22 + m33,
                s;

        if ( trace > 0 ) {
            s = 0.5f /  Math.sqrt( trace + 1.0 );
            w = 0.25f / s;
            x = ( m32 - m23 ) * s;
            y = ( m13 - m31 ) * s;
            z = ( m21 - m12 ) * s;
        } else if ( m11 > m22 && m11 > m33 ) {
            s = 2.0f *  Math.sqrt( 1.0 + m11 - m22 - m33 );
            w = ( m32 - m23 ) / s;
            x = 0.25f * s;
            y = ( m12 + m21 ) / s;
            z = ( m13 + m31 ) / s;
        } else if ( m22 > m33 ) {
            s = 2.0f *  Math.sqrt( 1.0 + m22 - m11 - m33 );
            w = ( m13 - m31 ) / s;
            x = ( m12 + m21 ) / s;
            y = 0.25f * s;
            z = ( m23 + m32 ) / s;
        } else {
            s = 2.0f *  Math.sqrt( 1.0 + m33 - m11 - m22 );
            w = ( m21 - m12 ) / s;
            x = ( m13 + m31 ) / s;
            y = ( m23 + m32 ) / s;
            z = 0.25f * s;
        }
        update();
        return this;
    }

    public Quaternion setFromUnitVectors(Vector3 vFrom, Vector3 vTo) {
        double EPS = 0.000001f;
        double r = vFrom.dot(vTo) + 1;
        if (r < EPS) {
            r = 0;
            if (Math.abs(vFrom.x) > Math.abs(vFrom.z)) {
                this.x = - vFrom.y;
                this.y = vFrom.x;
                this.z = 0;
                this.w = r;
            } else {
                this.x = 0;
                this.y = -vFrom.z;
                this.z = vFrom.y;
                this.w = r;
            }
        } else {
            this.x = vFrom.y * vTo.z - vFrom.z * vTo.y;
            this.y = vFrom.z * vTo.x - vFrom.x * vTo.z;
            this.z = vFrom.x * vTo.y - vFrom.y * vTo.x;
            this.w = r;
        }
        return this.normalize();
    }

    public Quaternion multiplyQuaternions(Quaternion a, Quaternion b) {
        // from http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/code/index.htm

        double qax = a.x, qay = a.y, qaz = a.z, qaw = a.w;
        double qbx = b.x, qby = b.y, qbz = b.z, qbw = b.w;

        x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby;
        y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz;
        z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx;
        w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz;

        update();
        return this;
    }

    public Quaternion multiply(Quaternion q) {
        multiplyQuaternions(this, q);
        update();
        return this;
    }

    public Quaternion premultiply(Quaternion q) {
        return multiplyQuaternions(q, this);
    }

    public boolean equals(Quaternion quaternion) {
        return quaternion.x == x && quaternion.y == y
                && quaternion.z == z && quaternion.w == w;
    }

    public Quaternion fromArray(double[] array) {
        return fromArray(array, 0);
    }
    public Quaternion fromArray(double[] array, int offset) {
        x = array[ offset ];
        y = array[ offset + 1 ];
        z = array[ offset + 2 ];
        w = array[ offset + 3 ];

        return this;
    }

    public double[] toArray() {
        double[] arr = new double[] {x, y, z, w};
        return arr;
    }

    public void onChange(IUpdate callback) {
        _onChangeCallback = callback;
    }

    public void x(double val) {
        x = val;
        update();
    }
    public double x() {
        return x;
    }

    public void y(double val) {
        y = val;
        update();
    }
    public double y() {
        return y;
    }

    public void z(double val) {
        z = val;
        update();
    }
    public double z() {
        return z;
    }
}
