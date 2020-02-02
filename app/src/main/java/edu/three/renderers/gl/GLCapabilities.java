package edu.three.renderers.gl;

import android.opengl.GLES30;

import edu.three.renderers.GLRenderer;

public class GLCapabilities {
    GLRenderer.Param parameters;
    public final int maxAnisotropy = 0;
    public final int maxTextures;
    public final int maxVertexTextures;
    public final int maxTextureSize;
    public final int maxCubemapSize;

    public final int maxAttributes;
    public final int maxVertexUniforms;
    public final int maxVaryings;
    public final int maxFragmentUniforms;
    public final int maxSamples;
    public final boolean floatVertexTextures = false;
    public final boolean logarithmicDepthBuffer = false;
    public final boolean vertexTextures;
    public final boolean floatFragmentTextures = true;
    public final String precision;

    public GLCapabilities(GLRenderer.Param parameters) {
        this.parameters = parameters;
        precision = parameters.precision;
        int[] temp = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_IMAGE_UNITS, temp, 0);
        maxTextures = temp[0];
        vertexTextures = maxTextures > 0;
        GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, temp, 0);
        maxVertexTextures = temp[0];
        GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, temp, 0);
        maxTextureSize = temp[0];
        GLES30.glGetIntegerv(GLES30.GL_MAX_CUBE_MAP_TEXTURE_SIZE, temp, 0);
        maxCubemapSize = temp[0];

        GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_ATTRIBS, temp, 0);
        maxAttributes = temp[0];
        GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_UNIFORM_VECTORS, temp, 0);
        maxVertexUniforms = temp[0];
        GLES30.glGetIntegerv(GLES30.GL_MAX_VARYING_VECTORS, temp, 0);
        maxVaryings = temp[0];
        GLES30.glGetIntegerv(GLES30.GL_MAX_FRAGMENT_UNIFORM_VECTORS, temp, 0);
        maxFragmentUniforms = temp[0];

        GLES30.glGetIntegerv(GLES30.GL_MAX_SAMPLES, temp, 0);
        maxSamples = temp[0];
    }
}
