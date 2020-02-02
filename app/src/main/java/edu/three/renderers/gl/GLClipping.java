package edu.three.renderers.gl;

import edu.three.cameras.Camera;
import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Plane;

public class GLClipping {
    float[] globalState = null;
    public int numGlobalPlanes;
    boolean localClippingEnabled = false;
    boolean renderingShadows = false;
    Plane plane = new Plane();
    Matrix3 viewNormalMatrix = new Matrix3();

    public float[] uniformValue = null;
    public boolean uniformNeedsUpdate = false;

    public int numPlanes = 0;
    public int numIntersection = 0;

    public boolean init(Plane[] planes, boolean enableLocalClipping, Camera camera) {
        boolean enabled = planes.length != 0 ||
                enableLocalClipping ||
                // enable state of previous frame - the clipping code has to
                // run another frame in order to reset the state:
                numGlobalPlanes != 0 ||
                localClippingEnabled;

        localClippingEnabled = enableLocalClipping;
        globalState = projectPlanes( planes, camera, 0, false );
        numGlobalPlanes = planes.length;

        return enabled;
    }

    public void beginShadows() {
        renderingShadows = true;
        projectPlanes(null, null, 0, false);
    }

    public void endShadows() {
        renderingShadows = false;
        resetGlobalState();
    }

    public void setState(Plane[] planes, boolean clipIntersection, boolean clipShadows, Camera camera, GLProperties.Fields cache, boolean fromCache) {
        if ( ! localClippingEnabled || planes == null || planes.length == 0 || renderingShadows && ! clipShadows ) {
            // there's no local clipping
            if ( renderingShadows ) {
                // there's no global clipping
                projectPlanes( );

            } else {
                resetGlobalState();
            }
        } else {
            int nGlobal = renderingShadows ? 0 : numGlobalPlanes;
            int lGlobal = nGlobal * 4;
            float[] dstArray = cache.clippingState;
            uniformValue = dstArray;
            dstArray = projectPlanes(planes, camera, lGlobal, fromCache);

            for (int i = 0; i != lGlobal; i++) {
                dstArray[i] = globalState[i];
            }
            cache.clippingState = dstArray;
            numIntersection = clipIntersection ? numPlanes : 0;
            numPlanes += nGlobal;
        }
    }

    public float[] projectPlanes() {
        return projectPlanes(null, null, 0, false);
    }

    public float[] projectPlanes(Plane[] planes, Camera camera, int dstOffset, boolean skipTransform) {
        int nPlanes = planes != null ? planes.length : 0;
        float[] dstArray = null;
        if (nPlanes != 0) {
            dstArray = uniformValue;
            if (!skipTransform || dstArray == null) {
                int flatSize = dstOffset + nPlanes * 4;
                Matrix4 viewMatrix = camera.matrixWorldInverse;

                viewNormalMatrix.getNormalMatrix(viewMatrix);
                if (dstArray == null || dstArray.length < flatSize) {
                    dstArray = new float[flatSize];
                }
                for (int i = 0, i4 = dstOffset; i < nPlanes; i++, i4 += 4) {
                    plane.copy(planes[i]).applyMatrix4(viewMatrix, viewNormalMatrix);
                    plane.normal.toArray(dstArray, i4);
                    dstArray[i4 + 3] = plane.constant;
                }
            }
        }
        uniformValue = dstArray;
        uniformNeedsUpdate = true;
        numPlanes = nPlanes;
        return dstArray;
    }

    public void resetGlobalState() {
        if (uniformValue != globalState) {
            uniformValue = globalState;
            uniformNeedsUpdate = numGlobalPlanes > 0;
        }
        numPlanes = numGlobalPlanes;
        numIntersection = 0;
    }

}
