package edu.three.math;

public class Line3 {
    public Vector3 start = new Vector3();
    public Vector3 end = new Vector3();

    public Line3(Vector3 start, Vector3 end) {
        this.start = start;
        this.end = end;
    }
    public Vector3 delta(Vector3 target) {
        if (target == null) {
            target = new Vector3();
        }
        return target.subVectors(end, start);
    }
}
