package edu.three.cameras;

import edu.three.core.Object3D;
import edu.three.math.MathTool;
import edu.three.math.Vector4;

public class PerspectiveCamera extends Camera {
    public float fov = 50;
    public float zoom = 1;

    float focus = 10;

    public float aspect = 1;

    int filmGauge = 35; // width of the film (default in millimeters)
    int filmOffset = 0; // horizontal film offset (same unit as gauge)
    View view = null;

    //for VR, XR only, XR(VR, AR, MR)
    public Vector4 viewport = new Vector4();

    public PerspectiveCamera() {
        near = 0.1f;
        far = 2000;
    }

    public PerspectiveCamera(float fov, float aspect, float near, float far) {
        this.fov = fov;
        this.near = near;
        this.far = far;
        this.aspect = aspect;
        updateProjectionMatrix();
    }

    /**
     * Sets the FOV by focal length in respect to the current .filmGauge.
     *
     * The default film gauge is 35, so that the focal length can be specified for
     * a 35mm (full frame) camera.
     *
     * Values for focal length and film gauge must have the same unit.
     */
    public void setFocalLength(float focalLength) {
        float vExtentSlope = 0.5f * getFilmHeight() / focalLength;
        fov = MathTool.RAD2DEG * 2 * (float) Math.atan(vExtentSlope);
        updateProjectionMatrix();
    }
    //Calculates the focal length from the current .fov and .filmGauge.
    public float getFocalLength() {
        float vExtentSlope = (float) Math.tan(MathTool.DEG2RAD * 0.5f * fov);
        return 0.5f * getFilmHeight() / vExtentSlope;
    }
    public float getEffectiveFOV() {
        return MathTool.RAD2DEG * 2 * (float) Math.atan(
                Math.tan(MathTool.DEG2RAD * 0.5 * fov) / zoom
        );
    }
    public float getFilmWidth() {
        // film not completely covered in portrait format (aspect < 1)
        return filmGauge * Math.min(aspect, 1);
    }

    public float getFilmHeight() {
        // film not completely covered in landscape format (aspect > 1)
        return filmGauge / Math.max( this.aspect, 1 );
    }
    public void updateProjectionMatrix() {
        float top = near * (float) Math.tan(MathTool.DEG2RAD * 0.5f * fov) / zoom;
        float height = 2 * top;
        float width = aspect * height;
        float left = -0.5f * width;
        if (view != null && view.enabled) {
            float fullWidth = view.fullWidth;
            float fullHeight = view.fullHeight;

            left += view.offsetX * width / fullWidth;
            top -= view.offsetY * height / fullHeight;
            width *= view.width / fullWidth;
            height *= view.height / fullHeight;
        }
        if (filmOffset != 0) {
            left += near * filmOffset / getFilmWidth();
        }
        projectionMatrix.makePerspective(left, left + width, top,  top - height, near, far);
        projectionMatrixInverse.getInverse(projectionMatrix);
    }

    public void setViewOffset(float fullWidth, float fullHeight, float x, float y, float width, float height) {
        aspect = fullWidth / fullHeight;
        if (view == null) {
            view = new View();
        }
        view.enabled = true;
        view.fullWidth = fullWidth;
        view.fullHeight = fullHeight;
        view.offsetX = x;
        view.offsetY = y;
        view.width = width;
        view.height = height;
        updateProjectionMatrix();
    }

    public void clearViewOffset() {
        if (view != null) {
            view.enabled = false;
        }
        updateProjectionMatrix();
    }

    @Override
    public Object3D copy(Object3D src, boolean recursive) {
        super.copy(src, recursive);
        PerspectiveCamera camera = (PerspectiveCamera) src;
        fov = camera.fov;
        zoom = camera.zoom;

        near = camera.near;
        far = camera.far;
        focus = camera.focus;

        aspect = camera.aspect;
        view = camera.view == null ? null : camera.view.clone();

        filmGauge = camera.filmGauge;
        filmOffset = camera.filmOffset;
        return this;
    }
}
