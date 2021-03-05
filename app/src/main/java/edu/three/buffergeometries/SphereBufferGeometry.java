package edu.three.buffergeometries;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.geometries.param.SphereParam;
import edu.three.math.Vector3;

public class SphereBufferGeometry extends BufferGeometry {
    private SphereParam param;
    //buffers
    int[] indices;
    float[] vertices;
    float[] normals;
    float[] uvs;
    int iCount, vCount, nCount, uCount;

    public SphereBufferGeometry(SphereParam sphereParam) {
        param = sphereParam;
        float radius = sphereParam.radius;
        int widthSegments = Math.max(3, param.widthSegments);
        int heightSegments = Math.max(2, param.heightSegments);
        float phiStart = param.phiStart;
        float phiLength = param.phiLength;
        float thetaStart = param.thetaStart;
        float thetaLength = param.thetaLength;
        float thetaEnd = Math.min(thetaStart + thetaLength, (float) Math.PI);

        int index = 0;
        Vector3 vertex = new Vector3();
        int pLen = (widthSegments + 1) * (heightSegments + 1);
        int iLen = heightSegments * widthSegments * 6;
        if (thetaStart <= 0) {
            iLen -= widthSegments * 3;
        }
        if (thetaEnd >= Math.PI) {
            iLen -= widthSegments * 3;
        }
        indices = new int[iLen];
        vertices = new float[pLen * 3];
        normals = new float[pLen * 3];
        uvs = new float[pLen * 2];

        int[][] grid = new int[heightSegments + 1][];
        int gCount = 0;

        // generate vertices, normals and uvs
        for (int iy = 0; iy <= heightSegments; iy++) {
            int[] indexRow = new int[widthSegments + 1];
            int count = 0;
            float v = (float) iy / heightSegments;
            // special case for the poles
            float uOffset = 0;
            if (iy == 0 && thetaStart == 0) {
                uOffset = 0.5f / widthSegments;
            } else if (iy == heightSegments && thetaEnd == Math.PI) {
                uOffset = -0.5f / widthSegments;
            }
            for (int ix = 0; ix <= widthSegments; ix++) {
                float u = (float) ix / widthSegments;
                // vertex
                vertex.x = - radius * (float) Math.cos( phiStart + u * phiLength ) * (float) Math.sin( thetaStart + v * thetaLength );
                vertex.y = radius * (float) Math.cos( thetaStart + v * thetaLength );
                vertex.z = radius * (float) Math.sin( phiStart + u * phiLength ) * (float) Math.sin( thetaStart + v * thetaLength );
                vertices[vCount++]=(float)vertex.x; vertices[vCount++]=(float)vertex.y; vertices[vCount++]=(float)vertex.z;
                // normal
                vertex.normalize();
                normals[nCount++]=(float)vertex.x; normals[nCount++]=(float)vertex.y; normals[nCount++]=(float)vertex.z;
                // uv
                uvs[uCount++] = u + uOffset; uvs[uCount++] = 1 - v;
                indexRow[count++] = index++;
            }
            grid[gCount++] = indexRow;
        }
        // indices
        for (int iy = 0; iy < heightSegments; iy++) {
            for (int ix = 0; ix < widthSegments; ix++) {
                int a = grid[iy][ix + 1];
                int b = grid[ iy ][ ix ];
                int c = grid[ iy + 1 ][ ix ];
                int d = grid[ iy + 1 ][ ix + 1 ];

                if ( iy != 0 || thetaStart > 0 ) {
                    indices[iCount++]=a; indices[iCount++]=b; indices[iCount++]=d;
                }
                if ( iy != heightSegments - 1 || thetaEnd < Math.PI ) {
                    indices[iCount++]=b; indices[iCount++]=c; indices[iCount++]=d;
                }
            }
        }
        //build geometry
        setIndex(new BufferAttribute().setArray(indices));
        position = new BufferAttribute().setArray(vertices).setItemSize(3);
        normal = new BufferAttribute().setArray(normals).setItemSize(3);
        uv = new BufferAttribute().setArray(uvs).setItemSize(2);

        computeBoundingBox();
        computeBoundingSphere();
    }
}
