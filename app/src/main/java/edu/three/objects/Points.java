package edu.three.objects;

import java.util.ArrayList;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Object3D;
import edu.three.core.Raycaster;
import edu.three.materials.Material;
import edu.three.materials.PointsMaterial;
import edu.three.math.Color;
import edu.three.math.Matrix4;
import edu.three.math.Ray;
import edu.three.math.Sphere;
import edu.three.math.Vector3;

public class Points extends Object3D {
    public Points() {
        this(null, null);
    }

    public Points(BufferGeometry geometry, Material material) {
        if (geometry != null) {
            this.geometry = geometry;
        } else {
            this.geometry = new BufferGeometry();
        }

        if (material != null) {
            this.material.add(material);
        } else {
            PointsMaterial pMaterial = new PointsMaterial();
            pMaterial.color = new Color().setHex((int) (Math.random() * 0xffffff));
            this.material.add(pMaterial);
        }
    }

    private void testPoint(Vector3 point, int index, float localThresholdSq, Ray ray,  Raycaster raycaster, Vector3 intersectPoint,
                           ArrayList<RaycastItem> intersects) {
        float rayPointDistanceSq = ray.distanceSqToPoint(point);
        if (rayPointDistanceSq < localThresholdSq) {
            ray.closestPointToPoint(point, intersectPoint);
            intersectPoint.applyMatrix4(matrixWorld);
            double distance = raycaster.ray.getOrigin().distanceTo(intersectPoint);
            if (distance < raycaster.near || distance > raycaster.far) {
                return;
            }
            RaycastItem item = new RaycastItem();
            item.distance = distance;
            item.distanceToRay = (float) Math.sqrt(rayPointDistanceSq);
            item.point = intersectPoint.clone();
            item.index = index;
            item.face = null;
            item.object = this;
            intersects.add(item);
        }
    }

    public void raycast(Raycaster raycaster, ArrayList<RaycastItem> intersects) {
        Matrix4 inverseMatrix = new Matrix4().getInverse(matrixWorld);
        Ray ray = new Ray();
        Sphere sphere = new Sphere();
        float threshold = raycaster.params.Points.threshold;
        // Checking boundingSphere distance to ray
        if (geometry.boundingSphere == null) {
            geometry.computeBoundingSphere();
        }
        sphere.copy(geometry.boundingSphere);
        sphere.applyMatrix4(matrixWorld);
        sphere.radius += threshold;
        if (!raycaster.ray.intersectsSphere(sphere)) {
            return;
        }
        ray.copy(raycaster.ray).applyMatrix4(inverseMatrix);

        float localThreshold = threshold / ((float)(scale.x + scale.y + scale.z) / 3);
//        float localThresholdSq = localThreshold * localThreshold;
        Vector3 position = new Vector3();
        Vector3 intersectPoint = new Vector3();

        BufferAttribute index = geometry.getIndex();
        float[] positions = geometry.position.arrayFloat;
        if (index != null) {
            int[] indices = index.arrayInt;
            for (int i = 0; i < indices.length; i++) {
                int a = indices[i];
                position.fromArray(positions, a * 3);
                testPoint(position, a, localThreshold, ray, raycaster, intersectPoint, intersects);
            }
        } else {
            for (int i = 0; i < positions.length / 3; i++) {
                position.fromArray(positions, i * 3);
                testPoint(position, i, localThreshold, ray, raycaster, intersectPoint, intersects);
            }
        }
    }
}
