package edu.three.geometries;

import edu.three.buffergeometries.CylinderBufferGeometry;
import edu.three.core.Geometry;
import edu.three.geometries.param.CylinderParam;

public class CylinderGeometry extends Geometry {

    public CylinderGeometry(CylinderParam param) {
        fromBufferGeometry(new CylinderBufferGeometry(param));
    }
}
