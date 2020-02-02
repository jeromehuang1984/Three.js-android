package edu.three.geometries.param;

import edu.three.geometries.CylinderGeometry;

public class CylinderParam {
    public float radiusTop = 1;
    public float radiusBottom = 1;
    public float height = 2;
    public int radialSegments = 8;
    public int heightSegments = 1;
    public boolean openEnded = false;
    public float thetaStart = 0;
    public float thetaLength = (float) Math.PI * 2;

    public CylinderParam setHeight(float h) {
        height = h;
        return this;
    }

    public CylinderParam setTopR(float topRadius) {
        radiusTop = topRadius;
        return this;
    }
    public CylinderParam setBottomR(float bottomRadius) {
        radiusBottom = bottomRadius;
        return this;
    }
    public CylinderParam setRadialSegments(int radialSegments) {
        this.radialSegments = radialSegments;
        return this;
    }
    public CylinderParam setHeightSegments(int heightSegments) {
        this.heightSegments = heightSegments;
        return this;
    }
}
