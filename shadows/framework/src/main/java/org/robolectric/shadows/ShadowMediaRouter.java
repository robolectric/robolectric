package org.robolectric.shadows;

import android.content.Context;
import android.media.MediaRouter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(MediaRouter.class)
public class ShadowMediaRouter {

  @Implementation
  protected void __constructor__(Context context) {}

  @Implementation
  public int getRouteCount() {
    return 0;
  }
}
