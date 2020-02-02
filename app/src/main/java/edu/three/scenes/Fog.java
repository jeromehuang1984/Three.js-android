package edu.three.scenes;

import edu.three.math.Color;

public class Fog {
    public String name;
    public Color color;

    public float near = 1;
    public float far = 1000;

    public Fog() {}

    public Fog(Color color, float near, float far) {
        this.color = color;
        this.near = near;
        this.far = far;
    }

    public Fog clone() {
        return new Fog(color, near, far);
    }
}
