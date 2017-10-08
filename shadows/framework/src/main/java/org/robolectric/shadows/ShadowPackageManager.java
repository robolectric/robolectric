package org.robolectric.shadows;

import static android.content.pm.ApplicationInfo.FLAG_ALLOW_BACKUP;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA;
import static android.content.pm.ApplicationInfo.FLAG_ALLOW_TASK_REPARENTING;
import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.content.pm.ApplicationInfo.FLAG_HAS_CODE;
import static android.content.pm.ApplicationInfo.FLAG_KILL_AFTER_RESTORE;
import static android.content.pm.ApplicationInfo.FLAG_PERSISTENT;
import static android.content.pm.ApplicationInfo.FLAG_RESIZEABLE_FOR_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_RESTORE_ANY_VERSION;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_LARGE_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_NORMAL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SCREEN_DENSITIES;
import static android.content.pm.ApplicationInfo.FLAG_SUPPORTS_SMALL_SCREENS;
import static android.content.pm.ApplicationInfo.FLAG_TEST_ONLY;
import static android.content.pm.ApplicationInfo.FLAG_VM_SAFE_MODE;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_CONFIGURATIONS;
import static android.content.pm.PackageManager.GET_GIDS;
import static android.content.pm.PackageManager.GET_INSTRUMENTATION;
import static android.content.pm.PackageManager.GET_INTENT_FILTERS;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_PERMISSIONS;
import static android.content.pm.PackageManager.GET_PROVIDERS;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_RESOLVED_FILTER;
import static android.content.pm.PackageManager.GET_SERVICES;
import static android.content.pm.PackageManager.GET_SHARED_LIBRARY_FILES;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.GET_URI_PERMISSION_PATTERNS;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_AWARE;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;
import static android.content.pm.PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS;
import static android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES;
import static android.content.pm.PackageManager.SIGNATURE_FIRST_NOT_SIGNED;
import static android.content.pm.PackageManager.SIGNATURE_MATCH;
import static android.content.pm.PackageManager.SIGNATURE_NEITHER_SIGNED;
import static android.content.pm.PackageManager.SIGNATURE_NO_MATCH;
import static android.content.pm.PackageManager.SIGNATURE_SECOND_NOT_SIGNED;
import static android.os.Build.VERSION_CODES.N;
import static java.util.Arrays.asList;

import android.Manifest;
import android.annotation.UserIdInt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageStats;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Pair;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.manifest.ActivityData;
import org.robolectric.manifest.PermissionItemData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.util.TempDirectory;

@Implements(PackageManager.class)
public class ShadowPackageManager {

  private static final List<Pair<String, Integer>> APPLICATION_FLAGS = asList(
      Pair.create("android:allowBackup", FLAG_ALLOW_BACKUP),
      Pair.create("android:allowClearUserData", FLAG_ALLOW_CLEAR_USER_DATA),
      Pair.create("android:allowTaskReparenting", FLAG_ALLOW_TASK_REPARENTING),
      Pair.create("android:debuggable", FLAG_DEBUGGABLE),
      Pair.create("android:hasCode", FLAG_HAS_CODE),
      Pair.create("android:killAfterRestore", FLAG_KILL_AFTER_RESTORE),
      Pair.create("android:persistent", FLAG_PERSISTENT),
      Pair.create("android:resizeable", FLAG_RESIZEABLE_FOR_SCREENS),
      Pair.create("android:restoreAnyVersion", FLAG_RESTORE_ANY_VERSION),
      Pair.create("android:largeScreens", FLAG_SUPPORTS_LARGE_SCREENS),
      Pair.create("android:normalScreens", FLAG_SUPPORTS_NORMAL_SCREENS),
      Pair.create("android:anyDensity", FLAG_SUPPORTS_SCREEN_DENSITIES),
      Pair.create("android:smallScreens", FLAG_SUPPORTS_SMALL_SCREENS),
      Pair.create("android:testOnly", FLAG_TEST_ONLY),
      Pair.create("android:vmSafeMode", FLAG_VM_SAFE_MODE)
  );

  private static final List<Pair<String, Integer>> CONFIG_OPTIONS = asList(
      Pair.create("mcc", ActivityInfo.CONFIG_MCC),
      Pair.create("mnc", ActivityInfo.CONFIG_MNC),
      Pair.create("locale", ActivityInfo.CONFIG_LOCALE),
      Pair.create("touchscreen", ActivityInfo.CONFIG_TOUCHSCREEN),
      Pair.create("keyboard", ActivityInfo.CONFIG_KEYBOARD),
      Pair.create("keyboardHidden", ActivityInfo.CONFIG_KEYBOARD_HIDDEN),
      Pair.create("navigation", ActivityInfo.CONFIG_NAVIGATION),
      Pair.create("screenLayout", ActivityInfo.CONFIG_SCREEN_LAYOUT),
      Pair.create("fontScale", ActivityInfo.CONFIG_FONT_SCALE),
      Pair.create("uiMode", ActivityInfo.CONFIG_UI_MODE),
      Pair.create("orientation", ActivityInfo.CONFIG_ORIENTATION),
      Pair.create("screenSize", ActivityInfo.CONFIG_SCREEN_SIZE),
      Pair.create("smallestScreenSize", ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE)
  );


  Map<String, Boolean> permissionRationaleMap = new HashMap<>();
  List<FeatureInfo> systemAvailableFeatures = new LinkedList<>();
  final Map<String, PackageInfo> packageInfos = new LinkedHashMap<>();
  final Map<String, Package> packages = new LinkedHashMap<>();
  private Map<String, PackageInfo> packageArchiveInfo = new HashMap<>();
  final Map<String, PackageStats> packageStatsMap = new HashMap<>();
  final Map<String, String> packageInstallerMap = new HashMap<>();
  final Map<Integer, String[]> packagesForUid = new HashMap<>();
  final Map<String, Integer> uidForPackage = new HashMap<>();
  final Map<Integer, String> namesForUid = new HashMap<>();
  final Map<Integer, Integer> verificationResults = new HashMap<>();
  final Map<Integer, Long> verificationTimeoutExtension = new HashMap<>();
  final Map<String, String> currentToCanonicalNames = new HashMap<>();
  final Map<ComponentName, ComponentState> componentList = new LinkedHashMap<>();
  final Map<ComponentName, Drawable> drawableList = new LinkedHashMap<>();
  final Map<String, Drawable> applicationIcons = new HashMap<>();
  final Map<String, Boolean> systemFeatureList = new LinkedHashMap<>();
  final Map<IntentFilter, ComponentName> preferredActivities = new LinkedHashMap<>();
  final Map<Pair<String, Integer>, Drawable> drawables = new LinkedHashMap<>();
  final Map<String, Integer> applicationEnabledSettingMap = new HashMap<>();
  boolean queryIntentImplicitly = false;
  Map<String, PermissionInfo> extraPermissions = new HashMap<>();
  public Map<String, Resources> resources = new HashMap<>();
  private final Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new TreeMap<>(new IntentComparator());
  private Set<String> deletedPackages = new HashSet<>();
  Map<String, IPackageDeleteObserver> pendingDeleteCallbacks = new HashMap<>();

  /**
   * Goes through the meta data and puts each value in to a
   * bundle as the correct type.
   *
   * Note that this will convert resource identifiers specified
   * via the value attribute as well.
   * @param meta Meta data to put in to a bundle
   * @return bundle containing the meta data
   */
  private static Bundle metaDataToBundle(Map<String, Object> meta) {
    if (meta.size() == 0) {
        return null;
    }

    Bundle bundle = new Bundle();

    for (Map.Entry<String,Object> entry : meta.entrySet()) {
      if (Boolean.class.isInstance(entry.getValue())) {
        bundle.putBoolean(entry.getKey(), (Boolean) entry.getValue());
      } else if (Float.class.isInstance(entry.getValue())) {
        bundle.putFloat(entry.getKey(), (Float) entry.getValue());
      } else if (Integer.class.isInstance(entry.getValue())) {
        bundle.putInt(entry.getKey(), (Integer) entry.getValue());
      } else {
        bundle.putString(entry.getKey(), entry.getValue().toString());
      }
    }
    return bundle;
  }

  // From com.android.server.pm.PackageManagerService.compareSignatures().
  static int compareSignature(Signature[] signatures1, Signature[] signatures2) {
    if (signatures1 == null) {
      return (signatures2 == null) ? SIGNATURE_NEITHER_SIGNED
          : SIGNATURE_FIRST_NOT_SIGNED;
    }
    if (signatures2 == null) {
      return SIGNATURE_SECOND_NOT_SIGNED;
    }
    if (signatures1.length != signatures2.length) {
      return SIGNATURE_NO_MATCH;
    }
    HashSet<Signature> signatures1set = new HashSet<>(Arrays.asList(signatures1));
    HashSet<Signature> signatures2set = new HashSet<>(Arrays.asList(signatures2));
    return signatures1set.equals(signatures2set) ? SIGNATURE_MATCH : SIGNATURE_NO_MATCH;
  }

  static String resolvePackageName(String packageName, ComponentName componentName) {
    String classString = componentName.getClassName();
    int index = classString.indexOf('.');
    if (index == -1) {
      classString = packageName + "." + classString;
    } else if (index == 0) {
      classString = packageName + classString;
    }
    return classString;
  }

  static IntentFilter matchIntentFilter(Intent intent, ArrayList<? extends IntentInfo> intentFilters) {
    for (IntentInfo intentInfo : intentFilters) {
      if (intentInfo.match(intent.getAction(), intent.getType(), intent.getScheme(), intent.getData(), intent.getCategories(), "ShadowPackageManager") >= 0) {
        return intentInfo;
      }
    }
    return null;
  }

  static ResolveInfo getResolveInfo(Activity activity, IntentFilter intentFilter) {
    ResolveInfo info = new ResolveInfo();
    info.isDefault = intentFilter.hasCategory("Intent.CATEGORY_DEFAULT");
    info.activityInfo = new ActivityInfo();
    info.activityInfo.name = activity.info.name;
    info.activityInfo.packageName = activity.info.packageName;
    info.activityInfo.applicationInfo = activity.info.applicationInfo;
    info.activityInfo.permission = activity.info.permission;
    info.filter = new IntentFilter(intentFilter);
    return info;
  }

  static ResolveInfo getResolveInfo(Service service, IntentFilter intentFilter) {
    ResolveInfo info = new ResolveInfo();
    info.isDefault = intentFilter.hasCategory("Intent.CATEGORY_DEFAULT");
    info.serviceInfo = new ServiceInfo();
    info.serviceInfo.name = service.info.name;
    info.serviceInfo.packageName = service.info.packageName;
    info.serviceInfo.applicationInfo = service.info.applicationInfo;
    info.activityInfo.permission = service.info.permission;
    info.filter = new IntentFilter(intentFilter);
    return info;
  }

  private static int decodeProtectionLevel(String protectionLevel) {
    if (protectionLevel == null) {
      return PermissionInfo.PROTECTION_NORMAL;
    }

    switch (protectionLevel) {
      case "normal":
        return PermissionInfo.PROTECTION_NORMAL;
      case "dangerous":
        return PermissionInfo.PROTECTION_DANGEROUS;
      case "signature":
        return PermissionInfo.PROTECTION_SIGNATURE;
      case "signatureOrSystem":
        return PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM;
      default:
        throw new IllegalArgumentException("unknown protection level " + protectionLevel);
    }
  }

  static PermissionInfo createPermissionInfo(int flags,
      PermissionItemData permissionItemData) throws NameNotFoundException {
    PermissionInfo permissionInfo = new PermissionInfo();
    String packageName = RuntimeEnvironment.getAppManifest().getPackageName();
    permissionInfo.packageName = packageName;
    permissionInfo.name = permissionItemData.getName();
    permissionInfo.group = permissionItemData.getPermissionGroup();
    permissionInfo.protectionLevel = decodeProtectionLevel(permissionItemData.getProtectionLevel());

    String descriptionRef = permissionItemData.getDescription();
    if (descriptionRef != null) {
      ResName descResName = AttributeResource
          .getResourceReference(descriptionRef, packageName, "string");
      permissionInfo.descriptionRes = RuntimeEnvironment.getAppResourceTable().getResourceId(descResName);
    }

    String labelRefOrString = permissionItemData.getLabel();
    if (labelRefOrString != null) {
      if (AttributeResource.isResourceReference(labelRefOrString)) {
        ResName labelResName = AttributeResource.getResourceReference(labelRefOrString, packageName, "string");
        permissionInfo.labelRes = RuntimeEnvironment.getAppResourceTable().getResourceId(labelResName);
      } else {
        permissionInfo.nonLocalizedLabel = labelRefOrString;
      }
    }

    if ((flags & GET_META_DATA) != 0) {
      permissionInfo.metaData = metaDataToBundle(permissionItemData.getMetaData().getValueMap());
    }
    return permissionInfo;
  }

  private static void setUpPackageStorage(ApplicationInfo applicationInfo) {
    TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();
    applicationInfo.sourceDir = tempDirectory.createIfNotExists(applicationInfo.packageName + "-sourceDir").toAbsolutePath().toString();
    applicationInfo.dataDir = tempDirectory.createIfNotExists(applicationInfo.packageName + "-dataDir").toAbsolutePath().toString();

    if (RuntimeEnvironment.getApiLevel() >= N) {
      applicationInfo.credentialProtectedDataDir = tempDirectory.createIfNotExists("userDataDir").toAbsolutePath().toString();
      applicationInfo.deviceProtectedDataDir = tempDirectory.createIfNotExists("deviceDataDir").toAbsolutePath().toString();
    }
  }

  /**
   * @deprecated Prefer {@link PackageManager#getPackageInfo(String, int)} instead.
   */
  @Deprecated
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#getApplicationInfo(String, int)} instead.
   */
  @Deprecated
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#getActivityInfo(ComponentName, int)} instead.
   */
  @Deprecated
  public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#getReceiverInfo(ComponentName, int)} instead.
   */
  @Deprecated
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#getServiceInfo(ComponentName, int)} instead.
   */
  @Deprecated
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#getInstalledPackages(int)} instead.
   */
  @Deprecated
  public List<PackageInfo> getInstalledPackages(int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#queryIntentActivities(Intent, int)} instead.
   */
  @Deprecated
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#queryIntentServices(Intent, int)}  instead.
   */
  @Deprecated
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#queryBroadcastReceivers(Intent, int)} instead.
   */
  @Deprecated
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#resolveActivity(Intent, int)} instead.
   */
  @Deprecated
  public ResolveInfo resolveActivity(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#resolveService(Intent, int)} instead.
   */
  @Deprecated
  public ResolveInfo resolveService(Intent intent, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info) {
    resolveInfoForIntent.put(intent, info);
  }

  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    List<ResolveInfo> infoList = resolveInfoForIntent.get(intent);
    if (infoList == null) {
      infoList = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList);
    }

    infoList.add(info);
  }

  public void removeResolveInfosForIntent(Intent intent, String packageName) {
    List<ResolveInfo> infoList = resolveInfoForIntent.get(intent);
    if (infoList == null) {
      infoList = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList);
    }

    for (Iterator<ResolveInfo> iterator = infoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      if (resolveInfo.activityInfo.packageName.equals(packageName)) {
        iterator.remove();
      }
    }
  }

  public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
    return drawableList.get(intent.getComponent());
  }

  public Drawable getActivityIcon(ComponentName componentName) throws NameNotFoundException {
    return drawableList.get(componentName);
  }

  public void addActivityIcon(ComponentName component, Drawable drawable) {
    drawableList.put(component, drawable);
  }

  public void addActivityIcon(Intent intent, Drawable drawable) {
    drawableList.put(intent.getComponent(), drawable);
  }

  /**
   * @deprecated Prefer {@link PackageManager#getApplicationIcon(String)} instead.
   */
  @Deprecated
  public Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void setApplicationIcon(String packageName, Drawable drawable) {
    applicationIcons.put(packageName, drawable);
  }

  /**
   * @deprecated Prefer {@link PackageManager#getLaunchIntentForPackage(String)} instead.
   */
  @Deprecated
  public Intent getLaunchIntentForPackage(String packageName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#getApplicationLabel(ApplicationInfo)} instead.
   */
  @Deprecated
  public CharSequence getApplicationLabel(ApplicationInfo info) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * @deprecated Prefer {@link PackageManager#setComponentEnabledSetting(ComponentName, int, int)} instead.
   */
  @Deprecated
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    applicationEnabledSettingMap.put(packageName, newState);
  }

  public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    preferredActivities.put(filter, activity);
  }

  public int getPreferredActivities(List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    if (outFilters == null) {
      return 0;
    }

    Set<IntentFilter> filters = preferredActivities.keySet();
    for (IntentFilter filter : outFilters) {
      step:
      for (IntentFilter testFilter : filters) {
        ComponentName name = preferredActivities.get(testFilter);
        // filter out based on the given packageName;
        if (packageName != null && !name.getPackageName().equals(packageName)) {
          continue step;
        }

        // Check actions
        Iterator<String> iterator = filter.actionsIterator();
        while (iterator.hasNext()) {
          if (!testFilter.matchAction(iterator.next())) {
            continue step;
          }
        }

        iterator = filter.categoriesIterator();
        while (iterator.hasNext()) {
          if (!filter.hasCategory(iterator.next())) {
            continue step;
          }
        }

        if (outActivities == null) {
          outActivities = new ArrayList<>();
        }

        outActivities.add(name);
      }
    }

    return 0;
  }

  /**
   * @deprecated Use {@link android.app.ApplicationPackageManager#getComponentEnabledSetting(ComponentName)} or
   * {@link #getComponentEnabledSettingFlags(ComponentName)} instead. This method will be removed in Robolectric 3.5.
   */
  @Deprecated
  public ComponentState getComponentState(ComponentName componentName) {
    return componentList.get(componentName);
  }

  /**
   * Return the flags set in call to {@link android.app.ApplicationPackageManager#setComponentEnabledSetting(ComponentName, int, int)}.
   *
   * @param componentName The component name.
   * @return The flags.
   */
  public int getComponentEnabledSettingFlags(ComponentName componentName) {
    ComponentState state = componentList.get(componentName);
    return state != null ? state.flags : 0;
  }


  public void addPackage(String packageName) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = packageName;
    setUpPackageStorage(applicationInfo);
    packageInfo.applicationInfo = applicationInfo;
    addPackage(packageInfo);
  }

  public void addPackage(PackageInfo packageInfo) {
    PackageStats packageStats = new PackageStats(packageInfo.packageName);
    addPackage(packageInfo, packageStats);
  }

  public void addPackage(PackageInfo packageInfo, PackageStats packageStats) {
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    if (packageInfo.applicationInfo != null) {
      namesForUid.put(packageInfo.applicationInfo.uid, packageInfo.packageName);
    }
  }

  public void addPermissionInfo(PermissionInfo permissionInfo) {
    extraPermissions.put(permissionInfo.name, permissionInfo);
  }

  public void removePackage(String packageName) {
    packages.remove(packageName);
    packageInfos.remove(packageName);
  }

  /**
   * @deprecated Prefer {@link PackageManager#hasSystemFeature(String)} instead.
   */
  @Deprecated
  public boolean hasSystemFeature(String name) {
    throw new UnsupportedOperationException("Not implemented");
  }

  public void setSystemFeature(String name, boolean supported) {
    systemFeatureList.put(name, supported);
  }

  public void addDrawableResolution(String packageName, int resourceId, Drawable drawable) {
    drawables.put(new Pair(packageName, resourceId), drawable);
  }

  public Drawable getDrawable(String packageName, int resourceId, ApplicationInfo applicationInfo) {
    return drawables.get(new Pair(packageName, resourceId));
  }

  /**
   * @deprecated Prefer {@link PackageManager#checkPermission(String, String)} instead.
   */
  @Deprecated
  public int checkPermission(String permName, String pkgName) {
    return 0;
  }

  public boolean isQueryIntentImplicitly() {
    return queryIntentImplicitly;
  }

  public void setQueryIntentImplicitly(boolean queryIntentImplicitly) {
    this.queryIntentImplicitly = queryIntentImplicitly;
  }

  public void setNameForUid(int uid, String name) {
    namesForUid.put(uid, name);
  }

  public void setPackagesForCallingUid(String... packagesForCallingUid) {
    packagesForUid.put(Binder.getCallingUid(), packagesForCallingUid);
    for (String packageName : packagesForCallingUid) {
      uidForPackage.put(packageName, Binder.getCallingUid());
    }
  }

  public void setPackagesForUid(int uid, String... packagesForCallingUid) {
    packagesForUid.put(uid, packagesForCallingUid);
    for (String packageName : packagesForCallingUid) {
      uidForPackage.put(packageName, uid);
    }
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
    return null;
  }

  @Implementation
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags, @UserIdInt int userId) {
    return null;
  }

  @Implementation
  public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
    List<PackageInfo> result = new ArrayList<>();
    for (PackageInfo packageInfo : packageInfos.values()) {
      if (applicationEnabledSettingMap.get(packageInfo.packageName)
          != COMPONENT_ENABLED_STATE_DISABLED
          || (flags & MATCH_UNINSTALLED_PACKAGES) == MATCH_UNINSTALLED_PACKAGES) {
            result.add(packageInfo);
          }
    }

    List<PackageInfo> packages = result;
    for (PackageInfo aPackage : packages) {
      ApplicationInfo appInfo = aPackage.applicationInfo;
      if (appInfo != null && archiveFilePath.equals(appInfo.sourceDir)) {
        return aPackage;
      }
    }
    return null;
  }

  @Implementation
  public void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {
  }

  @Implementation
  public void freeStorage(long freeStorageSize, IntentSender pi) {
  }

  /**
   * Runs the callbacks pending from calls to {@link PackageManager#deletePackage(String, IPackageDeleteObserver, int)}
   */
  public void doPendingUninstallCallbacks() {
    boolean hasDeletePackagesPermission = false;
    String[] requestedPermissions =
        packageInfos.get(RuntimeEnvironment.getAppManifest().getPackageName()).requestedPermissions;
    if (requestedPermissions != null) {
      for (String permission : requestedPermissions) {
        if (Manifest.permission.DELETE_PACKAGES.equals(permission)) {
          hasDeletePackagesPermission = true;
          break;
        }
      }
    }

    for (String packageName : pendingDeleteCallbacks.keySet()) {
      int resultCode = PackageManager.DELETE_FAILED_INTERNAL_ERROR;

      PackageInfo removed = packageInfos.get(packageName);
      if (hasDeletePackagesPermission && removed != null) {
        packageInfos.remove(packageName);
        deletedPackages.add(packageName);
        resultCode = PackageManager.DELETE_SUCCEEDED;
      }

      try {
        pendingDeleteCallbacks.get(packageName).packageDeleted(packageName, resultCode);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    }
    pendingDeleteCallbacks.clear();
  }

  /**
   * Returns package names successfully deleted with {@link PackageManager#deletePackage(String, IPackageDeleteObserver, int)}
   * Note that like real {@link PackageManager} the calling context must have {@link android.Manifest.permission#DELETE_PACKAGES} permission set.
   */
  public Set<String> getDeletedPackages() {
    return deletedPackages;
  }

  protected List<ResolveInfo> queryIntent(Intent intent, int flags) {
    List<ResolveInfo> result = resolveInfoForIntent.get(intent);
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  protected static int getConfigChanges(ActivityData activityData) {
    String s = activityData.getConfigChanges();

    int res = 0;

    //quick sanity check.
    if (s == null || "".equals(s)) {
      return res;
    }

    String[] pieces = s.split("\\|");

    for(String s1 : pieces) {
      s1 = s1.trim();

      for (Pair<String, Integer> pair : CONFIG_OPTIONS) {
        if (s1.equals(pair.first)) {
          res |= pair.second;
          break;
        }
      }
    }
    return res;
  }

  public void addPackage(Package appPackage) {
    int flags =
        GET_ACTIVITIES |
            GET_RECEIVERS |
            GET_SERVICES |
            GET_PROVIDERS |
            GET_INSTRUMENTATION |
            GET_INTENT_FILTERS |
            GET_SIGNATURES |
            GET_RESOLVED_FILTER |
            GET_META_DATA |
            GET_GIDS |
            MATCH_DISABLED_COMPONENTS |
            GET_SHARED_LIBRARY_FILES |
            GET_URI_PERMISSION_PATTERNS |
            GET_PERMISSIONS |
            MATCH_UNINSTALLED_PACKAGES |
            GET_CONFIGURATIONS |
            MATCH_DISABLED_UNTIL_USED_COMPONENTS |
            MATCH_DIRECT_BOOT_UNAWARE |
            MATCH_DIRECT_BOOT_AWARE
        ;

    packages.put(appPackage.packageName, appPackage);
    PackageInfo packageInfo = PackageParser.generatePackageInfo(appPackage, new int[]{0}, flags, 0, 0, new HashSet<String>(), new PackageUserState());
    addPackage(packageInfo);
  }

  public static class IntentComparator implements Comparator<Intent> {

    @Override
    public int compare(Intent i1, Intent i2) {
      if (i1 == null && i2 == null) return 0;
      if (i1 == null && i2 != null) return -1;
      if (i1 != null && i2 == null) return 1;
      if (i1.equals(i2)) return 0;
      String action1 = i1.getAction();
      String action2 = i2.getAction();
      if (action1 == null && action2 != null) return -1;
      if (action1 != null && action2 == null) return 1;
      if (action1 != null && action2 != null) {
        if (!action1.equals(action2)) {
          return action1.compareTo(action2);
        }
      }
      Uri data1 = i1.getData();
      Uri data2 = i2.getData();
      if (data1 == null && data2 != null) return -1;
      if (data1 != null && data2 == null) return 1;
      if (data1 != null && data2 != null) {
        if (!data1.equals(data2)) {
          return data1.compareTo(data2);
        }
      }
      ComponentName component1 = i1.getComponent();
      ComponentName component2 = i2.getComponent();
      if (component1 == null && component2 != null) return -1;
      if (component1 != null && component2 == null) return 1;
      if (component1 != null && component2 != null) {
        if (!component1.equals(component2)) {
          return component1.compareTo(component2);
        }
      }
      String package1 = i1.getPackage();
      String package2 = i2.getPackage();
      if (package1 == null && package2 != null) return -1;
      if (package1 != null && package2 == null) return 1;
      if (package1 != null && package2 != null) {
        if (!package1.equals(package2)) {
          return package1.compareTo(package2);
        }
      }
      Set<String> categories1 = i1.getCategories();
      Set<String> categories2 = i2.getCategories();
      if (categories1 == null) return categories2 == null ? 0 : -1;
      if (categories2 == null) return 1;
      if (categories1.size() > categories2.size()) return 1;
      if (categories1.size() < categories2.size()) return -1;
      String[] array1 = categories1.toArray(new String[0]);
      String[] array2 = categories2.toArray(new String[0]);
      Arrays.sort(array1);
      Arrays.sort(array2);
      for (int i = 0; i < array1.length; ++i) {
        int val = array1[i].compareTo(array2[i]);
        if (val != 0) return val;
      }
      return 0;
    }
  }

  /**
   * @deprecated Use {@link android.app.ApplicationPackageManager#getComponentEnabledSetting(ComponentName)} instead. This class will be made private in Robolectric 3.5.
   */
  @Deprecated
  public static class ComponentState {
    public int newState;
    public int flags;

    public ComponentState(int newState, int flags) {
      this.newState = newState;
      this.flags = flags;
    }
  }
}
