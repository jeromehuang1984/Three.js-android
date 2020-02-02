package edu.three.lights;

public class RectAreaLight extends Light {
    public int width, height;

    public RectAreaLight copy(RectAreaLight light) {
        super.copy(light);
        width = light.width;
        height = light.height;
        return this;
    }
}
