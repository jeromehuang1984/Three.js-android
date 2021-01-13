package edu.three.renderers.shaders;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.lights.LightShadow;
import edu.three.math.Color;
import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.renderers.gl.GLLights;
import edu.three.textures.Texture;

public class UniformsLib {
    public NeedUpdate needUpdate = new NeedUpdate();
    public HashMap<String, Object> hashMap = new HashMap<>();

    public UniformsLib common() {
        hashMap.put("diffuse", new Color(0xeeeeee));
        hashMap.put("opacity", 1f);
        hashMap.put("uvTransform", new Matrix3());
        hashMap.put("map", null);
        hashMap.put("alphaMap", null);
        return this;
    }

    public UniformsLib specularmap() {
        hashMap.put("specularMap", null);
        return this;
    }

    public UniformsLib envmap() {
        hashMap.put("flipEnvMap", -1);
        hashMap.put("reflectivity", 1f);
        hashMap.put("refractionRatio", 0.98f);
        hashMap.put("maxMipLevel", 0);
        hashMap.put("envMap", null);
        return this;
    }

    public UniformsLib aomap() {
        hashMap.put("aoMapIntensity", 1f);
        hashMap.put("aoMap", null);
        return this;
    }

    public UniformsLib lightmap() {
        hashMap.put("lightMapIntensity", 1f);
        hashMap.put("lightMap", null);
        return this;
    }

    public UniformsLib emissivemap() {
        hashMap.put("emissiveMap", null);
        return this;
    }

    public UniformsLib bumpmap() {
        hashMap.put("bumpScale", 1f);
        hashMap.put("bumpMap", null);
        return this;
    }

    public UniformsLib normalmap() {
        hashMap.put("normalScale", new Vector2(1, 1));
        hashMap.put("normalMap", null);
        return this;
    }

    public UniformsLib displacementmap() {
        hashMap.put("displacementScale", 1f);
        hashMap.put("displacementBias", 0f);
        hashMap.put("displacementMap", null);
        return this;
    }

    public UniformsLib roughnessmap() {
        hashMap.put("roughnessMap", null);
        return this;
    }

    public UniformsLib metalnessmap() {
        hashMap.put("metalnessMap", null);
        return this;
    }

    public UniformsLib gradientmap() {
        hashMap.put("gradientMap", null);
        return this;
    }

    public UniformsLib fog() {
        hashMap.put("fogDensity", 0.00025f);
        hashMap.put("fogNear", 1);
        hashMap.put("fogFar", 2000);
        hashMap.put("fogColor", 0xffffff);
        return this;
    }

    public UniformsLib points() {
        hashMap.put("diffuse", new Color(0xeeeeee));
        hashMap.put("opacity", 1f);
        hashMap.put("size", 1f);
        hashMap.put("scale", 1f);
        hashMap.put("uvTransform", new Matrix3());
        hashMap.put("map", null);
        hashMap.put("alphaMap", null);
        return this;
    }

    public UniformsLib sprite() {
        hashMap.put("diffuse", new Color(0xeeeeee));
        hashMap.put("opacity", 1f);
        hashMap.put("center", new Vector2(0.5f, 0.5f));
        hashMap.put("rotation", 0f);
        hashMap.put("uvTransform", new Matrix3());
        hashMap.put("map", null);
        hashMap.put("alphaMap", null);
        return this;
    }

    public UniformsLib lights() {
        hashMap.put("directionalLights", new ArrayList<GLLights.Uniforms>());
        hashMap.put("directionalShadowMap", new ArrayList<Texture>());
        hashMap.put("directionalShadowMatrix", new ArrayList<Matrix4>());

        hashMap.put("spotLights", new ArrayList<GLLights.Uniforms>());
        hashMap.put("spotShadowMap", new ArrayList<Texture>());
        hashMap.put("spotShadowMatrix", new ArrayList<Matrix4>());

        hashMap.put("pointLights", new ArrayList<GLLights.Uniforms>());
        hashMap.put("pointShadowMap", new ArrayList<Texture>());
        hashMap.put("pointShadowMatrix", new ArrayList<Matrix4>());

        hashMap.put("hemisphereLights", new ArrayList<GLLights.Uniforms>());
        hashMap.put("rectAreaLights", new ArrayList<GLLights.Uniforms>());

        return this;
    }

    public UniformsLib put(String key, Object value) {
        hashMap.put(key, value);
        return this;
    }

    public Object get(String key) {
        return hashMap.get(key);
    }

    public boolean contains(String key) {
        return hashMap.containsKey(key);
    }

    public static class NeedUpdate {
        public boolean ambientLightColor;
        public boolean lightProbe;
        public boolean directionalLights;
        public boolean pointLights;
        public boolean spotLights;
        public boolean rectAreaLights;
        public boolean hemisphereLights;
    }

    public static class DirectionalLight {
        public Vector3 direction;
        public int color;
        public LightShadow shadow;
        public float shadowBias;
        public float shadowRadius;
        public float shadowMapSize;
    }

    public static class SpotLight {
        public Vector3 direction;
        public Vector3 position;
        public int color;
        public float distance;
        public float coneCos;
        public float penumbraCos;
        public float decay;

        public LightShadow shadow;
        public float shadowBias;
        public float shadowRadius;
        public float shadowMapSize;
    }

    public static class PointLight {
        public Vector3 position;
        public int color;
        public float distance;
        public float decay;

        public LightShadow shadow;
        public float shadowBias;
        public float shadowRadius;
        public float shadowMapSize;
        public float shadowCameraNear;
        public float shadowCameraFar;
    }

    public static class HemisphereLight {
        public Vector3 direction;
        public int skyColor;
        public int groundColor;
    }

    public static class RectAreaLight {
        public int color;
        public Vector3 position;
        public int width;
        public int height;
    }
}


