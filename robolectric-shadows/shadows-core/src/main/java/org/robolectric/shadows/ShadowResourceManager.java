package org.robolectric.shadows;

import android.app.ResourcesManager;

import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

import static android.os.Build.VERSION_CODES.KITKAT;

@Implements(value = ResourcesManager.class, isInAndroidSdk = false, minSdk = KITKAT)
public class ShadowResourceManager {

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(ResourcesManager.class, "sResourcesManager", null);
  }
}