package edu.cs4730.opengl30cube.renderer;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.three.control.ITouchable;
import edu.three.renderers.GLRenderer;

public class BaseRender implements GLSurfaceView.Renderer {
    protected int mWidth;
    protected int mHeight;
    protected float aspect;
    protected String TAG = getClass().getSimpleName();
    protected GLRenderer renderer;

    protected ITouchable controls;
    protected GLSurfaceView mView;

    //
    public BaseRender(GLSurfaceView view) {
        mView = view;
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
    }

    // /
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame(GL10 glUnused) {
    }

    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    }

    public void onPause() {
        if (renderer != null) {
            renderer.onContextLost();
        }
    }

    public void onTouchEvent(MotionEvent event) {
        if (controls != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    controls.touchDown(event);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    controls.touchDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    controls.touchMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    controls.touchUp();
                    break;
            }
        }
    }
}
