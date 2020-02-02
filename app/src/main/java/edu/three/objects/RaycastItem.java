package edu.three.objects;

import edu.three.core.Face3;
import edu.three.core.Object3D;
import edu.three.math.Triangle;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class RaycastItem {
    public float distance;
    public Vector3 point;
    public Object3D object;
    public int faceIndex;
    public Face3 face;

    public float distanceToRay;
    public int index;

    public Vector2 uv;

    public Triangle triangle;
}
