package edu.three.buffergeometries;

import java.util.ArrayList;
import java.util.Arrays;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.extras.ShapeUtils;
import edu.three.extras.core.Curve;
import edu.three.extras.core.Shape;
import edu.three.math.MathTool;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class ExtrudeBufferGeometry extends BufferGeometry {
  static public class Param {
    public Param() {

    }
    public int curveSegments = 12;  // number of points on the curves
    public int steps = 1;  // number of points for z-side extrusions / used for subdividing segments of extrude spline too
    public double depth = 100;  // Depth to extrude the shape
    public boolean bevelEnabled = true; // turn on bevel
    public double bevelThickness = 6; // how deep into the original shape bevel goes
    public double bevelSize = bevelThickness - 2;  // how far from shape outline (including bevelOffset) is bevel
    public double bevelOffset = 0;  // how far from shape outline does bevel start
    public int bevelSegments = 3;  //number of bevel layers
    public Curve extrudePath;  // curve to extrude shape along
    //UVGenerator: <WorldUVGenerator> // object that provides UV generator functions
  }

  ArrayList<Shape> shapes;
  Param options;
  ArrayList<Float> verticesArray = new ArrayList<>();
  ArrayList<Float> uvArray = new ArrayList<>();
  public ExtrudeBufferGeometry init(ArrayList<Shape> shapes, Param options) {
    this.shapes = shapes;
    this.options = options;
    for (int i = 0, l = shapes.size(); i < l; i++) {
      Shape shape = shapes.get(i);
      addShape(shape);
    }
    // build geometry
    position = new BufferAttribute(MathTool.toArrayFloat(verticesArray), 3);
    uv = new BufferAttribute(MathTool.toArrayFloat(uvArray), 2);
    computeVertexNormals();
    return this;
  }

  public ExtrudeBufferGeometry init(Shape shape, Param options) {
    ArrayList<Shape> shapes = new ArrayList<>();
    shapes.add(shape);
    return init(shapes, options);
  }

  public ExtrudeBufferGeometry() {
  }

  Vector2 scalePt2(Vector2 pt, Vector2 vec, double size) {
    return vec.clone().multiplyScalar(size).add(pt);
  }

  // Find directions for point movement
  Vector2 getBevelVec(Vector2 inPt, Vector2 inPrev, Vector2 inNext) {
    // computes for inPt the corresponding point inPt' on a new contour
    //   shifted by 1 unit (length of normalized vector) to the left
    // if we walk along contour clockwise, this new contour is outside the old one
    //
    // inPt' is the intersection of the two lines parallel to the two
    //  adjacent edges of inPt at a distance of 1 unit on the left side.
    double v_trans_x, v_trans_y, shrink_by; // resulting translation vector for inPt
    // good reading for geometry algorithms (here: line-line intersection)
    // http://geomalgorithms.com/a05-_intersect-1.html
    double v_prev_x = inPt.x - inPrev.x,
        v_prev_y = inPt.y - inPrev.y;
    double v_next_x = inNext.x - inPt.x,
        v_next_y = inNext.y - inPt.y;

    double v_prev_lensq = (v_prev_x * v_prev_x + v_prev_y * v_prev_y);

    // check for collinear edges
    double collinear0 = (v_prev_x * v_next_y - v_prev_y * v_next_x);
    if (Math.abs(collinear0) > MathTool.EPSILON) {
      // not collinear

      // length of vectors for normalizing

      double v_prev_len = (double) Math.sqrt(v_prev_lensq);
      double v_next_len = (double) Math.sqrt(v_next_x * v_next_x + v_next_y * v_next_y);

      // shift adjacent points by unit vectors to the left

      double ptPrevShift_x = (inPrev.x - v_prev_y / v_prev_len);
      double ptPrevShift_y = (inPrev.y + v_prev_x / v_prev_len);

      double ptNextShift_x = (inNext.x - v_next_y / v_next_len);
      double ptNextShift_y = (inNext.y + v_next_x / v_next_len);

      // scaling factor for v_prev to intersection point
      double sf = ((ptNextShift_x - ptPrevShift_x) * v_next_y -
          (ptNextShift_y - ptPrevShift_y) * v_next_x) /
          (v_prev_x * v_next_y - v_prev_y * v_next_x);

      // vector from inPt to intersection point

      v_trans_x = (ptPrevShift_x + v_prev_x * sf - inPt.x);
      v_trans_y = (ptPrevShift_y + v_prev_y * sf - inPt.y);

      // Don't normalize!, otherwise sharp corners become ugly
      //  but prevent crazy spikes
      double v_trans_lensq = (v_trans_x * v_trans_x + v_trans_y * v_trans_y);
      if (v_trans_lensq <= 2) {

        return new Vector2(v_trans_x, v_trans_y);

      } else {

        shrink_by = (double) Math.sqrt(v_trans_lensq / 2);

      }
    } else {
      // handle special case of collinear edges

      boolean direction_eq = false; // assumes: opposite

      if (v_prev_x > MathTool.EPSILON) {

        if (v_next_x > MathTool.EPSILON) {

          direction_eq = true;

        }

      } else {

        if (v_prev_x < -MathTool.EPSILON) {

          if (v_next_x < -MathTool.EPSILON) {
            direction_eq = true;
          }

        } else {

          if (MathTool.sign(v_prev_y) == MathTool.sign(v_next_y)) {
            direction_eq = true;
          }

        }

      }

      if (direction_eq) {

        // console.log("Warning: lines are a straight sequence");
        v_trans_x = -v_prev_y;
        v_trans_y = v_prev_x;
        shrink_by = (double) Math.sqrt(v_prev_lensq);

      } else {

        // console.log("Warning: lines are a straight spike");
        v_trans_x = v_prev_x;
        v_trans_y = v_prev_y;
        shrink_by = (double) Math.sqrt(v_prev_lensq / 2);

      }

    }

    return new Vector2(v_trans_x / shrink_by, v_trans_y / shrink_by);
  }


  int vlen = 0, flen = 0;
  ArrayList<Float> placeholder = new ArrayList<>(); //addShape scope
  WorldUVGenerator uvgen = new WorldUVGenerator();

  void v(double x, double y, double z) {
    placeholder.add((float)x);
    placeholder.add((float)y);
    placeholder.add((float)z);
  }

  void f3(int a, int b, int c) {
    addVertex( a );
    addVertex( b );
    addVertex( c );

    int nextIndex = verticesArray.size() / 3;
    Vector2[] uvs = uvgen.generateTopUV( verticesArray, nextIndex - 3, nextIndex - 2, nextIndex - 1 );

    addUV( uvs[ 0 ] );
    addUV( uvs[ 1 ] );
    addUV( uvs[ 2 ] );
  }

  void f4(int a, int b, int c, int d) {
    addVertex( a );
    addVertex( b );
    addVertex( d );

    addVertex( b );
    addVertex( c );
    addVertex( d );


    int nextIndex = verticesArray.size() / 3;
    Vector2[] uvs = uvgen.generateSideWallUV(verticesArray, nextIndex - 6, nextIndex - 3, nextIndex - 2, nextIndex - 1 );

    addUV( uvs[ 0 ] );
    addUV( uvs[ 1 ] );
    addUV( uvs[ 3 ] );

    addUV( uvs[ 1 ] );
    addUV( uvs[ 2 ] );
    addUV( uvs[ 3 ] );
  }

  void addVertex(int index) {
    verticesArray.add( placeholder.get(index * 3) );
    verticesArray.add( placeholder.get(index * 3 + 1) );
    verticesArray.add( placeholder.get(index * 3 + 2) );
  }

  void addUV(Vector2 vector2) {
    uvArray.add((float)vector2.x);
    uvArray.add((float)vector2.y);
  }

  void sidewalls(int contourLen, int layeroffset) {
    int i = contourLen;
    while ( --i >= 0 ) {
      int j = i;
      int k = i - 1;
      if ( k < 0 ) k = contourLen - 1;

      //console.log('b', i,j, i-1, k,vertices.length);

      for ( int s = 0, sl = ( options.steps + options.bevelSegments * 2 ); s < sl; s ++ ) {

        int slen1 = vlen * s;
        int slen2 = vlen * ( s + 1 );

        int a = layeroffset + j + slen1,
            b = layeroffset + k + slen1,
            c = layeroffset + k + slen2,
            d = layeroffset + j + slen2;

        f4( a, b, c, d );
      }
    }
  }

  // Create faces for the z-sides of the shape
  void buildSideFaces(int contourLen, Vector2[][] holes) {
    int start = verticesArray.size() / 3;
    int layeroffset = 0;
    sidewalls(contourLen, layeroffset);
    layeroffset += contourLen;
    for ( int h = 0, hl = holes.length; h < hl; h ++ ) {

      Vector2[] ahole = holes[ h ];
      sidewalls( ahole.length, layeroffset );

      //, true
      layeroffset += ahole.length;

    }
    super.addGroup(start, verticesArray.size() / 3 - start, 1);
  }

  int[][] faces;
  void buildLidFaces() {
    int start = verticesArray.size() / 3;
    if (options.bevelEnabled) {
      int layer = 0; //steps + 1
      int offset = vlen * layer;
      // Bottom faces
      for (int i = 0; i < flen; i++) {
        int[] face = faces[i];
        f3( face[ 2 ] + offset, face[ 1 ] + offset, face[ 0 ] + offset );
      }
      layer = options.steps + options.bevelSegments * 2;
      offset = vlen * layer;

      // Top faces
      for (int i = 0; i < flen; i++) {
        int[] face = faces[i];
        f3( face[ 0 ] + offset, face[ 1 ] + offset, face[ 2 ] + offset );
      }
    } else {
      // Bottom faces
      for (int i = 0; i < flen; i++) {
        int[] face = faces[i];
        f3( face[ 2 ], face[ 1 ], face[ 0 ] );
      }
      // Top faces
      for (int i = 0; i < flen; i++) {
        int[] face = faces[i];
        f3( face[ 0 ] + vlen * options.steps, face[ 1 ] + vlen * options.steps, face[ 2 ] + vlen * options.steps );
      }
    }

    super.addGroup( start, verticesArray.size() / 3 - start, 0 );
  }

  void addShape(Shape shape) {
    placeholder = new ArrayList<>();
    Curve extrudePath = options.extrudePath;
    boolean extrudeByPath = false;
    Curve.FrenetItem splineTube = null;
    Vector3 binormal = new Vector3(), normal = new Vector3(), position2 = new Vector3();
    Vector3[] extrudePts = null;
    if (extrudePath != null) {
      extrudePts = extrudePath.getSpacedPoints(options.steps);
      extrudeByPath = true;
      options.bevelEnabled = false; //bevels not supported for path extrusion
      // SETUP TNB variables

      // TODO1 - have a .isClosed in spline?
      splineTube = extrudePath.computeFrenetFrames( options.steps, false );

      // console.log(splineTube, 'splineTube', splineTube.normals.length, 'steps', steps, 'extrudePts', extrudePts.length);
    }
    // Safeguards if bevels are not enabled
    if (!options.bevelEnabled) {
      options.bevelSegments = 0;
      options.bevelThickness = 0;
      options.bevelSize = 0;
      options.bevelOffset = 0;
    }
    // Variables initialization
    shape.extractPoints(options.curveSegments);
    Vector2[] vertices = shape.outline;
    Vector2[][] holes = shape.holeVertices;

    boolean reverse = !ShapeUtils.isClockWise(vertices);
    if (reverse) {
      vertices = MathTool.reverseArr(vertices);
      // Maybe we should also check if holes are in the opposite direction, just to be safe ...
      for (int h = 0, hl = holes.length; h < hl; h++) {
        Vector2[] ahole = holes[h];
        if (ShapeUtils.isClockWise(ahole)) {
          holes[h] = MathTool.reverseArr(ahole);
        }
      }
    }
    faces = ShapeUtils.triangulateShape(vertices, holes);
    /* Vertices */
    Vector2[] contour = vertices; // vertices has all points but contour has only points of circumference

    for ( int h = 0, hl = holes.length; h < hl; h ++ ) {
      Vector2[] ahole = holes[ h ];
      vertices = MathTool.concat(vertices, ahole);
    }

    vlen = vertices.length;
    flen = faces.length;
    Vector2[] contourMovements = new Vector2[contour.length];
    for ( int i = 0, il = contour.length, j = il - 1, k = i + 1; i < il; i ++, j ++, k ++ ) {

      if ( j == il ) j = 0;
      if ( k == il ) k = 0;

      //  (j)---(i)---(k)
      // Log.d('i,j,k', i, j , k)

      contourMovements[ i ] = getBevelVec( contour[ i ], contour[ j ], contour[ k ] );
    }

    Vector2[][] holesMovements = new Vector2[holes.length][];
    Vector2[] oneHoleMovements, verticesMovements = Arrays.copyOf(contourMovements, contourMovements.length);
    for ( int h = 0, hl = holes.length; h < hl; h ++ ) {
      Vector2[] ahole = holes[h];
      oneHoleMovements = new Vector2[ahole.length];
      for ( int i = 0, il = ahole.length, j = il - 1, k = i + 1; i < il; i ++, j ++, k ++ ) {

        if ( j == il ) j = 0;
        if ( k == il ) k = 0;

        //  (j)---(i)---(k)
        oneHoleMovements[ i ] = getBevelVec( ahole[ i ], ahole[ j ], ahole[ k ] );

      }
      holesMovements[h] = oneHoleMovements;
      verticesMovements = MathTool.concat(verticesMovements, oneHoleMovements);
    }

    // Loop bevelSegments, 1 for the front, 1 for the back
    for ( int b = 0; b < options.bevelSegments; b ++ ) {
      double t = (double) b / options.bevelSegments;
      double z = options.bevelThickness * (double) Math.cos( t * Math.PI / 2 );
      double bs = options.bevelSize * (double)Math.sin( t * Math.PI / 2 ) + options.bevelOffset;

      // contract shape
      for ( int i = 0, il = contour.length; i < il; i ++ ) {
        Vector2 vert = scalePt2( contour[ i ], contourMovements[ i ], bs );
        v( vert.x, vert.y, - z );
      }

      // expand holes
      for ( int h = 0, hl = holes.length; h < hl; h ++ ) {

        Vector2[] ahole = holes[ h ];
        oneHoleMovements = holesMovements[ h ];

        for ( int i = 0, il = ahole.length; i < il; i ++ ) {
          Vector2 vert = scalePt2( ahole[ i ], oneHoleMovements[ i ], bs );
          v( vert.x, vert.y, - z );
        }

      }
    }
    double bs = options.bevelSize + options.bevelOffset;
    // Back facing vertices
    for (int i = 0; i < vlen; i++) {
      Vector2 vert = options.bevelEnabled ? scalePt2( vertices[ i ], verticesMovements[ i ], bs ) : vertices[ i ];

      if ( ! extrudeByPath ) {
        v( vert.x, vert.y, 0 );
      } else {
        // v( vert.x, vert.y + extrudePts[ 0 ].y, extrudePts[ 0 ].x );

        normal.copy( splineTube.normals[ 0 ] ).multiplyScalar( vert.x );
        binormal.copy( splineTube.binormals[ 0 ] ).multiplyScalar( vert.y );

        position2.copy( extrudePts[ 0 ] ).add( normal ).add( binormal );

        v( position2.x, position2.y, position2.z );

      }
    }

    // Add stepped vertices...
    // Including front facing vertices
    for ( int s = 1; s <= options.steps; s ++ ) {

      for ( int i = 0; i < vlen; i ++ ) {

        Vector2 vert = options.bevelEnabled ? scalePt2( vertices[ i ], verticesMovements[ i ], bs ) : vertices[ i ];

        if ( ! extrudeByPath ) {

          v( vert.x, vert.y, options.depth / options.steps * s );

        } else {

          // v( vert.x, vert.y + extrudePts[ s - 1 ].y, extrudePts[ s - 1 ].x );

          normal.copy( splineTube.normals[ s ] ).multiplyScalar( vert.x );
          binormal.copy( splineTube.binormals[ s ] ).multiplyScalar( vert.y );

          position2.copy( extrudePts[ s ] ).add( normal ).add( binormal );

          v( position2.x, position2.y, position2.z );

        }

      }

    }
    // Add bevel segments planes
    for ( int b = options.bevelSegments - 1; b >= 0; b -- ) {

      double t = (double)b / options.bevelSegments;
      double z = options.bevelThickness * (double)Math.cos( t * Math.PI / 2 );
      bs = options.bevelSize * (double)Math.sin( t * Math.PI / 2 ) + options.bevelOffset;

      // contract shape

      for ( int i = 0, il = contour.length; i < il; i ++ ) {
        Vector2 vert = scalePt2( contour[ i ], contourMovements[ i ], bs );
        v( vert.x, vert.y, options.depth + z );
      }

      // expand holes

      for ( int h = 0, hl = holes.length; h < hl; h ++ ) {
        Vector2[] ahole = holes[ h ];
        oneHoleMovements = holesMovements[ h ];
        for ( int i = 0, il = ahole.length; i < il; i ++ ) {

          Vector2 vert = scalePt2( ahole[ i ], oneHoleMovements[ i ], bs );

          if ( ! extrudeByPath ) {

            v( vert.x, vert.y, options.depth + z );

          } else {

            v( vert.x, vert.y + extrudePts[ options.steps - 1 ].y, extrudePts[ options.steps - 1 ].x + z );

          }

        }

      }

    }
    /* Faces */
    // Top and bottom faces
    buildLidFaces();
    // Sides faces
    buildSideFaces(contour.length, holes);
  }

  class WorldUVGenerator {
    Vector2[] generateTopUV(ArrayList<Float> vertices, int indexA, int indexB, int indexC) {
      double a_x = vertices.get(indexA * 3);
      double a_y = vertices.get( indexA * 3 + 1 );
      double b_x = vertices.get( indexB * 3 );
      double b_y = vertices.get( indexB * 3 + 1 );
      double c_x = vertices.get( indexC * 3 );
      double c_y = vertices.get( indexC * 3 + 1 );
      return  new Vector2[] {
          new Vector2( a_x, a_y ),
          new Vector2( b_x, b_y ),
          new Vector2( c_x, c_y )
      };
    }

    Vector2[] generateSideWallUV(ArrayList<Float> vertices, int indexA, int indexB, int indexC, int indexD) {
      double a_x = vertices.get( indexA * 3 );
      double a_y = vertices.get( indexA * 3 + 1 );
      double a_z = vertices.get( indexA * 3 + 2 );
      double b_x = vertices.get( indexB * 3 );
      double b_y = vertices.get( indexB * 3 + 1 );
      double b_z = vertices.get( indexB * 3 + 2 );
      double c_x = vertices.get( indexC * 3 );
      double c_y = vertices.get( indexC * 3 + 1 );
      double c_z = vertices.get( indexC * 3 + 2 );
      double d_x = vertices.get( indexD * 3 );
      double d_y = vertices.get( indexD * 3 + 1 );
      double d_z = vertices.get( indexD * 3 + 2 );

      if (Math.abs(a_y - b_y) < 0.01) {
        return new Vector2[] {
            new Vector2( a_x, 1 - a_z ),
            new Vector2( b_x, 1 - b_z ),
            new Vector2( c_x, 1 - c_z ),
            new Vector2( d_x, 1 - d_z )
        };
      } else {
        return new Vector2[] {
            new Vector2( a_y, 1 - a_z ),
            new Vector2( b_y, 1 - b_z ),
            new Vector2( c_y, 1 - c_z ),
            new Vector2( d_y, 1 - d_z )
        };
      }
    }
  }
}
