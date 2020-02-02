package edu.three.geometries.param;

public class SphereParam {
    public float radius = 1;
    public int widthSegments = 8;
    public int heightSegments = 6;
    public float phiStart = 0;
    public float phiLength = (float) Math.PI * 2;
    public float thetaStart = 0;
    public float thetaLength = (float) Math.PI;

    public SphereParam(){}
    public SphereParam(float radius, int widthSegments, int heightSegments) {
        this.radius = radius;
        this.widthSegments = widthSegments;
        this.heightSegments = heightSegments;
    }


}
