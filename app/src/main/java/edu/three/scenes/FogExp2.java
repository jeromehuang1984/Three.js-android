package edu.three.scenes;

import edu.three.math.Color;

public class FogExp2 extends Fog {
    public float density = 0.00025f;

    public FogExp2(Color color, float density) {
        this.color = color;
        this.density = density;
    }

    public FogExp2 clone() {
        return new FogExp2(color, density);
    }
}
