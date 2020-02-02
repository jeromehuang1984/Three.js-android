package edu.three.materials;

import edu.three.math.Color;
import edu.three.textures.Texture;

public class SpriteMaterial extends Material {
    public float rotation = 0;

    public SpriteMaterial() {
        map = null;
        lights = false;
        transparent = true;
        color = new Color().setHex(0xffffff);
        sizeAttenuation = true;
    }

    public static class Param {
        public int color;
        public Texture map;
        public float rotation;
        public boolean sizeAttenuation;
    }
}
