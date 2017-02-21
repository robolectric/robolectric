package org.robolectric.res.builder;

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
import static android.os.Build.VERSION_CODES.N;
import static java.util.Arrays.asList;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PathPermission;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PatternMatcher;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Pair;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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
import org.robolectric.android.StubPackageManager;
import org.robolectric.manifest.ActivityData;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.manifest.IntentFilterData;
import org.robolectric.manifest.PackageItemData;
import org.robolectric.manifest.PathPermissionData;
import org.robolectric.manifest.PermissionItemData;
import org.robolectric.manifest.ServiceData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceTable;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TempDirectory;

/**
 * @deprecated use @{link ShadowPackageManager} instead.
 */
@Deprecated
public class DefaultPackageManager extends StubPackageManager implements RobolectricPackageManager {

  private final Map<String, AndroidManifest> androidManifests = new LinkedHashMap<>();
  private final Map<String, PackageInfo> packageInfos = new LinkedHashMap<>();
  private final Map<String, PackageStats> packageStatsMap = new HashMap<>();
  private final Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new TreeMap<>(new IntentComparator());
  private final Map<ComponentName, ComponentState> componentList = new LinkedHashMap<>();
  private final Map<ComponentName, Drawable> drawableList = new LinkedHashMap<>();
  private final Map<String, Drawable> applicationIcons = new HashMap<>();
  private final Map<String, Boolean> systemFeatureList = new LinkedHashMap<>();
  private final Map<IntentFilter, ComponentName> preferredActivities = new LinkedHashMap<>();
  private final Map<Pair<String, Integer>, Drawable> drawables = new LinkedHashMap<>();
  private final Map<String, Integer> applicationEnabledSettingMap = new HashMap<>();
  private final Map<Integer, String> namesForUid = new HashMap<>();
  private final Map<Integer, String[]> packagesForUid = new HashMap<>();
  private final Map<String, String> packageInstallerMap = new HashMap<>();
  private boolean queryIntentImplicitly = false;
  private PackageInstaller packageInstaller;
  private AndroidManifest applicationManifest;
  private ResourceTable appResourceTable;
  private Map<String, PermissionInfo> extraPermissions = new HashMap<>();

  @Override
  public PackageInstaller getPackageInstaller() {
    if (packageInstaller == null) {
      packageInstaller = new PackageInstaller(RuntimeEnvironment.application, this, ReflectionHelpers.createNullProxy(
          IPackageInstaller.class),
          null, UserHandle.myUserId());
    }
    return packageInstaller;
  }

  @Override
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    if (packageInfos.containsKey(packageName)) {
      return packageInfos.get(packageName);
    }

    throw new NameNotFoundException(packageName);
  }

  @Override
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    PackageInfo info = packageInfos.get(packageName);
    if (info != null) {
      return info.applicationInfo;
    } else {
      throw new NameNotFoundException(packageName);
    }
  }

  @Override public ActivityInfo getActivityInfo(ComponentName className, int flags) throws NameNotFoundException {
    ActivityInfo activityInfo = new ActivityInfo();
    String packageName = className.getPackageName();
    String activityName = className.getClassName();
    activityInfo.name = activityName;
    activityInfo.packageName = packageName;

    AndroidManifest androidManifest = androidManifests.get(packageName);

    // In the cases where there is no manifest entry for the activity, e.g: a test that creates
    // simply an android.app.Activity just return what we have.
    if (androidManifest == null) {
      return activityInfo;
    }

    ActivityData activityData = androidManifest.getActivityData(activityName);
    if (activityData != null) {
      activityInfo.configChanges = getConfigChanges(activityData);
      activityInfo.parentActivityName = activityData.getParentActivityName();
      activityInfo.metaData = metaDataToBundle(activityData.getMetaData().getValueMap());
      String themeRef;

      // Based on ShadowActivity
      if (activityData.getThemeRef() != null) {
        themeRef = activityData.getThemeRef();
      } else {
        themeRef = androidManifest.getThemeRef();
      }
      if (themeRef != null) {
        activityInfo.theme = RuntimeEnvironment.application.getResources().getIdentifier(themeRef.replace("@", ""), "style", packageName);
      }
    }
    activityInfo.applicationInfo = getApplicationInfo(packageName, flags);
    return activityInfo;
  }

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

  private int getConfigChanges(ActivityData activityData) {
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

  @Override
  public ProviderInfo getProviderInfo(ComponentName className, int flags) throws NameNotFoundException {
    String packageName = className.getPackageName();
    AndroidManifest androidManifest = androidManifests.get(packageName);
    String classString = resolvePackageName(packageName, className);

    for (ContentProviderData contentProviderData : androidManifest.getContentProviders()) {
      if (contentProviderData.getClassName().equals(classString)) {
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.packageName = packageName;
        providerInfo.name = contentProviderData.getClassName();
        providerInfo.authority = contentProviderData.getAuthorities(); // todo: support multiple authorities
        providerInfo.readPermission = contentProviderData.getReadPermission();
        providerInfo.writePermission = contentProviderData.getWritePermission();
        providerInfo.pathPermissions = createPathPermissions(contentProviderData.getPathPermissionDatas());
        if ((flags & GET_META_DATA) != 0) {
          providerInfo.metaData = metaDataToBundle(contentProviderData.getMetaData().getValueMap());
        }
        return providerInfo;
      }
    }
    return null;
  }

  private PathPermission[] createPathPermissions(List<PathPermissionData> pathPermissionDatas) {
    PathPermission[] pathPermissions = new PathPermission[pathPermissionDatas.size()];
    for (int i = 0; i < pathPermissions.length; i++) {
      PathPermissionData data = pathPermissionDatas.get(i);

      final String path;
      final int type;
      if (data.pathPrefix != null) {
        path = data.pathPrefix;
        type = PathPermission.PATTERN_PREFIX;
      } else if (data.pathPattern != null) {
        path = data.pathPattern;
        type = PathPermission.PATTERN_SIMPLE_GLOB;
      } else {
        path = data.path;
        type = PathPermission.PATTERN_LITERAL;
      }

      pathPermissions[i] = new PathPermission(path, type, data.readPermission, data.writePermission);
    }

    return pathPermissions;
  }

  @Override
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    String packageName = className.getPackageName();
    AndroidManifest androidManifest = androidManifests.get(packageName);
    String classString = resolvePackageName(packageName, className);

    for (PackageItemData receiver : androidManifest.getBroadcastReceivers()) {
      if (receiver.getClassName().equals(classString)) {
        ActivityInfo activityInfo = new ActivityInfo();
        activityInfo.packageName = packageName;
        activityInfo.name = classString;
        if ((flags & GET_META_DATA) != 0) {
          activityInfo.metaData = metaDataToBundle(receiver.getMetaData().getValueMap());
        }
        return activityInfo;
      }
    }
    return null;
  }

  private String resolvePackageName(String packageName, ComponentName componentName) {
    String classString = componentName.getClassName();
    int index = classString.indexOf('.');
    if (index == -1) {
      classString = packageName + "." + classString;
    } else if (index == 0) {
      classString = packageName + classString;
    }
    return classString;
  }

  @Override
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    String packageName = className.getPackageName();
    AndroidManifest androidManifest = androidManifests.get(packageName);
    String serviceName = className.getClassName();
    ServiceData serviceData = androidManifest.getServiceData(serviceName);
    if (serviceData == null) {
      throw new NameNotFoundException(serviceName);
    }
    
    ServiceInfo serviceInfo = new ServiceInfo();
    serviceInfo.packageName = packageName;
    serviceInfo.name = serviceName;
    serviceInfo.applicationInfo = getApplicationInfo(packageName, flags);
    serviceInfo.permission = serviceData.getPermission();
    if ((flags & GET_META_DATA) != 0) {
      serviceInfo.metaData = metaDataToBundle(serviceData.getMetaData().getValueMap());
    }
    return serviceInfo;   
  }

  @Override
  public List<PackageInfo> getInstalledPackages(int flags) {
    return new ArrayList<>(packageInfos.values());
  }

  @Override
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    List<ResolveInfo> resolveInfoList = queryIntent(intent, flags);

    if (resolveInfoList.isEmpty() && isQueryIntentImplicitly()) {
      resolveInfoList = queryImplicitIntent(intent, flags);
    }

    return resolveInfoList;
  }

  @Override
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    return queryIntent(intent, flags);
  }

  @Override
  public List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    return queryIntent(intent, flags);
  }

  @Override
  public ResolveInfo resolveActivity(Intent intent, int flags) {
    List<ResolveInfo> candidates = queryIntentActivities(intent, flags);
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  @Override
  public ResolveInfo resolveService(Intent intent, int flags) {
    return resolveActivity(intent, flags);
  }

  @Override
  public ProviderInfo resolveContentProvider(String name, int flags) {
    for (PackageInfo packageInfo : packageInfos.values()) {
      if (packageInfo.providers == null) continue;

      for (ProviderInfo providerInfo : packageInfo.providers) {
        if (name.equals(providerInfo.authority)) { // todo: support multiple authorities
          return providerInfo;
        }
      }
    }

    return null;
  }

  @Override
  public void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info) {
    resolveInfoForIntent.put(intent, info);
  }

  @Override
  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    List<ResolveInfo> infoList = findOrCreateInfoList(intent);
    infoList.add(info);
  }

  @Override
  public void removeResolveInfosForIntent(Intent intent, String packageName) {
    List<ResolveInfo> infoList = findOrCreateInfoList(intent);
    for (Iterator<ResolveInfo> iterator = infoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      if (resolveInfo.activityInfo.packageName.equals(packageName)) {
        iterator.remove();
      }
    }
  }

  @Override
  public Drawable getActivityIcon(Intent intent) {
    return drawableList.get(intent.getComponent());
  }

  @Override
  public Drawable getActivityIcon(ComponentName componentName) {
    return drawableList.get(componentName);
  }

  @Override
  public void addActivityIcon(ComponentName component, Drawable d) {
    drawableList.put(component, d);
  }

  @Override
  public void addActivityIcon(Intent intent, Drawable d) {
    drawableList.put(intent.getComponent(), d);
  }

  @Override
  public Intent getLaunchIntentForPackage(String packageName) {
    Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
    intentToResolve.addCategory(Intent.CATEGORY_INFO);
    intentToResolve.setPackage(packageName);
    List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);

    if (ris == null || ris.isEmpty()) {
      intentToResolve.removeCategory(Intent.CATEGORY_INFO);
      intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
      intentToResolve.setPackage(packageName);
      ris = queryIntentActivities(intentToResolve, 0);
    }
    if (ris == null || ris.isEmpty()) {
      return null;
    }
    Intent intent = new Intent(intentToResolve);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
    return intent;
  }

  @Override
  public void addPermissionInfo(PermissionInfo permissionInfo) {
    extraPermissions.put(permissionInfo.name, permissionInfo);
  }

  @Override
  public PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
    PermissionInfo permissionInfo = extraPermissions.get(name);
    if (permissionInfo != null) {
      return permissionInfo;
    }

    PermissionItemData permissionItemData = applicationManifest.getPermissions().get(name);
    if (permissionItemData == null) {
      throw new NameNotFoundException(name);
    }

    permissionInfo = new PermissionInfo();
    String packageName = applicationManifest.getPackageName();
    permissionInfo.packageName = packageName;
    permissionInfo.name = name;
    permissionInfo.group = permissionItemData.getPermissionGroup();
    permissionInfo.protectionLevel = decodeProtectionLevel(permissionItemData.getProtectionLevel());

    String descriptionRef = permissionItemData.getDescription();
    if (descriptionRef != null) {
      ResName descResName = AttributeResource.getResourceReference(descriptionRef, packageName, "string");
      permissionInfo.descriptionRes = appResourceTable.getResourceId(descResName);
    }

    String labelRefOrString = permissionItemData.getLabel();
    if (labelRefOrString != null) {
      if (AttributeResource.isResourceReference(labelRefOrString)) {
        ResName labelResName = AttributeResource.getResourceReference(labelRefOrString, packageName, "string");
        permissionInfo.labelRes = appResourceTable.getResourceId(labelResName);
      } else {
        permissionInfo.nonLocalizedLabel = labelRefOrString;
      }
    }

    if ((flags & GET_META_DATA) != 0) {
      permissionInfo.metaData = metaDataToBundle(permissionItemData.getMetaData().getValueMap());
    }

    return permissionInfo;
  }

  private int decodeProtectionLevel(String protectionLevel) {
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

  @Override
  public CharSequence getApplicationLabel(ApplicationInfo info) {
    return info.name;
  }

  @Override
  public Drawable getApplicationIcon(String packageName) {
    return applicationIcons.get(packageName);
  }

  @Override
  public void setApplicationIcon(String packageName, Drawable drawable) {
    applicationIcons.put(packageName, drawable);
  }

  @Override
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    componentList.put(componentName, new ComponentState(newState, flags));
  }

  @Override
  public int getComponentEnabledSetting(ComponentName componentName) {
    ComponentState state = componentList.get(componentName);
    return state != null ? state.newState : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
  }

  @Override
  public void addPreferredActivity(IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    preferredActivities.put(filter, activity);
  }

  @Override
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

  @Override
  public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
    List<PackageInfo> packages = getInstalledPackages(flags);
    for (PackageInfo aPackage : packages) {
      ApplicationInfo appInfo = aPackage.applicationInfo;
      if (archiveFilePath.equals(appInfo.sourceDir)) {
        return aPackage;
      }
    }
    return null;
  }

  /**
   * Non-Android accessor.  Use to make assertions on values passed to setComponentEnabledSetting.
   *
   * @param componentName Component name.
   * @return Component state.
   */
  @Override
  public ComponentState getComponentState(ComponentName componentName) {
    return componentList.get(componentName);
  }

  /**
   * Non-Android accessor.  Used to add a package to the list of those already 'installed' on system.
   *
   * @param packageInfo New package info.
   */
  @Override
  public void addPackage(PackageInfo packageInfo) {
    addPackage(packageInfo, new PackageStats(packageInfo.packageName));
  }

  public void addPackage(PackageInfo packageInfo, PackageStats packageStats) {
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
      PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(0);
      sessionParams.setAppPackageName(packageInfo.packageName);
      try {
        getPackageInstaller().createSession(sessionParams);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void addPackage(String packageName) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = packageName;
    applicationInfo.sourceDir = new File(".").getAbsolutePath();
    applicationInfo.dataDir = TempDirectory.create().toAbsolutePath().toString();

    packageInfo.applicationInfo = applicationInfo;

    addPackage(packageInfo);
  }

  @Override
  public void addManifest(AndroidManifest androidManifest, int labelRes) {
    androidManifests.put(androidManifest.getPackageName(), androidManifest);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = androidManifest.getPackageName();
    packageInfo.versionName = androidManifest.getVersionName();
    packageInfo.versionCode = androidManifest.getVersionCode();

    ContentProviderData[] cpdata = androidManifest.getContentProviders().toArray(new ContentProviderData[]{});
    if (cpdata.length == 0) {
      packageInfo.providers = null;
    } else {
      packageInfo.providers = new ProviderInfo[cpdata.length];
      for (int i = 0; i < cpdata.length; i++) {
        ProviderInfo info = new ProviderInfo();
        info.authority = cpdata[i].getAuthorities(); // todo: support multiple authorities
        info.name = cpdata[i].getClassName();
        info.packageName = androidManifest.getPackageName();
        packageInfo.providers[i] = info;
      }
    }

    // Populate information related to BroadcastReceivers. Broadcast receivers can be queried in two
    // possible ways,
    // 1. PackageManager#getPackageInfo(...),
    // 2. PackageManager#queryBroadcastReceivers(...)
    // The following piece of code will let you enable querying receivers through both the methods.
    List<ActivityInfo> receiverActivityInfos = new ArrayList<>();
    for (int i = 0; i < androidManifest.getBroadcastReceivers().size(); ++i) {
      ActivityInfo activityInfo = new ActivityInfo();
      activityInfo.name = androidManifest.getBroadcastReceivers().get(i).getClassName();
      activityInfo.permission = androidManifest.getBroadcastReceivers().get(i).getPermission();
      receiverActivityInfos.add(activityInfo);

      ResolveInfo resolveInfo = new ResolveInfo();
      resolveInfo.activityInfo = activityInfo;
      IntentFilter filter = new IntentFilter();
      for (String action : androidManifest.getBroadcastReceivers().get(i).getActions()) {
        filter.addAction(action);
      }
      resolveInfo.filter = filter;

      for (String action : androidManifest.getBroadcastReceivers().get(i).getActions()) {
        Intent intent = new Intent(action);
        intent.setPackage(androidManifest.getPackageName());
        addResolveInfoForIntent(intent, resolveInfo);
      }
    }
    packageInfo.receivers = receiverActivityInfos.toArray(new ActivityInfo[0]);

    String[] usedPermissions = androidManifest.getUsedPermissions().toArray(new String[]{});
    if (usedPermissions.length == 0) {
      packageInfo.requestedPermissions = null;
    } else {
      packageInfo.requestedPermissions = usedPermissions;
    }

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.flags = decodeFlags(androidManifest.getApplicationAttributes());
    applicationInfo.targetSdkVersion = androidManifest.getTargetSdkVersion();
    applicationInfo.packageName = androidManifest.getPackageName();
    applicationInfo.processName = androidManifest.getProcessName();
    applicationInfo.name = androidManifest.getApplicationName();
    applicationInfo.metaData = metaDataToBundle(androidManifest.getApplicationMetaData());
    applicationInfo.sourceDir = new File(".").getAbsolutePath();
    applicationInfo.dataDir = TempDirectory.create().toAbsolutePath().toString();

    if (RuntimeEnvironment.getApiLevel() >= N) {
      applicationInfo.credentialProtectedDataDir = TempDirectory.create().toAbsolutePath().toString();
      applicationInfo.deviceProtectedDataDir = TempDirectory.create().toAbsolutePath().toString();
    }
    applicationInfo.labelRes = labelRes;
    String labelRef = androidManifest.getLabelRef();
    if (labelRef != null && !labelRef.startsWith("@")) {
      applicationInfo.nonLocalizedLabel = labelRef;
    }

    packageInfo.applicationInfo = applicationInfo;
    addPackage(packageInfo);
  }

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

  private int decodeFlags(Map<String, String> applicationAttributes) {
    int applicationFlags = 0;
    for (Pair<String, Integer> pair : APPLICATION_FLAGS) {
      if ("true".equals(applicationAttributes.get(pair.first))) {
        applicationFlags |= pair.second;
      }
    }
    return applicationFlags;
  }

  @Override
  public void removePackage(String packageName) {
    packageInfos.remove(packageName);
  }

  @Override
  public boolean hasSystemFeature(String name) {
    return systemFeatureList.containsKey(name) ? systemFeatureList.get(name) : false;
  }

  /**
   * Non-Android accessor.  Used to declare a system feature is or is not supported.
   *
   * @param name Feature name.
   * @param supported Is the feature supported?
   */
  @Override
  public void setSystemFeature(String name, boolean supported) {
    systemFeatureList.put(name, supported);
  }

  @Override
  public void addDrawableResolution(String packageName, int resourceId, Drawable drawable) {
    drawables.put(new Pair(packageName, resourceId), drawable);
  }

  @Override
  public Drawable getDrawable(String packageName, int resourceId, ApplicationInfo applicationInfo) {
    return drawables.get(new Pair(packageName, resourceId));
  }

  private List<ResolveInfo> findOrCreateInfoList(Intent intent) {
    List<ResolveInfo> infoList = resolveInfoForIntent.get(intent);
    if (infoList == null) {
      infoList = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList);
    }
    return infoList;
  }

  private List<ResolveInfo> queryIntent(Intent intent, int flags) {
    List<ResolveInfo> result = resolveInfoForIntent.get(intent);
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }

  private List<ResolveInfo> queryImplicitIntent(Intent intent, int flags) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Map.Entry<String, AndroidManifest> androidManifest : androidManifests.entrySet()) {
      String packageName = androidManifest.getKey();
      AndroidManifest appManifest = androidManifest.getValue();

      for (Map.Entry<String, ActivityData> activity : appManifest.getActivityDatas().entrySet()) {
        String activityName = activity.getKey();
        ActivityData activityData = activity.getValue();
        if (activityData.getTargetActivity() != null) {
          activityName = activityData.getTargetActivityName();
        }

        if (matchIntentFilter(activityData, intent)) {
          ResolveInfo resolveInfo = new ResolveInfo();
          resolveInfo.resolvePackageName = packageName;
          resolveInfo.activityInfo = new ActivityInfo();
          resolveInfo.activityInfo.targetActivity = activityName;

          resolveInfoList.add(resolveInfo);
        }
      }
    }

    return resolveInfoList;
  }

  private boolean matchIntentFilter(ActivityData activityData, Intent intent) {
    for (IntentFilterData intentFilterData : activityData.getIntentFilters()) {
      List<String> actionList = intentFilterData.getActions();
      List<String> categoryList = intentFilterData.getCategories();
      IntentFilter intentFilter = new IntentFilter();

      for (String action : actionList) {
        intentFilter.addAction(action);
      }

      for (String category : categoryList) {
        intentFilter.addCategory(category);
      }

      for (String scheme : intentFilterData.getSchemes()) {
        intentFilter.addDataScheme(scheme);
      }

      for (String mimeType : intentFilterData.getMimeTypes()) {
        try {
          intentFilter.addDataType(mimeType);
        } catch (IntentFilter.MalformedMimeTypeException ex) {
          throw new RuntimeException(ex);
        }
      }

      for (String path : intentFilterData.getPaths()) {
        intentFilter.addDataPath(path, PatternMatcher.PATTERN_LITERAL);
      }

      for (String pathPattern : intentFilterData.getPathPatterns()) {
        intentFilter.addDataPath(pathPattern, PatternMatcher.PATTERN_SIMPLE_GLOB);
      }

      for (String pathPrefix : intentFilterData.getPathPrefixes()) {
        intentFilter.addDataPath(pathPrefix, PatternMatcher.PATTERN_PREFIX);
      }

      for (IntentFilterData.DataAuthority authority : intentFilterData.getAuthorities()) {
        intentFilter.addDataAuthority(authority.getHost(), authority.getPort());
      }

      // match action
      boolean matchActionResult = intentFilter.matchAction(intent.getAction());
      // match category
      String matchCategoriesResult = intentFilter.matchCategories(intent.getCategories());
      // match data

      int matchResult = intentFilter.matchData(intent.getType(),
          (intent.getData() != null ? intent.getData().getScheme() : null),
          intent.getData());
      if (matchActionResult && (matchCategoriesResult == null) &&
          (matchResult != IntentFilter.NO_MATCH_DATA && matchResult != IntentFilter.NO_MATCH_TYPE)){
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isQueryIntentImplicitly() {
    return queryIntentImplicitly;
  }

  @Override
  public void setQueryIntentImplicitly(boolean queryIntentImplicitly) {
    this.queryIntentImplicitly = queryIntentImplicitly;
  }

  @Override
  public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    applicationEnabledSettingMap.put(packageName, newState);
  }

  @Override
  public int getApplicationEnabledSetting(String packageName) {
      try {
          PackageInfo packageInfo = getPackageInfo(packageName, -1);
      } catch (NameNotFoundException e) {
          throw new IllegalArgumentException(e);
      }

      return applicationEnabledSettingMap.get(packageName);
  }

  @Override
  public int checkPermission(String permName, String pkgName) {
    PackageInfo permissionsInfo = packageInfos.get(pkgName);
    if (permissionsInfo == null || permissionsInfo.requestedPermissions == null) {
      return PackageManager.PERMISSION_DENIED;
    }
    for (String permission : permissionsInfo.requestedPermissions) {
      if (permission != null && permission.equals(permName)) {
        return PackageManager.PERMISSION_GRANTED;
      }
    }
    return PackageManager.PERMISSION_DENIED;
  }

  @Override
  public void reset() {
    for (PackageInfo info : packageInfos.values()) {
      if (info.applicationInfo != null && info.applicationInfo.dataDir != null) {
        TempDirectory.destroy(Paths.get(info.applicationInfo.dataDir));
      }
    }
  }

  @Override
  public void setNameForUid(int uid, String name) {
    namesForUid.put(uid, name);
  }

  @Override
  public String getNameForUid(int uid) {
    return namesForUid.get(uid);
  }

  @Override
  public List<ApplicationInfo> getInstalledApplications(int flags) {
    List<ApplicationInfo> result = new LinkedList<>();

    for (PackageInfo packageInfo : packageInfos.values()) {
      result.add(packageInfo.applicationInfo);
    }
    return result;
  }

  public void setPackagesForCallingUid(String... packagesForCallingUid) {
    setPackagesForUid(Binder.getCallingUid(), packagesForCallingUid);
  }

  /**
   * Override value returned by {@link #getPackagesForUid(int)}.
   */
  public void setPackagesForUid(int uid, String... packagesForCallingUid) {
    this.packagesForUid.put(uid, packagesForCallingUid);
  }

  @Override
  public String[] getPackagesForUid(int uid) {
    String[] packageNames = packagesForUid.get(uid);
    if (packageNames != null) {
      return packageNames;
    }

    Set<String> results = new HashSet<>();
    for (PackageInfo packageInfo : packageInfos.values()) {
      if (packageInfo.applicationInfo != null && packageInfo.applicationInfo.uid == uid) {
        results.add(packageInfo.packageName);
      }
    }

    return results.isEmpty()
        ? null
        :results.toArray(new String[results.size()]);
  }

  /**
   * Goes through the meta data and puts each value in to a
   * bundle as the correct type.
   *
   * Note that this will convert resource identifiers specified
   * via the value attribute as well.
   * @param meta Meta data to put in to a bundle
   * @return bundle containing the meta data
   */
  private Bundle metaDataToBundle(Map<String, Object> meta) {
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

  @Override
  public void getPackageSizeInfo(String pkgName, int uid, final IPackageStatsObserver callback) {
    final PackageStats packageStats = packageStatsMap.get(pkgName);
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        try {
          callback.onGetStatsCompleted(packageStats, packageStats != null);
        } catch (RemoteException remoteException) {
          remoteException.rethrowFromSystemServer();
        }
      }
    });
  }

  @Override
  public int checkSignatures(String packageName1, String packageName2) {
    try {
      PackageInfo packageInfo1 = getPackageInfo(packageName1, GET_SIGNATURES);
      PackageInfo packageInfo2 = getPackageInfo(packageName2, GET_SIGNATURES);
      return compareSignature(packageInfo1.signatures, packageInfo2.signatures);
    } catch (NameNotFoundException e) {
      return SIGNATURE_UNKNOWN_PACKAGE;
    }
  }

  // From com.android.server.pm.PackageManagerService.compareSignatures().
  private static int compareSignature(Signature[] signatures1, Signature[] signatures2) {
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

  @Override
  public String getInstallerPackageName(String packageName) {
    return packageInstallerMap.get(packageName);
  }

  @Override
  public void setInstallerPackageName(String targetPackage, String installerPackageName) {
    packageInstallerMap.put(targetPackage, installerPackageName);
  }

  public void setDependencies(AndroidManifest applicationManifest, ResourceTable appResourceTable) {
    this.applicationManifest = applicationManifest;
    this.appResourceTable = appResourceTable;
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
}
