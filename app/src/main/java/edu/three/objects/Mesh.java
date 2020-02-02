package edu.three.objects;

import java.util.ArrayList;

import edu.three.constant.Constants;
import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Face3;
import edu.three.core.Geometry;
import edu.three.core.Object3D;
import edu.three.core.Raycaster;
import edu.three.materials.Material;
import edu.three.math.Matrix4;
import edu.three.math.Ray;
import edu.three.math.Sphere;
import edu.three.math.Triangle;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

import static edu.three.constant.Constants.BackSide;
import static edu.three.constant.Constants.DoubleSide;
import static edu.three.constant.Constants.PCFShadowMap;

public class Mesh extends Object3D {
    private int drawMode = Constants.TrianglesDrawMode;
    public boolean wireframe = false;

    public Mesh(Geometry geometry, ArrayList<Material> material) {
        this.geometry = new BufferGeometry().fromGeometry(geometry);
        this.geometry.computeBoundingBox();
        this.geometry.computeBoundingSphere();
        this.material = material;
    }

    public Mesh(BufferGeometry geometry, ArrayList<Material> material) {
        this(geometry);
        this.material = material;
    }

    public Mesh(BufferGeometry geometry, Material material) {
        this(geometry);
        this.material.clear();
        this.material.add(material);
    }

    public Mesh(BufferGeometry geometry) {
        this.geometry = geometry;
        if (geometry != null) {
            this.geometry.computeBoundingBox();
            this.geometry.computeBoundingSphere();
        }
    }

    public void setDrawMode(int value) {
        drawMode = value;
    }

    public int getDrawMode() {
        return drawMode;
    }

    private RaycastItem checkIntersection(Ray ray, Vector3 pA, Vector3 pB, Vector3 pC, Vector3 point) {
        Vector3 intersect = ray.intersectTriangle(pA, pB, pC, false, point);
        if (intersect == null) {
            return null;
        }
        Vector3 intersectionPointWorld = new Vector3();
        intersectionPointWorld.copy(point);
        intersectionPointWorld.applyMatrix4(matrixWorld);
        RaycastItem ret = new RaycastItem();
        ret.distance = ray.getOrigin().distanceTo(intersectionPointWorld);
        ret.point = intersectionPointWorld;
        ret.object = this;
        return ret;
    }

    private class RaycastContext {
        Vector3 vA = new Vector3();
        Vector3 vB = new Vector3();
        Vector3 vC = new Vector3();

        Vector3 tempA = new Vector3();
        Vector3 tempB = new Vector3();
        Vector3 tempC = new Vector3();

        Vector3 morphA = new Vector3();
        Vector3 morphB = new Vector3();
        Vector3 morphC = new Vector3();

        Vector2 uvA = new Vector2();
        Vector2 uvB = new Vector2();
        Vector2 uvC = new Vector2();

        Vector3 intersectionPoint = new Vector3();
    }
    public void raycast(Raycaster raycaster, ArrayList<RaycastItem> intersects) {
        Matrix4 inverseMatrix = new Matrix4();
        Sphere sphere = new Sphere();
        Ray ray = new Ray();
        RaycastContext context = new RaycastContext();

        if (material.size() == 0) {
            return;
        }
        // Checking boundingSphere distance to ray
        if ( geometry.boundingSphere == null ) geometry.computeBoundingSphere();
        sphere.copy( geometry.boundingSphere );
        sphere.applyMatrix4( matrixWorld );

        if ( !raycaster.ray.intersectsSphere( sphere ) )
            return;
        inverseMatrix.getInverse( matrixWorld );
        ray.copy( raycaster.ray ).applyMatrix4( inverseMatrix );

        // Check boundingBox before continuing

        if ( geometry.boundingBox != null ) {
            if ( !ray.intersectsBox( geometry.boundingBox ))
                return;
        }
        RaycastItem intersection;
        int a, b, c;
        BufferAttribute index = geometry.getIndex();
        BufferAttribute position = geometry.position;
        ArrayList<BufferAttribute> morphPosition = geometry.morphAttributes;
        BufferAttribute uv = geometry.uv;
        ArrayList<GroupItem> groups = geometry.getGroups();
        int drawRangeStart = geometry.drawRangeStart;
        int drawRangeCount = geometry.drawRangeCount;
        int i, j, il, jl;
        GroupItem group;
        Material groupMaterial;
        int start, end;

        if ( index != null ) {

            // indexed buffer geometry

            if ( material.size() > 1 ) {

                for ( i = 0, il = groups.size(); i < il; i ++ ) {

                    group = groups.get(i);
                    groupMaterial = material.get(group.materialIndex);

                    start = Math.max( group.start, drawRangeStart );
                    end = Math.min( ( group.start + group.count ), ( drawRangeStart + drawRangeCount ) );

                    for ( j = start, jl = end; j < jl; j += 3 ) {

                        a = (int) index.getX( j );
                        b = (int) index.getX( j + 1 );
                        c = (int) index.getX( j + 2 );

                        intersection = checkBufferGeometryIntersection( this, groupMaterial, raycaster, ray, position, morphPosition, uv, a, b, c, context );

                        if ( intersection != null ) {

                            intersection.faceIndex = (int)Math.floor( j / 3 ); // triangle number in indexed buffer semantics
                            intersection.face.materialIndex = group.materialIndex;
                            intersects.add( intersection );

                        }

                    }

                }

            } else {

                start = Math.max( 0, drawRangeStart );
                end = Math.min( index.getCount(), ( drawRangeStart + drawRangeCount ) );

                for ( i = start, il = end; i < il; i += 3 ) {

                    a = (int) index.getX( i );
                    b = (int) index.getX( i + 1 );
                    c = (int) index.getX( i + 2 );

                    intersection = checkBufferGeometryIntersection( this, material.get(0), raycaster, ray, position, morphPosition, uv, a, b, c, context);

                    if ( intersection != null) {

                        intersection.faceIndex = (int) Math.floor( i / 3 ); // triangle number in indexed buffer semantics
                        intersects.add( intersection );

                    }

                }

            }

        } else if ( position != null ) {

            // non-indexed buffer geometry

            if (material.size() > 1) {
                for ( i = 0, il = groups.size(); i < il; i ++ ) {

                    group = groups.get(i);
                    groupMaterial = material.get(group.materialIndex);

                    start = Math.max( group.start, drawRangeStart );
                    end = Math.min( ( group.start + group.count ), ( drawRangeStart + drawRangeCount ) );

                    for ( j = start, jl = end; j < jl; j += 3 ) {

                        a = j;
                        b = j + 1;
                        c = j + 2;

                        intersection = checkBufferGeometryIntersection( this, groupMaterial, raycaster, ray, position, morphPosition, uv, a, b, c, context);

                        if ( intersection != null) {

                            intersection.faceIndex = (int) Math.floor( j / 3 ); // triangle number in non-indexed buffer semantics
                            intersection.face.materialIndex = group.materialIndex;
                            intersects.add( intersection );

                        }

                    }

                }

            } else {

                start = Math.max( 0, drawRangeStart );
                end = Math.min( position.getCount(), ( drawRangeStart + drawRangeCount ) );

                for ( i = start, il = end; i < il; i += 3 ) {

                    a = i;
                    b = i + 1;
                    c = i + 2;

                    intersection = checkBufferGeometryIntersection( this, material.get(0), raycaster, ray, position, morphPosition, uv, a, b, c, context);

                    if ( intersection != null) {
                        intersection.faceIndex = (int) Math.floor( i / 3 ); // triangle number in non-indexed buffer semantics
                        intersects.add( intersection );
                    }

                }

            }

        }
    }

    private RaycastItem checkIntersection(Object3D object, Material material, Raycaster raycaster, Ray ray,
                       Vector3 pA, Vector3 pB, Vector3 pC, Vector3 point) {
        Vector3 intersect;
        if (material.side == BackSide) {
            intersect = ray.intersectTriangle(pC, pB, pA, true, point);
        } else {
            intersect = ray.intersectTriangle( pA, pB, pC, material.side != DoubleSide, point );
        }
        if ( intersect == null ) return null;
        Vector3 intersectionPointWorld = new Vector3();
        intersectionPointWorld.copy( point );
        intersectionPointWorld.applyMatrix4( object.getWorldMatrix() );

        float distance = raycaster.ray.getOrigin().distanceTo( intersectionPointWorld );

        if ( distance < raycaster.near || distance > raycaster.far ) return null;
        RaycastItem item = new RaycastItem();
        item.distance = distance;
        item.point = intersectionPointWorld;
        item.object = object;
        return item;
    }

    private RaycastItem checkBufferGeometryIntersection(Object3D object, Material material, Raycaster raycaster, Ray ray,
                            BufferAttribute position, ArrayList<BufferAttribute> morphPosition, BufferAttribute uv,
                            int a, int b, int c, RaycastContext context) {
        context.vA.fromBufferAttribute( position, a );
        context.vB.fromBufferAttribute( position, b );
        context.vC.fromBufferAttribute( position, c );

        float[] morphInfluences = object.morphTargetInfluences;

        if ( material.morphTargets && morphPosition != null && morphInfluences != null) {
            context.morphA.set( 0, 0, 0 );
            context.morphB.set( 0, 0, 0 );
            context.morphC.set( 0, 0, 0 );

            for ( int i = 0, il = morphPosition.size(); i < il; i ++ ) {

                float influence = morphInfluences[ i ];
                BufferAttribute morphAttribute = morphPosition.get(i);

                if ( influence == 0 ) continue;

                context.tempA.fromBufferAttribute( morphAttribute, a );
                context.tempB.fromBufferAttribute( morphAttribute, b );
                context.tempC.fromBufferAttribute( morphAttribute, c );

                context.morphA.addScaledVector( context.tempA.sub( context.vA ), influence );
                context.morphB.addScaledVector( context.tempB.sub( context.vB ), influence );
                context.morphC.addScaledVector( context.tempC.sub( context.vC ), influence );

            }

            context.vA.add( context.morphA );
            context.vB.add( context.morphB );
            context.vC.add( context.morphC );
        }

        RaycastItem intersection = checkIntersection( object, material, raycaster, ray, context.vA, context.vB, context.vC,
                context.intersectionPoint);
        if (intersection != null) {
            if ( uv != null) {

                context.uvA.fromBufferAttribute( uv, a );
                context.uvB.fromBufferAttribute( uv, b );
                context.uvC.fromBufferAttribute( uv, c );

                intersection.uv = Triangle.getUV( context.intersectionPoint, context.vA, context.vB, context.vC,
                        context.uvA, context.uvB, context.uvC, new Vector2() );
            }

            Face3 face = new Face3( a, b, c );
            Triangle tri = new Triangle(context.vA.clone(), context.vB.clone(), context.vC.clone());
            intersection.face = face;
            intersection.triangle = tri;
        }
        return intersection;
    }

}
