package edu.three.cameras;

public class View {
    public boolean enabled = true;
    double fullWidth = 1;
    double fullHeight = 1;
    double offsetX = 0;
    double offsetY = 0;
    double width = 1;
    double height = 1;

    public View() {}

    public View(boolean enabled, double fullWidth, double fullHeight,
                double offsetX, double offsetY, double width, double height) {
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
