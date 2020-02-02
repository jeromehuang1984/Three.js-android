package edu.three.materials;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import edu.three.constant.Constants;
import edu.three.core.EventDispatcher;
import edu.three.math.Color;
import edu.three.math.Plane;
import edu.three.math.Vector3;
import edu.three.renderers.GLRenderer;
import edu.three.renderers.gl.GLProgram;
import edu.three.renderers.shaders.ShaderLib;
import edu.three.renderers.shaders.UniformsLib;
import edu.three.textures.CubeTexture;
import edu.three.textures.Texture;

public class Material extends EventDispatcher {
    public static long materialId = 0;
    public String uuid = UUID.randomUUID().toString();
    public long id;
    public String name = "";
    public String type = "";
    public boolean fog = true;
    public boolean lights = true;

    public int blending = Constants.NormalBlending;
    public int side = Constants.FrontSide;
    public boolean flatShading = false;
    public boolean vertexTangents = false;
    public int vertexColors = Constants.NoColors;

    public float opacity = 1;
    public boolean transparent = false;

    public int blendSrc = Constants.SrcAlphaFactor;
    public int blendDst = Constants.OneMinusSrcAlphaFactor;
    public int blendEquation = Constants.AddEquation;
    public int blendSrcAlpha;
    public int blendDstAlpha;
    public int blendEquationAlpha;

    public int depthFunc = Constants.LessEqualDepth;
    public boolean depthTest = true;
    public boolean depthWrite = true;

    public int stencilFunc = Constants.AlwaysStencilFunc;
    public int stencilRef = 0;
    public int stencilMask = 0xff;
    public int stencilFail = Constants.KeepStencilOp;
    public int stencilZFail = Constants.KeepStencilOp;
    public int stencilZPass = Constants.KeepStencilOp;
    public boolean stencilWrite = false;

    public Plane[] clippingPlanes = new Plane[0];
    public boolean clipIntersection = false;
    public boolean clipShadows = false;

    public Integer shadowSide;

    public boolean colorWrite = true;

    public String precision; // override the renderer's default precision for this material

    public boolean polygonOffset = false;
    public float polygonOffsetFactor = 0;
    public float polygonOffsetUnits = 0;

    public boolean dithering = false;

    public float alphaTest = 0;
    public boolean premultipliedAlpha = false;

    public boolean visible = true;

    public String userData; // json object string

    public boolean needsUpdate = true;

    public String fragmentShader = "";
    public String vertexShader = "";
    //used for loaders
    public HashMap<String, String> defines = null;
    public int combine;

    public Texture envMap = null;
    public float reflectivity;
    public float refractionRatio;

    public float lightMapIntensity;

    public float aoMapIntensity;

    public Texture map;
    public UniformsLib uniforms;
    public boolean wireframe = false;
    public float wireframeLinewidth;
    public int linewidth = -1;

    public int numSupportedMorphTargets;
    public int numSupportedMorphNormals;

    public Color color = null;
    public Texture alphaMap;

    public GLProgram program;
    public ICallback callback = new ICallback() {
        @Override
        public void onBeforeCompile(ShaderLib shaderLib, GLRenderer renderer) {

        }
    };

    public int depthPacking = -1;   //MeshDepthMaterial
    public Texture matcap = null; //MeshMatcapMaterial
    public Texture lightMap = null; //MeshLambertMaterial MeshPhongMaterial
    public Texture aoMap = null;
    public Color emissive = null; //color

    public float emissiveIntensity = -1;
    public Texture emissiveMap = null;

    public Texture bumpMap = null;

    public Texture normalMap = null;

    public int normalMapType = -1;

    public Texture displacementMap = null;

//    public Texture roughnessMap = null;

//    public Texture metalnessMap = null;public Texture roughnessMap = null;

    public Texture specularMap = null;

    public Texture gradientMap = null;

    public boolean sizeAttenuation = false;

    public boolean morphTargets = false;

    public boolean morphNormals = false;

    public boolean clipping = false;

    public boolean skinning = false;

    public HashMap<String, float[]> defaultAttributeValues() {
        return null;
    }

    public Material() {
        id = materialId++;
    }

    public Material copy(Material source) {
        this.name = source.name;

        this.fog = source.fog;
        this.lights = source.lights;

        this.blending = source.blending;
        this.side = source.side;
        this.flatShading = source.flatShading;
        this.vertexColors = source.vertexColors;

        this.opacity = source.opacity;
        this.transparent = source.transparent;

        this.blendSrc = source.blendSrc;
        this.blendDst = source.blendDst;
        this.blendEquation = source.blendEquation;
        this.blendSrcAlpha = source.blendSrcAlpha;
        this.blendDstAlpha = source.blendDstAlpha;
        this.blendEquationAlpha = source.blendEquationAlpha;

        this.depthFunc = source.depthFunc;
        this.depthTest = source.depthTest;
        this.depthWrite = source.depthWrite;

        this.stencilWrite = source.stencilWrite;
        this.stencilFunc = source.stencilFunc;
        this.stencilRef = source.stencilRef;
        this.stencilMask = source.stencilMask;
        this.stencilFail = source.stencilFail;
        this.stencilZFail = source.stencilZFail;
        this.stencilZPass = source.stencilZPass;

        this.colorWrite = source.colorWrite;

        this.precision = source.precision;

        this.polygonOffset = source.polygonOffset;
        this.polygonOffsetFactor = source.polygonOffsetFactor;
        this.polygonOffsetUnits = source.polygonOffsetUnits;

        this.dithering = source.dithering;

        this.alphaTest = source.alphaTest;
        this.premultipliedAlpha = source.premultipliedAlpha;

        this.visible = source.visible;
        this.userData = source.userData;

        this.clipShadows = source.clipShadows;
        this.clipIntersection = source.clipIntersection;

        Plane[] srcPlanes = source.clippingPlanes;
        if (srcPlanes != null && srcPlanes.length > 0) {
            Plane[] dstPlanes = new Plane[srcPlanes.length];
            for (int i = 0; i < srcPlanes.length; i++) {
                dstPlanes[ i ] = srcPlanes[ i ].clone();
            }
            clippingPlanes = dstPlanes;
        }

        this.shadowSide = source.shadowSide;

        return this;
    }

    public Material clone() {
        return new Material().copy(this);
    }

    public interface ICallback {
        void onBeforeCompile(ShaderLib shaderLib, GLRenderer renderer);
    }
}
