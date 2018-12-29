package org.robolectric.shadows;

import android.content.ContentProviderClient;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

@Implements(ContentProviderClient.class)
public class ShadowContentProviderClient {
  @RealObject private ContentProviderClient realContentProviderClient;

  private boolean released;

  @Implementation
  protected boolean release() {
    synchronized (this) {
      released = true;
    }
    return Shadow.directlyOn(realContentProviderClient, ContentProviderClient.class).release();
  }

  public boolean isStable() {
    return ReflectionHelpers.getField(realContentProviderClient, "mStable");
  }

  public boolean isReleased() {
    return released;
  }
}
