package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.annotation.DrawableRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.StringRes;
import android.annotation.UserIdInt;
import android.app.ApplicationPackageManager;
import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.MoveCallback;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import java.util.Collections;
import java.util.List;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = ApplicationPackageManager.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowApplicationPackageManager extends ShadowPackageManager {

  @Implementation
  public List<PackageInfo> getInstalledPackages(int flags) {
    return defaultPackageManager.getInstalledPackages(flags);
  }

  @Implementation
  public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
    return defaultPackageManager.getActivityInfo(component, flags);
  }

  @Implementation
  public boolean hasSystemFeature(String name) {
    return defaultPackageManager.hasSystemFeature(name);
  }

  @Implementation
  public int getComponentEnabledSetting(ComponentName componentName) {
    return defaultPackageManager.getComponentEnabledSetting(componentName);
  }

  @Implementation
  public @Nullable String getNameForUid(int uid) {
    return defaultPackageManager.getNameForUid(uid);
  }

  @Implementation
  public @Nullable String[] getPackagesForUid(int uid) {
    return defaultPackageManager.getPackagesForUid(uid);
  }

  @Implementation
  public int getApplicationEnabledSetting(String packageName) {
    return defaultPackageManager.getApplicationEnabledSetting(packageName);
  }

  @Implementation
  public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
    return defaultPackageManager.getProviderInfo(component, flags);
  }

  @Implementation
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    defaultPackageManager.setComponentEnabledSetting(componentName, newState, flags);
  }

  @Override @Implementation
  public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    defaultPackageManager.setApplicationEnabledSetting(packageName, newState, flags);
  }

  @Implementation
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    return defaultPackageManager.getApplicationInfo(packageName, flags);
  }

  @Implementation
  public ResolveInfo resolveActivity(Intent intent, int flags) {
    return defaultPackageManager.resolveActivity(intent, flags);
  }

  @Implementation
  public ProviderInfo resolveContentProvider(String name, int flags) {
    return defaultPackageManager.resolveContentProvider(name, flags);
  }

  @Implementation
  public ProviderInfo resolveContentProviderAsUser(String name, int flags, @UserIdInt int userId) {
    return null;
  }

  @Implementation
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    return defaultPackageManager.getPackageInfo(packageName, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    return defaultPackageManager.queryIntentServices(intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    return defaultPackageManager.queryIntentActivities(intent, flags);
  }

  @Implementation
  public int checkPermission(String permName, String pkgName) {
    return defaultPackageManager.checkPermission(permName, pkgName);
  }

  @Implementation
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    return defaultPackageManager.getReceiverInfo(className, flags);
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    return defaultPackageManager.queryBroadcastReceivers(intent, flags);
  }

  @Implementation
  public ResolveInfo resolveService(Intent intent, int flags) {
    return defaultPackageManager.resolveService(intent, flags);
  }

  @Implementation
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    return defaultPackageManager.getServiceInfo(className, flags);
  }

  @Implementation
  public Resources getResourcesForApplication(@NonNull ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
    return defaultPackageManager.getResourcesForApplication(applicationInfo.packageName);
  }

  @Implementation
  public List<ApplicationInfo> getInstalledApplications(int flags) {
    return defaultPackageManager.getInstalledApplications(flags);
  }

  @Implementation
  public String getInstallerPackageName(String packageName) {
    return defaultPackageManager.getInstallerPackageName(packageName);
  }

  @Implementation
  public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
    return defaultPackageManager.getPermissionInfo(name, flags);
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
  public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
    verificationTimeoutExtension.put(id, millisecondsToDelay);
  }

  @Implementation
  public void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {
  }

  @Implementation
  public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {
  }

  @Implementation
  public void setInstallerPackageName(String targetPackage, String installerPackageName) {
    defaultPackageManager.setInstallerPackageName(targetPackage, installerPackageName);
  }

  @Implementation
  public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
    return Collections.emptyList();
  }

  @Implementation
  public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
    return Collections.emptyList();
  }

  @Implementation
  public String getPermissionControllerPackageName() {
    return null;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void getPackageSizeInfo(String packageName, IPackageStatsObserver observer) {
    defaultPackageManager
        .getPackageSizeInfoAsUser(packageName, UserHandle.myUserId(), observer);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = M)
  public void getPackageSizeInfo(String pkgName, int uid, final IPackageStatsObserver callback) {
    defaultPackageManager.getPackageSizeInfoAsUser(pkgName, uid, callback);
  }

  @Implementation(minSdk = N)
  public void getPackageSizeInfoAsUser(String pkgName, int uid, final IPackageStatsObserver callback) {
    defaultPackageManager.getPackageSizeInfoAsUser(pkgName, uid, callback);
  }

  @Implementation
  public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
    defaultPackageManager.deletePackage(packageName, observer, flags);
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
    return false;
  }

  @Implementation
  public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    return defaultPackageManager.getApplicationIcon(packageName);
  }

  @Implementation
  public Drawable getApplicationIcon(ApplicationInfo info) {
    return null;
  }

  @Implementation
  public Drawable getUserBadgeForDensity(UserHandle userHandle, int i) {
    return null;
  }

  @Implementation
  public int checkSignatures(String pkg1, String pkg2) {
    return defaultPackageManager.checkSignatures(pkg1, pkg2);
  }

  @Implementation
  public int checkSignatures(int uid1, int uid2) {
    return 0;
  }

  @Implementation
  public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
    return defaultPackageManager.queryPermissionsByGroup(group, flags);
  }

  public CharSequence getApplicationLabel(ApplicationInfo info) {
    return info.name;
  }

  @Implementation
  public Intent getLaunchIntentForPackage(String packageName) {
    Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
    intentToResolve.addCategory(Intent.CATEGORY_INFO);
    intentToResolve.setPackage(packageName);
    List<ResolveInfo> ris = defaultPackageManager.queryIntentActivities(intentToResolve, 0);

    if (ris == null || ris.isEmpty()) {
      intentToResolve.removeCategory(Intent.CATEGORY_INFO);
      intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
      intentToResolve.setPackage(packageName);
      ris = defaultPackageManager.queryIntentActivities(intentToResolve, 0);
    }
    if (ris == null || ris.isEmpty()) {
      return null;
    }
    Intent intent = new Intent(intentToResolve);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
    return intent;
  }

  ////////////////////////////

  @Implementation
  public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  public String[] canonicalToCurrentPackageNames(String[] names) {
    return new String[0];
  }

  @Implementation
  public Intent getLeanbackLaunchIntentForPackage(String packageName) {
    return null;
  }

  @Implementation
  public int[] getPackageGids(String packageName) throws NameNotFoundException {
    return new int[0];
  }

  @Implementation
  public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
    return null;
  }

  @Implementation
  public int getPackageUid(String packageName, int flags) throws NameNotFoundException {
    return 0;
  }

  @Implementation
  public int getPackageUidAsUser(String packageName, int userId) throws NameNotFoundException {
    return 0;
  }

  @Implementation
  public int getPackageUidAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return 0;
  }

  @Implementation
  public PermissionGroupInfo getPermissionGroupInfo(String name, int flags)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
    return null;
  }

  @Implementation
  public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  public String[] getSystemSharedLibraryNames() {
    return defaultPackageManager.getSystemSharedLibraryNames();
  }

  @Implementation
  public
  @NonNull
  String getServicesSystemSharedLibraryPackageName() {
    return null;
  }

  @Implementation
  public
  @NonNull
  String getSharedSystemSharedLibraryPackageName() {
    return "";
  }

  @Implementation
  public boolean hasSystemFeature(String name, int version) {
    return false;
  }

  @Implementation
  public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
    return defaultPackageManager.isPermissionRevokedByPolicy(permName, pkgName);
  }

  @Implementation
  public boolean addPermission(PermissionInfo info) {
    return defaultPackageManager.addPermission(info);
  }

  @Implementation
  public boolean addPermissionAsync(PermissionInfo info) {
    return defaultPackageManager.addPermissionAsync(info);
  }

  @Implementation
  public void removePermission(String name) {
    defaultPackageManager.removePermission(name);
  }

  @Implementation
  public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
    defaultPackageManager.grantRuntimePermission(packageName, permissionName, user);
  }

  @Implementation
  public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
    defaultPackageManager.revokeRuntimePermission(packageName, permissionName, user);
  }

  @Implementation
  public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
    return defaultPackageManager.getPermissionFlags(permissionName, packageName, user);
  }

  @Implementation
  public void updatePermissionFlags(String permissionName, String packageName, int flagMask,
      int flagValues, UserHandle user) {
    defaultPackageManager
        .updatePermissionFlags(permissionName, packageName, flagMask, flagValues, user);
  }

  @Implementation
  public int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
    return defaultPackageManager.getUidForSharedUser(sharedUserName);
  }

  @Implementation
  public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
    return null;
  }

  @Implementation
  public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
    return defaultPackageManager.getPackagesHoldingPermissions(permissions, flags);
  }

  @Implementation
  public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
    return defaultPackageManager.resolveActivityAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
    return defaultPackageManager.queryIntentActivitiesAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
    return defaultPackageManager.queryIntentActivityOptions(caller, specifics, intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
    return null;
  }

  @Implementation
  public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
    return defaultPackageManager.queryIntentServicesAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
    return defaultPackageManager.queryContentProviders(processName, uid, flags);
  }

  @Implementation
  public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
    return defaultPackageManager.getInstrumentationInfo(className, flags);
  }

  @Implementation
  public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
    return defaultPackageManager.queryInstrumentation(targetPackage, flags);
  }

  @Override @Nullable
  @Implementation
  public Drawable getDrawable(String packageName, @DrawableRes int resId, @Nullable ApplicationInfo appInfo) {
    return defaultPackageManager.getDrawable(packageName, resId, appInfo);
  }

  @Override @Implementation
  public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
    return defaultPackageManager.getActivityIcon(activityName);
  }

  @Override public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
    return defaultPackageManager.getActivityIcon(intent);
  }

  @Implementation
  public Drawable getDefaultActivityIcon() {
    return defaultPackageManager.getDefaultActivityIcon();
  }

  @Implementation
  public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
    return defaultPackageManager.getActivityBanner(activityName);
  }

  @Implementation
  public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
    return defaultPackageManager.getActivityBanner(intent);
  }

  @Implementation
  public Drawable getApplicationBanner(ApplicationInfo info) {
    return defaultPackageManager.getApplicationBanner(info);
  }

  @Implementation
  public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
    return defaultPackageManager.getApplicationBanner(packageName);
  }

  @Implementation
  public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
    return defaultPackageManager.getActivityLogo(activityName);
  }

  @Implementation
  public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
    return defaultPackageManager.getActivityLogo(intent);
  }

  @Implementation
  public Drawable getApplicationLogo(ApplicationInfo info) {
    return defaultPackageManager.getApplicationLogo(info);
  }

  @Implementation
  public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
    return defaultPackageManager.getApplicationLogo(packageName);
  }

  @Implementation
  public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
    return defaultPackageManager.getUserBadgedIcon(icon, user);
  }

  @Implementation
  public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
    return defaultPackageManager
        .getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
  }

  @Implementation
  public Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
    return null;
  }

  @Implementation
  public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
    return defaultPackageManager.getUserBadgedLabel(label, user);
  }

  @Implementation
  public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
    return defaultPackageManager.getResourcesForActivity(activityName);
  }

  @Implementation
  public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
    return defaultPackageManager.getResourcesForApplication(appPackageName);
  }

  @Implementation
  public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws NameNotFoundException {
    return defaultPackageManager.getResourcesForApplicationAsUser(appPackageName, userId);
  }

  @Implementation
  public void addOnPermissionsChangeListener(Object listener) {
    defaultPackageManager.addOnPermissionsChangeListener((OnPermissionsChangedListener) listener);
  }

  @Implementation
  public void removeOnPermissionsChangeListener(Object listener) {
    defaultPackageManager.removeOnPermissionsChangeListener((OnPermissionsChangedListener) listener);
  }

  @Implementation
  public CharSequence getText(String packageName, @StringRes int resid, ApplicationInfo appInfo) {
    return defaultPackageManager.getText(packageName, resid, appInfo);
  }

  @Implementation
  public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName) {
    defaultPackageManager.installPackage(packageURI, observer, flags, installerPackageName);
  }

  @Implementation
  public void installPackage(Object packageURI, Object observer, Object flags, Object installerPackageName) {
    defaultPackageManager
        .installPackage((Uri) packageURI, (PackageInstallObserver) observer, (int) flags, (String) installerPackageName);
  }

  @Implementation
  public int installExistingPackage(String packageName) throws NameNotFoundException {
    return defaultPackageManager.installExistingPackage(packageName);
  }

  @Implementation
  public int installExistingPackageAsUser(String packageName, int userId) throws NameNotFoundException {
    return 0;
  }

  @Implementation
  public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {
  }

  @Implementation
  public int getIntentVerificationStatusAsUser(String packageName, int userId) {
    return 0;
  }

  @Implementation
  public boolean updateIntentVerificationStatusAsUser(String packageName, int status, int userId) {
    return false;
  }

  @Implementation
  public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
    return null;
  }

  @Implementation
  public List<IntentFilter> getAllIntentFilters(String packageName) {
    return null;
  }

  @Implementation
  public String getDefaultBrowserPackageNameAsUser(int userId) {
    return null;
  }

  @Implementation
  public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
    return false;
  }

  @Implementation
  public int getMoveStatus(int moveId) {
    return defaultPackageManager.getMoveStatus(moveId);
  }

  @Implementation
  public void registerMoveCallback(Object callback, Object handler) {
    defaultPackageManager.registerMoveCallback((MoveCallback) callback, (Handler) handler);
  }

  @Implementation
  public void unregisterMoveCallback(Object callback) {
    defaultPackageManager.unregisterMoveCallback((MoveCallback) callback);
  }

  @Implementation
  public Object movePackage(Object packageName, Object vol) {
    return defaultPackageManager.movePackage((String) packageName, (VolumeInfo) vol);
  }

  @Implementation
  public Object getPackageCurrentVolume(Object app) {
    return defaultPackageManager.getPackageCurrentVolume((ApplicationInfo) app);
  }

  @Implementation
  public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
    return defaultPackageManager.getPackageCandidateVolumes(app);
  }

  @Implementation
  public Object movePrimaryStorage(Object vol) {
    return defaultPackageManager.movePrimaryStorage((VolumeInfo) vol);
  }

  @Implementation
  public @Nullable Object getPrimaryStorageCurrentVolume() {
    return defaultPackageManager.getPrimaryStorageCurrentVolume();
  }

  @Implementation
  public @NonNull List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
    return defaultPackageManager.getPrimaryStorageCandidateVolumes();
  }

  @Implementation
  public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
  }

  @Implementation
  public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
    defaultPackageManager.clearApplicationUserData(packageName, observer);
  }

  @Implementation
  public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
    defaultPackageManager.deleteApplicationCacheFiles(packageName, observer);
  }

  @Implementation
  public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
  }

  @Implementation
  public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
    defaultPackageManager.freeStorage(volumeUuid, freeStorageSize, pi);
  }

  @Implementation
  public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId) {
    return null;
  }

  @Implementation
  public boolean isPackageSuspendedForUser(String packageName, int userId) {
    return false;
  }

  @Implementation
  public void addPackageToPreferred(String packageName) {
  }

  @Implementation
  public void removePackageFromPreferred(String packageName) {
  }

  @Implementation
  public List<PackageInfo> getPreferredPackages(int flags) {
    return null;
  }

  @Override public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    defaultPackageManager.addPreferredActivity(filter, match, set, activity);
  }

  @Implementation
  public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
  }

  @Implementation
  public void clearPackagePreferredActivities(String packageName) {
  }

  @Override public int getPreferredActivities(List<IntentFilter> outFilters,
      List<ComponentName> outActivities, String packageName) {
    return defaultPackageManager.getPreferredActivities(outFilters, outActivities, packageName);
  }

  @Implementation
  public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
    return null;
  }

  @Implementation
  public void flushPackageRestrictionsAsUser(int userId) {
  }

  @Implementation
  public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
    return false;
  }

  @Implementation
  public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
    return false;
  }

  @Implementation
  public Object getKeySetByAlias(String packageName, String alias) {
    return null;
  }

  @Implementation
  public Object getSigningKeySet(String packageName) {
    return null;
  }

  @Implementation
  public boolean isSignedBy(String packageName, Object ks) {
    return false;
  }

  @Implementation
  public boolean isSignedByExactly(String packageName, Object ks) {
    return false;
  }

  @Implementation
  public VerifierDeviceIdentity getVerifierDeviceIdentity() {
    return null;
  }

  @Implementation
  public boolean isUpgrade() {
    return false;
  }

  @Implementation
  public boolean isPackageAvailable(String packageName) {
    return false;
  }

  @Implementation
  public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
  }

  @Implementation
  public void clearCrossProfileIntentFilters(int sourceUserId) {
  }

  @Implementation
  public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return null;
  }

  @Implementation
  public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return null;
  }
}
