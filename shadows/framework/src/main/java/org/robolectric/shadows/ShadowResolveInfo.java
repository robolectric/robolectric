package org.robolectric.shadows;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;

/** Utilities for {@link ResolveInfo}. */
// TODO: Create a ResolveInfoBuilder in androidx and migrate factory methods there.
public class ShadowResolveInfo {

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
}
