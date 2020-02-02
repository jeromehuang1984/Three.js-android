package edu.three.geometries.param;

public class PlaneParam {
    public float width = 1;
    public float height = 1;
    public int widthSegments = 1;
    public int heightSegments = 1;

    public PlaneParam(float width, float height) {
        this(width, height, 1, 1);
    }
    public PlaneParam(float width, float height, int widthSegments, int heightSegments) {
        this.width = width;
        this.height = height;
        this.widthSegments = widthSegments;
        this.heightSegments = heightSegments;
    }
}
