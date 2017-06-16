package org.robolectric.shadows;

import android.annotation.UserIdInt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.builder.DefaultPackageManager;
import org.robolectric.res.builder.RobolectricPackageManager;

@Implements(PackageManager.class)
public class ShadowPackageManager implements RobolectricPackageManager {

  protected Map<String, Boolean> permissionRationaleMap = new HashMap<>();
  protected List<FeatureInfo> systemAvailableFeatures = new LinkedList<>();
  private Map<String, PackageInfo> packageArchiveInfo = new HashMap<>();
  protected final Map<Integer, Integer> verificationResults = new HashMap<>();
  protected final Map<Integer, Long> verificationTimeoutExtension = new HashMap<>();
  protected final Map<String, String> currentToCanonicalNames = new HashMap<>();

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getPackageInfo(String, int)} instead.
   */
  @Override
  @Deprecated
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getApplicationInfo(String, int)} instead.
   */
  @Override
  @Deprecated
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getActivityInfo(ComponentName, int)} instead.
   */
  @Override
  @Deprecated
  public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getReceiverInfo(ComponentName, int)} instead.
   */
  @Override
  @Deprecated
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getServiceInfo(ComponentName, int)} instead.
   */
  @Override
  @Deprecated
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getInstalledPackages(int)} instead.
   */
  @Override
  @Deprecated
  public List<PackageInfo> getInstalledPackages(int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#queryIntentActivities(Intent, int)} instead.
   */
  @Override
  @Deprecated
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#queryIntentServices(Intent, int)}  instead.
   */
  @Override
  @Deprecated
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#queryBroadcastReceivers(Intent, int)} instead.
   */
  @Override
  @Deprecated
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#resolveActivity(Intent, int)} instead.
   */
  @Override
  @Deprecated
  public ResolveInfo resolveActivity(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#resolveService(Intent, int)} instead.
   */
  @Override
  @Deprecated
  public ResolveInfo resolveService(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

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
  public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
    return RuntimeEnvironment.getRobolectricPackageManager().getActivityIcon(intent);
  }

  @Override
  public Drawable getActivityIcon(ComponentName componentName) throws NameNotFoundException {
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

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getApplicationIcon(String)} instead.
   */
  @Override
  @Deprecated
  public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void setApplicationIcon(String packageName, Drawable drawable) {
    RuntimeEnvironment.getRobolectricPackageManager().setApplicationIcon(packageName, drawable);
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getLaunchIntentForPackage(String)} instead.
   */
  @Override
  @Deprecated
  public Intent getLaunchIntentForPackage(String packageName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#getApplicationLabel(ApplicationInfo)} instead.
   */
  @Override
  @Deprecated
  public CharSequence getApplicationLabel(ApplicationInfo info) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#setComponentEnabledSetting(ComponentName, int, int)} instead.
   */
  @Override
  @Deprecated
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    RuntimeEnvironment.getRobolectricPackageManager().setApplicationEnabledSetting(packageName, newState, flags);
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
  public void addManifest(AndroidManifest androidManifest) {
    RuntimeEnvironment.getRobolectricPackageManager().addManifest(androidManifest);
  }

  @Override
  public void removePackage(String packageName) {
    RuntimeEnvironment.getRobolectricPackageManager().removePackage(packageName);
  }

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#hasSystemFeature(String)} instead.
   */
  @Override
  @Deprecated
  public boolean hasSystemFeature(String name) {
    throw new UnsupportedOperationException("Not implemented");
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

  /**
   * @deprecated We're forced to implement this because we're implementing {@link RobolectricPackageManager} which will
   * be removed in the next release. Prefer {@link PackageManager#checkPermission(String, String)} instead.
   */
  @Override
  @Deprecated
  public int checkPermission(String permName, String pkgName) {
    return 0;
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
  public long getVerificationExtendedTimeout(int id) {
    Long result = verificationTimeoutExtension.get(id);
    if (result == null) {
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

  @Implementation
  public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, UserHandle userHandle) {
    return getDelegatePackageManager().queryBroadcastReceiversAsUser(intent, flags, userHandle.getIdentifier());
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, @UserIdInt int userId) {
    return getDelegatePackageManager().queryBroadcastReceivers(intent, flags, userId);
  }

  @Override @Implementation
  public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
    return getDelegatePackageManager().getPackageArchiveInfo(archiveFilePath, flags);
  }

  @Implementation
  public void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {
    getDelegatePackageManager().freeStorageAndNotify(freeStorageSize, observer);
  }

  @Implementation
  public void freeStorage(long freeStorageSize, IntentSender pi) {
    getDelegatePackageManager().freeStorage(freeStorageSize, pi);
  }

  static DefaultPackageManager getDelegatePackageManager() {
    return (DefaultPackageManager) RuntimeEnvironment.getRobolectricPackageManager();
  }

  /**
   * Runs the callbacks pending from calls to {@link PackageManager#deletePackage(String, IPackageDeleteObserver, int)}
   */
  public void doPendingUninstallCallbacks() {
    RuntimeEnvironment.getRobolectricPackageManager().doPendingUninstallCallbacks();
  }

  /**
   * Returns package names successfully deleted with {@link PackageManager#deletePackage(String, IPackageDeleteObserver, int)}
   * Note that like real {@link PackageManager} the calling context must have {@link android.Manifest.permission#DELETE_PACKAGES} permission set.
   */
  public Set<String> getDeletedPackages() {
    return RuntimeEnvironment.getRobolectricPackageManager().getDeletedPackages();
  }
}
