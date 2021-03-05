package edu.three.math;

public class Ray {
    Vector3 origin = new Vector3();
    Vector3 direction = new Vector3();

    public Ray set(Vector3 start, Vector3 direction) {
        origin = start;
        this.direction = direction;
        return this;
    }

    public Ray copy(Ray ray) {
        origin.copy(ray.origin);
        direction.copy(ray.direction);
        return this;
    }
    public Ray clone() {
        return new Ray().copy(this);
    }

    public Vector3 getOrigin() {
        return origin;
    }
    public Vector3 getDirection() {
        return direction;
    }
    public Vector3 at(double t, Vector3 target) {
        Vector3 ret = target;
        if (ret == null) {
            ret = new Vector3();
        }
        return ret.copy(direction).multiplyScalar(t).add(origin);
    }
    public Ray lookAt(Vector3 v) {
        direction.copy(v).sub(origin).normalize();
        return this;
    }
    public Ray recast(double t) {
        Vector3 v1 = new Vector3();
        origin.copy(at(t, v1));
        return this;
    }

    public Vector3 closestPointToPoint(Vector3 point, Vector3 target) {
        Vector3 ret = target;
        if (ret == null) {
            ret = new Vector3();
        }
        ret.subVectors(point, origin);
        double directionDistance = ret.dot(direction);
        if (directionDistance < 0) {
            return ret.copy(origin);
        }
        return ret.copy(direction).multiplyScalar(directionDistance).add(origin);
    }

    public Ray applyMatrix4(Matrix4 matrix) {
        origin.applyMatrix4(matrix);
        direction.transformDirection(matrix);
        return this;
    }

    public float distanceSqToPoint(Vector3 point) {
        Vector3 v1 = new Vector3();
        double directionDistance = v1.subVectors( point, origin ).dot( direction );
        // point behind the ray
        if ( directionDistance < 0 ) {
            return (float)origin.distanceToSquared( point );
        }
        v1.copy( this.direction ).multiplyScalar( directionDistance ).add( origin );
        return (float)v1.distanceToSquared( point );
    }

    public double distanceToPoint(Vector3 point) {
        return (double) Math.sqrt(distanceSqToPoint(point));
    }

    public double distanceSqToSegment(Vector3 v0, Vector3 v1, Vector3 optionalPointOnRay, Vector3 optionalPointOnSegment) {
        Vector3 segCenter = new Vector3();
        Vector3 segDir = new Vector3();
        Vector3 diff = new Vector3();
        // from http://www.geometrictools.com/GTEngine/Include/Mathematics/GteDistRaySegment.h
        // It returns the min distance between the ray and the segment
        // defined by v0 and v1
        // It can also set two optional targets :
        // - The closest point on the ray
        // - The closest point on the segment
        segCenter.copy( v0 ).add( v1 ).multiplyScalar( 0.5f );
        segDir.copy( v1 ).sub( v0 ).normalize();
        diff.copy( origin ).sub( segCenter );

        double segExtent = v0.distanceTo(v1) * 0.5f;
        double a01 = - direction.dot(segDir);
        double b0 = diff.dot(diff);
        double b1 = diff.dot(segDir);
        double c = diff.lengthSq();
        double det = Math.abs(1 - a01 * a01);
        double s0, s1, sqrDist, extDet;

        if ( det > 0 ) {
            // The ray and segment are not parallel.
            s0 = a01 * b1 - b0;
            s1 = a01 * b0 - b1;
            extDet = segExtent * det;
            if ( s0 >= 0 ) {
                if ( s1 >= - extDet ) {
                    if ( s1 <= extDet ) {
                        // region 0
                        // Minimum at interior points of ray and segment.
                        double invDet = 1 / det;
                        s0 *= invDet;
                        s1 *= invDet;
                        sqrDist = s0 * ( s0 + a01 * s1 + 2 * b0 ) + s1 * ( a01 * s0 + s1 + 2 * b1 ) + c;
                    } else {
                        // region 1
                        s1 = segExtent;
                        s0 = Math.max( 0, - ( a01 * s1 + b0 ) );
                        sqrDist = - s0 * s0 + s1 * ( s1 + 2 * b1 ) + c;
                    }

                } else {
                    // region 5
                    s1 = - segExtent;
                    s0 = Math.max( 0, - ( a01 * s1 + b0 ) );
                    sqrDist = - s0 * s0 + s1 * ( s1 + 2 * b1 ) + c;
                }
            } else {
                if ( s1 <= - extDet ) {
                    // region 4
                    s0 = Math.max( 0, - ( - a01 * segExtent + b0 ) );
                    s1 = ( s0 > 0 ) ? - segExtent : Math.min( Math.max( - segExtent, - b1 ), segExtent );
                    sqrDist = - s0 * s0 + s1 * ( s1 + 2 * b1 ) + c;
                } else if ( s1 <= extDet ) {
                    // region 3
                    s0 = 0;
                    s1 = Math.min( Math.max( - segExtent, - b1 ), segExtent );
                    sqrDist = s1 * ( s1 + 2 * b1 ) + c;
                } else {
                    // region 2
                    s0 = Math.max( 0, - ( a01 * segExtent + b0 ) );
                    s1 = ( s0 > 0 ) ? segExtent : Math.min( Math.max( - segExtent, - b1 ), segExtent );
                    sqrDist = - s0 * s0 + s1 * ( s1 + 2 * b1 ) + c;
                }
            }
        } else {
            // Ray and segment are parallel.
            s1 = ( a01 > 0 ) ? - segExtent : segExtent;
            s0 = Math.max( 0, - ( a01 * s1 + b0 ) );
            sqrDist = - s0 * s0 + s1 * ( s1 + 2 * b1 ) + c;

        }
        if ( optionalPointOnRay != null) {
            optionalPointOnRay.copy( this.direction ).multiplyScalar( s0 ).add( this.origin );
        }
        if ( optionalPointOnSegment != null) {
            optionalPointOnSegment.copy( segDir ).multiplyScalar( s1 ).add( segCenter );
        }

        return sqrDist;
    }

    public Vector3 intersectSphere(Sphere sphere, Vector3 target) {
        Vector3 v1 = new Vector3();
        v1.subVectors(sphere.center, origin);
        double tca = v1.dot(direction);
        double d2 = v1.dot( v1 ) - tca * tca;
        double radius2 = sphere.radius * sphere.radius;
        if (d2 > radius2) {
            return null;
        }
        double thc = (double) Math.sqrt(radius2 - d2);
        // t0 = first intersect point - entrance on front of sphere
        double t0 = tca - thc;

        // t1 = second intersect point - exit point on back of sphere
        double t1 = tca + thc;

        // test to see if both t0 and t1 are behind the ray - if so, return null
        if ( t0 < 0 && t1 < 0 ) return null;

        // test to see if t0 is behind the ray:
        // if it is, the ray is inside the sphere, so return the second exit point scaled by t1,
        // in order to always return an intersect point that is in front of the ray.
        if ( t0 < 0 ) return this.at( t1, target );

        // else t0 is in front of the ray, so return the first collision point scaled by t0
        return this.at( t0, target );
    }

    public boolean intersectsSphere(Sphere sphere) {
        return distanceSqToPoint(sphere.center) <= (sphere.radius * sphere.radius);
    }

    public Double distanceToPlane(Plane plane) {
        double denominator = plane.normal.dot(direction);
        if (denominator == 0) {
            // line is coplanar, return origin
            if ( plane.distanceToPoint( this.origin ) == 0 ) {
                return 0d;
            }
            // Null is preferable to undefined since undefined means.... it is undefined
            return null;
        }
        double t = - ( origin.dot( plane.normal ) + plane.constant ) / denominator;

        // Return if the ray never intersects the plane
        return t >= 0 ? t : null;
    }

    public Vector3 intersectPlane(Plane plane, Vector3 target) {
        Double t = distanceToPlane(plane);
        if (t == null) {
            return null;
        }
        return at(t, target);
    }

    public boolean intersectsPlane(Plane plane) {
        // check if the ray lies on the plane first
        double distToPoint = plane.distanceToPoint(origin);
        if (distToPoint == 0) {
            return true;
        }
        double denominator = plane.normal.dot(direction);
        if (denominator * distToPoint < 0) {
            return true;
        }
        // ray origin is behind the plane (and is pointing behind it)
        return false;
    }

    public Vector3 intersectBox(Box3 box, Vector3 target) {
        double tmin, tmax, tymin, tymax, tzmin, tzmax;
        double invdirx = 1 / direction.x;
        double invdiry = 1 / direction.y;
        double invdirz = 1 / direction.z;

        if ( invdirx >= 0 ) {
            tmin = ( box.min.x - origin.x ) * invdirx;
            tmax = ( box.max.x - origin.x ) * invdirx;
        } else {
            tmin = ( box.max.x - origin.x ) * invdirx;
            tmax = ( box.min.x - origin.x ) * invdirx;
        }

        if ( invdiry >= 0 ) {
            tymin = ( box.min.y - origin.y ) * invdiry;
            tymax = ( box.max.y - origin.y ) * invdiry;
        } else {
            tymin = ( box.max.y - origin.y ) * invdiry;
            tymax = ( box.min.y - origin.y ) * invdiry;
        }

        if ( ( tmin > tymax ) || ( tymin > tmax ) ) return null;

        // These lines also handle the case where tmin or tmax is NaN
        // (result of 0 * Infinity). x !== x returns true if x is NaN

        if ( tymin > tmin || tmin != tmin ) tmin = tymin;

        if ( tymax < tmax || tmax != tmax ) tmax = tymax;

        if ( invdirz >= 0 ) {
            tzmin = ( box.min.z - origin.z ) * invdirz;
            tzmax = ( box.max.z - origin.z ) * invdirz;
        } else {
            tzmin = ( box.max.z - origin.z ) * invdirz;
            tzmax = ( box.min.z - origin.z ) * invdirz;
        }

        if ( ( tmin > tzmax ) || ( tzmin > tmax ) ) return null;

        if ( tzmin > tmin || tmin != tmin ) tmin = tzmin;

        if ( tzmax < tmax || tmax != tmax ) tmax = tzmax;

        //return point closest to the ray (positive side)
        if ( tmax < 0 ) return null;

        return this.at( tmin >= 0 ? tmin : tmax, target );
    }

    public boolean intersectsBox(Box3 box) {
        Vector3 v = new Vector3();
        return intersectBox(box, v) != null;
    }

    public Vector3 intersectTriangle(Vector3 a, Vector3 b, Vector3 c,
                                     boolean backfaceCulling, Vector3 target) {
        // Compute the offset origin, edges, and normal.
        Vector3 diff = new Vector3();
        Vector3 edge1 = new Vector3();
        Vector3 edge2 = new Vector3();
        Vector3 normal = new Vector3();

        // from http://www.geometrictools.com/GTEngine/Include/Mathematics/GteIntrRay3Triangle3.h
        edge1.subVectors( b, a );
        edge2.subVectors( c, a );
        normal.crossVectors( edge1, edge2 );

        // Solve Q + t*D = b1*E1 + b2*E2 (Q = kDiff, D = ray direction,
        // E1 = kEdge1, E2 = kEdge2, N = Cross(E1,E2)) by
        //   |Dot(D,N)|*b1 = sign(Dot(D,N))*Dot(D,Cross(Q,E2))
        //   |Dot(D,N)|*b2 = sign(Dot(D,N))*Dot(D,Cross(E1,Q))
        //   |Dot(D,N)|*t = -sign(Dot(D,N))*Dot(Q,N)
        double DdN = direction.dot( normal );
        double sign;
        if (DdN > 0) {
            if (backfaceCulling) {
                return null;
            }
            sign = 1;
        } else if ( DdN < 0) {
            sign = -1;
            DdN = - DdN;
        } else {
            return null;
        }
        diff.subVectors(origin, a);
        double DdQxE2 = sign * this.direction.dot( edge2.crossVectors( diff, edge2 ) );
        // b1 < 0, no intersection
        if ( DdQxE2 < 0 ) {
            return null;
        }

        double DdE1xQ = sign * this.direction.dot( edge1.cross( diff ) );
        // b2 < 0, no intersection
        if ( DdE1xQ < 0 ) {
            return null;
        }

        // b1+b2 > 1, no intersection
        if ( DdQxE2 + DdE1xQ > DdN ) {
            return null;
        }

        // Line intersects triangle, check if ray does.
        double QdN = - sign * diff.dot( normal );
        // t < 0, no intersection
        if ( QdN < 0 ) {
            return null;
        }

        // Ray intersects triangle.
        return this.at( QdN / DdN, target );
    }

    public boolean equals(Ray ray) {
        return ray.origin.equals(origin) && ray.direction.equals(direction);
    }
}
