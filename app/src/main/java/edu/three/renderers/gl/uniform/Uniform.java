package edu.three.renderers.gl.uniform;

import edu.three.renderers.gl.GLTextures;

public class Uniform {
    public String id;
    protected int addr;
    protected int size;
    protected int type;

    public Uniform() {}

    public Uniform(int addr, String id, int size, int type) {
        this.addr = addr;
        this.id = id;
        this.size = size;
        this.type = type;
    }

    public void setValue(Object value, GLTextures textures) {
    }

    public String toString() {
        return id;
    }
}
