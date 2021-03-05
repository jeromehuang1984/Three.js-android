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
    Comparator<double[]> absNumericalSort = new Comparator<double[]>() {
        @Override
        public int compare(double[] a, double[] b) {
            return MathTool.sign(Math.abs( b[ 1 ] ) - Math.abs( a[ 1 ] ));
        }
    };

    double[] morphInfluences = new double[8];
    HashMap<Long, double[][]> influencesList = new HashMap<>();

    public void update(Object3D object, BufferGeometry geometry, Material material, GLProgram program) {
        double[] objectInfluences = object.morphTargetInfluences;
        int length = objectInfluences.length;
        double[][] influences = influencesList.get(geometry.id);
        if (influences == null) {
            // initialise list
            influences = new double[length][];
            for (int i = 0; i < length; i++) {
                influences[ i ] = new double[] { i, 0 };
            }
            influencesList.put(geometry.id, influences);
        }

        ArrayList<BufferAttribute> morphTargets = material.morphTargets ? geometry.morphAttributeMap.get("position") : null;
        ArrayList<BufferAttribute> morphNormals = material.morphNormals ? geometry.morphAttributeMap.get("normal") : null;

        // Remove current morphAttributes
        for (int i = 0; i < length; i++) {
            double[] influence = influences[i];
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
            double[] influence = influences[i];
            influence[0] = i;
            influence[1] = objectInfluences[i];
        }
        List<double[]> list = Arrays.asList(influences);
        Collections.sort(list, absNumericalSort);

        // Add morphAttributes
        for (int i = 0; i < 8; i++) {
            double[] influence = influences[i];
            if (influence != null) {
                int index = (int) influence[0];
                double value = influence[1];
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
