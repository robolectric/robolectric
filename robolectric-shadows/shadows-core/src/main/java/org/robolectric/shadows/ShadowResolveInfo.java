package org.robolectric.shadows;

import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(ResolveInfo.class)
public class ShadowResolveInfo {
  private String label;

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

    ShadowResolveInfo shResolve = Shadows.shadowOf(resInfo);
    shResolve.setLabel(displayName);
    return resInfo;
  }

  @Implementation
  public String loadLabel(PackageManager mgr) {
    return label;
  }

  /**
   * Sets the value returned by {@link #loadLabel}.
   *
   * @param l Label.
   */
  public void setLabel(String l) {
    label = l;
  }
}
