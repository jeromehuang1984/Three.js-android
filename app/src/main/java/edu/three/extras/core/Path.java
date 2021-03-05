package edu.three.extras.core;

import edu.three.math.Vector3;

public class Path extends CurvePath {

  Vector3 currentPoint;

  public Path(Vector3[] points) {
    currentPoint = new Vector3();
    setFromPoints(points);
  }

  public Path setFromPoints(Vector3[] points) {
    moveTo( (float)points[ 0 ].x, (float)points[ 0 ].y );

    for ( int i = 1, l = points.length; i < l; i ++ ) {
      lineTo( (float)points[ i ].x, (float)points[ i ].y );
    }

    return this;
  }

  Path moveTo(float x, float y) {
    currentPoint.set(x, y, 0);
    return this;
  }

  Path lineTo(float x, float y) {
    LineCurve3 curve = new LineCurve3(currentPoint.clone(), new Vector3(x, y, 0));
    curves.add(curve);
    currentPoint.set(x, y, 0);
    return this;
  }
}
