package com.xtremelabs.robolectric.shadows;

import android.util.Base64;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(Base64.class)
public class ShadowBase64 {

  @Implementation
  public static String encodeToString(byte[] bytes, int mode) {
      return "";
  }

  @Implementation
  public static String encodeToString(byte[] input, int offset, int len, int flags) {
      return "";
  }

  @Implementation
  public static byte[] encode(byte[] input, int flags) {
      return new byte[0];
  }

  @Implementation
  public static byte[] encode(byte[] input, int offset, int len, int flags) {
      return new byte[0];
  }

  @Implementation
  public static byte[] decode(String str, int flags) {
      return new byte[0];
  }

  @Implementation
  public static byte[] decode(byte[] input, int flags) {
      return new byte[0];
  }

  @Implementation
  public static byte[] decode(byte[] input, int offset, int len, int flags) {
      return new byte[0];
  }
}
