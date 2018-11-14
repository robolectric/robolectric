package org.robolectric.res.android;

public class ApkAssetsCookie {

  public static final int kInvalidCookie = -1;
  public static final ApkAssetsCookie K_INVALID_COOKIE = new ApkAssetsCookie(kInvalidCookie);

  // hey memory/gc optimization!
  private static final ApkAssetsCookie[] PREBAKED = new ApkAssetsCookie[256];
  static {
    for (int i = 0; i < PREBAKED.length; i++) {
      PREBAKED[i] = new ApkAssetsCookie(i);
    }
  }

  public static ApkAssetsCookie forInt(int cookie) {
    if (cookie == kInvalidCookie) {
      return K_INVALID_COOKIE;
    }
    return PREBAKED[cookie];
  }

  private final int cookie;

  private ApkAssetsCookie(int cookie) {
    this.cookie = cookie;
  }

  public int intValue() {
    return cookie;
  }
}
