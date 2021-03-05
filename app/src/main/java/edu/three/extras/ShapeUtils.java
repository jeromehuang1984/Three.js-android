package edu.three.extras;

import java.util.ArrayList;
import java.util.Arrays;

import edu.three.math.MathTool;
import edu.three.math.Vector2;

public class ShapeUtils {
  static public float area(Vector2[] contour) {
    int n = contour.length;
    float a = 0;
    for ( int p = n - 1, q = 0; q < n; p = q ++ ) {

      a += contour[ p ].x * contour[ q ].y - contour[ q ].x * contour[ p ].y;

    }

    return a * 0.5f;
  }

  static public boolean isClockWise(Vector2[] pts) {
    return area(pts) < 0;
  }

  static Vector2[] removeDupEndPts(Vector2[] points) {
    Vector2[] target = points;
    int l = points.length;
    if (l > 0 && points[l - 1].equals(points[0])) {
      target = Arrays.copyOf(points, points.length - 1);
    }
    return target;
  }

  static void addContour(ArrayList<Double> vertices, Vector2[] contour) {
    for ( int i = 0; i < contour.length; i ++ ) {

      vertices.add( contour[i].x );
      vertices.add( contour[i].y );

    }
  }

  static public int[][] triangulateShape(Vector2[] contour, Vector2[][] holes) {
    ArrayList<Double> vertices = new ArrayList<>();
    int[] holeIndices = new int[holes.length];

    contour = removeDupEndPts( contour );
    addContour( vertices, contour );

    int holeIndex = contour.length;
    for (Vector2[] list : holes) {
      removeDupEndPts(list);
    }
    for ( int i = 0; i < holes.length; i ++ ) {
      holeIndices[i] = holeIndex;
      Vector2[] arr = removeDupEndPts(holes[i]);
      holeIndex += arr.length;
      addContour( vertices, arr );

    }

    ArrayList<Integer> triangles = Earcut.triangulate(
        MathTool.toArrayDouble(vertices),
        holeIndices, null );

    int[] triangleArr = MathTool.toArrayInt(triangles);

    int[][] faces = new int[triangleArr.length/3][];
    int count = 0;
    for ( int i = 0; i < triangleArr.length; i += 3 ) {
      faces[count++] = MathTool.slice(triangleArr, i, i+3);
    }

    return faces;
  }
}
