package edu.three.buffergeometries;


import java.util.ArrayList;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.extras.core.Curve;
import edu.three.math.MathTool;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class TubeBufferGeometry extends BufferGeometry {
  Curve path;
  // expose internals
  public int tubularSegments;
  public double radius;
  public int radialSegments;
  public boolean closed;

  Curve.FrenetItem frames;
  public Vector3[] tangents, normals, binormals;

  // helper variables
  Vector3 vertex = new Vector3();
  Vector3 normal = new Vector3();
  Vector3 P = new Vector3();
  Vector2 uv = new Vector2();

  // buffer
  ArrayList<Float> vertices = new ArrayList<>();
  ArrayList<Float> normalArr = new ArrayList<>();
  ArrayList<Float> uvs = new ArrayList<>();
  ArrayList<Integer> indices = new ArrayList<>();

  public TubeBufferGeometry(Curve path) {
    this(path, 64, 1, 8, false);
  }
  public TubeBufferGeometry(Curve path, int tubularSegments, double radius, int radialSegments, boolean closed) {
    this.path = path;
    this.tubularSegments = tubularSegments;
    this.radius = radius;
    this.radialSegments = radialSegments;
    this.closed = closed;

    frames = path.computeFrenetFrames(tubularSegments, closed);
    // expose internals
    tangents = frames.tangents;
    normals = frames.normals;
    binormals = frames.binormals;

    // create buffer data
    generateBufferData();

    // build geometry
    setIndex(new BufferAttribute().setArrayIntList(indices ) );
    position = new BufferAttribute().setArrayList(vertices).setItemSize(3);
    super.normal = new BufferAttribute().setArrayList(normalArr).setItemSize(3);
    super.uv = new BufferAttribute().setArrayList(uvs).setItemSize(2);
  }

  void generateBufferData() {
    for ( int i = 0; i < tubularSegments; i ++ ) {

      generateSegment( i );

    }

    // if the geometry is not closed, generate the last row of vertices and normals
    // at the regular position on the given path
    //
    // if the geometry is closed, duplicate the first row of vertices and normals (uvs will differ)

    generateSegment( ( closed == false ) ? tubularSegments : 0 );

    // uvs are generated in a separate function.
    // this makes it easy compute correct values for closed geometries

    generateUVs();

    // finally create faces

    generateIndices();
  }

  void generateSegment(int i) {
    // we use getPointAt to sample evenly distributed points from the given path

    P = path.getPointAt( (double)i / tubularSegments, P );

    // retrieve corresponding normal and binormal

    Vector3 N = frames.normals[ i ];
    Vector3 B = frames.binormals[ i ];

    // generate normals and vertices for the current segment

    for ( int j = 0; j <= radialSegments; j ++ ) {

				double v = (double)j / radialSegments * MathTool.PI * 2;

      double sin = Math.sin( v );
      double cos = - Math.cos( v );

      // normal
      normal.x = ( cos * N.x + sin * B.x );
      normal.y = ( cos * N.y + sin * B.y );
      normal.z = ( cos * N.z + sin * B.z );
      normal.normalize();

      MathTool.push(normalArr, normal.x, normal.y, normal.z);

      // vertex

      vertex.x = P.x + radius * normal.x;
      vertex.y = P.y + radius * normal.y;
      vertex.z = P.z + radius * normal.z;

      MathTool.push(vertices, vertex.x, vertex.y, vertex.z);

    }
  }

  void generateIndices() {
    for ( int j = 1; j <= tubularSegments; j ++ ) {

      for ( int i = 1; i <= radialSegments; i ++ ) {
        int a = ( radialSegments + 1 ) * ( j - 1 ) + ( i - 1 );
        int b = ( radialSegments + 1 ) * j + ( i - 1 );
        int c = ( radialSegments + 1 ) * j + i;
        int d = ( radialSegments + 1 ) * ( j - 1 ) + i;

        // faces
        MathTool.push(indices, a, b, d);
        MathTool.push(indices, b, c, d);
      }
    }
  }

  void generateUVs() {
    for ( int i = 0; i <= tubularSegments; i ++ ) {

      for ( int j = 0; j <= radialSegments; j ++ ) {

        uv.x = (double)i / tubularSegments;
        uv.y = (double)j / radialSegments;

        MathTool.push(uvs, uv.x, uv.y);
      }
    }
  }
}
