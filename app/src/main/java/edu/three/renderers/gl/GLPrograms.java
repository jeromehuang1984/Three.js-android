package edu.three.renderers.gl;

import android.nfc.Tag;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.three.constant.Constants;
import edu.three.core.Object3D;
import edu.three.lights.Light;
import edu.three.materials.Material;
import edu.three.materials.MeshStandardMaterial;
import edu.three.renderers.GLRenderTarget;
import edu.three.renderers.GLRenderer;
import edu.three.renderers.shaders.ShaderLib;
import edu.three.scenes.Fog;
import edu.three.scenes.FogExp2;
import edu.three.textures.Texture;

public class GLPrograms {
    String TAG = getClass().getSimpleName();
    public ArrayList<GLProgram> programs = new ArrayList<>();
    HashMap<String, String> shaderIDs = new HashMap<>();
    String[] parameterNames;
    GLRenderer renderer;
    GLCapabilities capabilities;

    public GLPrograms(GLRenderer renderer, GLCapabilities capabilities) {
        this.renderer = renderer;
        this.capabilities = capabilities;
        shaderIDs.put("MeshDepthMaterial", "depth");
        shaderIDs.put("MeshDistanceMaterial", "distanceRGBA");
        shaderIDs.put("MeshNormalMaterial", "normal");
        shaderIDs.put("MeshBasicMaterial", "basic");
        shaderIDs.put("MeshLambertMaterial", "lambert");
        shaderIDs.put("MeshPhongMaterial", "phong");
        shaderIDs.put("MeshToonMaterial", "phong");
        shaderIDs.put("MeshStandardMaterial", "physical");
        shaderIDs.put("MeshPhysicalMaterial", "physical");
        shaderIDs.put("MeshMatcapMaterial", "matcap");
        shaderIDs.put("LineBasicMaterial", "basic");
        shaderIDs.put("LineDashedMaterial", "dashed");
        shaderIDs.put("PointsMaterial", "points");
        shaderIDs.put("ShadowMaterial", "shadow");
        shaderIDs.put("SpriteMaterial", "sprite");

        parameterNames = new String[] {
                "precision", "supportsVertexTextures", "map", "mapEncoding", "matcap", "matcapEncoding", "envMap", "envMapMode", "envMapEncoding",
                "lightMap", "aoMap", "emissiveMap", "emissiveMapEncoding", "bumpMap", "normalMap", "objectSpaceNormalMap", "displacementMap", "specularMap",
                "roughnessMap", "metalnessMap", "gradientMap",
                "alphaMap", "combine", "vertexColors", "vertexTangents", "fog", "useFog", "fogExp",
                "flatShading", "sizeAttenuation", "logarithmicDepthBuffer", "skinning",
                "maxBones", "useVertexTexture", "morphTargets", "morphNormals",
                "maxMorphTargets", "maxMorphNormals", "premultipliedAlpha",
                "numDirLights", "numPointLights", "numSpotLights", "numHemiLights", "numRectAreaLights",
                "shadowMapEnabled", "shadowMapType", "toneMapping", "physicallyCorrectLights",
                "alphaTest", "doubleSided", "flipSided", "numClippingPlanes", "numClipIntersection", "depthPacking", "dithering"
        };
    }

    private int getTextureEncoding(Texture map) {
        return getTextureEncoding(map, false);
    }
    private int getTextureEncoding(Texture map, boolean gammaOverrideLinear) {
        int ret;
        if (map == null) {
            ret = Constants.LinearEncoding;
        } else {
            ret = map.encoding;
        }
        if (ret == Constants.LinearEncoding && gammaOverrideLinear) {
            ret = Constants.GammaEncoding;
        }
        return ret;
    }

    public Param getParameters(Material material, GLLights.State lights, ArrayList<Light> shadows, Fog fog,
                               int nClipPlanes, int nClipIntersection, Object3D object) {
        String shaderID = shaderIDs.get(material.getClass().getSimpleName());

        // heuristics to create shader parameters according to lights in the scene
        // (not to blow over maxLights budget)
        String precision = "highp"; // mediump
        if (material.precision != null) {
            precision = material.precision;
        }
        Param param = new Param();
        param.shaderID = shaderID;
        param.precision = precision;
        param.supportsVertexTextures = capabilities.vertexTextures;
        GLRenderTarget renderTarget = renderer.getRenderTarget();
        param.outputEncoding = getTextureEncoding( ( renderTarget == null ) ?
                null : renderTarget.texture, renderer.gammaOutput );

        param.map = material.map != null;
        if (material.map != null && material.map.flipY)
            param.flipY = true;
        param.mapEncoding = getTextureEncoding(material.map);
        param.matcap = material.matcap != null;
        param.matcapEncoding = getTextureEncoding(material.matcap);
        param.envMap = material.envMap != null;
        if (material.envMap != null) {
            param.envMapMode = material.envMap.mapping;
        }
        param.envMapEncoding = getTextureEncoding(material.envMap);
        param.envMapCubeUV = material.envMap != null && (material.envMap.mapping == Constants.CubeUVReflectionMapping ||
                material.envMap.mapping == Constants.CubeUVRefractionMapping);
        param.lightMap = material.lightMap != null;
        param.aoMap = material.aoMap != null;
        param.emissiveMap = material.emissiveMap != null;
        param.emissiveMapEncoding = getTextureEncoding(material.emissiveMap);
        param.bumpMap = material.bumpMap != null;
        param.normalMap = material.normalMap != null;
        param.objectSpaceNormalMap = material.normalMapType == Constants.ObjectSpaceNormalMap;
        param.displacementMap = material.displacementMap != null;
        if (material instanceof MeshStandardMaterial) {
            param.roughnessMap = ((MeshStandardMaterial) material).roughnessMap != null;
            param.metalnessMap = ((MeshStandardMaterial) material).metalnessMap != null;
        }
        param.specularMap = material.specularMap != null;
        param.alphaMap = material.alphaMap != null;

        param.gradientMap = material.gradientMap != null;

        param.combine = material.combine;

        param.vertexTangents = material.normalMap != null && material.vertexTangents;
        param.vertexColors = material.vertexColors > 0;

        param.fog = fog != null;
        param.useFog = material.fog;
        param.fogExp = param.fog && (fog instanceof FogExp2);

        param.flatShading = material.flatShading;

        param.sizeAttenuation = material.sizeAttenuation;
        param.logarithmicDepthBuffer = false;

        param.skinning = false;
        param.maxBones = 0;
        param.useVertexTexture = false;

        param.morphTargets = material.morphTargets;
        param.morphNormals = material.morphNormals;
        param.maxMorphTargets = 0;
        param.maxMorphNormals = 0;

        param.numDirLights = lights.directional.size();
        param.numPointLights = lights.point.size();
        param.numSpotLights = lights.spot.size();
        param.numRectAreaLights = lights.rectArea.size();
        param.numHemiLights = lights.hemi.size();

        param.numClippingPlanes = nClipPlanes;
        param.numClipIntersection = nClipIntersection;

        param.dithering = material.dithering;

        param.shadowMapEnabled = renderer.shadowMap.enabled && object.receiveShadow && shadows.size() > 0;
        param.shadowMapType = renderer.shadowMap.type;

        param.toneMapping = renderer.toneMapping;
        param.physicallyCorrectLights = renderer.physicallyCorrectLights;

        param.premultipliedAlpha = material.premultipliedAlpha;

        param.alphaTest = material.alphaTest;
        param.doubleSided = material.side == Constants.DoubleSide;
        param.flipSided = material.side == Constants.BackSide;

        param.depthPacking = material.depthPacking;
        return param;
    }

    public String getProgramCode(Material material, Param param) {
        ArrayList<String> array = new ArrayList<>();
        if (param.shaderID != null) {
            array.add(param.shaderID);
        } else {
            array.add(material.fragmentShader);
            array.add(material.vertexShader);
        }

        if (material.defines != null) {
            Iterator iter = material.defines.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                String name = entry.getKey();
                String value = entry.getValue();
                array.add(name);
                array.add(value);
            }
        }

        try {
            for (int i = 0; i < parameterNames.length; i++) {
                String field = parameterNames[i];
                Object val = param.getClass().getField(field).get(param);
                array.add(val.toString());
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        array.add("function() {}");
        array.add(renderer.gammaOutput + "");
        array.add(renderer.gammaFactor + "");
        return GLProgram.join(", ", array);
    }

    public GLProgram acquireProgram(Material material, ShaderLib shader, Param param, String code) {
        GLProgram program = null;
        // Check if code has been already compiled
        for (int i = 0; i < programs.size(); i++) {
            GLProgram programInfo = programs.get(i);
            if (programInfo.code.equals(code)) {
                program = programInfo;
                program.usedTimes++;
                break;
            }
        }
        if (program == null) {
            program = new GLProgram(renderer, code, material, shader, param);
            programs.add(program);
        }
        return program;
    }

    public void releaseProgram(GLProgram program) {
        if (-- program.usedTimes == 0) {
            programs.remove(program);
            program.destroy();
        }
    }

    public void releasePrograms() {
        Log.d(TAG, "release all programs");
        for (GLProgram program : programs) {
            program.destroy();
        }
        programs.clear();
    }

    static public class Param {
        public String shaderID;
        public String precision;
        public boolean supportsVertexTextures;
        public int outputEncoding;
        public boolean map;
        public boolean flipY;
        public int mapEncoding;
        public boolean matcap;
        public int matcapEncoding;
        public boolean envMap;
        public int envMapMode;
        public int envMapEncoding;
        public boolean envMapCubeUV;
        public boolean lightMap;
        public boolean aoMap;
        public boolean emissiveMap;
        public int emissiveMapEncoding;
        public boolean bumpMap;
        public boolean normalMap;
        public boolean objectSpaceNormalMap;
        public boolean displacementMap;
        public boolean roughnessMap;
        public boolean metalnessMap;
        public boolean specularMap;
        public boolean alphaMap;

        public boolean gradientMap;
        public int combine;

        public boolean vertexTangents;
        public boolean vertexColors;

        public boolean fog;
        public boolean useFog;
        public boolean fogExp;

        public boolean flatShading;

        public boolean sizeAttenuation;
        public boolean logarithmicDepthBuffer;

        public boolean skinning;
        public int maxBones;
        public boolean useVertexTexture;

        public boolean morphTargets;
        public boolean morphNormals;
        public int maxMorphTargets;
        public int maxMorphNormals;

        public int numDirLights;
        public int numPointLights;
        public int numSpotLights;
        public int numRectAreaLights;
        public int numHemiLights;

        public int numClippingPlanes;
        public int numClipIntersection;

        public boolean dithering;

        public boolean shadowMapEnabled;
        public int shadowMapType;

        public int toneMapping;
        public boolean physicallyCorrectLights;

        public boolean premultipliedAlpha;

        public float alphaTest;
        public boolean doubleSided;
        public boolean flipSided;

        public int depthPacking;
    }
}
