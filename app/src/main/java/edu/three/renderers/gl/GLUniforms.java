package edu.three.renderers.gl;

import android.opengl.GLES30;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.three.core.Object3D;
import edu.three.renderers.gl.uniform.PureArrayUniform;
import edu.three.renderers.gl.uniform.SingleUniform;
import edu.three.renderers.gl.uniform.StructuredUniform;
import edu.three.renderers.gl.uniform.Uniform;
import edu.three.renderers.shaders.UniformsLib;
import edu.three.textures.Texture;

public class GLUniforms extends StructuredUniform {
    String RePathPart = "([\\w\\d_]+)(\\])?(\\[|\\.)?";
    Pattern pattern = Pattern.compile(RePathPart);

    public GLUniforms(int program) {
        int[] count = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_ACTIVE_UNIFORMS, count, 0);
        for (int i = 0; i < count[0]; i++) {
            int bufSize = 256;
            byte[] bytes = new byte[bufSize];    //maximum name length
            int[] nameLen = new int[1];
            int[] size = new int[1];
            int[] type = new int[1];
            GLES30.glGetActiveUniform(program, i, bufSize, nameLen, 0, size, 0, type, 0, bytes, 0);
            String uniformName = bytesToStr(bytes, nameLen[0]);
            int addr = GLES30.glGetUniformLocation(program, uniformName);
            parseUnform(addr, uniformName, size[0], type[0], this);
        }
    }

    private String bytesToStr(byte[] bytes, int bytesLen) {
        byte[] byteLst = Arrays.copyOf(bytes, bytesLen);
        return new String(byteLst);
    }

    private void parseUnform(int addr, String uniformName, int size, int type, StructuredUniform container) {
        int pathLength = uniformName.length();
        Matcher match = pattern.matcher(uniformName);
        while (match.find()) {
            int matchEnd = match.end();
            String id = match.group(1);
            boolean idIsIndex = "]".equals(match.group(2));
            String subscript = match.group(3);
            if (subscript == null || (subscript.equals("[") && matchEnd + 2 == pathLength) ) {
                // bare name or "pure" bottom-level array "[0]" suffix
                Uniform uniform = subscript == null ? new SingleUniform(addr, id, size, type)
                        : new PureArrayUniform(addr, id, size, type);
                addUniform(container, uniform);
                break;
            } else {
                // step into inner node / create it in case it doesn't exist
                Uniform next = container.map.get(id);
                if (next == null) {
                    next = new StructuredUniform();
                    next.id = id;
                    addUniform(container, next);
                }
                if (next instanceof StructuredUniform) {
                    container = (StructuredUniform) next;
                }
            }
        }
    }

    public void setValue(String name, Object value) {
        Uniform u = map.get(name);
        if (u != null) {
            u.setValue(value, null);
        }
    }

    public void setValue(String name, Object value, GLTextures textures) {
        Uniform u = map.get(name);
        if (u != null) {
            u.setValue(value, textures);
        }
    }

    public void setOptional(Object object, String name) {
        try {
            Object v = object.getClass().getField(name).get(object);
            if (v != null) {
                setValue(name, v);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    static public void upload(ArrayList<Uniform> seq, UniformsLib values, GLTextures textures) {
        for (int i = 0; i < seq.size(); i++) {
            Uniform u = seq.get(i);
            if (values.contains(u.id)) {
                Object value = values.get(u.id);
                u.setValue(value, textures);
            }
        }
    }

    static public ArrayList<Uniform> seqWithValue(ArrayList<Uniform> seq, UniformsLib values) {
        ArrayList<Uniform> ret = new ArrayList<>();
        for (int i = 0; i < seq.size(); i++) {
            Uniform u = seq.get(i);
            if (values.contains(u.id)) {
                ret.add(u);
            }
        }
        return ret;
    }

}
