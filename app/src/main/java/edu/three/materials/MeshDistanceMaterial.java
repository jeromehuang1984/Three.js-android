package edu.three.materials;

import edu.three.math.Vector3;
import edu.three.textures.Texture;

public class MeshDistanceMaterial extends Material {
    //skinning
    public Vector3 referencePosition = new Vector3();
    public float nearDistance = 1f;
    public float farDistance = 1000f;

    //morphTargets

//    public Texture map = null;

//    public Texture alphaMap = null;

    //displacementMap
    public float displacementScale = 1;
    public float displacementBias = 0;

//    public boolean fog = false;
//    public boolean lights = false;

    public MeshDistanceMaterial() {
        fog = false;
        lights = false;
    }
}
