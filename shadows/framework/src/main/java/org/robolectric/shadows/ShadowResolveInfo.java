package org.robolectric.shadows;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Utilities for {@link ResolveInfo}. */
// TODO: Create a ResolveInfoBuilder in androidx and migrate factory methods there.
@Implements(ResolveInfo.class)
public class ShadowResolveInfo {

  @RealObject ResolveInfo realObject;

  /**
   * Creates a {@link ResolveInfo}.
   *
   * @param displayName Display name.
   * @param packageName Package name.
   * @return Resolve info instance.
   */
  public static ResolveInfo newResolveInfo(String displayName, String packageName) {
    return newResolveInfo(displayName, packageName, null);
  }

  /**
   * Creates a {@link ResolveInfo}.
   *
   * @param displayName Display name.
   * @param packageName Package name.
   * @param activityName Activity name.
   * @return Resolve info instance.
   */
  public static ResolveInfo newResolveInfo(String displayName, String packageName, String activityName) {
    ResolveInfo resInfo = new ResolveInfo();
    ActivityInfo actInfo = new ActivityInfo();
    actInfo.applicationInfo = new ApplicationInfo();
    actInfo.packageName = packageName;
    actInfo.applicationInfo.packageName = packageName;
    actInfo.name = activityName;
    resInfo.activityInfo = actInfo;
    resInfo.nonLocalizedLabel = displayName;
    return resInfo;
  }

  /**
   * Sets the value returned by {@link #loadLabel}.
   *
   * @param l Label.
   * @deprecated Just use {@link ResolveInfo#nonLocalizedLabel}.
   */
  @Deprecated
  public void setLabel(String l) {
    realObject.nonLocalizedLabel = l;
  }
}
