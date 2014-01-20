package org.robolectric.res.builder;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Pair;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.res.ActivityData;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowContext;
import org.robolectric.tester.android.content.pm.StubPackageManager;

public class RobolectricPackageManager extends StubPackageManager {

  private static class IntentComparator implements Comparator<Intent> {

    @Override
    public int compare(Intent i1, Intent i2) {
      if (i1 == null && i2 == null) return 0;
      if (i1 == null && i2 != null) return -1;
      if (i1 != null && i2 == null) return 1;
      if (i1.equals(i2)) return 0;
      if (i1.getAction() == null && i2.getAction() != null) return -1;
      if (i1.getAction() != null && i2.getAction() == null) return 1;
      if (i1.getAction() != null && i2.getAction() != null) {
        if (!i1.getAction().equals(i2.getAction())) {
          return i1.getAction().compareTo(i2.getAction());
        }
      }
      if (i1.getData() == null && i2.getData() != null) return -1;
      if (i1.getData() != null && i2.getData() == null) return 1;
      if (i1.getData() != null && i2.getData() != null) {
        if (!i1.getData().equals(i2.getData())) {
          return i1.getData().compareTo(i2.getData());
        }
      }
      if (i1.getComponent() == null && i2.getComponent() != null) return -1;
      if (i1.getComponent() != null && i2.getComponent() == null) return 1;
      if (i1.getComponent() != null && i2.getComponent() != null) {
        if (!i1.getComponent().equals(i2.getComponent())) {
          return i1.getComponent().compareTo(i2.getComponent());
        }
      }
      if (i1.getPackage() == null && i2.getPackage() != null) return -1;
      if (i1.getPackage() != null && i2.getPackage() == null) return 1;
      if (i1.getPackage() != null && i2.getPackage() != null) {
        if (!i1.getPackage().equals(i2.getPackage())) {
          return i1.getPackage().compareTo(i2.getPackage());
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

  private final Map<String, AndroidManifest> androidManifests = new LinkedHashMap<String, AndroidManifest>();
  private final Map<String, PackageInfo> packageInfos = new LinkedHashMap<String, PackageInfo>();
  private Map<Intent, List<ResolveInfo>> resolveInfoForIntent = new TreeMap<Intent, List<ResolveInfo>>(new IntentComparator());
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
      for (int i = 0; i < androidManifest.getReceiverCount(); ++i) {
        if (androidManifest.getReceiverClassName(i).equals(classString)) {
          activityInfo.metaData = metaDataToBundle(androidManifest.getReceiverMetaData(i));
          break;
        }
      }
    }
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

  public void addManifest(AndroidManifest androidManifest, ResourceLoader loader) {
    androidManifests.put(androidManifest.getPackageName(), androidManifest);
    ResourceIndex resourceIndex = loader.getResourceIndex();

    // first opportunity to access a resource index for this manifest, use it to init the references
    androidManifest.initMetaData(resourceIndex);

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
    applicationInfo.metaData = metaDataToBundle(androidManifest.getApplicationMetaData());

    if (androidManifest.getLabelRef() != null && resourceIndex != null) {
      Integer id = ResName.getResourceId(resourceIndex, androidManifest.getLabelRef(), androidManifest.getPackageName());
      applicationInfo.labelRes = id != null ? id : 0;
    }

    packageInfo.applicationInfo = applicationInfo;
    initApplicationInfo(applicationInfo);
    addPackage(packageInfo);
  }

  private void initApplicationInfo(ApplicationInfo applicationInfo) {
    applicationInfo.sourceDir = new File(".").getAbsolutePath();
    applicationInfo.dataDir = ShadowContext.FILES_DIR.getAbsolutePath();
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

  /***
   * Goes through the meta data and puts each value in to a
   * bundle as the correct type.
   *
   * Note that this will convert resource identifiers specified
   * via the value attribute as well.
   * @param meta Meta data to put in to a bundle
   * @return bundle containing the meta data
   */
  private Bundle metaDataToBundle(Map<String, String> meta) {
    if (meta.size() == 0) {
        return null;
    }

    Bundle bundle = new Bundle();

    for (Map.Entry<String,String> entry : meta.entrySet()) {
      if (entry.getValue() == null) {
        // skip it
      } else if ("true".equals(entry.getValue())) {
        bundle.putBoolean(entry.getKey(), true);
      } else if ("false".equals(entry.getValue())) {
        bundle.putBoolean(entry.getKey(), false);
      } else {
        if (entry.getValue().contains(".")) {
          // if it's a float, add it and continue
          try {
            bundle.putFloat(entry.getKey(), Float.parseFloat(entry.getValue()));
          } catch (NumberFormatException ef) {
            /* Not a float */
          }
        }

        if (!bundle.containsKey(entry.getKey()) && !entry.getValue().startsWith("#")) {
          // if it's an int, add it and continue
          try {
            bundle.putInt(entry.getKey(), Integer.parseInt(entry.getValue()));
          } catch (NumberFormatException ei) {
            /* Not an int */
          }
        }

        if (!bundle.containsKey(entry.getKey())) {
          // if it's a color, add it and continue
          try {
            bundle.putInt(entry.getKey(), Color.parseColor(entry.getValue()));
          } catch (IllegalArgumentException e) {
            /* Not a color */
          }
        }

        if (!bundle.containsKey(entry.getKey())) {
          // otherwise it's a string
          bundle.putString(entry.getKey(), entry.getValue());
        }
      }
    }
    return bundle;
  }
}
