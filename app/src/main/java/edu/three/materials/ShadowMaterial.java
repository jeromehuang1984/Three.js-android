package edu.three.materials;

import edu.three.math.Color;

public class ShadowMaterial extends Material {
    public boolean transparent = true;

    public ShadowMaterial() {
        color = new Color().setHex(0x000000);
    }
}
