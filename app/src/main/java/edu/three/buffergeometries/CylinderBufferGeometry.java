package edu.three.buffergeometries;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.geometries.param.CylinderParam;
import edu.three.math.Vector3;

public class CylinderBufferGeometry extends BufferGeometry {
    private CylinderParam param;

    //buffers
    int[] indices;
    float[] vertices;
    float[] normals;
    float[] uvs;
    int vCount, nCount, uCount;
    int countIndex = 0;

    int index = 0;
    float halfHeight;
    int groupStart = 0;

    public CylinderBufferGeometry(CylinderParam param) {
        this.param = param;
        // helper variables
        halfHeight = param.height / 2;
        vCount = nCount = uCount = 0;

        int indicesLen = param.radialSegments * param.heightSegments * 6;
        int pLen = (param.radialSegments + 1) * (param.heightSegments + 1);
        if (!param.openEnded) {
            if ( param.radiusTop > 0 ) {
                indicesLen += 3 * param.radialSegments;
                pLen += 2 * param.radialSegments + 1;
            }
            if ( param.radiusBottom > 0 ) {
                indicesLen += 3 * param.radialSegments;
                pLen += 2 * param.radialSegments + 1;
            }
        }
        indices = new int[indicesLen];
        vertices = new float[pLen * 3];
        normals = new float[pLen * 3];
        uvs = new float[pLen * 2];
        // generate geometry
        generateTorso();
        if (!param.openEnded) {
            if ( param.radiusTop > 0 ) generateCap( true );
            if ( param.radiusBottom > 0 ) generateCap( false );
        }

        // build geometry
        setIndex(new BufferAttribute().setArray(indices));
        position = new BufferAttribute().setArray(vertices).setItemSize(3);
        normal = new BufferAttribute().setArray(normals).setItemSize(3);
        uv = new BufferAttribute().setArray(uvs).setItemSize(2);

        computeBoundingBox();
        computeBoundingSphere();
    }

    // generate geometry
    private void generateTorso() {
        int x, y;
        Vector3 vertex = new Vector3();
        Vector3 normal = new Vector3();

        int[][] indexArray = new int[param.heightSegments+1][];
        int groupCount = 0;

        // this will be used to calculate the normal
        float slope = ( param.radiusBottom - param.radiusTop ) / param.height;
        // generate verticesLst
        for ( y = 0; y <= param.heightSegments; y++ ) {
            int[] indexRow = new int[param.radialSegments+1];
            float v = (float) y / (float) param.heightSegments;

            // calculate the radius of the current row
            float radius = v * (param.radiusBottom - param.radiusTop) + param.radiusTop;
            for (x = 0; x <= param.radialSegments; x++) {
                float u = (float) x / (float) param.radialSegments;
                float theta = u * param.thetaLength + param.thetaStart;
                float sin = (float) Math.sin(theta);
                float cos = (float) Math.cos(theta);

                // vertex
                float vx = radius * sin;
                float vy = - v * param.height + halfHeight;
                float vz = radius * cos;
                vertices[vCount++] = vx; vertices[vCount++] = vy; vertices[vCount++] = vz;
                // normal
                normal.set( sin, slope, cos ).normalize();
                normals[nCount++] = normal.x; normals[nCount++] = normal.y; normals[nCount++] = normal.z;

                // uv
                uvs[uCount++] = u; uvs[uCount++] = 1 - v;
                // save index of vertex in respective row
                indexRow[x] = index++;
            }
            // now save verticesLst of the row in our index array
            indexArray[y] = indexRow;
        }

        // generate indices
        for (x = 0; x < param.radialSegments; x++) {
            for (y = 0; y < param.heightSegments; y++) {
                // we use the index array to access the correct indices
                int a = indexArray[y][x];
                int b = indexArray[y + 1][x];
                int c = indexArray[y + 1][x + 1];
                int d = indexArray[y][x + 1];
                //faces
                indices[countIndex++] = a; indices[countIndex++] = b; indices[countIndex++] = d;
                indices[countIndex++] = b; indices[countIndex++] = c; indices[countIndex++] = d;
                // update group counter
                groupCount += 6;
            }
        }
        // add a group to the geometry. this will ensure multi material support
        addGroup( groupStart, groupCount, 0 );

        // calculate new start value for groups
        groupStart += groupCount;
    }

    private void generateCap(boolean top) {
        int x, centerIndexStart, centerIndexEnd;
        int groupCount = 0;
        float radius = top ? param.radiusTop : param.radiusBottom;
        int sign = top ? 1 : -1;

        // save the index of the first center vertex
        centerIndexStart = index;

        // first we generate the center vertex data of the cap.
        // because the geometry needs one set of uvs per face,
        // we must generate a center vertex per face/segment
        for (x = 1; x <= param.radialSegments; x++) {
            // vertex
            vertices[vCount++] = 0; vertices[vCount++] = halfHeight * sign; vertices[vCount++] = 0;
            //normal
            normals[nCount++] = 0; normals[nCount++] = halfHeight * sign; normals[nCount++] = 0;
            //uv
            uvs[uCount++] = 0.5f; uvs[uCount++] = 0.5f;
            // increase index
            index ++;
        }
        // save the index of the last center vertex
        centerIndexEnd = index;
        // now we generate the surrounding verticesLst
        for (x = 0; x <= param.radialSegments; x++) {
            float u = (float) x / (float) param.radialSegments;
            float theta = u * param.thetaLength + param.thetaStart;
            float cosTheta = (float) Math.cos(theta);
            float sinTheta = (float) Math.sin(theta);
            // vertex
            float vx = radius * sinTheta;
            float vy = halfHeight * sign;
            float vz = radius * cosTheta;
            vertices[vCount++] = vx; vertices[vCount++] = vy; vertices[vCount++] = vz;
            //normal
            normals[nCount++] = 0; normals[nCount++] = sign; normals[nCount++] = 0;
            //uv
            float ux = ( cosTheta * 0.5f ) + 0.5f;
            float uy = ( sinTheta * 0.5f * sign ) + 0.5f;
            uvs[uCount++] = ux; uvs[uCount++] = uy;
            // increase index
            index ++;
        }
        // generate indices
        for (x = 0; x < param.radialSegments; x++) {
            int c = centerIndexStart + x;
            int i = centerIndexEnd + x;
            if (top) {
                // face top
                indices[countIndex++] = i; indices[countIndex++] = i + 1; indices[countIndex++] = c;
            } else {
                // face bottom
                indices[countIndex++] = i + 1; indices[countIndex++] = i; indices[countIndex++] = c;
            }
            groupCount += 3;
        }
        // add a group to the geometry. this will ensure multi material support
        addGroup( groupStart, groupCount, top ? 1 : 2 );
        // calculate new start value for groups
        groupStart += groupCount;
    }

    public float getHeight() {
        return param.height;
    }
}
