package edu.three.core;

import android.util.Log;

import java.util.ArrayList;

import edu.three.math.Box3;
import edu.three.math.Color;
import edu.three.math.Sphere;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.objects.GroupItem;

public class DirectGeometry {
    String TAG = getClass().getSimpleName();
    public Vector3[] vertices;
    public Vector3[] normals;
    public Color[] colors;
    public Vector2[] uvs;
    public Vector2[] uvs2;
    public ArrayList<GroupItem> groups;

    public Box3 boundingBox = null;
    public Sphere boundingSphere = null;

    // update flags
    public boolean verticesNeedUpdate;
    public boolean normalsNeedUpdate;
    public boolean colorsNeedUpdate;
    public boolean uvsNeedUpdate;
    public boolean groupsNeedUpdate;

    private void computeGroups(Geometry geometry) {
        GroupItem group = null;
        ArrayList<GroupItem> groups = new ArrayList<>();
        int materialIndex = -1;
        ArrayList<Face3> faces = geometry.faces;
        int i = 0;
        for (i = 0; i < faces.size(); i++) {
            Face3 face = faces.get(i);
            // materials
            if (face.materialIndex != materialIndex) {
                materialIndex = face.materialIndex;
                if (group != null) {
                    group.count = (i * 3) - group.start;
                    groups.add(group);
                }
                group = new GroupItem(i * 3, 0, materialIndex);
            }
        }
        if (group != null) {
            group.count = (i * 3) - group.start;
            groups.add(group);
        }
        this.groups = groups;
    }

    public DirectGeometry fromGeometry(Geometry geometry) {
        ArrayList<Face3> faces = geometry.faces;
        Vector3[] vertices = geometry.vertices;
        Vector2[][][] faceVertexUvs = geometry.faceVertexUvs;

        boolean hasFaceVertexUv = false;
        boolean hasFaceVertexUv2 = false;

        if ( vertices.length > 0 && faces.size() == 0 ) {
            Log.e(TAG, "THREE.DirectGeometry: Faceless geometries are not supported.");
        }

        this.vertices = new Vector3[faces.size() *3];
        this.normals = new Vector3[faces.size() *3];
        this.colors = new Color[faces.size() *3];
        int vCount = 0, nCount = 0, cCount = 0, uCount = 0, u2Count = 0;
        for (int i = 0; i < faces.size(); i++) {
            Face3 face = faces.get(i);
            this.vertices[vCount++] = vertices[face.a];
            this.vertices[vCount++] = vertices[face.b];
            this.vertices[vCount++] = vertices[face.c];
            Vector3[] vertexNormals = face.vertexNormals;
            if (vertexNormals.length == 3) {
                this.normals[nCount++] = vertexNormals[ 0 ];
                this.normals[nCount++] = vertexNormals[ 1 ];
                this.normals[nCount++] = vertexNormals[ 2 ];
            } else {
                this.normals[nCount++] = face.normal;
                this.normals[nCount++] = face.normal;
                this.normals[nCount++] = face.normal;
            }
            Color[] vertexColors = face.vertexColors;
            if ( vertexColors.length == 3 ) {
                this.colors[cCount++] = vertexColors[ 0 ];
                this.colors[cCount++] = vertexColors[ 1 ];
                this.colors[cCount++] = vertexColors[ 2 ];
            } else {
                Color color = new Color(face.color);
                this.colors[cCount++] = color;
                this.colors[cCount++] = color;
                this.colors[cCount++] = color;
            }
            if (hasFaceVertexUv) {
                Vector2[] vertexUvs = faceVertexUvs[ 0 ][ i ];
                if (vertexUvs != null) {
                    this.uvs[uCount++] = vertexUvs[ 0 ];
                    this.uvs[uCount++] = vertexUvs[ 1 ];
                    this.uvs[uCount++] = vertexUvs[ 2 ];
                } else {
                    Log.w(TAG, "THREE.DirectGeometry.fromGeometry(): Undefined vertexUv " + i);
                    this.uvs[uCount++] = new Vector2();
                    this.uvs[uCount++] = new Vector2();
                    this.uvs[uCount++] = new Vector2();
                }
            }
            if (hasFaceVertexUv2) {
                Vector2[] vertexUvs = faceVertexUvs[ 1 ][ i ];
                if (vertexUvs != null) {
                    this.uvs2[u2Count++] = vertexUvs[ 0 ];
                    this.uvs2[u2Count++] = vertexUvs[ 1 ];
                    this.uvs2[u2Count++] = vertexUvs[ 2 ];
                } else {
                    Log.w(TAG, "THREE.DirectGeometry.fromGeometry(): Undefined vertexUv2 " + i);
                    this.uvs[u2Count++] = new Vector2();
                    this.uvs[u2Count++] = new Vector2();
                    this.uvs[u2Count++] = new Vector2();
                }
            }
        }
        computeGroups(geometry);
        this.verticesNeedUpdate = geometry.verticesNeedUpdate;
        this.normalsNeedUpdate = geometry.normalsNeedUpdate;
        this.colorsNeedUpdate = geometry.colorsNeedUpdate;
        this.uvsNeedUpdate = geometry.uvsNeedUpdate;
        this.groupsNeedUpdate = geometry.groupsNeedUpdate;

        if ( geometry.boundingSphere != null ) {
            this.boundingSphere = geometry.boundingSphere.clone();
        }

        if ( geometry.boundingBox != null ) {
            this.boundingBox = geometry.boundingBox.clone();
        }

        return this;
    }
}
