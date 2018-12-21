package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.app.ResourcesManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(value = ResourcesManager.class, isInAndroidSdk = false, minSdk = KITKAT)
public class ShadowResourcesManager {
  @RealObject ResourcesManager realResourcesManager;

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(ResourcesManager.class, "sResourcesManager", null);
  }

  /**
   * Exposes {@link ResourcesManager#applyCompatConfigurationLocked(int, Configuration)}.
   */
  public boolean callApplyConfigurationToResourcesLocked(Configuration configuration,
      CompatibilityInfo compatibilityInfo) {
    return ReflectionHelpers.callInstanceMethod(realResourcesManager,
        "applyConfigurationToResourcesLocked",
        ClassParameter.from(Configuration.class, configuration),
        ClassParameter.from(CompatibilityInfo.class, compatibilityInfo));
  }
}