package edu.three.core;

import edu.three.cameras.Camera;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;

public interface ICallback {
    void call(GLRenderer renderer, Scene scene, Camera camera);
}
