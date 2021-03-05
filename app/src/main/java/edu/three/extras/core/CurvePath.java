package edu.three.extras.core;

import java.util.ArrayList;

import edu.three.math.MathTool;
import edu.three.math.Vector3;

/**************************************************************
 *	Curved Path - a curve path is simply a array of connected
 *  curves, but retains the api of a curve
 **************************************************************/
public class CurvePath extends Curve {
  double[] cacheLengths;
  ArrayList<Curve> curves = new ArrayList<>();
  boolean autoClose = false; // Automatically closes the path

  public void add(Curve curve) {
    curves.add(curve);
  }

  public void closePath() {
    // Add a line curve if start and end of lines are not connected
    Vector3 startPoint = curves.get(0).getPoint( 0 );
    Vector3 endPoint = curves.get(this.curves.size() - 1).getPoint( 1 );

    if ( ! startPoint.equals( endPoint ) ) {
      curves.add( new LineCurve3( endPoint, startPoint ) );
    }
  }

  // To get accurate point with reference to
  // entire path distance at time t,
  // following has to be done:

  // 1. Length of each sub path have to be known
  // 2. Locate and identify type of curve
  // 3. Get t for the curve
  // 4. Return curve.getPointAt(t')
  public Vector3 getPoint(double t, Vector3 optionalTarget) {
    double d = t * getLength();
    double[] curveLengths = getCurveLengths();
    int i = 0;
    // To think about boundaries points.
    while (i < curveLengths.length) {
      if ( curveLengths[ i ] >= d ) {

				double diff = curveLengths[ i ] - d;
				Curve curve = this.curves.get(i);

				double segmentLength = curve.getLength();
        double u = segmentLength == 0 ? 0 : 1 - diff / segmentLength;

        return curve.getPointAt( u, null);

      }

      i ++;
    }
    return null;
    // loop where sum != 0, sum > d , sum+1 <d
  }

  // Get sequence of points using getPoint( t )
  public Vector3[] getPoints(int divisions) {
    ArrayList<Vector3> points = new ArrayList<>();
    Vector3 last = null;
    for (int i = 0; i < curves.size(); i++) {
      Curve curve = curves.get(i);
      int resolution = divisions;
      if (curve instanceof LineCurve || curve instanceof LineCurve3) {
        resolution = 1;
      }
      Vector3[] pts = curve.getPoints(resolution);
      for (int j = 0; j < pts.length; j++) {
        Vector3 point = pts[j];
        if (last != null && last.equals(point))
          continue;
        points.add(point);
        last = point;
      }
    }
    return MathTool.toArrayVector(points);
  }


  // We cannot use the default THREE.Curve getPoint() with getLength() because in
  // THREE.Curve, getLength() depends on getPoint() but in THREE.CurvePath
  // getPoint() depends on getLength
  public double getLength() {

		double[] lens = getCurveLengths();
    return lens[ lens.length - 1 ];
  }

  public void updateArcLengths() {
    needsUpdate = true;
    cacheLengths = null;
    getCurveLengths();
  }
  // Compute lengths and cache them
  // We cannot overwrite getLengths() because UtoT mapping uses it.
  double[] getCurveLengths() {
    // We use cache values if curves and cache array are same length
    if (cacheLengths != null && cacheLengths.length == curves.size()) {
      return cacheLengths;
    }

    // Get length of sub-curve
    // Push sums into cached array
    double[] lengths = new double[curves.size()];
    double sums = 0;
    for (int i = 0, l = curves.size(); i < l; i++) {
      sums += curves.get(i).getLength();
      lengths[i] = sums;
    }
    cacheLengths = lengths;
    return lengths;
  }

  public Vector3[] getSpacedPoints(int divisions) {
    Vector3[] points = autoClose ? new Vector3[divisions+2] : new Vector3[divisions+1];
    int i = 0;
    for (; i <= divisions; i++) {
      points[i] = getPoint((double) i / divisions);
    }
    if (autoClose) {
      points[i] = points[0];
    }
    return points;
  }

  public Curve copy(Curve source) {
    super.copy(source);
    CurvePath other = (CurvePath) source;
    curves = new ArrayList<>();
    for (int i = 0, l = other.curves.size(); i <l; i++) {
      Curve curve = other.curves.get(i);
      curves.add(curve.clone());
    }
    autoClose = ((CurvePath) source).autoClose;
    return this;
  }
}
