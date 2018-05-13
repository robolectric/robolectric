package org.robolectric.shadows;

import static android.content.IntentFilter.MATCH_CATEGORY_MASK;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.content.pm.PackageManager.GET_SIGNATURES;
import static android.content.pm.PackageManager.MATCH_DISABLED_COMPONENTS;
import static android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES;
import static android.content.pm.PackageManager.SIGNATURE_UNKNOWN_PACKAGE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.VolumeInfo;
import android.telecom.TelecomManager;
import android.util.Pair;
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
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(value = ApplicationPackageManager.class, isInAndroidSdk = false, looseSignatures = true)
public class ShadowApplicationPackageManager extends ShadowPackageManager {

  
  /** Package name of the Android platform. */
  private static final String PLATFORM_PACKAGE_NAME = "android";

  /** MIME type of Android Packages (APKs). */
  private static final String PACKAGE_MIME_TYPE = "application/vnd.android.package-archive";

  /** {@link Uri} scheme of installed apps. */
  private static final String PACKAGE_SCHEME = "package";
  

  @RealObject
  private ApplicationPackageManager realObject;

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

      result.add(packageInfo);
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
            if ((flags & GET_META_DATA) != 0) {
              result.metaData = activity.metaData;
            }

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

    return results.isEmpty()
        ? null
        :results.toArray(new String[results.size()]);
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
          ProviderInfo result = new ProviderInfo();
          result.packageName = provider.packageName;
          result.name = provider.name;
          result.authority = provider.authority;
          result.readPermission = provider.readPermission;
          result.writePermission = provider.writePermission;
          result.pathPermissions = provider.pathPermissions;

          if ((flags & GET_META_DATA) != 0) {
            result.metaData = provider.metaData;
          }
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
          return providerInfo;
        }
      }
    }
    return null;
  }

  @Implementation
  protected ProviderInfo resolveContentProviderAsUser(
      String name, int flags, @UserIdInt int userId) {
    return null;
  }

  @Implementation
  protected PackageInfo getPackageInfo(String packageName, int flags) throws NameNotFoundException {
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
      return info;
    } else {
      throw new NameNotFoundException(packageName);
    }
  }

  @Implementation
  protected List<ResolveInfo> queryIntentServices(Intent intent, int flags) {
    // Check the manually added resolve infos first.
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      return filterResolvedServices(resolveInfoList, flags);
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedService = resolveServiceForExplicitIntent(intent);
      if (resolvedService != null) {
        resolveInfoList = filterResolvedServices(Arrays.asList(resolvedService), flags);
      }
    } else {
      resolveInfoList = filterResolvedServices(queryImplicitIntentServices(intent, flags), flags);
    }
    return resolveInfoList;
  }

  private List<ResolveInfo> filterResolvedServices(List<ResolveInfo> resolveInfoList, int flags) {
    // If the flag is set, no further filtering will happen.
    if (isFlagSet(flags, PackageManager.MATCH_ALL)) {
      return resolveInfoList;
    }
    // Create a copy of the list for filtering
    resolveInfoList = new ArrayList<>(resolveInfoList);

    for (Iterator<ResolveInfo> iterator = resolveInfoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      if (isFlagSet(flags, PackageManager.MATCH_SYSTEM_ONLY)) {
        if (resolveInfo.serviceInfo == null || resolveInfo.serviceInfo.applicationInfo == null) {
          // TODO: for backwards compatibility just skip filtering. In future should just remove
          // invalid resolve infos from list
          iterator.remove();
          continue;
        } else {
          final int applicationFlags = resolveInfo.serviceInfo.applicationInfo.flags;
          if ((applicationFlags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM) {
            iterator.remove();
            continue;
          }
        }
      }
      if (!isFlagSet(flags, PackageManager.MATCH_DISABLED_COMPONENTS)
          && resolveInfo != null
          && isValidComponentInfo(resolveInfo.serviceInfo)) {
        ComponentName componentName =
            new ComponentName(
                resolveInfo.serviceInfo.applicationInfo.packageName, resolveInfo.serviceInfo.name);
        if ((getComponentEnabledSetting(componentName)
                & PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            != 0) {
          iterator.remove();
          continue;
        }
      }
      if (!isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)
          && resolveInfo != null
          && isValidComponentInfo(resolveInfo.serviceInfo)
          && hiddenPackages.contains(resolveInfo.serviceInfo.applicationInfo.packageName)) {
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
      result.addAll(filterResolvedActivities(resolveInfoList, flags));
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedActivity = resolveActivityForExplicitIntent(intent);
      if (resolvedActivity != null) {
        result.addAll(filterResolvedActivities(Arrays.asList(resolvedActivity), flags));
      }
    } else {
      result.addAll(filterResolvedActivities(queryImplicitIntentActivities(intent, flags), flags));
    }
    return result;
  }

  private List<ResolveInfo> filterResolvedActivities(List<ResolveInfo> resolveInfoList, int flags) {
    // If the flag is set, no further filtering will happen.
    if (isFlagSet(flags, PackageManager.MATCH_ALL)) {
      return resolveInfoList;
    }
    // Create a copy of the list for filtering
    resolveInfoList = new ArrayList<>(resolveInfoList);

    for (Iterator<ResolveInfo> iterator = resolveInfoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();

      if (isFlagSet(flags, PackageManager.MATCH_SYSTEM_ONLY)) {
        // TODO: for backwards compatibility only remove invalid components when MATCH_SYSTEM_ONLY
        // In future should just remove all invalid resolve infos from list
        if (resolveInfo.activityInfo == null || resolveInfo.activityInfo.applicationInfo == null) {
          iterator.remove();
          continue;
        } else {
          final int applicationFlags = resolveInfo.activityInfo.applicationInfo.flags;
          if (!isFlagSet(applicationFlags, ApplicationInfo.FLAG_SYSTEM)) {
            iterator.remove();
            continue;
          }
        }
      }
      if (!isFlagSet(flags, PackageManager.MATCH_DISABLED_COMPONENTS)
          && resolveInfo != null
          && isValidComponentInfo(resolveInfo.activityInfo)) {
        ComponentName componentName =
            new ComponentName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name);
        if ((getComponentEnabledSetting(componentName)
                & PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
            != 0) {
          iterator.remove();
          continue;
        }
      }
      if (!isFlagSet(flags, MATCH_UNINSTALLED_PACKAGES)
          && resolveInfo != null
          && isValidComponentInfo(resolveInfo.activityInfo)
          && hiddenPackages.contains(resolveInfo.activityInfo.applicationInfo.packageName)) {
        iterator.remove();
        continue;
      }
    }

    return resolveInfoList;
  }

  /** Behaves as {@link #queryIntentActivities(Intent, int)} and currently ignores userId. */
  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected List<ResolveInfo> queryIntentActivitiesAsUser(Intent intent, int flags, int userId) {
    return queryIntentActivities(intent, flags);
  }

  /**
   * Returns true if intent has specified a specific component.
   */
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

  private static <T extends Component> T findMatchingComponent(ComponentName componentName,
      List<T> components) {
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

  private List<ResolveInfo> queryImplicitIntentActivities(Intent intent, int flags) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Activity activity : appPackage.activities) {
          IntentFilter intentFilter = matchIntentFilter(intent, activity.intents, flags);
          if (intentFilter != null) {
            resolveInfoList.add(buildResolveInfo(activity, intentFilter));
          }
        }
      }
    }

    return resolveInfoList;
  }

  private List<ResolveInfo> queryImplicitIntentServices(Intent intent, int flags) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Service service : appPackage.services) {
          IntentFilter intentFilter = matchIntentFilter(intent, service.intents, flags);
          if (intentFilter != null) {
            resolveInfoList.add(buildResolveInfo(service, intentFilter));
          }
        }
      }
    }

    return resolveInfoList;
  }

  private List<ResolveInfo> queryImplicitIntentReceivers(Intent intent, int flags) {
    List<ResolveInfo> resolveInfoList = new ArrayList<>();

    for (Package appPackage : packages.values()) {
      if (intent.getPackage() == null || intent.getPackage().equals(appPackage.packageName)) {
        for (Activity activity : appPackage.receivers) {
          IntentFilter intentFilter = matchIntentFilter(intent, activity.intents, flags);
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
    for (String permission : permissionsInfo.requestedPermissions) {
      if (permission != null && permission.equals(permName)) {
        return PackageManager.PERMISSION_GRANTED;
      }
    }
    return PackageManager.PERMISSION_DENIED;
  }

  @Implementation
  protected ActivityInfo getReceiverInfo(ComponentName className, int flags)
      throws NameNotFoundException {
    String packageName = className.getPackageName();

    PackageInfo packageInfo = packageInfos.get(packageName);
    if (packageInfo != null && packageInfo.receivers != null) {
      for (ActivityInfo receiver : packageInfo.receivers) {
        if (resolvePackageName(packageName, className).equals(receiver.name)) {
          ActivityInfo result = new ActivityInfo();
          result.packageName = receiver.packageName;
          result.name = receiver.name;
          if ((flags & GET_META_DATA) != 0) {
            result.metaData = receiver.metaData;
          }
          return result;
        }
      }
    }

    return null;
  }

  @Implementation
  protected List<ResolveInfo> queryBroadcastReceivers(Intent intent, int flags) {
    // Check the manually added resolve infos first.
    List<ResolveInfo> resolveInfoList = queryOverriddenIntents(intent, flags);
    if (!resolveInfoList.isEmpty()) {
      return filterResolvedActivities(resolveInfoList, flags);
    }

    if (isExplicitIntent(intent)) {
      ResolveInfo resolvedReceiver = resolveReceiverForExplicitIntent(intent);
      if (resolvedReceiver != null) {
        resolveInfoList = filterResolvedActivities(Arrays.asList(resolvedReceiver), flags);
      }
    } else {
      resolveInfoList =
          filterResolvedActivities(queryImplicitIntentReceivers(intent, flags), flags);
    }
    return resolveInfoList;
  }

  private static IntentFilter matchIntentFilter(
      Intent intent, ArrayList<? extends PackageParser.IntentInfo> intentFilters, int flags) {
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
            ServiceInfo result = new ServiceInfo();
            result.packageName = service.packageName;
            result.name = service.name;
            result.applicationInfo = service.applicationInfo;
            result.permission = service.permission;
            if ((flags & GET_META_DATA) != 0) {
              result.metaData = service.metaData;
            }
            return result;
          }
        }
      }
      throw new NameNotFoundException(serviceName);
    }
    return null;
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
    return permissionRationaleMap.containsKey(permission) ? permissionRationaleMap.get(permission) : false;
  }

  @Implementation
  protected FeatureInfo[] getSystemAvailableFeatures() {
    return systemAvailableFeatures.isEmpty() ? null : systemAvailableFeatures.toArray(new FeatureInfo[systemAvailableFeatures.size()]);
  }

  @Implementation
  protected void verifyPendingInstall(int id, int verificationCode) {
    if (verificationResults.containsKey(id)) {
      throw new IllegalStateException("Multiple verifications for id=" + id);
    }
    verificationResults.put(id, verificationCode);
  }

  @Implementation
  protected void extendVerificationTimeout(
      int id, int verificationCodeAtTimeout, long millisecondsToDelay) {
    verificationTimeoutExtension.put(id, millisecondsToDelay);
  }

  @Implementation
  @Override
  protected void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {}

  @Implementation
  protected void freeStorageAndNotify(
      String volumeUuid, long freeStorageSize, IPackageDataObserver observer) {}

  @Implementation
  protected void setInstallerPackageName(String targetPackage, String installerPackageName) {
    packageInstallerMap.put(targetPackage, installerPackageName);
  }

  @Implementation
  protected List<ResolveInfo> queryIntentContentProviders(Intent intent, int flags) {
    return Collections.emptyList();
  }

  @Implementation
  protected List<ResolveInfo> queryIntentContentProvidersAsUser(
      Intent intent, int flags, int userId) {
    return Collections.emptyList();
  }

  @Implementation
  protected String getPermissionControllerPackageName() {
    return null;
  }

  @Implementation(maxSdk = JELLY_BEAN)
  protected void getPackageSizeInfo(Object pkgName, Object observer) {
    final PackageStats packageStats = packageStatsMap.get((String) pkgName);
    new Handler(Looper.getMainLooper()).post(() -> {
      try {
        ((IPackageStatsObserver) observer).onGetStatsCompleted(packageStats, packageStats != null);
      } catch (RemoteException remoteException) {
        remoteException.rethrowFromSystemServer();
      }
    });
  }

  @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = M)
  protected void getPackageSizeInfo(Object pkgName, Object uid, final Object observer) {
    final PackageStats packageStats = packageStatsMap.get((String) pkgName);
    new Handler(Looper.getMainLooper()).post(() -> {
      try {
        ((IPackageStatsObserver) observer).onGetStatsCompleted(packageStats, packageStats != null);
      } catch (RemoteException remoteException) {
        remoteException.rethrowFromSystemServer();
      }
    });
  }

  @Implementation(minSdk = N)
  protected void getPackageSizeInfoAsUser(Object pkgName, Object uid, final Object observer) {
    final PackageStats packageStats = packageStatsMap.get((String) pkgName);
    new Handler(Looper.getMainLooper()).post(() -> {
      try {
        ((IPackageStatsObserver) observer).onGetStatsCompleted(packageStats, packageStats != null);
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

  @Implementation
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

  @Implementation
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

  @Implementation
  protected int[] getPackageGids(String packageName, int flags) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected int getPackageUid(String packageName, int flags) throws NameNotFoundException {
    Integer uid = uidForPackage.get(packageName);
    if (uid == null) {
      throw new NameNotFoundException(packageName);
    }
    return uid;
  }

  @Implementation
  protected int getPackageUidAsUser(String packageName, int userId) throws NameNotFoundException {
    return 0;
  }

  @Implementation
  protected int getPackageUidAsUser(String packageName, int flags, int userId)
      throws NameNotFoundException {
    return 0;
  }

  /**
   * @see ShadowPackageManager#addPermissionGroupInfo(android.content.pm.PermissionGroupInfo)
   */
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

  /**
   * @see ShadowPackageManager#addPermissionGroupInfo(android.content.pm.PermissionGroupInfo)
   */
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
          PermissionGroupInfo permissionGroupInfo = PackageParser
              .generatePermissionGroupInfo(permissionGroup, flags);
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
        PackageInfo packageInfo = getPackageInfo(packageName, -1);
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

      return info.applicationInfo;
    } else {
      throw new NameNotFoundException(packageName);
    }
  }

  @Implementation
  protected String[] getSystemSharedLibraryNames() {
    return new String[0];
  }

  @Implementation
  protected @NonNull String getServicesSystemSharedLibraryPackageName() {
    return null;
  }

  @Implementation
  protected @NonNull String getSharedSystemSharedLibraryPackageName() {
    return "";
  }

  @Implementation
  protected boolean hasSystemFeature(String name, int version) {
    return false;
  }

  @Implementation
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

  @Implementation
  protected void grantRuntimePermission(
      String packageName, String permissionName, UserHandle user) {}

  @Implementation
  protected void revokeRuntimePermission(
      String packageName, String permissionName, UserHandle user) {}

  @Implementation
  protected int getPermissionFlags(String permissionName, String packageName, UserHandle user) {
    return 0;
  }

  @Implementation
  protected void updatePermissionFlags(
      String permissionName, String packageName, int flagMask, int flagValues, UserHandle user) {}

  @Implementation
  protected int getUidForSharedUser(String sharedUserName) throws NameNotFoundException {
    return 0;
  }

  @Implementation
  protected List<PackageInfo> getInstalledPackagesAsUser(int flags, int userId) {
    return null;
  }

  @Implementation
  protected List<PackageInfo> getPackagesHoldingPermissions(String[] permissions, int flags) {
    return null;
  }

  @Implementation
  protected ResolveInfo resolveActivityAsUser(Intent intent, int flags, int userId) {
    return null;
  }

  @Implementation
  protected List<ResolveInfo> queryIntentActivityOptions(
      ComponentName caller, Intent[] specifics, Intent intent, int flags) {
    return null;
  }

  @Implementation
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
    return drawables.get(new Pair<>(packageName, resId));
  }

  @Implementation
  protected Drawable getActivityIcon(ComponentName activityName) throws NameNotFoundException {
    return drawableList.get(activityName);
  }

  @Implementation
  protected Drawable getDefaultActivityIcon() {
    return Resources.getSystem().getDrawable(com.android.internal.R.drawable.sym_def_app_icon);
  }

  @Implementation
  protected Drawable getActivityBanner(ComponentName activityName) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected Drawable getActivityBanner(Intent intent) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected Drawable getApplicationBanner(ApplicationInfo info) {
    return null;
  }

  @Implementation
  protected Drawable getApplicationBanner(String packageName) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected Drawable getActivityLogo(ComponentName activityName) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected Drawable getActivityLogo(Intent intent) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected Drawable getApplicationLogo(ApplicationInfo info) {
    return null;
  }

  @Implementation
  protected Drawable getApplicationLogo(String packageName) throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected Drawable getUserBadgedIcon(Drawable icon, UserHandle user) {
    return null;
  }

  @Implementation
  protected Drawable getUserBadgedDrawableForDensity(
      Drawable drawable, UserHandle user, Rect badgeLocation, int badgeDensity) {
    return null;
  }

  @Implementation
  protected Drawable getUserBadgeForDensityNoBackground(UserHandle user, int density) {
    return null;
  }

  @Implementation
  protected CharSequence getUserBadgedLabel(CharSequence label, UserHandle user) {
    return null;
  }

  @Implementation
  protected Resources getResourcesForActivity(ComponentName activityName)
      throws NameNotFoundException {
    return null;
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

  @Implementation
  protected Resources getResourcesForApplicationAsUser(String appPackageName, int userId)
      throws NameNotFoundException {
    return null;
  }

  @Implementation
  protected void addOnPermissionsChangeListener(Object listener) {}

  @Implementation
  protected void removeOnPermissionsChangeListener(Object listener) {}

  @Implementation
  protected void installPackage(
      Object packageURI, Object observer, Object flags, Object installerPackageName) {}

  @Implementation
  protected int installExistingPackage(String packageName) throws NameNotFoundException {
    return 0;
  }

  @Implementation
  protected int installExistingPackageAsUser(String packageName, int userId)
      throws NameNotFoundException {
    return 0;
  }

  @Implementation
  protected void verifyIntentFilter(int id, int verificationCode, List<String> failedDomains) {}

  @Implementation
  protected int getIntentVerificationStatusAsUser(String packageName, int userId) {
    return 0;
  }

  @Implementation
  protected boolean updateIntentVerificationStatusAsUser(
      String packageName, int status, int userId) {
    return false;
  }

  @Implementation
  protected List<IntentFilterVerificationInfo> getIntentFilterVerifications(String packageName) {
    return null;
  }

  @Implementation
  protected List<IntentFilter> getAllIntentFilters(String packageName) {
    return null;
  }

  @Implementation
  protected String getDefaultBrowserPackageNameAsUser(int userId) {
    return null;
  }

  @Implementation
  protected boolean setDefaultBrowserPackageNameAsUser(String packageName, int userId) {
    return false;
  }

  @Implementation
  protected int getMoveStatus(int moveId) {
    return 0;
  }

  @Implementation
  protected void registerMoveCallback(Object callback, Object handler) {}

  @Implementation
  protected void unregisterMoveCallback(Object callback) {}

  @Implementation
  protected Object movePackage(Object packageName, Object vol) {
    return 0;
  }

  @Implementation
  protected Object getPackageCurrentVolume(Object app) {
    return null;
  }

  @Implementation
  protected List<VolumeInfo> getPackageCandidateVolumes(ApplicationInfo app) {
    return null;
  }

  @Implementation
  protected Object movePrimaryStorage(Object vol) {
    return 0;
  }

  @Implementation
  protected @Nullable Object getPrimaryStorageCurrentVolume() {
    return null;
  }

  @Implementation
  protected @NonNull List<VolumeInfo> getPrimaryStorageCandidateVolumes() {
    return null;
  }

  @Implementation
  protected void deletePackageAsUser(
      String packageName, IPackageDeleteObserver observer, int flags, int userId) {}

  @Implementation
  protected void clearApplicationUserData(String packageName, IPackageDataObserver observer) {}

  @Implementation
  protected void deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {}

  @Implementation
  protected void deleteApplicationCacheFilesAsUser(
      String packageName, int userId, IPackageDataObserver observer) {}

  @Implementation
  protected void freeStorage(String volumeUuid, long freeStorageSize, IntentSender pi) {}

  @Implementation
  protected String[] setPackagesSuspendedAsUser(
      String[] packageNames, boolean suspended, int userId) {
    return null;
  }

  @Implementation
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

  @Implementation
  protected ComponentName getHomeActivities(List<ResolveInfo> outActivities) {
    return null;
  }

  @Implementation
  protected void flushPackageRestrictionsAsUser(int userId) {}

  @Implementation
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

  @Implementation
  protected boolean getApplicationHiddenSettingAsUser(String packageName, UserHandle user) {
    // Note that this ignores the UserHandle parameter
    if (!packageInfos.containsKey(packageName)) {
      // Match Android behaviour of returning true if package isn't found
      return true;
    }
    return hiddenPackages.contains(packageName);
  }

  @Implementation
  protected Object getKeySetByAlias(String packageName, String alias) {
    return null;
  }

  @Implementation
  protected Object getSigningKeySet(String packageName) {
    return null;
  }

  @Implementation
  protected boolean isSignedBy(String packageName, Object ks) {
    return false;
  }

  @Implementation
  protected boolean isSignedByExactly(String packageName, Object ks) {
    return false;
  }

  @Implementation
  protected VerifierDeviceIdentity getVerifierDeviceIdentity() {
    return null;
  }

  @Implementation
  protected boolean isUpgrade() {
    return false;
  }

  @Implementation
  protected boolean isPackageAvailable(String packageName) {
    return false;
  }

  @Implementation
  protected void addCrossProfileIntentFilter(
      IntentFilter filter, int sourceUserId, int targetUserId, int flags) {}

  @Implementation
  protected void clearCrossProfileIntentFilters(int sourceUserId) {}

  @Implementation
  protected Drawable loadItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return null;
  }

  @Implementation
  protected Drawable loadUnbadgedItemIcon(PackageItemInfo itemInfo, ApplicationInfo appInfo) {
    return null;
  }

  @Implementation(minSdk = O)
  protected Object getChangedPackages(int sequenceNumber) {
    if (sequenceNumber < 0) {
      return null;
    }
    return new ChangedPackages(
        sequenceNumber + 1, new ArrayList<>(sequenceNumberChangedPackagesMap.get(sequenceNumber)));
  }
  
  @Implementation(minSdk = android.os.Build.VERSION_CODES.P)
  public String getSystemTextClassifierPackageName() {
    return "";
  }
  

  
  @Implementation(minSdk = android.os.Build.VERSION_CODES.P)
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

  @Implementation
  @HiddenApi
  protected boolean isPackageSuspended(String packageName) throws NameNotFoundException {
    PackageSetting setting = packageSettings.get(packageName);
    if (setting == null) {
      throw new NameNotFoundException(packageName);
    }
    return setting.isSuspended();
  }
}
