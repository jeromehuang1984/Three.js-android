package edu.three.extras.core;

import java.util.ArrayList;

import edu.three.math.Vector3;

public class Path3 extends CurvePath {
  Vector3 currentPoint;

  public Path3(ArrayList<Vector3> points) {
    currentPoint = new Vector3();
    if (points != null) {
      setFromPoints(points);
    }
  }

  void moveTo(double x, double y, double z) {
    currentPoint.set(x, y, z);
  }

  void lineTo(double x, double y, double z) {
    LineCurve3 curve = new LineCurve3(currentPoint.clone(), new Vector3(x, y,z ));
    curves.add(curve);
    currentPoint.set(x, y, z);
  }

  void setFromPoints(ArrayList<Vector3> points) {
    moveTo(points.get(0).x, points.get(0).y, points.get(0).z);
    for (int i = 1, l = points.size(); i < l; i++) {
      lineTo(points.get(i).x, points.get(i).y, points.get(i).z);
    }
  }
}
