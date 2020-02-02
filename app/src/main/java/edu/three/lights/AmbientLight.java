package edu.three.lights;

public class AmbientLight extends Light {
    public boolean castShadow;
    public AmbientLight(int color) {
        this.color = color;
    }
}
