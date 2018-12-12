package org.robolectric.shadows;

import static android.content.IntentFilter.MATCH_CATEGORY_MASK;
import static android.content.pm.PackageInfo.REQUESTED_PERMISSION_GRANTED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.MATCH_ALL;
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
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.DrawableRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
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
import android.content.pm.IPackageManager;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageParser.Provider;
import android.content.pm.PackageParser.Service;
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
import android.util.Pair;
import com.google.common.base.Function;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

@Implements(value = ApplicationPackageManager.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowApplicationPackageManager extends ShadowPackageManager {

  /** Package name of the Android platform. */
  private static final String PLATFORM_PACKAGE_NAME = "android";

  /** MIME type of Android Packages (APKs). */
  private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

  /** {@link Uri} scheme of installed apps. */
  private static final String PACKAGE_SCHEME = "package";

  @RealObject private ApplicationPackageManager realObject;

  private Context context;

  @Implementation
  protected void __constructor__(Object contextImpl, Object pm) {
    try {
      invokeConstructor(
          ApplicationPackageManager.class,
          realObject,
          from(Class.forName(ShadowContextImpl.CLASS_NAME), contextImpl),
          from(IPackageManager.class, pm));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    context = (Context) contextImpl;
  }

  @Implementation
  public List<PackageInfo> getInstalledPackages(int flags) {
    List<PackageInfo> result = new ArrayList<>();
    for (PackageInfo packageInfo : packageInfos.values()) {
      String packageName = packageInfo.packageName;

      if (applicationEnabledSettingMap.get(packageName) == COMPONENT_ENABLED_STATE_DISABLED
          && (flags & MATCH_UNINSTALLED_PACKAGES) != MATCH_UNINSTALLED_PACKAGES
          && (flags & MATCH_DISABLED_COMPONENTS) != MATCH_DISABLED_COMPONENTS) {
        continue;
      }

      if (hiddenPackages.contains(packageName) && !isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)) {
        continue;
      }

      result.add(newPackageInfo(packageInfo));
    }

    return result;
  }

  @Implementation
  protected ActivityInfo getActivityInfo(ComponentName component, int flags)
      throws NameNotFoundException {
    String activityName = component.getClassName();
    String packageName = component.getPackageName();
    PackageInfo packageInfo = packageInfos.get(packageName);

    if (packageInfo != null) {
      if (packageInfo.activities != null) {
        for (ActivityInfo activity : packageInfo.activities) {
          if (activityName.equals(activity.name)) {
            ActivityInfo result = new ActivityInfo(activity);
            applyFlagsToComponentInfo(result, flags);

            return result;
          }
        }
      }

      // Activity is requested is not listed in the AndroidManifest.xml
      ActivityInfo result = new ActivityInfo();
      result.name = activityName;
      result.packageName = packageName;
      result.applicationInfo = new ApplicationInfo(packageInfo.applicationInfo);
      return result;
    }

    // TODO: Should throw a NameNotFoundException
    // In the cases where an Activity from another package has been requested.
    ActivityInfo result = new ActivityInfo();
    result.name = activityName;
    result.packageName = packageName;
    result.applicationInfo = new ApplicationInfo();
    result.applicationInfo.packageName = packageName;
    result.applicationInfo.flags = ApplicationInfo.FLAG_INSTALLED;
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
  protected @Nullable String[] getPackagesForUid(int uid) {
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

    return results.isEmpty() ? null : results.toArray(new String[results.size()]);
  }

  @Implementation
  protected int getApplicationEnabledSetting(String packageName) {
    try {
      getPackageInfo(packageName, -1);
    } catch (NameNotFoundException e) {
      throw new IllegalArgumentException(e);
    }

    return applicationEnabledSettingMap.get(packageName);
  }

  @Implementation
  protected ProviderInfo getProviderInfo(ComponentName component, int flags)
      throws NameNotFoundException {
    String packageName = component.getPackageName();

    PackageInfo packageInfo = packageInfos.get(packageName);
    if (packageInfo != null && packageInfo.providers != null) {
      for (ProviderInfo provider : packageInfo.providers) {
        if (resolvePackageName(packageName, component).equals(provider.name)) {
          ProviderInfo result = new ProviderInfo(provider);
          applyFlagsToComponentInfo(result, flags);
          return result;
        }
      }
    }

    throw new NameNotFoundException("Package not found: " + packageName);
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
    HashSet<ComponentName> preferredComponents = new HashSet<>();

    for (Entry<IntentFilterWrapper, ComponentName> preferred : preferredActivities.entrySet()) {
      if ((preferred.getKey().getFilter().match(context.getContentResolver(), intent, false, "robo")
              & MATCH_CATEGORY_MASK)
          != 0) {
        preferredComponents.add(preferred.getValue());
      }
    }
    List<ResolveInfo> candidates = queryIntentActivities(intent, flags);

    return candidates.isEmpty()
        ? null
        : Collections.max(candidates, new ResolveInfoComparator(preferredComponents));
  }

  @Implementation
  protected ProviderInfo resolveContentProvider(String name, int flags) {
    if (name == null) {
      return null;
    }
    for (PackageInfo packageInfo : packageInfos.values()) {
      if (packageInfo.providers == null) continue;

      for (ProviderInfo providerInfo : packageInfo.providers) {
        if (name.equals(providerInfo.authority)) { // todo: support multiple authorities
          return new ProviderInfo(providerInfo);
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
    PackageInfo info = packageInfos.get(packageName);
    if (info != null) {
      if (applicationEnabledSettingMap.get(packageName) == COMPONENT_ENABLED_STATE_DISABLED
          && (flags & MATCH_UNINSTALLED_PACKAGES) != MATCH_UNINSTALLED_PACKAGES
          && (flags & MATCH_DISABLED_COMPONENTS) != MATCH_DISABLED_COMPONENTS) {
        throw new NameNotFoundException("Package is disabled, can't find");
      }
      if (hiddenPackages.contains(packageName) && !isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)) {
        throw new NameNotFoundException("Package is hidden, can't find");
      }
      return newPackageInfo(info);
    } else {
      throw new NameNotFoundException(packageName);
    }
  }

  // There is no copy constructor for PackageInfo
  private static PackageInfo newPackageInfo(PackageInfo orig) {
    Parcel parcel = Parcel.obtain();
    orig.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    return PackageInfo.CREATOR.createFromParcel(parcel);
  }

  @Implementation
  protected List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    List<ResolveInfo> result = new ArrayList<>();
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      result.addAll(
          filterResolvedComponent(
              resolveInfoList, flags, (resolveInfo) -> resolveInfo.serviceInfo));
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedService = resolveServiceForExplicitIntent(intent);
      if (resolvedService != null) {
        result.addAll(
            filterResolvedComponent(
                Arrays.asList(resolvedService), flags, (resolveInfo) -> resolveInfo.serviceInfo));
      }
    } else {
      result.addAll(
          filterResolvedComponent(
              queryImplicitIntentServices(intent),
              flags,
              (resolveInfo) -> resolveInfo.serviceInfo));
    }
    return result;
  }

  private List<ResolveInfo> filterResolvedComponent(
      List<ResolveInfo> resolveInfoList,
      int flags,
      Function<ResolveInfo, ComponentInfo> componentInfoFn) {
    // If the flag is set, no further filtering will happen.
    if (isFlagSet(flags, PackageManager.MATCH_ALL)) {
      return resolveInfoList;
    }
    // Create a copy of the list for filtering
    resolveInfoList = new ArrayList<>(resolveInfoList);

    for (Iterator<ResolveInfo> iterator = resolveInfoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      ComponentInfo componentInfo = componentInfoFn.apply(resolveInfo);

      boolean hasSomeComponentInfo =
          resolveInfo.activityInfo != null
              || resolveInfo.serviceInfo != null
              || (VERSION.SDK_INT >= VERSION_CODES.KITKAT && resolveInfo.providerInfo != null);
      if (componentInfo == null && hasSomeComponentInfo) {
        // wrong type of component. For backward compatibility we keep those entries that doesn't
        // have any component.
        iterator.remove();
        continue;
      }

      if (isFlagSet(flags, PackageManager.MATCH_SYSTEM_ONLY)) {
        if (componentInfo == null || componentInfo.applicationInfo == null) {
          // TODO: for backwards compatibility just skip filtering. In future should just remove
          // invalid resolve infos from list
          iterator.remove();
          continue;
        } else {
          final int applicationFlags = componentInfo.applicationInfo.flags;
          if ((applicationFlags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
            iterator.remove();
            continue;
          }
        }
      }
      if (!isFlagSet(flags, PackageManager.MATCH_DISABLED_COMPONENTS)
          && resolveInfo != null
          && isValidComponentInfo(componentInfo)) {
        ComponentName componentName =
            new ComponentName(componentInfo.applicationInfo.packageName, componentInfo.name);
        if ((getComponentEnabledSetting(componentName)
                & PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            != 0) {
          iterator.remove();
          continue;
        }
      }
      if (!isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)
          && resolveInfo != null
          && isValidComponentInfo(componentInfo)
          && hiddenPackages.contains(componentInfo.applicationInfo.packageName)) {
        iterator.remove();
        continue;
      }
    }
    return resolveInfoList;
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
    List<ResolveInfo> result = new ArrayList<>();
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      result.addAll(
          filterResolvedComponent(
              resolveInfoList, flags, (resolveInfo) -> resolveInfo.activityInfo));
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedActivity = resolveActivityForExplicitIntent(intent);
      if (resolvedActivity != null) {
        result.addAll(
            filterResolvedComponent(
                Arrays.asList(resolvedActivity), flags, (resolveInfo) -> resolveInfo.activityInfo));
      }
    } else {
      result.addAll(
          filterResolvedComponent(
              queryImplicitIntentActivities(intent),
              flags,
              (resolveInfo) -> resolveInfo.activityInfo));
    }
    return result;
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

  private ResolveInfo resolveActivityForExplicitIntent(Intent intent) {
    ComponentName component = getComponentForIntent(intent);
    for (Package appPackage : packages.values()) {
      Activity activity = findMatchingComponent(component, appPackage.activities);
      if (activity != null) {
        return buildResolveInfo(activity);
      }
    }
    return null;
  }

  private ResolveInfo resolveServiceForExplicitIntent(Intent intent) {
    ComponentName component = getComponentForIntent(intent);
    for (Package appPackage : packages.values()) {
      Service service = findMatchingComponent(component, appPackage.services);
      if (service != null) {
        return buildResolveInfo(service);
      }
    }
    return null;
  }

  private ResolveInfo resolveReceiverForExplicitIntent(Intent intent) {
    ComponentName component = getComponentForIntent(intent);
    for (Package appPackage : packages.values()) {
      Activity receiver = findMatchingComponent(component, appPackage.receivers);
      if (receiver != null) {
        return buildResolveInfo(receiver);
      }
    }
    return null;
  }

  private ResolveInfo resolveContentProviderForExplicitIntent(Intent intent) {
    ComponentName component = getComponentForIntent(intent);
    for (Package appPackage : packages.values()) {
      Provider provider = findMatchingComponent(component, appPackage.providers);
      if (provider != null) {
        return buildResolveInfo(provider);
      }
    }
    return null;
  }

  private static <T extends Component> T findMatchingComponent(
      ComponentName componentName, List<T> components) {
    for (T component : components) {
      if (componentName.equals(component.getComponentName())) {
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

  private List<ResolveInfo> queryImplicitIntentContentProviders(Intent intent) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Provider provider : appPackage.providers) {
          IntentFilter intentFilter = matchIntentFilter(intent, provider.intents);
          if (intentFilter != null) {
            resolveInfoList.add(buildResolveInfo(provider));
          }
        }
      }
    }

    return resolveInfoList;
  }

  private List<ResolveInfo> queryImplicitIntentActivities(Intent intent) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Activity activity : appPackage.activities) {
          IntentFilter intentFilter = matchIntentFilter(intent, activity.intents);
          if (intentFilter != null) {
            resolveInfoList.add(buildResolveInfo(activity, intentFilter));
          }
        }
      }
    }

    return resolveInfoList;
  }

  private List<ResolveInfo> queryImplicitIntentServices(Intent intent) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Service service : appPackage.services) {
          IntentFilter intentFilter = matchIntentFilter(intent, service.intents);
          if (intentFilter != null) {
            resolveInfoList.add(buildResolveInfo(service, intentFilter));
          }
        }
      }
    }

    return resolveInfoList;
  }

  private List<ResolveInfo> queryImplicitIntentReceivers(Intent intent) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Activity activity : appPackage.receivers) {
          IntentFilter intentFilter = matchIntentFilter(intent, activity.intents);
          if (intentFilter != null) {
            resolveInfoList.add(buildResolveInfo(activity, intentFilter));
          }
        }
      }
    }

    return resolveInfoList;
  }

  static ResolveInfo buildResolveInfo(Activity activity) {
    ResolveInfo resolveInfo = buildResolveInfo(activity.info);
    resolveInfo.activityInfo = activity.info;
    return resolveInfo;
  }

  static ResolveInfo buildResolveInfo(Service service) {
    ResolveInfo resolveInfo = buildResolveInfo(service.info);
    resolveInfo.serviceInfo = service.info;
    return resolveInfo;
  }

  static ResolveInfo buildResolveInfo(Provider provider) {
    ResolveInfo resolveInfo = buildResolveInfo(provider.info);
    resolveInfo.providerInfo = provider.info;
    return resolveInfo;
  }

  private static ResolveInfo buildResolveInfo(ComponentInfo componentInfo) {
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.resolvePackageName = componentInfo.applicationInfo.packageName;
    return resolveInfo;
  }

  static ResolveInfo buildResolveInfo(Activity activity, IntentFilter intentFilter) {
    ResolveInfo info = buildResolveInfo(activity);
    info.isDefault = intentFilter.hasCategory("Intent.CATEGORY_DEFAULT");
    info.filter = new IntentFilter(intentFilter);
    return info;
  }

  static ResolveInfo buildResolveInfo(Service service, IntentFilter intentFilter) {
    ResolveInfo info = buildResolveInfo(service);
    info.isDefault = intentFilter.hasCategory("Intent.CATEGORY_DEFAULT");
    info.serviceInfo = service.info;
    info.filter = new IntentFilter(intentFilter);
    return info;
  }

  @Implementation
  protected int checkPermission(String permName, String pkgName) {
    PackageInfo permissionsInfo = packageInfos.get(pkgName);
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
  protected ActivityInfo getReceiverInfo(ComponentName className, int flags)
      throws NameNotFoundException {
    String packageName = className.getPackageName();

    PackageInfo packageInfo = packageInfos.get(packageName);
    if (packageInfo != null && packageInfo.receivers != null) {
      for (ActivityInfo receiver : packageInfo.receivers) {
        if (resolvePackageName(packageName, className).equals(receiver.name)) {
          ActivityInfo result = new ActivityInfo(receiver);
          applyFlagsToComponentInfo(result, flags);
          return result;
        }
      }
    }

    return null;
  }

  @Implementation
  protected List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    List<ResolveInfo> result = new ArrayList<>();
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      result.addAll(
          filterResolvedComponent(
              resolveInfoList, flags, (resolveInfo) -> resolveInfo.activityInfo));
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedReceiver = resolveReceiverForExplicitIntent(intent);
      if (resolvedReceiver != null) {
        result.addAll(
            filterResolvedComponent(
                Arrays.asList(resolvedReceiver), flags, (resolveInfo) -> resolveInfo.activityInfo));
      }
    } else {
      result.addAll(
          filterResolvedComponent(
              queryImplicitIntentReceivers(intent),
              flags,
              (resolveInfo) -> resolveInfo.activityInfo));
    }
    return result;
  }

  private static IntentFilter matchIntentFilter(
      Intent intent, ArrayList<? extends PackageParser.IntentInfo> intentFilters) {
    for (PackageParser.IntentInfo intentInfo : intentFilters) {
      if (intentInfo.match(
              intent.getAction(),
              intent.getType(),
              intent.getScheme(),
              intent.getData(),
              intent.getCategories(),
              "ShadowPackageManager")
          >= 0) {
        return intentInfo;
      }
    }
    return null;
  }

  @Implementation
  protected ResolveInfo resolveService(Intent intent, int flags) {
    List<ResolveInfo> candidates = queryIntentServices(intent, flags);
    return candidates.isEmpty() ? null : candidates.get(0);
  }

  @Implementation
  protected ServiceInfo getServiceInfo(ComponentName className, int flags)
      throws NameNotFoundException {
    String packageName = className.getPackageName();
    PackageInfo packageInfo = packageInfos.get(packageName);

    if (packageInfo != null) {
      String serviceName = className.getClassName();
      if (packageInfo.services != null) {
        for (ServiceInfo service : packageInfo.services) {
          if (serviceName.equals(service.name)) {
            ServiceInfo result = new ServiceInfo(service);
            applyFlagsToComponentInfo(result, flags);
            result.applicationInfo = new ApplicationInfo(service.applicationInfo);
            if (result.processName == null) {
              result.processName = result.applicationInfo.processName;
            }
            return result;
          }
        }
      }
      throw new NameNotFoundException(serviceName);
    }
    return null;
  }

  private void applyFlagsToComponentInfo(ComponentInfo result, int flags)
      throws NameNotFoundException {
    if ((flags & GET_META_DATA) == 0) {
      result.metaData = null;
    }
    if ((flags & MATCH_ALL) != 0) {
      return;
    }
  }

  @Implementation
  protected Resources getResourcesForApplication(@NonNull ApplicationInfo applicationInfo)
      throws PackageManager.NameNotFoundException {
    return getResourcesForApplication(applicationInfo.packageName);
  }

  @Implementation
  protected List<ApplicationInfo> getInstalledApplications(int flags) {
    List<ApplicationInfo> result = new ArrayList<>();

    for (PackageInfo packageInfo : packageInfos.values()) {
      result.add(packageInfo.applicationInfo);
    }
    return result;
  }

  @Implementation
  protected String getInstallerPackageName(String packageName) {
    return packageInstallerMap.get(packageName);
  }

  @Implementation
  protected PermissionInfo getPermissionInfo(String name, int flags) throws NameNotFoundException {
    PermissionInfo permissionInfo = extraPermissions.get(name);
    if (permissionInfo != null) {
      return permissionInfo;
    }

    for (PackageInfo packageInfo : packageInfos.values()) {
      if (packageInfo.permissions != null) {
        for (PermissionInfo permission : packageInfo.permissions) {
          if (name.equals(permission.name)) {
            return createCopyPermissionInfo(permission, flags);
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
    List<ResolveInfo> result = new ArrayList<>();
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      result.addAll(
          filterResolvedComponent(
              resolveInfoList, flags, (resolveInfo) -> resolveInfo.providerInfo));
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedProvider = resolveContentProviderForExplicitIntent(intent);
      if (resolvedProvider != null) {
        result.addAll(
            filterResolvedComponent(
                Arrays.asList(resolvedProvider), flags, (resolveInfo) -> resolveInfo.providerInfo));
      }
    } else {
      result.addAll(
          filterResolvedComponent(
              queryImplicitIntentContentProviders(intent),
              flags,
              (resolveInfo) -> resolveInfo.providerInfo));
    }
    return result;
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

  @Implementation
  protected void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
    pendingDeleteCallbacks.put(packageName, observer);
  }

  @Implementation
  protected String[] currentToCanonicalPackageNames(String[] names) {
    String[] out = new String[names.length];
    for (int i = names.length - 1; i >= 0; i--) {
      if (currentToCanonicalNames.containsKey(names[i])) {
        out[i] = currentToCanonicalNames.get(names[i]);
      } else {
        out[i] = names[i];
      }
    }
    return out;
  }

  @Implementation
  protected boolean isSafeMode() {
    return false;
  }

  @Implementation
  protected Drawable getApplicationIcon(String packageName) throws NameNotFoundException {
    return applicationIcons.get(packageName);
  }

  @Implementation
  protected Drawable getApplicationIcon(ApplicationInfo info) {
    return null;
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

    for (PackageInfo packageInfo : packageInfos.values()) {
      if (packageInfo.permissions != null) {
        for (PermissionInfo permission : packageInfo.permissions) {
          if (Objects.equals(group, permission.group)) {
            result.add(createCopyPermissionInfo(permission, flags));
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

  @Implementation
  protected Intent getLaunchIntentForPackage(String packageName) {
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
    intent.setClassName(packageName, ris.get(0).activityInfo.name);
    return intent;
  }

  ////////////////////////////

  @Implementation(minSdk = N)
  protected PackageInfo getPackageInfoAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected String[] canonicalToCurrentPackageNames(String[] names) {
    return new String[0];
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
    if (extraPermissionGroups.containsKey(name)) {
      return new PermissionGroupInfo(extraPermissionGroups.get(name));
    }

    for (Package pkg : packages.values()) {
      for (PermissionGroup permissionGroup : pkg.permissionGroups) {
        if (name.equals(permissionGroup.info.name)) {
          return PackageParser.generatePermissionGroupInfo(permissionGroup, flags);
        }
      }
    }

    throw new NameNotFoundException(name);
  }

  /** @see ShadowPackageManager#addPermissionGroupInfo(android.content.pm.PermissionGroupInfo) */
  @Implementation
  protected List<PermissionGroupInfo> getAllPermissionGroups(int flags) {
    ArrayList<PermissionGroupInfo> allPermissionGroups = new ArrayList<PermissionGroupInfo>();
    // To be consistent with Android's implementation, return at most one PermissionGroupInfo object
    // per permission group string
    HashSet<String> handledPermissionGroups = new HashSet<>();

    for (PermissionGroupInfo permissionGroupInfo : extraPermissionGroups.values()) {
      allPermissionGroups.add(new PermissionGroupInfo(permissionGroupInfo));
      handledPermissionGroups.add(permissionGroupInfo.name);
    }

    for (Package pkg : packages.values()) {
      for (PermissionGroup permissionGroup : pkg.permissionGroups) {
        if (!handledPermissionGroups.contains(permissionGroup.info.name)) {
          PermissionGroupInfo permissionGroupInfo =
              PackageParser.generatePermissionGroupInfo(permissionGroup, flags);
          allPermissionGroups.add(new PermissionGroupInfo(permissionGroupInfo));
          handledPermissionGroups.add(permissionGroup.info.name);
        }
      }
    }

    return allPermissionGroups;
  }

  @Implementation
  protected ApplicationInfo getApplicationInfo(String packageName, int flags)
      throws NameNotFoundException {
    PackageInfo info = packageInfos.get(packageName);
    if (info != null) {
      try {
        getPackageInfo(packageName, -1);
      } catch (NameNotFoundException e) {
        throw new IllegalArgumentException(e);
      }

      if (applicationEnabledSettingMap.get(packageName) == COMPONENT_ENABLED_STATE_DISABLED
          && (flags & MATCH_UNINSTALLED_PACKAGES) != MATCH_UNINSTALLED_PACKAGES
          && (flags & MATCH_DISABLED_COMPONENTS) != MATCH_DISABLED_COMPONENTS) {
        throw new NameNotFoundException("Package is disabled, can't find");
      }

      if (hiddenPackages.contains(packageName) && !isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)) {
        throw new NameNotFoundException("Package is hidden, can't find");
      }

      if (info.applicationInfo != null) {
        return new ApplicationInfo(info.applicationInfo);
      }
    }
    throw new NameNotFoundException(packageName);
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

    if (!packageInfos.containsKey(packageName)) {
      throw new SecurityException("Package not found: " + packageName);
    }
    PackageInfo packageInfo = packageInfos.get(packageName);
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

    if (!packageInfos.containsKey(packageName)) {
      throw new SecurityException("Package not found: " + packageName);
    }
    PackageInfo packageInfo = packageInfos.get(packageName);
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
    return null;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
    return null;
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
    if (context.getPackageName().equals(appPackageName)) {
      return context.getResources();
    } else if (packageInfos.containsKey(appPackageName)) {
      Resources appResources = resources.get(appPackageName);
      if (appResources == null) {
        appResources = new Resources(new AssetManager(), null, null);
        resources.put(appPackageName, appResources);
      }
      return appResources;
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
    preferredActivities.put(new IntentFilterWrapper(filter), activity);
  }

  @Implementation
  protected void replacePreferredActivity(
      IntentFilter filter, int match, ComponentName[] set, ComponentName activity) {
    addPreferredActivity(filter, match, set, activity);
  }

  @Implementation
  public int getPreferredActivities(
      List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    if (outFilters == null) {
      return 0;
    }

    Set<IntentFilterWrapper> filters = preferredActivities.keySet();
    for (IntentFilter filter : outFilters) {
      step:
      for (IntentFilterWrapper testFilterWrapper : filters) {
        ComponentName name = preferredActivities.get(testFilterWrapper);
        IntentFilter testFilter = testFilterWrapper.getFilter();
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

  @Implementation
  protected void clearPackagePreferredActivities(String packageName) {
    Iterator<ComponentName> entryIterator = preferredActivities.values().iterator();
    while (entryIterator.hasNext()) {
      ComponentName next = entryIterator.next();
      if (next.getPackageName().equals(packageName)) {
        entryIterator.remove();
      }
    }
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
    if (!packageInfos.containsKey(packageName)) {
      // Package doesn't exist
      return false;
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
    if (!packageInfos.containsKey(packageName)) {
      // Match Android behaviour of returning true if package isn't found
      return true;
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
    if (hasProfileOwnerOrDeviceOwnerOnCurrentUser()) {
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
      setting.setSuspended(suspended, dialogMessage, appExtras, launcherExtras);
    }
    return unupdatedPackages.toArray(new String[0]);
  }

  /** Returns whether the current user profile has a profile owner or a device owner. */
  private boolean hasProfileOwnerOrDeviceOwnerOnCurrentUser() {
    DevicePolicyManager devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    return devicePolicyManager.getProfileOwner() != null
        || (UserHandle.of(UserHandle.myUserId()).isSystem()
            && devicePolicyManager.getDeviceOwner() != null);
  }

  private boolean canSuspendPackage(String packageName) {
    // This code approximately mirrors PackageManagerService#canSuspendPackageForUserLocked.
    return !packageName.equals(context.getPackageName())
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
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
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
        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
    return packageName.equals(telecomManager.getDefaultDialerPackage());
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
    return false;
  }
}
