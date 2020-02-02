package edu.three.renderers.gl;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.cameras.Camera;
import edu.three.core.Event;
import edu.three.core.IListener;
import edu.three.lights.Light;
import edu.three.scenes.Scene;

public class GLRenderStates {
    HashMap<Long, HashMap<Long, State>> renderStates = new HashMap<>();

    private IListener onSceneDispose = new IListener() {
        @Override
        public void onEvent(Event event) {
            Scene scene = (Scene) event.target;
            scene.removeEventListener("dispose", onSceneDispose);
            renderStates.remove(scene.id);
        }
    };

    public State get(Scene scene, Camera camera) {
        State renderState;
        if (renderStates.get(scene.id) == null) {
            renderState = new State();
            renderStates.put(scene.id, new HashMap<Long, State>());
            renderStates.get(scene.id).put(camera.id, renderState);
            scene.addEventListener("dispose", onSceneDispose);
        } else {
            if (renderStates.get(scene.id).get(camera.id) == null) {
                renderState = new State();
                renderStates.get(scene.id).put(camera.id, renderState);
            } else {
                renderState = renderStates.get(scene.id).get(camera.id);
            }
        }
        return renderState;
    }

    public void dispose() {
        renderStates.clear();
    }

    public static class State {
        public GLLights lights = new GLLights();
        public ArrayList<Light> lightsArray = new ArrayList<>();
        public ArrayList<Light> shadowsArray = new ArrayList<>();

        public void init() {
            lightsArray.clear();
            shadowsArray.clear();
        }

        public void pushLight(Light light) {
            lightsArray.add(light);
        }

        public void pushShadow(Light shadowLight) {
            shadowsArray.add(shadowLight);
        }

        public void setupLights(Camera camera) {
            Light[] lightLst = new Light[lightsArray.size()];
            lightsArray.toArray(lightLst);
            Light[] shadows = new Light[shadowsArray.size()];
            shadowsArray.toArray(shadows);
            lights.setup(lightLst, shadows, camera);
        }
    }
}
