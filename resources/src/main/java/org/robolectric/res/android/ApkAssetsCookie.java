package org.robolectric.res.android;

public class ApkAssetsCookie {

  public static final int kInvalidCookie = -1;
  public static final ApkAssetsCookie K_INVALID_COOKIE = new ApkAssetsCookie(kInvalidCookie);

  private final int cookie;

  public ApkAssetsCookie(int cookie) {
    this.cookie = cookie;
  }

  public int get() {
    return cookie;
  }
}
