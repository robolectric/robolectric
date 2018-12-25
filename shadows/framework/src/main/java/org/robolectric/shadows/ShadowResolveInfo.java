package org.robolectric.shadows;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;

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

  /**
   * Copies {@link ResolveInfo}.
   *
   * <p>Note that this is shallow copy as performed by the copy constructor existing in API 17.
   */
  public static ResolveInfo newResolveInfo(ResolveInfo orig) {
    ResolveInfo copy;
    if (Build.VERSION.SDK_INT >= 17) {
      copy = new ResolveInfo(orig);
    } else {
      copy = new ResolveInfo();
      copy.activityInfo = orig.activityInfo;
      copy.serviceInfo = orig.serviceInfo;
      copy.filter = orig.filter;
      copy.priority = orig.priority;
      copy.preferredOrder = orig.preferredOrder;
      copy.match = orig.match;
      copy.specificIndex = orig.specificIndex;
      copy.labelRes = orig.labelRes;
      copy.nonLocalizedLabel = orig.nonLocalizedLabel;
      copy.icon = orig.icon;
      copy.resolvePackageName = orig.resolvePackageName;
    }
    // For some reason isDefault field is not copied by the copy constructor.
    copy.isDefault = orig.isDefault;
    return copy;
  }
}
