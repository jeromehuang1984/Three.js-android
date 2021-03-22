package edu.three.materials;


import java.util.HashMap;

import edu.three.renderers.shaders.UniformsLib;

public class ShaderMaterial extends Material {
    public String index0AttributeName;
    HashMap<String, float[]> defaultAttributeValues = new HashMap<>();

    public boolean uniformsNeedUpdate = false;

    public ShaderMaterial() {
        defaultAttributeValues.put("color", new float[] {1, 1, 1});
        defaultAttributeValues.put("uv", new float[] {0, 0});
        defaultAttributeValues.put("uv2", new float[] {0, 0});
    }

    public HashMap<String, float[]> defaultAttributeValues() {
        return defaultAttributeValues;
    }
}
