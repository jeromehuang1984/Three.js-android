package edu.three.objects;

import java.util.ArrayList;

import edu.three.buffergeometries.CylinderBufferGeometry;
import edu.three.core.Object3D;
import edu.three.geometries.param.CylinderParam;
import edu.three.materials.LineBasicMaterial;
import edu.three.materials.Material;
import edu.three.math.Vector3;
import edu.three.objects.Mesh;

public class PuzzleArrow extends Object3D {
    Material material = new LineBasicMaterial();
    CylinderBufferGeometry coneGeometry;
    Mesh cone;
    Vector3 dir = new Vector3(0, 0, 1);
    Vector3 origin = new Vector3(0, 0, 0);
    float length = 1;
    int color = 0xffff00;
    float headLength = 0.2f * length;
    float headWidth = 0.2f * headLength;

    public PuzzleArrow(Vector3 dir, Vector3 origin, Float length, Integer color,
                       Float headLength, Float headWidth, Material materialInput) {
        if (dir != null) this.dir = dir;
        if (origin != null) this.origin = origin;
        if (length != null) this.length = length;
        if (color != null) this.color = color;
        if (headLength != null) this.headLength = headLength;
        if (headWidth != null) this.headWidth = headWidth;
        if (materialInput != null) this.material = materialInput;

        CylinderParam param = new CylinderParam().setTopR(0.5f).setBottomR(0)
                .setHeight(1).setRadialSegments(5).setHeightSegments(1);
        coneGeometry = new CylinderBufferGeometry(param);
        coneGeometry.translate(0, -0.5f, 0);

        position.copy(origin);
        cone = new Mesh(coneGeometry, material);
        cone.matrixAutoUpdate = false;
        add(cone);
        setDirection(dir);
        setLength(length, headLength, headWidth);
    }

    public void setDirection(Vector3 dir) {
        Vector3 axis = new Vector3();
        // dir is assumed to be normalized
        if ( dir.y > 0.99999 ) {
            this.quaternion.set( 0, 0, 0, 1 );
        } else if ( dir.y < - 0.99999 ) {
            this.quaternion.set( 1, 0, 0, 0 );
        } else {
            axis.set( dir.z, 0, - dir.x ).normalize();
            float radians = (float) Math.acos( dir.y );

            this.quaternion.setFromAxisAngle( axis, radians );
        }
    }

    public void setLength(float length, Float headLength, Float headWidth) {
        if ( headLength == null ) headLength = 0.2f * length;
        if ( headWidth == null ) headWidth = 0.2f * headLength;

        // this.line.scale.set( 1, Math.max( 0, length - headLength ), 1 );
        // this.line.updateMatrix();

        this.cone.scale.set( headWidth, headLength, headWidth );
        this.cone.position.y = length;
        this.cone.updateMatrix();
    }

    public void setColor(int color) {
        for (Material material : cone.material) {
            material.color.setHex(color);
        }
    }
    public void changeMaterial(Material material) {
        cone.material.clear();
        cone.material.add(material);
    }

    public void changeMaterial(ArrayList<Material> material) {
        cone.material = material;
    }
}
