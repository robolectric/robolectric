package org.robolectric.shadows;

import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_HOME;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
import static android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.accounts.Account;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.ApplicationPackageManager;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.app.admin.DevicePolicyManager.NearbyStreamingPolicy;
import android.app.admin.DevicePolicyManager.PasswordComplexity;
import android.app.admin.DevicePolicyManager.UserProvisioningState;
import android.app.admin.DevicePolicyState;
import android.app.admin.IDevicePolicyManager;
import android.app.admin.SystemUpdateInfo;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import com.android.internal.util.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link DevicePolicyManager}. */
@Implements(DevicePolicyManager.class)
@SuppressLint("NewApi")
public class ShadowDevicePolicyManager {
  /**
   * @see
   *     https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#setOrganizationColor(android.content.ComponentName,
   *     int)
   */
  private final Map<PackageAndPermission, Boolean> appPermissionGrantedMap = new HashMap<>();

  private final Map<PackageAndPermission, Integer> appPermissionGrantStateMap = new HashMap<>();
  private final Map<ComponentName, byte[]> passwordResetTokens = new HashMap<>();
  private final List<ComponentName> deviceAdmins = new ArrayList<>();
  private final Map<Integer, String> profileOwnerNamesMap = new HashMap<>();
  private final Map<String, Bundle> applicationRestrictionsMap = new HashMap<>();
  private List<String> permittedAccessibilityServices = new ArrayList<>();
  private List<String> permittedInputMethods = new ArrayList<>();
  private Collection<String> packagesToFailForSetApplicationHidden = Collections.emptySet();
  private Context context;
  private List<UserHandle> bindDeviceAdminTargetUsers = ImmutableList.of();
  private @RealObject DevicePolicyManager realObject;

  private static final int DEFAULT_ORGANIZATION_COLOR = 0xFF008080; // teal
  private static final Set<String> wasHiddenPackages = new HashSet<>();
  private static final Set<String> accountTypesWithManagementDisabled = new HashSet<>();
  private static final Set<String> systemAppsEnabled = new HashSet<>();
  private static final Set<String> uninstallBlockedPackages = new HashSet<>();
  private static final Set<String> suspendedPackages = new HashSet<>();
  private static final Set<String> affiliationIds = new HashSet<>();
  private static final Map<String, Set<String>> delegatedScopePackagesMap = new HashMap<>();
  private static final Map<ComponentName, Set<Integer>> adminPolicyGrantedMap = new HashMap<>();
  private static final Map<ComponentName, CharSequence> shortSupportMessageMap = new HashMap<>();
  private static final Map<ComponentName, CharSequence> longSupportMessageMap = new HashMap<>();
  private static final Set<ComponentName> componentsWithActivatedTokens = new HashSet<>();
  private static final List<String> lockTaskPackages = new ArrayList<>();
  private static final Map<UserHandle, Account> finalizedWorkProfileProvisioningMap =
      new HashMap<>();
  private static final Map<Integer, Integer> userProvisioningStatesMap = new HashMap<>();
  private static ComponentName deviceOwner;
  private static ComponentName profileOwner;
  private static CharSequence organizationName;
  private static int organizationColor;
  private static boolean isAutoTimeEnabled;
  private static boolean isAutoTimeRequired;
  private static boolean isAutoTimeZoneEnabled;
  private static String timeZone;
  private static int keyguardDisabledFeatures;
  private static String lastSetPassword;
  private static int requiredPasswordQuality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
  private static int passwordMinimumLength;
  private static int passwordMinimumLetters = 1;
  private static int passwordMinimumLowerCase;
  private static int passwordMinimumUpperCase;
  private static int passwordMinimumNonLetter;
  private static int passwordMinimumNumeric = 1;
  private static int passwordMinimumSymbols = 1;
  private static int passwordHistoryLength = 0;
  private static long passwordExpiration = 0;
  private static long passwordExpirationTimeout = 0;
  private static int maximumFailedPasswordsForWipe = 0;
  private static long maximumTimeToLock = 0;
  private static boolean cameraDisabled;
  private static boolean isActivePasswordSufficient;
  private static boolean isUniqueDeviceAttestationSupported;
  @PasswordComplexity private static int passwordComplexity;
  private static int wipeCalled;
  private static int storageEncryptionStatus;
  private static int permissionPolicy;
  private static boolean storageEncryptionRequested;
  private static int lockTaskFeatures;
  private static ApplicationPackageManager applicationPackageManager;
  private static SystemUpdatePolicy policy;
  private static boolean isDeviceProvisioned;
  private static boolean isDeviceProvisioningConfigApplied;
  private static volatile boolean organizationOwnedDeviceWithManagedProfile = false;
  private static int nearbyNotificationStreamingPolicy =
      DevicePolicyManager.NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY;
  private static int nearbyAppStreamingPolicy =
      DevicePolicyManager.NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY;
  private static boolean isUsbDataSignalingEnabled = true;
  @Nullable private static String devicePolicyManagementRoleHolderPackage;
  private static List<UserHandle> policyManagedProfiles = new ArrayList<>();
  @Nullable private static PersistableBundle lastTransferOwnershipBundle;
  private static Object /* DevicePolicyState */ devicePolicyState;
  @Nullable private static SystemUpdateInfo systemUpdateInfo;

  @Resetter
  public static void reset() {
    deviceOwner = null;
    profileOwner = null;
    organizationName = null;
    organizationColor = DEFAULT_ORGANIZATION_COLOR;
    isAutoTimeEnabled = false;
    isAutoTimeRequired = false;
    isAutoTimeZoneEnabled = false;
    timeZone = null;
    keyguardDisabledFeatures = 0;
    lastSetPassword = null;
    requiredPasswordQuality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;

    passwordMinimumLength = 0;
    passwordMinimumLetters = 1;
    passwordMinimumLowerCase = 0;
    passwordMinimumUpperCase = 0;
    passwordMinimumNonLetter = 0;
    passwordMinimumNumeric = 1;
    passwordMinimumSymbols = 1;
    passwordHistoryLength = 0;
    passwordExpiration = 0;
    passwordExpirationTimeout = 0;
    maximumFailedPasswordsForWipe = 0;
    maximumTimeToLock = 0;
    cameraDisabled = false;
    isActivePasswordSufficient = false;
    isUniqueDeviceAttestationSupported = false;
    passwordComplexity = 0;

    wipeCalled = 0;
    storageEncryptionStatus = DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED;
    permissionPolicy = 0;
    storageEncryptionRequested = false;
    wasHiddenPackages.clear();
    accountTypesWithManagementDisabled.clear();
    systemAppsEnabled.clear();
    uninstallBlockedPackages.clear();
    suspendedPackages.clear();
    affiliationIds.clear();
    delegatedScopePackagesMap.clear();
    adminPolicyGrantedMap.clear();
    shortSupportMessageMap.clear();
    longSupportMessageMap.clear();
    componentsWithActivatedTokens.clear();
    lockTaskFeatures = 0;
    lockTaskPackages.clear();
    policy = null;
    isDeviceProvisioned = false;
    isDeviceProvisioningConfigApplied = false;
    organizationOwnedDeviceWithManagedProfile = false;
    nearbyNotificationStreamingPolicy =
        DevicePolicyManager.NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY;
    nearbyAppStreamingPolicy = DevicePolicyManager.NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY;
    isUsbDataSignalingEnabled = true;
    devicePolicyManagementRoleHolderPackage = null;
    finalizedWorkProfileProvisioningMap.clear();
    policyManagedProfiles = new ArrayList<>();
    userProvisioningStatesMap.clear();
    lastTransferOwnershipBundle = null;
  }

  private static class PackageAndPermission {

    public PackageAndPermission(String packageName, String permission) {
      this.packageName = packageName;
      this.permission = permission;
    }

    private String packageName;
    private String permission;

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof PackageAndPermission)) {
        return false;
      }
      PackageAndPermission other = (PackageAndPermission) o;
      return packageName.equals(other.packageName) && permission.equals(other.permission);
    }

    @Override
    public int hashCode() {
      int result = packageName.hashCode();
      result = 31 * result + permission.hashCode();
      return result;
    }
  }

  @Implementation(maxSdk = M)
  protected void __constructor__(Context context, Handler handler) {
    init(context);
    invokeConstructor(
        DevicePolicyManager.class,
        realObject,
        from(Context.class, context),
        from(Handler.class, handler));
  }

  @Implementation(minSdk = N, maxSdk = N_MR1)
  protected void __constructor__(Context context, boolean parentInstance) {
    init(context);
  }

  @Implementation(minSdk = O)
  protected void __constructor__(Context context, IDevicePolicyManager service) {
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    this.applicationPackageManager =
        (ApplicationPackageManager) context.getApplicationContext().getPackageManager();
    organizationColor = DEFAULT_ORGANIZATION_COLOR;
    storageEncryptionStatus = DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED;
  }

  @Implementation
  protected boolean isDeviceOwnerApp(String packageName) {
    return deviceOwner != null && deviceOwner.getPackageName().equals(packageName);
  }

  @Implementation
  protected boolean isProfileOwnerApp(String packageName) {
    return profileOwner != null && profileOwner.getPackageName().equals(packageName);
  }

  @Implementation
  protected boolean isAdminActive(ComponentName who) {
    return who != null && deviceAdmins.contains(who);
  }

  @Implementation
  protected List<ComponentName> getActiveAdmins() {
    return deviceAdmins;
  }

  @Implementation
  protected void addUserRestriction(ComponentName admin, String key) {
    enforceActiveAdmin(admin);
    getShadowUserManager().setUserRestriction(Process.myUserHandle(), key, true);
  }

  @Implementation
  protected void clearUserRestriction(ComponentName admin, String key) {
    enforceActiveAdmin(admin);
    getShadowUserManager().setUserRestriction(Process.myUserHandle(), key, false);
  }

  @Implementation
  protected boolean setApplicationHidden(ComponentName admin, String packageName, boolean hidden) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    } else {
      enforceCallerDelegated(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS);
    }
    if (packagesToFailForSetApplicationHidden.contains(packageName)) {
      return false;
    }
    if (hidden) {
      wasHiddenPackages.add(packageName);
    }
    return applicationPackageManager.setApplicationHiddenSettingAsUser(
        packageName, hidden, Process.myUserHandle());
  }

  /**
   * Set package names for witch {@link DevicePolicyManager#setApplicationHidden} should fail.
   *
   * @param packagesToFail collection of package names or {@code null} to clear the packages.
   */
  public void failSetApplicationHiddenFor(Collection<String> packagesToFail) {
    if (packagesToFail == null) {
      packagesToFail = Collections.emptySet();
    }
    packagesToFailForSetApplicationHidden = packagesToFail;
  }

  @Implementation
  protected boolean isApplicationHidden(ComponentName admin, String packageName) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    } else {
      enforceCallerDelegated(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS);
    }
    return applicationPackageManager.getApplicationHiddenSettingAsUser(
        packageName, Process.myUserHandle());
  }

  /** Returns {@code true} if the given {@code packageName} was ever hidden. */
  public boolean wasPackageEverHidden(String packageName) {
    return wasHiddenPackages.contains(packageName);
  }

  @Implementation
  protected void enableSystemApp(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    systemAppsEnabled.add(packageName);
  }

  /** Returns {@code true} if the given {@code packageName} was a system app and was enabled. */
  public boolean wasSystemAppEnabled(String packageName) {
    return systemAppsEnabled.contains(packageName);
  }

  @Implementation
  protected void setUninstallBlocked(
      ComponentName admin, String packageName, boolean uninstallBlocked) {
    enforceActiveAdmin(admin);
    if (uninstallBlocked) {
      uninstallBlockedPackages.add(packageName);
    } else {
      uninstallBlockedPackages.remove(packageName);
    }
  }

  @Implementation
  protected boolean isUninstallBlocked(@Nullable ComponentName admin, String packageName) {
    if (admin == null) {
      // Starting from LOLLIPOP_MR1, the behavior of this API is changed such that passing null as
      // the admin parameter will return if any admin has blocked the uninstallation. Before L MR1,
      // passing null will cause a NullPointerException to be raised.
      if (Build.VERSION.SDK_INT < LOLLIPOP_MR1) {
        throw new NullPointerException("ComponentName is null");
      }
    } else {
      enforceActiveAdmin(admin);
    }
    return uninstallBlockedPackages.contains(packageName);
  }

  public void setIsUniqueDeviceAttestationSupported(boolean supported) {
    isUniqueDeviceAttestationSupported = supported;
  }

  @Implementation(minSdk = R)
  protected boolean isUniqueDeviceAttestationSupported() {
    return isUniqueDeviceAttestationSupported;
  }

  /** Sets USB signaling device restriction. */
  public void setIsUsbDataSignalingEnabled(boolean isEnabled) {
    isUsbDataSignalingEnabled = isEnabled;
  }

  @Implementation(minSdk = S)
  protected boolean isUsbDataSignalingEnabled() {
    return isUsbDataSignalingEnabled;
  }

  /**
   * @see #setDeviceOwner(ComponentName)
   */
  @Implementation
  protected String getDeviceOwner() {
    return deviceOwner != null ? deviceOwner.getPackageName() : null;
  }

  /**
   * @see #setDeviceOwner(ComponentName)
   */
  @Implementation(minSdk = N)
  public boolean isDeviceManaged() {
    return getDeviceOwner() != null;
  }

  /**
   * @see #setProfileOwner(ComponentName)
   */
  @Implementation
  protected ComponentName getProfileOwner() {
    return profileOwner;
  }

  /**
   * Returns the human-readable name of the profile owner for a user if set using {@link
   * #setProfileOwnerName}, otherwise null.
   */
  @Implementation
  protected String getProfileOwnerNameAsUser(int userId) {
    return profileOwnerNamesMap.get(userId);
  }

  @Implementation(minSdk = P)
  protected void transferOwnership(
      ComponentName admin, ComponentName target, @Nullable PersistableBundle bundle) {
    Objects.requireNonNull(admin, "ComponentName is null");
    Objects.requireNonNull(target, "Target cannot be null.");
    Preconditions.checkArgument(
        !admin.equals(target), "Provided administrator and target are the same object.");
    Preconditions.checkArgument(
        !admin.getPackageName().equals(target.getPackageName()),
        "Provided administrator and target have the same package name.");
    try {
      context.getPackageManager().getReceiverInfo(target, 0);
    } catch (PackageManager.NameNotFoundException e) {
      throw new IllegalArgumentException("Unknown admin: " + target);
    }
    if (admin.equals(deviceOwner)) {
      deviceOwner = target;
    } else if (admin.equals(profileOwner)) {
      profileOwner = target;
    } else {
      throw new SecurityException("Calling identity is not authorized");
    }
    lastTransferOwnershipBundle = bundle;
  }

  @Implementation(minSdk = P)
  @Nullable
  protected PersistableBundle getTransferOwnershipBundle() {
    return lastTransferOwnershipBundle;
  }

  private ShadowUserManager getShadowUserManager() {
    return Shadow.extract(context.getSystemService(Context.USER_SERVICE));
  }

  /**
   * Sets the admin as active admin and device owner.
   *
   * @see DevicePolicyManager#getDeviceOwner()
   */
  @Implementation(minSdk = N, maxSdk = S_V2)
  public boolean setDeviceOwner(ComponentName admin) {
    setActiveAdmin(admin);
    deviceOwner = admin;
    return true;
  }

  /**
   * Sets the admin as active admin and profile owner.
   *
   * @see DevicePolicyManager#getProfileOwner()
   */
  public void setProfileOwner(ComponentName admin) {
    setActiveAdmin(admin);
    profileOwner = admin;
  }

  public void setProfileOwnerName(int userId, String name) {
    profileOwnerNamesMap.put(userId, name);
  }

  /** Sets the given {@code componentName} as one of the active admins. */
  public void setActiveAdmin(ComponentName componentName) {
    deviceAdmins.add(componentName);
  }

  @Implementation
  protected void removeActiveAdmin(ComponentName admin) {
    deviceAdmins.remove(admin);
  }

  @Implementation
  protected void clearProfileOwner(ComponentName admin) {
    profileOwner = null;
    lastTransferOwnershipBundle = null;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      removeActiveAdmin(admin);
    }
  }

  @Implementation
  protected Bundle getApplicationRestrictions(ComponentName admin, String packageName) {
    if (admin != null) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceCallerDelegated(DevicePolicyManager.DELEGATION_APP_RESTRICTIONS);
    }
    return getApplicationRestrictions(packageName);
  }

  /** Returns all application restrictions of the {@code packageName} in a {@link Bundle}. */
  public Bundle getApplicationRestrictions(String packageName) {
    Bundle bundle = applicationRestrictionsMap.get(packageName);
    // If no restrictions were saved, DPM method should return an empty Bundle as per JavaDoc.
    return bundle != null ? new Bundle(bundle) : new Bundle();
  }

  @Implementation
  protected void setApplicationRestrictions(
      ComponentName admin, String packageName, Bundle applicationRestrictions) {
    if (admin != null) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceCallerDelegated(DevicePolicyManager.DELEGATION_APP_RESTRICTIONS);
    }
    setApplicationRestrictions(packageName, applicationRestrictions);
  }

  /**
   * Sets the application restrictions of the {@code packageName}.
   *
   * <p>The new {@code applicationRestrictions} always completely overwrites any existing ones.
   */
  public void setApplicationRestrictions(String packageName, Bundle applicationRestrictions) {
    applicationRestrictionsMap.put(packageName, new Bundle(applicationRestrictions));
  }

  private void enforceProfileOwner(ComponentName admin) {
    if (!admin.equals(profileOwner)) {
      throw new SecurityException("[" + admin + "] is not a profile owner");
    }
  }

  private void enforceDeviceOwnerOrProfileOwner(ComponentName admin) {
    if (!admin.equals(deviceOwner) && !admin.equals(profileOwner)) {
      throw new SecurityException("[" + admin + "] is neither a device owner nor a profile owner.");
    }
  }

  private void enforceActiveAdmin(ComponentName admin) {
    if (!deviceAdmins.contains(admin)) {
      throw new SecurityException("[" + admin + "] is not an active device admin");
    }
  }

  private void enforceCallerDelegated(String targetScope) {
    String caller = context.getPackageName();
    if (!delegatedScopePackagesMap.containsKey(caller)
        || !delegatedScopePackagesMap.get(caller).contains(targetScope)) {
      throw new SecurityException("[" + caller + "] is not delegated with" + targetScope);
    }
  }

  @Implementation(minSdk = O)
  protected void setDelegatedScopes(
      ComponentName admin, String delegatePackage, List<String> scopes) {
    enforceDeviceOwnerOrProfileOwner(admin);
    delegatedScopePackagesMap.put(delegatePackage, ImmutableSet.copyOf(scopes));
  }

  @Implementation(minSdk = O)
  protected List<String> getDelegatedScopes(ComponentName admin, String delegatePackage) {
    Objects.requireNonNull(delegatePackage, "Delegate package is null");
    if (admin == null) {
      String caller = context.getPackageName();
      if (!Objects.equals(caller, delegatePackage)) {
        throw new SecurityException(String.format("Caller is not %s.", delegatePackage));
      }
      // this app is retrieving its own delegated scopes
    } else {
      enforceDeviceOwnerOrProfileOwner(admin);
    }
    if (delegatedScopePackagesMap.containsKey(delegatePackage)) {
      return ImmutableList.copyOf(delegatedScopePackagesMap.get(delegatePackage));
    }
    return ImmutableList.of();
  }

  @Implementation
  protected void setAccountManagementDisabled(
      ComponentName admin, String accountType, boolean disabled) {
    enforceDeviceOwnerOrProfileOwner(admin);
    if (disabled) {
      accountTypesWithManagementDisabled.add(accountType);
    } else {
      accountTypesWithManagementDisabled.remove(accountType);
    }
  }

  @Implementation
  protected String[] getAccountTypesWithManagementDisabled() {
    return accountTypesWithManagementDisabled.toArray(new String[0]);
  }

  /**
   * Sets organization name.
   *
   * <p>The API can only be called by profile owner since Android N and can be called by both of
   * profile owner and device owner since Android O.
   */
  @Implementation(minSdk = N)
  protected void setOrganizationName(ComponentName admin, @Nullable CharSequence name) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceProfileOwner(admin);
    }

    if (TextUtils.isEmpty(name)) {
      organizationName = null;
    } else {
      organizationName = name;
    }
  }

  @Implementation(minSdk = N)
  protected String[] setPackagesSuspended(
      ComponentName admin, String[] packageNames, boolean suspended) {
    if (admin != null) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceCallerDelegated(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS);
    }
    if (packageNames == null) {
      throw new NullPointerException("package names cannot be null");
    }
    PackageManager pm = context.getPackageManager();
    ArrayList<String> packagesFailedToSuspend = new ArrayList<>();
    for (String packageName : packageNames) {
      try {
        // check if it is installed
        pm.getPackageInfo(packageName, 0);
        if (suspended) {
          suspendedPackages.add(packageName);
        } else {
          suspendedPackages.remove(packageName);
        }
      } catch (NameNotFoundException e) {
        packagesFailedToSuspend.add(packageName);
      }
    }
    return packagesFailedToSuspend.toArray(new String[0]);
  }

  @Implementation(minSdk = N)
  protected boolean isPackageSuspended(ComponentName admin, String packageName)
      throws NameNotFoundException {
    if (admin != null) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceCallerDelegated(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS);
    }
    // Throws NameNotFoundException
    context.getPackageManager().getPackageInfo(packageName, 0);
    return suspendedPackages.contains(packageName);
  }

  @Implementation(minSdk = N)
  protected void setOrganizationColor(ComponentName admin, int color) {
    enforceProfileOwner(admin);
    organizationColor = color;
  }

  /**
   * Returns organization name.
   *
   * <p>The API can only be called by profile owner since Android N.
   *
   * <p>Android framework has a hidden API for getting the organization name for device owner since
   * Android O. This method, however, is extended to return the organization name for device owners
   * too to make testing of {@link #setOrganizationName(ComponentName, CharSequence)} easier for
   * device owner cases.
   */
  @Implementation(minSdk = N)
  @Nullable
  protected CharSequence getOrganizationName(ComponentName admin) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceProfileOwner(admin);
    }

    return organizationName;
  }

  @Implementation(minSdk = N)
  protected int getOrganizationColor(ComponentName admin) {
    enforceProfileOwner(admin);
    return organizationColor;
  }

  @Implementation(minSdk = R)
  protected void setAutoTimeEnabled(ComponentName admin, boolean enabled) {
    enforceDeviceOwnerOrProfileOwner(admin);
    isAutoTimeEnabled = enabled;
  }

  @Implementation(minSdk = R)
  protected boolean getAutoTimeEnabled(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return isAutoTimeEnabled;
  }

  @Implementation
  protected void setAutoTimeRequired(ComponentName admin, boolean required) {
    enforceDeviceOwnerOrProfileOwner(admin);
    isAutoTimeRequired = required;
  }

  @Implementation
  protected boolean getAutoTimeRequired() {
    return isAutoTimeRequired;
  }

  @Implementation(minSdk = R)
  protected void setAutoTimeZoneEnabled(ComponentName admin, boolean enabled) {
    enforceDeviceOwnerOrProfileOwner(admin);
    isAutoTimeZoneEnabled = enabled;
  }

  @Implementation(minSdk = R)
  protected boolean getAutoTimeZoneEnabled(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return isAutoTimeZoneEnabled;
  }

  @Implementation(minSdk = P)
  protected boolean setTimeZone(ComponentName admin, String timeZone) {
    enforceDeviceOwnerOrProfileOwner(admin);
    if (isAutoTimeZoneEnabled) {
      return false;
    }
    this.timeZone = timeZone;
    return true;
  }

  /** Returns the time zone set by setTimeZone. */
  public String getTimeZone() {
    return timeZone;
  }

  /**
   * Sets permitted accessibility services.
   *
   * <p>The API can be called by either a profile or device owner.
   *
   * <p>This method does not check already enabled non-system accessibility services, so will always
   * set the restriction and return true.
   */
  @Implementation
  protected boolean setPermittedAccessibilityServices(
      ComponentName admin, List<String> packageNames) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permittedAccessibilityServices = packageNames;
    return true;
  }

  @Implementation
  @Nullable
  protected List<String> getPermittedAccessibilityServices(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return permittedAccessibilityServices;
  }

  /**
   * Sets permitted input methods.
   *
   * <p>The API can be called by either a profile or device owner.
   *
   * <p>This method does not check already enabled non-system input methods, so will always set the
   * restriction and return true.
   */
  @Implementation
  protected boolean setPermittedInputMethods(ComponentName admin, List<String> packageNames) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permittedInputMethods = packageNames;
    return true;
  }

  @Implementation
  @Nullable
  protected List<String> getPermittedInputMethods(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return permittedInputMethods;
  }

  /**
   * @return the previously set status; default is {@link
   *     DevicePolicyManager#ENCRYPTION_STATUS_UNSUPPORTED}
   * @see #setStorageEncryptionStatus(int)
   */
  @Implementation
  protected int getStorageEncryptionStatus() {
    return storageEncryptionStatus;
  }

  /** Setter for {@link DevicePolicyManager#getStorageEncryptionStatus()}. */
  public void setStorageEncryptionStatus(int status) {
    switch (status) {
      case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE:
      case DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE:
      case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING:
      case DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED:
        break;
      case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY:
        if (RuntimeEnvironment.getApiLevel() < M) {
          throw new IllegalArgumentException("status " + status + " requires API " + M);
        }
        break;
      case DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER:
        if (RuntimeEnvironment.getApiLevel() < N) {
          throw new IllegalArgumentException("status " + status + " requires API " + N);
        }
        break;
      default:
        throw new IllegalArgumentException("Unknown status: " + status);
    }

    storageEncryptionStatus = status;
  }

  @Implementation
  protected int setStorageEncryption(ComponentName admin, boolean encrypt) {
    enforceActiveAdmin(admin);
    this.storageEncryptionRequested = encrypt;
    return storageEncryptionStatus;
  }

  @Implementation
  protected boolean getStorageEncryption(ComponentName admin) {
    return storageEncryptionRequested;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected int getPermissionGrantState(
      ComponentName admin, String packageName, String permission) {
    enforceDeviceOwnerOrProfileOwner(admin);
    Integer state =
        appPermissionGrantStateMap.get(new PackageAndPermission(packageName, permission));
    return state == null ? DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT : state;
  }

  public boolean isPermissionGranted(String packageName, String permission) {
    Boolean isGranted =
        appPermissionGrantedMap.get(new PackageAndPermission(packageName, permission));
    return isGranted == null ? false : isGranted;
  }

  @Implementation(minSdk = VERSION_CODES.M)
  protected boolean setPermissionGrantState(
      ComponentName admin, String packageName, String permission, int grantState) {
    enforceDeviceOwnerOrProfileOwner(admin);

    String selfPackageName = context.getPackageName();

    if (packageName.equals(selfPackageName)) {
      PackageInfo packageInfo;
      try {
        packageInfo =
            context
                .getPackageManager()
                .getPackageInfo(selfPackageName, PackageManager.GET_PERMISSIONS);
      } catch (NameNotFoundException e) {
        throw new RuntimeException(e);
      }
      if (Arrays.asList(packageInfo.requestedPermissions).contains(permission)) {
        if (grantState == DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) {
          ShadowApplication.getInstance().grantPermissions(permission);
        }
        if (grantState == DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED) {
          ShadowApplication.getInstance().denyPermissions(permission);
        }
      } else {
        // the app does not require this permission
        return false;
      }
    }
    PackageAndPermission key = new PackageAndPermission(packageName, permission);
    switch (grantState) {
      case DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED:
        appPermissionGrantedMap.put(key, true);
        break;
      case DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED:
        appPermissionGrantedMap.put(key, false);
        break;
      default:
        // no-op
    }
    appPermissionGrantStateMap.put(key, grantState);
    return true;
  }

  @Implementation
  protected void lockNow() {
    KeyguardManager keyguardManager =
        (KeyguardManager) this.context.getSystemService(Context.KEYGUARD_SERVICE);
    ShadowKeyguardManager shadowKeyguardManager = Shadow.extract(keyguardManager);
    shadowKeyguardManager.setKeyguardLocked(true);
    shadowKeyguardManager.setIsDeviceLocked(true);
  }

  @Implementation
  protected void wipeData(int flags) {
    wipeCalled++;
  }

  public long getWipeCalledTimes() {
    return wipeCalled;
  }

  @Implementation
  protected void setPasswordQuality(ComponentName admin, int quality) {
    enforceActiveAdmin(admin);
    requiredPasswordQuality = quality;
  }

  @Implementation
  protected int getPasswordQuality(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return requiredPasswordQuality;
  }

  @Implementation
  protected boolean resetPassword(String password, int flags) {
    if (!passwordMeetsRequirements(password)) {
      return false;
    }
    lastSetPassword = password;
    boolean secure = !password.isEmpty();
    KeyguardManager keyguardManager =
        (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    shadowOf(keyguardManager).setIsDeviceSecure(secure);
    shadowOf(keyguardManager).setIsKeyguardSecure(secure);
    return true;
  }

  @Implementation(minSdk = O)
  protected boolean resetPasswordWithToken(
      ComponentName admin, String password, byte[] token, int flags) {
    enforceDeviceOwnerOrProfileOwner(admin);
    if (!Arrays.equals(passwordResetTokens.get(admin), token)
        || !componentsWithActivatedTokens.contains(admin)) {
      throw new IllegalStateException("wrong or not activated token");
    }
    resetPassword(password, flags);
    return true;
  }

  @Implementation(minSdk = O)
  protected boolean isResetPasswordTokenActive(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return componentsWithActivatedTokens.contains(admin);
  }

  @Implementation(minSdk = O)
  protected boolean setResetPasswordToken(ComponentName admin, byte[] token) {
    if (token.length < 32) {
      throw new IllegalArgumentException("token too short: " + token.length);
    }
    enforceDeviceOwnerOrProfileOwner(admin);
    passwordResetTokens.put(admin, token);
    componentsWithActivatedTokens.remove(admin);
    KeyguardManager keyguardManager =
        (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    if (!keyguardManager.isDeviceSecure()) {
      activateResetToken(admin);
    }
    return true;
  }

  @Implementation
  protected void setPasswordMinimumLength(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumLength = length;
  }

  @Implementation
  protected int getPasswordMinimumLength(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumLength;
  }

  @Implementation
  protected void setPasswordMinimumLetters(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumLetters = length;
  }

  @Implementation
  protected int getPasswordMinimumLetters(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumLetters;
  }

  @Implementation
  protected void setPasswordMinimumLowerCase(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumLowerCase = length;
  }

  @Implementation
  protected int getPasswordMinimumLowerCase(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumLowerCase;
  }

  @Implementation
  protected void setPasswordMinimumUpperCase(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumUpperCase = length;
  }

  @Implementation
  protected int getPasswordMinimumUpperCase(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumUpperCase;
  }

  @Implementation
  protected void setPasswordMinimumNonLetter(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumNonLetter = length;
  }

  @Implementation
  protected int getPasswordMinimumNonLetter(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumNonLetter;
  }

  @Implementation
  protected void setPasswordMinimumNumeric(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumNumeric = length;
  }

  @Implementation
  protected int getPasswordMinimumNumeric(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumNumeric;
  }

  @Implementation
  protected void setPasswordMinimumSymbols(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumSymbols = length;
  }

  @Implementation
  protected int getPasswordMinimumSymbols(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordMinimumSymbols;
  }

  @Implementation
  protected void setMaximumFailedPasswordsForWipe(ComponentName admin, int num) {
    enforceActiveAdmin(admin);
    maximumFailedPasswordsForWipe = num;
  }

  @Implementation
  protected int getMaximumFailedPasswordsForWipe(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return maximumFailedPasswordsForWipe;
  }

  @Implementation
  protected void setCameraDisabled(ComponentName admin, boolean disabled) {
    enforceActiveAdmin(admin);
    cameraDisabled = disabled;
  }

  @Implementation
  protected boolean getCameraDisabled(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return cameraDisabled;
  }

  @Implementation
  protected void setPasswordExpirationTimeout(ComponentName admin, long timeout) {
    enforceActiveAdmin(admin);
    passwordExpirationTimeout = timeout;
  }

  @Implementation
  protected long getPasswordExpirationTimeout(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordExpirationTimeout;
  }

  /**
   * Sets the password expiration time for a particular admin.
   *
   * @param admin which DeviceAdminReceiver this request is associated with.
   * @param timeout the password expiration time, in milliseconds since epoch.
   */
  public void setPasswordExpiration(ComponentName admin, long timeout) {
    enforceActiveAdmin(admin);
    passwordExpiration = timeout;
  }

  @Implementation
  protected long getPasswordExpiration(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordExpiration;
  }

  @Implementation
  protected void setMaximumTimeToLock(ComponentName admin, long timeMs) {
    enforceActiveAdmin(admin);
    maximumTimeToLock = timeMs;
  }

  @Implementation
  protected long getMaximumTimeToLock(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return maximumTimeToLock;
  }

  @Implementation
  protected void setPasswordHistoryLength(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordHistoryLength = length;
  }

  @Implementation
  protected int getPasswordHistoryLength(ComponentName admin) {
    if (admin != null) {
      enforceActiveAdmin(admin);
    }
    return passwordHistoryLength;
  }

  /**
   * Sets if the password meets the current requirements.
   *
   * @param sufficient indicates the password meets the current requirements
   */
  public void setActivePasswordSufficient(boolean sufficient) {
    isActivePasswordSufficient = sufficient;
  }

  @Implementation
  protected boolean isActivePasswordSufficient() {
    return isActivePasswordSufficient;
  }

  /** Sets whether the device is provisioned. */
  public void setDeviceProvisioned(boolean isProvisioned) {
    isDeviceProvisioned = isProvisioned;
  }

  @Implementation(minSdk = O)
  @SystemApi
  @RequiresPermission(android.Manifest.permission.MANAGE_USERS)
  protected boolean isDeviceProvisioned() {
    return isDeviceProvisioned;
  }

  @Implementation(minSdk = O)
  @SystemApi
  @RequiresPermission(android.Manifest.permission.MANAGE_USERS)
  protected void setDeviceProvisioningConfigApplied() {
    isDeviceProvisioningConfigApplied = true;
  }

  @Implementation(minSdk = O)
  @SystemApi
  @RequiresPermission(android.Manifest.permission.MANAGE_USERS)
  protected boolean isDeviceProvisioningConfigApplied() {
    return isDeviceProvisioningConfigApplied;
  }

  /** Sets the password complexity. */
  public void setPasswordComplexity(@PasswordComplexity int passwordComplexity) {
    this.passwordComplexity = passwordComplexity;
  }

  @PasswordComplexity
  @Implementation(minSdk = Q)
  protected int getPasswordComplexity() {
    return passwordComplexity;
  }

  private boolean passwordMeetsRequirements(String password) {
    int digit = 0;
    int alpha = 0;
    int upper = 0;
    int lower = 0;
    int symbol = 0;
    for (int i = 0; i < password.length(); i++) {
      char c = password.charAt(i);
      if (Character.isDigit(c)) {
        digit++;
      }
      if (Character.isLetter(c)) {
        alpha++;
      }
      if (Character.isUpperCase(c)) {
        upper++;
      }
      if (Character.isLowerCase(c)) {
        lower++;
      }
      if (!Character.isLetterOrDigit(c)) {
        symbol++;
      }
    }
    switch (requiredPasswordQuality) {
      case DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED:
      case DevicePolicyManager.PASSWORD_QUALITY_MANAGED:
      case DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK:
        return true;
      case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
        return password.length() > 0;
      case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
      case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX: // complexity not enforced
        return digit > 0 && password.length() >= passwordMinimumLength;
      case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
        return digit > 0 && alpha > 0 && password.length() >= passwordMinimumLength;
      case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
        return password.length() >= passwordMinimumLength
            && alpha >= passwordMinimumLetters
            && lower >= passwordMinimumLowerCase
            && upper >= passwordMinimumUpperCase
            && digit + symbol >= passwordMinimumNonLetter
            && digit >= passwordMinimumNumeric
            && symbol >= passwordMinimumSymbols;
      default:
        return true;
    }
  }

  /**
   * Retrieves last password set through {@link DevicePolicyManager#resetPassword} or {@link
   * DevicePolicyManager#resetPasswordWithToken}.
   */
  public String getLastSetPassword() {
    return lastSetPassword;
  }

  /**
   * Activates reset token for given admin.
   *
   * @param admin Which {@link DeviceAdminReceiver} this request is associated with.
   * @return if the activation state changed.
   * @throws IllegalArgumentException if there is no token set for this admin.
   */
  public boolean activateResetToken(ComponentName admin) {
    if (!passwordResetTokens.containsKey(admin)) {
      throw new IllegalArgumentException("No token set for comopnent: " + admin);
    }
    return componentsWithActivatedTokens.add(admin);
  }

  @Implementation
  protected void addPersistentPreferredActivity(
      ComponentName admin, IntentFilter filter, ComponentName activity) {
    enforceDeviceOwnerOrProfileOwner(admin);

    PackageManager packageManager = context.getPackageManager();
    Shadow.<ShadowPackageManager>extract(packageManager)
        .addPersistentPreferredActivity(filter, activity);
  }

  @Implementation
  protected void clearPackagePersistentPreferredActivities(
      ComponentName admin, String packageName) {
    enforceDeviceOwnerOrProfileOwner(admin);
    PackageManager packageManager = context.getPackageManager();
    Shadow.<ShadowPackageManager>extract(packageManager)
        .clearPackagePersistentPreferredActivities(packageName);
  }

  @Implementation
  protected void setKeyguardDisabledFeatures(ComponentName admin, int which) {
    enforceActiveAdmin(admin);
    keyguardDisabledFeatures = which;
  }

  @Implementation
  protected int getKeyguardDisabledFeatures(ComponentName admin) {
    return keyguardDisabledFeatures;
  }

  /**
   * Sets the user provisioning state.
   *
   * @param state to store provisioning state
   */
  public void setUserProvisioningState(int state) {
    setUserProvisioningState(state, Process.myUserHandle());
  }

  @Implementation(minSdk = TIRAMISU)
  protected void setUserProvisioningState(@UserProvisioningState int state, UserHandle userHandle) {
    userProvisioningStatesMap.put(userHandle.getIdentifier(), state);
  }

  /**
   * Returns the provisioning state set in {@link #setUserProvisioningState(int)}, or {@link
   * DevicePolicyManager#STATE_USER_UNMANAGED} if none is set.
   */
  @Implementation(minSdk = N)
  protected int getUserProvisioningState() {
    return getUserProvisioningStateForUser(Process.myUserHandle().getIdentifier());
  }

  @Implementation
  protected boolean hasGrantedPolicy(@NonNull ComponentName admin, int usesPolicy) {
    enforceActiveAdmin(admin);
    Set<Integer> policyGrantedSet = adminPolicyGrantedMap.get(admin);
    return policyGrantedSet != null && policyGrantedSet.contains(usesPolicy);
  }

  @Implementation(minSdk = P)
  protected int getLockTaskFeatures(ComponentName admin) {
    Objects.requireNonNull(admin, "ComponentName is null");
    enforceDeviceOwnerOrProfileOwner(admin);
    return lockTaskFeatures;
  }

  @Implementation(minSdk = P)
  protected void setLockTaskFeatures(ComponentName admin, int flags) {
    Objects.requireNonNull(admin, "ComponentName is null");
    enforceDeviceOwnerOrProfileOwner(admin);
    // Throw if Overview is used without Home.
    boolean hasHome = (flags & LOCK_TASK_FEATURE_HOME) != 0;
    boolean hasOverview = (flags & LOCK_TASK_FEATURE_OVERVIEW) != 0;
    Preconditions.checkArgument(
        hasHome || !hasOverview,
        "Cannot use LOCK_TASK_FEATURE_OVERVIEW without LOCK_TASK_FEATURE_HOME");
    boolean hasNotification = (flags & LOCK_TASK_FEATURE_NOTIFICATIONS) != 0;
    Preconditions.checkArgument(
        hasHome || !hasNotification,
        "Cannot use LOCK_TASK_FEATURE_NOTIFICATIONS without LOCK_TASK_FEATURE_HOME");

    lockTaskFeatures = flags;
  }

  @Implementation
  protected void setLockTaskPackages(@NonNull ComponentName admin, String[] packages) {
    enforceDeviceOwnerOrProfileOwner(admin);
    lockTaskPackages.clear();
    Collections.addAll(lockTaskPackages, packages);
  }

  @Implementation
  protected String[] getLockTaskPackages(@NonNull ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return lockTaskPackages.toArray(new String[0]);
  }

  @Implementation
  protected boolean isLockTaskPermitted(@NonNull String pkg) {
    return lockTaskPackages.contains(pkg);
  }

  @Implementation(minSdk = O)
  protected void setAffiliationIds(@NonNull ComponentName admin, @NonNull Set<String> ids) {
    enforceDeviceOwnerOrProfileOwner(admin);
    affiliationIds.clear();
    affiliationIds.addAll(ids);
  }

  @Implementation(minSdk = O)
  protected Set<String> getAffiliationIds(@NonNull ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return affiliationIds;
  }

  @Implementation(minSdk = M)
  protected void setPermissionPolicy(@NonNull ComponentName admin, int policy) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permissionPolicy = policy;
  }

  @Implementation(minSdk = M)
  protected int getPermissionPolicy(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return permissionPolicy;
  }

  /**
   * Grants a particular device policy for an active ComponentName.
   *
   * @param admin the ComponentName which DeviceAdminReceiver this request is associated with. Must
   *     be an active administrator, or an exception will be thrown. This value must never be null.
   * @param usesPolicy the uses-policy to check
   */
  public void grantPolicy(@NonNull ComponentName admin, int usesPolicy) {
    enforceActiveAdmin(admin);
    Set<Integer> policyGrantedSet = adminPolicyGrantedMap.get(admin);
    if (policyGrantedSet == null) {
      policyGrantedSet = new HashSet<>();
      policyGrantedSet.add(usesPolicy);
      adminPolicyGrantedMap.put(admin, policyGrantedSet);
    } else {
      policyGrantedSet.add(usesPolicy);
    }
  }

  @Implementation(minSdk = M)
  protected SystemUpdatePolicy getSystemUpdatePolicy() {
    return policy;
  }

  @Implementation(minSdk = M)
  protected void setSystemUpdatePolicy(ComponentName admin, SystemUpdatePolicy policy) {
    this.policy = policy;
  }

  /**
   * Sets the system update policy.
   *
   * @see #setSystemUpdatePolicy(ComponentName, SystemUpdatePolicy)
   */
  public void setSystemUpdatePolicy(SystemUpdatePolicy policy) {
    setSystemUpdatePolicy(null, policy);
  }

  /**
   * Set the list of target users that the calling device or profile owner can use when calling
   * {@link #bindDeviceAdminServiceAsUser}.
   *
   * @see #getBindDeviceAdminTargetUsers(ComponentName)
   */
  public void setBindDeviceAdminTargetUsers(List<UserHandle> bindDeviceAdminTargetUsers) {
    this.bindDeviceAdminTargetUsers = bindDeviceAdminTargetUsers;
  }

  /**
   * Returns the list of target users that the calling device or profile owner can use when calling
   * {@link #bindDeviceAdminServiceAsUser}.
   *
   * @see #setBindDeviceAdminTargetUsers(List)
   */
  @Implementation(minSdk = O)
  protected List<UserHandle> getBindDeviceAdminTargetUsers(ComponentName admin) {
    return bindDeviceAdminTargetUsers;
  }

  /**
   * Bind to the same package in another user.
   *
   * <p>This validates that the targetUser is one from {@link
   * #getBindDeviceAdminTargetUsers(ComponentName)} but does not actually bind to a different user,
   * instead binding to the same user.
   *
   * <p>It also does not validate the service being bound to.
   */
  @Implementation(minSdk = O)
  protected boolean bindDeviceAdminServiceAsUser(
      ComponentName admin,
      Intent serviceIntent,
      ServiceConnection conn,
      int flags,
      UserHandle targetUser) {
    if (!getBindDeviceAdminTargetUsers(admin).contains(targetUser)) {
      throw new SecurityException("Not allowed to bind to target user id");
    }

    return context.bindServiceAsUser(serviceIntent, conn, flags, targetUser);
  }

  @Implementation(minSdk = N)
  protected void setShortSupportMessage(ComponentName admin, @Nullable CharSequence message) {
    enforceActiveAdmin(admin);
    shortSupportMessageMap.put(admin, message);
  }

  @Implementation(minSdk = N)
  @Nullable
  protected CharSequence getShortSupportMessage(ComponentName admin) {
    enforceActiveAdmin(admin);
    return shortSupportMessageMap.get(admin);
  }

  @Implementation(minSdk = N)
  protected void setLongSupportMessage(ComponentName admin, @Nullable CharSequence message) {
    enforceActiveAdmin(admin);
    longSupportMessageMap.put(admin, message);
  }

  @Implementation(minSdk = N)
  @Nullable
  protected CharSequence getLongSupportMessage(ComponentName admin) {
    enforceActiveAdmin(admin);
    return longSupportMessageMap.get(admin);
  }

  /**
   * Sets the return value of the {@link
   * DevicePolicyManager#isOrganizationOwnedDeviceWithManagedProfile} method (only for Android R+).
   */
  public void setOrganizationOwnedDeviceWithManagedProfile(boolean value) {
    organizationOwnedDeviceWithManagedProfile = value;
  }

  /**
   * Returns the value stored using in the shadow, while the real method returns the value store on
   * the device.
   *
   * <p>The value can be set by {@link #setOrganizationOwnedDeviceWithManagedProfile} and is {@code
   * false} by default.
   */
  @Implementation(minSdk = R)
  protected boolean isOrganizationOwnedDeviceWithManagedProfile() {
    return organizationOwnedDeviceWithManagedProfile;
  }

  @Implementation(minSdk = S)
  @NearbyStreamingPolicy
  protected int getNearbyNotificationStreamingPolicy() {
    return nearbyNotificationStreamingPolicy;
  }

  @Implementation(minSdk = S)
  protected void setNearbyNotificationStreamingPolicy(@NearbyStreamingPolicy int policy) {
    nearbyNotificationStreamingPolicy = policy;
  }

  @Implementation(minSdk = S)
  @NearbyStreamingPolicy
  protected int getNearbyAppStreamingPolicy() {
    return nearbyAppStreamingPolicy;
  }

  @Implementation(minSdk = S)
  protected void setNearbyAppStreamingPolicy(@NearbyStreamingPolicy int policy) {
    nearbyAppStreamingPolicy = policy;
  }

  @Nullable
  @Implementation(minSdk = TIRAMISU)
  protected String getDevicePolicyManagementRoleHolderPackage() {
    return devicePolicyManagementRoleHolderPackage;
  }

  /**
   * Sets the package name of the device policy management role holder.
   *
   * @see #getDevicePolicyManagementRoleHolderPackage()
   */
  public void setDevicePolicyManagementRoleHolderPackage(@Nullable String packageName) {
    devicePolicyManagementRoleHolderPackage = packageName;
  }

  @Implementation(minSdk = TIRAMISU)
  protected void finalizeWorkProfileProvisioning(
      UserHandle managedProfileUser, @Nullable Account migratedAccount) {
    finalizedWorkProfileProvisioningMap.put(managedProfileUser, migratedAccount);
  }

  /**
   * Returns if {@link #finalizeWorkProfileProvisioning(UserHandle, Account)} was called with the
   * provided parameters.
   */
  public boolean isWorkProfileProvisioningFinalized(
      UserHandle userHandle, @Nullable Account migratedAccount) {
    return finalizedWorkProfileProvisioningMap.containsKey(userHandle)
        && Objects.equals(finalizedWorkProfileProvisioningMap.get(userHandle), migratedAccount);
  }

  /**
   * Returns the managed profiles set in {@link #setPolicyManagedProfiles(List)}. This value does
   * not take the user handle parameter into account.
   */
  @Implementation(minSdk = TIRAMISU)
  protected List<UserHandle> getPolicyManagedProfiles(UserHandle userHandle) {
    return policyManagedProfiles;
  }

  /** Sets the value returned by {@link #getPolicyManagedProfiles(UserHandle)}. */
  public void setPolicyManagedProfiles(List<UserHandle> policyManagedProfiles) {
    this.policyManagedProfiles = policyManagedProfiles;
  }

  /**
   * Returns the user provisioning state set by {@link #setUserProvisioningState(int, UserHandle)},
   * or {@link DevicePolicyManager#STATE_USER_UNMANAGED} if none is set.
   */
  @UserProvisioningState
  public int getUserProvisioningStateForUser(int userId) {
    return userProvisioningStatesMap.getOrDefault(userId, DevicePolicyManager.STATE_USER_UNMANAGED);
  }

  /** Return a stub value set by {@link #setDevicePolicyState(DevicePolicyState policyState)} */
  @Implementation(minSdk = U.SDK_INT)
  protected @ClassName("android.app.admin.DevicePolicyState") Object getDevicePolicyState() {
    return devicePolicyState;
  }

  /**
   * Set the {@link DevicePolicyState} which can be constructed from {@link
   * DevicePolicyStateBuilder}
   */
  public void setDevicePolicyState(Object policyState) {
    devicePolicyState = policyState;
  }

  /**
   * Set the {@link SystemUpdateInfo} to be returned by {@link
   * DevicePolicyManager#getPendingSystemUpdate(ComponentName)}.
   */
  public void setPendingSystemUpdate(@Nullable SystemUpdateInfo info) {
    systemUpdateInfo = info;
  }

  @Implementation(minSdk = O)
  protected void notifyPendingSystemUpdate(long updateReceivedTime) {
    systemUpdateInfo = reflector(SystemUpdateInfoReflector.class).of(updateReceivedTime);
  }

  @Implementation(minSdk = O)
  protected void notifyPendingSystemUpdate(long updateReceivedTime, boolean isSecurityPatch) {
    systemUpdateInfo =
        reflector(SystemUpdateInfoReflector.class).of(updateReceivedTime, isSecurityPatch);
  }

  @Implementation(minSdk = O)
  @Nullable
  protected SystemUpdateInfo getPendingSystemUpdate(@Nullable ComponentName admin) {
    return systemUpdateInfo;
  }

  @ForType(SystemUpdateInfo.class)
  interface SystemUpdateInfoReflector {
    @Static
    SystemUpdateInfo of(long updateReceivedTime);

    @Static
    SystemUpdateInfo of(long updateReceivedTime, boolean isSecurityPatch);
  }
}
