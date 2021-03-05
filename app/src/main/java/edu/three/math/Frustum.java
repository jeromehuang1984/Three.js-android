package edu.three.math;

import edu.three.core.BufferGeometry;
import edu.three.core.Object3D;
import edu.three.objects.Mesh;
import edu.three.objects.Sprite;

public class Frustum {
    public Plane[] planes = new Plane[6];

    public Frustum() {
        planes[0] = new Plane();
        planes[1] = new Plane();
        planes[2] = new Plane();
        planes[3] = new Plane();
        planes[4] = new Plane();
        planes[5] = new Plane();
    }

    public Frustum set(Plane p0, Plane p1, Plane p2, Plane p3,
                     Plane p4, Plane p5) {
        planes[0].copy(p0);
        planes[1].copy(p1);
        planes[2].copy(p2);
        planes[3].copy(p3);
        planes[4].copy(p4);
        planes[5].copy(p5);
        return this;
    }

    public Frustum copy(Frustum frustum) {
        Plane[] planes = this.planes;

        for ( int i = 0; i < 6; i ++ ) {
            planes[ i ].copy( frustum.planes[ i ] );
        }

        return this;
    }

    public Frustum setFromMatrix(Matrix4 m) {
        double[] me = m.te;
        double me0 = me[ 0 ], me1 = me[ 1 ], me2 = me[ 2 ], me3 = me[ 3 ];
        double me4 = me[ 4 ], me5 = me[ 5 ], me6 = me[ 6 ], me7 = me[ 7 ];
        double me8 = me[ 8 ], me9 = me[ 9 ], me10 = me[ 10 ], me11 = me[ 11 ];
        double me12 = me[ 12 ], me13 = me[ 13 ], me14 = me[ 14 ], me15 = me[ 15 ];

        planes[ 0 ].setComponents( me3 - me0, me7 - me4, me11 - me8, me15 - me12 ).normalize();
        planes[ 1 ].setComponents( me3 + me0, me7 + me4, me11 + me8, me15 + me12 ).normalize();
        planes[ 2 ].setComponents( me3 + me1, me7 + me5, me11 + me9, me15 + me13 ).normalize();
        planes[ 3 ].setComponents( me3 - me1, me7 - me5, me11 - me9, me15 - me13 ).normalize();
        planes[ 4 ].setComponents( me3 - me2, me7 - me6, me11 - me10, me15 - me14 ).normalize();
        planes[ 5 ].setComponents( me3 + me2, me7 + me6, me11 + me10, me15 + me14 ).normalize();

        return this;
    }

    public boolean intersectsObject(Object3D object) {
        Sphere sphere = new Sphere();
        BufferGeometry geometry = object.geometry;
        if (geometry.boundingSphere == null) {
            geometry.computeBoundingSphere();
        }
        sphere.copy(geometry.boundingSphere).applyMatrix4(object.getWorldMatrix());
        return intersectsSphere(sphere);
    }

    public boolean intersectsSprite(Sprite sprite) {
        Sphere sphere = new Sphere();
        sphere.center.set(0, 0, 0);
        sphere.radius = 0.7071067811865476f;
        sphere.applyMatrix4(sprite.getWorldMatrix());
        return intersectsSphere(sphere);
    }

    public boolean intersectsSphere(Sphere sphere) {
        Vector3 center = sphere.center;
        double negRadius = -sphere.radius;

        for (int i = 0; i < 6; i++) {
            double distance = planes[i].distanceToPoint(center);
            if (distance < negRadius) {
                return false;
            }
        }
        return true;
    }

    public boolean intersectsBox(Box3 box) {
        Vector3 p = new Vector3();
        for ( int i = 0; i < 6; i ++ ) {

            Plane plane = planes[ i ];

            // corner at max distance

            p.x = plane.normal.x > 0 ? box.max.x : box.min.x;
            p.y = plane.normal.y > 0 ? box.max.y : box.min.y;
            p.z = plane.normal.z > 0 ? box.max.z : box.min.z;

            if ( plane.distanceToPoint( p ) < 0 ) {

                return false;

            }

        }

        return true;
    }

    public boolean containsPoint(Vector3 point) {
        for ( int i = 0; i < 6; i ++ ) {
            if ( planes[ i ].distanceToPoint( point ) < 0 ) {
                return false;
            }
        }

        return true;
    }
}
