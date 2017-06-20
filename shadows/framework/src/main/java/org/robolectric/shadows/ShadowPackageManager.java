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
import static android.content.pm.PackageManager.GET_META_DATA;
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
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.PatternMatcher;
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
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.manifest.IntentFilterData;
import org.robolectric.manifest.PathPermissionData;
import org.robolectric.manifest.PermissionItemData;
import org.robolectric.manifest.ServiceData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.ResName;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.util.TempDirectory;

@Implements(PackageManager.class)
public class ShadowPackageManager implements RobolectricPackageManager {

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
  protected Map<String, Boolean> permissionRationaleMap = new HashMap<>();
  protected List<FeatureInfo> systemAvailableFeatures = new LinkedList<>();
  private Map<String, PackageInfo> packageArchiveInfo = new HashMap<>();
  protected final Map<Integer, Integer> verificationResults = new HashMap<>();
  protected final Map<Integer, Long> verificationTimeoutExtension = new HashMap<>();
  protected final Map<String, String> currentToCanonicalNames = new HashMap<>();

  public final Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new TreeMap<>(new IntentComparator());
  
  public ShadowPackageManager() {
    addManifest(RuntimeEnvironment.getAppManifest());
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
  protected static Bundle metaDataToBundle(Map<String, Object> meta) {
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
  protected static int compareSignature(Signature[] signatures1, Signature[] signatures2) {
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

  protected static String resolvePackageName(String packageName, ComponentName componentName) {
    String classString = componentName.getClassName();
    int index = classString.indexOf('.');
    if (index == -1) {
      classString = packageName + "." + classString;
    } else if (index == 0) {
      classString = packageName + classString;
    }
    return classString;
  }

  protected static PathPermission[] createPathPermissions(List<PathPermissionData> pathPermissionDatas) {
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

  protected static IntentFilter matchIntentFilter(Intent intent, List<IntentFilterData> intentFilters) {
    for (IntentFilterData intentFilterData : intentFilters) {
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
        return intentFilter;
      }
    }
    return null;
  }

  protected static ResolveInfo getResolveInfo(ServiceData service, IntentFilter intentFilter,
      String packageName) {
    try {
      ResolveInfo info = new ResolveInfo();
      info.isDefault = intentFilter.hasCategory("Intent.CATEGORY_DEFAULT");
      info.serviceInfo = new ServiceInfo();
      info.serviceInfo.name = service.getClassName();
      info.serviceInfo.packageName = packageName;
      info.serviceInfo.applicationInfo = new ApplicationInfo();
      info.filter = new IntentFilter();
      for (Iterator<String> it = intentFilter.typesIterator(); it.hasNext(); ) {
        info.filter.addDataType(it.next());
      }
      return info;
    } catch (IntentFilter.MalformedMimeTypeException e) {
      throw new RuntimeException(e);
    }
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

  protected static PermissionInfo createPermissionInfo(int flags,
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

  private static int decodeFlags(Map<String, String> applicationAttributes) {
    int applicationFlags = 0;
    for (Pair<String, Integer> pair : APPLICATION_FLAGS) {
      if ("true".equals(applicationAttributes.get(pair.first))) {
        applicationFlags |= pair.second;
      }
    }
    return applicationFlags;
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
    resolveInfoForIntent.put(intent, info);
  }

  @Override
  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    List<ResolveInfo> infoList1 = resolveInfoForIntent.get(intent);
    if (infoList1 == null) {
      infoList1 = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList1);
    }
    List<ResolveInfo> infoList = infoList1;
    infoList.add(info);
  }

  @Override
  public void removeResolveInfosForIntent(Intent intent, String packageName) {
    List<ResolveInfo> infoList1 = resolveInfoForIntent.get(intent);
    if (infoList1 == null) {
      infoList1 = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList1);
    }
    List<ResolveInfo> infoList = infoList1;
    for (Iterator<ResolveInfo> iterator = infoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      if (resolveInfo.activityInfo.packageName.equals(packageName)) {
        iterator.remove();
      }
    }
  }

  @Override
  public Drawable getActivityIcon(Intent intent) throws NameNotFoundException {
    return drawableList.get(intent.getComponent());
  }

  @Override
  public Drawable getActivityIcon(ComponentName componentName) throws NameNotFoundException {
    return drawableList.get(componentName);
  }

  @Override
  public void addActivityIcon(ComponentName component, Drawable drawable) {
    drawableList.put(component, drawable);
  }

  @Override
  public void addActivityIcon(Intent intent, Drawable drawable) {
    drawableList.put(intent.getComponent(), drawable);
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
    applicationIcons.put(packageName, drawable);
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
    applicationEnabledSettingMap.put(packageName, newState);
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
  public ComponentState getComponentState(ComponentName componentName) {
    return componentList.get(componentName);
  }

  @Override
  public void addPackage(PackageInfo packageInfo) {
    PackageStats packageStats = new PackageStats(packageInfo.packageName);
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    Resources r = new Resources(new AssetManager(), null, null);
    resources.put(packageInfo.packageName, r);
    if (packageInfo.applicationInfo != null) {
      namesForUid.put(packageInfo.applicationInfo.uid, packageInfo.packageName);
    }
  }

  @Override
  public void addPackage(PackageInfo packageInfo, PackageStats packageStats) {
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    Resources r = new Resources(new AssetManager(), null, null);
    resources.put(packageInfo.packageName, r);
    if (packageInfo.applicationInfo != null) {
      namesForUid.put(packageInfo.applicationInfo.uid, packageInfo.packageName);
    }
  }

  @Override
  public void addPermissionInfo(PermissionInfo permissionInfo) {
    extraPermissions.put(permissionInfo.name, permissionInfo);
  }

  @Override
  public void addPackage(String packageName) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = packageName;
    setUpPackageStorage(applicationInfo);

    packageInfo.applicationInfo = applicationInfo;

    PackageStats packageStats = new PackageStats(packageInfo.packageName);
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    Resources r = new Resources(new AssetManager(), null, null);
    resources.put(packageInfo.packageName, r);
    if (packageInfo.applicationInfo != null) {
      namesForUid.put(packageInfo.applicationInfo.uid, packageInfo.packageName);
    }
  }

  @Override
  public void addManifest(AndroidManifest androidManifest) {
    androidManifests.put(androidManifest.getPackageName(), androidManifest);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = androidManifest.getPackageName();
    packageInfo.versionName = androidManifest.getVersionName();
    packageInfo.versionCode = androidManifest.getVersionCode();

    Map<String,ActivityData> activityDatas = androidManifest.getActivityDatas();

    for (ActivityData data : activityDatas.values()) {
      String name = data.getName();
      String activityName = name.startsWith(".") ? androidManifest.getPackageName() + name : name;
      Intent intent = new Intent(activityName);
      List<ResolveInfo> infoList1 = resolveInfoForIntent.get(intent);
      if (infoList1 == null) {
        infoList1 = new ArrayList<>();
        resolveInfoForIntent.put(intent, infoList1);
      }
      List<ResolveInfo> infoList = infoList1;
      infoList.add(new ResolveInfo());
    }

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
        info.metaData = metaDataToBundle(cpdata[i].getMetaData().getValueMap());
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
        List<ResolveInfo> infoList1 = resolveInfoForIntent.get(intent);
        if (infoList1 == null) {
          infoList1 = new ArrayList<>();
          resolveInfoForIntent.put(intent, infoList1);
        }
        List<ResolveInfo> infoList = infoList1;
        infoList.add(resolveInfo);
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
    setUpPackageStorage(applicationInfo);

    int labelRes = 0;
    if (androidManifest.getLabelRef() != null) {
      String fullyQualifiedName = ResName.qualifyResName(androidManifest.getLabelRef(), androidManifest
          .getPackageName());
      Integer id = fullyQualifiedName == null ? null : RuntimeEnvironment.getAppResourceTable().getResourceId(new ResName(fullyQualifiedName));
      labelRes = id != null ? id : 0;
    }

    applicationInfo.labelRes = labelRes;
    String labelRef = androidManifest.getLabelRef();
    if (labelRef != null && !labelRef.startsWith("@")) {
      applicationInfo.nonLocalizedLabel = labelRef;
    }

    packageInfo.applicationInfo = applicationInfo;
    PackageStats packageStats = new PackageStats(packageInfo.packageName);
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    Resources r = new Resources(new AssetManager(), null, null);
    resources.put(packageInfo.packageName, r);
    if (packageInfo.applicationInfo != null) {
      namesForUid.put(packageInfo.applicationInfo.uid, packageInfo.packageName);
    }
  }

  @Override
  public void removePackage(String packageName) {
    packageInfos.remove(packageName);
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
    return queryIntentImplicitly;
  }

  @Override
  public void setQueryIntentImplicitly(boolean queryIntentImplicitly) {
    queryIntentImplicitly = queryIntentImplicitly;
  }

  @Override
  public void setNameForUid(int uid, String name) {
    namesForUid.put(uid, name);
  }

  @Override
  public void setPackagesForCallingUid(String... packagesForCallingUid) {
    packagesForUid.put(Binder.getCallingUid(), packagesForCallingUid);
  }

  @Override
  public void setPackagesForUid(int uid, String... packagesForCallingUid) {
    packagesForUid.put(uid, packagesForCallingUid);
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

  @Override @Implementation
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

  public final Map<String, AndroidManifest> androidManifests = new LinkedHashMap<>();
  public final Map<String, PackageInfo> packageInfos = new LinkedHashMap<>();
  public final Map<String, PackageStats> packageStatsMap = new HashMap<>();
  public final Map<ComponentName, ComponentState> componentList = new LinkedHashMap<>();
  public final Map<ComponentName, Drawable> drawableList = new LinkedHashMap<>();
  public final Map<String, Drawable> applicationIcons = new HashMap<>();
  public final Map<String, Boolean> systemFeatureList = new LinkedHashMap<>();
  public final Map<IntentFilter, ComponentName> preferredActivities = new LinkedHashMap<>();
  public final Map<Pair<String, Integer>, Drawable> drawables = new LinkedHashMap<>();
  public final Map<String, Integer> applicationEnabledSettingMap = new HashMap<>();
  public final Map<Integer, String> namesForUid = new HashMap<>();
  public final Map<Integer, String[]> packagesForUid = new HashMap<>();
  public final Map<String, String> packageInstallerMap = new HashMap<>();
  public boolean queryIntentImplicitly = false;
  public Map<String, PermissionInfo> extraPermissions = new HashMap<>();
  public Map<String, Resources> resources = new HashMap<>();







  protected List<ResolveInfo> queryIntent(Intent intent, int flags) {
    List<ResolveInfo> result = resolveInfoForIntent.get(intent);
    if (result == null) {
      return Collections.emptyList();
    } else {
      return result;
    }
  }
  
  private Set<String> deletedPackages = new HashSet<>();
  public Map<String, IPackageDeleteObserver> pendingDeleteCallbacks = new HashMap<>();

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

  public static class IntentComparator implements Comparator<Intent> {


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
