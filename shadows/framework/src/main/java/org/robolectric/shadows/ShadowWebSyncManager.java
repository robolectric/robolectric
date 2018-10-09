package org.robolectric.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "android.webkit.WebSyncManager")
public class ShadowWebSyncManager {
  protected boolean synced = false;

  @Implementation
  protected void sync() {
    synced = true;
  }

  public boolean synced() {
    return synced;
  }

  public void reset() {
    synced = false;
  }
}
