package edu.three.materials;

import java.util.HashMap;

import edu.three.constant.Constants;
import edu.three.math.Color;
import edu.three.math.Vector2;
import edu.three.textures.Texture;

public class MeshStandardMaterial extends Material {
//    public HashMap<String, String> defines = new HashMap<>();
//    {
//        defines.put("STANDARD", "");
//    }

//    public int color = 0xffffff;   // diffuse
    public Texture roughnessMap = null;
    public float roughness = 0.5f;
    public Texture metalnessMap = null;
    public float metalness = 0.5f;

//    public Texture map = null;
    //lightMap
//    public float lightMapIntensity = 1f;

    //aoMap
//    public float aoMapIntensity = 1f;

//    public int emissive = 0x000000;
    //emissiveIntensity
    //emissiveMap

    //bumpMap
    public float bumpScale = 1f;

    //normalMap
    //normalMapType
    public Vector2 normalScale = new Vector2(1, 1);

    //displacementMap
    public float displacementScale = 1;
    public float displacementBias = 0;

    //roughnessMap
    //metalnessMap

//    public Texture alphaMap = null;

//    public Texture envMap = null;
    public float envMapIntensity = 1f;

//    public float refractionRatio = 0.98f;

//    public boolean wireframe = false;
//    public int wireframeLinewidth = 1;
    public String wireframeLinecap = "round";
    public String wireframeLinejoin = "round";

    //skinning
    //morphTargets
    //morphNormals
    public MeshStandardMaterial() {
        normalMapType = Constants.TangentSpaceNormalMap;
        emissiveIntensity = 1f;
        wireframe = false;
        wireframeLinewidth = 1;
        refractionRatio = 0.98f;
        envMapIntensity = 1f;
        emissive = new Color().setHex(0x000000);
        color = new Color().setHex(0xffffff);
        aoMapIntensity = 1f;
        lightMapIntensity = 1f;

        defines.put("STANDARD", "");
    }
}
