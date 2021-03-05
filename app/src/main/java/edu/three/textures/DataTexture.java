package edu.three.textures;

import android.graphics.Bitmap;

public class DataTexture extends Texture {
    double[] data;
    int width;
    int height;
    int format;
    int type;
    public boolean needsUpdate;

    public DataTexture(double[] data, int width, int height, int format, int type) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.format = format;
        this.type = type;
    }
}
