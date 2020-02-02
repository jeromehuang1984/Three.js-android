package edu.three.lights;

import edu.three.core.Object3D;
import edu.three.math.Vector3;

public class SpotLight extends Light {
    public Object3D target = new Object3D();

    public float distance = 0;
    public float angle = (float) Math.PI / 3;
    public float penumbra = 0;
    public float decay = 1;    //for physically correct lights, should be 2.
    SpotLightShadow shadow = new SpotLightShadow();

    public SpotLight(int color) {
        this(color, 1);
    }
    public SpotLight(int color, float intensity) {
        this.color = color;
        this.intensity = intensity;
        position = Object3D.DefaultUp.clone();
        updateMatrix();
    }

    public SpotLight(int color, float intensity, float distance, float angle, float penumbra, float decay) {
        this(color, intensity);
        this.distance = distance;
        this.angle = angle;
        this.penumbra = penumbra;
        this.decay = decay;
    }

    public float getDistance() {
        return distance;
    }

    public SpotLight copy(SpotLight source) {
        super.copy(source);
        color = source.color;
        distance = source.distance;
        angle = source.angle;
        penumbra = source.penumbra;
        decay = source.decay;
        target = source.target.clone();
        shadow = (SpotLightShadow) source.shadow.clone();
        return this;
    }

    public LightShadow getShadow() {
        return shadow;
    }
}
