package org.robolectric.shadows;

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
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.Service;
import android.content.pm.PackageStats;
import android.content.pm.PackageUserState;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArraySet;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TempDirectory;

@Implements(PackageManager.class)
public class ShadowPackageManager {

  Map<String, Boolean> permissionRationaleMap = new HashMap<>();
  List<FeatureInfo> systemAvailableFeatures = new ArrayList<>();
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
  Map<String, PermissionInfo> extraPermissions = new HashMap<>();
  Map<String, PermissionGroupInfo> extraPermissionGroups = new HashMap<>();
  public Map<String, Resources> resources = new HashMap<>();
  private final Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new TreeMap<>(new IntentComparator());
  private Set<String> deletedPackages = new HashSet<>();
  Map<String, IPackageDeleteObserver> pendingDeleteCallbacks = new HashMap<>();

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
    HashSet<Signature> signatures1set = new HashSet<>(asList(signatures1));
    HashSet<Signature> signatures2set = new HashSet<>(asList(signatures2));
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
    info.serviceInfo.permission = service.info.permission;
    info.filter = new IntentFilter(intentFilter);
    return info;
  }

  private static void setUpPackageStorage(ApplicationInfo applicationInfo) {
    TempDirectory tempDirectory = RuntimeEnvironment.getTempDirectory();
    if (applicationInfo.sourceDir == null) {
      applicationInfo.sourceDir =
          tempDirectory
              .createIfNotExists(applicationInfo.packageName + "-sourceDir")
              .toAbsolutePath()
              .toString();
    }
    if (applicationInfo.dataDir == null) {
      applicationInfo.dataDir =
          tempDirectory
              .createIfNotExists(applicationInfo.packageName + "-dataDir")
              .toAbsolutePath()
              .toString();
    }
    applicationInfo.publicSourceDir = applicationInfo.sourceDir;

    if (RuntimeEnvironment.getApiLevel() >= N) {
      applicationInfo.credentialProtectedDataDir = tempDirectory.createIfNotExists("userDataDir").toAbsolutePath().toString();
      applicationInfo.deviceProtectedDataDir = tempDirectory.createIfNotExists("deviceDataDir").toAbsolutePath().toString();
    }
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

  public void setApplicationIcon(String packageName, Drawable drawable) {
    applicationIcons.put(packageName, drawable);
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
   * Return the flags set in call to {@link android.app.ApplicationPackageManager#setComponentEnabledSetting(ComponentName, int, int)}.
   *
   * @param componentName The component name.
   * @return The flags.
   */
  public int getComponentEnabledSettingFlags(ComponentName componentName) {
    ComponentState state = componentList.get(componentName);
    return state != null ? state.flags : 0;
  }

  /** @deprecated - use {@link #addPackage(PackageInfo)} instead */
  @Deprecated
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

  /**
   * Allows overriding or adding permission-group elements. These would be otherwise specified by
   * either the system (https://developer.android.com/guide/topics/permissions/requesting.html#perm-groups)
   * or by the app itself, as part of its manifest
   * (https://developer.android.com/guide/topics/manifest/permission-group-element.html).
   * 
   * PermissionGroups added through this method have precedence over those specified with the same name
   * by one of the aforementioned methods.
   */
  public void addPermissionGroupInfo(PermissionGroupInfo permissionGroupInfo) {
    extraPermissionGroups.put(permissionGroupInfo.name, permissionGroupInfo);
  }

  public void removePackage(String packageName) {
    packages.remove(packageName);
    packageInfos.remove(packageName);
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
        packageInfos.get(RuntimeEnvironment.application.getPackageName()).requestedPermissions;
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

  protected List<ResolveInfo> queryOverriddenIntents(Intent intent, int flags) {
    List<ResolveInfo> result = resolveInfoForIntent.get(intent);
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  public void addPackage(Package appPackage) {
    int flags =
        GET_ACTIVITIES
            | GET_RECEIVERS
            | GET_SERVICES
            | GET_PROVIDERS
            | GET_INSTRUMENTATION
            | GET_INTENT_FILTERS
            | GET_SIGNATURES
            | GET_RESOLVED_FILTER
            | GET_META_DATA
            | GET_GIDS
            | MATCH_DISABLED_COMPONENTS
            | GET_SHARED_LIBRARY_FILES
            | GET_URI_PERMISSION_PATTERNS
            | GET_PERMISSIONS
            | MATCH_UNINSTALLED_PACKAGES
            | GET_CONFIGURATIONS
            | MATCH_DISABLED_UNTIL_USED_COMPONENTS
            | MATCH_DIRECT_BOOT_UNAWARE
            | MATCH_DIRECT_BOOT_AWARE;

    packages.put(appPackage.packageName, appPackage);
    PackageInfo packageInfo;
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.M) {
      packageInfo =
          PackageParser.generatePackageInfo(
              appPackage,
              new int[] {0},
              flags,
              0,
              0,
              new HashSet<String>(),
              new PackageUserState());
    } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      packageInfo =
          ReflectionHelpers.callStaticMethod(
              PackageParser.class,
              "generatePackageInfo",
              ReflectionHelpers.ClassParameter.from(Package.class, appPackage),
              ReflectionHelpers.ClassParameter.from(int[].class, new int[] {0}),
              ReflectionHelpers.ClassParameter.from(int.class, flags),
              ReflectionHelpers.ClassParameter.from(long.class, 0L),
              ReflectionHelpers.ClassParameter.from(long.class, 0L),
              ReflectionHelpers.ClassParameter.from(ArraySet.class, new ArraySet<>()),
              ReflectionHelpers.ClassParameter.from(
                  PackageUserState.class, new PackageUserState()));
    } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      packageInfo =
          ReflectionHelpers.callStaticMethod(
              PackageParser.class,
              "generatePackageInfo",
              ReflectionHelpers.ClassParameter.from(Package.class, appPackage),
              ReflectionHelpers.ClassParameter.from(int[].class, new int[] {0}),
              ReflectionHelpers.ClassParameter.from(int.class, flags),
              ReflectionHelpers.ClassParameter.from(long.class, 0L),
              ReflectionHelpers.ClassParameter.from(long.class, 0L),
              ReflectionHelpers.ClassParameter.from(HashSet.class, new HashSet<>()),
              ReflectionHelpers.ClassParameter.from(
                  PackageUserState.class, new PackageUserState()));
    } else {
      packageInfo =
          ReflectionHelpers.callStaticMethod(
              PackageParser.class,
              "generatePackageInfo",
              ReflectionHelpers.ClassParameter.from(Package.class, appPackage),
              ReflectionHelpers.ClassParameter.from(int[].class, new int[] {0}),
              ReflectionHelpers.ClassParameter.from(int.class, flags),
              ReflectionHelpers.ClassParameter.from(long.class, 0L),
              ReflectionHelpers.ClassParameter.from(long.class, 0L),
              ReflectionHelpers.ClassParameter.from(HashSet.class, new HashSet<>()));
    }

    packageInfo.applicationInfo.uid = Process.myUid();
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

  protected static class ComponentState {
    public int newState;
    public int flags;

    public ComponentState(int newState, int flags) {
      this.newState = newState;
      this.flags = flags;
    }
  }

  /**
   * Get list of intent filters defined for given activity.
   *
   * @param componentName Name of the activity whose intent filters are to be retrieved
   * @return the activity's intent filters
   */
  public List<IntentFilter> getIntentFiltersForActivity(ComponentName componentName)
      throws NameNotFoundException {
    return getIntentFiltersForComponent(getAppPackage(componentName).activities, componentName);
  }

  /**
   * Get list of intent filters defined for given service.
   *
   * @param componentName Name of the service whose intent filters are to be retrieved
   * @return the service's intent filters
   */
  public List<IntentFilter> getIntentFiltersForService(ComponentName componentName)
      throws NameNotFoundException {
    return getIntentFiltersForComponent(getAppPackage(componentName).services, componentName);
  }

  /**
   * Get list of intent filters defined for given receiver.
   *
   * @param componentName Name of the receiver whose intent filters are to be retrieved
   * @return the receiver's intent filters
   */
  public List<IntentFilter> getIntentFiltersForReceiver(ComponentName componentName)
      throws NameNotFoundException {
    return getIntentFiltersForComponent(getAppPackage(componentName).receivers, componentName);
  }

  private static List<IntentFilter> getIntentFiltersForComponent(
      List<? extends Component> components, ComponentName componentName)
      throws NameNotFoundException {
    for (Component component : components) {
      if (component.getComponentName().equals(componentName)) {
        return component.intents;
      }
    }
    throw new NameNotFoundException("unknown component " + componentName);
  }

  private Package getAppPackage(ComponentName componentName) throws NameNotFoundException {
    Package appPackage = this.packages.get(componentName.getPackageName());
    if (appPackage == null) {
      throw new NameNotFoundException("unknown package " + componentName.getPackageName());
    }
    return appPackage;
  }

  private static List<IntentFilter> convertIntentFilters(
      List<? extends PackageParser.IntentInfo> intentInfos) {
    List<IntentFilter> intentFilters = new ArrayList<>(intentInfos.size());
    for (IntentInfo intentInfo : intentInfos) {
      intentFilters.add(intentInfo);
    }
    return intentFilters;
  }
}
