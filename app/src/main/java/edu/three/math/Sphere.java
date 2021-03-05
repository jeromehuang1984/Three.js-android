package edu.three.math;

public class Sphere {
    public Vector3 center = new Vector3();
    public double radius;

    public Sphere set(Vector3 center, double radius) {
        this.center.copy(center);
        this.radius = radius;
        return this;
    }

    public Sphere setFromPoints(Vector3[] points) {
        return setFromPoints(points, null);
    }
    public Sphere setFromPoints(Vector3[] points, Vector3 optionalCenter) {
        Box3 box = new Box3();
        if (optionalCenter != null) {
            center.copy(optionalCenter);
        } else {
            center = box.setFromPoints(points).getCenter();
        }
        double maxRadiusSq = 0;
        for (int i = 0; i < points.length; i++) {
            maxRadiusSq = Math.max(maxRadiusSq, center.distanceToSquared(points[i]));
        }
        radius =  Math.sqrt(maxRadiusSq);
        return this;
    }

    public Sphere applyMatrix4(Matrix4 matrix) {
        center.applyMatrix4(matrix);
        radius = radius * matrix.getMaxScaleOnAxis();
        return this;
    }

    public Sphere clone() {
        return new Sphere().set(center, radius);
    }

    public Sphere copy(Sphere sphere) {
        center.copy(sphere.center);
        radius = sphere.radius;
        return this;
    }

    public boolean isEmpty() {
        return radius <= 0;
    }

    public boolean equals(Sphere sphere) {
        return sphere.center.equals(center) && sphere.radius == radius;
    }
}
