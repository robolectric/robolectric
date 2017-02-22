package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.graphics.drawable.Drawable;
import org.robolectric.manifest.AndroidManifest;

import java.util.List;

public interface RobolectricPackageManager {
  PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException;

  ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException;

  ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException;

  ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException;

  ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException;
  
  List<PackageInfo> getInstalledPackages(int flags);

  List<ResolveInfo> queryIntentActivities(Intent intent, int flags);

  List<ResolveInfo> queryIntentServices(Intent intent, int flags);

  List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags);

  ResolveInfo resolveActivity(Intent intent, int flags);

  ResolveInfo resolveService(Intent intent, int flags);

  void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info);

  void addResolveInfoForIntent(Intent intent, ResolveInfo info);

  void removeResolveInfosForIntent(Intent intent, String packageName);

  Drawable getActivityIcon(Intent intent);

  Drawable getActivityIcon(ComponentName componentName);

  void addActivityIcon(ComponentName component, Drawable d);

  void addActivityIcon(Intent intent, Drawable d);

  Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException;

  void setApplicationIcon(String packageName, Drawable d);

  Intent getLaunchIntentForPackage(String packageName);

  CharSequence getApplicationLabel(ApplicationInfo info);

  void setComponentEnabledSetting(ComponentName componentName, int newState, int flags);

  void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity);

  int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName);

  ComponentState getComponentState(ComponentName componentName);

  void addPackage(PackageInfo packageInfo);

  void addPackage(String packageName);

  void addManifest(AndroidManifest androidManifest, int labelRes);

  void removePackage(String packageName);

  boolean hasSystemFeature(String name);

  void setSystemFeature(String name, boolean supported);

  void addDrawableResolution(String packageName, int resourceId, Drawable drawable);

  Drawable getDrawable(String packageName, int resourceId, ApplicationInfo applicationInfo);

  int checkPermission(String permName, String pkgName);

  boolean isQueryIntentImplicitly();

  void setQueryIntentImplicitly(boolean queryIntentImplicitly);

  void reset();

  void setNameForUid(int uid, String name);

  void setPackagesForCallingUid(String... packagesForCallingUid);

  void setPackagesForUid(int uid, String... packagesForCallingUid);

  PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags);

  void addPermissionInfo(PermissionInfo permissionInfo);

  class ComponentState {
    public int newState;
    public int flags;

    public ComponentState(int newState, int flags) {
      this.newState = newState;
      this.flags = flags;
    }
  }
}
