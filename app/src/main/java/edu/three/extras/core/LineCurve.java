package edu.three.extras.core;

import edu.three.math.Vector2;

public class LineCurve extends Curve {
  Vector2 v1, v2;

  public LineCurve(Vector2 v1, Vector2 v2) {
    this.v1 = v1;
    this.v2 = v2;
  }

  public Vector2 getPoint2(double t) {
    return getPoint2(t, null);
  }
  public Vector2 getPoint2(double t, Vector2 optionalTarget) {
    Vector2 point = optionalTarget == null ? new Vector2() : optionalTarget;
    if ( t == 1 ) {

      point.copy( this.v2 );

    } else {

      point.copy( this.v2 ).sub( this.v1 );
      point.multiplyScalar( t ).add( this.v1 );

    }

    return point;
  }

  // Line curve is linear, so we can overwrite default getPointAt
  public Vector2 getPointAt(double u, Vector2 optionalTarget) {
    return getPoint2(u, optionalTarget);
  }

  public Vector2 getTangent(double t, Vector2 optionalTarget) {
    Vector2 tangent = optionalTarget == null ? new Vector2() : optionalTarget;
    tangent.copy( this.v2 ).sub( this.v1 ).normalize();
    return tangent;
  }

  public LineCurve copy(LineCurve source) {
    super.copy(source);
    v1.copy(source.v1);
    v2.copy(source.v2);
    return this;
  }
}
