package edu.three.materials;

import edu.three.math.Color;

public class MeshBasicMaterial extends Material {

//    public boolean wireframe = false;
//    public int wireframeLinewidth = 1;
    public String wireframeLinecap = "round";
    public String wireframeLinejoin = "round";

    public MeshBasicMaterial(int color) {
        this.color = new Color(color);
        lights = false;
        wireframe = false;
        wireframeLinewidth = 1;
    }

    public MeshBasicMaterial(int color, float opacity) {
        this(color);
        this.transparent = true;
        this.opacity = opacity;
    }

    public MeshBasicMaterial() {
        this(0xffccff);
    }

    public MeshBasicMaterial setWireFrame(boolean val) {
        wireframe = val;
        return this;
    }

    public MeshBasicMaterial setColor(int val) {
        color = new Color(val);
        return this;
    }

    public MeshBasicMaterial setSide(int val) {
        side = val;
        return this;
    }
}
