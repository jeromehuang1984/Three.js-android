package edu.three.objects;

import edu.three.buffergeometries.CylinderBufferGeometry;
import edu.three.core.BufferGeometry;
import edu.three.geometries.param.CylinderParam;
import edu.three.materials.Material;
import edu.three.math.Matrix4;
import edu.three.math.Vector3;

public class Arrow extends Mesh {
    float length = 0.3f;
    float topR = 0.15f;

    public Arrow(Material material) {
        super(null, material);
    }

    public Arrow set(Vector3 dir, Vector3 origin, float length) {
        setLength(length);
        if (origin != null) {
            position.copy(origin);
        }
        // set quaternion
        if (dir.y > 0.99999f) {
            quaternion.set(0, 0, 0, 1);
        } else if ( dir.y < - 0.99999f ) {
            quaternion.set( 1, 0, 0, 0 );
        } else {
            Vector3 axis = new Vector3();
            axis.set( dir.z, 0, - dir.x ).normalize();
            float radians = (float) Math.acos( dir.y );
            quaternion.setFromAxisAngle( axis, radians );
        }
        matrix.compose(position, quaternion, new Vector3(1, 1, 1));
        matrix.multiply(new Matrix4().makeTranslation(0, length/2, 0));
        matrix.decompose(position, quaternion, scale);
        return this;
    }

    public Arrow setLength(float length) {
        this.length = length;
        geometry = new CylinderBufferGeometry(new CylinderParam().setHeight(length).setTopR(topR).setBottomR(0));
        return this;
    }

    public Arrow setWidth(float width) {
        topR = width/2;
        geometry = new CylinderBufferGeometry(new CylinderParam().setHeight(length).setTopR(topR).setBottomR(0));
        return this;
    }
}
