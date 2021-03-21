package edu.three.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import edu.three.math.Box3;
import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Sphere;
import edu.three.math.Vector3;
import edu.three.objects.GroupItem;

public class BufferGeometry extends EventDispatcher implements IGeometry {
    static long BufferGeometryId = 1;
    public String uuid = UUID.randomUUID().toString();
    public long id = BufferGeometryId += 2;
    public String name = "";

    private BufferAttribute index = null;
    public BufferAttribute position;
    public BufferAttribute normal;
    public BufferAttribute uv;
    public BufferAttribute color;
    public ArrayList<BufferAttribute> morphAttributes = new ArrayList<>();
    public HashMap<String, ArrayList<BufferAttribute>> morphAttributeMap = new HashMap<>();
    private HashMap<String, BufferAttribute> attributes = new HashMap<>();

    public Box3 boundingBox = new Box3();
    public Sphere boundingSphere = new Sphere();

    public int drawRangeStart = 0;
    public int drawRangeCount = Integer.MAX_VALUE;

    public String userData; // json object string

    private Matrix4 _m1 = new Matrix4();
    private Object3D _obj = new Object3D();
    private Vector3 _offset = new Vector3();
    private Vector3 _vector = new Vector3();
    private Box3 _box = new Box3();
    private ArrayList<GroupItem> groups = new ArrayList<>();

    public ArrayList<BufferAttribute> getNonNullAttributes() {
        ArrayList<BufferAttribute> ret = new ArrayList<>();
        ret.add(position);
        if (normal != null) ret.add(normal);
        if (uv != null) ret.add(uv);
        if (color != null) ret.add(color);

        Iterator iter = attributes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, BufferAttribute> entry = (Map.Entry<String, BufferAttribute>) iter.next();
            String name = entry.getKey();
            BufferAttribute value = entry.getValue();
            ret.add(value);
        }
        return ret;
    }

    public HashMap<String, BufferAttribute> getAttributesTotal() {
        HashMap<String, BufferAttribute> ret = new HashMap<>();
        ret.putAll(attributes);
        ret.put("position", position);
        if (index != null) {
            ret.put("index", index);
        }
        if (color != null) {
            ret.put("color", color);
        }
        if (normal != null) {
            ret.put("normal", normal);
        }
        if (uv != null) {
            ret.put("uv", uv);
        }
        return ret;
    }

    public BufferAttribute getIndex() {
        return index;
    }

    public void setIndex(BufferAttribute index) {
        this.index = index;
    }

    public BufferAttribute getPosition() {
        return position;
    }

    public void setPosition(BufferAttribute position) {
        this.position = position;
    }

    public BufferAttribute getNormal() {
        return normal;
    }

    public void setNormal(BufferAttribute normal) {
        this.normal = normal;
    }

    public BufferAttribute getColor() {
        return color;
    }

    public void setColor(BufferAttribute color) {
        this.color = color;
    }

    public void setDrawRange(int start, int count) {
        drawRangeStart = start;
        drawRangeCount = count;
    }

    public void addAttribute(String name, BufferAttribute attribute) {
        if (attribute != null) {
            if (attribute.name.equals("index")) {
                setIndex(attribute);
            } else {
                attributes.put(name, attribute);
            }
        }
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public BufferAttribute getAttribute(String name) {
        return attributes.get(name);
    }

    private BufferGeometry applyMatrix(Matrix4 matrix) {
        if (position != null) {
            matrix.applyToBufferAttribute(position);
            position.setNeedsUpdate(true);
        }

        if (normal != null) {
            Matrix3 normalMatrix = new Matrix3().getNormalMatrix(matrix);
            normalMatrix.applyToBufferAttribute(normal);
            normal.setNeedsUpdate(true);
        }

        BufferAttribute tangent = attributes.get("tangent");
        if (tangent != null) {
            Matrix3 normalMatrix = new Matrix3().getNormalMatrix(matrix);
            // Tangent is vec4, but the '.w' component is a sign value (+1/-1).
            normalMatrix.applyToBufferAttribute(tangent);
            tangent.setNeedsUpdate(true);
        }
        computeBoundingBox();
        computeBoundingSphere();
        return this;
    }

    public void computeBoundingBox() {
        if (position != null) {
            boundingBox.setFromBufferAttribute(position);
        } else {
            boundingBox.makeEmpty();
        }
    }

    public void computeBoundingSphere() {
        if (position != null) {
            Vector3 center = boundingSphere.center;
            _box.setFromBufferAttribute(position);

            center.copy(_box.getCenter());

            double maxRadiusSq = 0;
            for (int i = 0; i < position.getCount(); i++) {
                _vector.fromBufferAttribute(position, i);
                maxRadiusSq = Math.max(maxRadiusSq, center.distanceToSquared(_vector));
            }

            boundingSphere.radius = (double) Math.sqrt( maxRadiusSq );
        }
    }

    public void addGroup(int start, int count, int materialIndex) {
        groups.add(new GroupItem(start, count, materialIndex));
    }

    public void clearGroups() {
        groups.clear();
    }

    public ArrayList<GroupItem> getGroups() {
        return groups;
    }

    public BufferGeometry rotateX(double angle) {
        // rotate geometry around world x-axis
        _m1.makeRotationX( angle );

        this.applyMatrix( _m1 );
        return this;
    }

    public BufferGeometry rotateY(double angle) {
        // rotate geometry around world y-axis
        _m1.makeRotationY( angle );

        this.applyMatrix( _m1 );
        return this;
    }

    public BufferGeometry rotateZ(double angle) {
        // rotate geometry around world z-axis
        _m1.makeRotationZ( angle );

        this.applyMatrix( _m1 );
        return this;
    }

    public BufferGeometry translate(double x, double y, double z) {
        _m1.makeTranslation(x, y, z);
        applyMatrix(_m1);
        return this;
    }

    public BufferGeometry scale(double x, double y, double z) {
        _m1.makeScale(x, y, z);
        applyMatrix(_m1);
        return this;
    }

    public BufferGeometry lookAt(Vector3 vector) {
        _obj.lookAt(vector);
        _obj.updateMatrix();
        applyMatrix(_obj.matrix);
        return this;
    }

    public BufferGeometry center() {
        computeBoundingBox();
        _offset = boundingBox.getCenter().negate();
        translate(_offset.x, _offset.y, _offset.z);
        return this;
    }

    public BufferGeometry setFromObject(Object3D object) {

        return null;
    }

    public void updateFromObject(Object3D object) {

    }

    public BufferGeometry setFromPoints(Vector3[] points) {
        float[] position = new float[points.length * 3];
        int count = 0;
        for (int i = 0; i < points.length; i++) {
            Vector3 point = points[i];
            position[count++] = (float)point.x;
            position[count++] = (float)point.y;
            position[count++] = (float)point.z;
        }
        this.position = new BufferAttribute().setArray(position);
        return this;
    }

    public String log(int[] arr) {
        String ret = "";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + " ";
        }
        return ret;
    }

    public String log(double[] arr) {
        String ret = "";
        for (int i = 0; i < arr.length; i++) {
            ret += arr[i] + " ";
        }
        return ret;
    }

    public BufferGeometry fromGeometry(Geometry geometrySrc) {
        DirectGeometry geometry = new DirectGeometry().fromGeometry(geometrySrc);
        float[] positions = new float[geometry.vertices.length * 3];
        position = new BufferAttribute(positions, 3).copyVector3sArray(geometry.vertices).setName("position");
        if (geometry.normals.length > 0) {
            float[] normals = new float[geometry.normals.length * 3];
            normal = new BufferAttribute(normals, 3).copyVector3sArray(geometry.normals).setName("normal");
        }
        if (geometry.colors != null && geometry.colors.length > 0) {
            float[] colors = new float[geometry.colors.length * 3];
            color = new BufferAttribute(colors, 3).copyColorsArray(geometry.colors).setName("color");
        }
        if (geometry.uvs != null && geometry.uvs.length > 0) {
            float[] uvs = new float[geometry.uvs.length * 2];
            uv = new BufferAttribute(uvs, 2).copyVector2sArray(geometry.uvs).setName("uv");
        }
        if (geometry.uvs2 != null && geometry.uvs2.length > 0) {
            float[] uvs2 = new float[geometry.uvs2.length * 2];
            addAttribute("uv2", new BufferAttribute(uvs2, 2).copyVector2sArray(geometry.uvs2));
        }
        // groups
        this.groups = geometry.groups;

        if ( geometry.boundingSphere != null ) {
            this.boundingSphere = geometry.boundingSphere.clone();
        }

        if ( geometry.boundingBox != null ) {
            this.boundingBox = geometry.boundingBox.clone();
        }

        return this;
    }

    public void computeVertexNormals() {
        float[] positions = position.arrayFloat;
        if (normal == null) {
            normal = new BufferAttribute(new float[positions.length], 3);
        }
        float[] normals = normal.arrayFloat;
        for (int i = 0; i < normals.length; i++) {
            normals[i] = 0;
        }
        int vA, vB, vC;
        Vector3 pA = new Vector3(), pB = new Vector3(), pC = new Vector3();
        Vector3 cb = new Vector3(), ab = new Vector3();
        if (index != null) {
            int[] indices = index.arrayInt;
            for (int i = 0; i < index.getCount(); i += 3) {
                vA = indices[i] * 3;
                vB = indices[i + 1] * 3;
                vC = indices[i + 2] * 3;

                pA.fromArray(positions, vA);
                pB.fromArray(positions, vB);
                pC.fromArray(positions, vC);

                cb.subVectors(pC, pB);
                ab.subVectors(pA, pB);
                cb.cross(ab);

                normals[vA] += cb.x;
                normals[vA + 1] += cb.y;
                normals[vA + 2] += cb.z;

                normals[vB] += cb.x;
                normals[vB + 1] += cb.y;
                normals[vB + 2] += cb.z;

                normals[vC] += cb.x;
                normals[vC + 1] += cb.y;
                normals[vC + 2] += cb.z;
            }
        } else {
            // non-indexed elements (unconnected triangle soup)
            for ( int i = 0; i < positions.length; i += 9 ) {

                pA.fromArray( positions, i );
                pB.fromArray( positions, i + 3 );
                pC.fromArray( positions, i + 6 );

                cb.subVectors( pC, pB );
                ab.subVectors( pA, pB );
                cb.cross( ab );

                normals[ i ] = (float)cb.x;
                normals[ i + 1 ] = (float)cb.y;
                normals[ i + 2 ] = (float)cb.z;

                normals[ i + 3 ] = (float)cb.x;
                normals[ i + 4 ] = (float)cb.y;
                normals[ i + 5 ] = (float)cb.z;

                normals[ i + 6 ] = (float)cb.x;
                normals[ i + 7 ] = (float)cb.y;
                normals[ i + 8 ] = (float)cb.z;

            }

        }

        normalizeNormals();
        normal.setNeedsUpdate(true);
    }

    public void normalizeNormals() {
        if (normal != null) {
            Vector3 vector = new Vector3();
            for ( int i = 0; i < normal.getCount(); i ++ ) {

                vector.x = normal.getX( i );
                vector.y = normal.getY( i );
                vector.z = normal.getZ( i );

                vector.normalize();

                normal.setXYZ( i, vector.x, vector.y, vector.z );
            }
        }
    }
}
