package edu.three.materials;

import java.util.HashMap;

public class MeshPhysicalMaterial extends MeshStandardMaterial {

//    public float reflectivity = 0.5f;   // maps to F0 = 0.04
    public float clearCoat = 0;
    public float clearCoatRoughness = 0;

    public MeshPhysicalMaterial() {
        defines.put("PHYSICAL", "");
        reflectivity = 0.5f;   // maps to F0 = 0.04
    }

}
