package edu.three.renderers.gl.uniform;

import android.opengl.GLES30;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.math.Vector4;
import edu.three.renderers.gl.GLTextures;
import edu.three.textures.CubeTexture;
import edu.three.textures.DataTexture2DArray;
import edu.three.textures.DataTexture3D;
import edu.three.textures.Texture;

public class PureArrayUniform extends Uniform {

    public PureArrayUniform(int addr, String id, int size, int type) {
        super(addr, id, size, type);
    }

    public void setValue(Object value, GLTextures textures) {
        switch (type) {
            case 0x1406:
                setValueV1fArray((float[]) value); // FLOAT
                break;
            case 0x8b50:
                setValueV2fArray((ArrayList<Object>) value); // _VEC2
                break;
            case 0x8b51:
                setValueV3fArray((ArrayList<Object>) value); // _VEC3
                break;
            case 0x8b52:
                setValueV4fArray((ArrayList<Object>) value); // _VEC4
                break;
            case 0x8b5b:
                setValueM3Array((ArrayList<Object>) value); // _MAT3
                break;
            case 0x8b5c:
                setValueM4Array((ArrayList<Object>) value);// _MAT4
                break;
            case 0x8b5e:
                setValueT1Array((ArrayList) value, textures);// SAMPLER_2D
                break;
            case 0x8b60:
                setValueT6Array((ArrayList) value, textures);// SAMPLER_CUBE
                break;

            case 0x1404: case 0x8b56: setValueV1iArray((int[]) value); break; // INT, BOOL
            case 0x8b53: case 0x8b57: setValueV2iArray((int[]) value); break; // _VEC2
            case 0x8b54: case 0x8b58: setValueV3iArray((int[]) value); break; // _VEC3
            case 0x8b55: case 0x8b59: setValueV4iArray((int[]) value); break; // _VEC4
        }
    }

    private float[] flatten(ArrayList<Object> array, int nBlocks, int blockSize) {
        int n = nBlocks * blockSize;
        float[] ret = new float[n];
        int offset = 0;
        for (int i = 0; i < nBlocks; i++) {
            Object obj = array.get(i);
            if (obj instanceof Vector2) {
                ((Vector2) obj).toArray(ret, offset);
            } else if (obj instanceof Vector3) {
                ((Vector3) obj).toArray(ret, offset);
            } else if (obj instanceof Vector4) {
                ((Vector4) obj).toArray(ret, offset);
            } else if (obj instanceof Matrix3) {
                ((Matrix3) obj).toArray(ret, offset);
            } else if (obj instanceof Matrix4) {
                ((Matrix4) obj).toArray(ret, offset);
            }
            offset += blockSize;
        }
        return ret;
    }

    private void setValueV1iArray(int[] v) {
        GLES30.glUniform1iv(addr, 1, v, 0);
    }

    private void setValueV2iArray(int[] v) {
        GLES30.glUniform2iv(addr, 1, v, 0);
    }

    private void setValueV3iArray(int[] v) {
        GLES30.glUniform1iv(addr, 1, v, 0);
    }

    private void setValueV4iArray(int[] v) {
        GLES30.glUniform4iv(addr, 1, v, 0);
    }

    private void setValueV1fArray(float[] v) {
        GLES30.glUniform1fv(addr, 1, v, 0);
    }

    private void setValueV2fArray(ArrayList<Object> lst) {
        float[] data = flatten(lst, size, 2);
        GLES30.glUniform2fv(addr, 1, data, 0);
    }

    private void setValueV3fArray(ArrayList<Object> lst) {
        float[] data = flatten(lst, size, 3);
        GLES30.glUniform3fv(addr, 1, data, 0);
    }

    private void setValueV4fArray(ArrayList<Object> lst) {
        float[] data = flatten(lst, size, 4);
        GLES30.glUniform4fv(addr, 1, data, 0);
    }

    private void setValueM3Array(ArrayList<Object> lst) {
        float[] data = flatten(lst, size, 9);
        GLES30.glUniformMatrix3fv(addr, 1, false, data, 0);
    }

    private void setValueM4Array(ArrayList<Object> lst) {
        float[] data = flatten(lst, size, 16);
        GLES30.glUniformMatrix4fv(addr, 1, false, data, 0);
    }

    private HashMap<Integer, int[]> arrayCacheI32 = new HashMap<>();
    Texture emptyTexture = new Texture();
    DataTexture2DArray emptyTexture2dArray = new DataTexture2DArray();
    DataTexture3D emptyTexture3d = new DataTexture3D();
    CubeTexture emptyCubeTexture = new CubeTexture();

    private int[] allocTexUnits(GLTextures textures, int n) {
        int[] r = arrayCacheI32.get(n);
        if (r == null) {
            r = new int[n];
            arrayCacheI32.put(n, r);
        }
        for (int i = 0; i < n; i++) {
            r[i] = textures.allocateTextureUnit();
        }
        return r;
    }
    // Array of textures (2D / Cube), assume v.size() == 1
    private void setValueT1Array(ArrayList v, GLTextures textures) {
        int n = v.size();
        int[] units = allocTexUnits(textures, n);
        GLES30.glUniform1iv(addr, 1, units, 0);
        for (int i = 0; i < n; i++) {
            Texture t = emptyTexture;
            if (v.get(i) != null) {
                t = (Texture) v.get(i);
            }
            textures.setTexture2D(t, units[i]);
        }
    }

    private void setValueT6Array(ArrayList v, GLTextures textures) {
        int n = v.size();
        int[] units = allocTexUnits(textures, n);
        GLES30.glUniform1iv(addr, 1, units, 0);
        for (int i = 0; i < n; i++) {
            CubeTexture t = emptyCubeTexture;
            if (v.get(i) != null) {
                t = (CubeTexture) v.get(i);
            }
            textures.setTextureCube(t, units[i]);
        }
    }
}
