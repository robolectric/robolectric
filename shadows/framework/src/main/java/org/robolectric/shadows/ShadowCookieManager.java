package org.robolectric.shadows;

import android.webkit.CookieManager;
import android.webkit.RoboCookieManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(CookieManager.class)
public class ShadowCookieManager {
  private static RoboCookieManager cookieManager;
  private boolean flushed;

  @Resetter
  public static void resetCookies() {
    cookieManager = null;
  }

  @Implementation
  protected static CookieManager getInstance() {
    if (cookieManager == null) {
      cookieManager = new RoboCookieManager();
    }
    return cookieManager;
  }
}