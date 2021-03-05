package edu.three.math;

public class Line3 {
    public Vector3 start = new Vector3();
    public Vector3 end = new Vector3();
    Vector3 startP = new Vector3(), startEnd = new Vector3();

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

    public double closestPointToPointParameter(Vector3 point, boolean clampToLine) {
        startP.subVectors(point, start);
        startEnd.subVectors(end, start);
        double startEnd2 = startEnd.dot(startEnd);
        double startEnd_startP = startEnd.dot(startP);
        double t = startEnd_startP / startEnd2;
        if (clampToLine) {
            t = MathTool.clamp(t, 0, 1);
        }
        return t;
    }

    public Vector3 closestPointToPoint(Vector3 point, boolean clampToLine, Vector3 target) {
        double t = closestPointToPointParameter(point, clampToLine);
        if (target == null) {
            target = new Vector3();
        }
        return delta(target).multiplyScalar(t).add(start);
    }
}
