package edu.three.extras.core;

import java.util.ArrayList;

import edu.three.math.MathTool;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class Shape extends Path {
  public ArrayList<Curve> holes = new ArrayList<>();
  public Vector2[] outline;
  public Vector2[][] holeVertices;

  public Shape(Vector3[] points) {
    super(points);
  }

  public Vector2[][] getPointsHoles(int divisions) {
    Vector2[][] holesPts = new Vector2[holes.size()][];
    for ( int i = 0, l = holes.size(); i < l; i ++ ) {
      Vector3[] vector3s = holes.get(i).getPoints( divisions );
      holesPts[ i ] = MathTool.vector3s_vector2s(vector3s);
    }
    return holesPts;
  }

  public Shape extractPoints(int divisions) {
    outline = MathTool.vector3s_vector2s(getPoints(divisions));
    holeVertices = getPointsHoles(divisions);
    return this;
  }

  public Shape copy(Shape source) {
    super.copy(source);
    holes.clear();
    for ( int i = 0, l = source.holes.size(); i < l; i ++ ) {
			Curve hole = source.holes.get(i);
      this.holes.add( hole.clone() );
    }

    return this;
  }
}
