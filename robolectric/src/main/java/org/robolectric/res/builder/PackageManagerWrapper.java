package org.robolectric.res.builder;

import android.app.PackageInstallObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ContainerEncryptionParams;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
import android.content.pm.ManifestDigest;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.VerificationParams;
import android.content.pm.VerifierDeviceIdentity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import org.robolectric.manifest.AndroidManifest;

import java.util.List;

public class PackageManagerWrapper extends PackageManager implements RobolectricPackageManager {
  private PackageManager delegate;
  private RobolectricPackageManager delegateRPM;

  public PackageManagerWrapper(PackageManager delegate, RobolectricPackageManager delegateRPM) {
    this.delegate = delegate;
    this.delegateRPM = delegateRPM;
  }

  public PackageManagerWrapper(DefaultPackageManager delegate) {
    this(delegate, delegate);
  }

  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    return delegate.getPackageInfo(packageName, flags);
  }

  public String[] currentToCanonicalPackageNames(String[] names) {
    return delegate.currentToCanonicalPackageNames(names);
  }

  public String[] canonicalToCurrentPackageNames(String[] names) {
    return delegate.canonicalToCurrentPackageNames(names);
  }

  public Intent getLaunchIntentForPackage(String packageName) {
    return delegate.getLaunchIntentForPackage(packageName);
  }

  public Intent getLeanbackLaunchIntentForPackage(String packageName) {
    return delegate.getLeanbackLaunchIntentForPackage(packageName);
  }

  public int[] getPackageGids(String packageName) throws NameNotFoundException {
    return delegate.getPackageGids(packageName);
  }

  public int getPackageUid(String packageName, int userHandle) throws NameNotFoundException {
    return delegate.getPackageUid(packageName, userHandle);
  }

  public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
    return delegate.getPermissionInfo(name, flags);
  }

  public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
    return delegate.queryPermissionsByGroup(group, flags);
  }

  public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
    return delegate.getPermissionGroupInfo(name, flags);
  }

  public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
    return delegate.getAllPermissionGroups(flags);
  }

  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    return delegate.getApplicationInfo(packageName, flags);
  }

  public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
    return delegate.getActivityInfo(className, flags);
  }

  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    return delegate.getReceiverInfo(className, flags);
  }

  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    return delegate.getServiceInfo(className, flags);
  }

  public ProviderInfo getProviderInfo(ComponentName className, int flags) throws NameNotFoundException {
    return delegate.getProviderInfo(className, flags);
  }

  public String[] getSystemSharedLibraryNames() {
    return delegate.getSystemSharedLibraryNames();
  }

  public FeatureInfo[] getSystemAvailableFeatures() {
    return delegate.getSystemAvailableFeatures();
  }

  public boolean hasSystemFeature(String name) {
    return delegate.hasSystemFeature(name);
  }

  @Override
  public void setSystemFeature(String name, boolean supported) {
    delegateRPM.setSystemFeature(name, supported);
  }

  @Override
  public void addDrawableResolution(String packageName, int resourceId, Drawable drawable) {
    delegateRPM.addDrawableResolution(packageName, resourceId, drawable);
  }

  public int checkPermission(String permName, String pkgName) {
    return delegate.checkPermission(permName, pkgName);
  }

  @Override
  public boolean isQueryIntentImplicitly() {
    return delegateRPM.isQueryIntentImplicitly();
  }

  @Override
  public void setQueryIntentImplicitly(boolean queryIntentImplicitly) {
    delegateRPM.setQueryIntentImplicitly(queryIntentImplicitly);
  }

  @Override
  public void reset() {
    delegateRPM.reset();
  }

  @Override
  public void setNameForUid(int uid, String name) {
    delegateRPM.setNameForUid(uid, name);
  }

  @Override
  public void setPackagesForCallingUid(String... packagesForCallingUid) {
    delegateRPM.setPackagesForCallingUid(packagesForCallingUid);
  }

  @Override
  public void setPackagesForUid(int uid, String... packagesForCallingUid) {
    delegateRPM.setPackagesForUid(uid, packagesForCallingUid);
  }

  public boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
    return delegate.isPermissionRevokedByPolicy(permName, pkgName);
  }

  public String getPermissionControllerPackageName() {
    return delegate.getPermissionControllerPackageName();
  }

  public boolean addPermission(PermissionInfo info) {
    return delegate.addPermission(info);
  }

  public boolean addPermissionAsync(PermissionInfo info) {
    return delegate.addPermissionAsync(info);
  }

  public void removePermission(String name) {
    delegate.removePermission(name);
  }

  public void grantRuntimePermission(String packageName, String permissionName, UserHandle user) {
    delegate.grantRuntimePermission(packageName, permissionName, user);
  }

  public void revokeRuntimePermission(String packageName, String permissionName, UserHandle user) {
    delegate.revokeRuntimePermission(packageName, permissionName, user);
  }

  public int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
    return delegate.getPermissionFlags(permissionName, packageName, user);
  }

  public void updatePermissionFlags(String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {
    delegate.updatePermissionFlags(permissionName, packageName, flagMask, flagValues, user);
  }

  public boolean shouldShowRequestPermissionRationale(String permission) {
    return delegate.shouldShowRequestPermissionRationale(permission);
  }

  public int checkSignatures(String pkg1, String pkg2) {
    return delegate.checkSignatures(pkg1, pkg2);
  }

  public int checkSignatures(int uid1, int uid2) {
    return delegate.checkSignatures(uid1, uid2);
  }

  public String[] getPackagesForUid(int uid) {
    return delegate.getPackagesForUid(uid);
  }

  public String getNameForUid(int uid) {
    return delegate.getNameForUid(uid);
  }

  public int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
    return delegate.getUidForSharedUser(sharedUserName);
  }

  public List<PackageInfo> getInstalledPackages(int flags) {
    return delegate.getInstalledPackages(flags);
  }

  public List<PackageInfo> getInstalledPackages(int flags, int userId) {
    return delegate.getInstalledPackages(flags, userId);
  }

  public List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
    return delegate.getPackagesHoldingPermissions(permissions, flags);
  }

  public List<ApplicationInfo> getInstalledApplications(int flags) {
    return delegate.getInstalledApplications(flags);
  }

  public ResolveInfo resolveActivity(Intent intent, int flags) {
    return delegate.resolveActivity(intent, flags);
  }

  public ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
    return delegate.resolveActivityAsUser(intent, flags, userId);
  }

  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    return delegate.queryIntentActivities(intent, flags);
  }

  public List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
    return delegate.queryIntentActivitiesAsUser(intent, flags, userId);
  }

  public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
    return delegate.queryIntentActivityOptions(caller, specifics, intent, flags);
  }

  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, int userId) {
    return delegate.queryBroadcastReceivers(intent, flags, userId);
  }

  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    return delegate.queryBroadcastReceivers(intent, flags);
  }

  public ResolveInfo resolveService(Intent intent, int flags) {
    return delegate.resolveService(intent, flags);
  }

  @Override
  public void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info) {
    delegateRPM.addResolveInfoForIntent(intent, info);
  }

  @Override
  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    delegateRPM.addResolveInfoForIntent(intent, info);
  }

  @Override
  public void removeResolveInfosForIntent(Intent intent, String packageName) {
    delegateRPM.removeResolveInfosForIntent(intent, packageName);
  }

  public List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
    return delegate.queryIntentServicesAsUser(intent, flags, userId);
  }

  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    return delegate.queryIntentServices(intent, flags);
  }

  public List<ResolveInfo> queryIntentContentProvidersAsUser(Intent intent, int flags, int userId) {
    return delegate.queryIntentContentProvidersAsUser(intent, flags, userId);
  }

  public List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
    return delegate.queryIntentContentProviders(intent, flags);
  }

  public ProviderInfo resolveContentProvider(String name, int flags) {
    return delegate.resolveContentProvider(name, flags);
  }

  public ProviderInfo resolveContentProviderAsUser(String name, int flags, int userId) {
    return delegate.resolveContentProviderAsUser(name, flags, userId);
  }

  public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
    return delegate.queryContentProviders(processName, uid, flags);
  }

  public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
    return delegate.getInstrumentationInfo(className, flags);
  }

  public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
    return delegate.queryInstrumentation(targetPackage, flags);
  }

  public Drawable getDrawable(String packageName, int resId, ApplicationInfo appInfo) {
    return delegate.getDrawable(packageName, resId, appInfo);
  }

  public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
    return delegate.getActivityIcon(activityName);
  }

  @Override
  public void addActivityIcon(ComponentName component, Drawable d) {
    delegateRPM.addActivityIcon(component, d);
  }

  @Override
  public void addActivityIcon(Intent intent, Drawable d) {
    delegateRPM.addActivityIcon(intent, d);
  }

  public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
    return delegate.getActivityIcon(intent);
  }

  public Drawable getDefaultActivityIcon() {
    return delegate.getDefaultActivityIcon();
  }

  public Drawable getApplicationIcon(ApplicationInfo info) {
    return delegate.getApplicationIcon(info);
  }

  public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    return delegate.getApplicationIcon(packageName);
  }

  @Override
  public void setApplicationIcon(String packageName, Drawable d) {
    delegateRPM.setApplicationIcon(packageName, d);
  }

  public Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
    return delegate.getActivityBanner(activityName);
  }

  public Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
    return delegate.getActivityBanner(intent);
  }

  public Drawable getApplicationBanner(ApplicationInfo info) {
    return delegate.getApplicationBanner(info);
  }

  public Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
    return delegate.getApplicationBanner(packageName);
  }

  public Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
    return delegate.getActivityLogo(activityName);
  }

  public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
    return delegate.getActivityLogo(intent);
  }

  public Drawable getApplicationLogo(ApplicationInfo info) {
    return delegate.getApplicationLogo(info);
  }

  public Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
    return delegate.getApplicationLogo(packageName);
  }

  public Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
    return delegate.getUserBadgedIcon(icon, user);
  }

  public Drawable getUserBadgedDrawableForDensity(Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
    return delegate.getUserBadgedDrawableForDensity(drawable, user, badgeLocation, badgeDensity);
  }

  public Drawable getUserBadgeForDensity(UserHandle user, int density) {
    return delegate.getUserBadgeForDensity(user, density);
  }

  public CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
    return delegate.getUserBadgedLabel(label, user);
  }

  public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
    return delegate.getResourcesForActivity(activityName);
  }

  public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
    return delegate.getResourcesForApplication(app);
  }

  public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
    return delegate.getResourcesForApplication(appPackageName);
  }

  public Resources getResourcesForApplicationAsUser(String appPackageName, int userId) throws NameNotFoundException {
    return delegate.getResourcesForApplicationAsUser(appPackageName, userId);
  }

  public boolean isSafeMode() {
    return delegate.isSafeMode();
  }

  public void addOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
    delegate.addOnPermissionsChangeListener(listener);
  }

  public void removeOnPermissionsChangeListener(OnPermissionsChangedListener listener) {
    delegate.removeOnPermissionsChangeListener(listener);
  }

  public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
    return delegate.getText(packageName, resid, appInfo);
  }

  public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
    return delegate.getXml(packageName, resid, appInfo);
  }

  public CharSequence getApplicationLabel(ApplicationInfo info) {
    return delegate.getApplicationLabel(info);
  }

  public void installPackage(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName) {
    delegate.installPackage(packageURI, observer, flags, installerPackageName);
  }

  public void installPackageWithVerification(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName, Uri verificationURI, ManifestDigest manifestDigest, ContainerEncryptionParams encryptionParams) {
    delegate.installPackageWithVerification(packageURI, observer, flags, installerPackageName, verificationURI, manifestDigest, encryptionParams);
  }

  public void installPackageWithVerificationAndEncryption(Uri packageURI, IPackageInstallObserver observer, int flags, String installerPackageName, VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {
    delegate.installPackageWithVerificationAndEncryption(packageURI, observer, flags, installerPackageName, verificationParams, encryptionParams);
  }

  public void installPackage(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName) {
    delegate.installPackage(packageURI, observer, flags, installerPackageName);
  }

  public void installPackageWithVerification(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName, Uri verificationURI, ManifestDigest manifestDigest, ContainerEncryptionParams encryptionParams) {
    delegate.installPackageWithVerification(packageURI, observer, flags, installerPackageName, verificationURI, manifestDigest, encryptionParams);
  }

  public void installPackageWithVerificationAndEncryption(Uri packageURI, PackageInstallObserver observer, int flags, String installerPackageName, VerificationParams verificationParams, ContainerEncryptionParams encryptionParams) {
    delegate.installPackageWithVerificationAndEncryption(packageURI, observer, flags, installerPackageName, verificationParams, encryptionParams);
  }

  public int installExistingPackage(String packageName) throws NameNotFoundException {
    return delegate.installExistingPackage(packageName);
  }

  public void verifyPendingInstall(int id, int response) {
    delegate.verifyPendingInstall(id, response);
  }

  public void extendVerificationTimeout(int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
    delegate.extendVerificationTimeout(id, verificationCodeAtTimeout, millisecondsToDelay);
  }

  public void verifyIntentFilter(int id, int verificationCode, List<String> outFailedDomains) {
    delegate.verifyIntentFilter(id, verificationCode, outFailedDomains);
  }

  public int getIntentVerificationStatus(String packageName, int userId) {
    return delegate.getIntentVerificationStatus(packageName, userId);
  }

  public boolean updateIntentVerificationStatus(String packageName, int status, int userId) {
    return delegate.updateIntentVerificationStatus(packageName, status, userId);
  }

  public List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
    return delegate.getIntentFilterVerifications(packageName);
  }

  public List<IntentFilter> getAllIntentFilters(String packageName) {
    return delegate.getAllIntentFilters(packageName);
  }

  public String getDefaultBrowserPackageName(int userId) {
    return delegate.getDefaultBrowserPackageName(userId);
  }

  public boolean setDefaultBrowserPackageName(String packageName, int userId) {
    return delegate.setDefaultBrowserPackageName(packageName, userId);
  }

  public void setInstallerPackageName(String targetPackage, String installerPackageName) {
    delegate.setInstallerPackageName(targetPackage, installerPackageName);
  }

  public String getInstallerPackageName(String packageName) {
    return delegate.getInstallerPackageName(packageName);
  }

  public int getMoveStatus(int moveId) {
    return delegate.getMoveStatus(moveId);
  }

  public void registerMoveCallback(MoveCallback callback, Handler handler) {
    delegate.registerMoveCallback(callback, handler);
  }

  public void unregisterMoveCallback(MoveCallback callback) {
    delegate.unregisterMoveCallback(callback);
  }

  public int movePackage(String packageName, VolumeInfo vol) {
    return delegate.movePackage(packageName, vol);
  }

  public VolumeInfo getPackageCurrentVolume(ApplicationInfo app) {
    return delegate.getPackageCurrentVolume(app);
  }

  public List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
    return delegate.getPackageCandidateVolumes(app);
  }

  public int movePrimaryStorage(VolumeInfo vol) {
    return delegate.movePrimaryStorage(vol);
  }

  public VolumeInfo getPrimaryStorageCurrentVolume() {
    return delegate.getPrimaryStorageCurrentVolume();
  }

  public List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
    return delegate.getPrimaryStorageCandidateVolumes();
  }

  public void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
    delegate.deletePackage(packageName, observer, flags);
  }

  public void clearApplicationUserData(String packageName, IPackageDataObserver observer) {
    delegate.clearApplicationUserData(packageName, observer);
  }

  public void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
    delegate.deleteApplicationCacheFiles(packageName, observer);
  }

  public void freeStorageAndNotify(String volumeUuid, long idealStorageSize, IPackageDataObserver observer) {
    delegate.freeStorageAndNotify(volumeUuid, idealStorageSize, observer);
  }

  public void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {
    delegate.freeStorage(volumeUuid, freeStorageSize, pi);
  }

  public void getPackageSizeInfo(String packageName, int userHandle, IPackageStatsObserver observer) {
    delegate.getPackageSizeInfo(packageName, userHandle, observer);
  }

  public void addPackageToPreferred(String packageName) {
    delegate.addPackageToPreferred(packageName);
  }

  public void removePackageFromPreferred(String packageName) {
    delegate.removePackageFromPreferred(packageName);
  }

  public List<PackageInfo> getPreferredPackages(int flags) {
    return delegate.getPreferredPackages(flags);
  }

  public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    delegate.addPreferredActivity(filter, match, set, activity);
  }

  public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
    delegate.addPreferredActivity(filter, match, set, activity, userId);
  }

  public void replacePreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    delegate.replacePreferredActivity(filter, match, set, activity);
  }

  public void replacePreferredActivityAsUser(IntentFilter filter, int match, ComponentName[] set, ComponentName activity, int userId) {
    delegate.replacePreferredActivityAsUser(filter, match, set, activity, userId);
  }

  public void clearPackagePreferredActivities(String packageName) {
    delegate.clearPackagePreferredActivities(packageName);
  }

  public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    return delegate.getPreferredActivities(outFilters, outActivities, packageName);
  }

  @Override
  public ComponentState getComponentState(ComponentName componentName) {
    return delegateRPM.getComponentState(componentName);
  }

  @Override
  public void addPackage(PackageInfo packageInfo) {
    delegateRPM.addPackage(packageInfo);
  }

  @Override
  public void addPackage(String packageName) {
    delegateRPM.addPackage(packageName);
  }

  @Override
  public void addManifest(AndroidManifest androidManifest, int labelRes) {
    delegateRPM.addManifest(androidManifest, labelRes);
  }

  @Override
  public void removePackage(String packageName) {
    delegateRPM.removePackage(packageName);
  }

  public ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
    return delegate.getHomeActivities(outActivities);
  }

  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    delegate.setComponentEnabledSetting(componentName, newState, flags);
  }

  public int getComponentEnabledSetting(ComponentName componentName) {
    return delegate.getComponentEnabledSetting(componentName);
  }

  public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    delegate.setApplicationEnabledSetting(packageName, newState, flags);
  }

  public int getApplicationEnabledSetting(String packageName) {
    return delegate.getApplicationEnabledSetting(packageName);
  }

  public boolean setApplicationHiddenSettingAsUser(String packageName, boolean hidden, UserHandle user) {
    return delegate.setApplicationHiddenSettingAsUser(packageName, hidden, user);
  }

  public boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
    return delegate.getApplicationHiddenSettingAsUser(packageName, user);
  }

  public KeySet getKeySetByAlias(String packageName, String alias) {
    return delegate.getKeySetByAlias(packageName, alias);
  }

  public KeySet getSigningKeySet(String packageName) {
    return delegate.getSigningKeySet(packageName);
  }

  public boolean isSignedBy(String packageName, KeySet ks) {
    return delegate.isSignedBy(packageName, ks);
  }

  public boolean isSignedByExactly(String packageName, KeySet ks) {
    return delegate.isSignedByExactly(packageName, ks);
  }

  public VerifierDeviceIdentity getVerifierDeviceIdentity() {
    return delegate.getVerifierDeviceIdentity();
  }

  public boolean isUpgrade() {
    return delegate.isUpgrade();
  }

  public PackageInstaller getPackageInstaller() {
    return delegate.getPackageInstaller();
  }

  public boolean isPackageAvailable(String packageName) {
    return delegate.isPackageAvailable(packageName);
  }

  public void addCrossProfileIntentFilter(IntentFilter filter, int sourceUserId, int targetUserId, int flags) {
    delegate.addCrossProfileIntentFilter(filter, sourceUserId, targetUserId, flags);
  }

  public void clearCrossProfileIntentFilters(int sourceUserId) {
    delegate.clearCrossProfileIntentFilters(sourceUserId);
  }

  public Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return delegate.loadItemIcon(itemInfo, appInfo);
  }

  public Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return delegate.loadUnbadgedItemIcon(itemInfo, appInfo);
  }
}
