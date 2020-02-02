package edu.three.cameras;

public class View {
    public boolean enabled = true;
    float fullWidth = 1;
    float fullHeight = 1;
    float offsetX = 0;
    float offsetY = 0;
    float width = 1;
    float height = 1;

    public View() {}

    public View(boolean enabled, float fullWidth, float fullHeight,
                float offsetX, float offsetY, float width, float height) {
        this.enabled = enabled;
        this.fullWidth = fullWidth;
        this.fullHeight = fullHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
    }

    public View clone() {
        return new View(enabled, fullWidth, fullHeight,
                offsetX, offsetY, width, height);
    }
}
