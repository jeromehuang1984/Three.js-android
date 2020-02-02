package edu.three.math;

import java.util.ArrayList;

public class MathTool {
    static public float PI = (float) Math.PI;
    static public float PI2 = (float) Math.PI * 2;
    static public float LN2 = (float) Math.log(2);
    static public float SQ2 = (float) Math.sqrt(2);
    static public float SQ3 = (float) Math.sqrt(3);
    static public float SQRT1_2 = (float) Math.sqrt(2)/2;
    static public float LOG2E = 1 / (float) Math.log(2);
    static public float DEG2RAD = (float) Math.PI / 180;
    static public float RAD2DEG = 180 / (float) Math.PI;

    static public int sign(long val) {
        if (val > 0) {
            return 1;
        } else if (val < 0) {
            return -1;
        }
        return 0;
    }
    static public int sign(float val) {
        if (val > 0) {
            return 1;
        } else if (val < 0) {
            return -1;
        }
        return 0;
    }

    static public float ceilPowerOfTwo(float value) {
        return (float) Math.pow(2, Math.ceil(Math.log(value) / LN2));
    }

    static public float floorPowerOfTwo(float value) {
        return (float) Math.pow(2, Math.floor(Math.log(value) / LN2));
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

    static public float max(float... intArray) {
        float max = Float.NEGATIVE_INFINITY;
        for (float i : intArray) {
            if (max < i) {
                max = i;
            }
        }
        return max;
    }

    static public char getMainField(Vector3 vector) {
        float max = MathTool.maxNoSign(vector.x, vector.y, vector.z);
        if (max == vector.x) {
            return 'x';
        } else if (max == vector.y) {
            return 'y';
        }
        return 'z';
    }

    static public float maxNoSign(float... floatArray) {
        float max = Float.NEGATIVE_INFINITY;
        int sign = 1;
        for (float i : floatArray) {
            if (max < Math.abs(i)) {
                sign = sign(i);
                max = Math.abs(i);
            }
        }
        return sign * max;
    }

    static public float min(float... intArray) {
        float min = Float.POSITIVE_INFINITY;
        for (float i : intArray) {
            if (min > i) {
                min = i;
            }
        }
        return min;
    }

    static public float roundAngle(float angle, float round) {
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

    static public int indexOf(float[] arr, float value) {
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

    static public float[] flatten(Vector3[] vertices) {
        float[] target = new float[vertices.length * 3];
        int count = 0;
        for(Vector3 vector : vertices) {
            target[count++] = vector.x;
            target[count++] = vector.y;
            target[count++] = vector.z;
        }
        return target;
    }

    // Clamp value to range <a, b>
    static public float clamp(float x, float a, float b) {
        return ( x < a ) ? a : ( ( x > b ) ? b : x );
    }

    static public float sqrt(float value) {
        return (float) Math.sqrt(value);
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

    static public void push(ArrayList lst, float... floatArr) {
        for (float item : floatArr) {
            lst.add(item);
        }
    }

    static public void push(ArrayList lst, int... floatArr) {
        for (int item : floatArr) {
            lst.add(item);
        }
    }

    static public float[] reverseArr(float[] arr) {
        float[] target = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            target[i] = arr[arr.length - 1 - i];
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

    static public Vector3[] offsetVertices(Vector3[] points, Vector3 deltaVector) {
        Vector3[] vertices = new Vector3[points.length];
        for (int i = 0; i < points.length; i++) {
            vertices[i] = points[i].clone().add(deltaVector);
        }
        return vertices;
    }

    static public int pointToBall(Vector3 point, Vector3 ballCenter, float radius) {
        float rr = radius * radius;
        float dd = point.distanceToSquared(ballCenter);
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
