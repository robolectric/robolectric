package org.robolectric.bytecode;

public class AClassToForget {
    public String memorableMethod() {
        return "get this!";
    }

    public String forgettableMethod() {
        return "shouldn't get this!";
    }

    public static String memorableStaticMethod() {
        return "yess?";
    }

    public static String forgettableStaticMethod() {
        return "noooo!";
    }

    public static int intReturningMethod() {
        return 1;
    }

    public static int[] intArrayReturningMethod() {
        return new int[0];
    }

    public static long longReturningMethod() {
        return 1;
    }

    public static long[] longArrayReturningMethod() {
        return new long[0];
    }

    public static byte byteReturningMethod() {
        return 0;
    }

    public static byte[] byteArrayReturningMethod() {
        return new byte[0];
    }
}
