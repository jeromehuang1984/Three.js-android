package edu.three.materials;

import edu.three.constant.Constants;
import edu.three.math.Color;
import edu.three.math.Vector2;
import edu.three.textures.CubeTexture;
import edu.three.textures.Texture;

public class MeshPhongMaterial extends Material {
    public Color specular = new Color().setHex(0x111111);
    public float shininess = 30;

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

    //specularMap
//    public Texture alphaMap = null;

//    public float reflectivity = 1f;
//    public float refractionRatio = 0.98f;

//    public boolean wireframe = false;
//    public int wireframeLinewidth = 1;
    public String wireframeLinecap = "round";
    public String wireframeLinejoin = "round";

    //skinning
    //morphTargets
    //morphNormals

    public MeshPhongMaterial() {
        color = new Color().setHex(0xffffff);
        emissive = new Color().setHex(0x000000);
        normalMapType = Constants.TangentSpaceNormalMap;
        emissiveIntensity = 1f;
        lightMapIntensity = 1f;
        aoMapIntensity = 1f;

        alphaMap = null;
        reflectivity = 1f;
        refractionRatio = 0.98f;
        wireframe = false;
        wireframeLinewidth = 1;
    }

    public MeshPhongMaterial copy(MeshPhongMaterial source) {
        super.copy(source);
        combine = Constants.MultiplyOperation;
        color = source.color;
        specular = source.specular;
        shininess = source.shininess;
        map = source.map;

        lightMap = source.lightMap;
        lightMapIntensity = source.lightMapIntensity;
        aoMap = source.aoMap;
        aoMapIntensity = source.aoMapIntensity;

        emissive = source.emissive;
        emissiveMap = source.emissiveMap;
        emissiveIntensity = source.emissiveIntensity;

        bumpMap = source.bumpMap;
        bumpScale = source.bumpScale;

        normalMap = source.normalMap;
        normalMapType = source.normalMapType;
        normalScale.copy(source.normalScale);

        specularMap = source.specularMap;
        alphaMap = source.alphaMap;

        envMap = source.envMap;
        combine = source.combine;
        reflectivity = source.reflectivity;
        refractionRatio = source.refractionRatio;

        wireframe = source.wireframe;
        wireframeLinewidth = source.wireframeLinewidth;
        wireframeLinecap = source.wireframeLinecap;
        wireframeLinejoin = source.wireframeLinejoin;

        skinning = source.skinning;
        return this;
    }
}
