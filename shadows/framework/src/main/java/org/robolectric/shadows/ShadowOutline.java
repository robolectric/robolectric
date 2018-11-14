package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.graphics.Outline;
import android.graphics.Path;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = Outline.class, minSdk = LOLLIPOP)
public class ShadowOutline {

  @Implementation
  protected void setConvexPath(Path convexPath) {}
}