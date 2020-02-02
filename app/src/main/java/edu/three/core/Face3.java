package edu.three.core;

import edu.three.math.Color;
import edu.three.math.Vector3;

public class Face3 {
    public int a, b, c;
    int color;
    public int materialIndex = 0;
    public Vector3 normal = new Vector3();
    public Vector3 center;
    public Vector3[] vertexNormals = null;
    public Color[] vertexColors = new Color[0];

    public Face3(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public Face3(int a, int b, int c, Vector3[] vertexNormals, int materialIndex) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.vertexNormals = vertexNormals;
        this.materialIndex = materialIndex;
    }
}
