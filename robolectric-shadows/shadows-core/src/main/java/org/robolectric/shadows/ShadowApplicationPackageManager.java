package org.robolectric.shadows;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UserIdInt;
import android.app.ApplicationPackageManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.*;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.UserHandle;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.List;

import static android.os.Build.VERSION_CODES.*;

@Implements(value = ApplicationPackageManager.class, isInAndroidSdk = false)
public class ShadowApplicationPackageManager extends ShadowPackageManager {

  @Implementation(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public PackageInstaller getPackageInstaller() {
    return ((PackageManager) RuntimeEnvironment.getRobolectricPackageManager()).getPackageInstaller();
  }

  @Implementation
  public List<PackageInfo> getInstalledPackages(int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().getInstalledPackages(flags);
  }

  @Implementation
  public ActivityInfo getActivityInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
    return RuntimeEnvironment.getRobolectricPackageManager().getActivityInfo(component, flags);
  }

  @Implementation
  public boolean hasSystemFeature(String name) {
    return RuntimeEnvironment.getRobolectricPackageManager().hasSystemFeature(name);
  }

  @Implementation
  public int getComponentEnabledSetting(ComponentName componentName) {
    return ((PackageManager) RuntimeEnvironment.getRobolectricPackageManager()).getComponentEnabledSetting(componentName);
  }

  @Implementation
  public @Nullable String getNameForUid(int uid) {
    return ((PackageManager) RuntimeEnvironment.getRobolectricPackageManager()).getNameForUid(uid);
  }

  @Implementation
  public @Nullable String[] getPackagesForUid(int uid) {
    return ((PackageManager) RuntimeEnvironment.getRobolectricPackageManager()).getPackagesForUid(uid);
  }

  @Implementation
  public int getApplicationEnabledSetting(String packageName) {
    return ((PackageManager) RuntimeEnvironment.getRobolectricPackageManager()).getApplicationEnabledSetting(packageName);
  }

  @Implementation
  public ProviderInfo getProviderInfo(ComponentName component, int flags) throws PackageManager.NameNotFoundException {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getProviderInfo(component, flags);
  }

  @Implementation
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    RuntimeEnvironment.getRobolectricPackageManager().setComponentEnabledSetting(componentName, newState, flags);
  }

  @Implementation
  public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    ((PackageManager) RuntimeEnvironment.getRobolectricPackageManager()).setApplicationEnabledSetting(packageName, newState, flags);
  }

  @Implementation
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
    return RuntimeEnvironment.getRobolectricPackageManager().getApplicationInfo(packageName, flags);
  }

  @Implementation
  public ResolveInfo resolveActivity(Intent intent, int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().resolveActivity(intent, flags);
  }

  @Implementation
  public ProviderInfo resolveContentProvider(String name, int flags) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).resolveContentProvider(name, flags);
  }

  @Implementation
  public ProviderInfo resolveContentProviderAsUser(String name, int flags, @UserIdInt int userId) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).resolveContentProviderAsUser(name, flags, userId);
  }

  @Implementation
  public PackageInfo getPackageInfo(String packageName, int flags) throws PackageManager.NameNotFoundException {
    return RuntimeEnvironment.getRobolectricPackageManager().getPackageInfo(packageName, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().queryIntentServices(intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().queryIntentActivities(intent, flags);
  }

  @Implementation
  public int checkPermission(String permName, String pkgName) {
    return RuntimeEnvironment.getRobolectricPackageManager().checkPermission(permName, pkgName);
  }

  @Implementation
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
    return RuntimeEnvironment.getRobolectricPackageManager().getReceiverInfo(className, flags);
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().queryBroadcastReceivers(intent, flags);
  }

  @Implementation
  public ResolveInfo resolveService(Intent intent, int flags) {
    return RuntimeEnvironment.getRobolectricPackageManager().resolveService(intent, flags);
  }

  @Implementation
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws PackageManager.NameNotFoundException {
    return RuntimeEnvironment.getRobolectricPackageManager().getServiceInfo(className, flags);
  }

  @Implementation
  public Resources getResourcesForApplication(@NonNull ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
    if (RuntimeEnvironment.application.getPackageName().equals(applicationInfo.packageName)) {
      return RuntimeEnvironment.application.getResources();
    }
    throw new PackageManager.NameNotFoundException(applicationInfo.packageName);
  }

  @Implementation
  public List<ApplicationInfo> getInstalledApplications(int flags) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getInstalledApplications(flags);
  }

  @Implementation
  public String getInstallerPackageName(String packageName) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getInstallerPackageName(packageName);
  }

  @Implementation
  public PermissionInfo getPermissionInfo(String name, int flags) throws PackageManager.NameNotFoundException {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getPermissionInfo(name, flags);
  }

  @Implementation(minSdk = M)
  public boolean shouldShowRequestPermissionRationale(String permission) {
    return permissionRationaleMap.containsKey(permission) ? permissionRationaleMap.get(permission) : false;
  }

  @Implementation
  public FeatureInfo[] getSystemAvailableFeatures() {
    return systemAvailableFeatures.isEmpty() ? null : systemAvailableFeatures.toArray(new FeatureInfo[systemAvailableFeatures.size()]);
  }

  @Implementation
  public void verifyPendingInstall(int id, int verificationCode) {
    if (verificationResults.containsKey(id)) {
      throw new IllegalStateException("Multiple verifications for id=" + id);
    }
    verificationResults.put(id, verificationCode);
  }

  @Implementation
  public void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {

  }

  @Implementation
  public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {

  }

  @Implementation
  public void setInstallerPackageName(String targetPackage, String installerPackageName) {
    ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).setInstallerPackageName(targetPackage, installerPackageName);
  }

  @Implementation
  public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).queryIntentContentProviders(intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).queryIntentContentProvidersAsUser(intent, flags, userId);
  }

  @Implementation
  public String getPermissionControllerPackageName() {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getPermissionControllerPackageName();
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void getPackageSizeInfo(String packageName, IPackageStatsObserver observer) {
    ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getPackageSizeInfoAsUser(packageName, UserHandle.myUserId(), observer);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = M)
  public void getPackageSizeInfo(String pkgName, int uid, final IPackageStatsObserver callback) {
    ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getPackageSizeInfoAsUser(pkgName, uid, callback);
  }

  @Implementation(minSdk = N)
  public void getPackageSizeInfoAsUser(String pkgName, int uid, final IPackageStatsObserver callback) {
    ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getPackageSizeInfoAsUser(pkgName, uid, callback);
  }

  @Implementation
  public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
  }

  @Implementation
  public String[] currentToCanonicalPackageNames(String[] names) {
    String[] out = new String[names.length];
    for (int i = names.length - 1; i >= 0; i--) {
      if (currentToCanonicalNames.containsKey(names[i])) {
        out[i] = currentToCanonicalNames.get(names[i]);
      } else {
        out[i] = names[i];
      }
    }
    return out;
  }

  @Implementation
  public boolean isSafeMode() {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).isSafeMode();
  }

  @Implementation
  public Drawable getApplicationIcon(String packageName) throws PackageManager.NameNotFoundException {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getApplicationIcon(packageName);
  }

  @Implementation
  public Drawable getApplicationIcon(ApplicationInfo info) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getApplicationIcon(info);
  }

  @Implementation
  public Drawable getUserBadgeForDensity(UserHandle userHandle, int i) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).getUserBadgeForDensity(userHandle, i);
  }

  @Implementation
  public int checkSignatures(String pkg1, String pkg2) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).checkSignatures(pkg1, pkg2);
  }

  @Implementation
  public int checkSignatures(int uid1, int uid2) {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).checkSignatures(uid1, uid2);
  }

  @Implementation
  public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws PackageManager.NameNotFoundException {
    return ((PackageManager)RuntimeEnvironment.getRobolectricPackageManager()).queryPermissionsByGroup(group, flags);
  }

  public CharSequence getApplicationLabel(ApplicationInfo info) {
    return RuntimeEnvironment.getRobolectricPackageManager().getApplicationLabel(info);
  }

  @Implementation
  public Intent getLaunchIntentForPackage(String packageName) {
    return RuntimeEnvironment.getRobolectricPackageManager().getLaunchIntentForPackage(packageName);
  }
}
