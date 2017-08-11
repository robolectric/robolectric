package org.robolectric.res.android;

import java.nio.ByteOrder;

public class Util {

  static final int SIZEOF_SHORT = 2;
  static final int SIZEOF_INT = 4;
  private static boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

  public static short dtohs(short v) {
    return littleEndian
        ? v
        : (short) ((v << 8) | (v >> 8));
  }

  public static int dtohl(int v) {
    return littleEndian
        ? v
        : (v << 24) | ((v << 8) & 0x00FF0000) | ((v >> 8) & 0x0000FF00) | (v >> 24);
  }
}
