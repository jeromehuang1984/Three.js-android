package edu.three.objects;

import java.nio.FloatBuffer;

import edu.three.core.Object3D;
import edu.three.materials.Material;

public class ImmediateRenderObject extends Object3D {
    public Material material;
    public boolean hasPositions;
    public boolean hasNormals;
    public boolean hasUvs;
    public boolean hasColors;
    public int count;

    public FloatBuffer positionArray;
    public FloatBuffer normalArray;
    public FloatBuffer uvArray;
    public FloatBuffer colorArray;

    public int positionArraySize;
    public int normalArraySize;
    public int uvArraySize;
    public int colorArraySize;

    //needs to be override by subclass
    public void render(RenderCallback callback) {
    }

    public interface RenderCallback {
        void renderCallback(ImmediateRenderObject object);
    }
}
