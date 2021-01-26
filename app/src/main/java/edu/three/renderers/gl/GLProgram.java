package edu.three.renderers.gl;

import android.opengl.GLES30;
import android.opengl.GLES31Ext;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.microedition.khronos.opengles.GL;

import edu.three.constant.Constants;
import edu.three.materials.Material;
import edu.three.materials.RawShaderMaterial;
import edu.three.materials.ShaderMaterial;
import edu.three.renderers.GLRenderer;
import edu.three.renderers.shaders.ShaderChunk;
import edu.three.renderers.shaders.ShaderLib;
//import static edu.three.constant.Constants.LinearEncoding;

public class GLProgram {
    static int programIdCount = 0;

    private String TAG = "GLProgram";
    GLUniforms cachedUniforms;
    HashMap<String, Integer> cachedAttributes;
    public int program;

    public String name;
    public int id;
    public String code;
    public int usedTimes = 1;
    public int vertexShader;
    public int fragmentShader;

    static public String join(String delimiter, ArrayList<String> array) {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < array.size(); i++) {
            ret.append(array.get(i));
            if (i < array.size() - 1) {
                ret.append(delimiter);
            }
        }
        return ret.toString();
    }

    static  public String join(String delimiter, String[] array) {
        ArrayList<String> lst = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            String item = array[i];
            if (item == null || item.trim().length() == 0) {
                continue;
            }
            lst.add(item);
        }
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < lst.size(); i++) {
            ret.append(lst.get(i));
            if (i < lst.size() - 1) {
                ret.append(delimiter);
            }
        }
        return ret.toString();
    }

    private String[] getEncodingComponents(int encoding) {
        switch (encoding) {
            case Constants.LinearEncoding:
                return new String[] {"Linear", "( value )"};
            case Constants.sRGBEncoding:
                return new String[] {"sRGB", "( value )"};
            case Constants.RGBEEncoding:
                return new String[] {"RGBE", "( value )"};
            case Constants.RGBM7Encoding:
                return new String[] {"RGBM", "( value, 7.0 )"};
            case Constants.RGBM16Encoding:
                return new String[] {"RGBM", "( value, 16.0 )"};
            case Constants.RGBDEncoding:
                return new String[] {"RGBD", "( value, 256.0 )"};
            case Constants.GammaEncoding:
                return new String[] {"Gamma", "( value, float( GAMMA_FACTOR ) )"};
            default:
                Log.e(TAG, "unsupported encoding: " + encoding);
                break;
        }
        return new String[0];
    }

    public String getTexelDecodingFunction(String functionName, int encoding) {
        String[] components = getEncodingComponents(encoding);
        return "vec4 " + functionName + "( vec4 value ) { return " + components[0] + "ToLinear" + components[1] + "; }";
    }

    public String getTexelEncodingFunction(String functionName, int encoding) {
        String[] components = getEncodingComponents(encoding);
        return "vec4 " + functionName + "( vec4 value ) { return LinearTo" + components[0]  + components[1] + "; }";
    }

    private String getToneMappingFunction(String functionName, int toneMapping) {
        String toneMappingName = "none";
        switch ( toneMapping ) {

            case Constants.LinearToneMapping:
                toneMappingName = "Linear";
                break;

            case Constants.ReinhardToneMapping:
                toneMappingName = "Reinhard";
                break;

            case Constants.Uncharted2ToneMapping:
                toneMappingName = "Uncharted2";
                break;

            case Constants.CineonToneMapping:
                toneMappingName = "OptimizedCineon";
                break;

            case Constants.ACESFilmicToneMapping:
                toneMappingName = "ACESFilmic";
                break;

            default:
                Log.e(TAG, "unsupported toneMapping: " + toneMapping);
                break;
        }
        return "vec3 " + functionName + "( vec3 color ) { return " + toneMappingName + "ToneMapping( color ); }";
    }

    private String generateDefines(HashMap<String, String> defines) {
        if (defines == null) {
            return null;
        }
        ArrayList<String> chunks = new ArrayList<>();
        Iterator iter = defines.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            String name = entry.getKey();
            String value = entry.getValue();
            chunks.add("#define" + name + " " + value);
        }
        return join("\n", chunks);
    }

    public HashMap<String, Integer> fetchAttributeLocations(int program) {
        HashMap<String, Integer> attributes = new HashMap<>();
        int[] count = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_ACTIVE_ATTRIBUTES, count, 0);
        for (int i = 0; i < count[0]; i++) {
            int bufSize = 32;
            byte[] bytes = new byte[bufSize];    //maximum name length
            int[] nameLen = new int[1];
            int[] size = new int[1];
            int[] type = new int[1];
            GLES30.glGetActiveAttrib(program, i, bufSize, nameLen, 0, size, 0, type, 0, bytes, 0);
            byte[] byteLst = Arrays.copyOf(bytes, nameLen[0]);
            String attrName = new String(byteLst);
            int addr = GLES30.glGetAttribLocation(program, attrName);
            attributes.put(attrName, addr);
        }
        return attributes;
    }

    public String replaceLightNums(String string, GLPrograms.Param param) {
        return string.replaceAll("NUM_DIR_LIGHTS", param.numDirLights + "")
                .replaceAll("NUM_SPOT_LIGHTS", param.numSpotLights + "")
                .replaceAll("NUM_RECT_AREA_LIGHTS", param.numRectAreaLights + "")
                .replaceAll("NUM_POINT_LIGHTS", param.numPointLights + "")
                .replaceAll("NUM_HEMI_LIGHTS", param.numHemiLights + "");
    }

    public String replaceClippingPlaneNums(String string, GLPrograms.Param param) {
        return string.replaceAll("NUM_CLIPPING_PLANES", param.numClippingPlanes + "")
                .replaceAll("UNION_CLIPPING_PLANES", param.numClipIntersection + "");
    }

    public String parseIncludes(String string) {
        String regx = "^[ \\t]*#include +<([\\w\\d./]+)>";
        Pattern pattern = Pattern.compile(regx, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(string);
        boolean modified = false;
        String ret = string;
        while (matcher.find()) {
            modified = true;
            String include = matcher.group(1);
            String replace = ShaderChunk.lookup(include);
            String reg = "[ \\t]*#include +<" + include + ">";
            ret = ret.replaceFirst(reg, replace);
        }
        if (modified) {
            return parseIncludes(ret);
        }
        return ret;
    }

    public String unrollLoops(String string) {
        String regx = "#pragma unroll_loop[\\s]+?for \\( int i \\= (\\d+)\\; i < (\\d+)\\; i \\+\\+ \\) \\{([\\s\\S]+?)(?=\\})\\}";
        Pattern pattern = Pattern.compile(regx);
        Matcher matcher = pattern.matcher(string);
        String ret = string;
        while (matcher.find()) {
            String unroll = "";
            int start = Integer.parseInt(matcher.group(1));
            int end = Integer.parseInt(matcher.group(2));
            String snippet = matcher.group(3);
            for (int i = start; i < end; i++) {
                unroll += snippet.replaceAll("\\[ i \\]", "[ " + i + " ]");
            }
            ret = ret.replaceFirst(regx, unroll);
        }
        return ret;
    }

    public GLProgram(int id) {
        this.id = id;
    }

    public GLProgram(GLRenderer renderer, String code, Material material, ShaderLib shader, GLPrograms.Param params) {
        String vertexShader = shader.vertexShader;
        String fragmentShader = shader.fragmentShader;
        String shadowMapTypeDefine = "SHADOWMAP_TYPE_BASIC";
        if (params.shadowMapType == Constants.PCFShadowMap) {
            shadowMapTypeDefine = "SHADOWMAP_TYPE_PCF";
        } else if (params.shadowMapType == Constants.PCFSoftShadowMap) {
            shadowMapTypeDefine = "SHADOWMAP_TYPE_PCF_SOFT";
        }

        String envMapTypeDefine = "ENVMAP_TYPE_CUBE";
        String envMapModeDefine = "ENVMAP_MODE_REFLECTION";
        String envMapBlendingDefine = "ENVMAP_BLENDING_MULTIPLY";
        if (params.envMap && material.envMap != null) {
            switch (material.envMap.mapping) {
                case Constants.CubeReflectionMapping:
                case Constants.CubeRefractionMapping:
                    envMapTypeDefine = "ENVMAP_TYPE_CUBE";
                    break;

                case Constants.CubeUVReflectionMapping:
                case Constants.CubeUVRefractionMapping:
                    envMapTypeDefine = "ENVMAP_TYPE_CUBE_UV";
                    break;

                case Constants.EquirectangularReflectionMapping:
                case Constants.EquirectangularRefractionMapping:
                    envMapTypeDefine = "ENVMAP_TYPE_EQUIREC";
                    break;

                case Constants.SphericalReflectionMapping:
                    envMapTypeDefine = "ENVMAP_TYPE_SPHERE";
                    break;
            }

            switch ( material.envMap.mapping ) {

                case Constants.CubeRefractionMapping:
                case Constants.EquirectangularRefractionMapping:
                    envMapModeDefine = "ENVMAP_MODE_REFRACTION";
                    break;

            }

            switch ( material.combine ) {

                case Constants.MultiplyOperation:
                    envMapBlendingDefine = "ENVMAP_BLENDING_MULTIPLY";
                    break;

                case Constants.MixOperation:
                    envMapBlendingDefine = "ENVMAP_BLENDING_MIX";
                    break;

                case Constants.AddOperation:
                    envMapBlendingDefine = "ENVMAP_BLENDING_ADD";
                    break;

            }
        }

        float gammaFactorDefine = (renderer.gammaFactor > 0) ? renderer.gammaFactor : 1f;

        String customDefines = generateDefines(material.defines);

        program = GLES30.glCreateProgram();
        String prefixVertex = "", prefixFragment = "";

        if (material instanceof RawShaderMaterial) {
            if (customDefines != null) {
                prefixVertex = customDefines;
                prefixFragment = customDefines;
            }
            if (prefixVertex.length() > 0) {
                prefixVertex += "\n";
            }
            if (prefixFragment.length() > 0) {
                prefixFragment += "\n";
            }
        } else {
            String[] prefixVertexArr = new String[] {
                    "precision " + params.precision + " float;",
                    "precision " + params.precision + " int;",

                    "#define SHADER_NAME " + shader.name,

                    customDefines,

                    params.supportsVertexTextures ? "#define VERTEX_TEXTURES" : "",

                    "#define GAMMA_FACTOR " + gammaFactorDefine,

                    "#define MAX_BONES " + params.maxBones,
                    ( params.useFog && params.fog ) ? "#define USE_FOG" : "",
                    ( params.useFog && params.fogExp ) ? "#define FOG_EXP2" : "",

                    params.map ? "#define USE_MAP" : "",
                    params.flipY ? "#define FLIP_Y" : "",
                    params.envMap ? "#define USE_ENVMAP" : "",
                    params.envMap ? "#define " + envMapModeDefine : "",
                    params.lightMap ? "#define USE_LIGHTMAP" : "",
                    params.aoMap ? "#define USE_AOMAP" : "",
                    params.emissiveMap ? "#define USE_EMISSIVEMAP" : "",
                    params.bumpMap ? "#define USE_BUMPMAP" : "",
                    params.normalMap ? "#define USE_NORMALMAP" : "",
                    ( params.normalMap && params.objectSpaceNormalMap ) ? "#define OBJECTSPACE_NORMALMAP" : "",
                    params.displacementMap && params.supportsVertexTextures ? "#define USE_DISPLACEMENTMAP" : "",
                    params.specularMap ? "#define USE_SPECULARMAP" : "",
                    params.roughnessMap ? "#define USE_ROUGHNESSMAP" : "",
                    params.metalnessMap ? "#define USE_METALNESSMAP" : "",
                    params.alphaMap ? "#define USE_ALPHAMAP" : "",

                    params.vertexTangents ? "#define USE_TANGENT" : "",
                    params.vertexColors ? "#define USE_COLOR" : "",

                    params.flatShading ? "#define FLAT_SHADED" : "",

                    params.skinning ? "#define USE_SKINNING" : "",
                    params.useVertexTexture ? "#define BONE_TEXTURE" : "",

                    params.morphTargets ? "#define USE_MORPHTARGETS" : "",
                    params.morphNormals && params.flatShading == false ? "#define USE_MORPHNORMALS" : "",
                    params.doubleSided ? "#define DOUBLE_SIDED" : "",
                    params.flipSided ? "#define FLIP_SIDED" : "",

                    params.shadowMapEnabled ? "#define USE_SHADOWMAP" : "",
                    params.shadowMapEnabled ? "#define " + shadowMapTypeDefine : "",

                    params.sizeAttenuation ? "#define USE_SIZEATTENUATION" : "",

//                    params.logarithmicDepthBuffer ? "#define USE_LOGDEPTHBUF" : "",
//                    params.logarithmicDepthBuffer && ( capabilities.isWebGL2 || extensions.get( "EXT_frag_depth" ) ) ? "#define USE_LOGDEPTHBUF_EXT" : "",

                    "uniform mat4 modelMatrix;",
                    "uniform mat4 modelViewMatrix;",
                    "uniform mat4 projectionMatrix;",
                    "uniform mat4 viewMatrix;",
                    "uniform mat3 normalMatrix;",
                    "uniform vec3 cameraPosition;",

                    "attribute vec3 position;",
                    "attribute vec3 normal;",
                    "attribute vec2 uv;",

                    "#ifdef USE_TANGENT",

                    "	attribute vec4 tangent;",

                    "#endif",

                    "#ifdef USE_COLOR",

                    "	attribute vec3 color;",

                    "#endif",

                    "#ifdef USE_MORPHTARGETS",

                    "	attribute vec3 morphTarget0;",
                    "	attribute vec3 morphTarget1;",
                    "	attribute vec3 morphTarget2;",
                    "	attribute vec3 morphTarget3;",

                    "	#ifdef USE_MORPHNORMALS",

                    "		attribute vec3 morphNormal0;",
                    "		attribute vec3 morphNormal1;",
                    "		attribute vec3 morphNormal2;",
                    "		attribute vec3 morphNormal3;",

                    "	#else",

                    "		attribute vec3 morphTarget4;",
                    "		attribute vec3 morphTarget5;",
                    "		attribute vec3 morphTarget6;",
                    "		attribute vec3 morphTarget7;",

                    "	#endif",

                    "#endif",

                    "#ifdef USE_SKINNING",

                    "	attribute vec4 skinIndex;",
                    "	attribute vec4 skinWeight;",

                    "#endif",

                    "\n"
            };

            float alphaTest = params.alphaTest - (int) params.alphaTest;
            String[] prefixFragmentArr = new String[] {
                    "precision " + params.precision + " float;",
                    "precision " + params.precision + " int;",

                    "#define SHADER_NAME " + shader.name,

                    customDefines,

                    params.alphaTest > 0 ? "#define ALPHATEST " + params.alphaTest + ( alphaTest > 0 ? "" : ".0" ) : "", // add ".0" if integer

                    "#define GAMMA_FACTOR " + gammaFactorDefine,

                    ( params.useFog && params.fog ) ? "#define USE_FOG" : "",
                    ( params.useFog && params.fogExp ) ? "#define FOG_EXP2" : "",

                    params.map ? "#define USE_MAP" : "",
                    params.flipY ? "#define FLIP_Y" : "",
                    params.matcap ? "#define USE_MATCAP" : "",
                    params.envMap ? "#define USE_ENVMAP" : "",
                    params.envMap ? "#define " + envMapTypeDefine : "",
                    params.envMap ? "#define " + envMapModeDefine : "",
                    params.envMap ? "#define " + envMapBlendingDefine : "",
                    params.lightMap ? "#define USE_LIGHTMAP" : "",
                    params.aoMap ? "#define USE_AOMAP" : "",
                    params.emissiveMap ? "#define USE_EMISSIVEMAP" : "",
                    params.bumpMap ? "#define USE_BUMPMAP" : "",
                    params.normalMap ? "#define USE_NORMALMAP" : "",
                    ( params.normalMap && params.objectSpaceNormalMap ) ? "#define OBJECTSPACE_NORMALMAP" : "",
                    params.specularMap ? "#define USE_SPECULARMAP" : "",
                    params.roughnessMap ? "#define USE_ROUGHNESSMAP" : "",
                    params.metalnessMap ? "#define USE_METALNESSMAP" : "",
                    params.alphaMap ? "#define USE_ALPHAMAP" : "",

                    params.vertexTangents ? "#define USE_TANGENT" : "",
                    params.vertexColors ? "#define USE_COLOR" : "",

                    params.gradientMap ? "#define USE_GRADIENTMAP" : "",

                    params.flatShading ? "#define FLAT_SHADED" : "",

                    params.doubleSided ? "#define DOUBLE_SIDED" : "",
                    params.flipSided ? "#define FLIP_SIDED" : "",

                    params.shadowMapEnabled ? "#define USE_SHADOWMAP" : "",
                    params.shadowMapEnabled ? "#define " + shadowMapTypeDefine : "",

                    params.premultipliedAlpha ? "#define PREMULTIPLIED_ALPHA" : "",

                    params.physicallyCorrectLights ? "#define PHYSICALLY_CORRECT_LIGHTS" : "",

//                    params.logarithmicDepthBuffer ? "#define USE_LOGDEPTHBUF" : "",
//                    params.logarithmicDepthBuffer && ( capabilities.isWebGL2 || extensions.get( "EXT_frag_depth" ) ) ? "#define USE_LOGDEPTHBUF_EXT" : "",
//
//                    params.envMap && ( capabilities.isWebGL2 || extensions.get( "EXT_shader_texture_lod" ) ) ? "#define TEXTURE_LOD_EXT" : "",

                    "uniform mat4 viewMatrix;",
                    "uniform vec3 cameraPosition;",

                    ( params.toneMapping != Constants.NoToneMapping ) ? "#define TONE_MAPPING" : "",
                    ( params.toneMapping != Constants.NoToneMapping ) ? ShaderChunk.lookup("tonemapping_pars_fragment") : "", // this code is required here because it is used by the toneMapping() function defined below
                    ( params.toneMapping != Constants.NoToneMapping ) ? getToneMappingFunction( "toneMapping", params.toneMapping ) : "",

                    params.dithering ? "#define DITHERING" : "",

                    ( params.outputEncoding > 0 || params.mapEncoding > 0 || params.matcapEncoding > 0 || params.envMapEncoding > 0 || params.emissiveMapEncoding > 0 ) ?
                            ShaderChunk.lookup("encodings_pars_fragment") : "", // this code is required here because it is used by the various encoding/decoding function defined below
                    params.mapEncoding > 0 ? getTexelDecodingFunction( "mapTexelToLinear", params.mapEncoding) : "",
                    params.matcapEncoding > 0 ? getTexelDecodingFunction( "matcapTexelToLinear", params.matcapEncoding ) : "",
                    params.envMapEncoding > 0 ? getTexelDecodingFunction( "envMapTexelToLinear", params.envMapEncoding ) : "",
                    params.emissiveMapEncoding > 0 ? getTexelDecodingFunction( "emissiveMapTexelToLinear", params.emissiveMapEncoding ) : "",
                    params.outputEncoding > 0 ? getTexelEncodingFunction( "linearToOutputTexel", params.outputEncoding ) : "",

                    params.depthPacking > 0 ? "#define DEPTH_PACKING " + material.depthPacking : "",

                    "\n"
            };
            prefixVertex = join("\n", prefixVertexArr);
            prefixFragment = join("\n", prefixFragmentArr);
        }
        vertexShader = parseIncludes(vertexShader);
        vertexShader = replaceLightNums(vertexShader, params);
        vertexShader = replaceClippingPlaneNums(vertexShader, params);

        fragmentShader = parseIncludes(fragmentShader);
        fragmentShader = replaceLightNums(fragmentShader, params);
        fragmentShader = replaceClippingPlaneNums(fragmentShader, params);

        vertexShader = unrollLoops( vertexShader );
        fragmentShader = unrollLoops( fragmentShader );

        if (! (material instanceof RawShaderMaterial) ) {
            boolean isGLSL3ShaderMaterial = false;
            String versionRegx = "^\\s*#version\\s+300\\s+es\\s*\\n";
            String version = "#version 300 es";
            if ((material instanceof ShaderMaterial) && vertexShader.indexOf(version) >= 0 &&
                fragmentShader.indexOf(version) >= 0) {
                isGLSL3ShaderMaterial = true;

                vertexShader = vertexShader.replaceFirst( versionRegx, "" );
                fragmentShader = fragmentShader.replaceFirst( versionRegx, "" );
            }

            // GLSL 3.0 conversion
            String[] prefixArr = new String[] {
                    "#version 300 es\n",
                    "#define attribute in",
                    "#define varying out",
                    "#define texture2D texture"
            };
            String prefixArrStr = join("\n", prefixArr);
            prefixVertex = prefixArrStr + "\n" + prefixVertex;

            prefixArr = new String[] {
                    "#version 300 es\n",
                    "#define varying in",
                    isGLSL3ShaderMaterial ? "" : "out highp vec4 pc_fragColor;",
                    isGLSL3ShaderMaterial ? "" : "#define gl_FragColor pc_fragColor",
                    "#define gl_FragDepthEXT gl_FragDepth",
                    "#define texture2D texture",
                    "#define textureCube texture",
                    "#define texture2DProj textureProj",
                    "#define texture2DLodEXT textureLod",
                    "#define texture2DProjLodEXT textureProjLod",
                    "#define textureCubeLodEXT textureLod",
                    "#define texture2DGradEXT textureGrad",
                    "#define texture2DProjGradEXT textureProjGrad",
                    "#define textureCubeGradEXT textureGrad"
            };
            prefixArrStr = join("\n", prefixArr);
            prefixFragment = prefixArrStr + "\n" + prefixFragment;
        }

        String vertexGlsl = prefixVertex + vertexShader;
        String fragmentGlsl = prefixFragment + fragmentShader;

        int glVertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexGlsl);
        int glFragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentGlsl);

        GLES30.glAttachShader(program, glVertexShader);
        GLES30.glAttachShader(program, glFragmentShader);

        // Force a particular attribute to index 0.
        if (material instanceof ShaderMaterial && ((ShaderMaterial) material).index0AttributeName != null) {
            GLES30.glBindAttribLocation(program, 0, ((ShaderMaterial) material).index0AttributeName);
        } else if (params.morphTargets) {
            // programs with morphTargets displace position out of attribute 0
            GLES30.glBindAttribLocation(program, 0, "position");
        }
        GLES30.glLinkProgram(program);

        // check for link errors
        if (renderer.checkShaderErrors) {
            String programLog = GLES30.glGetProgramInfoLog(program).trim();
            String vertexLog = GLES30.glGetShaderInfoLog(glVertexShader).trim();
            String fragmentLog = GLES30.glGetShaderInfoLog(glFragmentShader).trim();

            boolean runnable = true;
            boolean haveDiagnostics = true;
            int[] linked = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0);
            if (linked[0] == 0) {
                runnable = false;
                int error;
                if ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
                    Log.e(TAG, "glError " + error);
                }
                Log.e(TAG, "opengl es VALIDATE_STATUS");
                Log.e(TAG, "program log: " + programLog);
                Log.e(TAG, "vertex log: " + vertexLog);
                Log.e(TAG, "fragment log: " + fragmentLog);
            } else if (programLog.length() > 0) {
                Log.w(TAG, "program log: " + programLog);
            } else if (vertexLog.length() > 0 || fragmentLog.length() > 0) {
                haveDiagnostics = false;
            }
        }

        // clean up
        GLES30.glDeleteShader(glVertexShader);
        GLES30.glDeleteShader(glFragmentShader);

        // set up caching for uniform locations
        cachedUniforms = new GLUniforms(program);
        cachedAttributes = fetchAttributeLocations(program);

        name = shader.name;
        id = programIdCount ++;
        this.code = code;
        this.vertexShader = glVertexShader;
        this.fragmentShader = glFragmentShader;
    }

    public GLUniforms getUniforms() {
        return cachedUniforms;
    }

    public HashMap<String, Integer> getAttributes() {
        return cachedAttributes;
    }

    public void destroy() {
        GLES30.glDeleteProgram(program);
    }

    private int loadShader(int type, String shaderSrc) {
        int shader;
        int[] compiled = new int[1];

        // Create the shader object
        shader = GLES30.glCreateShader(type);

        if (shader == 0) {
            return 0;
        }

        // Load the shader source
        GLES30.glShaderSource(shader, shaderSrc);

        // Compile the shader
        GLES30.glCompileShader(shader);

        // Check the compile status
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0) {
            Log.e(TAG, "Erorr!!!!");
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }
}
