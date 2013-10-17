package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.res.ActivityData;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceIndex;
import org.robolectric.shadows.ShadowContext;
import org.robolectric.tester.android.content.pm.StubPackageManager;

public class RobolectricPackageManager extends StubPackageManager {

  private final Map<String, AndroidManifest> androidManifests = new LinkedHashMap<String, AndroidManifest>();
  private final Map<String, PackageInfo> packageInfos = new LinkedHashMap<String, PackageInfo>();
  private Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new LinkedHashMap<Intent, List<ResolveInfo>>();
  private Map<ComponentName, ComponentState> componentList = new LinkedHashMap<ComponentName, ComponentState>();
  private Map<ComponentName, Drawable> drawableList = new LinkedHashMap<ComponentName, Drawable>();
  private Map<String, Boolean> systemFeatureList = new LinkedHashMap<String, Boolean>();
  private Map<IntentFilter, ComponentName> preferredActivities = new LinkedHashMap<IntentFilter, ComponentName>();
  private Map<Pair<String, Integer>, Drawable> drawables = new LinkedHashMap<Pair<String, Integer>, Drawable>();

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
    String packageName = className.getPackageName();
    AndroidManifest androidManifest = androidManifests.get(packageName);
    String activityName = className.getClassName();
    ActivityData activityData = androidManifest.getActivityData(activityName);
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.packageName = packageName;
    activityInfo.name = activityName;
    if (activityData != null) {
      ResourceIndex resourceIndex = Robolectric.getShadowApplication().getResourceLoader().getResourceIndex();
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
  public List<PackageInfo> getInstalledPackages(int flags) {
    return new ArrayList<PackageInfo>(packageInfos.values());
  }

  @Override
  public List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    return queryIntent(intent, flags);
  }

  @Override
  public List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
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

  public void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info) {
    resolveInfoForIntent.put(intent, info);
  }

  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    List<ResolveInfo> infoList = findOrCreateInfoList(intent);
    infoList.add(info);
  }

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

  public void addActivityIcon(ComponentName component, Drawable d) {
    drawableList.put(component, d);
  }

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
  public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    componentList.put(componentName, new ComponentState(newState, flags));
  }

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
          outActivities = new ArrayList<ComponentName>();
        }

        outActivities.add(name);
      }
    }

    return 0;
  }

  /**
   * Non-Android accessor.  Use to make assertions on values passed to
   * setComponentEnabledSetting.
   *
   * @param componentName
   * @return
   */
  public ComponentState getComponentState(ComponentName componentName) {
    return componentList.get(componentName);
  }

  /**
   * Non-Android accessor.  Used to add a package to the list of those
   * already 'installed' on system.
   *
   * @param packageInfo
   */
  public void addPackage(PackageInfo packageInfo) {
    packageInfos.put(packageInfo.packageName, packageInfo);
  }

  public void addPackage(String packageName) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.packageName = packageName;
    initApplicationInfo(applicationInfo);

    packageInfo.applicationInfo = applicationInfo;

    addPackage(packageInfo);
  }

  public void addManifest(AndroidManifest androidManifest) {
    androidManifests.put(androidManifest.getPackageName(), androidManifest);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = androidManifest.getPackageName();
    packageInfo.versionName = androidManifest.getVersionName();
    packageInfo.versionCode = androidManifest.getVersionCode();

    ApplicationInfo applicationInfo = new ApplicationInfo();
    applicationInfo.flags = androidManifest.getApplicationFlags();
    applicationInfo.targetSdkVersion = androidManifest.getTargetSdkVersion();
    applicationInfo.packageName = androidManifest.getPackageName();
    applicationInfo.processName = androidManifest.getProcessName();
    applicationInfo.name = androidManifest.getApplicationName();
    initApplicationInfo(applicationInfo);
    initApplicationMetaData(applicationInfo, androidManifest);

    packageInfo.applicationInfo = applicationInfo;

    addPackage(packageInfo);
  }

  private void initApplicationInfo(ApplicationInfo applicationInfo) {
    applicationInfo.sourceDir = new File(".").getAbsolutePath();
    applicationInfo.dataDir = ShadowContext.FILES_DIR.getAbsolutePath();
  }

  private void initApplicationMetaData(ApplicationInfo applicationInfo, AndroidManifest androidManifest) {
    Map<String, String> meta = androidManifest.getApplicationMetaData();
    if (meta.isEmpty()) { return; }
    applicationInfo.metaData = new Bundle();
    for (Entry<String, String> metaEntry : meta.entrySet()) {
      applicationInfo.metaData.putString(metaEntry.getKey(), metaEntry.getValue());
    }
  }
  
  public void removePackage(String packageName) {
    packageInfos.remove(packageName);
  }

  @Override
  public boolean hasSystemFeature(String name) {
    return systemFeatureList.containsKey(name) ? systemFeatureList.get(name) : false;
  }

  /**
   * Non-Android accessor.  Used to declare a system feature is
   * or is not supported.
   *
   * @param name
   * @param supported
   */
  public void setSystemFeature(String name, boolean supported) {
    systemFeatureList.put(name, supported);
  }

  public void addDrawableResolution(String packageName, int resourceId, Drawable drawable) {
    drawables.put(new Pair(packageName, resourceId), drawable);
  }

  public class ComponentState {
    public int newState;
    public int flags;

    public ComponentState(int newState, int flags) {
      this.newState = newState;
      this.flags = flags;
    }
  }

  @Override
  public Drawable getDrawable(String packageName, int resourceId, ApplicationInfo applicationInfo) {
    return drawables.get(new Pair(packageName, resourceId));
  }

  private List<ResolveInfo> findOrCreateInfoList(Intent intent) {
    List<ResolveInfo> infoList = resolveInfoForIntent.get(intent);
    if (infoList == null) {
      infoList = new ArrayList<ResolveInfo>();
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
}
