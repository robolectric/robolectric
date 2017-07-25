package org.robolectric.res.android;

public class Formatter {
  public static StringBuilder toHex(int value, int digits) {
    StringBuilder sb = new StringBuilder(digits + 2);
    sb.append("0x");
    String hex = Integer.toHexString(value);
    for (int i = hex.length(); i < digits; i++) {
      sb.append("0");
    }
    return sb.append(hex);
  }
}
