package org.robolectric.rap;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.S;

import android.app.Application;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowApplication;

/** This shadow tests the logic that emits resetters in RAP when a min/max sdk is specified */
@Implements(value = Application.class, minSdk = P, maxSdk = S)
public class ExtendedShadowApplication extends ShadowApplication {

  @Resetter
  public static void reset() {}
}
