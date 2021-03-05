package edu.three.math;

import android.util.Log;

import edu.three.core.BufferAttribute;
import edu.three.exeception.DegenerateException;

public class Matrix4 {
    private Vector3 _v1 = new Vector3();
    public double[] te = new double[] {
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1};

    public Matrix4 set(double n11, double n12, double n13, double n14,
                       double n21, double n22, double n23, double n24,
                       double n31, double n32, double n33, double n34,
                       double n41, double n42, double n43, double n44) {
        te[ 0 ] = n11; te[ 4 ] = n12; te[ 8 ] = n13; te[ 12 ] = n14;
        te[ 1 ] = n21; te[ 5 ] = n22; te[ 9 ] = n23; te[ 13 ] = n24;
        te[ 2 ] = n31; te[ 6 ] = n32; te[ 10 ] = n33; te[ 14 ] = n34;
        te[ 3 ] = n41; te[ 7 ] = n42; te[ 11 ] = n43; te[ 15 ] = n44;

        return this;
    }

    public Matrix4 identity() {
        return set(
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    public Matrix4 fromArray(double[] array) {
        return fromArray(array, 0);
    }
    public Matrix4 fromArray(double[] array, int offset) {
        for ( int i = 0; i < 16; i ++ ) {
            te[ i ] = array[ i + offset ];
        }
        return this;
    }

    public Matrix4 clone() {
        return new Matrix4().fromArray(te, 0);
    }

    public Matrix4 copy(Matrix4 m) {
        
        double[] me = m.te;
        te[ 0 ] = me[ 0 ]; te[ 1 ] = me[ 1 ]; te[ 2 ] = me[ 2 ]; te[ 3 ] = me[ 3 ];
        te[ 4 ] = me[ 4 ]; te[ 5 ] = me[ 5 ]; te[ 6 ] = me[ 6 ]; te[ 7 ] = me[ 7 ];
        te[ 8 ] = me[ 8 ]; te[ 9 ] = me[ 9 ]; te[ 10 ] = me[ 10 ]; te[ 11 ] = me[ 11 ];
        te[ 12 ] = me[ 12 ]; te[ 13 ] = me[ 13 ]; te[ 14 ] = me[ 14 ]; te[ 15 ] = me[ 15 ];

        return this;
    }

    public double determinant() {
        
        double n11 = te[ 0 ], n12 = te[ 4 ], n13 = te[ 8 ], n14 = te[ 12 ];
        double n21 = te[ 1 ], n22 = te[ 5 ], n23 = te[ 9 ], n24 = te[ 13 ];
        double n31 = te[ 2 ], n32 = te[ 6 ], n33 = te[ 10 ], n34 = te[ 14 ];
        double n41 = te[ 3 ], n42 = te[ 7 ], n43 = te[ 11 ], n44 = te[ 15 ];

        //TODO: make this more efficient
        //( based on http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/fourD/index.htm )
        return (
                n41 * (
                        +n14 * n23 * n32
                                - n13 * n24 * n32
                                - n14 * n22 * n33
                                + n12 * n24 * n33
                                + n13 * n22 * n34
                                - n12 * n23 * n34
                ) +
                        n42 * (
                                +n11 * n23 * n34
                                        - n11 * n24 * n33
                                        + n14 * n21 * n33
                                        - n13 * n21 * n34
                                        + n13 * n24 * n31
                                        - n14 * n23 * n31
                        ) +
                        n43 * (
                                +n11 * n24 * n32
                                        - n11 * n22 * n34
                                        - n14 * n21 * n32
                                        + n12 * n21 * n34
                                        + n14 * n22 * n31
                                        - n12 * n24 * n31
                        ) +
                        n44 * (
                                -n13 * n22 * n31
                                        - n11 * n23 * n32
                                        + n11 * n22 * n33
                                        + n13 * n21 * n32
                                        - n12 * n21 * n33
                                        + n12 * n23 * n31
                        )

        );
    }

    public Matrix4 transpose() {
        double[] te = this.te;
        double tmp;

        tmp = te[ 1 ]; te[ 1 ] = te[ 4 ]; te[ 4 ] = tmp;
        tmp = te[ 2 ]; te[ 2 ] = te[ 8 ]; te[ 8 ] = tmp;
        tmp = te[ 6 ]; te[ 6 ] = te[ 9 ]; te[ 9 ] = tmp;

        tmp = te[ 3 ]; te[ 3 ] = te[ 12 ]; te[ 12 ] = tmp;
        tmp = te[ 7 ]; te[ 7 ] = te[ 13 ]; te[ 13 ] = tmp;
        tmp = te[ 11 ]; te[ 11 ] = te[ 14 ]; te[ 14 ] = tmp;
        return this;
    }

    public Matrix4 getInverse(Matrix4 m) {
        try {
            return getInverse(m, false);
        } catch (DegenerateException e) {
            e.printStackTrace();
        }
        return new Matrix4();
    }
    public Matrix4 getInverse(Matrix4 m, boolean throwOnDegenerate) throws DegenerateException {
        double[] te = this.te;
        double[] me = m.te;
        double n11 = me[ 0 ], n21 = me[ 1 ], n31 = me[ 2 ], n41 = me[ 3 ],
                n12 = me[ 4 ], n22 = me[ 5 ], n32 = me[ 6 ], n42 = me[ 7 ],
                n13 = me[ 8 ], n23 = me[ 9 ], n33 = me[ 10 ], n43 = me[ 11 ],
                n14 = me[ 12 ], n24 = me[ 13 ], n34 = me[ 14 ], n44 = me[ 15 ],

                t11 = n23 * n34 * n42 - n24 * n33 * n42 + n24 * n32 * n43 - n22 * n34 * n43 - n23 * n32 * n44 + n22 * n33 * n44,
                t12 = n14 * n33 * n42 - n13 * n34 * n42 - n14 * n32 * n43 + n12 * n34 * n43 + n13 * n32 * n44 - n12 * n33 * n44,
                t13 = n13 * n24 * n42 - n14 * n23 * n42 + n14 * n22 * n43 - n12 * n24 * n43 - n13 * n22 * n44 + n12 * n23 * n44,
                t14 = n14 * n23 * n32 - n13 * n24 * n32 - n14 * n22 * n33 + n12 * n24 * n33 + n13 * n22 * n34 - n12 * n23 * n34;

        double det = n11 * t11 + n21 * t12 + n31 * t13 + n41 * t14;
        if (det == 0) {
            String msg = "THREE.Matrix4: .getInverse() can't invert matrix, determinant is 0";
            if (throwOnDegenerate) {
                throw new DegenerateException(msg);
            } else {
                Log.w("THREE.Matrix4", msg);
            }
            return identity();
        }

        double detInv = 1 / det;
        te[ 0 ] = t11 * detInv;
        te[ 1 ] = ( n24 * n33 * n41 - n23 * n34 * n41 - n24 * n31 * n43 + n21 * n34 * n43 + n23 * n31 * n44 - n21 * n33 * n44 ) * detInv;
        te[ 2 ] = ( n22 * n34 * n41 - n24 * n32 * n41 + n24 * n31 * n42 - n21 * n34 * n42 - n22 * n31 * n44 + n21 * n32 * n44 ) * detInv;
        te[ 3 ] = ( n23 * n32 * n41 - n22 * n33 * n41 - n23 * n31 * n42 + n21 * n33 * n42 + n22 * n31 * n43 - n21 * n32 * n43 ) * detInv;

        te[ 4 ] = t12 * detInv;
        te[ 5 ] = ( n13 * n34 * n41 - n14 * n33 * n41 + n14 * n31 * n43 - n11 * n34 * n43 - n13 * n31 * n44 + n11 * n33 * n44 ) * detInv;
        te[ 6 ] = ( n14 * n32 * n41 - n12 * n34 * n41 - n14 * n31 * n42 + n11 * n34 * n42 + n12 * n31 * n44 - n11 * n32 * n44 ) * detInv;
        te[ 7 ] = ( n12 * n33 * n41 - n13 * n32 * n41 + n13 * n31 * n42 - n11 * n33 * n42 - n12 * n31 * n43 + n11 * n32 * n43 ) * detInv;

        te[ 8 ] = t13 * detInv;
        te[ 9 ] = ( n14 * n23 * n41 - n13 * n24 * n41 - n14 * n21 * n43 + n11 * n24 * n43 + n13 * n21 * n44 - n11 * n23 * n44 ) * detInv;
        te[ 10 ] = ( n12 * n24 * n41 - n14 * n22 * n41 + n14 * n21 * n42 - n11 * n24 * n42 - n12 * n21 * n44 + n11 * n22 * n44 ) * detInv;
        te[ 11 ] = ( n13 * n22 * n41 - n12 * n23 * n41 - n13 * n21 * n42 + n11 * n23 * n42 + n12 * n21 * n43 - n11 * n22 * n43 ) * detInv;

        te[ 12 ] = t14 * detInv;
        te[ 13 ] = ( n13 * n24 * n31 - n14 * n23 * n31 + n14 * n21 * n33 - n11 * n24 * n33 - n13 * n21 * n34 + n11 * n23 * n34 ) * detInv;
        te[ 14 ] = ( n14 * n22 * n31 - n12 * n24 * n31 - n14 * n21 * n32 + n11 * n24 * n32 + n12 * n21 * n34 - n11 * n22 * n34 ) * detInv;
        te[ 15 ] = ( n12 * n23 * n31 - n13 * n22 * n31 + n13 * n21 * n32 - n11 * n23 * n32 - n12 * n21 * n33 + n11 * n22 * n33 ) * detInv;
        return this;
    }

    public Matrix4 scale(Vector3 v) {
        double x = v.x, y = v.y, z = v.z;
        te[ 0 ] *= x; te[ 4 ] *= y; te[ 8 ] *= z;
        te[ 1 ] *= x; te[ 5 ] *= y; te[ 9 ] *= z;
        te[ 2 ] *= x; te[ 6 ] *= y; te[ 10 ] *= z;
        te[ 3 ] *= x; te[ 7 ] *= y; te[ 11 ] *= z;

        return this;
    }

    public double getMaxScaleOnAxis() {
        double scaleXSq = te[ 0 ] * te[ 0 ] + te[ 1 ] * te[ 1 ] + te[ 2 ] * te[ 2 ];
        double scaleYSq = te[ 4 ] * te[ 4 ] + te[ 5 ] * te[ 5 ] + te[ 6 ] * te[ 6 ];
        double scaleZSq = te[ 8 ] * te[ 8 ] + te[ 9 ] * te[ 9 ] + te[ 10 ] * te[ 10 ];

        return (double) Math.sqrt( Math.max(Math.max( scaleXSq, scaleYSq), scaleZSq ) );
    }

    public Matrix4 makeTranslation(Vector3 v) {
        return makeTranslation(v.x, v.y, v.z);
    }
    public Matrix4 makeTranslation(double x, double y, double z) {
        return set(
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1
        );
    }
    public Matrix4 makeRotationX(double theta) {
        double c = (double) Math.cos( theta ), s = (double) Math.sin( theta );
        this.set(
                1, 0, 0, 0,
                0, c, - s, 0,
                0, s, c, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Matrix4 makeRotationY(double theta) {
        double c = (double) Math.cos( theta ), s = (double) Math.sin( theta );
        this.set(
                c, 0, s, 0,
                0, 1, 0, 0,
                - s, 0, c, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Matrix4 makeRotationZ(double theta) {
        double c = (double) Math.cos( theta ), s = (double) Math.sin( theta );
        this.set(
                c, - s, 0, 0,
                s, c, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
        return this;
    }

    public Matrix4 makeRotationAxis(Vector3 axis, double angle) {
        // Based on http://www.gamedev.net/reference/articles/article1199.asp
        double c = (double) Math.cos( angle );
        double s = (double) Math.sin( angle );
        double t = 1 - c;
        double x = axis.x, y = axis.y, z = axis.z;
        double tx = t * x, ty = t * y;

        this.set(
                tx * x + c, tx * y - s * z, tx * z + s * y, 0,
                tx * y + s * z, ty * y + c, ty * z - s * x, 0,
                tx * z - s * y, ty * z + s * x, t * z * z + c, 0,
                0, 0, 0, 1
        );

        return this;
    }

    public Matrix4 makeScale(double x, double y, double z) {
        return set(

                x, 0, 0, 0,
                0, y, 0, 0,
                0, 0, z, 0,
                0, 0, 0, 1

        );
    }

    public Matrix4 makeShear(double x, double y, double z) {
        return set(

                1, y, z, 0,
                x, 1, z, 0,
                x, y, 1, 0,
                0, 0, 0, 1

        );
    }

    public Matrix4 setPosition(Vector3 x) {
        te[ 12 ] = x.x;
        te[ 13 ] = x.y;
        te[ 14 ] = x.z;
        return this;
    }

    public Matrix4 setPosition(double x, double y, double z) {
        te[ 12 ] = x;
        te[ 13 ] = y;
        te[ 14 ] = z;
        return this;
    }

    public Matrix4 copyPosition(Matrix4 m) {
        double[] me = m.te;
        te[ 12 ] = me[ 12 ];
        te[ 13 ] = me[ 13 ];
        te[ 14 ] = me[ 14 ];
        return this;
    }

    public Matrix4 compose(Vector3 position, Quaternion quaternion, Vector3 scale) {
        

        double x = quaternion.x, y = quaternion.y, z = quaternion.z, w = quaternion.w;
        double x2 = x + x,	y2 = y + y, z2 = z + z;
        double xx = x * x2, xy = x * y2, xz = x * z2;
        double yy = y * y2, yz = y * z2, zz = z * z2;
        double wx = w * x2, wy = w * y2, wz = w * z2;

        double sx = scale.x, sy = scale.y, sz = scale.z;

        te[ 0 ] = ( 1 - ( yy + zz ) ) * sx;
        te[ 1 ] = ( xy + wz ) * sx;
        te[ 2 ] = ( xz - wy ) * sx;
        te[ 3 ] = 0;

        te[ 4 ] = ( xy - wz ) * sy;
        te[ 5 ] = ( 1 - ( xx + zz ) ) * sy;
        te[ 6 ] = ( yz + wx ) * sy;
        te[ 7 ] = 0;

        te[ 8 ] = ( xz + wy ) * sz;
        te[ 9 ] = ( yz - wx ) * sz;
        te[ 10 ] = ( 1 - ( xx + yy ) ) * sz;
        te[ 11 ] = 0;

        te[ 12 ] = position.x;
        te[ 13 ] = position.y;
        te[ 14 ] = position.z;
        te[ 15 ] = 1;

        return this;
    }

    public void decompose(Vector3 position, Quaternion quaternion, Vector3 scale) {
        
        Vector3 vector = new Vector3();
        Matrix4 matrix = new Matrix4();

        double sx = vector.set( te[ 0 ], te[ 1 ], te[ 2 ] ).length();
        double sy = vector.set( te[ 4 ], te[ 5 ], te[ 6 ] ).length();
        double sz = vector.set( te[ 8 ], te[ 9 ], te[ 10 ] ).length();

        // if determine is negative, we need to invert one scale
        double det = this.determinant();
        if ( det < 0 ) sx = - sx;

        position.x = te[ 12 ];
        position.y = te[ 13 ];
        position.z = te[ 14 ];

        // scale the rotation part
        matrix.copy( this );

        double invSX = 1 / sx;
        double invSY = 1 / sy;
        double invSZ = 1 / sz;

        matrix.te[ 0 ] *= invSX;
        matrix.te[ 1 ] *= invSX;
        matrix.te[ 2 ] *= invSX;

        matrix.te[ 4 ] *= invSY;
        matrix.te[ 5 ] *= invSY;
        matrix.te[ 6 ] *= invSY;

        matrix.te[ 8 ] *= invSZ;
        matrix.te[ 9 ] *= invSZ;
        matrix.te[ 10 ] *= invSZ;

        quaternion.setFromRotationMatrix( matrix );

        scale.x = sx;
        scale.y = sy;
        scale.z = sz;
    }

    public Matrix4 makeRotationFromQuaternion(Quaternion q) {
        Vector3 zero = new Vector3( 0, 0, 0 );
        Vector3 one = new Vector3( 1, 1, 1 );
        return compose(zero, q, one);
    }

    public void extraceBasis(Vector3 xAxis, Vector3 yAxis, Vector3 zAxis) {
        xAxis.setFromMatrixColumn(this, 0);
        xAxis.setFromMatrixColumn(this, 1);
        xAxis.setFromMatrixColumn(this, 2);
    }
    public Matrix4 extractRotation(Matrix4 m) {
        Vector3 v1 = new Vector3();
        // this method does not support reflection matrices

        double[] te = this.te;
        double[] me = m.te;

        double scaleX = 1 / v1.setFromMatrixColumn( m, 0 ).length();
        double scaleY = 1 / v1.setFromMatrixColumn( m, 1 ).length();
        double scaleZ = 1 / v1.setFromMatrixColumn( m, 2 ).length();

        te[ 0 ] = me[ 0 ] * scaleX;
        te[ 1 ] = me[ 1 ] * scaleX;
        te[ 2 ] = me[ 2 ] * scaleX;
        te[ 3 ] = 0;

        te[ 4 ] = me[ 4 ] * scaleY;
        te[ 5 ] = me[ 5 ] * scaleY;
        te[ 6 ] = me[ 6 ] * scaleY;
        te[ 7 ] = 0;

        te[ 8 ] = me[ 8 ] * scaleZ;
        te[ 9 ] = me[ 9 ] * scaleZ;
        te[ 10 ] = me[ 10 ] * scaleZ;
        te[ 11 ] = 0;

        te[ 12 ] = 0;
        te[ 13 ] = 0;
        te[ 14 ] = 0;
        te[ 15 ] = 1;

        return this;
    }

    public Matrix4 lookAt(Vector3 eye, Vector3 target, Vector3 up) {
        Vector3 x = new Vector3();
        Vector3 y = new Vector3();
        Vector3 z = new Vector3();

        z.subVectors( eye, target );
        if ( z.lengthSq() == 0 ) {
            // eye and target are in the same position
            z.z = 1;
        }

        z.normalize();
        x.crossVectors( up, z );
        if ( x.lengthSq() == 0 ) {
            // up and z are parallel
            if ( Math.abs( up.z ) == 1 ) {
                z.x += 0.0001;
            } else {
                z.z += 0.0001;
            }

            z.normalize();
            x.crossVectors( up, z );
        }

        x.normalize();
        y.crossVectors( z, x );
        te[ 0 ] = x.x; te[ 4 ] = y.x; te[ 8 ] = z.x;
        te[ 1 ] = x.y; te[ 5 ] = y.y; te[ 9 ] = z.y;
        te[ 2 ] = x.z; te[ 6 ] = y.z; te[ 10 ] = z.z;

        return this;
    }

    public Matrix4 multiplyMatrices(Matrix4 a, Matrix4 b) {
        double[] ae = a.te;
        double[] be = b.te;
        double[] te = this.te;

        double a11 = ae[ 0 ], a12 = ae[ 4 ], a13 = ae[ 8 ], a14 = ae[ 12 ];
        double a21 = ae[ 1 ], a22 = ae[ 5 ], a23 = ae[ 9 ], a24 = ae[ 13 ];
        double a31 = ae[ 2 ], a32 = ae[ 6 ], a33 = ae[ 10 ], a34 = ae[ 14 ];
        double a41 = ae[ 3 ], a42 = ae[ 7 ], a43 = ae[ 11 ], a44 = ae[ 15 ];

        double b11 = be[ 0 ], b12 = be[ 4 ], b13 = be[ 8 ], b14 = be[ 12 ];
        double b21 = be[ 1 ], b22 = be[ 5 ], b23 = be[ 9 ], b24 = be[ 13 ];
        double b31 = be[ 2 ], b32 = be[ 6 ], b33 = be[ 10 ], b34 = be[ 14 ];
        double b41 = be[ 3 ], b42 = be[ 7 ], b43 = be[ 11 ], b44 = be[ 15 ];

        te[ 0 ] = a11 * b11 + a12 * b21 + a13 * b31 + a14 * b41;
        te[ 4 ] = a11 * b12 + a12 * b22 + a13 * b32 + a14 * b42;
        te[ 8 ] = a11 * b13 + a12 * b23 + a13 * b33 + a14 * b43;
        te[ 12 ] = a11 * b14 + a12 * b24 + a13 * b34 + a14 * b44;

        te[ 1 ] = a21 * b11 + a22 * b21 + a23 * b31 + a24 * b41;
        te[ 5 ] = a21 * b12 + a22 * b22 + a23 * b32 + a24 * b42;
        te[ 9 ] = a21 * b13 + a22 * b23 + a23 * b33 + a24 * b43;
        te[ 13 ] = a21 * b14 + a22 * b24 + a23 * b34 + a24 * b44;

        te[ 2 ] = a31 * b11 + a32 * b21 + a33 * b31 + a34 * b41;
        te[ 6 ] = a31 * b12 + a32 * b22 + a33 * b32 + a34 * b42;
        te[ 10 ] = a31 * b13 + a32 * b23 + a33 * b33 + a34 * b43;
        te[ 14 ] = a31 * b14 + a32 * b24 + a33 * b34 + a34 * b44;

        te[ 3 ] = a41 * b11 + a42 * b21 + a43 * b31 + a44 * b41;
        te[ 7 ] = a41 * b12 + a42 * b22 + a43 * b32 + a44 * b42;
        te[ 11 ] = a41 * b13 + a42 * b23 + a43 * b33 + a44 * b43;
        te[ 15 ] = a41 * b14 + a42 * b24 + a43 * b34 + a44 * b44;

        return this;
    }

    public Matrix4 multiply(Matrix4 m) {
        return multiplyMatrices(this, m);
    }

    public Matrix4 premultiply(Matrix4 m) {
        return multiplyMatrices(m, this);
    }

    public Matrix4 multiplyScalar(double s) {
        te[ 0 ] *= s; te[ 4 ] *= s; te[ 8 ] *= s; te[ 12 ] *= s;
        te[ 1 ] *= s; te[ 5 ] *= s; te[ 9 ] *= s; te[ 13 ] *= s;
        te[ 2 ] *= s; te[ 6 ] *= s; te[ 10 ] *= s; te[ 14 ] *= s;
        te[ 3 ] *= s; te[ 7 ] *= s; te[ 11 ] *= s; te[ 15 ] *= s;

        return this;
    }

    public void applyToBufferAttribute(BufferAttribute attribute) {
        for (int i = 0; i < attribute.getCount(); i++) {
            _v1.x = attribute.getX(i);
            _v1.y = attribute.getY(i);
            _v1.z = attribute.getZ(i);
            _v1.applyMatrix4(this);
            attribute.setXYZ(i, _v1.x, _v1.y, _v1.z);
        }
    }

    public Matrix4 makePerspective(double left, double right, double top, double bottom,
                                   double near, double far) {
        double x = 2 * near / ( right - left );
        double y = 2 * near / ( top - bottom );

        double a = ( right + left ) / ( right - left );
        double b = ( top + bottom ) / ( top - bottom );
        double c = - ( far + near ) / ( far - near );
        double d = - 2 * far * near / ( far - near );

        te[ 0 ] = x;	te[ 4 ] = 0;	te[ 8 ] = a;	te[ 12 ] = 0;
        te[ 1 ] = 0;	te[ 5 ] = y;	te[ 9 ] = b;	te[ 13 ] = 0;
        te[ 2 ] = 0;	te[ 6 ] = 0;	te[ 10 ] = c;	te[ 14 ] = d;
        te[ 3 ] = 0;	te[ 7 ] = 0;	te[ 11 ] = - 1;	te[ 15 ] = 0;

        return this;
    }

    public Matrix4 makeOrthographic(double left, double right, double top, double bottom,
                                    double near, double far) {
        double w = 1.0f / ( right - left );
        double h = 1.0f / ( top - bottom );
        double p = 1.0f / ( far - near );

        double x = ( right + left ) * w;
        double y = ( top + bottom ) * h;
        double z = ( far + near ) * p;

        te[ 0 ] = 2 * w;	te[ 4 ] = 0;	te[ 8 ] = 0;	te[ 12 ] = - x;
        te[ 1 ] = 0;	te[ 5 ] = 2 * h;	te[ 9 ] = 0;	te[ 13 ] = - y;
        te[ 2 ] = 0;	te[ 6 ] = 0;	te[ 10 ] = - 2 * p;	te[ 14 ] = - z;
        te[ 3 ] = 0;	te[ 7 ] = 0;	te[ 11 ] = 0;	te[ 15 ] = 1;

        return this;
    }

    public boolean equals(Matrix4 matrix) {
        double[] me = matrix.te;
        for ( int i = 0; i < 16; i ++ ) {
            if ( te[ i ] != me[ i ] ) return false;
        }

        return true;
    }

    public double[] toArray(double[] array, int offset) {
        if (array == null) {
            array = new double[16];
        }
        array[ offset ] = te[ 0 ];
        array[ offset + 1 ] = te[ 1 ];
        array[ offset + 2 ] = te[ 2 ];
        array[ offset + 3 ] = te[ 3 ];

        array[ offset + 4 ] = te[ 4 ];
        array[ offset + 5 ] = te[ 5 ];
        array[ offset + 6 ] = te[ 6 ];
        array[ offset + 7 ] = te[ 7 ];

        array[ offset + 8 ] = te[ 8 ];
        array[ offset + 9 ] = te[ 9 ];
        array[ offset + 10 ] = te[ 10 ];
        array[ offset + 11 ] = te[ 11 ];

        array[ offset + 12 ] = te[ 12 ];
        array[ offset + 13 ] = te[ 13 ];
        array[ offset + 14 ] = te[ 14 ];
        array[ offset + 15 ] = te[ 15 ];
        return array;
    }

    public float[] toArrayF() {
        return toArrayF(null, 0);
    }

    public float[] toArrayF(float[] array, int offset) {
        if (array == null) {
            array = new float[16];
        }
        array[ offset ] = (float)te[ 0 ];
        array[ offset + 1 ] = (float)te[ 1 ];
        array[ offset + 2 ] = (float)te[ 2 ];
        array[ offset + 3 ] = (float)te[ 3 ];

        array[ offset + 4 ] = (float)te[ 4 ];
        array[ offset + 5 ] = (float)te[ 5 ];
        array[ offset + 6 ] = (float)te[ 6 ];
        array[ offset + 7 ] = (float)te[ 7 ];

        array[ offset + 8 ] = (float)te[ 8 ];
        array[ offset + 9 ] = (float)te[ 9 ];
        array[ offset + 10 ] = (float)te[ 10 ];
        array[ offset + 11 ] = (float)te[ 11 ];

        array[ offset + 12 ] = (float)te[ 12 ];
        array[ offset + 13 ] = (float)te[ 13 ];
        array[ offset + 14 ] = (float)te[ 14 ];
        array[ offset + 15 ] = (float)te[ 15 ];
        return array;
    }

    public double[] toArray() {
        return toArray(null, 0);
    }

    public String log() {
        String ret = "";
        for (int i = 0; i < te.length; i++) {
            ret += te[i] + " ";
        }
        return ret;
    }
}
