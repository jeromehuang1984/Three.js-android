package edu.three.objects;

import android.util.Log;

import edu.three.core.BufferAttribute;
import edu.three.math.Vector3;

public class LineSegments extends Line {
    public LineSegments computeLineDistances() {
        Vector3 start = new Vector3();
        Vector3 end = new Vector3();
        // we assume non-indexed geometry
        if (geometry.getIndex() == null) {
            BufferAttribute postion = geometry.position;
            float[] lineDistances = new float[postion.getCount()];
            for (int i = 0; i < postion.getCount(); i += 2) {
                start.fromBufferAttribute(postion, i );
                end.fromBufferAttribute(postion, i + 1);
                lineDistances[ i ] = ( i == 0 ) ? 0 : lineDistances[ i - 1 ];
                lineDistances[ i + 1 ] += lineDistances[ i ] + start.distanceTo( end );
            }
            geometry.addAttribute("lineDistance", new BufferAttribute().setArray(lineDistances).setItemSize(1));
        } else {
            Log.e(TAG, "THREE.LineSegments.computeLineDistances(): Computation only possible with non-indexed BufferGeometry.");
        }
        return this;
    }
}
