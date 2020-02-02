package edu.three.renderers.gl;

import android.opengl.GLES30;
import android.util.Log;

import java.util.ArrayList;

public class GLInfo {
    public ArrayList<GLProgram> programs;
    // memory
    public int geometries = 0;
    public int textures = 0;

    // render
    public int frame = 0;
    public int calls = 0;
    public int triangles = 0;
    public int points = 0;
    public int lines = 0;

    public boolean autoReset = true;

    public void reset() {
        frame++;
        calls = 0;
        triangles = 0;
        points = 0;
        lines = 0;
    }

    public void update(int count, int mode, int instanceCount) {
        if (instanceCount <= 0) {
            instanceCount = 1;
        }
        calls++;
        switch (mode) {
            case GLES30.GL_TRIANGLES:
                triangles += instanceCount * ( count / 3 );
                break;
            case GLES30.GL_TRIANGLE_STRIP:
            case GLES30.GL_TRIANGLE_FAN:
                triangles += instanceCount * ( count - 2 );
                break;

            case GLES30.GL_LINES:
                lines += instanceCount * ( count / 2 );
                break;

            case GLES30.GL_LINE_STRIP:
                lines += instanceCount * ( count - 1 );
                break;

            case GLES30.GL_LINE_LOOP:
                lines += instanceCount * count;
                break;

            case GLES30.GL_POINTS:
                points += instanceCount * count;
                break;

            default:
                Log.e(getClass().getSimpleName(), "THREE.WebGLInfo: Unknown draw mode:" + mode);
                break;
        }
    }
}
