package edu.three.lights;

import edu.three.math.Color;
import edu.three.math.Vector3;

public class HemisphereLightProbe extends LightProbe {

    public HemisphereLightProbe(int skyColor, int groundColor, float intensity) {
        this.intensity = intensity;
        Color color1 = new Color().setHex(skyColor);
        Color color2 = new Color().setHex(groundColor);
        Vector3 sky = new Vector3(color1.r, color1.g, color1.b);
        Vector3 ground = new Vector3(color2.r, color2.g, color2.b);

        // without extra factor of PI in the shader, should = 1 / Math.sqrt( Math.PI );
        float c0 = (float) Math.sqrt(Math.PI);
        float c1 = c0 * (float) Math.sqrt(0.75f);
        sh.elements()[0].copy(sky).add(ground).multiplyScalar(c0);
        sh.elements()[1].copy(sky).add(ground).multiplyScalar(c1);
    }
}
