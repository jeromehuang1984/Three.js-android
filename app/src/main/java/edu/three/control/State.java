package edu.three.control;

public enum State {
    NONE(-1), ROTATE(0), ZOOM(1), PAN(2);

    private final int val;
    private State(int value) {
        val = value;
    }
}
