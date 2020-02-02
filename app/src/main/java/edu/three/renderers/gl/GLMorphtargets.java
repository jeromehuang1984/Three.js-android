package edu.three.renderers.gl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Object3D;
import edu.three.materials.Material;
import edu.three.math.MathTool;

public class GLMorphtargets {
    Comparator<float[]> absNumericalSort = new Comparator<float[]>() {
        @Override
        public int compare(float[] a, float[] b) {
            return MathTool.sign(Math.abs( b[ 1 ] ) - Math.abs( a[ 1 ] ));
        }
    };

    float[] morphInfluences = new float[8];
    HashMap<Long, float[][]> influencesList = new HashMap<>();

    public void update(Object3D object, BufferGeometry geometry, Material material, GLProgram program) {
        float[] objectInfluences = object.morphTargetInfluences;
        int length = objectInfluences.length;
        float[][] influences = influencesList.get(geometry.id);
        if (influences == null) {
            // initialise list
            influences = new float[length][];
            for (int i = 0; i < length; i++) {
                influences[ i ] = new float[] { i, 0 };
            }
            influencesList.put(geometry.id, influences);
        }

        ArrayList<BufferAttribute> morphTargets = material.morphTargets ? geometry.morphAttributeMap.get("position") : null;
        ArrayList<BufferAttribute> morphNormals = material.morphNormals ? geometry.morphAttributeMap.get("normal") : null;

        // Remove current morphAttributes
        for (int i = 0; i < length; i++) {
            float[] influence = influences[i];
            if (influence[1] != 0) {
                if (morphTargets != null) {
                    geometry.removeAttribute("morphTarget" + i);
                }
                if (morphNormals != null) {
                    geometry.removeAttribute("morphNormal" + i);
                }
            }
        }

        // Collect influences
        for (int i = 0; i < length; i++) {
            float[] influence = influences[i];
            influence[0] = i;
            influence[1] = objectInfluences[i];
        }
        List<float[]> list = Arrays.asList(influences);
        Collections.sort(list, absNumericalSort);

        // Add morphAttributes
        for (int i = 0; i < 8; i++) {
            float[] influence = influences[i];
            if (influence != null) {
                int index = (int) influence[0];
                float value = influence[1];
                if (value > 0) {
                    if (morphTargets != null) {
                        geometry.addAttribute("morphTarget" + i, morphTargets.get(index));
                    }
                    if (morphNormals != null) {
                        geometry.addAttribute("morphNormal" + i, morphNormals.get(index));
                    }
                    morphInfluences[ i ] = value;
                    continue;
                }
            }
            morphInfluences[ i ] = 0;
        }
        program.getUniforms().setValue("morphTargetInfluences", morphInfluences);
    }
}
