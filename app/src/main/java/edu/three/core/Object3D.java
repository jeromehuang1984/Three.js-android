package edu.three.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import edu.three.cameras.Camera;
import edu.three.lights.Light;
import edu.three.materials.Material;
import edu.three.materials.MeshDepthMaterial;
import edu.three.materials.MeshDistanceMaterial;
import edu.three.math.Box3;
import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Quaternion;
import edu.three.math.Ray;
import edu.three.math.Sphere;
import edu.three.math.Vector3;
import edu.three.objects.RaycastItem;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;

public class Object3D extends EventDispatcher {
    static long object3DId = 0;
    public static Vector3 DefaultUp = new Vector3(0, 1, 0);
    public String name = "";
    public final long id = object3DId++;
    public final String uuid;
    public int renderOrder = 0;

    public ICallback onBeforeRender = new ICallback() {
        @Override
        public void call(GLRenderer renderer, Scene scene, Camera camera) {
        }
    };

    public ICallback onAfterRender = new ICallback() {
        @Override
        public void call(GLRenderer renderer, Scene scene, Camera camera) {
        }
    };

    public boolean matrixAutoUpdate = true;
    private boolean matrixWorldNeedsUpdate = false;
    public Vector3 position = new Vector3();
    public Quaternion quaternion = new Quaternion();
    public Vector3 scale = new Vector3(1, 1, 1);

    protected Matrix4 matrixWorld = new Matrix4();
    protected Matrix4 matrix = new Matrix4();
    public Matrix4 modelViewMatrix = new Matrix4();
    protected Matrix3 normalMatrix = new Matrix3();

    public boolean visible = true;
    public boolean castShadow = false;
    public boolean receiveShadow = false;
    public boolean frustumCulled = true;
    public Layers layers = new Layers();
    public String userData = "";

    private Object3D parent = null;
    public ArrayList<Object3D> children = new ArrayList<>();

    public Vector3 up = DefaultUp.clone();
    private Matrix4 _m1 = new Matrix4();

    public MeshDepthMaterial customDepthMaterial = null;
    public MeshDistanceMaterial customDistanceMaterial = null;

    public Object3D() {
        uuid = UUID.randomUUID().toString();
    }

    public void applyMatrix(Matrix4 matrix) {
        if (matrixAutoUpdate) {
            updateMatrix();
        }
        this.matrix.premultiply(matrix);
        this.matrix.decompose(position, quaternion, scale);
    }

    public void clearMatrix() {
        matrix.identity();
        matrix.decompose(position, quaternion, scale);
        matrixWorld.identity();
    }
    public void decomposeMatrix() {
        matrix.decompose(position, quaternion, scale);
    }

    public Object3D applyQuaternion(Quaternion q) {
        quaternion.premultiply(q);
        return this;
    }

    // assumes axis is normalized
    public void setRotationFromAxisAngle(Vector3 axis, float angle) {
        quaternion.setFromAxisAngle( axis, angle );
    }
    // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)
    public void setRotationFromMatrix(Matrix4 m) {
        quaternion.setFromRotationMatrix(m);
    }
    // assumes q is normalized
    public void setRotationFromQuaternion(Quaternion q) {
        quaternion.copy(q);
    }

    // rotate object on axis in object space
    // axis is assumed to be normalized
    public Object3D rotateOnAxis(Vector3 axis, float angle) {
        Quaternion q1 = new Quaternion();
        q1.setFromAxisAngle(axis, angle);
        quaternion.multiply(q1);
        return this;
    }

    // rotate object on axis in world space
    // axis is assumed to be normalized
    // method assumes no rotated parent
    public Object3D rotateOnWorldAxis(Vector3 axis, float angle) {
        Quaternion q1 = new Quaternion();
        q1.setFromAxisAngle(axis, angle);
        quaternion.premultiply( q1 );
        return this;
    }

    public Object3D rotateX(float angle) {
        return rotateOnAxis(new Vector3(1, 0, 0), angle);
    }

    public Object3D rotateY(float angle) {
        return rotateOnAxis(new Vector3(0, 1, 0), angle);
    }

    public Object3D rotateZ(float angle) {
        return rotateOnAxis(new Vector3(0, 0, 1), angle);
    }

    // translate object by distance along axis in object space
    // axis is assumed to be normalized
    public Object3D translateOnAxis(Vector3 axis, float distance) {
        Vector3 v1 = new Vector3();
        v1.copy(axis).applyQuaternion(quaternion);
        position.add(v1.multiplyScalar(distance));
        return this;
    }

    public Object3D translateX(float distance) {
        return translateOnAxis(new Vector3(1, 0, 0), distance);
    }

    public Object3D translateY(float distance) {
        return translateOnAxis(new Vector3(0, 1, 0), distance);
    }

    public Object3D translateZ(float distance) {
        return translateOnAxis(new Vector3(0, 0, 1), distance);
    }

    public Vector3 localToWorld(Vector3 vector) {
        return vector.applyMatrix4(matrixWorld);
    }

    public Vector3 worldToLocal(Vector3 vector) {
        Matrix4 m1 = new Matrix4();
        return vector.applyMatrix4((m1.getInverse(matrixWorld) ) );
    }

    public Matrix3 getNormalMatrix() {
        return normalMatrix;
    }
//    public Matrix4 getModelViewMatrix() {
//        return modelViewMatrix;
//    }
    public Matrix4 getModelMatrix() {
        return matrix;
    }

    public Object3D setWorldMatrix(Matrix4 modelMatrix) {
        matrixWorld = modelMatrix;
        return this;
    }

    public Matrix4 getWorldMatrix() {
        return matrixWorld;
    }

    public void updateMatrix() {
        matrix.compose(position, quaternion, scale);
        matrixWorldNeedsUpdate = true;
    }

    public void updateMatrixWorld() {
        updateMatrixWorld(false);
    }

    public void updateMatrixWorld(boolean force) {
        if (matrixAutoUpdate) {
            updateMatrix();
        }
        if (matrixWorldNeedsUpdate || force) {
            if (parent == null) {
                matrixWorld.copy(matrix);
            } else {
                matrixWorld.multiplyMatrices(parent.matrixWorld, matrix);
            }
//            modelViewMatrix.multiplyMatrices(Camera.getInstance().matrixWorldInverse, matrixWorld);
//            normalMatrix.setFromMatrix4(modelViewMatrix);
            matrixWorldNeedsUpdate = false;
            force = true;
        }
        // update children
        for ( int i = 0; i < children.size(); i++ ) {
            children.get(i).updateMatrixWorld( force );
        }
    }

    private void updateWorldMatrix(boolean updateParents, boolean updateChildren) {
        if (updateParents && parent != null) {
            parent.updateWorldMatrix(true, false);
        }
        if (matrixAutoUpdate) {
            updateMatrix();
        }
        if (parent == null) {
            matrixWorld.copy(matrix);
        } else {
            matrixWorld.multiplyMatrices(parent.matrixWorld, matrix);
        }

        // update children
        if (updateChildren) {
            for (int i = 0; i < children.size(); i++) {
                children.get(i).updateWorldMatrix(false, true);
            }
        }
    }

    public void add(Object3D child) {
        if (child == null || child == this) {
            return;
        }
        if (child.parent != null) {
            child.parent.remove(child);
        }
        child.parent = this;
        children.add(child);
    }

    public void remove(Object3D child) {
        child.parent = null;
        children.remove(child);
    }

    // adds object as a child of this, while maintaining the object's world transform
    public Object3D attach(Object3D object) {
        Matrix4 m = new Matrix4();
        updateWorldMatrix(true, false);
        m.getInverse(matrixWorld);
        if (object.parent != null) {
            object.parent.updateWorldMatrix(true, false);
            m.multiply(object.parent.matrixWorld);
        }
        object.applyMatrix(m);
        object.updateWorldMatrix(false, false);
        add(object);
        return this;
    }

    public Vector3 getWorldPosition(Vector3 target) {
        updateMatrixWorld(true);
        return target.setFromMatrixPosition(matrixWorld);
    }

    public Quaternion getWorldQuaternion(Quaternion target) {
        Vector3 position = new Vector3();
        Vector3 scale = new Vector3();
        updateMatrixWorld(true);
        matrixWorld.decompose(position, target, scale);
        return target;
    }

    public Vector3 getWorldScale(Vector3 target) {
        Vector3 position = new Vector3();
        Quaternion quaternion = new Quaternion();
        updateMatrixWorld(true);
        matrixWorld.decompose(position, quaternion, target);
        return target;
    }

    public Vector3 getWorldDirection(Vector3 target) {
        updateMatrixWorld(true);
        float[] e = matrixWorld.te;
        return target.set( e[ 8 ], e[ 9 ], e[ 10 ] ).normalize();
    }

    public Object3D getParent() {
        return parent;
    }

    public void lookAt(Vector3 target) {
        updateWorldMatrix(true, false);
        position.setFromMatrixPosition(matrixWorld);
        if (this instanceof Camera || this instanceof Light) {
            _m1.lookAt(position, target, up);
        } else {
            _m1.lookAt(target, position, up);
        }
        quaternion.setFromRotationMatrix(_m1);
        if (parent != null) {
            _m1.extractRotation(parent.matrixWorld);
            Quaternion q1 = new Quaternion().setFromRotationMatrix(_m1);
            quaternion.premultiply(q1.inverse());
        }
    }

    public float[] getDrawMatrixElements() {
        return modelViewMatrix.toArray();
    }

    public Object3D copy(Object3D source) {
        return copy(source, true);
    }
    public Object3D copy(Object3D source, boolean recursive) {
        name = source.name;
        up.copy(source.up);

        position.copy(source.position);
        quaternion.copy(source.quaternion);
        scale.copy(source.scale);

        matrix.copy(source.matrix);
        matrixWorld.copy(source.matrixWorld);

        matrixAutoUpdate = source.matrixAutoUpdate;
        matrixWorldNeedsUpdate = source.matrixWorldNeedsUpdate;

        layers.mask = source.layers.mask;
        visible = source.visible;

        castShadow = source.castShadow;
        receiveShadow = source.receiveShadow;

        userData = String.copyValueOf(source.userData.toCharArray());
        if (recursive) {
            for (int i = 0; i < source.children.size(); i++) {
                Object3D child = source.children.get(i);
                add(child.clone());
            }
        }
        return this;
    }

    public Object3D clone() {
        return new Object3D().copy(this, true);
    }

    public void raycast(Raycaster raycaster, ArrayList<RaycastItem> intersects) {

    }

    public float[] morphTargetInfluences = new float[0];
    public HashMap<String, Integer> morphTargetDictionary = new HashMap<>();

    public BufferGeometry geometry = null;

    public ArrayList<Material> material = new ArrayList<>();
}
