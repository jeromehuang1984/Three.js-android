package edu.three.lights;

import edu.three.cameras.PerspectiveCamera;

public class PointLight extends Light {
    public float distance = 0;
    // for physically correct lights, should be 2.
    public float decay = 1;
    public LightShadow shadow;

    public PointLight(int color) {
        this(color, 1, 0, 1);
    }
    public PointLight(int color, float intensity, float distance, float decay) {
        this.color = color;
        this.intensity = intensity;
        this.distance = distance;
        this.decay = decay;
        shadow = new LightShadow(new PerspectiveCamera(90, 1, 0.5f, 500));
    }

    // intensity = power per solid angle.
    // ref: equation (15) from https://seblagarde.files.wordpress.com/2015/07/course_notes_moving_frostbite_to_pbr_v32.pdf
    public float getPower() {
        return (float)intensity * 4 * (float) Math.PI;
    }

    public void setPower(float power) {
        intensity = power / (4 * (float) Math.PI);
    }

    @Override
    public Light copy(Light source) {
        super.copy(source);
        PointLight light = (PointLight) source;
        distance = light.distance;
        decay = light.decay;
        shadow = light.shadow.clone();
        return this;
    }

    public float getDistance() {
        return distance;
    }

    public LightShadow getShadow() {
        return shadow;
    }
}
