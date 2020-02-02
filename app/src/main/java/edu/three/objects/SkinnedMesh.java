package edu.three.objects;

import android.util.Log;

import java.util.ArrayList;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.materials.Material;
import edu.three.math.Matrix4;
import edu.three.math.Vector2;
import edu.three.math.Vector4;

public class SkinnedMesh extends Mesh {
    public String bindMode = "bindMode";
    public Matrix4 bindMatrix = new Matrix4();
    public Matrix4 bindMatrixInverse = new Matrix4();
    public Skeleton skeleton;

    public SkinnedMesh(BufferGeometry geometry, ArrayList<Material> material) {
        super(geometry, material);
    }

    public void bind(Skeleton skeleton, Matrix4 bindMatrix) {
        this.skeleton = skeleton;

        if ( bindMatrix == null ) {

            this.updateMatrixWorld( true );

            this.skeleton.calculateInverses();

            bindMatrix = this.matrixWorld;

        }

        this.bindMatrix.copy( bindMatrix );
        this.bindMatrixInverse.getInverse( bindMatrix );
    }

    public void pose() {
        skeleton.pose();
    }

    public void normalizeSkinWeights() {
        Vector4 vector = new Vector4();
        BufferAttribute skinWeight = geometry.getAttribute("skinWeight");

        for(int i = 0; i < skinWeight.getCount(); i++) {
            vector.x = skinWeight.getX( i );
            vector.y = skinWeight.getY( i );
            vector.z = skinWeight.getZ( i );
            vector.w = skinWeight.getW( i );

            float scale = 1.0f / vector.manhattanLength();

            if ( scale != Float.POSITIVE_INFINITY ) {
                vector.multiplyScalar( scale );
            } else {
                vector.set( 1, 0, 0, 0 ); // do something reasonable
            }

            skinWeight.setXYZW( i, vector.x, vector.y, vector.z, vector.w );
        }
    }

    public void updateMatrixWorld(boolean force) {
        super.updateMatrixWorld(force);
        if (bindMode.equals("attached")) {
            bindMatrixInverse.getInverse(matrixWorld);
        } else if (bindMode.equals("detached")) {
            bindMatrixInverse.getInverse(bindMatrix);
        } else {
            Log.w(getClass().getSimpleName(), "THREE.SkinnedMesh: Unrecognized bindMode: " + bindMode);
        }
    }

    public SkinnedMesh clone() {
        SkinnedMesh mesh = new SkinnedMesh(geometry, material);
        return (SkinnedMesh) mesh.copy(this);
    }
}
