package edu.three.scenes;

import java.util.ArrayList;

import edu.three.core.Object3D;
import edu.three.materials.Material;
import edu.three.objects.Mesh;

public class Scene extends Object3D {

    public Object background;   //Color, Texture
    public Fog fog;
    ArrayList<Mesh> meshLst = null;

    public boolean autoUpdate = true;
    public Material overrideMaterial = null;

//    private ArrayList<Mesh> getMeshLst() {
//        ArrayList<Mesh> ret = new ArrayList<>();
//        ArrayList<Object3D> bfsLst = new ArrayList<>();
//        bfsLst.add(this);
//        while (bfsLst.size() > 0) { //broad first traverse
//            Object3D item = bfsLst.get(0);
//            bfsLst.remove(0);
//            if (item instanceof Mesh) {
//                ret.add((Mesh) item);
//            }
//            if (item.children.size() > 0) {
//                bfsLst.addAll(item.children);
//            }
//        }
//        return ret;
//    }
}
