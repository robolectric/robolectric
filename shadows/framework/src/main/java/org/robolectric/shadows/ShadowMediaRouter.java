package org.robolectric.shadows;

import android.media.MediaRouter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(MediaRouter.class)
public class ShadowMediaRouter {

  @Implementation
  public int getRouteCount() {
    return 0;
  }
}
