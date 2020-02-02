package edu.three.materials;

import edu.three.math.Color;

public class PointsMaterial extends Material {
    public float size = 1;
    //sizeAttenuation
    //morphTargets

    public PointsMaterial() {
        map = null;
        lights = false;
        color = new Color().setHex(0xffffff);
    }
}
