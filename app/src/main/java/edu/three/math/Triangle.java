package edu.three.math;

import android.os.Build;

public class Triangle {
    Vector3 a, b, c;

    public Triangle(Vector3 v0, Vector3 v1, Vector3 v2) {
        a = v0;
        b = v1;
        c = v2;
    }

    public Vector3 getNormal() {
        Vector3 ret = new Vector3();
        return getNormal(a, b, c, ret);
    }


    public static Vector3 getNormal(Vector3 a, Vector3 b, Vector3 c, Vector3 ret) {
        Vector3 v0 = new Vector3();
        ret.subVectors(c, b);
        v0.subVectors(a, b);
        ret.cross(v0);
        float lengthSq = ret.lengthSq();
        if (lengthSq > 0) {
            return ret.multiplyScalar(1 / (float) Math.sqrt(lengthSq));
        }
        return ret.set(0, 0, 0);
    }

    public Vector3 getCenter() {
        return a.clone().add(b).add(c).divideScalar(3);
    }

    public static Vector2 getUV(Vector3 point, Vector3 p1, Vector3 p2, Vector3 p3, Vector2 uv1, Vector2 uv2,
                                Vector2 uv3, Vector2 target) {
        Vector3 barycoord = new Vector3();
        getBarycoord( point, p1, p2, p3, barycoord );
        target.set( 0, 0);
        target.addScaledVector( uv1, barycoord.x );
        target.addScaledVector( uv2, barycoord.y );
        target.addScaledVector( uv3, barycoord.z );
        return target;
    }

    public static Vector3 getBarycoord(Vector3 point, Vector3 a, Vector3 b, Vector3 c, Vector3 target) {
        Vector3 v0 = new Vector3();
        Vector3 v1 = new Vector3();
        Vector3 v2 = new Vector3();

        v0.subVectors( c, a );
        v1.subVectors( b, a );
        v2.subVectors( point, a );

        float dot00 = v0.dot( v0 );
        float dot01 = v0.dot( v1 );
        float dot02 = v0.dot( v2 );
        float dot11 = v1.dot( v1 );
        float dot12 = v1.dot( v2 );

        float denom = ( dot00 * dot11 - dot01 * dot01 );

        // collinear or singular triangle
        if ( denom == 0 ) {
            // arbitrary location outside of triangle?
            // not sure if this is the best idea, maybe should be returning undefined
            return target.set( - 2, - 1, - 1 );

        }

        float invDenom = 1 / denom;
        float u = ( dot11 * dot02 - dot01 * dot12 ) * invDenom;
        float v = ( dot00 * dot12 - dot01 * dot02 ) * invDenom;

        // barycentric coordinates must always sum to 1
        return target.set( 1 - u - v, v, u );
    }
}
