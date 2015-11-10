package org.robolectric.shadows;

import android.webkit.CookieManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 * Shadow for {@code android.webkit.CookieManager}.
 */
@Implements(CookieManager.class)
public class ShadowCookieManager {
  private boolean flushed;

  @Resetter
  public static void resetCookies() {
  }

  @Implementation
  public static CookieManager getInstance() {
    return null;
  }
}
