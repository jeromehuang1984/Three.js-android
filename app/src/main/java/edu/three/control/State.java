package edu.three.control;

public enum State {
    NONE(-1), ROTATE(0), ZOOM(1), PAN(2), DOLLY_PAN(3), DOLLY_ROTATE(4);

    private final int val;
    private State(int value) {
        val = value;
    }
}
