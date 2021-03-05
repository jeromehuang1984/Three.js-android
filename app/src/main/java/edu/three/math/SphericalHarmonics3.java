package edu.three.math;

// 3-band SH defined by 9 coefficients
public class SphericalHarmonics3 {
    Vector3[] coefficients;

    public SphericalHarmonics3() {
        coefficients = new Vector3[9];
        for (int i = 0; i < 9; i++) {
            coefficients[i] = new Vector3();
        }
    }

    public Vector3[] elements() {
        return coefficients;
    }

    public SphericalHarmonics3 set(Vector3[] coefficients) {
        for (int i = 0; i < 9; i++) {
            this.coefficients[i].copy(coefficients[i]);
        }
        return this;
    }

    public SphericalHarmonics3 zero(Vector3[] coefficients) {
        for (int i = 0; i < 9; i++) {
            this.coefficients[i].set(0, 0, 0);
        }
        return this;
    }

    // get the radiance in the direction of the normal
    // target is a Vector3
    public Vector3 getAt(Vector3 normal, Vector3 target) {
        // normal is assumed to be unit length
        double x = normal.x;
        double y = normal.y;
        double z = normal.z;
        Vector3[] coeff = coefficients;
        //band 0
        target.copy(coeff[0]).multiplyScalar(0.282095f);

        //band 1
        target.addScale( coeff[ 1 ], 0.488603f * y );
        target.addScale( coeff[ 2 ], 0.488603f * z );
        target.addScale( coeff[ 3 ], 0.488603f * x );

        // band 2
        target.addScale( coeff[ 4 ], 1.092548f * ( x * y ) );
        target.addScale( coeff[ 5 ], 1.092548f * ( y * z ) );
        target.addScale( coeff[ 6 ], 0.315392f * ( 3.0f * z * z - 1.0f ) );
        target.addScale( coeff[ 7 ], 1.092548f * ( x * z ) );
        target.addScale( coeff[ 8 ], 0.546274f * ( x * x - y * y ) );

        return target;
    }

    // get the irradiance (radiance convolved with cosine lobe) in the direction of the normal
    // target is a Vector3
    // https://graphics.stanford.edu/papers/envmap/envmap.pdf
    public Vector3 getIrradianceAt(Vector3 normal, Vector3 target) {
        // normal is assumed to be unit length
        double x = normal.x;
        double y = normal.y;
        double z = normal.z;
        Vector3[] coeff = coefficients;
        // band 0
        target.copy( coeff[ 0 ] ).multiplyScalar( 0.886227f ); // π * 0.282095

        // band 1
        target.addScale( coeff[ 1 ], 2.0f * 0.511664f * y ); // ( 2 * π / 3 ) * 0.488603
        target.addScale( coeff[ 2 ], 2.0f * 0.511664f * z );
        target.addScale( coeff[ 3 ], 2.0f * 0.511664f * x );

        // band 2
        target.addScale( coeff[ 4 ], 2.0f * 0.429043f * x * y ); // ( π / 4 ) * 1.092548
        target.addScale( coeff[ 5 ], 2.0f * 0.429043f * y * z );
        target.addScale( coeff[ 6 ], 0.743125f * z * z - 0.247708f ); // ( π / 4 ) * 0.315392 * 3
        target.addScale( coeff[ 7 ], 2.0f * 0.429043f * x * z );
        target.addScale( coeff[ 8 ], 0.429043f * ( x * x - y * y ) ); // ( π / 4 ) * 0.546274

        return target;
    }

    public SphericalHarmonics3 add(SphericalHarmonics3 sh) {
        for (int i = 0; i < 9; i++) {
            coefficients[i].add(sh.coefficients[i]);
        }
        return this;
    }

    public SphericalHarmonics3 scale(double s) {
        for (int i = 0; i < 9; i++) {
            coefficients[i].multiplyScalar(s);
        }
        return this;
    }

    public SphericalHarmonics3 lerp(SphericalHarmonics3 sh, double alpha) {
        for (int i = 0; i < 9; i++) {
            coefficients[i].lerp(sh.coefficients[i], alpha);
        }
        return this;
    }

    public boolean equals(SphericalHarmonics3 sh) {
        for ( int i = 0; i < 9; i ++ ) {
            if ( ! coefficients[ i ].equals( sh.coefficients[ i ] ) ) {
                return false;
            }
        }

        return true;
    }

    public SphericalHarmonics3 copy(SphericalHarmonics3 sh) {
        return set(sh.coefficients);
    }

    public SphericalHarmonics3 clone() {
        return new SphericalHarmonics3().copy(this);
    }

    public SphericalHarmonics3 fromArray(double[] array) {
        for ( int i = 0; i < 9; i ++ ) {
            coefficients[ i ].fromArray( array, i * 3 );
        }

        return this;
    }

    public double[] toArray() {
        double[] array = new double[27];
        for ( int i = 0; i < 9; i ++ ) {
            coefficients[ i ].toArray( array, i * 3 );
        }
        return array;
    }

    // evaluate the basis functions
    // shBasis is an Array[ 9 ]
    public void getBasisAt(Vector3 normal, double[] shBasis) {
        double x = normal.x, y = normal.y, z = normal.z;

        // band 0
        shBasis[ 0 ] = 0.282095f;

        // band 1
        shBasis[ 1 ] = 0.488603f * y;
        shBasis[ 2 ] = 0.488603f * z;
        shBasis[ 3 ] = 0.488603f * x;

        // band 2
        shBasis[ 4 ] = 1.092548f * x * y;
        shBasis[ 5 ] = 1.092548f * y * z;
        shBasis[ 6 ] = 0.315392f * ( 3 * z * z - 1 );
        shBasis[ 7 ] = 1.092548f * x * z;
        shBasis[ 8 ] = 0.546274f * ( x * x - y * y );
    }
}
