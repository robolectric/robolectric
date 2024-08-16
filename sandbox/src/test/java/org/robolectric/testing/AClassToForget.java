package org.robolectric.testing;

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

  public static long longReturningMethod(String str, int i, long l) {
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

  public static float floatReturningMethod() {
    return 0f;
  }

  public static float[] floatArrayReturningMethod() {
    return new float[0];
  }

  public static double doubleReturningMethod() {
    return 0;
  }

  public static double[] doubleArrayReturningMethod() {
    return new double[0];
  }

  public static short shortReturningMethod() {
    return 0;
  }

  public static short[] shortArrayReturningMethod() {
    return new short[0];
  }

  public static void voidReturningMethod() {}
}
