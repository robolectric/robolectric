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
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.N;
import static java.util.Arrays.asList;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.Manifest;
import android.annotation.UserIdInt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.ModuleInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Component;
import android.content.pm.PackageParser.IntentInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.PermissionGroup;
import android.content.pm.PackageStats;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Pair;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Array;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowPackageParser._PackageParser_;

@SuppressWarnings("NewApi")
@Implements(PackageManager.class)
public class ShadowPackageManager {
  static final String TAG = "PackageManager";

  @RealObject PackageManager realPackageManager;

  static Map<String, Boolean> permissionRationaleMap = new HashMap<>();
  static List<FeatureInfo> systemAvailableFeatures = new ArrayList<>();
  static final List<String> systemSharedLibraryNames = new ArrayList<>();
  static final Map<String, PackageInfo> packageInfos =
      Collections.synchronizedMap(new LinkedHashMap<>());
  static final Map<String, ModuleInfo> moduleInfos =
      Collections.synchronizedMap(new LinkedHashMap<>());

  // Those maps contain filter for components. If component exists but doesn't have filters,
  // it will have an entry in the map with an empty list.
  static final SortedMap<ComponentName, List<IntentFilter>> activityFilters = new TreeMap<>();
  static final SortedMap<ComponentName, List<IntentFilter>> serviceFilters = new TreeMap<>();
  static final SortedMap<ComponentName, List<IntentFilter>> providerFilters = new TreeMap<>();
  static final SortedMap<ComponentName, List<IntentFilter>> receiverFilters = new TreeMap<>();

  private static Map<String, PackageInfo> packageArchiveInfo = new HashMap<>();
  static final Map<String, PackageStats> packageStatsMap = new HashMap<>();
  static final Map<String, String> packageInstallerMap = new HashMap<>();
  static final Map<Integer, String[]> packagesForUid = new HashMap<>();
  static final Map<String, Integer> uidForPackage = new HashMap<>();
  static final Map<Integer, String> namesForUid = new HashMap<>();
  static final Map<Integer, Integer> verificationResults = new HashMap<>();
  static final Map<Integer, Long> verificationTimeoutExtension = new HashMap<>();
  static final Map<String, String> currentToCanonicalNames = new HashMap<>();
  static final Map<String, String> canonicalToCurrentNames = new HashMap<>();
  static final Map<ComponentName, ComponentState> componentList = new LinkedHashMap<>();
  static final Map<ComponentName, Drawable> drawableList = new LinkedHashMap<>();
  static final Map<String, Drawable> applicationIcons = new HashMap<>();
  static final Map<String, Drawable> unbadgedApplicationIcons = new HashMap<>();
  static final Map<String, Boolean> systemFeatureList = new LinkedHashMap<>();
  static final SortedMap<ComponentName, List<IntentFilter>> preferredActivities = new TreeMap<>();
  static final SortedMap<ComponentName, List<IntentFilter>> persistentPreferredActivities =
      new TreeMap<>();
  static final Map<Pair<String, Integer>, Drawable> drawables = new LinkedHashMap<>();
  static final Map<String, Integer> applicationEnabledSettingMap = new HashMap<>();
  static Map<String, PermissionInfo> extraPermissions = new HashMap<>();
  static Map<String, PermissionGroupInfo> permissionGroups = new HashMap<>();
  public static Map<String, Resources> resources = new HashMap<>();
  static final Map<Intent, List<ResolveInfo>> resolveInfoForIntent =
      new TreeMap<>(new IntentComparator());
  static Set<String> deletedPackages = new HashSet<>();
  static Map<String, IPackageDeleteObserver> pendingDeleteCallbacks = new HashMap<>();
  static Set<String> hiddenPackages = new HashSet<>();
  static Multimap<Integer, String> sequenceNumberChangedPackagesMap = HashMultimap.create();
  static boolean canRequestPackageInstalls = false;
  static boolean safeMode = false;
  boolean shouldShowActivityChooser = false;
  static final Map<String, Integer> distractingPackageRestrictions = new ConcurrentHashMap<>();

  /**
   * Makes sure that given activity exists.
   *
   * If the activity doesn't exist yet, it will be created with {@code applicationInfo} set to an
   * existing application, or if it doesn't exist, a new package will be created.
   *
   * @return existing or newly created activity info.
   */
  public ActivityInfo addActivityIfNotPresent(ComponentName componentName) {
    return addComponent(
        activityFilters,
        p -> p.activities,
        (p, a) -> p.activities = a,
        updateName(componentName, new ActivityInfo()),
        false);
  }

  /**
   * Makes sure that given service exists.
   *
   * If the service doesn't exist yet, it will be created with {@code applicationInfo} set to an
   * existing application, or if it doesn't exist, a new package will be created.
   *
   * @return existing or newly created service info.
   */
  public ServiceInfo addServiceIfNotPresent(ComponentName componentName) {
    return addComponent(
        serviceFilters,
        p -> p.services,
        (p, a) -> p.services = a,
        updateName(componentName, new ServiceInfo()),
        false);
  }

  /**
   * Makes sure that given receiver exists.
   *
   * If the receiver doesn't exist yet, it will be created with {@code applicationInfo} set to an
   * existing application, or if it doesn't exist, a new package will be created.
   *
   * @return existing or newly created receiver info.
   */
  public ActivityInfo addReceiverIfNotPresent(ComponentName componentName) {
    return addComponent(
        receiverFilters,
        p -> p.receivers,
        (p, a) -> p.receivers = a,
        updateName(componentName, new ActivityInfo()),
        false);
  }

  /**
   * Makes sure that given provider exists.
   *
   * If the provider doesn't exist yet, it will be created with {@code applicationInfo} set to an
   * existing application, or if it doesn't exist, a new package will be created.
   *
   * @return existing or newly created provider info.
   */
  public ProviderInfo addProviderIfNotPresent(ComponentName componentName) {
    return addComponent(
        providerFilters,
        p -> p.providers,
        (p, a) -> p.providers = a,
        updateName(componentName, new ProviderInfo()),
        false);
  }

  private <C extends ComponentInfo> C updateName(ComponentName name, C component) {
    component.name = name.getClassName();
    component.packageName = name.getPackageName();
    if (component.applicationInfo != null) {
      component.applicationInfo.packageName = component.packageName;
    }
    return component;
  }

  /**
   * Adds or updates given activity in the system.
   *
   * If activity with the same {@link ComponentInfo#name} and {@code ComponentInfo#packageName}
   * exists it will be updated. Its {@link ComponentInfo#applicationInfo} is always set to {@link
   * ApplicationInfo} already existing in the system, but if no application exists a new one will
   * be created using {@link ComponentInfo#applicationInfo} in this component.
   */
  public void addOrUpdateActivity(ActivityInfo activityInfo) {
    addComponent(
        activityFilters,
        p -> p.activities,
        (p, a) -> p.activities = a,
        new ActivityInfo(activityInfo),
        true);
  }

  /**
   * Adds or updates given service in the system.
   *
   * If service with the same {@link ComponentInfo#name} and {@code ComponentInfo#packageName}
   * exists it will be updated. Its {@link ComponentInfo#applicationInfo} is always set to {@link
   * ApplicationInfo} already existing in the system, but if no application exists a new one will be
   * created using {@link ComponentInfo#applicationInfo} in this component.
   */
  public void addOrUpdateService(ServiceInfo serviceInfo) {
    addComponent(
        serviceFilters,
        p -> p.services,
        (p, a) -> p.services = a,
        new ServiceInfo(serviceInfo),
        true);
  }

  /**
   * Adds or updates given broadcast receiver in the system.
   *
   * If broadcast receiver with the same {@link ComponentInfo#name} and {@code
   * ComponentInfo#packageName} exists it will be updated. Its {@link ComponentInfo#applicationInfo}
   * is always set to {@link ApplicationInfo} already existing in the system, but if no
   * application exists a new one will be created using {@link ComponentInfo#applicationInfo} in
   * this component.
   */
  public void addOrUpdateReceiver(ActivityInfo receiverInfo) {
    addComponent(
        receiverFilters,
        p -> p.receivers,
        (p, a) -> p.receivers = a,
        new ActivityInfo(receiverInfo),
        true);
  }

  /**
   * Adds or updates given content provider in the system.
   *
   * If content provider with the same {@link ComponentInfo#name} and {@code
   * ComponentInfo#packageName} exists it will be updated. Its {@link ComponentInfo#applicationInfo}
   * is always set to {@link ApplicationInfo} already existing in the system, but if no
   * application exists a new one will be created using {@link ComponentInfo#applicationInfo} in
   * this component.
   */
  public void addOrUpdateProvider(ProviderInfo providerInfo) {
    addComponent(
        providerFilters,
        p -> p.providers,
        (p, a) -> p.providers = a,
        new ProviderInfo(providerInfo),
        true);
  }

  /**
   * Removes activity from the package manager.
   *
   * @return the removed component or {@code null} if no such component existed.
   */
  @Nullable
  public ActivityInfo removeActivity(ComponentName componentName) {
    return removeComponent(
        componentName, activityFilters, p -> p.activities, (p, a) -> p.activities = a);
  }

  /**
   * Removes service from the package manager.
   *
   * @return the removed component or {@code null} if no such component existed.
   */
  @Nullable
  public ServiceInfo removeService(ComponentName componentName) {
    return removeComponent(
        componentName, serviceFilters, p -> p.services, (p, a) -> p.services = a);
  }

  /**
   * Removes content provider from the package manager.
   *
   * @return the removed component or {@code null} if no such component existed.
   */
  @Nullable
  public ProviderInfo removeProvider(ComponentName componentName) {
    return removeComponent(
        componentName, providerFilters, p -> p.providers, (p, a) -> p.providers = a);
  }

  /**
   * Removes broadcast receiver from the package manager.
   *
   * @return the removed component or {@code null} if no such component existed.
   */
  @Nullable
  public ActivityInfo removeReceiver(ComponentName componentName) {
    return removeComponent(
        componentName, receiverFilters, p -> p.receivers, (p, a) -> p.receivers = a);
  }

  private <C extends ComponentInfo> C addComponent(
      SortedMap<ComponentName, List<IntentFilter>> filtersMap,
      Function<PackageInfo, C[]> componentArrayInPackage,
      BiConsumer<PackageInfo, C[]> componentsSetter,
      C newComponent,
      boolean updateIfExists) {
    String packageName = newComponent.packageName;
    if (packageName == null && newComponent.applicationInfo != null) {
      packageName = newComponent.applicationInfo.packageName;
    }
    if (packageName == null) {
      throw new IllegalArgumentException("Component needs a package name");
    }
    if (newComponent.name == null) {
      throw new IllegalArgumentException("Component needs a name");
    }
    PackageInfo packageInfo = packageInfos.get(packageName);
    if (packageInfo == null) {
      packageInfo = new PackageInfo();
      packageInfo.packageName = packageName;
      packageInfo.applicationInfo = newComponent.applicationInfo;
      installPackage(packageInfo);
      packageInfo = packageInfos.get(packageName);
    }
    newComponent.applicationInfo = packageInfo.applicationInfo;
    C[] components = componentArrayInPackage.apply(packageInfo);
    if (components == null) {
      @SuppressWarnings("unchecked")
      C[] newComponentArray = (C[]) Array.newInstance(newComponent.getClass(), 0);
      components = newComponentArray;
    } else {
      for (int i = 0; i < components.length; i++) {
        if (newComponent.name.equals(components[i].name)) {
          if (updateIfExists) {
            components[i] = newComponent;
          }
          return components[i];
        }
      }
    }
    components = Arrays.copyOf(components, components.length + 1);
    componentsSetter.accept(packageInfo, components);
    components[components.length - 1] = newComponent;

    filtersMap.put(
        new ComponentName(newComponent.packageName, newComponent.name), new ArrayList<>());
    return newComponent;
  }

  @Nullable
  private <C extends ComponentInfo> C removeComponent(
      ComponentName componentName,
      SortedMap<ComponentName, List<IntentFilter>> filtersMap,
      Function<PackageInfo, C[]> componentArrayInPackage,
      BiConsumer<PackageInfo, C[]> componentsSetter) {
    filtersMap.remove(componentName);
    String packageName = componentName.getPackageName();
    PackageInfo packageInfo = packageInfos.get(packageName);
    if (packageInfo == null) {
      return null;
    }
    C[] components = componentArrayInPackage.apply(packageInfo);
    if (components == null) {
      return null;
    }
    for (int i = 0; i < components.length; i++) {
      C component = components[i];
      if (componentName.getClassName().equals(component.name)) {
        C[] newComponents;
        if (components.length == 1) {
          newComponents = null;
        } else {
          newComponents = Arrays.copyOf(components, components.length - 1);
          System.arraycopy(components, i + 1, newComponents, i, components.length - i - 1);
        }
        componentsSetter.accept(packageInfo, newComponents);
        return component;
      }
    }
    return null;
  }

  /**
   * Settings for a particular package.
   *
   * This class mirrors {@link com.android.server.pm.PackageSetting}, which is used by {@link
   * PackageManager}.
   */
  public static class PackageSetting {

    /** Whether the package is suspended in {@link PackageManager}. */
    private boolean suspended = false;

    /** The message to be displayed to the user when they try to launch the app. */
    private String dialogMessage = null;

    /**
     * The info for how to display the dialog that shows to the user when they try to launch the
     * app. On Q, one of this field or dialogMessage will be present when a package is suspended.
     */
    private Object dialogInfo = null;

    /** An optional {@link PersistableBundle} shared with the app. */
    private PersistableBundle suspendedAppExtras = null;

    /** An optional {@link PersistableBundle} shared with the launcher. */
    private PersistableBundle suspendedLauncherExtras = null;

    public PackageSetting() {}

    public PackageSetting(PackageSetting that) {
      this.suspended = that.suspended;
      this.dialogMessage = that.dialogMessage;
      this.dialogInfo = that.dialogInfo;
      this.suspendedAppExtras = deepCopyNullablePersistableBundle(that.suspendedAppExtras);
      this.suspendedLauncherExtras =
          deepCopyNullablePersistableBundle(that.suspendedLauncherExtras);
    }

    /**
     * Sets the suspension state of the package.
     *
     * <p>If {@code suspended} is false, {@code dialogInfo}, {@code appExtras}, and {@code
     * launcherExtras} will be ignored.
     */
    void setSuspended(
        boolean suspended,
        String dialogMessage,
        /* SuspendDialogInfo */ Object dialogInfo,
        PersistableBundle appExtras,
        PersistableBundle launcherExtras) {
      Preconditions.checkArgument(dialogMessage == null || dialogInfo == null);
      this.suspended = suspended;
      this.dialogMessage = suspended ? dialogMessage : null;
      this.dialogInfo = suspended ? dialogInfo : null;
      this.suspendedAppExtras = suspended ? deepCopyNullablePersistableBundle(appExtras) : null;
      this.suspendedLauncherExtras =
          suspended ? deepCopyNullablePersistableBundle(launcherExtras) : null;
    }

    public boolean isSuspended() {
      return suspended;
    }

    public String getDialogMessage() {
      return dialogMessage;
    }

    public Object getDialogInfo() {
      return dialogInfo;
    }

    public PersistableBundle getSuspendedAppExtras() {
      return suspendedAppExtras;
    }

    public PersistableBundle getSuspendedLauncherExtras() {
      return suspendedLauncherExtras;
    }

    private static PersistableBundle deepCopyNullablePersistableBundle(PersistableBundle bundle) {
      return bundle == null ? null : bundle.deepCopy();
    }

  }

  static final Map<String, PackageSetting> packageSettings = new HashMap<>();

  // From com.android.server.pm.PackageManagerService.compareSignatures().
  static int compareSignature(Signature[] signatures1, Signature[] signatures2) {
    if (signatures1 == null) {
      return (signatures2 == null) ? SIGNATURE_NEITHER_SIGNED : SIGNATURE_FIRST_NOT_SIGNED;
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

  // TODO(christianw): reconcile with AndroidTestEnvironment.setUpPackageStorage
  private static void setUpPackageStorage(ApplicationInfo applicationInfo) {
    if (applicationInfo.sourceDir == null) {
      applicationInfo.sourceDir = createTempDir(applicationInfo.packageName + "-sourceDir");
    }

    if (applicationInfo.dataDir == null) {
      applicationInfo.dataDir = createTempDir(applicationInfo.packageName + "-dataDir");
    }
    if (applicationInfo.publicSourceDir == null) {
      applicationInfo.publicSourceDir = applicationInfo.sourceDir;
    }
    if (RuntimeEnvironment.getApiLevel() >= N) {
      applicationInfo.credentialProtectedDataDir = createTempDir("userDataDir");
      applicationInfo.deviceProtectedDataDir = createTempDir("deviceDataDir");
    }
  }

  private static String createTempDir(String name) {
    return RuntimeEnvironment.getTempDirectory()
        .createIfNotExists(name)
        .toAbsolutePath()
        .toString();
  }

  /**
   * Sets extra resolve infos for an intent.
   *
   * Those entries are added to whatever might be in the manifest already.
   *
   * Note that all resolve infos will have {@link ResolveInfo#isDefault} field set to {@code
   * true} to allow their resolution for implicit intents. If this is not what you want, then you
   * still have the reference to those ResolveInfos, and you can set the field back to {@code
   * false}.
   *
   * @deprecated see the note on {@link #addResolveInfoForIntent(Intent, ResolveInfo)}.
   */
  @Deprecated
  public void setResolveInfosForIntent(Intent intent, List<ResolveInfo> info) {
    resolveInfoForIntent.remove(intent);
    for (ResolveInfo resolveInfo : info) {
      addResolveInfoForIntent(intent, resolveInfo);
    }
  }

  /** @deprecated see note on {@link #addResolveInfoForIntent(Intent, ResolveInfo)}. */
  @Deprecated
  public void addResolveInfoForIntent(Intent intent, List<ResolveInfo> info) {
    setResolveInfosForIntent(intent, info);
  }

  /**
   * Adds extra resolve info for an intent.
   *
   * Note that this resolve info will have {@link ResolveInfo#isDefault} field set to {@code
   * true} to allow its resolution for implicit intents. If this is not what you want, then please
   * use {@link #addResolveInfoForIntentNoDefaults} instead.
   *
   * @deprecated use {@link #addIntentFilterForComponent} instead and if the component doesn't exist
   *     add it using any of {@link #installPackage}, {@link #addOrUpdateActivity}, {@link
   *     #addActivityIfNotPresent} or their counterparts for other types of components.
   */
  @Deprecated
  public void addResolveInfoForIntent(Intent intent, ResolveInfo info) {
    info.isDefault = true;
    ComponentInfo[] componentInfos =
        new ComponentInfo[] {
          info.activityInfo,
          info.serviceInfo,
          Build.VERSION.SDK_INT >= KITKAT ? info.providerInfo : null
        };
    for (ComponentInfo component : componentInfos) {
      if (component != null && component.applicationInfo != null) {
        component.applicationInfo.flags |= ApplicationInfo.FLAG_INSTALLED;
        if (component.applicationInfo.processName == null) {
          component.applicationInfo.processName = component.applicationInfo.packageName;
        }
      }
    }
    if (info.match == 0) {
      info.match = Integer.MAX_VALUE; // make sure, that this is as good match as possible.
    }
    addResolveInfoForIntentNoDefaults(intent, info);
  }

  /**
   * Adds the {@code info} as {@link ResolveInfo} for the intent but without applying any default
   * values.
   *
   * In particular it will not make the {@link ResolveInfo#isDefault} field {@code true}, that
   * means that this resolve info will not resolve for {@link Intent#resolveActivity} and {@link
   * Context#startActivity}.
   *
   * @deprecated see the note on {@link #addResolveInfoForIntent(Intent, ResolveInfo)}.
   */
  @Deprecated
  public void addResolveInfoForIntentNoDefaults(Intent intent, ResolveInfo info) {
    Preconditions.checkNotNull(info);
    List<ResolveInfo> infoList = resolveInfoForIntent.get(intent);
    if (infoList == null) {
      infoList = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList);
    }
    infoList.add(info);
  }

  /**
   * Removes {@link ResolveInfo}s registered using {@link #addResolveInfoForIntent}.
   *
   * @deprecated see note on {@link #addResolveInfoForIntent(Intent, ResolveInfo)}.
   */
  @Deprecated
  public void removeResolveInfosForIntent(Intent intent, String packageName) {
    List<ResolveInfo> infoList = resolveInfoForIntent.get(intent);
    if (infoList == null) {
      infoList = new ArrayList<>();
      resolveInfoForIntent.put(intent, infoList);
    }

    for (Iterator<ResolveInfo> iterator = infoList.iterator(); iterator.hasNext(); ) {
      ResolveInfo resolveInfo = iterator.next();
      if (getPackageName(resolveInfo).equals(packageName)) {
        iterator.remove();
      }
    }
  }

  private static String getPackageName(ResolveInfo resolveInfo) {
    if (resolveInfo.resolvePackageName != null) {
      return resolveInfo.resolvePackageName;
    } else if (resolveInfo.activityInfo != null) {
      return resolveInfo.activityInfo.packageName;
    } else if (resolveInfo.serviceInfo != null) {
      return resolveInfo.serviceInfo.packageName;
    } else if (resolveInfo.providerInfo != null) {
      return resolveInfo.providerInfo.packageName;
    }
    throw new IllegalStateException(
        "Could not find package name for ResolveInfo " + resolveInfo.toString());
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

  public void setUnbadgedApplicationIcon(String packageName, Drawable drawable) {
    unbadgedApplicationIcons.put(packageName, drawable);
  }

  /**
   * Return the flags set in call to {@link
   * android.app.ApplicationPackageManager#setComponentEnabledSetting(ComponentName, int, int)}.
   *
   * @param componentName The component name.
   * @return The flags.
   */
  public int getComponentEnabledSettingFlags(ComponentName componentName) {
    ComponentState state = componentList.get(componentName);
    return state != null ? state.flags : 0;
  }

  /**
   * Installs a module with the {@link PackageManager} as long as it is not {@code null}
   *
   * <p>In order to create ModuleInfo objects in a valid state please use {@link ModuleInfoBuilder}.
   */
  public void installModule(Object moduleInfoObject) {
    ModuleInfo moduleInfo = (ModuleInfo) moduleInfoObject;
    if (moduleInfo != null) {
      moduleInfos.put(moduleInfo.getPackageName(), moduleInfo);
      // Checking to see if package exists in the system
      if (packageInfos.get(moduleInfo.getPackageName()) == null) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.packageName = moduleInfo.getPackageName();
        applicationInfo.name = moduleInfo.getName().toString();

        PackageInfo packageInfo = new PackageInfo();
        packageInfo.applicationInfo = applicationInfo;
        packageInfo.packageName = moduleInfo.getPackageName();
        installPackage(packageInfo);
      }
    }
  }

  /**
   * Deletes a module when given the module's package name {@link ModuleInfo} be sure to give the
   * correct name as this method does not ensure existence of the module before deletion. Since
   * module installation ensures that a package exists in the device, also delete the package for
   * full deletion.
   *
   * @param packageName should be the value of {@link ModuleInfo#getPackageName}.
   * @return deleted module of {@code null} if no module with this name exists.
   */
  public Object deleteModule(String packageName) {
    // Removes the accompanying package installed with the module
    return moduleInfos.remove(packageName);
  }

  /**
   * Installs a package with the {@link PackageManager}.
   *
   * In order to create PackageInfo objects in a valid state please use {@link
   * androidx.test.core.content.pm.PackageInfoBuilder}.
   *
   * This method automatically simulates instalation of a package in the system, so it adds a
   * flag {@link ApplicationInfo#FLAG_INSTALLED} to the application info and makes sure it exits. It
   * will update applicationInfo in package components as well.
   *
   * If you don't want the package to be installed, use {@link #addPackageNoDefaults} instead.
   */
  public void installPackage(PackageInfo packageInfo) {
    ApplicationInfo appInfo = packageInfo.applicationInfo;
    if (appInfo == null) {
      appInfo = new ApplicationInfo();
      packageInfo.applicationInfo = appInfo;
    }
    if (appInfo.packageName == null) {
      appInfo.packageName = packageInfo.packageName;
    }
    if (appInfo.processName == null) {
      appInfo.processName = appInfo.packageName;
    }
    appInfo.flags |= ApplicationInfo.FLAG_INSTALLED;
    ComponentInfo[][] componentInfoArrays =
        new ComponentInfo[][] {
          packageInfo.activities,
          packageInfo.services,
          packageInfo.providers,
          packageInfo.receivers,
        };
    int uniqueNameCounter = 0;
    for (ComponentInfo[] componentInfos : componentInfoArrays) {
      if (componentInfos == null) {
        continue;
      }
      for (ComponentInfo componentInfo : componentInfos) {
        if (componentInfo.name == null) {
          componentInfo.name = appInfo.packageName + ".DefaultName" + uniqueNameCounter++;
          componentInfo.packageName = packageInfo.packageName;
        }
        componentInfo.applicationInfo = appInfo;
        componentInfo.packageName = appInfo.packageName;
        if (componentInfo.processName == null) {
          componentInfo.processName = appInfo.processName;
        }
      }
    }
    addPackageNoDefaults(packageInfo);
  }

  /**
   * Adds a package to the {@link PackageManager}, but doesn't set any default values on it.
   *
   * Right now it will not set {@link ApplicationInfo#FLAG_INSTALLED} flag on its application, so
   * if not set explicitly, it will be treated as not installed.
   */
  public void addPackageNoDefaults(PackageInfo packageInfo) {
    PackageStats packageStats = new PackageStats(packageInfo.packageName);
    addPackage(packageInfo, packageStats);
  }

  /**
   * Installs a package with its stats with the {@link PackageManager}.
   *
   * This method doesn't add any defaults to the {@code packageInfo} parameters. You should make
   * sure it is valid (see {@link #installPackage(PackageInfo)}).
   */
  public synchronized void addPackage(PackageInfo packageInfo, PackageStats packageStats) {
    if (packageInfo.applicationInfo != null
        && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_INSTALLED) == 0) {
      Log.w(TAG, "Adding not installed package: " + packageInfo.packageName);
    }
    Preconditions.checkArgument(packageInfo.packageName.equals(packageStats.packageName));

    packageInfos.put(packageInfo.packageName, packageInfo);
    packageStatsMap.put(packageInfo.packageName, packageStats);

    packageSettings.put(packageInfo.packageName, new PackageSetting());

    applicationEnabledSettingMap.put(
        packageInfo.packageName, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
    if (packageInfo.applicationInfo != null) {
      namesForUid.put(packageInfo.applicationInfo.uid, packageInfo.packageName);
    }
  }

  /** @deprecated Use {@link #installPackage(PackageInfo)} instead. */
  @Deprecated
  public void addPackage(String packageName) {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;

    ApplicationInfo applicationInfo = new ApplicationInfo();

    applicationInfo.packageName = packageName;
    // TODO: setUpPackageStorage should be in installPackage but we need to fix all tests first
    setUpPackageStorage(applicationInfo);
    packageInfo.applicationInfo = applicationInfo;
    installPackage(packageInfo);
  }

  /** This method is getting renamed to {link {@link #installPackage}. */
  @Deprecated
  public void addPackage(PackageInfo packageInfo) {
    installPackage(packageInfo);
  }

  /**
   * Testing API allowing to retrieve internal package representation.
   *
   * This will allow to modify the package in a way visible to Robolectric, as this is
   * Robolectric's internal full package representation.
   *
   * Note that maybe a better way is to just modify the test manifest to make those modifications
   * in a standard way.
   *
   * Retrieving package info using {@link PackageManager#getPackageInfo} / {@link
   * PackageManager#getApplicationInfo} will return defensive copies that will be stripped out of
   * information according to provided flags. Don't use it to modify Robolectric state.
   */
  public PackageInfo getInternalMutablePackageInfo(String packageName) {
    return packageInfos.get(packageName);
  }

  /** @deprecated Use {@link #getInternalMutablePackageInfo} instead. It has better name. */
  @Deprecated
  public PackageInfo getPackageInfoForTesting(String packageName) {
    return getInternalMutablePackageInfo(packageName);
  }

  public void addPermissionInfo(PermissionInfo permissionInfo) {
    extraPermissions.put(permissionInfo.name, permissionInfo);
  }

  /**
   * Adds {@code packageName} to the list of changed packages for the particular {@code
   * sequenceNumber}.
   *
   * @param sequenceNumber has to be >= 0
   * @param packageName name of the package that was changed
   */
  public void addChangedPackage(int sequenceNumber, String packageName) {
    if (sequenceNumber < 0) {
      return;
    }
    sequenceNumberChangedPackagesMap.put(sequenceNumber, packageName);
  }

  /**
   * Allows overriding or adding permission-group elements. These would be otherwise specified by
   * either (the
   * system)[https://developer.android.com/guide/topics/permissions/requesting.html#perm-groups] or
   * by (the app
   * itself)[https://developer.android.com/guide/topics/manifest/permission-group-element.html], as
   * part of its manifest
   *
   * {@link android.content.pm.PackageParser.PermissionGroup}s added through this method have
   * precedence over those specified with the same name by one of the aforementioned methods.
   *
   * @see PackageManager#getAllPermissionGroups(int)
   * @see PackageManager#getPermissionGroupInfo(String, int)
   */
  public void addPermissionGroupInfo(PermissionGroupInfo permissionGroupInfo) {
    permissionGroups.put(permissionGroupInfo.name, permissionGroupInfo);
  }

  public void removePackage(String packageName) {
    packageInfos.remove(packageName);

    packageSettings.remove(packageName);
  }

  public void setSystemFeature(String name, boolean supported) {
    systemFeatureList.put(name, supported);
  }

  public void addDrawableResolution(String packageName, int resourceId, Drawable drawable) {
    drawables.put(new Pair(packageName, resourceId), drawable);
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

  @Implementation
  @Nullable
  protected String[] getPackagesForUid(int uid) {
    return packagesForUid.get(uid);
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

  /** Adds a value to be returned by {@link PackageManager#getSystemSharedLibraryNames()}. */
  public void addSystemSharedLibraryName(String name) {
    systemSharedLibraryNames.add(name);
  }

  /** Clears the values returned by {@link PackageManager#getSystemSharedLibraryNames()}. */
  public void clearSystemSharedLibraryNames() {
    systemSharedLibraryNames.clear();
  }

  @Deprecated
  /** @deprecated use {@link #addCanonicalName} instead.} */
  public void addCurrentToCannonicalName(String currentName, String canonicalName) {
    currentToCanonicalNames.put(currentName, canonicalName);
  }

  /**
   * Adds a canonical package name for a package.
   *
   * <p>This will be reflected when calling {@link
   * PackageManager#currentToCanonicalPackageNames(String[])} or {@link
   * PackageManager#canonicalToCurrentPackageNames(String[])} (String[])}.
   */
  public void addCanonicalName(String currentName, String canonicalName) {
    currentToCanonicalNames.put(currentName, canonicalName);
    canonicalToCurrentNames.put(canonicalName, currentName);
  }

  /**
   * Sets if the {@link PackageManager} is allowed to request package installs through package
   * installer.
   */
  public void setCanRequestPackageInstalls(boolean canRequestPackageInstalls) {
    ShadowPackageManager.canRequestPackageInstalls = canRequestPackageInstalls;
  }

  @Implementation(minSdk = N)
  protected List<ResolveInfo> queryBroadcastReceiversAsUser(
      Intent intent, int flags, UserHandle userHandle) {
    return null;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected List<ResolveInfo> queryBroadcastReceivers(
      Intent intent, int flags, @UserIdInt int userId) {
    return null;
  }

  @Implementation
  protected PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
    if (packageArchiveInfo.containsKey(archiveFilePath)) {
      return packageArchiveInfo.get(archiveFilePath);
    }

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

    return Shadow.directlyOn(realPackageManager, PackageManager.class).getPackageArchiveInfo(
        archiveFilePath, flags);
  }

  @Implementation
  protected void freeStorageAndNotify(long freeStorageSize, IPackageDataObserver observer) {}

  @Implementation
  protected void freeStorage(long freeStorageSize, IntentSender pi) {}

  /**
   * Uninstalls the package from the system in a way, that will allow its discovery through {@link
   * PackageManager#MATCH_UNINSTALLED_PACKAGES}.
   */
  public void deletePackage(String packageName) {
    deletedPackages.add(packageName);
    packageInfos.remove(packageName);
    mapForPackage(activityFilters, packageName).clear();
    mapForPackage(serviceFilters, packageName).clear();
    mapForPackage(providerFilters, packageName).clear();
    mapForPackage(receiverFilters, packageName).clear();
    moduleInfos.remove(packageName);
  }

  protected void deletePackage(String packageName, IPackageDeleteObserver observer, int flags) {
    pendingDeleteCallbacks.put(packageName, observer);
  }

  /**
   * Runs the callbacks pending from calls to {@link PackageManager#deletePackage(String,
   * IPackageDeleteObserver, int)}
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
        deletePackage(packageName);
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
   * Returns package names successfully deleted with {@link PackageManager#deletePackage(String,
   * IPackageDeleteObserver, int)} Note that like real {@link PackageManager} the calling context
   * must have {@link android.Manifest.permission#DELETE_PACKAGES} permission set.
   */
  public Set<String> getDeletedPackages() {
    return deletedPackages;
  }

  protected List<ResolveInfo> queryOverriddenIntents(Intent intent, int flags) {
    List<ResolveInfo> overrides = resolveInfoForIntent.get(intent);
    if (overrides == null) {
      return Collections.emptyList();
    }
    List<ResolveInfo> result = new ArrayList<>(overrides.size());
    for (ResolveInfo resolveInfo : overrides) {
      result.add(ShadowResolveInfo.newResolveInfo(resolveInfo));
    }
    return result;
  }

  /**
   * Internal use only.
   *
   * @param appPackage
   */
  public void addPackageInternal(Package appPackage) {
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

    for (PermissionGroup permissionGroup : appPackage.permissionGroups) {
      PermissionGroupInfo permissionGroupInfo =
          PackageParser.generatePermissionGroupInfo(permissionGroup, flags);
      addPermissionGroupInfo(permissionGroupInfo);
    }
    PackageInfo packageInfo =
        reflector(_PackageParser_.class)
            .generatePackageInfo(appPackage, new int[] {0}, flags, 0, 0);

    packageInfo.applicationInfo.uid = Process.myUid();
    packageInfo.applicationInfo.dataDir = createTempDir(packageInfo.packageName + "-dataDir");
    installPackage(packageInfo);
    addFilters(activityFilters, appPackage.activities);
    addFilters(serviceFilters, appPackage.services);
    addFilters(providerFilters, appPackage.providers);
    addFilters(receiverFilters, appPackage.receivers);
  }

  private void addFilters(
      Map<ComponentName, List<IntentFilter>> componentMap,
      List<? extends PackageParser.Component<?>> components) {
    if (components == null) {
      return;
    }
    for (Component<?> component : components) {
      ComponentName componentName = component.getComponentName();
      List<IntentFilter> registeredFilters = componentMap.get(componentName);
      if (registeredFilters == null) {
        registeredFilters = new ArrayList<>();
        componentMap.put(componentName, registeredFilters);
      }
      for (IntentInfo intentInfo : component.intents) {
        registeredFilters.add(new IntentFilter(intentInfo));
      }
    }
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

  /**
   * Compares {@link ResolveInfo}s, ordering better matches before worse ones. This is the order in
   * which resolve infos should be returned to the user.
   */
  static class ResolveInfoComparator implements Comparator<ResolveInfo> {

    @Override
    public int compare(ResolveInfo o1, ResolveInfo o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      if (o1.preferredOrder != o2.preferredOrder) {
        // higher priority is before lower
        return -Integer.compare(o1.preferredOrder, o2.preferredOrder);
      }
      if (o1.priority != o2.priority) {
        // higher priority is before lower
        return -Integer.compare(o1.priority, o2.priority);
      }
      if (o1.match != o2.match) {
        // higher match is before lower
        return -Integer.compare(o1.match, o2.match);
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
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public List<IntentFilter> getIntentFiltersForActivity(ComponentName componentName)
      throws NameNotFoundException {
    return getIntentFiltersForComponent(componentName, activityFilters);
  }

  /**
   * Get list of intent filters defined for given service.
   *
   * @param componentName Name of the service whose intent filters are to be retrieved
   * @return the service's intent filters
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public List<IntentFilter> getIntentFiltersForService(ComponentName componentName)
      throws NameNotFoundException {
    return getIntentFiltersForComponent(componentName, serviceFilters);
  }

  /**
   * Get list of intent filters defined for given receiver.
   *
   * @param componentName Name of the receiver whose intent filters are to be retrieved
   * @return the receiver's intent filters
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public List<IntentFilter> getIntentFiltersForReceiver(ComponentName componentName)
      throws NameNotFoundException {
      return getIntentFiltersForComponent(componentName, receiverFilters);
  }

  /**
   * Get list of intent filters defined for given provider.
   *
   * @param componentName Name of the provider whose intent filters are to be retrieved
   * @return the provider's intent filters
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public List<IntentFilter> getIntentFiltersForProvider(ComponentName componentName)
      throws NameNotFoundException {
    return getIntentFiltersForComponent(componentName, providerFilters);
  }

  /**
   * Add intent filter for given activity.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void addIntentFilterForActivity(ComponentName componentName, IntentFilter filter)
      throws NameNotFoundException {
    addIntentFilterForComponent(componentName, filter, activityFilters);
  }

  /**
   * Add intent filter for given service.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void addIntentFilterForService(ComponentName componentName, IntentFilter filter)
      throws NameNotFoundException {
    addIntentFilterForComponent(componentName, filter, serviceFilters);
  }

  /**
   * Add intent filter for given receiver.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void addIntentFilterForReceiver(ComponentName componentName, IntentFilter filter)
      throws NameNotFoundException {
    addIntentFilterForComponent(componentName, filter, receiverFilters);
  }

  /**
   * Add intent filter for given provider.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void addIntentFilterForProvider(ComponentName componentName, IntentFilter filter)
      throws NameNotFoundException {
    addIntentFilterForComponent(componentName, filter, providerFilters);
  }

  /**
   * Clears intent filters for given activity.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void clearIntentFilterForActivity(ComponentName componentName)
      throws NameNotFoundException {
    clearIntentFilterForComponent(componentName, activityFilters);
  }

  /**
   * Clears intent filters for given service.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void clearIntentFilterForService(ComponentName componentName)
      throws NameNotFoundException {
    clearIntentFilterForComponent(componentName, serviceFilters);
  }

  /**
   * Clears intent filters for given receiver.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void clearIntentFilterForReceiver(ComponentName componentName)
      throws NameNotFoundException {
    clearIntentFilterForComponent(componentName, receiverFilters);
  }

  /**
   * Clears intent filters for given provider.
   *
   * @throws NameNotFoundException if component with given name doesn't exist.
   */
  public void clearIntentFilterForProvider(ComponentName componentName)
      throws NameNotFoundException {
    clearIntentFilterForComponent(componentName, providerFilters);
  }

  private void addIntentFilterForComponent(
      ComponentName componentName,
      IntentFilter filter,
      Map<ComponentName, List<IntentFilter>> filterMap)
      throws NameNotFoundException {
    // Existing components should have an entry in respective filterMap.
    // It is OK to search over all filter maps, as it is impossible to have the same component name
    // being of two comopnent types (like activity and service at the same time).
    List<IntentFilter> filters = filterMap.get(componentName);
    if (filters != null) {
      filters.add(filter);
      return;
    }
    throw new NameNotFoundException(componentName + " doesn't exist");
  }

  private void clearIntentFilterForComponent(
      ComponentName componentName, Map<ComponentName, List<IntentFilter>> filterMap)
      throws NameNotFoundException {
    List<IntentFilter> filters = filterMap.get(componentName);
    if (filters != null) {
      filters.clear();
      return;
    }
    throw new NameNotFoundException(componentName + " doesn't exist");
  }

  private List<IntentFilter> getIntentFiltersForComponent(
      ComponentName componentName, Map<ComponentName, List<IntentFilter>> filterMap)
      throws NameNotFoundException {
    List<IntentFilter> filters = filterMap.get(componentName);
    if (filters != null) {
      return new ArrayList<>(filters);
    }
    throw new NameNotFoundException(componentName + " doesn't exist");
  }

  /**
   * Method to retrieve persistent preferred activities as set by {@link
   * android.app.admin.DevicePolicyManager#addPersistentPreferredActivity}.
   *
   * Works the same way as analogous {@link PackageManager#getPreferredActivities} for regular
   * preferred activities.
   */
  public int getPersistentPreferredActivities(
      List<IntentFilter> outFilters, List<ComponentName> outActivities, String packageName) {
    return getPreferredActivitiesInternal(
        outFilters, outActivities, packageName, persistentPreferredActivities);
  }

  protected static int getPreferredActivitiesInternal(
      List<IntentFilter> outFilters,
      List<ComponentName> outActivities,
      String packageName,
      SortedMap<ComponentName, List<IntentFilter>> preferredActivitiesMap) {
    SortedMap<ComponentName, List<IntentFilter>> preferredMap = preferredActivitiesMap;
    if (packageName != null) {
      preferredMap = mapForPackage(preferredActivitiesMap, packageName);
    }
    int result = 0;
    for (Entry<ComponentName, List<IntentFilter>> entry : preferredMap.entrySet()) {
      int filterCount = entry.getValue().size();
      result += filterCount;
      ComponentName[] componentNames = new ComponentName[filterCount];
      Arrays.fill(componentNames, entry.getKey());
      outActivities.addAll(asList(componentNames));
      outFilters.addAll(entry.getValue());
    }

    return result;
  }

  void clearPackagePersistentPreferredActivities(String packageName) {
    clearPackagePreferredActivitiesInternal(packageName, persistentPreferredActivities);
  }

  protected static void clearPackagePreferredActivitiesInternal(
      String packageName, SortedMap<ComponentName, List<IntentFilter>> preferredActivitiesMap) {
    mapForPackage(preferredActivitiesMap, packageName).clear();
  }

  void addPersistentPreferredActivity(IntentFilter filter, ComponentName activity) {
    addPreferredActivityInternal(filter, activity, persistentPreferredActivities);
  }

  protected static void addPreferredActivityInternal(
      IntentFilter filter,
      ComponentName activity,
      SortedMap<ComponentName, List<IntentFilter>> preferredActivitiesMap) {
    List<IntentFilter> filters = preferredActivitiesMap.get(activity);
    if (filters == null) {
      filters = new ArrayList<>();
      preferredActivitiesMap.put(activity, filters);
    }
    filters.add(filter);
  }

  protected static <V> SortedMap<ComponentName, V> mapForPackage(
      SortedMap<ComponentName, V> input, @Nullable String packageName) {
    if (packageName == null) {
      return input;
    }
    if (packageName == null) {
      return input;
    }
    return input.subMap(
        new ComponentName(packageName, ""), new ComponentName(packageName + " ", ""));
  }

  static boolean isComponentEnabled(@Nullable ComponentInfo componentInfo) {
    if (componentInfo == null) {
      return true;
    }
    if (componentInfo.applicationInfo == null
        || componentInfo.applicationInfo.packageName == null
        || componentInfo.name == null) {
      return componentInfo.enabled;
    }
    ComponentName name =
        new ComponentName(componentInfo.applicationInfo.packageName, componentInfo.name);
    ComponentState componentState = componentList.get(name);
    if (componentState == null
        || componentState.newState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
      return componentInfo.enabled;
    }
    return componentState.newState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
  }

  /**
   * Returns the current {@link PackageSetting} of {@code packageName}.
   *
   * If {@code packageName} is not present in this {@link ShadowPackageManager}, this method will
   * return null.
   */
  public PackageSetting getPackageSetting(String packageName) {
    PackageSetting setting = packageSettings.get(packageName);
    return setting == null ? null : new PackageSetting(setting);
  }

  /**
   * If this method has been called with true, then in cases where many activities match a filter,
   * an activity chooser will be resolved instead of just the first pick.
   */
  public void setShouldShowActivityChooser(boolean shouldShowActivityChooser) {
    this.shouldShowActivityChooser = shouldShowActivityChooser;
  }

  /** Set value to be returned by {@link PackageManager#isSafeMode}. */
  public void setSafeMode(boolean safeMode) {
    ShadowPackageManager.safeMode = safeMode;
  }

  /**
   * Returns the last value provided to {@code setDistractingPackageRestrictions} for {@code pkg}.
   *
   * Defaults to {@code PackageManager.RESTRICTION_NONE} if {@code
   * setDistractingPackageRestrictions} has not been called for {@code pkg}.
   */
  public int getDistractingPackageRestrictions(String pkg) {
    return distractingPackageRestrictions.getOrDefault(pkg, PackageManager.RESTRICTION_NONE);
  }

  @Resetter
  public static void reset() {
    permissionRationaleMap.clear();
    systemAvailableFeatures.clear();
    systemSharedLibraryNames.clear();
    packageInfos.clear();
    packageArchiveInfo.clear();
    packageStatsMap.clear();
    packageInstallerMap.clear();
    packagesForUid.clear();
    uidForPackage.clear();
    namesForUid.clear();
    verificationResults.clear();
    verificationTimeoutExtension.clear();
    currentToCanonicalNames.clear();
    canonicalToCurrentNames.clear();
    componentList.clear();
    drawableList.clear();
    applicationIcons.clear();
    unbadgedApplicationIcons.clear();
    systemFeatureList.clear();
    preferredActivities.clear();
    persistentPreferredActivities.clear();
    drawables.clear();
    applicationEnabledSettingMap.clear();
    extraPermissions.clear();
    permissionGroups.clear();
    resources.clear();
    resolveInfoForIntent.clear();
    deletedPackages.clear();
    pendingDeleteCallbacks.clear();
    hiddenPackages.clear();
    sequenceNumberChangedPackagesMap.clear();
    activityFilters.clear();
    serviceFilters.clear();
    providerFilters.clear();
    receiverFilters.clear();
    packageSettings.clear();
    safeMode = false;
  }
}
