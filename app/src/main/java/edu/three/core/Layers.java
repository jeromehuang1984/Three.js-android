package edu.three.core;

public class Layers {
    public int mask = 1 | 0;

    public void set(int channel) {
        mask = 1 << channel | 0;
    }

    public void enable(int channel) {
        mask |= 1 << channel | 0;
    }

    public void toggle(int channel) {
        mask ^= 1 << channel | 0;
    }

    public void disable(int channel) {
        mask &= ~(1 << channel | 0);
    }

    public boolean test(Layers layers) {
        return (mask & layers.mask) != 0;
    }
}
