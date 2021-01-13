package edu.three.math;

public class Quaternion {
    public float x,y,z,w;
    public Quaternion() {
        w = 1;
    }
    public Quaternion(float x, float y, float z, float w) {
        set(x, y, z, w);
    }
    public Quaternion copy(Quaternion quat) {
        x = quat.x;
        y = quat.y;
        z = quat.z;
        w = quat.w;
        return this;
    }
    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }
    public Quaternion clone() {
        return new Quaternion(x, y, z, w);
    }
    public Quaternion setFromAxisAngle(Vector3 axis, float angle) {
        float halfAngle = angle / 2, s = (float)Math.sin(halfAngle);
        x = axis.x * s;
        y = axis.y * s;
        z = axis.z * s;
        w = (float)Math.cos(halfAngle);
        return this;
    }

    public float dot(Quaternion quat) {
        return x * quat.x + y * quat.y + z * quat.z + w * quat.w;
    }

    public Quaternion conjugate() {
        x *= -1;
        y *= -1;
        z *= -1;
        return this;
    }

    public Quaternion inverse() {
        return conjugate();
    }

    public float lengthSq() {
        return x * x + y * y + z * z + w * w;
    }
    public float length() {
        return (float)Math.sqrt(lengthSq());
    }
    public Quaternion normalize() {
        float len = length();
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

    public Quaternion setFromRotationMatrix(Matrix4 m) {
        // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
        // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)

        float[] te = m.te;
        float m11 = te[ 0 ], m12 = te[ 4 ], m13 = te[ 8 ],
                m21 = te[ 1 ], m22 = te[ 5 ], m23 = te[ 9 ],
                m31 = te[ 2 ], m32 = te[ 6 ], m33 = te[ 10 ],
                trace = m11 + m22 + m33,
                s;

        if ( trace > 0 ) {
            s = 0.5f / (float) Math.sqrt( trace + 1.0 );
            w = 0.25f / s;
            x = ( m32 - m23 ) * s;
            y = ( m13 - m31 ) * s;
            z = ( m21 - m12 ) * s;
        } else if ( m11 > m22 && m11 > m33 ) {
            s = 2.0f * (float) Math.sqrt( 1.0 + m11 - m22 - m33 );
            w = ( m32 - m23 ) / s;
            x = 0.25f * s;
            y = ( m12 + m21 ) / s;
            z = ( m13 + m31 ) / s;
        } else if ( m22 > m33 ) {
            s = 2.0f * (float) Math.sqrt( 1.0 + m22 - m11 - m33 );
            w = ( m13 - m31 ) / s;
            x = ( m12 + m21 ) / s;
            y = 0.25f * s;
            z = ( m23 + m32 ) / s;
        } else {
            s = 2.0f * (float) Math.sqrt( 1.0 + m33 - m11 - m22 );
            w = ( m21 - m12 ) / s;
            x = ( m13 + m31 ) / s;
            y = ( m23 + m32 ) / s;
            z = 0.25f * s;
        }

        return this;
    }

    public Quaternion setFromUnitVectors(Vector3 vFrom, Vector3 vTo) {
        float EPS = 0.000001f;
        float r = vFrom.dot(vTo) + 1;
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

        float qax = a.x, qay = a.y, qaz = a.z, qaw = a.w;
        float qbx = b.x, qby = b.y, qbz = b.z, qbw = b.w;

        x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby;
        y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz;
        z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx;
        w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz;

        return this;
    }

    public Quaternion multiply(Quaternion q) {
        return multiplyQuaternions(this, q);
    }

    public Quaternion premultiply(Quaternion q) {
        return multiplyQuaternions(q, this);
    }

    public boolean equals(Quaternion quaternion) {
        return quaternion.x == x && quaternion.y == y
                && quaternion.z == z && quaternion.w == w;
    }

    public Quaternion fromArray(float[] array) {
        return fromArray(array, 0);
    }
    public Quaternion fromArray(float[] array, int offset) {
        x = array[ offset ];
        y = array[ offset + 1 ];
        z = array[ offset + 2 ];
        w = array[ offset + 3 ];

        return this;
    }

    public float[] toArray() {
        float[] arr = new float[] {x, y, z, w};
        return arr;
    }
}
