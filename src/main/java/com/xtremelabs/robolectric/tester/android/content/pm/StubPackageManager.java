package com.xtremelabs.robolectric.tester.android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.InstrumentationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import java.util.List;

public class StubPackageManager extends PackageManager {
    @Override
    public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
        return null;
    }

    @Override public String[] currentToCanonicalPackageNames(String[] strings) {
        return new String[0];
    }

    @Override public String[] canonicalToCurrentPackageNames(String[] strings) {
        return new String[0];
    }

    @Override public Intent getLaunchIntentForPackage(String packageName) {
        return null;
    }

    @Override public int[] getPackageGids(String packageName) throws NameNotFoundException {
        return new int[0];
    }

    @Override
    public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public List<PermissionInfo> queryPermissionsByGroup(String group, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public PermissionGroupInfo getPermissionGroupInfo(String name, int flags) throws NameNotFoundException {
        return null;
    }

    @Override public List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
        return null;
    }

    @Override
    public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
        return null;
    }

    @Override
    public ProviderInfo getProviderInfo(ComponentName componentName, int i) throws NameNotFoundException {
        return null;
    }

    @Override public List<PackageInfo> getInstalledPackages(int flags) {
        return null;
    }

    @Override public int checkPermission(String permName, String pkgName) {
        return 0;
    }

    @Override public boolean addPermission(PermissionInfo info) {
        return false;
    }

    @Override public boolean addPermissionAsync(PermissionInfo permissionInfo) {
        return false;
    }

    @Override public void removePermission(String name) {
    }

    @Override public int checkSignatures(String pkg1, String pkg2) {
        return 0;
    }

    @Override public int checkSignatures(int uid1, int uid2) {
        return 0;
    }

    @Override public String[] getPackagesForUid(int uid) {
        return new String[0];
    }

    @Override public String getNameForUid(int uid) {
        return null;
    }

    @Override public List<ApplicationInfo> getInstalledApplications(int flags) {
        return null;
    }

    @Override public String[] getSystemSharedLibraryNames() {
        return new String[0];
    }

    @Override public FeatureInfo[] getSystemAvailableFeatures() {
        return new FeatureInfo[0];
    }

    @Override public boolean hasSystemFeature(String name) {
        return false;
    }

    @Override public ResolveInfo resolveActivity(Intent intent, int flags) {
        return null;
    }

    @Override public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
        return null;
    }

    @Override
    public List<ResolveInfo> queryIntentActivityOptions(ComponentName caller, Intent[] specifics, Intent intent, int flags) {
        return null;
    }

    @Override public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
        return null;
    }

    @Override public ResolveInfo resolveService(Intent intent, int flags) {
        return null;
    }

    @Override public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
        return null;
    }

    @Override public ProviderInfo resolveContentProvider(String name, int flags) {
        return null;
    }

    @Override public List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
        return null;
    }

    @Override
    public InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags) throws NameNotFoundException {
        return null;
    }

    @Override public List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
        return null;
    }

    @Override public Drawable getDrawable(String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override public Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
        return null;
    }

    @Override public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
        return null;
    }

    @Override public Drawable getDefaultActivityIcon() {
        return null;
    }

    @Override public Drawable getApplicationIcon(ApplicationInfo info) {
        return null;
    }

    @Override public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
        return null;
    }

    @Override public Drawable getActivityLogo(ComponentName componentName) throws NameNotFoundException {
        return null;
    }

    @Override public Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
        return null;
    }

    @Override public Drawable getApplicationLogo(ApplicationInfo applicationInfo) {
        return null;
    }

    @Override public Drawable getApplicationLogo(String s) throws NameNotFoundException {
        return null;
    }

    @Override public CharSequence getText(String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override public XmlResourceParser getXml(String packageName, int resid, ApplicationInfo appInfo) {
        return null;
    }

    @Override public CharSequence getApplicationLabel(ApplicationInfo info) {
        return null;
    }

    @Override
    public Resources getResourcesForActivity(ComponentName activityName) throws NameNotFoundException {
        return null;
    }

    @Override
    public Resources getResourcesForApplication(ApplicationInfo app) throws NameNotFoundException {
        return null;
    }

    @Override
    public Resources getResourcesForApplication(String appPackageName) throws NameNotFoundException {
        return null;
    }

    @Override public String getInstallerPackageName(String packageName) {
        return null;
    }

    @Override public void addPackageToPreferred(String packageName) {
    }

    @Override public void removePackageFromPreferred(String packageName) {
    }

    @Override public List<PackageInfo> getPreferredPackages(int flags) {
        return null;
    }

    @Override
    public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    }

    @Override public void clearPackagePreferredActivities(String packageName) {
    }

    @Override
    public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
        return 0;
    }

    @Override public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    }

    @Override public int getComponentEnabledSetting(ComponentName componentName) {
        return 0;
    }

    @Override public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    }

    @Override public int getApplicationEnabledSetting(String packageName) {
        return 0;
    }

    @Override public boolean isSafeMode() {
        return false;
    }

	@Override public void verifyPendingInstall(int id, int verificationCode) {
	}

	@Override public void setInstallerPackageName(String targetPackage, String installerPackageName) {
	}
}
