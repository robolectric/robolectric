package org.robolectric.shadows;

import android.content.Context;
import android.webkit.CookieSyncManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(CookieSyncManager.class)
public class ShadowCookieSyncManager extends ShadowWebSyncManager {

  private static CookieSyncManager sRef;

  @Implementation
  protected static synchronized CookieSyncManager createInstance(Context ctx) {
    if (sRef == null) {
      sRef = Shadow.newInstanceOf(CookieSyncManager.class);
    }
    return sRef;
  }

  @Implementation
  protected static CookieSyncManager getInstance() {
    if (sRef == null) {
      throw new IllegalStateException("createInstance must be called first");
    }
    return sRef;
  }
}
