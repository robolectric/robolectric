package org.robolectric.shadows;

import static android.content.IntentFilter.MATCH_CATEGORY_MASK;
import static android.content.pm.ApplicationInfo.FLAG_INSTALLED;
import static android.content.pm.ApplicationInfo.FLAG_SYSTEM;
import static android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_PROVIDERS;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_RESOLVED_FILTER;
import static android.content.pm.PackageManager.GET_SERVICES;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.MATCH_ALL;
import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;
import static android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES;
import static android.content.pm.PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.robolectric.annotation.GetInstallerPackageNameMode.Mode.REALISTIC;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.Manifest.permission;
import android.annotation.DrawableRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.UserIdInt;
import android.app.ApplicationPackageManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.ComponentInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.VerifierDeviceIdentity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import android.telecom.TelecomManager;
import android.util.Log;
import android.util.Pair;
import com.google.common.base.Function;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.BiConsumer;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.GetInstallerPackageNameMode;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(value = ApplicationPackageManager.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowApplicationPackageManager extends ShadowPackageManager {
  /** Package name of the Android platform. */
  private static final String PLATFORM_PACKAGE_NAME = "android";

  /** MIME type of Android Packages (APKs). */
  private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

  /** {@link Uri} scheme of installed apps. */
  private static final String PACKAGE_SCHEME = "package";

  @RealObject private ApplicationPackageManager realObject;

  @Implementation
  public List<PackageInfo> getInstalledPackages(int flags) {
    List<PackageInfo> result = new ArrayList<>();
    synchronized (packageInfos) {
      for (String packageName : packageInfos.keySet()) {
        try {
          PackageInfo packageInfo = getPackageInfo(packageName, flags);
          result.add(packageInfo);
        } catch (NameNotFoundException e) {
          Log.i(TAG, String.format("Package %s filtered out: %s", packageName, e.getMessage()));
        }
      }
    }
    return result;
  }

  @Implementation(minSdk = Q)
  protected List<ModuleInfo> getInstalledModules(int flags) {
    List<ModuleInfo> result = new ArrayList<>();
    synchronized (moduleInfos) {
      for (String moduleName : moduleInfos.keySet()) {
        try {
          ModuleInfo moduleInfo = (ModuleInfo) getModuleInfo(moduleName, flags);
          result.add(moduleInfo);
        } catch (NameNotFoundException e) {
          Log.i(TAG, String.format("Module %s filtered out: %s", moduleName, e.getMessage()));
        }
      }
    }
    return result;
  }

  @Implementation(minSdk = Q)
  protected Object getModuleInfo(String packageName, int flags) throws NameNotFoundException {
    // Double checks that the respective package matches and is not disabled
    getPackageInfo(packageName, flags);

    synchronized (moduleInfos) {
      Object info = moduleInfos.get(packageName);
      if (info == null) {
        throw new NameNotFoundException("Module: " + packageName + " is not installed.");
      }
      return info;
    }
  }

  @Implementation
  protected ActivityInfo getActivityInfo(ComponentName component, int flags)
      throws NameNotFoundException {
    return getComponentInfo(
        component,
        flags,
        packageInfo -> packageInfo.activities,
        resolveInfo -> resolveInfo.activityInfo,
        ActivityInfo::new);
  }

  private <T extends ComponentInfo> T getComponentInfo(
      ComponentName component,
      int flags,
      Function<PackageInfo, T[]> componentsInPackage,
      Function<ResolveInfo, T> componentInResolveInfo,
      Function<T, T> copyConstructor)
      throws NameNotFoundException {
    String activityName = component.getClassName();
    String packageName = component.getPackageName();
    PackageInfo packageInfo = getInternalMutablePackageInfo(packageName);
    T result = null;
    ApplicationInfo appInfo = null;
    // search in the manifest
    if (packageInfo != null) {
      if (packageInfo.applicationInfo != null) {
        appInfo = packageInfo.applicationInfo;
      }
      T[] components = componentsInPackage.apply(packageInfo);
      if (components != null) {
        for (T activity : components) {
          if (activityName.equals(activity.name)) {
            result = copyConstructor.apply(activity);
            break;
          }
        }
      }
    }
    if (result == null) {
      // look in the registered intents
      outer:
      for (List<ResolveInfo> listOfResolveInfo : resolveInfoForIntent.values()) {
        for (ResolveInfo resolveInfo : listOfResolveInfo) {
          T info = componentInResolveInfo.apply(resolveInfo);
          if (isValidComponentInfo(info)
              && component.equals(new ComponentName(info.applicationInfo.packageName, info.name))) {
            result = copyConstructor.apply(info);
            if (appInfo == null) {
              // we found valid app info in the resolve info. Use it.
              appInfo = result.applicationInfo;
            }
            break outer;
          }
        }
      }
    }
    if (result == null) {
      throw new NameNotFoundException("Component not found: " + component);
    }
    if (appInfo == null) {
      appInfo = new ApplicationInfo();
      appInfo.packageName = packageName;
      appInfo.flags = ApplicationInfo.FLAG_INSTALLED;
    } else {
      appInfo = new ApplicationInfo(appInfo);
    }
    result.applicationInfo = appInfo;
    applyFlagsToComponentInfo(result, flags);
    return result;
  }

  @Implementation
  protected boolean hasSystemFeature(String name) {
    return systemFeatureList.containsKey(name) ? systemFeatureList.get(name) : false;
  }

  @Implementation
  protected int getComponentEnabledSetting(ComponentName componentName) {
    ComponentState state = componentList.get(componentName);
    return state != null ? state.newState : PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
  }

  @Implementation
  protected @Nullable String getNameForUid(int uid) {
    return namesForUid.get(uid);
  }

  @Implementation
  @Override
  protected @Nullable String[] getPackagesForUid(int uid) {
    String[] packageNames = packagesForUid.get(uid);
    if (packageNames != null) {
      return packageNames;
    }

    Set<String> results = new HashSet<>();
    synchronized (packageInfos) {
      for (PackageInfo packageInfo : packageInfos.values()) {
        if (packageInfo.applicationInfo != null && packageInfo.applicationInfo.uid == uid) {
          results.add(packageInfo.packageName);
        }
      }
    }

    return results.isEmpty() ? null : results.toArray(new String[results.size()]);
  }

  @Implementation
  protected int getApplicationEnabledSetting(String packageName) {
    synchronized (packageInfos) {
      if (!packageInfos.containsKey(packageName)) {
        throw new IllegalArgumentException("Package doesn't exist: " + packageName);
      }
    }
    return applicationEnabledSettingMap.get(packageName);
  }

  @Implementation
  protected ProviderInfo getProviderInfo(ComponentName component, int flags)
      throws NameNotFoundException {
    return getComponentInfo(
        component,
        flags,
        packageInfo -> packageInfo.providers,
        resolveInfo -> resolveInfo.providerInfo,
        ProviderInfo::new);
  }

  @Implementation
  protected void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
    componentList.put(componentName, new ComponentState(newState, flags));
  }

  @Implementation
  protected void setApplicationEnabledSetting(String packageName, int newState, int flags) {
    applicationEnabledSettingMap.put(packageName, newState);
  }

  @Implementation
  protected ResolveInfo resolveActivity(Intent intent, int flags) {
    List<ResolveInfo> candidates = queryIntentActivities(intent, flags);
    if (candidates.isEmpty()) {
      return null;
    }
    if (candidates.size() == 1) {
      return candidates.get(0);
    }
    ResolveInfo persistentPreferredResolveInfo =
        resolvePreferredActivity(intent, candidates, persistentPreferredActivities);
    if (persistentPreferredResolveInfo != null) {
      return persistentPreferredResolveInfo;
    }
    ResolveInfo preferredResolveInfo =
        resolvePreferredActivity(intent, candidates, preferredActivities);
    if (preferredResolveInfo != null) {
      return preferredResolveInfo;
    }
    if (!shouldShowActivityChooser) {
      return candidates.get(0);
    }
    ResolveInfo c1 = candidates.get(0);
    ResolveInfo c2 = candidates.get(1);
    if (c1.preferredOrder == c2.preferredOrder
        && isValidComponentInfo(c1.activityInfo)
        && isValidComponentInfo(c2.activityInfo)) {
      // When the top pick is as good as the second and is not preferred explicitly show the
      // chooser
      ResolveInfo result = new ResolveInfo();
      result.activityInfo = new ActivityInfo();
      result.activityInfo.name = "ActivityResolver";
      result.activityInfo.packageName = "android";
      result.activityInfo.applicationInfo = new ApplicationInfo();
      result.activityInfo.applicationInfo.flags = FLAG_INSTALLED | FLAG_SYSTEM;
      result.activityInfo.applicationInfo.packageName = "android";
      return result;
    } else {
      return c1;
    }
  }

  private ResolveInfo resolvePreferredActivity(
      Intent intent,
      List<ResolveInfo> candidates,
      SortedMap<ComponentName, List<IntentFilter>> preferredActivities) {
    preferredActivities = mapForPackage(preferredActivities, intent.getPackage());
    for (ResolveInfo candidate : candidates) {
      ActivityInfo activityInfo = candidate.activityInfo;
      if (!isValidComponentInfo(activityInfo)) {
        continue;
      }
      ComponentName candidateName =
          new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
      List<IntentFilter> intentFilters = preferredActivities.get(candidateName);
      if (intentFilters == null) {
        continue;
      }
      for (IntentFilter filter : intentFilters) {
        if ((filter.match(getContext().getContentResolver(), intent, false, "robo")
                & MATCH_CATEGORY_MASK)
            != 0) {
          return candidate;
        }
      }
    }
    return null;
  }

  @Implementation
  protected ProviderInfo resolveContentProvider(String name, int flags) {
    if (name == null) {
      return null;
    }

    synchronized (packageInfos) {
      for (PackageInfo packageInfo : packageInfos.values()) {
        if (packageInfo.providers == null) {
          continue;
        }

        for (ProviderInfo providerInfo : packageInfo.providers) {
          if (name.equals(providerInfo.authority)) { // todo: support multiple authorities
            return new ProviderInfo(providerInfo);
          }
        }
      }
    }
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected ProviderInfo resolveContentProviderAsUser(
      String name, int flags, @UserIdInt int userId) {
    return null;
  }

  @Implementation
  protected synchronized PackageInfo getPackageInfo(String packageName, int flags)
      throws NameNotFoundException {
    PackageInfo info = getInternalMutablePackageInfo(packageName);
    if (info == null
        && (flags & MATCH_UNINSTALLED_PACKAGES) != 0
        && deletedPackages.contains(packageName)) {
      info = new PackageInfo();
      info.packageName = packageName;
      info.applicationInfo = new ApplicationInfo();
      info.applicationInfo.packageName = packageName;
    }
    if (info == null) {
      throw new NameNotFoundException(packageName);
    }
    info = newPackageInfo(info);
    if (info.applicationInfo == null) {
      return info;
    }
    if (hiddenPackages.contains(packageName) && !isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)) {
      throw new NameNotFoundException("Package is hidden, can't find");
    }
    applyFlagsToApplicationInfo(info.applicationInfo, flags);
    info.activities =
        applyFlagsToComponentInfoList(info.activities, flags, GET_ACTIVITIES, ActivityInfo::new);
    info.services =
        applyFlagsToComponentInfoList(info.services, flags, GET_SERVICES, ServiceInfo::new);
    info.receivers =
        applyFlagsToComponentInfoList(info.receivers, flags, GET_RECEIVERS, ActivityInfo::new);
    info.providers =
        applyFlagsToComponentInfoList(info.providers, flags, GET_PROVIDERS, ProviderInfo::new);
    return info;
  }

  // There is no copy constructor for PackageInfo
  private static PackageInfo newPackageInfo(PackageInfo orig) {
    Parcel parcel = Parcel.obtain();
    orig.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    return PackageInfo.CREATOR.createFromParcel(parcel);
  }

  private <T extends ComponentInfo> T[] applyFlagsToComponentInfoList(
      T[] components, int flags, int activationFlag, Function<T, T> copyConstructor) {
    if (components == null || (flags & activationFlag) == 0) {
      return null;
    }
    List<T> returned = new ArrayList<>(components.length);
    for (T component : components) {
      component = copyConstructor.apply(component);
      try {
        applyFlagsToComponentInfo(component, flags);
        returned.add(component);
      } catch (NameNotFoundException e) {
        // skip this component
      }
    }
    if (returned.isEmpty()) {
      return null;
    }
    @SuppressWarnings("unchecked") // component arrays are of their respective types.
    Class<T[]> componentArrayType = (Class<T[]>) components.getClass();
    T[] result = Arrays.copyOf(components, returned.size(), componentArrayType);
    return returned.toArray(result);
  }

  @Implementation
  protected List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    return queryIntentComponents(
        intent,
        flags,
        (pkg) -> pkg.services,
        serviceFilters,
        (resolveInfo, serviceInfo) -> resolveInfo.serviceInfo = serviceInfo,
        (resolveInfo) -> resolveInfo.serviceInfo,
        ServiceInfo::new);
  }

  private boolean hasSomeComponentInfo(ResolveInfo resolveInfo) {

    return resolveInfo.activityInfo != null
        || resolveInfo.serviceInfo != null
        || (VERSION.SDK_INT >= VERSION_CODES.KITKAT && resolveInfo.providerInfo != null);
  }

  private static boolean isFlagSet(int flags, int matchFlag) {
    return (flags & matchFlag) == matchFlag;
  }

  private static boolean isValidComponentInfo(ComponentInfo componentInfo) {
    return componentInfo != null
        && componentInfo.applicationInfo != null
        && componentInfo.applicationInfo.packageName != null
        && componentInfo.name != null;
  }

  /** Behaves as {@link #queryIntentServices(Intent, int)} and currently ignores userId. */
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected List<ResolveInfo> queryIntentServicesAsUser(Intent intent, int flags, int userId) {
    return queryIntentServices(intent, flags);
  }

  @Implementation
  protected List<ResolveInfo> queryIntentActivities(Intent intent, int flags) {
    return this.queryIntentComponents(
        intent,
        flags,
        (pkg) -> pkg.activities,
        activityFilters,
        (resolveInfo, activityInfo) -> resolveInfo.activityInfo = activityInfo,
        (resolveInfo) -> resolveInfo.activityInfo,
        ActivityInfo::new);
  }

  private <I extends ComponentInfo> List<ResolveInfo> queryIntentComponents(
      Intent intent,
      int flags,
      Function<PackageInfo, I[]> componentsInPackage,
      SortedMap<ComponentName, List<IntentFilter>> filters,
      BiConsumer<ResolveInfo, I> componentSetter,
      Function<ResolveInfo, I> componentInResolveInfo,
      Function<I, I> copyConstructor) {
    if (intent.getComponent() != null) {
      flags &= ~MATCH_DEFAULT_ONLY;
    }
    List<ResolveInfo> result = new ArrayList<>();
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      result.addAll(resolveInfoList);
    }

    result.addAll(queryComponentsInManifest(intent, componentsInPackage, filters, componentSetter));

    for (Iterator<ResolveInfo> iterator = result.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      I componentInfo = componentInResolveInfo.apply(resolveInfo);
      if (hasSomeComponentInfo(resolveInfo) && componentInfo == null) {
        Log.d(TAG, "ResolveInfo for different component type");
        // different component type
        iterator.remove();
        continue;
      }
      if (componentInfo == null) {
        // null component? Don't filter this sh...
        continue;
      }
      if (!applyFlagsToResolveInfo(resolveInfo, flags)) {
        Log.d(TAG, "ResolveInfo doesn't match flags");
        iterator.remove();
        continue;
      }
      ApplicationInfo applicationInfo = componentInfo.applicationInfo;
      if (applicationInfo == null) {
        String packageName = null;
        if (getComponentForIntent(intent) != null) {
          packageName = getComponentForIntent(intent).getPackageName();
        } else if (intent.getPackage() != null) {
          packageName = intent.getPackage();
        } else if (componentInfo.packageName != null) {
          packageName = componentInfo.packageName;
        }
        if (packageName != null) {
          PackageInfo packageInfo = getInternalMutablePackageInfo(packageName);
          if (packageInfo != null && packageInfo.applicationInfo != null) {
            applicationInfo = new ApplicationInfo(packageInfo.applicationInfo);
          } else {
            applicationInfo = new ApplicationInfo();
            applicationInfo.packageName = packageName;
            applicationInfo.flags = FLAG_INSTALLED;
          }
        }
      } else {
        applicationInfo = new ApplicationInfo(applicationInfo);
      }
      componentInfo = copyConstructor.apply(componentInfo);
      componentSetter.accept(resolveInfo, componentInfo);
      componentInfo.applicationInfo = applicationInfo;

      try {
        applyFlagsToComponentInfo(componentInfo, flags);
      } catch (NameNotFoundException e) {
        Log.d(TAG, "ComponentInfo doesn't match flags:" + e.getMessage());
        iterator.remove();
        continue;
      }
    }
    Collections.sort(result, new ResolveInfoComparator());
    return result;
  }

  private boolean applyFlagsToResolveInfo(ResolveInfo resolveInfo, int flags) {
    if ((flags & GET_RESOLVED_FILTER) == 0) {
      resolveInfo.filter = null;
    }
    return (flags & MATCH_DEFAULT_ONLY) == 0 || resolveInfo.isDefault;
  }

  private <I extends ComponentInfo> List<ResolveInfo> queryComponentsInManifest(
      Intent intent,
      Function<PackageInfo, I[]> componentsInPackage,
      SortedMap<ComponentName, List<IntentFilter>> filters,
      BiConsumer<ResolveInfo, I> componentSetter) {
    if (isExplicitIntent(intent)) {
      ComponentName component = getComponentForIntent(intent);
      PackageInfo appPackage = getInternalMutablePackageInfo(component.getPackageName());
      if (appPackage == null) {
        return Collections.emptyList();
      }
      I componentInfo = findMatchingComponent(component, componentsInPackage.apply(appPackage));
      if (componentInfo != null) {
        ResolveInfo resolveInfo = buildResolveInfo(componentInfo);
        componentSetter.accept(resolveInfo, componentInfo);
        return new ArrayList<>(Collections.singletonList(resolveInfo));
      }

      return Collections.emptyList();
    } else {
      List<ResolveInfo> resolveInfoList = new ArrayList<>();
      Map<ComponentName, List<IntentFilter>> filtersForPackage =
          mapForPackage(filters, intent.getPackage());
      components:
      for (Map.Entry<ComponentName, List<IntentFilter>> componentEntry :
          filtersForPackage.entrySet()) {
        ComponentName componentName = componentEntry.getKey();
        for (IntentFilter filter : componentEntry.getValue()) {
          int match = matchIntentFilter(intent, filter);
          if (match > 0) {
            PackageInfo packageInfo = getInternalMutablePackageInfo(componentName.getPackageName());
            I[] componentInfoArray = componentsInPackage.apply(packageInfo);
            for (I componentInfo : componentInfoArray) {
              if (!componentInfo.name.equals(componentName.getClassName())) {
                continue;
              }
              ResolveInfo resolveInfo = buildResolveInfo(componentInfo, filter);
              resolveInfo.match = match;
              componentSetter.accept(resolveInfo, componentInfo);
              resolveInfoList.add(resolveInfo);
              continue components;
            }
          }
        }
      }
      return resolveInfoList;
    }
  }

  /** Behaves as {@link #queryIntentActivities(Intent, int)} and currently ignores userId. */
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
    return queryIntentActivities(intent, flags);
  }

  /** Returns true if intent has specified a specific component. */
  private static boolean isExplicitIntent(Intent intent) {
    return getComponentForIntent(intent) != null;
  }

  private static <T extends ComponentInfo> T findMatchingComponent(
      ComponentName componentName, T[] components) {
    if (components == null) {
      return null;
    }
    for (T component : components) {
      if (componentName.equals(new ComponentName(component.packageName, component.name))) {
        return component;
      }
    }
    return null;
  }

  private static ComponentName getComponentForIntent(Intent intent) {
    ComponentName component = intent.getComponent();
    if (component == null) {
      if (intent.getSelector() != null) {
        intent = intent.getSelector();
        component = intent.getComponent();
      }
    }
    return component;
  }

  private static ResolveInfo buildResolveInfo(ComponentInfo componentInfo) {
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.resolvePackageName = componentInfo.applicationInfo.packageName;
    resolveInfo.labelRes = componentInfo.labelRes;
    resolveInfo.icon = componentInfo.icon;
    resolveInfo.nonLocalizedLabel = componentInfo.nonLocalizedLabel;
    return resolveInfo;
  }

  static ResolveInfo buildResolveInfo(ComponentInfo componentInfo, IntentFilter intentFilter) {
    ResolveInfo info = buildResolveInfo(componentInfo);
    info.isDefault = intentFilter.hasCategory("android.intent.category.DEFAULT");
    info.filter = new IntentFilter(intentFilter);
    info.priority = intentFilter.getPriority();
    return info;
  }

  @Implementation
  protected int checkPermission(String permName, String pkgName) {
    PackageInfo permissionsInfo = getInternalMutablePackageInfo(pkgName);
    if (permissionsInfo == null || permissionsInfo.requestedPermissions == null) {
      return PackageManager.PERMISSION_DENIED;
    }

    String permission;
    for (int i = 0; i < permissionsInfo.requestedPermissions.length; i++) {
      permission = permissionsInfo.requestedPermissions[i];
      if (permission != null && permission.equals(permName)) {
        // The package requests this permission. Now check if it's been granted to the package.
        if (isGrantedForBackwardsCompatibility(pkgName, permissionsInfo)) {
          return PackageManager.PERMISSION_GRANTED;
        }

        if ((permissionsInfo.requestedPermissionsFlags[i] & REQUESTED_PERMISSION_GRANTED)
            == REQUESTED_PERMISSION_GRANTED) {
          return PackageManager.PERMISSION_GRANTED;
        }
      }
    }

    return PackageManager.PERMISSION_DENIED;
  }

  /**
   * Returns whether a permission should be treated as granted to the package for backward
   * compatibility reasons.
   *
   * <p>Before Robolectric 4.0 the ShadowPackageManager treated every requested permission as
   * automatically granted. 4.0 changes this behavior, and only treats a permission as granted if
   * PackageInfo.requestedPermissionFlags[permissionIndex] & REQUESTED_PERMISSION_GRANTED ==
   * REQUESTED_PERMISSION_GRANTED which matches the real PackageManager's behavior.
   *
   * <p>Since many existing tests didn't set the requestedPermissionFlags on their {@code
   * PackageInfo} objects, but assumed that all permissions are granted, we auto-grant all
   * permissions if the requestedPermissionFlags is not set. If the requestedPermissionFlags is set,
   * we assume that the test is configuring the permission grant state, and we don't override this
   * setting.
   */
  private boolean isGrantedForBackwardsCompatibility(String pkgName, PackageInfo permissionsInfo) {
    // Note: it might be cleaner to auto-grant these permissions when the package is added to the
    // PackageManager. But many existing tests modify the requested permissions _after_ adding the
    // package to the PackageManager, without updating the requestedPermissionsFlags.
    return permissionsInfo.requestedPermissionsFlags == null
        // Robolectric uses the PackageParser to create the current test package's PackageInfo from
        // the manifest XML. The parser populates the requestedPermissionsFlags, but doesn't grant
        // the permissions. Several tests rely on the test package being granted all permissions, so
        // we treat this as a special case.
        || pkgName.equals(RuntimeEnvironment.application.getPackageName());
  }

  @Implementation
  protected ActivityInfo getReceiverInfo(ComponentName component, int flags)
      throws NameNotFoundException {
    return getComponentInfo(
        component,
        flags,
        packageInfo -> packageInfo.receivers,
        resolveInfo -> resolveInfo.activityInfo,
        ActivityInfo::new);
  }

  @Implementation
  protected List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    return this.queryIntentComponents(
        intent,
        flags,
        (pkg) -> pkg.receivers,
        receiverFilters,
        (resolveInfo, activityInfo) -> resolveInfo.activityInfo = activityInfo,
        (resolveInfo) -> resolveInfo.activityInfo,
        ActivityInfo::new);
  }

  private static int matchIntentFilter(Intent intent, IntentFilter intentFilter) {
    return intentFilter.match(
        intent.getAction(),
        intent.getType(),
        intent.getScheme(),
        intent.getData(),
        intent.getCategories(),
        TAG);
  }

  @Implementation
  protected ResolveInfo resolveService(Intent intent, int flags) {
    List<ResolveInfo> candidates = queryIntentServices(intent, flags);
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  @Implementation
  protected ServiceInfo getServiceInfo(ComponentName component, int flags)
      throws NameNotFoundException {
    return getComponentInfo(
        component,
        flags,
        packageInfo -> packageInfo.services,
        resolveInfo -> resolveInfo.serviceInfo,
        ServiceInfo::new);
  }

  /**
   * Modifies the component in place using.
   *
   * @throws NameNotFoundException when component is filtered out by a flag
   */
  private void applyFlagsToComponentInfo(ComponentInfo componentInfo, int flags)
      throws NameNotFoundException {
    componentInfo.name = (componentInfo.name == null) ? "" : componentInfo.name;
    ApplicationInfo applicationInfo = componentInfo.applicationInfo;
    boolean isApplicationEnabled = true;
    if (applicationInfo != null) {
      if (applicationInfo.packageName == null) {
        applicationInfo.packageName = componentInfo.packageName;
      }
      applyFlagsToApplicationInfo(componentInfo.applicationInfo, flags);
      componentInfo.packageName = applicationInfo.packageName;
      isApplicationEnabled = applicationInfo.enabled;
    }
    if ((flags & GET_META_DATA) == 0) {
      componentInfo.metaData = null;
    }
    boolean isComponentEnabled = isComponentEnabled(componentInfo);
    if ((flags & MATCH_ALL) != 0 && Build.VERSION.SDK_INT >= 23) {
      return;
    }
    // Android don't override the enabled field of component with the actual value.
    boolean isEnabledForFiltering =
        isComponentEnabled && (Build.VERSION.SDK_INT >= 24 ? isApplicationEnabled : true);
    if ((flags & MATCH_DISABLED_COMPONENTS) == 0 && !isEnabledForFiltering) {
      throw new NameNotFoundException("Disabled component: " + componentInfo);
    }
    if (isFlagSet(flags, PackageManager.MATCH_SYSTEM_ONLY)) {
      if (applicationInfo == null) {
        // TODO: for backwards compatibility just skip filtering. In future should just remove
        // invalid resolve infos from list
      } else {
        final int applicationFlags = applicationInfo.flags;
        if ((applicationFlags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
          throw new NameNotFoundException("Not system component: " + componentInfo);
        }
      }
    }
    if (!isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)
        && isValidComponentInfo(componentInfo)
        && hiddenPackages.contains(componentInfo.applicationInfo.packageName)) {
      throw new NameNotFoundException("Uninstalled package: " + componentInfo);
    }
  }

  @Implementation
  protected Resources getResourcesForApplication(@NonNull ApplicationInfo applicationInfo)
      throws PackageManager.NameNotFoundException {
    if (getContext().getPackageName().equals(applicationInfo.packageName)) {
      return getContext().getResources();
    }

    synchronized (packageInfos) {
      if (packageInfos.containsKey(applicationInfo.packageName)) {
        Resources appResources = resources.get(applicationInfo.packageName);
        if (appResources == null) {
          appResources = new Resources(new AssetManager(), null, null);
          resources.put(applicationInfo.packageName, appResources);
        }
        return appResources;
      }
    }

    Resources resources = null;

    if (RuntimeEnvironment.useLegacyResources()
        && (applicationInfo.publicSourceDir == null
            || !new File(applicationInfo.publicSourceDir).exists())) {
      // In legacy mode, the underlying getResourcesForApplication implementation just returns an
      // empty Resources instance in this case.
      throw new NameNotFoundException(applicationInfo.packageName);
    }

    try {
      resources =
          Shadow.directlyOn(realObject, ApplicationPackageManager.class)
              .getResourcesForApplication(applicationInfo);
    } catch (Exception ex) {
      // handled below
    }
    if (resources == null) {
      throw new NameNotFoundException(applicationInfo.packageName);
    }
    return resources;
  }

  @Implementation
  protected List<ApplicationInfo> getInstalledApplications(int flags) {
    List<PackageInfo> packageInfos = getInstalledPackages(flags);
    List<ApplicationInfo> result = new ArrayList<>(packageInfos.size());

    for (PackageInfo packageInfo : packageInfos) {
      if (packageInfo.applicationInfo != null) {
        result.add(packageInfo.applicationInfo);
      }
    }
    return result;
  }

  @Implementation
  protected String getInstallerPackageName(String packageName) {
    if (ConfigurationRegistry.get(GetInstallerPackageNameMode.Mode.class) == REALISTIC
        && !packageInstallerMap.containsKey(packageName)) {
      throw new IllegalArgumentException("Package is not installed: " + packageName);
    } else if (!packageInstallerMap.containsKey(packageName)) {
      Log.w(
          TAG,
          String.format(
              "Call to getInstallerPackageName returns null for package: '%s'. Please run"
                  + " setInstallerPackageName to set installer package name before making the"
                  + " call.",
              packageName));
    }

    return packageInstallerMap.get(packageName);
  }

  @Implementation
  protected PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
    PermissionInfo permissionInfo = extraPermissions.get(name);
    if (permissionInfo != null) {
      return permissionInfo;
    }

    synchronized (packageInfos) {
      for (PackageInfo packageInfo : packageInfos.values()) {
        if (packageInfo.permissions != null) {
          for (PermissionInfo permission : packageInfo.permissions) {
            if (name.equals(permission.name)) {
              return createCopyPermissionInfo(permission, flags);
            }
          }
        }
      }
    }

    throw new NameNotFoundException(name);
  }

  @Implementation(minSdk = M)
  protected boolean shouldShowRequestPermissionRationale(String permission) {
    return permissionRationaleMap.containsKey(permission)
        ? permissionRationaleMap.get(permission)
        : false;
  }

  @Implementation
  protected FeatureInfo[] getSystemAvailableFeatures() {
    return systemAvailableFeatures.isEmpty()
        ? null
        : systemAvailableFeatures.toArray(new FeatureInfo[systemAvailableFeatures.size()]);
  }

  @Implementation
  protected void verifyPendingInstall(int id, int verificationCode) {
    if (verificationResults.containsKey(id)) {
      throw new IllegalStateException("Multiple verifications for id=" + id);
    }
    verificationResults.put(id, verificationCode);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void extendVerificationTimeout(
      int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
    verificationTimeoutExtension.put(id, millisecondsToDelay);
  }

  @Override
  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {}

  @Implementation(minSdk = M)
  protected void freeStorageAndNotify(
      String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {}

  @Implementation
  protected void setInstallerPackageName(String targetPackage, String installerPackageName) {
    packageInstallerMap.put(targetPackage, installerPackageName);
  }

  @Implementation(minSdk = KITKAT)
  protected List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
    return this.queryIntentComponents(
        intent,
        flags,
        (pkg) -> pkg.providers,
        providerFilters,
        (resolveInfo, providerInfo) -> resolveInfo.providerInfo = providerInfo,
        (resolveInfo) -> resolveInfo.providerInfo,
        ProviderInfo::new);
  }

  @Implementation(minSdk = KITKAT)
  protected List<ResolveInfo> queryIntentContentProvidersAsUser(
      Intent intent, int flags, int userId) {
    return Collections.emptyList();
  }

  @Implementation(minSdk = M)
  protected String getPermissionControllerPackageName() {
    return null;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  protected void getPackageSizeInfo(Object pkgName, Object observer) {
    final PackageStats packageStats = packageStatsMap.get((String) pkgName);
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              try {
                ((IPackageStatsObserver) observer)
                    .onGetStatsCompleted(packageStats, packageStats != null);
              } catch (RemoteException remoteException) {
                remoteException.rethrowFromSystemServer();
              }
            });
  }

  @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = M)
  protected void getPackageSizeInfo(Object pkgName, Object uid, final Object observer) {
    final PackageStats packageStats = packageStatsMap.get((String) pkgName);
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              try {
                ((IPackageStatsObserver) observer)
                    .onGetStatsCompleted(packageStats, packageStats != null);
              } catch (RemoteException remoteException) {
                remoteException.rethrowFromSystemServer();
              }
            });
  }

  @Implementation(minSdk = N)
  protected void getPackageSizeInfoAsUser(Object pkgName, Object uid, final Object observer) {
    final PackageStats packageStats = packageStatsMap.get((String) pkgName);
    new Handler(Looper.getMainLooper())
        .post(
            () -> {
              try {
                ((IPackageStatsObserver) observer)
                    .onGetStatsCompleted(packageStats, packageStats != null);
              } catch (RemoteException remoteException) {
                remoteException.rethrowFromSystemServer();
              }
            });
  }

  @Override
  @Implementation
  protected void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
    super.deletePackage(packageName, observer, flags);
  }

  @Implementation
  protected String[] currentToCanonicalPackageNames(String[] names) {
    String[] out = new String[names.length];
    for (int i = 0; i < names.length; i++) {
      out[i] = currentToCanonicalNames.getOrDefault(names[i], names[i]);
    }
    return out;
  }

  @Implementation
  protected String[] canonicalToCurrentPackageNames(String[] names) {
    String[] out = new String[names.length];
    for (int i = 0; i < names.length; i++) {
      out[i] = canonicalToCurrentNames.getOrDefault(names[i], names[i]);
    }
    return out;
  }

  @Implementation
  protected boolean isSafeMode() {
    return safeMode;
  }

  @Implementation
  protected Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    return applicationIcons.get(packageName);
  }

  @Implementation
  protected Drawable getApplicationIcon(ApplicationInfo info) throws NameNotFoundException {
    return getApplicationIcon(info.packageName);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected Drawable getUserBadgeForDensity(UserHandle userHandle, int i) {
    return null;
  }

  @Implementation
  protected int checkSignatures(String pkg1, String pkg2) {
    try {
      PackageInfo packageInfo1 = getPackageInfo(pkg1, GET_SIGNATURES);
      PackageInfo packageInfo2 = getPackageInfo(pkg2, GET_SIGNATURES);
      return compareSignature(packageInfo1.signatures, packageInfo2.signatures);
    } catch (NameNotFoundException e) {
      return SIGNATURE_UNKNOWN_PACKAGE;
    }
  }

  @Implementation
  protected int checkSignatures(int uid1, int uid2) {
    return 0;
  }

  @Implementation
  protected List<PermissionInfo> queryPermissionsByGroup(String group, int flags)
      throws NameNotFoundException {
    List<PermissionInfo> result = new ArrayList<>();
    for (PermissionInfo permissionInfo : extraPermissions.values()) {
      if (Objects.equals(permissionInfo.group, group)) {
        result.add(permissionInfo);
      }
    }

    synchronized (packageInfos) {
      for (PackageInfo packageInfo : packageInfos.values()) {
        if (packageInfo.permissions != null) {
          for (PermissionInfo permission : packageInfo.permissions) {
            if (Objects.equals(group, permission.group)) {
              result.add(createCopyPermissionInfo(permission, flags));
            }
          }
        }
      }
    }

    if (result.isEmpty()) {
      throw new NameNotFoundException(group);
    }

    return result;
  }

  private static PermissionInfo createCopyPermissionInfo(PermissionInfo src, int flags) {
    PermissionInfo matchedPermission = new PermissionInfo(src);
    if ((flags & GET_META_DATA) != GET_META_DATA) {
      matchedPermission.metaData = null;
    }
    return matchedPermission;
  }

  private Intent getLaunchIntentForPackage(String packageName, String launcherCategory) {
    Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
    intentToResolve.addCategory(Intent.CATEGORY_INFO);
    intentToResolve.setPackage(packageName);
    List<ResolveInfo> ris = queryIntentActivities(intentToResolve, 0);

    if (ris == null || ris.isEmpty()) {
      intentToResolve.removeCategory(Intent.CATEGORY_INFO);
      intentToResolve.addCategory(launcherCategory);
      intentToResolve.setPackage(packageName);
      ris = queryIntentActivities(intentToResolve, 0);
    }
    if (ris == null || ris.isEmpty()) {
      return null;
    }
    Intent intent = new Intent(intentToResolve);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.setClassName(packageName, ris.get(0).activityInfo.name);
    return intent;
  }

  @Implementation
  protected Intent getLaunchIntentForPackage(String packageName) {
    return getLaunchIntentForPackage(packageName, Intent.CATEGORY_LAUNCHER);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected Intent getLeanbackLaunchIntentForPackage(String packageName) {
    return getLaunchIntentForPackage(packageName, Intent.CATEGORY_LEANBACK_LAUNCHER);
  }

  ////////////////////////////

  @Implementation(minSdk = N)
  protected PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected int[] getPackageGids(String packageName) throws NameNotFoundException {
    return new int[0];
  }

  @Implementation(minSdk = N)
  protected int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
    return null;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected int getPackageUid(String packageName, int flags) throws NameNotFoundException {
    Integer uid = uidForPackage.get(packageName);
    if (uid == null) {
      throw new NameNotFoundException(packageName);
    }
    return uid;
  }

  @Implementation(minSdk = N)
  protected int getPackageUidAsUser(String packageName, int userId) throws NameNotFoundException {
    return 0;
  }

  @Implementation(minSdk = N)
  protected int getPackageUidAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return 0;
  }

  /** @see ShadowPackageManager#addPermissionGroupInfo(android.content.pm.PermissionGroupInfo) */
  @Implementation
  protected PermissionGroupInfo getPermissionGroupInfo(String name, int flags)
      throws NameNotFoundException {
    if (permissionGroups.containsKey(name)) {
      return new PermissionGroupInfo(permissionGroups.get(name));
    }

    throw new NameNotFoundException(name);
  }

  /** @see ShadowPackageManager#addPermissionGroupInfo(android.content.pm.PermissionGroupInfo) */
  @Implementation
  protected List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
    ArrayList<PermissionGroupInfo> allPermissionGroups = new ArrayList<PermissionGroupInfo>();

    for (PermissionGroupInfo permissionGroupInfo : permissionGroups.values()) {
      allPermissionGroups.add(new PermissionGroupInfo(permissionGroupInfo));
    }

    return allPermissionGroups;
  }

  @Implementation
  protected ApplicationInfo getApplicationInfo(String packageName, int flags)
      throws NameNotFoundException {
    PackageInfo packageInfo = getPackageInfo(packageName, flags);
    if (packageInfo.applicationInfo == null) {
      throw new NameNotFoundException("Package found but without application info");
    }
    // Maybe query app infos from overridden resolveInfo as well?
    return packageInfo.applicationInfo;
  }

  private void applyFlagsToApplicationInfo(@Nullable ApplicationInfo appInfo, int flags)
      throws NameNotFoundException {
    if (appInfo == null) {
      return;
    }
    String packageName = appInfo.packageName;

    Integer stateOverride = applicationEnabledSettingMap.get(packageName);
    if (stateOverride == null) {
      stateOverride = COMPONENT_ENABLED_STATE_DEFAULT;
    }
    appInfo.enabled =
        (appInfo.enabled && stateOverride == COMPONENT_ENABLED_STATE_DEFAULT)
            || stateOverride == COMPONENT_ENABLED_STATE_ENABLED;

    if (deletedPackages.contains(packageName)) {
      appInfo.flags &= ~FLAG_INSTALLED;
    }

    if ((flags & MATCH_ALL) != 0 && Build.VERSION.SDK_INT >= 23) {
      return;
    }
    if ((flags & MATCH_UNINSTALLED_PACKAGES) == 0 && (appInfo.flags & FLAG_INSTALLED) == 0) {
      throw new NameNotFoundException("Package not installed: " + packageName);
    }
    if ((flags & MATCH_UNINSTALLED_PACKAGES) == 0 && hiddenPackages.contains(packageName)) {
      throw new NameNotFoundException("Package hidden: " + packageName);
    }
  }

  /**
   * Returns all the values added via {@link
   * ShadowPackageManager#addSystemSharedLibraryName(String)}.
   */
  @Implementation
  protected String[] getSystemSharedLibraryNames() {
    return systemSharedLibraryNames.toArray(new String[systemSharedLibraryNames.size()]);
  }

  @Implementation(minSdk = N)
  protected @NonNull String getServicesSystemSharedLibraryPackageName() {
    return null;
  }

  @Implementation(minSdk = N)
  protected @NonNull String getSharedSystemSharedLibraryPackageName() {
    return "";
  }

  @Implementation(minSdk = N)
  protected boolean hasSystemFeature(String name, int version) {
    return false;
  }

  @Implementation(minSdk = M)
  protected boolean isPermissionRevokedByPolicy(String permName, String pkgName) {
    return false;
  }

  @Implementation
  protected boolean addPermission(PermissionInfo info) {
    return false;
  }

  @Implementation
  protected boolean addPermissionAsync(PermissionInfo info) {
    return false;
  }

  @Implementation
  protected void removePermission(String name) {}

  @Implementation(minSdk = M)
  protected void grantRuntimePermission(
      String packageName, String permissionName, UserHandle user) {

    PackageInfo packageInfo;
    synchronized (packageInfos) {
      if (!packageInfos.containsKey(packageName)) {
        throw new SecurityException("Package not found: " + packageName);
      }
      packageInfo = packageInfos.get(packageName);
    }

    checkPermissionGrantStateInitialized(packageInfo);

    int permissionIndex = getPermissionIndex(packageInfo, permissionName);
    if (permissionIndex < 0) {
      throw new SecurityException(
          "Permission " + permissionName + " not requested by package " + packageName);
    }

    packageInfo.requestedPermissionsFlags[permissionIndex] |= REQUESTED_PERMISSION_GRANTED;
  }

  @Implementation(minSdk = M)
  protected void revokeRuntimePermission(
      String packageName, String permissionName, UserHandle user) {

    PackageInfo packageInfo;
    synchronized (packageInfos) {
      if (!packageInfos.containsKey(packageName)) {
        throw new SecurityException("Package not found: " + packageName);
      }
      packageInfo = packageInfos.get(packageName);
    }

    checkPermissionGrantStateInitialized(packageInfo);

    int permissionIndex = getPermissionIndex(packageInfo, permissionName);
    if (permissionIndex < 0) {
      throw new SecurityException(
          "Permission " + permissionName + " not requested by package " + packageName);
    }

    packageInfo.requestedPermissionsFlags[permissionIndex] &= ~REQUESTED_PERMISSION_GRANTED;
  }

  private void checkPermissionGrantStateInitialized(PackageInfo packageInfo) {
    if (packageInfo.requestedPermissionsFlags == null) {
      // In the real OS this would never be null, but tests don't necessarily initialize this
      // structure.
      throw new SecurityException(
          "Permission grant state (PackageInfo.requestedPermissionFlags) "
              + "is null. This operation requires this variable to be initialized.");
    }
  }

  /**
   * Returns the index of the given permission in the PackageInfo.requestedPermissions array, or -1
   * if it's not found.
   */
  private int getPermissionIndex(PackageInfo packageInfo, String permissionName) {
    if (packageInfo.requestedPermissions != null) {
      for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
        if (permissionName.equals(packageInfo.requestedPermissions[i])) {
          return i;
        }
      }
    }

    return -1;
  }

  @Implementation(minSdk = M)
  protected int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
    return 0;
  }

  @Implementation(minSdk = M)
  protected void updatePermissionFlags(
      String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {}

  @Implementation
  protected int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
    return 0;
  }

  @Implementation(minSdk = N)
  protected List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
    return null;
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
    List<PackageInfo> packageInfosWithPermissions = new ArrayList<>();
    synchronized (packageInfos) {
      for (PackageInfo packageInfo : packageInfos.values()) {
        for (String permission : permissions) {
          int permissionIndex = getPermissionIndex(packageInfo, permission);
          if (permissionIndex >= 0) {
            packageInfosWithPermissions.add(packageInfo);
            break;
          }
        }
      }
    }
    return packageInfosWithPermissions;
  }

  /** Behaves as {@link #resolveActivity(Intent, int)} and currently ignores userId. */
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
    return resolveActivity(intent, flags);
  }

  @Implementation
  protected List<ResolveInfo> queryIntentActivityOptions(
      ComponentName caller, Intent[] specifics, Intent intent, int flags) {
    return null;
  }

  @Implementation(minSdk = N)
  protected List<ResolveInfo> queryBroadcastReceiversAsUser(Intent intent, int flags, int userId) {
    return null;
  }

  @Implementation
  protected List<ProviderInfo> queryContentProviders(String processName, int uid, int flags) {
    return null;
  }

  @Implementation
  protected InstrumentationInfo getInstrumentationInfo(ComponentName className, int flags)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected List<InstrumentationInfo> queryInstrumentation(String targetPackage, int flags) {
    return null;
  }

  @Nullable
  @Implementation
  protected Drawable getDrawable(
      String packageName, @DrawableRes int resId, @Nullable ApplicationInfo appInfo) {
    Drawable result = drawables.get(new Pair<>(packageName, resId));
    if (result != null) {
      return result;
    }
    return Shadow.directlyOn(realObject, ApplicationPackageManager.class)
        .getDrawable(packageName, resId, appInfo);
  }

  @Implementation
  protected Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
    Drawable result = drawableList.get(activityName);
    if (result != null) {
      return result;
    }
    return Shadow.directlyOn(realObject, ApplicationPackageManager.class)
        .getActivityIcon(activityName);
  }

  @Implementation
  protected Drawable getDefaultActivityIcon() {
    return Resources.getSystem().getDrawable(com.android.internal.R.drawable.sym_def_app_icon);
  }

  @Implementation
  protected Resources getResourcesForActivity(ComponentName activityName)
      throws NameNotFoundException {
    return getResourcesForApplication(activityName.getPackageName());
  }

  @Implementation
  protected Resources getResourcesForApplication(String appPackageName)
      throws NameNotFoundException {
    if (getContext().getPackageName().equals(appPackageName)) {
      return getContext().getResources();
    }

    synchronized (packageInfos) {
      if (packageInfos.containsKey(appPackageName)) {
        Resources appResources = resources.get(appPackageName);
        if (appResources == null) {
          appResources = new Resources(new AssetManager(), null, null);
          resources.put(appPackageName, appResources);
        }
        return appResources;
      }
    }

    throw new NameNotFoundException(appPackageName);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected Resources getResourcesForApplicationAsUser(String appPackageName, int userId)
      throws NameNotFoundException {
    return null;
  }

  @Implementation(minSdk = M)
  protected void addOnPermissionsChangeListener(Object listener) {}

  @Implementation(minSdk = M)
  protected void removeOnPermissionsChangeListener(Object listener) {}

  @Implementation(maxSdk = O_MR1)
  protected void installPackage(
      Object packageURI, Object observer, Object flags, Object installerPackageName) {}

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected int installExistingPackage(String packageName) throws NameNotFoundException {
    return 0;
  }

  @Implementation(minSdk = N)
  protected int installExistingPackageAsUser(String packageName, int userId)
      throws NameNotFoundException {
    return 0;
  }

  @Implementation(minSdk = M)
  protected void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {}

  @Implementation(minSdk = N)
  protected int getIntentVerificationStatusAsUser(String packageName, int userId) {
    return 0;
  }

  @Implementation(minSdk = N)
  protected boolean updateIntentVerificationStatusAsUser(
      String packageName, int status, int userId) {
    return false;
  }

  @Implementation(minSdk = M)
  protected List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
    return null;
  }

  @Implementation(minSdk = M)
  protected List<IntentFilter> getAllIntentFilters(String packageName) {
    return null;
  }

  @Implementation(minSdk = N)
  protected String getDefaultBrowserPackageNameAsUser(int userId) {
    return null;
  }

  @Implementation(minSdk = N)
  protected boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
    return false;
  }

  @Implementation(minSdk = M)
  protected int getMoveStatus(int moveId) {
    return 0;
  }

  @Implementation(minSdk = M)
  protected void registerMoveCallback(Object callback, Object handler) {}

  @Implementation(minSdk = M)
  protected void unregisterMoveCallback(Object callback) {}

  @Implementation(minSdk = M)
  protected Object movePackage(Object packageName, Object vol) {
    return 0;
  }

  @Implementation(minSdk = M)
  protected Object getPackageCurrentVolume(Object app) {
    return null;
  }

  @Implementation(minSdk = M)
  protected List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
    return null;
  }

  @Implementation(minSdk = M)
  protected Object movePrimaryStorage(Object vol) {
    return 0;
  }

  @Implementation(minSdk = M)
  protected @Nullable Object getPrimaryStorageCurrentVolume() {
    return null;
  }

  @Implementation(minSdk = M)
  protected @NonNull List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
    return null;
  }

  @Implementation(minSdk = N)
  protected void deletePackageAsUser(
      String packageName, IPackageDeleteObserver observer, int flags, int userId) {}

  @Implementation
  protected void clearApplicationUserData(String packageName, IPackageDataObserver observer) {}

  @Implementation
  protected void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {}

  @Implementation(minSdk = N)
  protected void deleteApplicationCacheFilesAsUser(
      String packageName, int userId, IPackageDataObserver observer) {}

  @Implementation(minSdk = M)
  protected void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {}

  @Implementation(minSdk = N, maxSdk = O_MR1)
  protected String[] setPackagesSuspendedAsUser(
      String[] packageNames, boolean suspended, int userId) {
    return null;
  }

  @Implementation(minSdk = N)
  protected boolean isPackageSuspendedForUser(String packageName, int userId) {
    return false;
  }

  @Implementation
  protected void addPackageToPreferred(String packageName) {}

  @Implementation
  protected void removePackageFromPreferred(String packageName) {}

  @Implementation
  protected List<PackageInfo> getPreferredPackages(int flags) {
    return null;
  }

  @Implementation
  public void addPreferredActivity(
      IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    addPreferredActivityInternal(filter, activity, preferredActivities);
  }

  @Implementation
  protected void replacePreferredActivity(
      IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    addPreferredActivity(filter, match, set, activity);
  }

  @Implementation
  public int getPreferredActivities(
      List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    return getPreferredActivitiesInternal(
        outFilters, outActivities, packageName, preferredActivities);
  }

  @Implementation
  protected void clearPackagePreferredActivities(String packageName) {
    clearPackagePreferredActivitiesInternal(packageName, preferredActivities);
  }

  @Implementation(minSdk = KITKAT)
  protected ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
    return null;
  }

  @Implementation(minSdk = N)
  protected void flushPackageRestrictionsAsUser(int userId) {}

  @Implementation(minSdk = LOLLIPOP)
  protected boolean setApplicationHiddenSettingAsUser(
      String packageName, boolean hidden, UserHandle user) {
    // Note that this ignores the UserHandle parameter
    synchronized (packageInfos) {
      if (!packageInfos.containsKey(packageName)) {
        // Package doesn't exist
        return false;
      }
    }

    if (hidden) {
      hiddenPackages.add(packageName);
    } else {
      hiddenPackages.remove(packageName);
    }
    return true;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
    // Note that this ignores the UserHandle parameter
    synchronized (packageInfos) {
      if (!packageInfos.containsKey(packageName)) {
        // Match Android behaviour of returning true if package isn't found
        return true;
      }
    }
    return hiddenPackages.contains(packageName);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected Object getKeySetByAlias(String packageName, String alias) {
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected Object getSigningKeySet(String packageName) {
    return null;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean isSignedBy(String packageName, Object ks) {
    return false;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean isSignedByExactly(String packageName, Object ks) {
    return false;
  }

  @Implementation
  protected VerifierDeviceIdentity getVerifierDeviceIdentity() {
    return null;
  }

  @Implementation(minSdk = LOLLIPOP_MR1)
  protected boolean isUpgrade() {
    return false;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean isPackageAvailable(String packageName) {
    return false;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void addCrossProfileIntentFilter(
      IntentFilter filter, int sourceUserId, int targetUserId, int flags) {}

  @Implementation(minSdk = LOLLIPOP)
  protected void clearCrossProfileIntentFilters(int sourceUserId) {}

  /**
   * Gets the unbadged icon based on the values set by {@link
   * ShadowPackageManager#setUnbadgedApplicationIcon} or returns null if nothing has been set.
   */
  @Implementation(minSdk = LOLLIPOP_MR1)
  protected Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    Drawable result = unbadgedApplicationIcons.get(itemInfo.packageName);
    if (result != null) {
      return result;
    }
    return Shadow.directlyOn(realObject, ApplicationPackageManager.class)
        .loadUnbadgedItemIcon(itemInfo, appInfo);
  }

  /**
   * Adds a profile badge to the icon.
   *
   * <p>This implementation just returns the unbadged icon, as some default implementations add an
   * internal resource to the icon that is unavailable to Robolectric.
   */
  @Implementation(minSdk = LOLLIPOP)
  protected Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
    return icon;
  }

  @Implementation(minSdk = O)
  protected boolean canRequestPackageInstalls() {
    return canRequestPackageInstalls;
  }

  @Implementation(minSdk = O)
  protected Object getChangedPackages(int sequenceNumber) {
    if (sequenceNumber < 0 || sequenceNumberChangedPackagesMap.get(sequenceNumber).isEmpty()) {
      return null;
    }
    return new ChangedPackages(
        sequenceNumber + 1, new ArrayList<>(sequenceNumberChangedPackagesMap.get(sequenceNumber)));
  }

  @Implementation(minSdk = P)
  public String getSystemTextClassifierPackageName() {
    return "";
  }

  @Implementation(minSdk = P)
  @HiddenApi
  protected String[] setPackagesSuspended(
      String[] packageNames,
      boolean suspended,
      PersistableBundle appExtras,
      PersistableBundle launcherExtras,
      String dialogMessage) {
    return setPackagesSuspended(
        packageNames, suspended, appExtras, launcherExtras, dialogMessage, /* dialogInfo= */ null);
  }

  @Implementation(minSdk = Q)
  @HiddenApi
  protected /* String[] */ Object setPackagesSuspended(
      /* String[] */ Object packageNames,
      /* boolean */ Object suspended,
      /* PersistableBundle */ Object appExtras,
      /* PersistableBundle */ Object launcherExtras,
      /* SuspendDialogInfo */ Object dialogInfo) {
    return setPackagesSuspended(
        (String[]) packageNames,
        (boolean) suspended,
        (PersistableBundle) appExtras,
        (PersistableBundle) launcherExtras,
        /* dialogMessage= */ null,
        dialogInfo);
  }

  /**
   * Sets {@code packageNames} suspension status to {@code suspended} in the package manager.
   *
   * <p>At least one of {@code dialogMessage} and {@code dialogInfo} should be null.
   */
  private String[] setPackagesSuspended(
      String[] packageNames,
      boolean suspended,
      PersistableBundle appExtras,
      PersistableBundle launcherExtras,
      String dialogMessage,
      Object dialogInfo) {
    if (hasProfileOwnerOrDeviceOwnerOnCurrentUser()
        && (VERSION.SDK_INT < VERSION_CODES.Q
            || !isCurrentApplicationProfileOwnerOrDeviceOwner())) {
      throw new UnsupportedOperationException();
    }
    ArrayList<String> unupdatedPackages = new ArrayList<>();
    for (String packageName : packageNames) {
      if (!canSuspendPackage(packageName)) {
        unupdatedPackages.add(packageName);
        continue;
      }
      PackageSetting setting = packageSettings.get(packageName);
      if (setting == null) {
        unupdatedPackages.add(packageName);
        continue;
      }
      setting.setSuspended(suspended, dialogMessage, dialogInfo, appExtras, launcherExtras);
    }
    return unupdatedPackages.toArray(new String[0]);
  }

  /** Returns whether the current user profile has a profile owner or a device owner. */
  private boolean isCurrentApplicationProfileOwnerOrDeviceOwner() {
    String currentApplication = getContext().getPackageName();
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    return devicePolicyManager.isProfileOwnerApp(currentApplication)
        || devicePolicyManager.isDeviceOwnerApp(currentApplication);
  }

  /** Returns whether the current user profile has a profile owner or a device owner. */
  private boolean hasProfileOwnerOrDeviceOwnerOnCurrentUser() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    return devicePolicyManager.getProfileOwner() != null
        || (UserHandle.of(UserHandle.myUserId()).isSystem()
            && devicePolicyManager.getDeviceOwner() != null);
  }

  private boolean canSuspendPackage(String packageName) {
    // This code approximately mirrors PackageManagerService#canSuspendPackageForUserLocked.
    return !packageName.equals(getContext().getPackageName())
        && !isPackageDeviceAdmin(packageName)
        && !isPackageActiveLauncher(packageName)
        && !isPackageRequiredInstaller(packageName)
        && !isPackageRequiredUninstaller(packageName)
        && !isPackageRequiredVerifier(packageName)
        && !isPackageDefaultDialer(packageName)
        && !packageName.equals(PLATFORM_PACKAGE_NAME);
  }

  private boolean isPackageDeviceAdmin(String packageName) {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
    // Strictly speaking, this should be devicePolicyManager.getDeviceOwnerComponentOnAnyUser(),
    // but that method is currently not shadowed.
    return packageName.equals(devicePolicyManager.getDeviceOwner());
  }

  private boolean isPackageActiveLauncher(String packageName) {
    Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
    ResolveInfo info = resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
    return info != null && packageName.equals(info.activityInfo.packageName);
  }

  private boolean isPackageRequiredInstaller(String packageName) {
    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.setDataAndType(Uri.fromFile(new File("foo.apk")), PACKAGE_MIME_TYPE);
    ResolveInfo info =
        resolveActivity(
            intent,
            PackageManager.MATCH_SYSTEM_ONLY
                | PackageManager.MATCH_DIRECT_BOOT_AWARE
                | PackageManager.MATCH_DIRECT_BOOT_UNAWARE);
    return info != null && packageName.equals(info.activityInfo.packageName);
  }

  private boolean isPackageRequiredUninstaller(String packageName) {
    final Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
    intent.addCategory(Intent.CATEGORY_DEFAULT);
    intent.setData(Uri.fromParts(PACKAGE_SCHEME, "foo.bar", null));
    ResolveInfo info =
        resolveActivity(
            intent,
            PackageManager.MATCH_SYSTEM_ONLY
                | PackageManager.MATCH_DIRECT_BOOT_AWARE
                | PackageManager.MATCH_DIRECT_BOOT_UNAWARE);
    return info != null && packageName.equals(info.activityInfo.packageName);
  }

  private boolean isPackageRequiredVerifier(String packageName) {
    final Intent intent = new Intent(Intent.ACTION_PACKAGE_NEEDS_VERIFICATION);
    List<ResolveInfo> infos =
        queryBroadcastReceivers(
            intent,
            PackageManager.MATCH_SYSTEM_ONLY
                | PackageManager.MATCH_DIRECT_BOOT_AWARE
                | PackageManager.MATCH_DIRECT_BOOT_UNAWARE);
    if (infos != null) {
      for (ResolveInfo info : infos) {
        if (packageName.equals(info.activityInfo.packageName)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isPackageDefaultDialer(String packageName) {
    TelecomManager telecomManager =
        (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
    return packageName.equals(telecomManager.getDefaultDialerPackage());
  }

  @HiddenApi
  @Implementation(minSdk = Q)
  @RequiresPermission(permission.SUSPEND_APPS)
  protected String[] getUnsuspendablePackages(String[] packageNames) {
    checkNotNull(packageNames, "packageNames cannot be null");
    if (getContext().checkSelfPermission(permission.SUSPEND_APPS)
        != PackageManager.PERMISSION_GRANTED) {
      throw new SecurityException("Current process does not have " + permission.SUSPEND_APPS);
    }
    ArrayList<String> unsuspendablePackages = new ArrayList<>();
    for (String packageName : packageNames) {
      if (!canSuspendPackage(packageName)) {
        unsuspendablePackages.add(packageName);
      }
    }
    return unsuspendablePackages.toArray(new String[0]);
  }

  @HiddenApi
  @Implementation(minSdk = P)
  protected boolean isPackageSuspended(String packageName) throws NameNotFoundException {
    PackageSetting setting = packageSettings.get(packageName);
    if (setting == null) {
      throw new NameNotFoundException(packageName);
    }
    return setting.isSuspended();
  }

  @Implementation(minSdk = O)
  protected boolean isInstantApp(String packageName) {
    PackageInfo pi = getInternalMutablePackageInfo(packageName);
    if (pi == null) {
      return false;
    }
    ApplicationInfo ai = pi.applicationInfo;
    if (ai == null) {
      return false;
    }
    return ai.isInstantApp();
  }

  @HiddenApi
  @Implementation(minSdk = Q)
  protected String[] setDistractingPackageRestrictions(String[] packages, int restrictionFlags) {
    for (String pkg : packages) {
      distractingPackageRestrictions.put(pkg, restrictionFlags);
    }
    return new String[0];
  }

  private Context getContext() {
    return reflector(ReflectorApplicationPackageManager.class, realObject).getContext();
  }

  /** Accessor interface for {@link ApplicationPackageManager}'s internals. */
  @ForType(ApplicationPackageManager.class)
  private interface ReflectorApplicationPackageManager {
    @Accessor("mContext")
    Context getContext();
  }
}
