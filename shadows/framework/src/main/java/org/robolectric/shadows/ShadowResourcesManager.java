package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.ResourcesManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;

@Implements(value = ResourcesManager.class, isInAndroidSdk = false, minSdk = KITKAT)
public class ShadowResourcesManager {
  @RealObject ResourcesManager realResourcesManager;

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(ResourcesManager.class, "sResourcesManager", null);
  }

  @ForType(ResourcesManager.class)
  private interface _ResourcesManager_ {
    boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat);
  }

  /**
   * Exposes {@link ResourcesManager#applyCompatConfigurationLocked(int, Configuration)}.
   */
  public boolean callApplyConfigurationToResourcesLocked(Configuration configuration,
      CompatibilityInfo compatibilityInfo) {
    return reflector(_ResourcesManager_.class, realResourcesManager)
        .applyConfigurationToResourcesLocked(configuration, compatibilityInfo);
  }
}