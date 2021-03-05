package edu.three.math;

import java.util.ArrayList;
import java.util.List;

import edu.three.cameras.Camera;
import edu.three.control.Screen;

public class MathTool {
    static public float PI =  (float)Math.PI;
    static public double PI2 =  Math.PI * 2;
    static public double PI_d2 =  Math.PI * 0.5;
    static public float LN2 =  (float)Math.log(2);
    static public double SQ2 =  Math.sqrt(2);
    static public double SQ3 =  Math.sqrt(3);
    static public double SQRT1_2 =  Math.sqrt(2)/2;
    static public double LOG2E = 1 /  Math.log(2);
    static public double DEG2RAD =  Math.PI / 180;
    static public double RAD2DEG = 180 /  Math.PI;
    static public double EPSILON = 2.22045e-16f;

    static public int sign(long val) {
        if (val > 0) {
            return 1;
        } else if (val < 0) {
            return -1;
        }
        return 0;
    }
    static public int sign(double val) {
        if (val > 0) {
            return 1;
        } else if (val < 0) {
            return -1;
        }
        return 0;
    }

    static public double ceilPowerOfTwo(double value) {
        return  Math.pow(2, Math.ceil(Math.log(value) / LN2));
    }

    static public double floorPowerOfTwo(double value) {
        return  Math.pow(2, Math.floor(Math.log(value) / LN2));
    }

    static public boolean isPowerOfTwo(int value) {
        return ( value & ( value - 1 ) ) == 0 && value != 0;
    }

    static public int max(int... intArray) {
        int max = -Integer.MAX_VALUE;
        for (int i : intArray) {
            if (max < i) {
                max = i;
            }
        }
        return max;
    }

    static public int min(int... intArray) {
        int min = Integer.MAX_VALUE;
        for (int i : intArray) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

    static public double max(double... intArray) {
        double max = Float.NEGATIVE_INFINITY;
        for (double i : intArray) {
            if (max < i) {
                max = i;
            }
        }
        return max;
    }

    static public char getMainField(Vector3 vector) {
        double max = MathTool.maxNoSign(vector.x, vector.y, vector.z);
        if (max == vector.x) {
            return 'x';
        } else if (max == vector.y) {
            return 'y';
        }
        return 'z';
    }

    static public double maxNoSign(double... floatArray) {
        double max = Float.NEGATIVE_INFINITY;
        int sign = 1;
        for (double i : floatArray) {
            if (max < Math.abs(i)) {
                sign = sign(i);
                max = Math.abs(i);
            }
        }
        return sign * max;
    }

    static public double min(double... intArray) {
        double min = Float.POSITIVE_INFINITY;
        for (double i : intArray) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

    static public double roundAngle(double angle, double round) {
        return Math.round(angle / round) * round;
    }

    static public<T> int indexOf(T[] arr, T value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }

    static public int indexOfStr(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(value) ) {
                return i;
            }
        }
        return -1;
    }

    static public int indexOf(double[] arr, double value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }

    static public int indexOf(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }

    static public int indexOf(char[] arr, char value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return i;
            }
        }
        return -1;
    }

//    static public<T> ArrayList<T> slice(T[] arr, int start, int end) {
//        ArrayList target = new ArrayList();
//        for (int i = start; i < end; i++) {
//            target.add(arr[i]);
//        }
//        return target;
//    }

    static public int[] slice(int[] arr, int start, int end) {
        int[] target = new int[end - start];
        int count = 0;
        for (int i = start; i < end; i++) {
            target[count++] = arr[i];
        }
        return target;
    }
    static public Vector3[] slice(Vector3[] arr, int start, int end) {
        Vector3[] target = new Vector3[end - start];
        int count = 0;
        for (int i = start; i < end; i++) {
            target[count++] = arr[i];
        }
        return target;
    }

    static public int[] slice2(int[] dst, int dstStart,int[] arr, int start, int end) {
        int count = 0;
        for (int i = start; i < end; i++) {
            dst[dstStart + count++] = arr[i];
        }
        return dst;
    }

    static public int[] splice(int[] arr, int index, int deleteCount) {
        int[] target = new int[arr.length - deleteCount];
        int count = 0;
        for (int i = 0; i < arr.length; i++) {
            if (i >= index && i < index + deleteCount) {
                continue;
            }
            target[count++] = arr[i];
        }
        return target;
    }

    static public String join(ArrayList<String> lst) {
        StringBuilder builder = new StringBuilder();
        for (String str : lst) {
            builder.append(str);
            builder.append(", ");
        }
        return builder.toString();
    }

    static public String join(String[] lst) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lst.length; i++) {
            builder.append(lst[i]);
            if (i < lst.length - 1) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    static public float[] flatten(List<Vector3> vertices) {
        float[] target = new float[vertices.size() * 3];
        int count = 0;
        for(Vector3 vector : vertices) {
            target[count++] = (float)vector.x;
            target[count++] = (float)vector.y;
            target[count++] = (float)vector.z;
        }
        return target;
    }

    static public float[] flatten(Vector3[] vertices) {
        float[] target = new float[vertices.length * 3];
        int count = 0;
        for(Vector3 vector : vertices) {
            target[count++] = (float)vector.x;
            target[count++] = (float)vector.y;
            target[count++] = (float)vector.z;
        }
        return target;
    }

    static public float[] flatten(Vector2[] vertices) {
        float[] target = new float[vertices.length * 3];
        int count = 0;
        for(Vector2 vector : vertices) {
            target[count++] = (float)vector.x;
            target[count++] = (float)vector.y;
        }
        return target;
    }

    //已知一个点二维坐标（x, y)，由点（0，0）到点（x, y) 记为向量 a, 求向量 a 与 x 轴的夹角。
    //其中约定从一个向量 (1, 0) 从旋转到 (0, 1) 为正的90度旋转，而任意一个向量从y轴到x轴为负的90度旋转
    public static double getAngle(double x, double y) {
        double a =  Math.atan2(y, x);
        double ret = a * 180 / MathTool.PI; //弧度转角度，方便调试
        if (ret > 360) {
            ret -= 360;
        }
        if (ret < 0) {
            ret += 360;
        }
        return ret;
    }

    static public double getMinNum(ArrayList<Double> numArr) {
        double min = Double.POSITIVE_INFINITY;
        for (double num : numArr) {
            if (min > num)
                min = num;
        }
        return min;
    }

//    public static PointXY transToScreenCoord(Vector3 vector, Camera camera, Screen screen) {
//        PointXY screenCoord = new PointXY();
//        Vector3 v = vector.clone().project(camera);
//        screenCoord.x = (float)(0.5 + v.x / 2) * screen.width;
//        screenCoord.y = (float)(0.5 - v.y / 2) * screen.height;
//        return screenCoord;
//    }

    static public Vector2[] vector3s_vector2s(Vector3[] vectors) {
        Vector2[] target = new Vector2[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            target[i] = new Vector2(vectors[i].x, vectors[i].y);
        }
        return target;
     }

    static public Vector3[] vector2s_vector3s(Vector2[] vectors) {
        Vector3[] target = new Vector3[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            target[i] = new Vector3(vectors[i].x, vectors[i].y, 0);
        }
        return target;
    }

    // Clamp value to range <a, b>
    static public double clamp(double x, double a, double b) {
        return ( x < a ) ? a : ( ( x > b ) ? b : x );
    }
    static public double lerp(double fromVal, double toVal, double alpha) {
        return fromVal + (toVal - fromVal) * alpha;
    }

    static public double sqrt(double value) {
        return  Math.sqrt(value);
    }

    static public int[] concat(int[] first, int[] second) {
        if (first == null) {
            return second;
        }
        int[] target = new int[first.length + second.length];
        System.arraycopy(first, 0, target, 0, first.length);
        System.arraycopy(second, 0, target, first.length, second.length);
        return target;
    }

    static public Vector2[] concat(Vector2[] first, Vector2[] second) {
        if (first == null) {
            return second;
        }
        Vector2[] target = new Vector2[first.length + second.length];
        System.arraycopy(first, 0, target, 0, first.length);
        System.arraycopy(second, 0, target, first.length, second.length);
        return target;
    }

    static public void concat(ArrayList dst, ArrayList... lstArr) {
        for (ArrayList item : lstArr) {
            dst.addAll(item);
        }
    }

    static public<T> void push2(ArrayList lst, T...arr) {
        for (T item : arr) {
            lst.add(item);
        }
    }

    static public void push(ArrayList lst, double... floatArr) {
        for (double item : floatArr) {
            lst.add((float)item);
        }
    }

    static public void push(ArrayList lst, int... intArr) {
        for (int item : intArr) {
            lst.add(item);
        }
    }

    static public double[] reverseArr(double[] arr) {
        double[] target = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            target[i] = arr[arr.length - 1 - i];
        }
        return target;
    }

    static public Vector2[] reverseArr(Vector2[] arr) {
        Vector2[] target = new Vector2[arr.length];
        for (int i = 0; i < arr.length; i++) {
            target[i] = arr[arr.length - 1 - i];
        }
        return target;
    }

    static public double[] toArrayDouble(ArrayList<Double> arrayList) {
        double[] target = new double[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            target[i] = arrayList.get(i);
        }
        return target;
    }

    static public float[] toArrayFloat(ArrayList<Float> arrayList) {
        float[] target = new float[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            target[i] = arrayList.get(i);
        }
        return target;
    }

    static public Vector3[] toArrayVector(ArrayList<Vector3> arrayList) {
        Vector3[] target = new Vector3[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            target[i] = arrayList.get(i);
        }
        return target;
    }

    static public int[] toArrayInt(ArrayList<Integer> arrayList) {
        int[] target = new int[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            target[i] = arrayList.get(i);
        }
        return target;
    }

    static public ArrayList<Integer> toArrayList(int[] arr) {
        ArrayList<Integer> target = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            target.add(arr[i]);
        }
        return target;
    }
    static public ArrayList<Vector3> toArrayList(Vector3[] arr) {
        ArrayList<Vector3> target = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            target.add(arr[i]);
        }
        return target;
    }

    static public Vector3[] offsetVertices(Vector3[] points, Vector3 deltaVector) {
        Vector3[] vertices = new Vector3[points.length];
        for (int i = 0; i < points.length; i++) {
            vertices[i] = points[i].clone().add(deltaVector);
        }
        return vertices;
    }

    static public int pointToBall(Vector3 point, Vector3 ballCenter, double radius) {
        double rr = radius * radius;
        double dd = point.distanceToSquared(ballCenter);
        if (Math.abs(dd - rr ) < 0.1f) {
            return  1; //on sphere's surface
        }
        if (dd < radius * radius) {
            return 0; //in ball
        }
        return 2;   //out of ball
    }

    static public int[] mergeArray(int[] src, int[] dst) {
        ArrayList<Integer> target = toArrayList(dst);
        for (int i = 0; i < src.length; i++) {
            if (target.indexOf(src[i]) < 0) {
                target.add(src[i]);
            }
        }
        return toArrayInt(target);
    }

    static public ArrayList intersectArrayList(ArrayList first, ArrayList second) {
        ArrayList target = new ArrayList();
        for (int i = 0; i < first.size(); i++) {
            if (second.indexOf(first.get(i)) >= 0) {
                target.add(first.get(i));
            }
        }
        return target;
    }
}
