package edu.three.buffergeometries;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.geometries.param.BoxParam;
import edu.three.math.Vector3;

public class BoxBufferGeometry extends BufferGeometry {
    BoxParam param;
    //buffers
    int[] indices;
    float[] vertices;
    float[] normals;
    float[] uvs;
    int iCount, vCount, nCount, uCount;

    // helper variables
    int numberOfVertices = 0;
    int groupStart = 0;

    public BoxBufferGeometry(BoxParam param) {
        this.param = param;

        // build each side of the box geometry
        int ds = param.depthSegments, hs = param.heightSegments, ws = param.widthSegments;
        int ds1 = ds + 1, hs1 = hs + 1, ws1 = ws + 1;
        int iLen = (ds * hs + hs * ws + ws * ds) * 2;
        int pLen = (ds1 * hs1 + hs1 * ws1 + ws1 * ds1) * 2;

        indices = new int[iLen*6];
        vertices = new float[pLen*3];
        normals = new float[pLen*3];
        uvs = new float[pLen*2];

        buildPlane( 'z', 'y', 'x', - 1, - 1, param.depth, param.height, param.width, ds, hs, 0 ); // px
        buildPlane( 'z', 'y', 'x', 1, - 1, param.depth, param.height, - param.width, ds, hs, 1 ); // nx
        buildPlane( 'x', 'z', 'y', 1, 1, param.width, param.depth, param.height, ws, ds, 2 ); // py
        buildPlane( 'x', 'z', 'y', 1, - 1, param.width, param.depth, - param.height, ws, ds, 3 ); // ny
        buildPlane( 'x', 'y', 'z', 1, - 1, param.width, param.height, param.depth, ws, ds, 4 ); // pz
        buildPlane( 'x', 'y', 'z', - 1, - 1, param.width, param.height, - param.depth, ws, ds, 5 ); // nz

        // build geometry
        setIndex(new BufferAttribute().setArray(indices));
        position = new BufferAttribute().setArray(vertices).setItemSize(3);
        normal = new BufferAttribute().setArray(normals).setItemSize(3);
        uv = new BufferAttribute().setArray(uvs).setItemSize(2);

        computeBoundingBox();
        computeBoundingSphere();
    }

    private void buildPlane(char u, char v, char w,
        int udir, int vdir, float width, float height, float depth,
        int gridX, int gridY, int materialIndex) {
        float segmentWdith = width / gridX;
        float segmentHeight = height / gridY;

        float widthHalf = width / 2;
        float heightHalf = height / 2;
        float depthHalf = depth / 2;

        int gridX1 = gridX + 1;
        int gridY1 = gridY + 1;

        int vertexCounter = 0;
        int groupCount = 0;
        Vector3 vector = new Vector3();
        // generate vertices, normals and uvs
        for (int iy = 0; iy < gridY1; iy++) {
            float y = iy * segmentHeight - heightHalf;
            for (int ix = 0; ix < gridX1; ix++) {
                float x = ix * segmentWdith - widthHalf;
                // set values to correct vector component
                vector.setField(u, x * udir);
                vector.setField(v, y * vdir);
                vector.setField(w, depthHalf);
                // now apply vector to vertex buffer
                vertices[vCount++] = (float)vector.x; vertices[vCount++] = (float)vector.y; vertices[vCount++] = (float)vector.z;
                // set values to correct vector component
                vector.setField(u, 0);
                vector.setField(v, 0);
                vector.setField(w, depth > 0 ? 1 : - 1);
                // now apply vector to normal buffer
                normals[nCount++] = (float)vector.x; normals[nCount++] = (float)vector.y; normals[nCount++] = (float)vector.z;
                // uvs
                uvs[uCount++] = ix / gridX;
                uvs[uCount++] = 1 - ( iy / gridY );

                // counters
                vertexCounter += 1;
            }
        }
        // indices

        // 1. you need three indices to draw a single face
        // 2. a single segment consists of two faces
        // 3. so we need to generate six (2*3) indices per segment
        for (int iy = 0; iy < gridY; iy++) {
            for (int ix = 0; ix < gridX; ix++) {
                int a = numberOfVertices + ix + gridX1 * iy;
                int b = numberOfVertices + ix + gridX1 * ( iy + 1 );
                int c = numberOfVertices + ( ix + 1 ) + gridX1 * ( iy + 1 );
                int d = numberOfVertices + ( ix + 1 ) + gridX1 * iy;
                // faces
                indices[iCount++] = a; indices[iCount++] = b; indices[iCount++] = d;
                indices[iCount++] = b; indices[iCount++] = c; indices[iCount++] = d;
                // increase counter
                groupCount += 6;
            }
        }
        // add a group to the geometry. this will ensure multi material support
        addGroup(groupStart, groupCount, materialIndex);
        // calculate new start value for groups
        groupStart += groupCount;

        // update total number of vertices
        numberOfVertices += vertexCounter;
    }
}
