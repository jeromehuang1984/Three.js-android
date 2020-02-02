package edu.three.renderers.gl;

import android.opengl.GLES30;

import edu.three.core.BufferGeometry;

public class GLIndexedBufferRenderer extends GLBufferRenderer {
    int type;
    int bytesPerElement;

    public GLIndexedBufferRenderer(GLInfo glInfo) {
        super(glInfo);
    }

    public void setIndex(GLAttributes.BufferItem value) {
        type = value.type;
        bytesPerElement = value.bytesPerElement;
    }

    public void render(int start, int count) {
        GLES30.glDrawElements(mode, count, type, start * bytesPerElement);
        info.update(count, mode, 0);
    }

    public void renderInstances(BufferGeometry geometry, int start, int count) {
        GLES30.glDrawElementsInstanced(mode, count, type, start * bytesPerElement, geometry.maxInstancedCount());
        info.update(count, mode, geometry.maxInstancedCount());
    }
}
