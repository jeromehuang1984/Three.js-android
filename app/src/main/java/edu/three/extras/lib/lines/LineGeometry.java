package edu.three.extras.lib.lines;

public class LineGeometry extends LineSegmentsGeometry {

  public LineGeometry() {
    super();
  }
  public LineGeometry setPositions(float[] array) {
    // converts [ x1, y1, z1,  x2, y2, z2, ... ] to pairs format
    int length = array.length - 3;
    float[] points = new float[2*length];
    for ( int i = 0; i < length; i += 3 ) {

      points[ 2 * i ] = array[ i ];
      points[ 2 * i + 1 ] = array[ i + 1 ];
      points[ 2 * i + 2 ] = array[ i + 2 ];

      points[ 2 * i + 3 ] = array[ i + 3 ];
      points[ 2 * i + 4 ] = array[ i + 4 ];
      points[ 2 * i + 5 ] = array[ i + 5 ];

    }
    super.setPositions(points);

    return this;
  }

  public LineGeometry setColors(float[] array) {
    // converts [ r1, g1, b1,  r2, g2, b2, ... ] to pairs format

    int length = array.length - 3;
    float[] colors = new float[2*length];

    for ( int i = 0; i < length; i += 3 ) {

      colors[ 2 * i ] = array[ i ];
      colors[ 2 * i + 1 ] = array[ i + 1 ];
      colors[ 2 * i + 2 ] = array[ i + 2 ];

      colors[ 2 * i + 3 ] = array[ i + 3 ];
      colors[ 2 * i + 4 ] = array[ i + 4 ];
      colors[ 2 * i + 5 ] = array[ i + 5 ];

    }
    super.setColors(array);

    return this;
  }
}
