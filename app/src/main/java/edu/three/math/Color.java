package edu.three.math;

public class Color {
    public float r, g, b;
    int hex;

    public Color() {
    }
    public Color(int hex) {
        setHex(hex);
    }
    public Color setHex(int hex) {
        this.hex = hex;
        this.r = ( hex >> 16 & 255 ) / 255f;
        this.g = ( hex >> 8 & 255 ) / 255f;
        this.b = ( hex & 255 ) / 255f;

        return this;
    }

    public Color multiplyScalar(float s) {
        this.r *= s;
        this.g *= s;
        this.b *= s;

        return this;
    }

    public int getHex() {
        return (int)( r * 255 ) << 16 ^ (int)( g * 255 ) << 8 ^ (int)( b * 255 ) << 0;
    }

    public Color fromArray(float[] array) {
        return fromArray(array, 0);
    }
    public Color fromArray(float[] array, int offset) {
        r = array[ offset ];
        g = array[ offset + 1 ];
        b = array[ offset + 2 ];
        return this;
    }

    public Color copy(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;

        return this;
    }

    public Color clone() {
        return new Color().copy(this);
    }
}
