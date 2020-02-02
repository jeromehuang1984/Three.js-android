package edu.three.objects;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Object3D;
import edu.three.core.Raycaster;
import edu.three.materials.LineBasicMaterial;
import edu.three.materials.Material;
import edu.three.materials.PointsMaterial;
import edu.three.math.Color;
import edu.three.math.Matrix4;
import edu.three.math.Ray;
import edu.three.math.Sphere;
import edu.three.math.Vector3;

public class Line extends Object3D {
    String TAG = getClass().getSimpleName();

    public Line() {
        this(null, null);
    }

    public Line(BufferGeometry geometry, Material material) {
        if (geometry != null) {
            this.geometry = geometry;
        } else {
            this.geometry = new BufferGeometry();
        }

        if (material != null) {
            this.material.add(material);
        } else {
            LineBasicMaterial lMaterial = new LineBasicMaterial();
            lMaterial.color = new Color().setHex((int) (Math.random() * 0xffffff));
            this.material.add(lMaterial);
        }
    }

    public Line computeLineDistances() {
        Vector3 start = new Vector3();
        Vector3 end = new Vector3();
        // we assume non-indexed geometry
        if (geometry.getIndex() == null) {
            BufferAttribute postion = geometry.position;
            float[] lineDistances = new float[postion.getCount()];
            for (int i = 0; i < postion.getCount(); i++) {
                start.fromBufferAttribute(postion, i - 1);
                end.fromBufferAttribute(postion, i);
                lineDistances[ i ] = lineDistances[ i - 1 ];
                lineDistances[ i ] += start.distanceTo( end );
            }
            geometry.addAttribute("lineDistance", new BufferAttribute().setArray(lineDistances).setItemSize(1));
        } else {
            Log.e(TAG, "THREE.Line.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");
        }
        return this;
    }

    public void raycast(Raycaster raycaster, ArrayList<RaycastItem> intersects) {
        Matrix4 inverseMatrix = new Matrix4().getInverse(matrixWorld);
        Ray ray = new Ray();
        Sphere sphere = new Sphere();
        float precision = raycaster.linePrecision;
        if (geometry.boundingSphere == null) {
            geometry.computeBoundingSphere();
        }
        sphere.copy(geometry.boundingSphere);
        sphere.applyMatrix4(matrixWorld);
        sphere.radius += precision;

        if (!raycaster.ray.intersectsSphere(sphere)) {
            return;
        }

        ray.copy(raycaster.ray).applyMatrix4(inverseMatrix);
        float localPrecision = precision / ( ( scale.x + scale.y + scale.z ) / 3 );
        float localPrecisionSq = localPrecision * localPrecision;

        Vector3 vStart = new Vector3();
        Vector3 vEnd = new Vector3();
        Vector3 interSegment = new Vector3();
        Vector3 interRay = new Vector3();
        int step = ( this != null && this instanceof LineSegments ) ? 2 : 1;

        BufferAttribute index = geometry.getIndex();
        float[] positions = geometry.position.arrayFloat;

        if (index != null) {
            int[] indices = index.arrayInt;
            for (int i = 0; i < indices.length - 1; i+= step) {
                int a = indices[i];
                int b = indices[i + 1];
                vStart.fromArray(positions, a * 3);
                vEnd.fromArray(positions, b * 3);
                float distSq = ray.distanceSqToSegment( vStart, vEnd, interRay, interSegment );

                if ( distSq > localPrecisionSq ) continue;

                interRay.applyMatrix4(matrixWorld); //Move back to world space for distance calculation
                float distance = raycaster.ray.getOrigin().distanceTo(interRay);
                if (distance < raycaster.near || distance > raycaster.far) continue;

                RaycastItem item = new RaycastItem();
                item.distance = distance;
                // What do we want? intersection point on the ray or on the segment??
                // point: raycaster.ray.at( distance ),
                item.point = interSegment.clone().applyMatrix4( this.matrixWorld );
                item.index = i;
                item.face = null;
                item.object = this;
                intersects.add(item);
            }
        } else {
            for (int i = 0; i < positions.length / 3 - 1; i += step) {
                vStart.fromArray(positions, 3 * i);
                vEnd.fromArray(positions, 3 * i + 3);
                float distSq = ray.distanceSqToSegment( vStart, vEnd, interRay, interSegment );
                interRay.applyMatrix4(matrixWorld); //Move back to world space for distance calculation
                float distance = raycaster.ray.getOrigin().distanceTo( interRay );
                if ( distance < raycaster.near || distance > raycaster.far ) continue;
                RaycastItem item = new RaycastItem();
                item.distance = distance;
                // What do we want? intersection point on the ray or on the segment??
                // point: raycaster.ray.at( distance ),
                item.point = interSegment.clone().applyMatrix4( this.matrixWorld );
                item.index = i;
                item.face = null;
                item.object = this;
                intersects.add(item);
            }
        }
    }
}
