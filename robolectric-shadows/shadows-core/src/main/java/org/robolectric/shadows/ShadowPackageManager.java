package org.robolectric.shadows;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.*;
import android.graphics.drawable.Drawable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.StubPackageManager;
import org.robolectric.annotation.Implements;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.builder.RobolectricPackageManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Implements(PackageManager.class)
abstract public class ShadowPackageManager implements RobolectricPackageManager {

  protected Map<String, Boolean> permissionRationaleMap = new HashMap<>();
  protected List<FeatureInfo> systemAvailableFeatures = new LinkedList<>();
  private Map<String, PackageInfo> packageArchiveInfo = new HashMap<>();
  protected final Map<Integer, Integer> verificationResults = new HashMap<>();
  protected final Map<String, String> currentToCanonicalNames = new HashMap<>();

  @Override
  public void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info) {
    RuntimeEnvironment.getRobolectricPackageManager().addResolveInfoForIntent(intent, info);
  }

  @Override
  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    RuntimeEnvironment.getRobolectricPackageManager().addResolveInfoForIntent(intent, info);
  }

  @Override
  public void removeResolveInfosForIntent(Intent intent, String packageName) {
    RuntimeEnvironment.getRobolectricPackageManager().removeResolveInfosForIntent(intent, packageName);
  }

  @Override
  public Drawable getActivityIcon(Intent intent) {
    return RuntimeEnvironment.getRobolectricPackageManager().getActivityIcon(intent);
  }

  @Override
  public Drawable getActivityIcon(ComponentName componentName) {
    return RuntimeEnvironment.getRobolectricPackageManager().getActivityIcon(componentName);
  }

  @Override
  public void addActivityIcon(ComponentName component, Drawable drawable) {
    RuntimeEnvironment.getRobolectricPackageManager().addActivityIcon(component, drawable);
  }

  @Override
  public void addActivityIcon(Intent intent, Drawable drawable) {
    RuntimeEnvironment.getRobolectricPackageManager().addActivityIcon(intent, drawable);
  }

  @Override
  public void setApplicationIcon(String packageName, Drawable drawable) {
    RuntimeEnvironment.getRobolectricPackageManager().setApplicationIcon(packageName, drawable);
  }

  @Override
  public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    RuntimeEnvironment.getRobolectricPackageManager().addPreferredActivity(filter, match, set, activity);
  }

  @Override
  public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    return RuntimeEnvironment.getRobolectricPackageManager().getPreferredActivities(outFilters, outActivities, packageName);
  }

  @Override
  public ComponentState getComponentState(ComponentName componentName) {
    return RuntimeEnvironment.getRobolectricPackageManager().getComponentState(componentName);
  }

  @Override
  public void addPackage(PackageInfo packageInfo) {
    RuntimeEnvironment.getRobolectricPackageManager().addPackage(packageInfo);
  }

  @Override
  public void addPackage(PackageInfo packageInfo, PackageStats packageStats) {
    RuntimeEnvironment.getRobolectricPackageManager().addPackage(packageInfo, packageStats);
  }

  @Override
  public void addPermissionInfo(PermissionInfo permissionInfo) {
    RuntimeEnvironment.getRobolectricPackageManager().addPermissionInfo(permissionInfo);
  }

  @Override
  public void addPackage(String packageName) {
    RuntimeEnvironment.getRobolectricPackageManager().addPackage(packageName);
  }

  @Override
  public void addManifest(AndroidManifest androidManifest, int labelRes) {
    RuntimeEnvironment.getRobolectricPackageManager().addManifest(androidManifest, labelRes);
  }

  @Override
  public void removePackage(String packageName) {
    RuntimeEnvironment.getRobolectricPackageManager().removePackage(packageName);
  }

  @Override
  public void setSystemFeature(String name, boolean supported) {
    RuntimeEnvironment.getRobolectricPackageManager().setSystemFeature(name, supported);
  }

  @Override
  public void addDrawableResolution(String packageName, int resourceId, Drawable drawable) {
    RuntimeEnvironment.getRobolectricPackageManager().addDrawableResolution(packageName, resourceId, drawable);
  }

  @Override
  public Drawable getDrawable(String packageName, int resourceId, ApplicationInfo applicationInfo) {
    return RuntimeEnvironment.getRobolectricPackageManager().getDrawable(packageName, resourceId, applicationInfo);
  }

  @Override
  public boolean isQueryIntentImplicitly() {
    return RuntimeEnvironment.getRobolectricPackageManager().isQueryIntentImplicitly();
  }

  @Override
  public void setQueryIntentImplicitly(boolean queryIntentImplicitly) {
    RuntimeEnvironment.getRobolectricPackageManager().setQueryIntentImplicitly(queryIntentImplicitly);
  }

  @Override
  public void reset() {
    RuntimeEnvironment.getRobolectricPackageManager().reset();
  }

  @Override
  public void setNameForUid(int uid, String name) {
    RuntimeEnvironment.getRobolectricPackageManager().setNameForUid(uid, name);
  }

  @Override
  public void setPackagesForCallingUid(String... packagesForCallingUid) {
    RuntimeEnvironment.getRobolectricPackageManager().setPackagesForCallingUid(packagesForCallingUid);
  }

  @Override
  public void setPackagesForUid(int uid, String... packagesForCallingUid) {
    RuntimeEnvironment.getRobolectricPackageManager().setPackagesForUid(uid, packagesForCallingUid);
  }

  @Override
  public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().getPackageArchiveInfo(archiveFilePath, flags);
  }

  public void setPackageArchiveInfo(String archiveFilePath, PackageInfo packageInfo) {
    packageArchiveInfo.put(archiveFilePath, packageInfo);
  }

  public int getVerificationResult(int id) {
    Integer result = verificationResults.get(id);
    if (result == null) {
      // 0 isn't a "valid" result, so we can check for the case when verification isn't
      // called, if needed
      return 0;
    }
    return result;
  }

  public void setShouldShowRequestPermissionRationale(String permission, boolean show) {
    permissionRationaleMap.put(permission, show);
  }

  public void addSystemAvailableFeature(FeatureInfo featureInfo) {
    systemAvailableFeatures.add(featureInfo);
  }

  public void clearSystemAvailableFeatures() {
    systemAvailableFeatures.clear();
  }

  public void addCurrentToCannonicalName(String currentName, String canonicalName) {
    currentToCanonicalNames.put(currentName, canonicalName);
  }

  public abstract void getPackageSizeInfo(String pkgName, int uid, IPackageStatsObserver callback);

  @Implements(value = StubPackageManager.class, isInAndroidSdk = false)
  public static class ShadowStubPackageManager extends ShadowPackageManager {
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
      return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
      return null;
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
      return null;
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
      return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
      return null;
    }

    @Override
    public List<PackageInfo> getInstalledPackages(int flags) {
      return null;
    }

    @Override
    public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
      return null;
    }

    @Override
    public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
      return null;
    }

    @Override
    public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
      return null;
    }

    @Override
    public ResolveInfo resolveActivity(Intent intent, int flags) {
      return null;
    }

    @Override
    public ResolveInfo resolveService(Intent intent, int flags) {
      return null;
    }

    @Override
    public Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException {
      return null;
    }

    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
      return null;
    }

    @Override
    public CharSequence getApplicationLabel(ApplicationInfo info) {
      return null;
    }

    @Override
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {

    }

    @Override
    public boolean hasSystemFeature(String name) {
      return false;
    }

    @Override
    public int checkPermission(String permName, String pkgName) {
      return 0;
    }

    @Override
    public void getPackageSizeInfo(String pkgName, int uid, IPackageStatsObserver callback) {

    }
  }
}
