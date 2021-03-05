package edu.three.core;

import java.util.ArrayList;
import java.util.UUID;

import edu.three.math.Box3;
import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Sphere;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.objects.GroupItem;

public class Geometry extends EventDispatcher implements IGeometry {
    static long GeometryId = 0;
    public final String uuid = UUID.randomUUID().toString();
    public long id = GeometryId += 2;
    public String name;

    protected Box3 boundingBox;
    protected Sphere boundingSphere;
    public Vector3[] vertices;
    public ArrayList<Face3> faces = new ArrayList<>();
    public Vector2[][][] faceVertexUvs;
    protected int[] indices;

    public boolean normalsNeedUpdate = false;
    public boolean verticesNeedUpdate = false;
    public boolean colorsNeedUpdate;
    public boolean uvsNeedUpdate;
    public boolean groupsNeedUpdate;
    private Matrix4 _m1 = new Matrix4();
    private Object3D _obj = new Object3D();
    

    public Geometry applyMatrix(Matrix4 matrix) {
        Matrix3 normalMatrix = new Matrix3().getNormalMatrix(matrix);
        for (int i = 0; i < vertices.length; i++) {
            Vector3 vertex = vertices[i];
            vertex.applyMatrix4(matrix);
        }
        for (int i = 0; i < faces.size(); i++) {
            Face3 face = faces.get(i);
            face.normal.applyMatrix3(normalMatrix).normalize();
            for (int j = 0; j < face.vertexNormals.length; j++) {
                face.vertexNormals[j].applyMatrix3(normalMatrix).normalize();
            }
        }
        computeBoundingBox();
        computeBoundingSphere();
        verticesNeedUpdate = true;
        normalsNeedUpdate = true;
        return this;
    }

    public void computeBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new Box3();
        }
        boundingBox.setFromPoints(vertices);
    }

    public void computeBoundingSphere() {
        if ( boundingSphere == null ) {
            boundingSphere = new Sphere();
        }
        boundingSphere.setFromPoints( vertices );
    }

    public Geometry rotateX(double angle) {
        // rotate geometry around world x-axis
        _m1.makeRotationX(angle);
        applyMatrix(_m1);
        return this;
    }

    public Geometry rotateY(double angle) {
        // rotate geometry around world y-axis
        _m1.makeRotationY(angle);
        applyMatrix(_m1);
        return this;
    }

    public Geometry rotateZ(double angle) {
        // rotate geometry around world z-axis
        _m1.makeRotationZ(angle);
        applyMatrix(_m1);
        return this;
    }

    public Geometry translate(double x, double y, double z) {
        _m1.makeTranslation(x, y, z);
        applyMatrix(_m1);
        return this;
    }

    public Geometry scale(double x, double y, double z) {
        // scale geometry
        _m1.makeScale( x, y, z );
        this.applyMatrix( _m1 );
        return this;
    }

    public Geometry lookAt(Vector3 vector) {
//        _obj.lookAt(vector);
        _obj.updateMatrix();
        applyMatrix(_obj.matrix);
        return this;
    }

    public Geometry center() {
        computeBoundingBox();
        Vector3 offset = boundingBox.getCenter().negate();
        translate(offset.x, offset.y, offset.z);
        return this;
    }

    public Geometry normalize() {
        computeBoundingSphere();
        Vector3 center = boundingSphere.center;
        double radius = boundingSphere.radius;
        double s = radius == 0 ? 1 : 1f / radius;
        Matrix4 matrix4 = new Matrix4();
        matrix4.set(
            s, 0, 0, - s * center.x,
            0, s, 0, - s * center.y,
            0, 0, s, - s * center.z,
            0, 0, 0, 1
        );
        applyMatrix(matrix4);
        return this;
    }

    public void computeFaceNormals() {
        Vector3 cb = new Vector3(), ab = new Vector3();
        for(int i = 0; i < faces.size(); i++) {
            Face3 face = faces.get(i);
            Vector3 vA = vertices[face.a];
            Vector3 vB = vertices[face.b];
            Vector3 vC = vertices[face.c];

            cb.subVectors(vC, vB);
            ab.subVectors(vA, vB);
            cb.cross(ab);
            cb.normalize();
            face.normal.copy(cb);
        }
    }

    public void computeVertexNormals() {
        computeVertexNormals(true);
    }
    public void computeVertexNormals(boolean areaWeighted) {
        Vector3[] verticeArr = new Vector3[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            verticeArr[i] = new Vector3();
        }
        if (areaWeighted) {
            // vertex normals weighted by triangle areas
            // http://www.iquilezles.org/www/articles/normals/normals.htm
            Vector3 vA, vB, vC;
            Vector3 cb = new Vector3(), ab = new Vector3();
            for (int i = 0; i < faces.size(); i++) {
                Face3 face = faces.get(i);
                vA = vertices[face.a];
                vB = vertices[face.b];
                vC = vertices[face.c];

                cb.subVectors(vC, vB);
                ab.subVectors(vA, vB);
                cb.cross(ab);

                verticeArr[face.a].add(cb);
                verticeArr[face.b].add(cb);
                verticeArr[face.c].add(cb);
            }
        } else {
            computeFaceNormals();
            for (int i = 0; i < faces.size(); i++) {
                Face3 face = faces.get(i);
                verticeArr[face.a].add(face.normal);
                verticeArr[face.b].add(face.normal);
                verticeArr[face.c].add(face.normal);
            }
        }
        for (int i = 0; i < verticeArr.length; i++) {
            verticeArr[i].normalize();
        }

        for (int i = 0; i < faces.size(); i++) {
            Face3 face = faces.get(i);
            Vector3[] vertexNormals = face.vertexNormals;
            if (face.vertexNormals == null) {
                vertexNormals = new Vector3[3];
                vertexNormals[0] = verticeArr[face.a].clone();
                vertexNormals[1] = verticeArr[face.b].clone();
                vertexNormals[2] = verticeArr[face.c].clone();
                face.vertexNormals = vertexNormals;
            } else {
                vertexNormals[0].copy(verticeArr[face.a]);
                vertexNormals[1].copy(verticeArr[face.b]);
                vertexNormals[2].copy(verticeArr[face.c]);
            }
        }

        if (faces.size() > 0) {
            normalsNeedUpdate = true;
        }
    }

    private void addFace(int a, int b, int c, int materialIndex, float[] normals) {
        Vector3[] vertexNormals;
        if (normals == null) {
            vertexNormals = new Vector3[0];
        } else {
            vertexNormals = new Vector3[] {
                new Vector3().fromArray(normals, a*3),
                new Vector3().fromArray(normals, b*3),
                new Vector3().fromArray(normals, c*3),
            };
        }
        Face3 face = new Face3(a, b, c, vertexNormals, materialIndex);
        faces.add(face);
    }

    public Geometry fromBufferGeometry(BufferGeometry geometry) {
        int[] indices = null;
        if (geometry.getIndex() != null) {
            indices = geometry.getIndex().arrayInt;
        }
        float[] normals = null;
        if (geometry.normal != null) {
            normals = geometry.normal.arrayFloat;
        }
        float[] positions = geometry.position.arrayFloat;

        vertices = new Vector3[positions.length/3];
        for (int i = 0; i < positions.length; i += 3) {
            vertices[i/3] = new Vector3().fromArray(positions, i);
        }

        ArrayList<GroupItem> groups = geometry.getGroups();
        if (groups.size() > 0) {
            for (int i = 0; i < groups.size(); i++) {
                GroupItem group = groups.get(i);
                for (int j = group.start; j < group.start + group.count; j += 3) {
                    if (indices != null) {
                        addFace(indices[j], indices[j+1], indices[j+2], group.materialIndex, normals);
                    } else {
                        addFace(j, j+1, j+2, group.materialIndex, normals);
                    }
                }
            }
        } else {
            if (indices != null) {
                for (int i = 0; i < indices.length; i += 3) {
                    addFace(indices[i], indices[i+1], indices[i+2], 0, normals);
                }
            } else {
                for (int i = 0; i < positions.length / 3; i += 3) {
                    addFace(i, i+1, i+2, 0, normals);
                }
            }
        }
        computeFaceNormals();
        boundingBox = geometry.boundingBox.clone();
        boundingSphere = geometry.boundingSphere.clone();
        return this;
    }
}
