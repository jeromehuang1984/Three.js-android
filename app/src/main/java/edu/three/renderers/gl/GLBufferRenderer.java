package edu.three.renderers.gl;

import android.opengl.GLES30;

import edu.three.core.BufferGeometry;

public class GLBufferRenderer {
    int mode;
    GLInfo info;

    public GLBufferRenderer(GLInfo glInfo) {
        info = glInfo;
    }

    public void setMode(int value) {
        mode = value;
    }

    public void render(int start, int count) {
        GLES30.glDrawArrays(mode, start, count);
        info.update(count, mode, 0);
    }

    public void renderInstances(BufferGeometry geometry, int start, int count) {
        GLES30.glDrawArraysInstanced(mode, start, count, geometry.maxInstancedCount());
        info.update(count, mode, geometry.maxInstancedCount());
    }
}
