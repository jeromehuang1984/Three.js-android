package edu.three.objects;

import java.util.ArrayList;

import edu.three.cameras.Camera;
import edu.three.core.Object3D;
import edu.three.core.Raycaster;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

//Level of Detail
public class LOD extends Object3D {
    public boolean autoUpdate = true;
    public ArrayList<LevelItem> levels = new ArrayList<>();

    public LOD addLevel(Object3D object, Float distance) {
        if (distance == null) {
            distance = 0f;
        }
        distance = Math.abs(distance);
        int i;
        for (i = 0; i < levels.size(); i++) {
            if (distance < levels.get(i).distance) {
                break;
            }
        }
        levels.add(i, new LevelItem(distance, object));
        this.add( object );

        return this;
    }

    public Object3D getObjectForDistance(double distance) {
        int i;
        for (i = 1; i < levels.size(); i++) {
            if (distance < levels.get(i).distance) {
                break;
            }
        }
        return levels.get(i - 1).object;
    }

    public LOD copy(LOD source) {
        super.copy(source, false);
        ArrayList<LevelItem> otherLevels = source.levels;
        for (int i = 0; i < otherLevels.size(); i++) {
            LevelItem level = otherLevels.get(i);
            addLevel(level.object.clone(), level.distance);
        }
        return this;
    }

    public void raycast(Raycaster raycaster, ArrayList<RaycastItem> intersects) {
        Vector3 matrixPosition = new Vector3();
        matrixPosition.setFromMatrixPosition(matrixWorld);
        double distance = raycaster.ray.getOrigin().distanceTo(matrixPosition);
        getObjectForDistance(distance).raycast(raycaster, intersects);
    }

    public void update(Camera camera) {
        Vector3 v1 = new Vector3();
        Vector3 v2 = new Vector3();
        if (levels.size() > 1) {
            v1.setFromMatrixPosition(camera.getWorldMatrix());
            v2.setFromMatrixPosition(matrixWorld);
            double distance = v1.distanceTo(v2);
            levels.get(0).object.visible = true;
            int i;
            for (i = 1; i < levels.size(); i++) {
                if (distance >= levels.get(i).distance) {
                    levels.get(i - 1).object.visible = false;
                    levels.get(i).object.visible = true;
                } else {
                    break;
                }
            }
            for(;i < levels.size(); i++) {
                levels.get(i).object.visible = false;
            }
        }
    }

    public static class LevelItem {
        Float distance;
        Object3D object;

        public LevelItem(Float distance, Object3D object) {
            this.distance = distance;
            this.object = object;
        }
    }
}
