package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.util.Pair;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.robolectric.ShadowsAdapter;
import org.robolectric.manifest.ActivityData;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.BroadcastReceiverData;
import org.robolectric.manifest.ContentProviderData;
import org.robolectric.manifest.IntentFilterData;
import org.robolectric.manifest.ServiceData;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.util.TempDirectory;

public class DefaultPackageManager extends StubPackageManager implements RobolectricPackageManager {

  public DefaultPackageManager(ShadowsAdapter shadowsAdapter) {
    this.shadowsAdapter = shadowsAdapter;
  }

  static class IntentComparator implements Comparator<Intent> {

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

  private final ShadowsAdapter shadowsAdapter;
  private final Map<String, AndroidManifest> androidManifests = new LinkedHashMap<>();
  private final Map<String, PackageInfo> packageInfos = new LinkedHashMap<>();
  private Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new TreeMap<>(new IntentComparator());
  private Map<ComponentName, ComponentState> componentList = new LinkedHashMap<>();
  private Map<ComponentName, Drawable> drawableList = new LinkedHashMap<>();
  private Map<String, Drawable> applicationIcons = new HashMap<>();
  private Map<String, Boolean> systemFeatureList = new LinkedHashMap<>();
  private Map<IntentFilter, ComponentName> preferredActivities = new LinkedHashMap<>();
  private Map<Pair<String, Integer>, Drawable> drawables = new LinkedHashMap<>();
  private boolean queryIntentImplicitly = false;
  private HashMap<String, Integer> applicationEnabledSettingMap = new HashMap<>();

  @Override
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    if (packageInfos.containsKey(packageName)) {
      return packageInfos.get(packageName);
    }

    throw new NameNotFoundException();
  }

  @Override
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    PackageInfo info = packageInfos.get(packageName);
    if (info != null) {
      return info.applicationInfo;
    } else {
      throw new NameNotFoundException();
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
      activityInfo.parentActivityName = activityData.getParentActivityName();
      activityInfo.metaData = metaDataToBundle(activityData.getMetaData().getValueMap());
      ResourceIndex resourceIndex = shadowsAdapter.getResourceLoader().getResourceIndex();
      String themeRef;

      // Based on ShadowActivity
      if (activityData.getThemeRef() != null) {
        themeRef = activityData.getThemeRef();
      } else {
        themeRef = androidManifest.getThemeRef();
      }
      if (themeRef != null) {
        ResName style = ResName.qualifyResName(themeRef.replace("@", ""), packageName, "style");
        activityInfo.theme = resourceIndex.getResourceId(style);
      }
    }
    activityInfo.applicationInfo = getApplicationInfo(packageName, flags);
    return activityInfo;
  }

  @Override
  public ActivityInfo getReceiverInfo(ComponentName className, int flags) throws NameNotFoundException {
    String packageName = className.getPackageName();
    AndroidManifest androidManifest = androidManifests.get(packageName);
    String classString = className.getClassName();
    int index = classString.indexOf('.');
    if (index == -1) {
      classString = packageName + "." + classString;
    } else if (index == 0) {
      classString = packageName + classString;
    }

    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.packageName = packageName;
    activityInfo.name = classString;
    if ((flags & GET_META_DATA) != 0) {
      for (BroadcastReceiverData receiver : androidManifest.getBroadcastReceivers()) {
        if (receiver.getClassName().equals(classString)) {
          activityInfo.metaData = metaDataToBundle(receiver.getMetaData().getValueMap());
          break;
        }
      }
    }
    return activityInfo;
  }
  
  @Override
  public ServiceInfo getServiceInfo(ComponentName className, int flags) throws NameNotFoundException {
    String packageName = className.getPackageName();
    AndroidManifest androidManifest = androidManifests.get(packageName);
    String serviceName = className.getClassName();
    ServiceData serviceData = androidManifest.getServiceData(serviceName);
    if (serviceData == null) {
      throw new NameNotFoundException();
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
    packageInfos.put(packageInfo.packageName, packageInfo);
    applicationEnabledSettingMap.put(packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
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
  public void addManifest(AndroidManifest androidManifest, ResourceLoader loader) {
    androidManifests.put(androidManifest.getPackageName(), androidManifest);
    ResourceIndex resourceIndex = loader.getResourceIndex();

    // first opportunity to access a resource index for this manifest, use it to init the references
    androidManifest.initMetaData(loader);

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
        info.authority = cpdata[i].getAuthority();
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
    applicationInfo.flags = androidManifest.getApplicationFlags();
    applicationInfo.targetSdkVersion = androidManifest.getTargetSdkVersion();
    applicationInfo.packageName = androidManifest.getPackageName();
    applicationInfo.processName = androidManifest.getProcessName();
    applicationInfo.name = androidManifest.getApplicationName();
    applicationInfo.metaData = metaDataToBundle(androidManifest.getApplicationMetaData());
    applicationInfo.sourceDir = new File(".").getAbsolutePath();
    applicationInfo.dataDir = TempDirectory.create().toAbsolutePath().toString();

    if (androidManifest.getLabelRef() != null && resourceIndex != null) {
      Integer id = ResName.getResourceId(resourceIndex, androidManifest.getLabelRef(), androidManifest.getPackageName());
      applicationInfo.labelRes = id != null ? id : 0;
    }

    packageInfo.applicationInfo = applicationInfo;
    addPackage(packageInfo);
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
}
