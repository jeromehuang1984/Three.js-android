package edu.three.objects;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import edu.three.math.Matrix4;
import edu.three.textures.Texture;

public class Skeleton {
    public String TAG = getClass().getSimpleName();
    public ArrayList<Bone> bones = new ArrayList<>();
    public Matrix4[] boneInverses;
    public double[] boneMatrices;
    public Texture boneTexture;

    public int boneTextureSize;

    public Skeleton(ArrayList<Bone> bones, Matrix4[] boneInverses) {
        this.bones = bones;
        boneMatrices = new double[bones.size() * 16];
        if (boneInverses != null) {
            calculateInverses();
        } else {
            if (this.bones.size() == boneInverses.length) {
                System.arraycopy(boneInverses, 0, this.boneInverses, 0, boneInverses.length);
            } else {
                Log.w(TAG, "THREE.Skeleton boneInverses is the wrong length.");
                this.boneInverses = new Matrix4[this.bones.size()];
                int bCount = 0;
                for (int i = 0; i < this.bones.size(); i++) {
                    this.boneInverses[bCount++] = new Matrix4();
                }
            }
        }
    }

    public void calculateInverses() {
        this.boneInverses = new Matrix4[bones.size()];
        int bCount = 0;
        for (int i = 0; i < bones.size(); i++) {
            Matrix4 inverse = new Matrix4();
            if (bones.get(i) != null) {
                inverse.getInverse(bones.get(i).getWorldMatrix());
            }
            boneInverses[bCount++] = inverse;
        }
    }

    public void pose() {
        Bone bone;
        // recover the bind-time world matrices
        for (int i = 0; i < bones.size(); i++) {
            bone = bones.get(i);
            if (bone != null) {
                bone.getWorldMatrix().getInverse(boneInverses[i]);
            }
        }
        // compute the local matrices, positions, rotations and scales
        for (int i = 0; i < bones.size(); i++) {
            bone = bones.get(i);
            if (bone != null) {
                if ( bone.getParent() != null && bone.getParent() instanceof Bone ) {

                    bone.getModelMatrix().getInverse( bone.getParent().getWorldMatrix() );
                    bone.getModelMatrix().multiply( bone.getWorldMatrix() );

                } else {

                    bone.getModelMatrix().copy( bone.getWorldMatrix() );

                }

                bone.getModelMatrix().decompose( bone.position, bone.quaternion, bone.scale );
            }
        }
    }

    public void update() {
        Matrix4 offsetMatrix = new Matrix4();
        Matrix4 identityMatrix = new Matrix4();

        // flatten bone matrices to array
        for (int i = 0; i < bones.size(); i++) {
            // compute the offset between the current and the original transform
            Matrix4 matrix = bones.get(i) != null ? bones.get(i).getWorldMatrix() : identityMatrix;
            offsetMatrix.multiplyMatrices(matrix, boneInverses[i]);
            offsetMatrix.toArray(boneMatrices, i * 16);
        }
        if (boneTexture != null) {
            boneTexture.setNeedsUpdate(true);
        }
    }

    public Skeleton clone() {
        return new Skeleton(bones, boneInverses);
    }

    public Bone getBoneByName(String name) {
        for (int i = 0; i < bones.size(); i++) {
            Bone bone = bones.get(i);
            if (bone.name.equals(name) ) {
                return bone;
            }
        }
        return null;
    }
}
