package org.robolectric.shadows;

import android.graphics.Path;
import android.graphics.Outline;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implementation;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Shadow for {@link android.graphics.Outline}.
 */
@Implements(value = Outline.class, minSdk = LOLLIPOP)
public class ShadowOutline {

  @Implementation
  public void setConvexPath(Path convexPath) {
  }
}