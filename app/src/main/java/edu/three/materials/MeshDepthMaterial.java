package edu.three.materials;

import edu.three.textures.Texture;

import static edu.three.constant.Constants.BasicDepthPacking;

public class MeshDepthMaterial extends Material {
//    public int depthPacking = BasicDepthPacking;

    //morphTargets
    //skinning

    //displacementMap
    public float displacementScale = 1;
    public float displacementBias = 0;

//    public Texture map = null;

//    public Texture alphaMap = null;

//    public float reflectivity = 1f;
//    public float refractionRatio = 0.98f;

//    public boolean wireframe = false;
//    public int wireframeLinewidth = 1;

//    public boolean fog = false;
//    public boolean lights = false;

    public MeshDepthMaterial() {
        depthPacking = BasicDepthPacking;
        reflectivity = 1f;
        refractionRatio = 0.98f;
        wireframe = false;
        wireframeLinewidth = 1;
        fog = false;
        lights = false;
    }
}
