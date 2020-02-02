package edu.three.buffergeometries;

import java.util.ArrayList;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.math.MathTool;

import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class PolyhedronBufferGeometry extends BufferGeometry {
    float radius = 1;
    int detail = 0;
    float[] vertices;
    int[] indices;
    ArrayList<Float> vertexBuffer = new ArrayList<>();
    ArrayList<Float> uvBuffer = new ArrayList<>();

    public PolyhedronBufferGeometry() {

    }
    public PolyhedronBufferGeometry set(float[] vertices, int[] indices, Float radius, Integer detail) {
        if (radius != null) {
            this.radius = radius;
        }
        if (detail != null) {
            this.detail = detail;
        }
        this.vertices = vertices;
        this.indices = indices;
        // the subdivision creates the vertex buffer data

        subdivide( detail );

        // all vertices should lie on a conceptual sphere with a given radius

        appplyRadius( radius );

        // finally, create the uv data

        generateUVs();

        // build non-indexed geometry
        position = new BufferAttribute(MathTool.toArrayFloat(vertexBuffer), 3);
        normal = new BufferAttribute(MathTool.toArrayFloat(vertexBuffer), 3);
        uv = new BufferAttribute(MathTool.toArrayFloat(uvBuffer), 2);

        if (detail == 0) {
            computeVertexNormals();
        } else {
            normalizeNormals();
        }
        return this;
    }

    // helper functions
    void subdivide(int detail) {
        Vector3 a = new Vector3();
        Vector3 b = new Vector3();
        Vector3 c = new Vector3();

        // iterate over all faces and apply a subdivison with the given detail value

        for ( int i = 0; i < indices.length; i += 3 ) {

            // get the vertices of the face

            getVertexByIndex( indices[ i + 0 ], a );
            getVertexByIndex( indices[ i + 1 ], b );
            getVertexByIndex( indices[ i + 2 ], c );

            // perform subdivision

            subdivideFace( a, b, c, detail );

        }
    }

    void subdivideFace(Vector3 a, Vector3 b, Vector3 c, int detail) {
        int cols = (int) Math.pow(2, detail);
        // we use this multidimensional array as a data structure for creating the subdivision
        Vector3[][] v = new Vector3[cols + 1][];
        int i, j;
        // construct all of the vertices for this subdivision
        for ( i = 0; i <= cols; i ++ ) {
            int rows = cols - i;
            v[i] = new Vector3[rows + 1];
            Vector3 aj = a.clone().lerp(c, i / cols);
            Vector3 bj = b.clone().lerp(c, i / cols);
            for ( j = 0; j <= rows; j++) {
                if (j == 0 && i == cols) {
                    v[i][j] = aj;
                } else {
                    v[ i ][ j ] = aj.clone().lerp( bj, j / rows );
                }
            }
        }
        // construct all of the faces
        for ( i = 0; i < cols; i ++ ) {

            for ( j = 0; j < 2 * ( cols - i ) - 1; j ++ ) {

                int k = (int) Math.floor( j / 2 );

                if ( j % 2 == 0 ) {

                    pushVertex( v[ i ][ k + 1 ] );
                    pushVertex( v[ i + 1 ][ k ] );
                    pushVertex( v[ i ][ k ] );

                } else {

                    pushVertex( v[ i ][ k + 1 ] );
                    pushVertex( v[ i + 1 ][ k + 1 ] );
                    pushVertex( v[ i + 1 ][ k ] );

                }

            }

        }
    }

    void appplyRadius(float radius) {
        Vector3 vertex = new Vector3();
        // iterate over the entire buffer and apply the radius to each vertex
        for ( int i = 0; i < vertexBuffer.size(); i += 3 ) {

            vertex.x = vertexBuffer.get(i);
            vertex.y = vertexBuffer.get(i + 1);
            vertex.z = vertexBuffer.get(i + 2);

            vertex.normalize().multiplyScalar( radius );

            vertexBuffer.set(i, vertex.x);
            vertexBuffer.set(i + 1, vertex.y);
            vertexBuffer.set(i + 2, vertex.z);
        }
    }

    void generateUVs() {
        Vector3 vertex = new Vector3();
        for ( int i = 0; i < vertexBuffer.size(); i += 3 ) {

            vertex.x = vertexBuffer.get(i);
            vertex.y = vertexBuffer.get(i + 1);
            vertex.z = vertexBuffer.get(i + 2);

            float u = azimuth( vertex ) / 2 / MathTool.PI + 0.5f;
            float v = inclination( vertex ) / MathTool.PI + 0.5f;
            uvBuffer.add( u);
            uvBuffer.add(1 - v);
        }

        correctUVs();

        correctSeam();
    }

    void correctSeam() {
        // handle case when face straddles the seam, see #3269
        for (int i = 0; i < uvBuffer.size(); i += 6) {
            // uv data of a single face
            float x0 = uvBuffer.get(i);
            float x1 = uvBuffer.get(i + 2);
            float x2 = uvBuffer.get(i + 4);

            float max = MathTool.max( x0, x1, x2 );
            float min = MathTool.min( x0, x1, x2 );

            // 0.9 is somewhat arbitrary

            if ( max > 0.9f && min < 0.1f ) {

                if ( x0 < 0.2f ) uvBuffer.set(i, x0 + 1);
                if ( x1 < 0.2f ) uvBuffer.set(i + 2, x1 + 1);
                if ( x2 < 0.2f ) uvBuffer.set(i + 4, x2 + 1);

            }
        }
    }

    void pushVertex(Vector3 vertex) {
        MathTool.push(vertexBuffer, vertex.x, vertex.y, vertex.z);
    }

    void getVertexByIndex(int index, Vector3 vertex) {
        int stride = index * 3;
        vertex.x = vertices[ stride + 0 ];
        vertex.y = vertices[ stride + 1 ];
        vertex.z = vertices[ stride + 2 ];
    }

    void correctUVs() {
        Vector3 a = new Vector3();
        Vector3 b = new Vector3();
        Vector3 c = new Vector3();
        Vector3 centroid = new Vector3();

        Vector2 uvA = new Vector2();
        Vector2 uvB = new Vector2();
        Vector2 uvC = new Vector2();
        for (int i = 0, j = 0; i < vertexBuffer.size(); i += 9, j += 6) {
            a.set( vertexBuffer.get(i), vertexBuffer.get(i + 1), vertexBuffer.get(i + 2) );
            b.set( vertexBuffer.get(i + 3), vertexBuffer.get(i + 4), vertexBuffer.get(i + 5) );
            c.set( vertexBuffer.get(i + 6), vertexBuffer.get(i + 7), vertexBuffer.get(i + 8) );

            uvA.set( uvBuffer.get(j + 0), uvBuffer.get(j + 1) );
            uvB.set( uvBuffer.get(j + 2), uvBuffer.get(j + 3) );
            uvC.set( uvBuffer.get(j + 4), uvBuffer.get(j + 5) );


            centroid.copy( a ).add( b ).add( c ).divideScalar( 3 );

            float azi = azimuth( centroid );

            correctUV( uvA, j + 0, a, azi );
            correctUV( uvB, j + 2, b, azi );
            correctUV( uvC, j + 4, c, azi );
        }
    }
    void correctUV(Vector2 uv, int stride, Vector3 vector, float azimuth) {
        if ( ( azimuth < 0 ) && ( uv.x == 1 ) ) {
            uvBuffer.set(stride, uv.x - 1);
        }

        if ( ( vector.x == 0 ) && ( vector.z == 0 ) ) {
            uvBuffer.set(stride, azimuth / 2 / MathTool.PI + 0.5f);
        }
    }
    // Angle around the Y axis, counter-clockwise when looking from above.
    float azimuth(Vector3 vector) {
        return (float) Math.atan2(vector.z, - vector.x);
    }
    // Angle above the XZ plane.
    float inclination(Vector3 vector) {
        return (float) Math.atan2(- vector.y, Math.sqrt(( vector.x * vector.x ) + ( vector.z * vector.z )));
    }
}
