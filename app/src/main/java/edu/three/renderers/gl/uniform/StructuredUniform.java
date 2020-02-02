package edu.three.renderers.gl.uniform;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.renderers.gl.GLLights;
import edu.three.renderers.gl.GLTextures;

public class StructuredUniform extends Uniform {
    public String TAG = getClass().getSimpleName();
    public ArrayList<Uniform> seq = new ArrayList<>();
    public HashMap<String, Uniform> map = new HashMap<>();

//    public void setValue(Object value) {
//        HashMap<String, Object> map = (HashMap<String, Object>) value;
//        for (int i = 0; i < seq.size(); i++) {
//            Uniform u = seq.get(i);
//            u.setValue(map.get(u.id));
//        }
//    }

    public void setValue(Object value, GLTextures textures) {
        try {
            for (int i = 0; i < seq.size(); i++) {
                Uniform u = seq.get(i);
                Object val;
                if (value instanceof ArrayList) {
                    val = ((ArrayList) value).get(Integer.parseInt(u.id));
                } else if (value instanceof GLLights.Uniforms) {
                    val = ((GLLights.Uniforms) value).get(u.id);
                } else {
                    Log.w(TAG, "should not run here to use reflection.");
                    val = value.getClass().getField(u.id).get(value);
                }
                u.setValue(val, textures);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void addUniform(StructuredUniform container, Uniform uniform) {
        container.seq.add(uniform);
        container.map.put(uniform.id, uniform);
    }
}
