package edu.three.cameras;

import edu.three.core.Object3D;

public class OrthographicCamera extends Camera {
    float zoom = 1;
    View view = null;

    int left = -1;
    int right = 1;
    int top = 1;
    int bottom = -1;

    public OrthographicCamera() {
        near = 0.1f;
        far = 2000;
    }
    public OrthographicCamera(int left, int right, int top, int bottom, float near, float far) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.near = near;
        this.far = far;
    }

    public void updateProjectionMatrix() {
        float dx = (right - left) / (2 * zoom);
        float dy = (top - bottom) / (2 * zoom);
        float cx = (right + left) / 2;
        float cy = (top + bottom) / 2;
        float left = cx - dx;
        float right = cx + dx;
        float top = cy + dy;
        float bottom = cy - dy;

        if (view != null && view.enabled) {
            float zoomW = zoom / (view.width / view.fullWidth);
            float zoomH = zoom / (view.height / view.fullHeight);
            float scaleW = (right - left) / view.width;
            float scaleH = (top - bottom) / view.height;

            left += scaleW * (view.offsetX / zoomW);
            right = left + scaleW * (view.width / zoomW);
            top -= scaleH * (view.offsetY / zoomH);
            bottom = top - scaleH * (view.height / zoomH);
        }
        projectionMatrix.makeOrthographic(left, right, top, bottom, near, far);
        projectionMatrixInverse.getInverse(projectionMatrix);
    }

    public void setViewOffset(float fullWidth, float fullHeight, float x, float y, float width, float height) {
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
        OrthographicCamera camera = (OrthographicCamera) src;
        left = camera.left;
        right = camera.right;
        top = camera.top;
        bottom = camera.bottom;
        near = camera.near;
        far = camera.far;

        zoom = camera.zoom;
        view = camera.view == null ? null : camera.view.clone();

        return this;
    }
}
