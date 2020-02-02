package edu.three.renderers.gl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.WeakHashMap;

import edu.three.renderers.gl.uniform.Uniform;
import edu.three.renderers.shaders.ShaderLib;
import edu.three.renderers.shaders.UniformsLib;
import edu.three.scenes.Fog;

public class GLProperties {
    WeakHashMap<Object, Fields> properties = new WeakHashMap<>();

    public Fields get(Object object) {
        Fields map = properties.get(object);
        if (map == null) {
            map = new Fields();
            properties.put(object, map);
        }
        return map;
    }

    public void remove(Object object) {
        properties.remove(object);
    }

    public void update(Object object, String key, Object value) {
        Fields val = properties.get(object);
        if (val != null) {
            try {
                Field field = Fields.class.getField(key);
                field.set(val, value);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispose() {
        properties = new WeakHashMap<>();
    }

    public static class Fields {
        public float[] clippingState = null;
        public int maxMipLevel;
        public boolean glInit = false;
        public Integer glTexture = null;

        public int[][] glFramebuffers;
        public int[] glFramebuffer;
        public int[] glDepthbuffer;
        public int[] glMultisampledFramebuffer;
        public int[] glColorRenderbuffer;
        public int[] glDepthRenderbuffer;
        public int version;

        public GLProgram program;

        public int[] position;
        public int[] normal;
        public int[] uv;
        public int[] color;

        public int lightsStateVersion;

        public String shaderName;
        public ShaderLib shaderLib;

        public int numClippingPlanes = -1;
        public int numIntersection;

        public Fog fog;

        public ArrayList<Uniform> uniformsList;
    }
}
