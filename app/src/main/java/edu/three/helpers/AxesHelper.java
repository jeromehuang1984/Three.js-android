package edu.three.helpers;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.materials.LineBasicMaterial;
import edu.three.objects.LineSegments;

import static edu.three.constant.Constants.VertexColors;

public class AxesHelper extends LineSegments {
    float size = 1;
    public AxesHelper(float size) {
        if (size > 0) {
            this.size = size;
        }
        float[] vertices = new float[] {
                0, 0, 0,	size, 0, 0,
                0, 0, 0,	0, size, 0,
                0, 0, 0,	0, 0, size
        };
        float[] colors = new float[] {
                1, 0, 0,	1, 0.6f, 0,
                0, 1, 0,	0.6f, 1, 0,
                0, 0, 1,	0, 0.6f, 1
        };
        geometry = new BufferGeometry();
        geometry.position = new BufferAttribute(vertices, 3);
        geometry.color = new BufferAttribute(colors, 3);
        LineBasicMaterial lineMaterial = new LineBasicMaterial();
        lineMaterial.vertexColors = VertexColors;
        material.clear();
        material.add(lineMaterial);
    }
}
