package edu.three.materials;

import edu.three.math.Color;
import edu.three.math.Vector2;
import edu.three.math.Vector4;
import edu.three.textures.Texture;

import static edu.three.constant.Constants.TangentSpaceNormalMap;

public class MeshMatcapMaterial extends Material {
    public MeshMatcapMaterial() {
        normalMapType = TangentSpaceNormalMap;
        color = new Color().setHex(0xffffff);// diffuse
        lights = false;
    }
    //matcap`
//    public Texture map = null;

    //bumpMap
    public float bumpScale = 1;

    //normalMap
    public Vector2 normalScale = new Vector2(1, 1);

    //displacementMap
    public float displacementScale = 1;
    public float displacementBias = 0;

//    public Texture alphaMap = null;

    //skinning
    //morphTargets
    //morphNormals

//    public boolean lights = false;
}
