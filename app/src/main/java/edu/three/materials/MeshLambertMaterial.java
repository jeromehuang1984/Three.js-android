package edu.three.materials;

import edu.three.constant.Constants;
import edu.three.math.Color;
import edu.three.textures.CubeTexture;
import edu.three.textures.Texture;

public class MeshLambertMaterial extends Material {
//    int color = 0xffffff;
//    float opacity;

    //Texture map = null;
    //lightMap
    //lightMapIntensity
    //Texture aoMap = null;
    //float aoMapIntensity;

    //int emissive = 0x000000;
    //float emissiveIntensity = 1f;
    //emissiveMap

//    Texture specularMap = null;
//    Texture alphaMap = null;

//    float reflectivity = 1f;
//    float refractionRatio = 0.98f;

//    boolean wireframe = false;
//    int wireframeLinewidth = 1;
    public String wireframeLinecap = "round";
    public String wireframeLinejoin = "round";

//    boolean skinning = false;

    public MeshLambertMaterial() {
        lightMapIntensity = 1;
        color = new Color().setHex(0xffffff);
        emissive = new Color().setHex(0x000000);
        emissiveIntensity = 1f;
        reflectivity = 1f;
        refractionRatio = 0.98f;
        wireframeLinewidth = 1;
        skinning = false;
    }

    public MeshLambertMaterial copy(MeshLambertMaterial source) {
        super.copy(source);
        combine = Constants.MultiplyOperation;
        color = source.color;
        map = source.map;

        lightMap = source.lightMap;
        lightMapIntensity = source.lightMapIntensity;
        aoMap = source.aoMap;
        aoMapIntensity = source.aoMapIntensity;

        emissive = source.emissive;
        emissiveMap = source.emissiveMap;
        emissiveIntensity = source.emissiveIntensity;

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
