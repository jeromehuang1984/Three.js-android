package edu.three.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import edu.three.cameras.Camera;
import edu.three.cameras.OrthographicCamera;
import edu.three.cameras.PerspectiveCamera;
import edu.three.math.Ray;
import edu.three.math.MathTool;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.objects.RaycastItem;

public class Raycaster {
    Camera _camera;
    Vector3 origin;
    Vector3 direction;
    public final float linePrecision = 1;
    public float near;
    public float far;
    public Ray ray;
    public Config params = new Config();

    public Raycaster() {
        ray = new Ray();
    }

    public Raycaster(Vector3 origin, Vector3 direction, float near, float far) {
        this.origin = origin;
        this.direction = direction;
        this.near = near;
        this.far = far;
        ray = new Ray().set(origin, direction);
    }

    private Comparator<RaycastItem> ascSort = new Comparator<RaycastItem>() {
        @Override
        public int compare(RaycastItem a, RaycastItem b) {
            return MathTool.sign(a.distance - b.distance);
        }
    };

    private void intersectObject(Object obj, Raycaster raycaster, ArrayList<RaycastItem> intersects,
                                 boolean recursive) {
        Object3D object = null;
        if (obj instanceof Object3D) {
            object = (Object3D) obj;
        } else {
            return;
        }
        if (!object.visible) {
            return;
        }
        object.raycast(raycaster, intersects);
        if (recursive) {
            for (Object3D child : object.children) {
                intersectObject(child, raycaster, intersects, true);
            }
        }
    }

    public Camera getCamera() {
        return _camera;
    }

    public void set(Vector3 origin, Vector3 direction) {
        ray.set(origin, direction);
    }

    public void setFromCamera(Vector2 coords, Camera camera) {
        if (camera != null) {
            near = camera.near;
            far = camera.far;
            if (camera instanceof PerspectiveCamera) {
                ray.getOrigin().setFromMatrixPosition(camera.getWorldMatrix());
                ray.getDirection().set(coords.x, coords.y, 0.5f).unproject(camera);
                ray.getDirection().sub(ray.getOrigin()).normalize();
                _camera = camera;
            } else if (camera instanceof OrthographicCamera) {
                ray.getOrigin().set(coords.x, coords.y, (camera.near + camera.far) / (camera.near - camera.far))
                        .unproject(camera); // set origin in plane of camera
                ray.getDirection().set(0, 0, -1).transformDirection(camera.getWorldMatrix());
                _camera = camera;
            }
        }
    }

    public ArrayList<RaycastItem> intersectObject(Object3D object, boolean recursive, ArrayList<RaycastItem> optionalTarget) {
        if (optionalTarget == null) {
            optionalTarget = new ArrayList<>();
        }
        intersectObject(object, this, optionalTarget, recursive);
        Collections.sort(optionalTarget, ascSort);
        return optionalTarget;
    }

    public ArrayList<RaycastItem> intersectObjects(ArrayList objects, boolean recursive) {
        return intersectObjects(objects, recursive, new ArrayList<RaycastItem>());
    }
    public ArrayList<RaycastItem> intersectObjects(ArrayList objects, boolean recursive, ArrayList<RaycastItem> optionalTarget) {
        if (optionalTarget == null) {
            optionalTarget = new ArrayList<>();
        }
        for (Object object : objects) {
            intersectObject(object, this, optionalTarget, recursive);
        }
        Collections.sort(optionalTarget, ascSort);
        return optionalTarget;
    }

    public static class Config {
        public ConfigVals Mesh = new ConfigVals();
        public ConfigVals Line = new ConfigVals();
        public ConfigVals LOD = new ConfigVals();
        public ConfigVals Points = new ConfigVals();
        public ConfigVals Sprite = new ConfigVals();
        public Config() {
            Points.threshold = 1;
        }
    }

    public static class ConfigVals {
        public float threshold;
    }
}
