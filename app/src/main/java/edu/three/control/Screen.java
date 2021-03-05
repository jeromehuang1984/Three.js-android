package edu.three.control;

public class Screen {
    public int left, top, width, height;
    public float devicePixelRatio;
    float aspect;
    public Screen(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        aspect = (float) width / height;
    }

    public Screen setDensity(float density) {
        devicePixelRatio = density;
        return this;
    }

    public float getAspect() {
        return aspect;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        aspect = (float) width / height;
    }
}
