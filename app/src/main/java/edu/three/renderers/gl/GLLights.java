package edu.three.renderers.gl;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.cameras.Camera;
import edu.three.lights.AmbientLight;
import edu.three.lights.DirectionalLight;
import edu.three.lights.HemisphereLight;
import edu.three.lights.Light;
import edu.three.lights.LightProbe;
import edu.three.lights.LightShadow;
import edu.three.lights.PointLight;
import edu.three.lights.RectAreaLight;
import edu.three.lights.SpotLight;
import edu.three.lights.SpotLightShadow;
import edu.three.math.Color;
import edu.three.math.Matrix4;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.renderers.shaders.UniformsLib;
import edu.three.textures.Texture;

public class GLLights {
    UniformsCache cache = new UniformsCache();
    public State state = new State();
    private int nextVersion = 0;
    //temp
    Vector3 vector3 = new Vector3();
    Matrix4 matrix4 = new Matrix4();
    Matrix4 matrix42 = new Matrix4();

    public GLLights() {
        State state = new State();
    }

    public void setup(Light[] lights, Light[] shadows, Camera camera) {
        float r = 0, g = 0, b = 0;
        state.clear();

        Matrix4 viewMatrix = camera.matrixWorldInverse;
        for (int i = 0; i < lights.length; i++) {
            Light light = lights[i];
            Color color = new Color().setHex(light.color);
            float intensity = light.intensity;
            Texture shadowMap = (light.getShadow() != null && light.getShadow().map != null) ?
                    light.getShadow().map.texture : null;
            if (light instanceof AmbientLight) {
                r += color.r * intensity;
                g += color.g * intensity;
                b += color.b * intensity;
            } else if (light instanceof LightProbe) {
                LightProbe lightProbe = (LightProbe) light;
                for (int j = 0; j < 9; j++) {
                    state.probe[j].addScale(lightProbe.sh.elements()[j], intensity);
                }
            } else if (light instanceof DirectionalLight) {
                DirectionalLight directionalLight = (DirectionalLight) light;
                Uniforms uniforms = cache.get(light);
                uniforms.put("color", new Color(light.color).multiplyScalar(light.intensity));
                Vector3 direction = new Vector3().setFromMatrixPosition(light.getWorldMatrix());
                uniforms.put("direction",  direction);
                vector3.setFromMatrixPosition(directionalLight.target.getWorldMatrix());
                direction.sub(vector3);
                direction.transformDirection(viewMatrix);
                uniforms.put("shadow",  light.castShadow);
                if (light.castShadow) {
                    LightShadow shadow = light.getShadow();
                    uniforms.put("shadowBias",  shadow.bias);
                    uniforms.put("shadowRadius",  shadow.radius);
                    uniforms.put("shadowMapSize",  shadow.mapSize);
                }
                state.directionalShadowMap.add(shadowMap);
                state.directionalShadowMatrix.add(directionalLight.getShadow().matrix);
                state.directional.add(uniforms);
            } else if (light instanceof SpotLight) {
                SpotLight spotLight = (SpotLight) light;
                Uniforms uniforms = cache.get(light);
                Vector3 position = new Vector3().setFromMatrixPosition(light.getWorldMatrix());
                position.applyMatrix4(viewMatrix);
                uniforms.put("position", position);

                uniforms.put("color", new Color(light.color).multiplyScalar(intensity));
                uniforms.put("distance", ((SpotLight) light).distance);

                Vector3 direction = new Vector3().setFromMatrixPosition(light.getWorldMatrix());
                vector3.setFromMatrixPosition(spotLight.target.getWorldMatrix());
                direction.sub(vector3);
                direction.transformDirection(viewMatrix);
                uniforms.put("direction", direction);

                uniforms.put("coneCos", (float) Math.cos(spotLight.angle));
                uniforms.put("penumbraCos", (float) Math.cos(spotLight.angle * (1 - spotLight.penumbra)));
                uniforms.put("decay", spotLight.decay);
                uniforms.put("shadow", light.castShadow);
                if (light.castShadow) {
                    LightShadow shadow = light.getShadow();
                    uniforms.put("shadowBias",  shadow.bias);
                    uniforms.put("shadowRadius",  shadow.radius);
                    uniforms.put("shadowMapSize",  shadow.mapSize);
                }
                state.spotShadowMap.add(shadowMap);
                state.spotShadowMatrix.add(light.getShadow().matrix);
                state.spot.add(uniforms);
            } else if (light instanceof RectAreaLight) {
                Uniforms uniforms = cache.get(light);
                // (a) intensity is the total visible light emitted
                //uniforms.color.copy( color ).multiplyScalar( intensity / ( light.width * light.height * Math.PI ) );

                // (b) intensity is the brightness of the light
                uniforms.put("color", new Color(light.color).multiplyScalar(intensity));
                Vector3 position = new Vector3().setFromMatrixPosition(light.getWorldMatrix());
                position.applyMatrix4(viewMatrix);
                uniforms.put("position", position);

                // extract local rotation of light to derive width/height half vectors
                matrix42.identity();
                matrix4.copy(light.getWorldMatrix());
                matrix4.premultiply(viewMatrix);
                matrix42.extractRotation(matrix4);

                Vector3 halfWidth = new Vector3().set(((RectAreaLight) light).width * 0.5f, 0, 0);
                halfWidth.applyMatrix4(matrix42);
                uniforms.put("halfWidth", halfWidth);

                Vector3 halfHeight = new Vector3().set(0, ((RectAreaLight) light).height * 0.5f, 0);
                halfHeight.applyMatrix4(matrix42);
                uniforms.put("halfHeight", halfHeight);

                state.rectArea.add(uniforms);
            } else if (light instanceof PointLight) {
                Uniforms uniforms = cache.get(light);
                Vector3 position = new Vector3().setFromMatrixPosition(light.getWorldMatrix());
                position.applyMatrix4(viewMatrix);
                uniforms.put("position", position);

                uniforms.put("color", new Color(light.color).multiplyScalar(intensity));
                uniforms.put("distance", ((PointLight) light).distance);
                uniforms.put("decay", ((PointLight) light).decay);

                uniforms.put("shadow", light.castShadow);
                if (light.castShadow) {
                    LightShadow shadow = light.getShadow();
                    uniforms.put("shadowBias", shadow.bias);
                    uniforms.put("shadowRadius", shadow.radius);
                    uniforms.put("shadowMapSize", shadow.mapSize);
                    uniforms.put("shadowCameraNear", shadow.camera.near);
                    uniforms.put("shadowCameraFar", shadow.camera.far);
                } else {
                    uniforms.put("shadowBias", 0f);
                    uniforms.put("shadowRadius", 1f);
                    uniforms.put("shadowMapSize", new Vector2());
                    uniforms.put("shadowCameraNear", 1f);
                    uniforms.put("shadowCameraFar", 1000f);
                }
                state.pointShadowMap.add(shadowMap);
                state.pointShadowMatrix.add(light.getShadow().matrix);
                state.point.add(uniforms);
            } else if (light instanceof HemisphereLight) {
                HemisphereLight hemiLight = (HemisphereLight) light;
                Uniforms uniforms = cache.get(light);
                uniforms.put("direction", new Vector3().setFromMatrixPosition( light.getModelMatrix() )
                        .transformDirection(viewMatrix).normalize() );

                uniforms.put("skyColor", new Color(light.color).multiplyScalar(intensity));
                uniforms.put("groundColor", new Color(hemiLight.groundColor).multiplyScalar(intensity));
                state.hemi.add(uniforms);
            }
            state.ambient[0] = r;
            state.ambient[1] = g;
            state.ambient[2] = b;

            State.Hash hash = state.hash;
            if ( hash.directionalLength != state.directional.size() ||
                    hash.pointLength != state.point.size() ||
                    hash.spotLength != state.spot.size() ||
                    hash.rectAreaLength != state.rectArea.size() ||
                    hash.hemiLength != state.hemi.size() ||
                    hash.shadowsLength != shadows.length ) {

                hash.directionalLength = state.directional.size();
                hash.pointLength = state.point.size();
                hash.spotLength = state.spot.size();
                hash.rectAreaLength = state.rectArea.size();
                hash.hemiLength = state.hemi.size();
                hash.shadowsLength = shadows.length;

                state.version = nextVersion ++;

            }
        }
    }

    public static class State {
        public int version = 0;
        public Hash hash = new Hash();
        public float[] ambient = new float[] { 0, 0, 0};
        public Vector3[] probe;
        public ArrayList<Uniforms> directional = new ArrayList<>();
        public ArrayList<Texture> directionalShadowMap = new ArrayList<>();
        public ArrayList<Matrix4> directionalShadowMatrix = new ArrayList<>();

        public ArrayList<Uniforms> spot = new ArrayList<>();
        public ArrayList<Texture> spotShadowMap = new ArrayList<>();
        public ArrayList<Matrix4> spotShadowMatrix = new ArrayList<>();

        public ArrayList<Uniforms> rectArea = new ArrayList<>();
        public ArrayList<Uniforms> point = new ArrayList<>();
        public ArrayList<Texture> pointShadowMap = new ArrayList<>();
        public ArrayList<Matrix4> pointShadowMatrix = new ArrayList<>();
        public ArrayList<Uniforms> hemi = new ArrayList<>();

        public void clear() {
            directional.clear();
            directionalShadowMap.clear();
            directionalShadowMatrix.clear();
            spot.clear();
            spotShadowMap.clear();
            spotShadowMatrix.clear();

            rectArea.clear();
            point.clear();
            pointShadowMap.clear();
            pointShadowMatrix.clear();
            hemi.clear();
        }

        public State() {
            probe = new Vector3[9];
            for (int i = 0; i < 9; i++) {
                probe[i] = new Vector3();
            }
        }

        public static class Hash {
            int directionalLength = -1;
            int pointLength = -1;
            int spotLength = -1;
            int rectAreaLength = -1;
            int hemiLength = -1;
            int shadowsLength = -1;
        }
    }

    public static class UniformsCache {
        HashMap<Long, Uniforms> lights = new HashMap<>();

        public Uniforms get(Light light) {
            if (lights.get(light.id) != null) {
                return lights.get(light.id);
            }
            Uniforms uniforms = new Uniforms();
            lights.put(light.id, uniforms);
            return uniforms;
        }
    }

    public static class Uniforms {
        HashMap<String, Object> hashMap = new HashMap<>();

        public Uniforms put(String key, Object value) {
            hashMap.put(key, value);
            return this;
        }

        public Object get(String key) {
            return hashMap.get(key);
        }

        public boolean contains(String key) {
            return hashMap.containsKey(key);
        }

//        Vector3 direction = new Vector3();
//        Color color;
//        boolean shadow = false;
//        float shadowBias = 0;
//        float shadowRadius = 1;
//        Vector2 shadowMapSize = new Vector2();
//
//        Vector3 position = new Vector3();
//        float distance = 0;
//        float coneCos = 0;
//        float penumbraCos = 0;
//        float decay = 0;
//
//        float shadowCameraNear = 1;
//        float shadowCameraFar = 1000;
//
//        Color skyColor;
//        Color groundColor;
//
//        Vector3 halfWidth = new Vector3();
//        Vector3 halfHeight = new Vector3();
    }
}
