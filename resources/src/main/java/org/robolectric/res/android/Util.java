package org.robolectric.res.android;

import java.nio.ByteOrder;

public class Util {

  static final int SIZEOF_SHORT = 2;
  public static final int SIZEOF_INT = 4;
  private static boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

  private static final boolean DEBUG = false;

  static short dtohs(short v) {
    return littleEndian
        ? v
        : (short) ((v << 8) | (v >> 8));
  }

  static int dtohl(int v) {
    return littleEndian
        ? v
        : (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00) | (v >> 24);
  }

  static short htods(short v) {
    return littleEndian
        ? v
        : (short) ((v << 8) | (v >> 8));
  }

  static int htodl(int v) {
    return littleEndian
        ? v
        : (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00) | (v >> 24);
  }

  public static boolean isTruthy(int i) {
    return i != 0;
  }

  public static boolean isTruthy(Object o) {
    return o != null;
  }

  static void ALOGD(String message, Object... args) {
    if (DEBUG) {
      System.out.println("DEBUG: " + String.format(message, args));
    }
  }

  static void ALOGW(String message, Object... args) {
    System.out.println("WARN: " + String.format(message, args));
  }

  public static void ALOGV(String message, Object... args) {
    if (DEBUG) {
      System.out.println("VERBOSE: " + String.format(message, args));
    }
  }

  public static void ALOGI(String message, Object... args) {
    if (DEBUG) {
      System.out.println("INFO: " + String.format(message, args));
    }
  }

  static void ALOGE(String message, Object... args) {
    System.out.println("ERROR: " + String.format(message, args));
  }

  static void LOG_FATAL_IF(boolean assertion, String message, Object... args) {
    assert !assertion : String.format(message, args);
  }

  static void ATRACE_CALL() {
  }
}
