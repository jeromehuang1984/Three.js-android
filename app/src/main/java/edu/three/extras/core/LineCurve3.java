package edu.three.extras.core;

import edu.three.math.Vector3;

public class LineCurve3 extends Curve {
  public Vector3 v1;
  public Vector3 v2;

  public LineCurve3(Vector3 v1, Vector3 v2) {
    this.v1 = v1;
    this.v2 = v2;
  }

  public Vector3 getPoint(double t, Vector3 optionalTarget) {
    Vector3 point = optionalTarget == null ? new Vector3() : optionalTarget;
    if (t == 1) {
      point.copy(v2);
    } else {
      point.copy(v2).sub(v1);
      point.multiplyScalar(t).add(v1);
    }
    return point;
  }

  // Line curve is linear, so we can overwrite default getPointAt
  public Vector3 getPointAt(double u, Vector3 optionalTarget) {
    return getPoint(u, optionalTarget);
  }

  public Curve copy(Curve source) {
    LineCurve3 other = (LineCurve3) source;
    v1.copy(other.v1);
    v2.copy(other.v2);
    return super.copy(source);
  }
}
