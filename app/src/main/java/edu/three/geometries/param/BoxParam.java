package edu.three.geometries.param;

public class BoxParam {
    public float width = 1;
    public float height = 1;
    public float depth = 1;
    public int widthSegments = 1;
    public int heightSegments = 1;
    public int depthSegments = 1;

    public BoxParam() {
    }

    public BoxParam(float width, float height, float depth) {
        this(width, height, depth, 1, 1, 1);
    }
    public BoxParam(float width, float height, float depth,
                    int widthSegments, int heightSegments, int depthSegments) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.widthSegments = widthSegments;
        this.heightSegments = heightSegments;
        this.depthSegments = depthSegments;
    }
}
