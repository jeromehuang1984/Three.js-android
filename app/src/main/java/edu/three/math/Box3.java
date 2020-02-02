package edu.three.math;

import edu.three.core.BufferAttribute;

public class Box3 {
    public Vector3 min, max;
    public Box3() {
        min = new Vector3();
        max = new Vector3();
        makeEmpty();
    }
    public Box3(Vector3 min, Vector3 max) {
        float Infinity = Float.POSITIVE_INFINITY;
        this.min = ( min != null ) ? min : new Vector3( + Infinity, + Infinity, + Infinity );
        this.max = ( max != null ) ? max : new Vector3( - Infinity, - Infinity, - Infinity );
    }

    public Box3 makeEmpty() {
        min.x = min.y = min.z = Float.POSITIVE_INFINITY;
        max.x = max.y = max.z = Float.NEGATIVE_INFINITY;
        return this;
    }

    public boolean containsPoint(Vector3 point) {
        if ( point.x < this.min.x || point.x > this.max.x ||
                point.y < this.min.y || point.y > this.max.y ||
                point.z < this.min.z || point.z > this.max.z ) {
            return false;
        }

        return true;
    }

    public Box3 expandByPoint(Vector3 point) {
        min.min(point);
        max.max(point);
        return this;
    }

    public Box3 expandByVector(Vector3 vector) {
        min.sub(vector);
        max.add(vector);
        return this;
    }

    public Box3 setFromBufferAttribute(BufferAttribute attribute) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;

        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < attribute.getCount(); i++) {
            float x = attribute.getX(i);
            float y = attribute.getY(i);
            float z = attribute.getZ(i);

            if ( x < minX ) minX = x;
            if ( y < minY ) minY = y;
            if ( z < minZ ) minZ = z;

            if ( x > maxX ) maxX = x;
            if ( y > maxY ) maxY = y;
            if ( z > maxZ ) maxZ = z;
        }
        min.set(minX, minY, minZ);
        max.set(maxX, maxY, maxZ);
        return this;
    }

    public Box3 setFromPoints(Vector3[] points) {
        makeEmpty();
        for (int i = 0; i < points.length; i++) {
            expandByPoint(points[i]);
        }
        return this;
    }

    public boolean isEmpty() {
        // this is a more robust check for empty than ( volume <= 0 ) because volume can get positive with two negative axes
        return ( max.x < min.x ) || ( max.y < min.y ) || ( max.z < min.z );

    }

    public Vector3 getCenter() {
        if (isEmpty()) {
            return new Vector3(0, 0, 0);
        } else {
            Vector3 ret = new Vector3();
            return ret.addVectors(min, max).multiplyScalar(0.5f);
        }
    }

    public Vector3 getSize() {
        Vector3 ret = new Vector3(0, 0, 0);
        return isEmpty() ? ret : ret.subVectors(max, min);
    }

    public Box3 copy(Box3 box) {
        min.copy(box.min);
        max.copy(box.max);
        return this;
    }

    public Box3 clone() {
        return new Box3().copy(this);
    }
}
