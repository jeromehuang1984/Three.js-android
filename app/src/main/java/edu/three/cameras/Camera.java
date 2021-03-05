package edu.three.cameras;

import edu.three.core.Object3D;
import edu.three.math.Matrix4;
import edu.three.math.Vector3;

public class Camera extends Object3D {
    public Matrix4 matrixWorldInverse = new Matrix4();
    public Matrix4 projectionMatrix = new Matrix4();
    public Matrix4 projectionMatrixInverse = new Matrix4();
    public double[] projectionArr;

    private static Camera instance;
    public static Camera getInstance(){
        if(instance == null){
            instance = new Camera();
        }
        return instance;
    }

    public Camera set(Matrix4 matrixWorld, Matrix4 projectionMatrix) {
        this.matrixWorld.copy(matrixWorld);
        this.projectionMatrix.copy(projectionMatrix);
        projectionMatrixInverse.getInverse(projectionMatrix);
        matrixWorldInverse.getInverse(matrixWorld);
        projectionArr = projectionMatrix.toArray();
        return this;
    }

    public Camera updateWorldMatrix(Matrix4 matrix) {
        matrixWorld.copy(matrix);
        matrixWorldInverse.getInverse(matrix);
        return this;
    }

    public void updateProjectionMatrix() {}

    public void updateMatrixWorld(boolean force) {
        super.updateMatrixWorld(force);
        matrixWorldInverse.getInverse(matrixWorld);
        int a = 1;
    }

    @Override
    public Object3D copy(Object3D obj, boolean recursive) {
        Camera camera = (Camera) obj;
        super.copy(camera, recursive);
        matrixWorldInverse.copy(camera.matrixWorldInverse);
        projectionMatrix.copy(camera.projectionMatrix);
        projectionMatrixInverse.copy(camera.projectionMatrixInverse);
        projectionArr = projectionMatrix.toArray();
        return this;
    }

    public double near = 1;
    public double far = 40;
}
