package edu.three.extras.core;

import android.util.Log;

import edu.three.math.MathTool;
import edu.three.math.Matrix4;
import edu.three.math.Vector3;

public class Curve {
  String TAG = getClass().getSimpleName();
  public int arcLengthDivisions = 200;
  double[] cacheArcLengths;
  boolean needsUpdate = false;

  public Vector3 getPoint(double t, Vector3 optionalTarget) {
    Log.d(TAG, "THREE.Curve: .getPoint() not implemented.");
    return null;
  }

  public Vector3 getPoint(double t) {
    return getPoint(t, null);
  }

  // Get point at relative position in curve according to arc length
  // - u [0 .. 1]
  public Vector3 getPointAt(double u, Vector3 optionalTarget) {
    double t = getUtoTmapping(u, 0);
    return getPoint(t, optionalTarget);
  }

  // Get sequence of points using getPoint( t )
  public Vector3[] getPoints(int divisions) {
    Vector3[] points = new Vector3[divisions + 1];
    for (int d = 0; d <= divisions; d++) {
      points[d] = getPoint((double)d / divisions);
    }
    return points;
  }
  // Get sequence of points using getPointAt( t )
  public Vector3[] getSpacedPoints(int divisions) {
    Vector3[] points = new Vector3[divisions + 1];
    for (int d = 0; d <= divisions; d++) {
      points[d] = getPointAt((double)d / divisions, null);
    }
    return points;
  }
  // Get total curve arc length
  public double getLength() {
    double[] lengths = getLengths(0);
    return lengths[lengths.length - 1];
  }

  // Get list of cumulative segment lengths
  public double[] getLengths(int divisions) {
    if (divisions <= 0)
      divisions = arcLengthDivisions;
    if (cacheArcLengths != null && cacheArcLengths.length == divisions + 1
    && needsUpdate) {
      return cacheArcLengths;
    }
    needsUpdate = false;
    double[] cache = new double[divisions+1];
    Vector3 current, last = getPoint(0);
    double sum = 0;
    cache[0] = 0;

    for (int p = 1; p <= divisions; p++) {
      current = getPoint(p/divisions);
      sum += current.distanceTo(last);
      cache[p] = sum;
      last = current;
    }
    cacheArcLengths = cache;
    return cache;
  }

  public void updateArcLengths() {
    needsUpdate = true;
    getLengths(0);
  }

// Given u ( 0 .. 1 ), get a t to find p. This gives you points which are equidistant
  double getUtoTmapping(double u, double distance) {
    double[] arcLengths = getLengths(0);
    int i = 0;
    int il = arcLengths.length;
    double targetArcLength; //The targeted u distance value to get
    if (distance > 0) {
      targetArcLength = distance;
    } else {
      targetArcLength = u * arcLengths[il - 1];
    }
    // binary search for the index with largest value smaller than target u distance
    int low = 0, high = il - 1;
    double comparison;
    while (low <= high) {
      i = (int) Math.floor(low + (high - low) / 2);// less likely to overflow, though probably not issue here,
      comparison = arcLengths[i] - targetArcLength;
      if ( comparison < 0 ) {
        low = i + 1;
      } else if ( comparison > 0 ) {
        high = i - 1;
      } else {
        high = i;
        break;
        // DONE
      }
    }
    i = high;
    if (arcLengths[i] == targetArcLength) {
      return (double)i / (il - 1);
    }

    // we could get finer grain at lengths, or use simple interpolation between two points
    double lengthBefore = arcLengths[i];
    double lengthAfter = arcLengths[i + 1];
    double segmentLength = lengthAfter - lengthBefore;

    // determine where we are between the 'before' and 'after' points
    double segmentFraction = (targetArcLength - lengthBefore) / segmentLength;
    // add that fractional amount to t
    double t = (i + segmentFraction) / (il - 1);
    return t;
  }

  // Returns a unit vector tangent at t
  // In case any sub curve does not implement its tangent derivation,
  // 2 points a small delta apart will be used to find its gradient
  // which seems to give a reasonable approximation
  public Vector3 getTangent(double t, Vector3 optionalTarget) {
    double delta = 0.0001f;
    double t1 = t - delta;
    double t2 = t + delta;

    // Capping in case of danger
    if (t1 < 0) t1 = 0;
    if (t2 > 1) t2 = 1;
    Vector3 pt1 = getPoint(t1);
    Vector3 pt2 = getPoint(t2);
    Vector3 tangent = optionalTarget != null ? optionalTarget : new Vector3();
    tangent.copy(pt2).sub(pt1).normalize();
    return tangent;
  }

  public Vector3 getTangentAt(double u, Vector3 optionalTarget) {
    double t = getUtoTmapping(u, 0);
    return getTangent(t, optionalTarget);
  }

  // see http://www.cs.indiana.edu/pub/techreports/TR425.pdf
  public FrenetItem computeFrenetFrames(int segments, boolean closed) {
    Vector3 normal = new Vector3();
    Vector3[] tangents = new Vector3[segments + 1];
    Vector3[] normals = new Vector3[segments + 1];
    Vector3[] binormals = new Vector3[segments + 1];

    Vector3 vec = new Vector3();
    Matrix4 mat = new Matrix4();
    // compute the tangent vectors for each segment on the curve
    for (int i = 0; i <= segments; i++) {
      double u = (double) i / segments;
      tangents[i] = getTangentAt(u, new Vector3());
      tangents[i].normalize();
    }

    // select an initial normal vector perpendicular to the first tangent vector,
    // and in the direction of the minimum tangent xyz component
    normals[0] = new Vector3();
    binormals[0] = new Vector3();
    double min = Float.MAX_VALUE;
    double tx = Math.abs(tangents[0].x);
    double ty = Math.abs(tangents[0].y);
    double tz = Math.abs(tangents[0].z);

    if (tx <= min) {
      min = tx;
      normal.set(1, 0, 0);
    }
    if (ty <= min) {
      min = ty;
      normal.set(0, 1, 0);
    }
    if (tz <= min) {
      normal.set(0, 0, 1);
    }

    vec.crossVectors(tangents[0], normal).normalize();
    normals[ 0 ].crossVectors( tangents[ 0 ], vec );
    binormals[ 0 ].crossVectors( tangents[ 0 ], normals[ 0 ] );
    // compute the slowly-varying normal and binormal vectors for each segment on the curve
    for (int i = 1; i <= segments; i++) {
      normals[ i ] = normals[ i - 1 ].clone();

      binormals[ i ] = binormals[ i - 1 ].clone();

      vec.crossVectors( tangents[ i - 1 ], tangents[ i ] );

      if ( vec.length() > MathTool.EPSILON ) {

        vec.normalize();

				double theta = (double)Math.acos( MathTool.clamp( tangents[ i - 1 ].dot( tangents[ i ] ), - 1, 1 ) ); // clamp for doubleing pt errors

        normals[ i ].applyMatrix4( mat.makeRotationAxis( vec, theta ) );

      }

      binormals[ i ].crossVectors( tangents[ i ], normals[ i ] );

    }
    // if the curve is closed, postprocess the vectors so the first and last normal vectors are the same
    if (closed) {
      double theta = (double) Math.acos(MathTool.clamp(normals[0].dot(normals[segments]), -1, 1));
      theta /= segments;

      if (tangents[0].dot(vec.crossVectors(normals[0], normals[segments])) > 0) {
        theta = -theta;
      }
      for (int i = 1; i <= segments; i++) {
        // twist a little...
        normals[i].applyMatrix4(mat.makeRotationAxis(tangents[i], theta * i));
        binormals[i].crossVectors(tangents[i], normals[i]);
      }
    }
    return new FrenetItem(tangents, normals, binormals);
  }

  public class FrenetItem {
    public FrenetItem(Vector3[] tangents, Vector3[] normals, Vector3[] binormals) {
      this.tangents = tangents;
      this.normals = normals;
      this.binormals = binormals;
    }

    public Vector3[] tangents, normals, binormals;
  }

  public Curve copy(Curve source) {
    arcLengthDivisions = source.arcLengthDivisions;
    return this;
  }

  public Curve clone() {
    return new Curve().copy(this);
  }
}
