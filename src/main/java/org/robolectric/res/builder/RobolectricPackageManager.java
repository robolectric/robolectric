package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import org.robolectric.AndroidManifest;
import org.robolectric.shadows.ShadowContext;
import org.robolectric.tester.android.content.pm.StubPackageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RobolectricPackageManager extends StubPackageManager {

  private final Map<String, AndroidManifest> manifests = new LinkedHashMap<String, AndroidManifest>();
  private final Map<String, PackageInfo> packageList = new LinkedHashMap<String, PackageInfo>();
  private Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new HashMap<Intent, List<ResolveInfo>>();
  private Map<ComponentName, ComponentState> componentList = new HashMap<ComponentName, ComponentState>();
  private Map<ComponentName, Drawable> drawableList = new HashMap<ComponentName, Drawable>();
  private Map<String, Boolean> systemFeatureList = new HashMap<String, Boolean>();
  private Map<IntentFilter, ComponentName> preferredActivities = new HashMap<IntentFilter, ComponentName>();
  private Map<Pair<String, Integer>, Drawable> drawables = new HashMap<Pair<String, Integer>, Drawable>();

  private ApplicationInfo applicationInfo;

  @Override
  public PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
    if (packageList.containsKey(packageName)) {
      return packageList.get(packageName);
    }

    throw new NameNotFoundException();
  }

  @Override
  public ApplicationInfo getApplicationInfo(String packageName, int flags) throws NameNotFoundException {
    AndroidManifest androidManifest = manifests.get(packageName);

    if (androidManifest != null) {
      if (applicationInfo == null) {
        applicationInfo = new ApplicationInfo();
        applicationInfo.flags = androidManifest.getApplicationFlags();
        applicationInfo.targetSdkVersion = androidManifest.getTargetSdkVersion();
        applicationInfo.packageName = androidManifest.getPackageName();
        applicationInfo.processName = androidManifest.getProcessName();
        applicationInfo.name = androidManifest.getApplicationName();
        applicationInfo.sourceDir = new File(".").getAbsolutePath();
        applicationInfo.dataDir = ShadowContext.FILES_DIR.getAbsolutePath();
      }
      return applicationInfo;
    }

    PackageInfo info;
    if ((info = packageList.get(packageName)) != null) {
      return info.applicationInfo;
    }

    throw new NameNotFoundException();
  }



  @Override
  public List<PackageInfo> getInstalledPackages(int flags) {
    return new ArrayList<PackageInfo>(packageList.values());
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
    Intent i = new Intent();
    i.setComponent( new ComponentName(packageName, "") );
    return i;
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
  public RobolectricPackageManager.ComponentState getComponentState(ComponentName componentName) {
    return componentList.get(componentName);
  }

  /**
   * Non-Android accessor.  Used to add a package to the list of those
   * already 'installed' on system.
   *
   * @param packageInfo
   */
  public void addPackage(PackageInfo packageInfo) {
    packageList.put(packageInfo.packageName, packageInfo);
  }

  public void removePackage(String packageName) {
    packageList.remove(packageName);
  }

  public void addPackage(String packageName) {
    PackageInfo info = new PackageInfo();
    info.packageName = packageName;
    addPackage(info);
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

  public void addManifest(AndroidManifest androidManifest) {
    manifests.put(androidManifest.getPackageName(), androidManifest);

    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = androidManifest.getPackageName();
    packageInfo.versionName = androidManifest.getVersionName();
    packageInfo.versionCode = androidManifest.getVersionCode();
    addPackage(packageInfo);
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
