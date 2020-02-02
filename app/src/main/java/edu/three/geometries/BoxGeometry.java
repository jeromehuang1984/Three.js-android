package edu.three.geometries;

import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.core.Geometry;
import edu.three.geometries.param.BoxParam;

public class BoxGeometry extends Geometry {
    public BoxGeometry(BoxParam param) {
        fromBufferGeometry(new BoxBufferGeometry(param));
    }
}
