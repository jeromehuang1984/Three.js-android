package edu.three.math;

public class Plane {
    public String name;
    public Vector3 normal = new Vector3();
    public double constant;

    public Plane set(Vector3 normal, double constant) {
        this.normal.copy(normal);
        this.constant = constant;
        return this;
    }

    public Plane setComponents(double x, double y, double z, double w) {
        normal.set(x, y, z);
        constant = w;
        return this;
    }

    public Plane setFromNormalAndCoplanarPoint(Vector3 normal, Vector3 point) {
        this.normal.copy(normal);
        constant = - point.dot(normal);
        return this;
    }

    public Plane setFromCoplanarPoints(Vector3 a, Vector3 b, Vector3 c) {
        Vector3 v1 = new Vector3();
        Vector3 v2 = new Vector3();

        Vector3 normal = v1.subVectors(c, b).cross( v2.subVectors(a, b) ).normalize();
        return setFromNormalAndCoplanarPoint(normal, a);
    }

    public Plane copy(Plane plane) {
        return set(plane.normal, plane.constant);
    }

    public Plane clone() {
        return new Plane().copy(this);
    }

    public Plane normalize() {
        // Note: will lead to a divide by zero if the plane is invalid.
        double inverseNormalLength = 1f / normal.length();
        normal.multiplyScalar(inverseNormalLength);
        constant *= inverseNormalLength;
        return this;
    }

    public Plane negate() {
        constant *= -1;
        normal.negate();
        return this;
    }

    public double distanceToPoint(Vector3 point) {
        return normal.dot(point) + constant;
    }

    public double distanceToSphere(Sphere sphere) {
        return distanceToPoint(sphere.center) - sphere.radius;
    }

    public Vector3 projectPoint(Vector3 point, Vector3 target) {
        if (target == null) {
            target = new Vector3();
        }
        return target.copy(normal).multiplyScalar(- distanceToPoint(point)).add(point);
    }

    public Vector3 intersectLine(Line3 line, Vector3 target) {
        Vector3 v1 = new Vector3();
        if (target == null) {
            target = new Vector3();
        }
        Vector3 direction = line.delta(v1);
        double denominator = normal.dot(direction);
        if (denominator == 0) {
            // line is coplanar, return origin
            if (distanceToPoint(line.start) == 0) {
                return target.copy(line.start);
            }
            // Unsure if this is the correct method to handle this case.
            return null;
        }

        double t = -(line.start.dot(normal) + constant) / denominator;
        if (t < 0 || t > 1) {
            return null;
        }
        return target.copy(direction).multiplyScalar(t).add(line.start);
    }

    // Note: this tests if a line intersects the plane, not whether it (or its end-points) are coplanar with it.
    public boolean intersectsLine(Line3 line) {
        double startSign = distanceToPoint(line.start);
        double endSign = distanceToPoint(line.end);
        return ( startSign < 0 && endSign > 0 ) || ( endSign < 0 && startSign > 0 );
    }

//    public boolean intersectsBox(Box3 box) {
//        return box.intersectsPlane(this);
//    }

    public Vector3 coplanarPoint(Vector3 target) {
        if (target == null) {
            target = new Vector3();
        }
        return target.copy(normal).multiplyScalar(- constant);
    }

    public Plane applyMatrix4(Matrix4 matrix, Matrix3 optionalNormalMatrix) {
        Vector3 v1 = new Vector3();
        Matrix3 normalMatrix = optionalNormalMatrix;
        if (normalMatrix == null) {
            normalMatrix = new Matrix3().getNormalMatrix(matrix);
        }
        Vector3 referencePoint = coplanarPoint(v1).applyMatrix4(matrix);
        Vector3 normal = this.normal.applyMatrix3(normalMatrix).normalize();
        constant = - referencePoint.dot(normal);
        return this;
    }

    public Plane translate(Vector3 offset) {
        constant -= offset.dot(normal);
        return this;
    }

    public boolean equals(Plane plane) {
        return plane.normal.equals(normal) && plane.constant == constant;
    }
}
