package edu.three.renderers.gl.uniform;

import android.opengl.GLES30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import edu.three.math.Color;
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

public class SingleUniform extends Uniform {
    private float[] cache;
    private int[] cacheInt;

    public SingleUniform(int addr, String id, int size, int type) {
        super(addr, id, size, type);
    }

    static FloatBuffer mat2array = ByteBuffer
            .allocateDirect(4 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(new float[4]);

    static FloatBuffer mat3array = ByteBuffer
            .allocateDirect(9 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(new float[9]);

    static FloatBuffer mat4array = ByteBuffer
            .allocateDirect(16 * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(new float[16]);

    static public boolean arraysEqual(float[] a, float[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    static public boolean arraysEqual(int[] a, int[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

//    public void setValue(Object value, GLTextures textures) {
//        if (value instanceof Float) {
//            setValueV1f( (Float) value);
//        } else if (value instanceof Vector2) {
//            setValueV2f((Vector2) value);
//        } else if (value instanceof Vector3) {
//            setValueV3f((Vector3) value);
//        } else if (value instanceof Color) {
//            setValueV3f((Color) value);
//        } else if (value instanceof Vector4) {
//            setValueV4f((Vector4) value);
//        } else if (value instanceof Matrix3) {
//            setValueM3((Matrix3) value);
//        } else if (value instanceof Matrix4) {
//            setValueM4((Matrix4) value);
//        } else if (value instanceof Integer) {
//            setValueV1i( (Integer) value);
//        } else if (value instanceof int[]) {
//            setValueIv((int[]) value);
//        } else if (value instanceof float[]) {
//            setValueFv((float[]) value);
//        }
//    }

    float toFloat(Object value) {
        if (value instanceof Double)
            return ((Number)value).floatValue();
        return (float) value;
    }
    public void setValue(Object value, GLTextures textures) {
        if (value == null) {
            return;
        }
        if (value instanceof int[]) {
            setValueIv((int[]) value);
        } else if (value instanceof float[]) {
            setValueFv((float[]) value);
        } else {
            switch ( type ) {
                case 0x1406: setValueV1f( toFloat(value)); break; // FLOAT
                case 0x8b50: setValueV2f((Vector2) value); break; // _VEC2
                case 0x8b51:    // _VEC3
                    if (value instanceof Color) {
                        setValueV3f((Color) value);
                    } else {
                        setValueV3f((Vector3) value);
                    }
                    break;
                case 0x8b52: setValueV4f((Vector4) value); break; // _VEC4

                case 0x8b5a: setValueM2((float[]) value); break; // _MAT2
                case 0x8b5b: setValueM3((Matrix3) value); break; // _MAT3
                case 0x8b5c: setValueM4((Matrix4) value); break; // _MAT4

                case 0x8b5e: case 0x8d66: setValueT1((Texture) value, textures); break; // SAMPLER_2D, SAMPLER_EXTERNAL_OES
                case 0x8b5f: setValueT3D1((DataTexture3D) value, textures); break;  // SAMPLER_3D
                case 0x8b60: setValueT6((CubeTexture) value, textures); break; // SAMPLER_CUBE
                case 0x8DC1: setValueT2DArray1((DataTexture2DArray) value, textures); break; // SAMPLER_2D_ARRAY

                case 0x1404: case 0x8b56:   // INT, BOOL
                    if (value instanceof Boolean) {
                        int v = ((Boolean) value) ? 1 : 0;
                        setValueV1i( v );
                    } else {
                        setValueV1i( (Integer) value);
                    }
                    break;
//                case 0x8b53: case 0x8b57: setValueV2i( (int[]) value); break; // _VEC2
//                case 0x8b54: case 0x8b58: setValueV3i( (int[]) value); break; // _VEC3
//                case 0x8b55: case 0x8b59: setValueV4i( (int[]) value); break; // _VEC4

            }
        }
    }

    private void setValueFv(float[] arr) {
        if (cache == null) {
            cache = new float[arr.length];
        } else {
            if (arraysEqual(cache, arr)) {
                return;
            }
            switch (arr.length) {
                case 2:
                    GLES30.glUniform2fv(addr, 1, arr, 0);
                    break;
                case 3:
                    GLES30.glUniform3fv(addr, 1, arr, 0);
                    break;
                case 4:
                    GLES30.glUniform4fv(addr, 1, arr, 0);
                    break;
                case 9:
                    GLES30.glUniformMatrix3fv(addr, 1,  false, arr,0);
                    break;
                case 16:
                    GLES30.glUniformMatrix4fv(addr, 1,  false, arr,0);
                    break;
            }
            cache = Arrays.copyOf(arr, arr.length);
        }
    }

    private void setValueIv(int[] arr) {
        if (cacheInt == null) {
            cacheInt = new int[arr.length];
        } else {
            if (arraysEqual(cacheInt, arr)) {
                return;
            }
            switch (arr.length) {
                case 2:
                    GLES30.glUniform2iv(addr, 1, arr, 0);
                    break;
                case 3:
                    GLES30.glUniform3iv(addr, 1, arr, 0);
                    break;
                case 4:
                    GLES30.glUniform4iv(addr, 1, arr, 0);
                    break;
            }
            cacheInt = Arrays.copyOf(arr, arr.length);
        }
    }

    private void setValueV1f(float v) {
        if (cache == null) {
            cache = new float[1];
        } else {
            if (cache[0] == v) {
                return;
            }
        }
        GLES30.glUniform1f(addr, v);
        cache[0] = v;
    }

    private void setValueV2f(Vector2 v) {
        if (cache == null) {
            cache = new float[2];
        } else {
            if (cache[0] != v.x || cache[1] != v.y) {
                GLES30.glUniform2f(addr, (float)v.x, (float)v.y);
                cache[ 0 ] = (float)v.x;
                cache[ 1 ] = (float)v.y;
            }
        }
    }

    private void setValueV3f(Vector3 v) {
        if (cache == null) {
            cache = new float[3];
        } else {
            if (cache[0] != v.x || cache[1] != v.y || cache[2] != v.z) {
                GLES30.glUniform3f(addr, (float)v.x, (float)v.y, (float)v.z);
                cache[ 0 ] = (float)v.x;
                cache[ 1 ] = (float)v.y;
                cache[ 2 ] = (float)v.z;
            }
        }
    }

    private void setValueV3f(Color v) {
        if (cache == null) {
            cache = new float[3];
        } else {
            if (cache[0] != v.r || cache[1] != v.g || cache[2] != v.b) {
                GLES30.glUniform3f(addr, (float)v.r, (float)v.g, (float)v.b);
                cache[ 0 ] = (float)v.r;
                cache[ 1 ] = (float)v.g;
                cache[ 2 ] = (float)v.b;
            }
        }
    }

    private void setValueV4f(Vector4 v) {
        if (cache == null) {
            cache = new float[4];
        } else {
            if (cache[0] != v.x || cache[1] != v.y || cache[2] != v.z || cache[3] != v.w) {
                GLES30.glUniform4f(addr, (float)v.x, (float)v.y, (float)v.z, (float)v.w);
                cache[ 0 ] = (float)v.x;
                cache[ 1 ] = (float)v.y;
                cache[ 2 ] = (float)v.z;
                cache[ 3 ] = (float)v.w;
            }
        }
    }

    // Single matrix (from flat array or MatrixN)
    private void setValueM2(float[] elements) {
        if (cache == null) {
            cache = new float[4];
        } else {
            if (arraysEqual(cache, elements)) {
                return;
            }
            mat2array.clear();
            mat2array.put(elements);
            mat2array.position(0);
            GLES30.glUniformMatrix3fv(addr, 1, false, mat2array);
            System.arraycopy(elements, 0, cache, 0, cache.length);
        }
    }

    private void setValueM3(Matrix3 v) {
        float[] elements = v.toArrayF();
        if (cache == null) {
            cache = new float[9];
        } else {
            if (arraysEqual(cache, elements)) {
                return;
            }
            mat3array.clear();
            mat3array.put(elements);
            mat3array.position(0);
            GLES30.glUniformMatrix3fv(addr, 1, false, mat3array);
            System.arraycopy(elements, 0, cache, 0, cache.length);
        }
    }

    private void setValueM4(Matrix4 v) {
        float[] elements = v.toArrayF();
        if (cache == null) {
            cache = new float[16];
        } else {
            if (arraysEqual(cache, elements)) {
                return;
            }
            mat4array.clear();
            mat4array.put(elements);
            mat4array.position(0);
            GLES30.glUniformMatrix4fv(addr, 1, false, mat4array);
            System.arraycopy(elements, 0, cache, 0, cache.length);
        }
    }

    // Integer / Boolean vectors or arrays thereof (always flat arrays)
    private void setValueV1i(int v) {
        if (cacheInt == null) {
            cacheInt = new int[1];
        } else {
            if (cacheInt[0] == v) {
                return;
            }
            GLES30.glUniform1i(addr, v);
            cacheInt[0] = 1;
        }
    }

    private void setValueV2i(int[] v) {
        if (cacheInt == null) {
            cacheInt = new int[2];
        } else {
            if (arraysEqual(cacheInt, v)) {
                return;
            }
            GLES30.glUniform2iv(addr, 1, v, 0);
            cacheInt[0] = v[0];
            cacheInt[1] = v[1];
        }
    }

    private void setValueV3i(int[] v) {
        if (cacheInt == null) {
            cacheInt = new int[3];
        } else {
            if (arraysEqual(cacheInt, v)) {
                return;
            }
            GLES30.glUniform3iv(addr, 1, v, 0);
            cacheInt[0] = v[0];
            cacheInt[1] = v[1];
            cacheInt[2] = v[2];
        }
    }

    private void setValueV4i(int[] v) {
        if (cacheInt == null) {
            cacheInt = new int[4];
        } else {
            if (arraysEqual(cacheInt, v)) {
                return;
            }
            GLES30.glUniform4iv(addr, 1, v, 0);
            cacheInt[0] = v[0];
            cacheInt[1] = v[1];
            cacheInt[2] = v[2];
            cacheInt[3] = v[3];
        }
    }
    // Single texture (2D / Cube)
    private void setValueT1(Texture v, GLTextures textures) {
        if (cacheInt == null) {
            cacheInt = new int[] {-1};
        }
        int unit = textures.allocateTextureUnit();
        if (cacheInt[0] != unit) {
            GLES30.glUniform1i(addr, unit);
            cacheInt[0] = unit;
        }
        if (v == null) {
            v = new Texture();
        }
        textures.setTexture2D(v, unit);
    }

    private void setValueT2DArray1(DataTexture2DArray v, GLTextures textures) {
        if (cacheInt == null) {
            cacheInt = new int[] {-1};
        }
        int unit = textures.allocateTextureUnit();
        if (cacheInt[0] != unit) {
            GLES30.glUniform1i(addr, unit);
            cacheInt[0] = unit;
        }
        if (v == null) {
            v = new DataTexture2DArray();
        }
        textures.setTexture2DArray(v, unit);
    }

    private void setValueT3D1(DataTexture3D v, GLTextures textures) {
        if (cacheInt == null) {
            cacheInt = new int[] {-1};
        }
        int unit = textures.allocateTextureUnit();
        if (cacheInt[0] != unit) {
            GLES30.glUniform1i(addr, unit);
            cacheInt[0] = unit;
        }
        if (v == null) {
            v = new DataTexture3D();
        }
        textures.setTexture3D(v, unit);
    }

    private void setValueT6(CubeTexture v, GLTextures textures) {
        if (cacheInt == null) {
            cacheInt = new int[] {-1};
        }
        int unit = textures.allocateTextureUnit();
        if (cacheInt[0] != unit) {
            GLES30.glUniform1i(addr, unit);
            cacheInt[0] = unit;
        }
        if (v == null) {
            v = new CubeTexture();
        }
        textures.setTextureCube(v, unit);
    }

}
