package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import java.util.List;
import java.util.Set;
import org.robolectric.manifest.AndroidManifest;

public interface RobolectricPackageManager {

  /**
   * @deprecated Prefer {@link PackageManager#getPackageInfo(String, int)} instead.
   */
  @Deprecated
  PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException;

  /**
   * @deprecated Prefer {@link PackageManager#getApplicationInfo(String, int)} instead.
   */
  @Deprecated
  ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException;

  /**
   * @deprecated Prefer {@link PackageManager#getActivityInfo(ComponentName, int)} instead.
   */
  @Deprecated
  ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException;

  /**
   * @deprecated Prefer {@link PackageManager#getReceiverInfo(ComponentName, int)} instead.
   */
  @Deprecated
  ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException;

  /**
   * @deprecated Prefer {@link PackageManager#getServiceInfo(ComponentName, int)} instead.
   */
  @Deprecated
  ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException;

  /**
   * @deprecated Prefer {@link PackageManager#getInstalledPackages(int)} instead.
   */
  @Deprecated
  List<PackageInfo> getInstalledPackages(int flags);

  /**
   * @deprecated Prefer {@link PackageManager#queryIntentActivities(Intent, int)} instead.
   */
  @Deprecated
  List<ResolveInfo> queryIntentActivities(Intent intent, int flags);

  /**
   * @deprecated Prefer {@link PackageManager#queryIntentServices(Intent, int)} instead.
   */
  @Deprecated
  List<ResolveInfo> queryIntentServices(Intent intent, int flags);

  /**
   * @deprecated Prefer {@link PackageManager#queryBroadcastReceivers(Intent, int)} instead.
   */
  @Deprecated
  List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags);

  /**
   * @deprecated Prefer {@link PackageManager#resolveActivity(Intent, int)} instead.
   */
  @Deprecated
  ResolveInfo resolveActivity(Intent intent, int flags);

  /**
   * @deprecated Prefer {@link PackageManager#resolveService(Intent, int)} instead.
   */
  @Deprecated
  ResolveInfo resolveService(Intent intent, int flags);

  /**
   * @deprecated Prefer {@link PackageManager#getApplicationIcon(String)} instead.
   */
  @Deprecated
  Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException;

  /**
   * @deprecated Prefer {@link PackageManager#getLaunchIntentForPackage(String)} instead.
   */
  @Deprecated
  Intent getLaunchIntentForPackage(String packageName);

  /**
   * @deprecated Prefer {@link PackageManager#getApplicationLabel(ApplicationInfo)} instead.
   */
  @Deprecated
  CharSequence getApplicationLabel(ApplicationInfo info);

  /**
   * @deprecated Prefer {@link PackageManager#setComponentEnabledSetting(ComponentName, int, int)} instead.
   */
  @Deprecated
  void setComponentEnabledSetting(ComponentName componentName, int newState, int flags);

  /**
   * @deprecated Prefer {@link PackageManager#hasSystemFeature(String)} instead.
   */
  @Deprecated
  boolean hasSystemFeature(String name);

  void doPendingUninstallCallbacks();

  void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info);

  void addResolveInfoForIntent(Intent intent, ResolveInfo info);

  void removeResolveInfosForIntent(Intent intent, String packageName);

  Drawable getActivityIcon(Intent intent) throws NameNotFoundException;

  Drawable getActivityIcon(ComponentName componentName) throws NameNotFoundException;

  void addActivityIcon(ComponentName component, Drawable d);

  void addActivityIcon(Intent intent, Drawable d);

  void setApplicationIcon(String packageName, Drawable d);

  void setApplicationEnabledSetting(String packageName, int newState, int flags);

  void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity);

  int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName);

  ComponentState getComponentState(ComponentName componentName);

  void addPackage(PackageInfo packageInfo, PackageStats packageStats);

  void addPackage(PackageInfo packageInfo);

  void addPackage(String packageName);

  @Deprecated
  void addManifest(AndroidManifest androidManifest);

  void removePackage(String packageName);

  void setSystemFeature(String name, boolean supported);

  void addDrawableResolution(String packageName, int resourceId, Drawable drawable);

  Drawable getDrawable(String packageName, int resourceId, ApplicationInfo applicationInfo);

  int checkPermission(String permName, String pkgName);

  boolean isQueryIntentImplicitly();

  void setQueryIntentImplicitly(boolean queryIntentImplicitly);

  void setNameForUid(int uid, String name);

  void setPackagesForCallingUid(String... packagesForCallingUid);

  void setPackagesForUid(int uid, String... packagesForCallingUid);

  PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags);

  void addPermissionInfo(PermissionInfo permissionInfo);

  Set<String> getDeletedPackages();

  class ComponentState {
    public int newState;
    public int flags;

    public ComponentState(int newState, int flags) {
      this.newState = newState;
      this.flags = flags;
    }
  }
}
