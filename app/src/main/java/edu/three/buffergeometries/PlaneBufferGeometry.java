package edu.three.buffergeometries;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.geometries.param.PlaneParam;

public class PlaneBufferGeometry extends BufferGeometry {
    private PlaneParam param;
    //buffers
    int[] indices;
    float[] vertices;
    float[] normals;
    float[] uvs;
    int iCount, vCount, nCount, uCount;

    public PlaneBufferGeometry(PlaneParam planeParam) {
        param = planeParam;
        float widthHalf = param.width / 2;
        float heightHalf = param.height / 2;
        int gridX = param.widthSegments;
        int gridY = param.heightSegments;
        int gridX1 = gridX + 1;
        int gridY1 = gridY + 1;
        float segmentWdith = param.width / gridX;
        float segmentHeight = param.height / gridY;

        int pLen = gridX1 * gridY1;
        int iLen = gridX * gridY * 6;
        vertices = new float[pLen * 3];
        normals = new float[pLen * 3];
        uvs = new float[pLen * 2];
        indices = new int[iLen];

        // generate vertices, normals and uvs
        for (int iy = 0; iy < gridY1; iy++) {
            float y = iy * segmentHeight - heightHalf;
            for(int ix = 0; ix < gridX1; ix++) {
                float x = ix * segmentWdith - widthHalf;
                vertices[vCount++]=x; vertices[vCount++]=-y; vertices[vCount++]=0;
                normals[nCount++]=0; normals[nCount++]=0; normals[nCount++]=1;
                uvs[uCount++] = ix / gridX;
                uvs[uCount++] = 1f - iy / gridY;
            }
        }

        // indices
        for (int iy = 0; iy < gridY; iy++) {
            for (int ix = 0; ix < gridX; ix++) {
                int a = ix + gridX1 * iy;
                int b = ix + gridX1 * (iy + 1);
                int c = (ix + 1) + gridX1 * (iy + 1);
                int d = (ix + 1) + gridX1 * iy;
                // faces
                indices[iCount++]=a; indices[iCount++]=b; indices[iCount++]=d;
                indices[iCount++]=b; indices[iCount++]=c; indices[iCount++]=d;
            }
        }
        String tt = log(indices);
        //build geometry
        setIndex(new BufferAttribute().setArray(indices));
        position = new BufferAttribute().setArray(vertices).setItemSize(3);
        normal = new BufferAttribute().setArray(normals).setItemSize(3);
        uv = new BufferAttribute().setArray(uvs).setItemSize(2);
    }

}
