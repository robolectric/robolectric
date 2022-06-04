package org.robolectric.res.android;

import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import java.nio.ByteOrder;

public class Util {

  public static final boolean JNI_TRUE = true;
  public static final boolean JNI_FALSE = false;

  public static final int SIZEOF_SHORT = 2;
  public static final int SIZEOF_INT = 4;
  public static final int SIZEOF_CPTR = 4;
  private static boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

  private static final boolean DEBUG = false;

  static short dtohs(short v) {
    return littleEndian
        ? v
        : (short) ((v << 8) | (v >> 8));
  }

  static char dtohs(char v) {
    return littleEndian
        ? v
        : (char) ((v << 8) | (v >> 8));
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

  @FormatMethod
  static void ALOGD(@FormatString String message, Object... args) {
    if (DEBUG) {
      System.out.println("DEBUG: " + String.format(message, args));
    }
  }

  @FormatMethod
  static void ALOGW(@FormatString String message, Object... args) {
    System.out.println("WARN: " + String.format(message, args));
  }

  @FormatMethod
  public static void ALOGV(@FormatString String message, Object... args) {
    if (DEBUG) {
      System.out.println("VERBOSE: " + String.format(message, args));
    }
  }

  @FormatMethod
  public static void ALOGI(@FormatString String message, Object... args) {
    if (DEBUG) {
      System.out.println("INFO: " + String.format(message, args));
    }
  }

  @FormatMethod
  static void ALOGE(@FormatString String message, Object... args) {
    System.out.println("ERROR: " + String.format(message, args));
  }

  @FormatMethod
  static void LOG_FATAL_IF(boolean assertion, @FormatString String message, Object... args) {
    assert !assertion : String.format(message, args);
  }

  static void ATRACE_CALL() {
  }

  public static void ATRACE_NAME(String s) {
  }

  static boolean UNLIKELY(boolean b) {
    return b;
  }

  public static void CHECK(boolean b) {
    assert b;
  }

  static void logError(String s) {
    System.err.println(s);
  }

  static void logWarning(String s) {
    System.err.println("[WARN] " + s);
  }

  static String ReadUtf16StringFromDevice(char[] src, int len/*, std::string* out*/) {
    int i = 0;
    StringBuilder strBuf = new StringBuilder();
    while (src[i] != '\0' && len != 0) {
      char c = dtohs(src[i]);
      // utf16_to_utf8(&c, 1, buf, sizeof(buf));
      // out->append(buf, strlen(buf));
      strBuf.append(c);
      ++i;
      --len;
    }
    return strBuf.toString();
  }
}
