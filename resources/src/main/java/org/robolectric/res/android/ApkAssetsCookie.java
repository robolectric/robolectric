package org.robolectric.res.android;

class ApkAssetsCookie {

  private int cookie;

  public ApkAssetsCookie(int cookie) {
    this.cookie = cookie;
  }

  public int get() {
    return cookie;
  }

  public void set(int cookie) {
    this.cookie = cookie;
  }
}
