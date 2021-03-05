package edu.three.renderers.shaders;

import java.util.HashMap;

import edu.three.math.Color;
import edu.three.math.Matrix3;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class ShaderLib {
    public UniformsLib uniforms;
    public String vertexShader;
    public String fragmentShader;
    public String name;

    public ShaderLib() {
    }

    public ShaderLib(UniformsLib uniforms, String vertexShader, String fragmentShader) {
        this.uniforms = uniforms;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }


    public static ShaderLib basic() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().specularmap()
                .envmap().aomap().lightmap().fog();
        ret.vertexShader = ShaderChunk.meshbasic_vert;
        ret.fragmentShader = ShaderChunk.meshbasic_frag;
        ret.name = "MeshBasicMaterial";
        return ret;
    }

    public static ShaderLib lambert() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().specularmap()
                .envmap().aomap().lightmap().fog().lights();
        ret.uniforms.put("emissive", new Color(0x000000));
        ret.vertexShader = ShaderChunk.meshlambert_vert;
        ret.fragmentShader = ShaderChunk.meshlambert_frag;
        ret.name = "MeshLambertMaterial";
        return ret;
    }

    public static ShaderLib phong() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().specularmap().envmap().aomap().lightmap()
                .emissivemap().bumpmap().normalmap().displacementmap().gradientmap().fog().lights();
        ret.uniforms.put("emissive", new Color(0x000000));
        ret.uniforms.put("specular", new Color(0x111111));
        ret.uniforms.put("shininess", 30f);
        ret.vertexShader = ShaderChunk.meshphong_vert;
        ret.fragmentShader = ShaderChunk.meshphong_frag;
        ret.name = "MeshPhongMaterial";
        return ret;
    }

    public static ShaderLib standard() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().envmap().aomap().lightmap()
                .emissivemap().bumpmap().normalmap().displacementmap()
                .roughnessmap().metalnessmap().fog().lights();
        ret.uniforms.put("emissive", new Color(0x000000));
        ret.uniforms.put("roughness", 0.5f);
        ret.uniforms.put("metalness", 0.5f);
        ret.uniforms.put("envMapIntensity", 1f);

        ret.vertexShader = ShaderChunk.meshphysical_vert;
        ret.fragmentShader = ShaderChunk.meshphysical_frag;
        ret.name = "MeshStandardMaterial";
        return ret;
    }

    public static ShaderLib matcap() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().bumpmap().normalmap().displacementmap().fog();
//        ret.uniforms.matcap = null;

        ret.vertexShader = ShaderChunk.meshmatcap_vert;
        ret.fragmentShader = ShaderChunk.meshmatcap_frag;
        ret.name = "MeshMatcapMaterial";
        return ret;
    }

    public static ShaderLib points() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().points().fog();
        ret.vertexShader = ShaderChunk.points_vert;
        ret.fragmentShader = ShaderChunk.points_frag;
        ret.name = "PointsMaterial";
        return ret;
    }

    public static ShaderLib line() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().fog();
        ret.uniforms.put("linewidth", 1f);
        ret.uniforms.put("resolution", new Vector2(1, 1));
        ret.uniforms.put("dashScale", 1f);
        ret.uniforms.put("dashSize", 1f);
        ret.uniforms.put("dashOffset", 0);
        ret.uniforms.put("gapSize", 1);
        ret.uniforms.put("opacity", 1);

        ret.vertexShader = ShaderChunk.line_vert;
        ret.fragmentShader = ShaderChunk.line_frag;
        ret.name = "LineMaterial";
        return ret;
    }

    public static ShaderLib dashed() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().fog();
        ret.uniforms.put("scale", 1f);
        ret.uniforms.put("dashSize", 1f);
        ret.uniforms.put("totalSize", 1f);

        ret.vertexShader = ShaderChunk.linedashed_vert;
        ret.fragmentShader = ShaderChunk.linedashed_frag;
        ret.name = "LineDashedMaterial";
        return ret;
    }

    public static ShaderLib depth() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().displacementmap();
        ret.vertexShader = ShaderChunk.depth_vert;
        ret.fragmentShader = ShaderChunk.depth_frag;
        ret.name = "MeshDepthMaterial";
        return ret;
    }

    public static ShaderLib normal() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().bumpmap().normalmap().displacementmap();
        ret.uniforms.put("opacity", 1f);
        ret.vertexShader = ShaderChunk.normal_vert;
        ret.fragmentShader = ShaderChunk.normal_frag;
        ret.name = "MeshNormalMaterial";
        return ret;
    }

    public static ShaderLib sprite() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().sprite().fog();
        ret.vertexShader = ShaderChunk.sprite_vert;
        ret.fragmentShader = ShaderChunk.sprite_frag;
        ret.name = "SpriteMaterial";
        return ret;
    }

    public static ShaderLib background() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib();
        ret.uniforms.put("uvTransform", new Matrix3());
//        ret.uniforms.t2D = null;
        ret.vertexShader = ShaderChunk.background_vert;
        ret.fragmentShader = ShaderChunk.background_frag;
        ret.name = "MeshBasicMaterial";
        return ret;
    }

    public static ShaderLib cube() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib();
        ret.uniforms.put("tFlip", -1f);
        ret.uniforms.put("opacity", 1f);
//        ret.uniforms.tCube = null;
        ret.vertexShader = ShaderChunk.cube_vert;
        ret.fragmentShader = ShaderChunk.cube_frag;
        ret.name = "MeshBasicMaterial";
        return ret;
    }

    public static ShaderLib equirect() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib();
//        ret.uniforms.tEquirect = null;

        ret.vertexShader = ShaderChunk.equirect_vert;
        ret.fragmentShader = ShaderChunk.equirect_frag;
        ret.name = "MeshBasicMaterial";
        return ret;
    }

    public static ShaderLib distanceRGBA() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().common().displacementmap();
        ret.uniforms.put("referencePosition", new Vector3());
        ret.uniforms.put("nearDistance", 1f);
        ret.uniforms.put("farDistance", 1000f);
        ret.vertexShader = ShaderChunk.distanceRGBA_vert;
        ret.fragmentShader = ShaderChunk.distanceRGBA_frag;
        ret.name = "MeshDistanceMaterial";
        return ret;
    }

    public static ShaderLib shadow() {
        ShaderLib ret = new ShaderLib();
        ret.uniforms = new UniformsLib().lights().fog();
        ret.uniforms.put("color", new Color(0x000000));
        ret.uniforms.put("opacity", 1f);
        ret.vertexShader = ShaderChunk.shadow_vert;
        ret.fragmentShader = ShaderChunk.shadow_frag;
        ret.name = "ShadowMaterial";
        return ret;
    }

    public static ShaderLib physical() {
        ShaderLib ret = ShaderLib.standard();
        ret.uniforms.put("clearCoat", 0f);
        ret.uniforms.put("clearCoatRoughness", 0f);
        ret.vertexShader = ShaderChunk.meshphysical_vert;
        ret.fragmentShader = ShaderChunk.meshphysical_frag;
        ret.name = "MeshPhysicalMaterial";
        return ret;
    }

    static HashMap<String, ShaderLib> map = new HashMap<>();
    static {
        map.put("depth", depth());
        map.put("distanceRGBA", distanceRGBA());
        map.put("normal", normal());
        map.put("basic", basic());
        map.put("lambert", lambert());
        map.put("phong", phong());
        map.put("physical", physical());
        map.put("matcap", matcap());
        map.put("dashed", dashed());
        map.put("points", points());
        map.put("shadow", shadow());
        map.put("sprite", sprite());
        map.put("standard", standard());
        map.put("equirect", equirect());
        map.put("cube", cube());
    }
    public static ShaderLib lookup(String name) {
        return map.get(name);
    }
}
