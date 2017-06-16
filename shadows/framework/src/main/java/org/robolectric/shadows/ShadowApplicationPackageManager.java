package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.RuntimeEnvironment.getRobolectricPackageManager;
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
import android.content.pm.KeySet;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
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
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = ApplicationPackageManager.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowApplicationPackageManager extends ShadowPackageManager {

  @Implementation
  public List<PackageInfo> getInstalledPackages(int flags) {
    return getRobolectricPackageManager().getInstalledPackages(flags);
  }

  @Implementation
  public ActivityInfo getActivityInfo(ComponentName component, int flags) throws NameNotFoundException {
    return getRobolectricPackageManager().getActivityInfo(component, flags);
  }

  @Implementation
  public boolean hasSystemFeature(String name) {
    return getRobolectricPackageManager().hasSystemFeature(name);
  }

  @Implementation
  public int getComponentEnabledSetting(ComponentName componentName) {
    return getDelegatePackageManager().getComponentEnabledSetting(componentName);
  }

  @Implementation
  public @Nullable String getNameForUid(int uid) {
    return getDelegatePackageManager().getNameForUid(uid);
  }

  @Implementation
  public @Nullable String[] getPackagesForUid(int uid) {
    return getDelegatePackageManager().getPackagesForUid(uid);
  }

  @Implementation
  public int getApplicationEnabledSetting(String packageName) {
    return getDelegatePackageManager().getApplicationEnabledSetting(packageName);
  }

  @Implementation
  public ProviderInfo getProviderInfo(ComponentName component, int flags) throws NameNotFoundException {
    return getDelegatePackageManager().getProviderInfo(component, flags);
  }

  @Implementation
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    getRobolectricPackageManager().setComponentEnabledSetting(componentName, newState, flags);
  }

  @Override @Implementation
  public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    getDelegatePackageManager().setApplicationEnabledSetting(packageName, newState, flags);
  }

  @Implementation
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    return getRobolectricPackageManager().getApplicationInfo(packageName, flags);
  }

  @Implementation
  public ResolveInfo resolveActivity(Intent intent, int flags) {
    return getRobolectricPackageManager().resolveActivity(intent, flags);
  }

  @Implementation
  public ProviderInfo resolveContentProvider(String name, int flags) {
    return getDelegatePackageManager().resolveContentProvider(name, flags);
  }

  @Implementation
  public ProviderInfo resolveContentProviderAsUser(String name, int flags, @UserIdInt int userId) {
    return getDelegatePackageManager().resolveContentProviderAsUser(name, flags, userId);
  }

  @Implementation
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    return getRobolectricPackageManager().getPackageInfo(packageName, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    return getRobolectricPackageManager().queryIntentServices(intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    return getRobolectricPackageManager().queryIntentActivities(intent, flags);
  }

  @Implementation
  public int checkPermission(String permName, String pkgName) {
    return getRobolectricPackageManager().checkPermission(permName, pkgName);
  }

  @Implementation
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    return getRobolectricPackageManager().getReceiverInfo(className, flags);
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    return getRobolectricPackageManager().queryBroadcastReceivers(intent, flags);
  }

  @Implementation
  public ResolveInfo resolveService(Intent intent, int flags) {
    return getRobolectricPackageManager().resolveService(intent, flags);
  }

  @Implementation
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    return getRobolectricPackageManager().getServiceInfo(className, flags);
  }

  @Implementation
  public Resources getResourcesForApplication(@NonNull ApplicationInfo applicationInfo) throws PackageManager.NameNotFoundException {
    return getDelegatePackageManager().getResourcesForApplication(applicationInfo);
  }

  @Implementation
  public List<ApplicationInfo> getInstalledApplications(int flags) {
    return getDelegatePackageManager().getInstalledApplications(flags);
  }

  @Implementation
  public String getInstallerPackageName(String packageName) {
    return getDelegatePackageManager().getInstallerPackageName(packageName);
  }

  @Implementation
  public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
    return getDelegatePackageManager().getPermissionInfo(name, flags);
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
    getDelegatePackageManager().freeStorageAndNotify(freeStorageSize, observer);
  }

  @Implementation
  public void freeStorageAndNotify(String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {
    getDelegatePackageManager().freeStorageAndNotify(volumeUuid, freeStorageSize, observer);
  }

  @Implementation
  public void setInstallerPackageName(String targetPackage, String installerPackageName) {
    getDelegatePackageManager().setInstallerPackageName(targetPackage, installerPackageName);
  }

  @Implementation
  public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
    return getDelegatePackageManager().queryIntentContentProviders(intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
    return getDelegatePackageManager().queryIntentContentProvidersAsUser(intent, flags, userId);
  }

  @Implementation
  public String getPermissionControllerPackageName() {
    return getDelegatePackageManager().getPermissionControllerPackageName();
  }

  @Implementation(maxSdk = JELLY_BEAN)
  public void getPackageSizeInfo(String packageName, IPackageStatsObserver observer) {
    getDelegatePackageManager()
        .getPackageSizeInfoAsUser(packageName, UserHandle.myUserId(), observer);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = M)
  public void getPackageSizeInfo(String pkgName, int uid, final IPackageStatsObserver callback) {
    getDelegatePackageManager().getPackageSizeInfoAsUser(pkgName, uid, callback);
  }

  @Implementation(minSdk = N)
  public void getPackageSizeInfoAsUser(String pkgName, int uid, final IPackageStatsObserver callback) {
    getDelegatePackageManager().getPackageSizeInfoAsUser(pkgName, uid, callback);
  }

  @Implementation
  public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
    getDelegatePackageManager().deletePackage(packageName, observer, flags);
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
    return getDelegatePackageManager().isSafeMode();
  }

  @Implementation
  public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    return getDelegatePackageManager().getApplicationIcon(packageName);
  }

  @Implementation
  public Drawable getApplicationIcon(ApplicationInfo info) {
    return getDelegatePackageManager().getApplicationIcon(info);
  }

  @Implementation
  public Drawable getUserBadgeForDensity(UserHandle userHandle, int i) {
    return getDelegatePackageManager().getUserBadgeForDensity(userHandle, i);
  }

  @Implementation
  public int checkSignatures(String pkg1, String pkg2) {
    return getDelegatePackageManager().checkSignatures(pkg1, pkg2);
  }

  @Implementation
  public int checkSignatures(int uid1, int uid2) {
    return getDelegatePackageManager().checkSignatures(uid1, uid2);
  }

  @Implementation
  public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
    return getDelegatePackageManager().queryPermissionsByGroup(group, flags);
  }

  public CharSequence getApplicationLabel(ApplicationInfo info) {
    return getRobolectricPackageManager().getApplicationLabel(info);
  }

  @Implementation
  public Intent getLaunchIntentForPackage(String packageName) {
    return getRobolectricPackageManager().getLaunchIntentForPackage(packageName);
  }

  ////////////////////////////

  @Implementation
  public PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return getDelegatePackageManager().getPackageInfoAsUser(packageName, flags, userId);
  }

  @Implementation
  public String[] canonicalToCurrentPackageNames(String[] names) {
    return getDelegatePackageManager().canonicalToCurrentPackageNames(names);
  }

  @Implementation
  public Intent getLeanbackLaunchIntentForPackage(String packageName) {
    return getDelegatePackageManager().getLeanbackLaunchIntentForPackage(packageName);
  }

  @Implementation
  public int[] getPackageGids(String packageName) throws NameNotFoundException {
    return getDelegatePackageManager().getPackageGids(packageName);
  }

  @Implementation
  public int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
    return getDelegatePackageManager().getPackageGids(packageName, flags);
  }

  @Implementation
  public int getPackageUid(String packageName, int flags) throws NameNotFoundException {
    return getDelegatePackageManager().getPackageUid(packageName, flags);
  }

  @Implementation
  public int getPackageUidAsUser(String packageName, int userId) throws NameNotFoundException {
    return getDelegatePackageManager().getPackageUidAsUser(packageName, userId);
  }

  @Implementation
  public int getPackageUidAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return getDelegatePackageManager().getPackageUidAsUser(packageName, flags, userId);
  }

  @Implementation
  public PermissionGroupInfo getPermissionGroupInfo(String name, int flags)
      throws NameNotFoundException {
    return getDelegatePackageManager().getPermissionGroupInfo(name, flags);
  }

  @Implementation
  public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
    return getDelegatePackageManager().getAllPermissionGroups(flags);
  }

  @Implementation
  public ApplicationInfo getApplicationInfoAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return getDelegatePackageManager().getApplicationInfoAsUser(packageName, flags, userId);
  }

  @Implementation
  public String[] getSystemSharedLibraryNames() {
    return getDelegatePackageManager().getSystemSharedLibraryNames();
  }

  @Implementation
  public
  @NonNull
  String getServicesSystemSharedLibraryPackageName() {
    return getDelegatePackageManager().getServicesSystemSharedLibraryPackageName();
  }

  @Implementation
  public
  @NonNull
  String getSharedSystemSharedLibraryPackageName() {
    return getDelegatePackageManager().getSharedSystemSharedLibraryPackageName();
  }

  @Implementation
  public boolean hasSystemFeature(String name, int version) {
    return getDelegatePackageManager().hasSystemFeature(name, version);
  }

  @Implementation
  public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
    return getDelegatePackageManager().isPermissionRevokedByPolicy(permName, pkgName);
  }

  @Implementation
  public boolean addPermission(PermissionInfo info) {
    return getDelegatePackageManager().addPermission(info);
  }

  @Implementation
  public boolean addPermissionAsync(PermissionInfo info) {
    return getDelegatePackageManager().addPermissionAsync(info);
  }

  @Implementation
  public void removePermission(String name) {
    getDelegatePackageManager().removePermission(name);
  }

  @Implementation
  public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
    getDelegatePackageManager().grantRuntimePermission(packageName, permissionName, user);
  }

  @Implementation
  public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
    getDelegatePackageManager().revokeRuntimePermission(packageName, permissionName, user);
  }

  @Implementation
  public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
    return getDelegatePackageManager().getPermissionFlags(permissionName, packageName, user);
  }

  @Implementation
  public void updatePermissionFlags(String permissionName, String packageName, int flagMask,
      int flagValues, UserHandle user) {
    getDelegatePackageManager()
        .updatePermissionFlags(permissionName, packageName, flagMask, flagValues, user);
  }

  @Implementation
  public int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
    return getDelegatePackageManager().getUidForSharedUser(sharedUserName);
  }

  @Implementation
  public List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
    return getDelegatePackageManager().getInstalledPackagesAsUser(flags, userId);
  }

  @Implementation
  public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
    return getDelegatePackageManager().getPackagesHoldingPermissions(permissions, flags);
  }

  @Implementation
  public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
    return getDelegatePackageManager().resolveActivityAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
    return getDelegatePackageManager().queryIntentActivitiesAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
    return getDelegatePackageManager().queryIntentActivityOptions(caller, specifics, intent, flags);
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
    return getDelegatePackageManager().queryBroadcastReceiversAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
    return getDelegatePackageManager().queryIntentServicesAsUser(intent, flags, userId);
  }

  @Implementation
  public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
    return getDelegatePackageManager().queryContentProviders(processName, uid, flags);
  }

  @Implementation
  public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
    return getDelegatePackageManager().getInstrumentationInfo(className, flags);
  }

  @Implementation
  public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
    return getDelegatePackageManager().queryInstrumentation(targetPackage, flags);
  }

  @Override @Nullable
  @Implementation
  public Drawable getDrawable(String packageName, @DrawableRes int resId, @Nullable ApplicationInfo appInfo) {
    return getDelegatePackageManager().getDrawable(packageName, resId, appInfo);
  }

  @Override @Implementation
  public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
    return getDelegatePackageManager().getActivityIcon(activityName);
  }

  @Override public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
    return getDelegatePackageManager().getActivityIcon(intent);
  }

  @Implementation
  public Drawable getDefaultActivityIcon() {
    return getDelegatePackageManager().getDefaultActivityIcon();
  }

  @Implementation
  public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
    return getDelegatePackageManager().getActivityBanner(activityName);
  }

  @Implementation
  public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
    return getDelegatePackageManager().getActivityBanner(intent);
  }

  @Implementation
  public Drawable getApplicationBanner(ApplicationInfo info) {
    return getDelegatePackageManager().getApplicationBanner(info);
  }

  @Implementation
  public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
    return getDelegatePackageManager().getApplicationBanner(packageName);
  }

  @Implementation
  public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
    return getDelegatePackageManager().getActivityLogo(activityName);
  }

  @Implementation
  public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
    return getDelegatePackageManager().getActivityLogo(intent);
  }

  @Implementation
  public Drawable getApplicationLogo(ApplicationInfo info) {
    return getDelegatePackageManager().getApplicationLogo(info);
  }

  @Implementation
  public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
    return getDelegatePackageManager().getApplicationLogo(packageName);
  }

  @Implementation
  public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
    return getDelegatePackageManager().getUserBadgedIcon(icon, user);
  }

  @Implementation
  public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
    return getDelegatePackageManager().getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
  }

  @Implementation
  public Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
    return getDelegatePackageManager().getUserBadgeForDensityNoBackground(user, density);
  }

  @Implementation
  public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
    return getDelegatePackageManager().getUserBadgedLabel(label, user);
  }

  @Implementation
  public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
    return getDelegatePackageManager().getResourcesForActivity(activityName);
  }

  @Implementation
  public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
    return getDelegatePackageManager().getResourcesForApplication(appPackageName);
  }

  @Implementation
  public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws NameNotFoundException {
    return getDelegatePackageManager().getResourcesForApplicationAsUser(appPackageName, userId);
  }

  @Implementation
  public void addOnPermissionsChangeListener(Object listener) {
    getDelegatePackageManager().addOnPermissionsChangeListener((OnPermissionsChangedListener) listener);
  }

  @Implementation
  public void removeOnPermissionsChangeListener(Object listener) {
    getDelegatePackageManager().removeOnPermissionsChangeListener((OnPermissionsChangedListener) listener);
  }

  @Implementation
  public CharSequence getText(String packageName, @StringRes int resid, ApplicationInfo appInfo) {
    return getDelegatePackageManager().getText(packageName, resid, appInfo);
  }

  @Implementation
  public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName) {
    getDelegatePackageManager().installPackage(packageURI, observer, flags, installerPackageName);
  }

  @Implementation
  public void installPackage(Object packageURI, Object observer, Object flags, Object installerPackageName) {
    getDelegatePackageManager().installPackage((Uri) packageURI, (PackageInstallObserver) observer, (int) flags, (String) installerPackageName);
  }

  @Implementation
  public int installExistingPackage(String packageName) throws NameNotFoundException {
    return getDelegatePackageManager().installExistingPackage(packageName);
  }

  @Implementation
  public int installExistingPackageAsUser(String packageName, int userId) throws NameNotFoundException {
    return getDelegatePackageManager().installExistingPackageAsUser(packageName, userId);
  }

  @Implementation
  public void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {
    getDelegatePackageManager().verifyIntentFilter(id, verificationCode, failedDomains);
  }

  @Implementation
  public int getIntentVerificationStatusAsUser(String packageName, int userId) {
    return getDelegatePackageManager().getIntentVerificationStatusAsUser(packageName, userId);
  }

  @Implementation
  public boolean updateIntentVerificationStatusAsUser(String packageName, int status, int userId) {
    return getDelegatePackageManager().updateIntentVerificationStatusAsUser(packageName, status, userId);
  }

  @Implementation
  public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
    return getDelegatePackageManager().getIntentFilterVerifications(packageName);
  }

  @Implementation
  public List<IntentFilter> getAllIntentFilters(String packageName) {
    return getDelegatePackageManager().getAllIntentFilters(packageName);
  }

  @Implementation
  public String getDefaultBrowserPackageNameAsUser(int userId) {
    return getDelegatePackageManager().getDefaultBrowserPackageNameAsUser(userId);
  }

  @Implementation
  public boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
    return getDelegatePackageManager().setDefaultBrowserPackageNameAsUser(packageName, userId);
  }

  @Implementation
  public int getMoveStatus(int moveId) {
    return getDelegatePackageManager().getMoveStatus(moveId);
  }

  @Implementation
  public void registerMoveCallback(Object callback, Object handler) {
    getDelegatePackageManager().registerMoveCallback((MoveCallback) callback, (Handler) handler);
  }

  @Implementation
  public void unregisterMoveCallback(Object callback) {
    getDelegatePackageManager().unregisterMoveCallback((MoveCallback) callback);
  }

  @Implementation
  public Object movePackage(Object packageName, Object vol) {
    return getDelegatePackageManager().movePackage((String) packageName, (VolumeInfo) vol);
  }

  @Implementation
  public Object getPackageCurrentVolume(Object app) {
    return getDelegatePackageManager().getPackageCurrentVolume((ApplicationInfo) app);
  }

  @Implementation
  public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
    return getDelegatePackageManager().getPackageCandidateVolumes(app);
  }

  @Implementation
  public Object movePrimaryStorage(Object vol) {
    return getDelegatePackageManager().movePrimaryStorage((VolumeInfo) vol);
  }

  @Implementation
  public @Nullable Object getPrimaryStorageCurrentVolume() {
    return getDelegatePackageManager().getPrimaryStorageCurrentVolume();
  }

  @Implementation
  public @NonNull List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
    return getDelegatePackageManager().getPrimaryStorageCandidateVolumes();
  }

  @Implementation
  public void deletePackageAsUser(String packageName, IPackageDeleteObserver observer, int flags, int userId) {
    getDelegatePackageManager().deletePackageAsUser(packageName, observer, flags, userId);
  }

  @Implementation
  public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
    getDelegatePackageManager().clearApplicationUserData(packageName, observer);
  }

  @Implementation
  public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
    getDelegatePackageManager().deleteApplicationCacheFiles(packageName, observer);
  }

  @Implementation
  public void deleteApplicationCacheFilesAsUser(String packageName, int userId, IPackageDataObserver observer) {
    getDelegatePackageManager().deleteApplicationCacheFilesAsUser(packageName, userId, observer);
  }

  @Implementation
  public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
    getDelegatePackageManager().freeStorage(volumeUuid, freeStorageSize, pi);
  }

  @Implementation
  public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, int userId) {
    return getDelegatePackageManager().setPackagesSuspendedAsUser(packageNames, suspended, userId);
  }

  @Implementation
  public boolean isPackageSuspendedForUser(String packageName, int userId) {
    return getDelegatePackageManager().isPackageSuspendedForUser(packageName, userId);
  }

  @Implementation
  public void addPackageToPreferred(String packageName) {
    getDelegatePackageManager().addPackageToPreferred(packageName);
  }

  @Implementation
  public void removePackageFromPreferred(String packageName) {
    getDelegatePackageManager().removePackageFromPreferred(packageName);
  }

  @Implementation
  public List<PackageInfo> getPreferredPackages(int flags) {
    return getDelegatePackageManager().getPreferredPackages(flags);
  }

  @Override public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    getDelegatePackageManager().addPreferredActivity(filter, match, set, activity);
  }

  @Implementation
  public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    getDelegatePackageManager().replacePreferredActivity(filter, match, set, activity);
  }

  @Implementation
  public void clearPackagePreferredActivities(String packageName) {
    getDelegatePackageManager().clearPackagePreferredActivities(packageName);
  }

  @Override public int getPreferredActivities(List<IntentFilter> outFilters,
      List<ComponentName> outActivities, String packageName) {
    return getDelegatePackageManager().getPreferredActivities(outFilters, outActivities, packageName);
  }

  @Implementation
  public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
    return getDelegatePackageManager().getHomeActivities(outActivities);
  }

  @Implementation
  public void flushPackageRestrictionsAsUser(int userId) {
    getDelegatePackageManager().flushPackageRestrictionsAsUser(userId);
  }

  @Implementation
  public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
    return getDelegatePackageManager().setApplicationHiddenSettingAsUser(packageName, hidden, user);
  }

  @Implementation
  public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
    return getDelegatePackageManager().getApplicationHiddenSettingAsUser(packageName, user);
  }

  @Implementation
  public Object getKeySetByAlias(String packageName, String alias) {
    return getDelegatePackageManager().getKeySetByAlias(packageName, alias);
  }

  @Implementation
  public Object getSigningKeySet(String packageName) {
    return getDelegatePackageManager().getSigningKeySet(packageName);
  }

  @Implementation
  public boolean isSignedBy(String packageName, Object ks) {
    return getDelegatePackageManager().isSignedBy(packageName, (KeySet) ks);
  }

  @Implementation
  public boolean isSignedByExactly(String packageName, Object ks) {
    return getDelegatePackageManager().isSignedByExactly(packageName, (KeySet) ks);
  }

  @Implementation
  public VerifierDeviceIdentity getVerifierDeviceIdentity() {
    return getDelegatePackageManager().getVerifierDeviceIdentity();
  }

  @Implementation
  public boolean isUpgrade() {
    return getDelegatePackageManager().isUpgrade();
  }

  @Implementation
  public boolean isPackageAvailable(String packageName) {
    return getDelegatePackageManager().isPackageAvailable(packageName);
  }

  @Implementation
  public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
    getDelegatePackageManager().addCrossProfileIntentFilter(filter, sourceUserId, targetUserId, flags);
  }

  @Implementation
  public void clearCrossProfileIntentFilters(int sourceUserId) {
    getDelegatePackageManager().clearCrossProfileIntentFilters(sourceUserId);
  }

  @Implementation
  public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return getDelegatePackageManager().loadItemIcon(itemInfo, appInfo);
  }

  @Implementation
  public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return getDelegatePackageManager().loadUnbadgedItemIcon(itemInfo, appInfo);
  }
}
