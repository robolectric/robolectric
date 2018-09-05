package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.app.ApplicationPackageManager;
import android.app.KeyguardManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;

/** Shadow for {@link DevicePolicyManager} */
@Implements(DevicePolicyManager.class)
@SuppressLint("NewApi")
public class ShadowDevicePolicyManager {
  /**
   * @see
   *     https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#setOrganizationColor(android.content.ComponentName,
   *     int)
   */
  private static final int DEFAULT_ORGANIZATION_COLOR = 0xFF008080; // teal

  private ComponentName deviceOwner;
  private ComponentName profileOwner;
  private List<ComponentName> deviceAdmins = new ArrayList<>();
  private List<String> permittedAccessibilityServices = new ArrayList<>();
  private List<String> permittedInputMethods = new ArrayList<>();
  private Map<String, Bundle> applicationRestrictionsMap = new HashMap<>();
  private CharSequence organizationName;
  private int organizationColor;
  private boolean isAutoTimeRequired;
  private int keyguardDisabledFeatures;
  private String lastSetPassword;
  private int requiredPasswordQuality = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
  private int userProvisioningState = DevicePolicyManager.STATE_USER_UNMANAGED;

  private int passwordMinimumLength;
  private int passwordMinimumLetters = 1;
  private int passwordMinimumLowerCase;
  private int passwordMinimumUpperCase;
  private int passwordMinimumNonLetter;
  private int passwordMinimumNumeric = 1;
  private int passwordMinimumSymbols = 1;

  private int wipeCalled;
  private int storageEncryptionStatus;
  private final Set<String> wasHiddenPackages = new HashSet<>();
  private final Set<String> accountTypesWithManagementDisabled = new HashSet<>();
  private final Set<String> systemAppsEnabled = new HashSet<>();
  private final Set<String> uninstallBlockedPackages = new HashSet<>();
  private final Set<String> suspendedPackages = new HashSet<>();
  private final Map<PackageAndPermission, Boolean> appPermissionGrantedMap = new HashMap<>();
  private final Map<PackageAndPermission, Integer> appPermissionGrantStateMap = new HashMap<>();
  private final Map<ComponentName, byte[]> passwordResetTokens = new HashMap<>();
  private final Set<ComponentName> componentsWithActivatedTokens = new HashSet<>();
  private Collection<String> packagesToFailForSetApplicationHidden = Collections.emptySet();
  private Context context;
  private ApplicationPackageManager applicationPackageManager;

  private @RealObject DevicePolicyManager realObject;

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

  @Implementation(minSdk = JELLY_BEAN_MR2)
  public boolean isDeviceOwnerApp(String packageName) {
    return deviceOwner != null && deviceOwner.getPackageName().equals(packageName);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean isProfileOwnerApp(String packageName) {
    return profileOwner != null && profileOwner.getPackageName().equals(packageName);
  }

  @Implementation
  public boolean isAdminActive(ComponentName who) {
    return who != null && deviceAdmins.contains(who);
  }

  @Implementation
  public List<ComponentName> getActiveAdmins() {
    return deviceAdmins;
  }

  @Implementation(minSdk = LOLLIPOP)
  public void addUserRestriction(ComponentName admin, String key) {
    enforceActiveAdmin(admin);
    getShadowUserManager().setUserRestriction(Process.myUserHandle(), key, true);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void clearUserRestriction(ComponentName admin, String key) {
    enforceActiveAdmin(admin);
    getShadowUserManager().setUserRestriction(Process.myUserHandle(), key, false);
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean setApplicationHidden(ComponentName admin, String packageName, boolean hidden) {
    enforceActiveAdmin(admin);
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

  @Implementation(minSdk = LOLLIPOP)
  public boolean isApplicationHidden(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    return applicationPackageManager.getApplicationHiddenSettingAsUser(
        packageName, Process.myUserHandle());
  }

  /** Returns {@code true} if the given {@code packageName} was ever hidden. */
  public boolean wasPackageEverHidden(String packageName) {
    return wasHiddenPackages.contains(packageName);
  }

  @Implementation(minSdk = LOLLIPOP)
  public int enableSystemApp(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    systemAppsEnabled.add(packageName);
    return 1;
  }

  /** Returns {@code true} if the given {@code packageName} was a system app and was enabled. */
  public boolean wasSystemAppEnabled(String packageName) {
    return systemAppsEnabled.contains(packageName);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setUninstallBlocked(
      ComponentName admin, String packageName, boolean uninstallBlocked) {
    enforceActiveAdmin(admin);
    if (uninstallBlocked) {
      uninstallBlockedPackages.add(packageName);
    } else {
      uninstallBlockedPackages.remove(packageName);
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean isUninstallBlocked(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    return uninstallBlockedPackages.contains(packageName);
  }

  /** @see #setDeviceOwner(ComponentName) */
  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected String getDeviceOwner() {
    return deviceOwner != null ? deviceOwner.getPackageName() : null;
  }

  /** @see #setProfileOwner(ComponentName) */
  @Implementation(minSdk = LOLLIPOP)
  protected ComponentName getProfileOwner() {
    return profileOwner;
  }

  private ShadowUserManager getShadowUserManager() {
    return Shadow.extract(context.getSystemService(Context.USER_SERVICE));
  }

  /**
   * Sets the admin as active admin and device owner.
   *
   * @see DevicePolicyManager#getDeviceOwner()
   */
  public void setDeviceOwner(ComponentName admin) {
    setActiveAdmin(admin);
    deviceOwner = admin;
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

  /** Sets the given {@code componentName} as one of the active admins. */
  public void setActiveAdmin(ComponentName componentName) {
    deviceAdmins.add(componentName);
  }

  @Implementation
  public void removeActiveAdmin(ComponentName admin) {
    deviceAdmins.remove(admin);
  }

  @Implementation(minSdk = LOLLIPOP)
  public void clearProfileOwner(ComponentName admin) {
    profileOwner = null;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      removeActiveAdmin(admin);
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  public Bundle getApplicationRestrictions(ComponentName admin, String packageName) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return getApplicationRestrictions(packageName);
  }

  /** Returns all application restrictions of the {@code packageName} in a {@link Bundle}. */
  public Bundle getApplicationRestrictions(String packageName) {
    Bundle bundle = applicationRestrictionsMap.get(packageName);
    // If no restrictions were saved, DPM method should return an empty Bundle as per JavaDoc.
    return bundle != null ? new Bundle(bundle) : new Bundle();
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setApplicationRestrictions(
      ComponentName admin, String packageName, Bundle applicationRestrictions) {
    enforceDeviceOwnerOrProfileOwner(admin);
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

  private void enforceDeviceOwner(ComponentName admin) {
    if (!admin.equals(deviceOwner)) {
      throw new SecurityException("[" + admin + "] is not a device owner");
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

  @Implementation(minSdk = LOLLIPOP)
  public void setAccountManagementDisabled(
      ComponentName admin, String accountType, boolean disabled) {
    enforceDeviceOwnerOrProfileOwner(admin);
    if (disabled) {
      accountTypesWithManagementDisabled.add(accountType);
    } else {
      accountTypesWithManagementDisabled.remove(accountType);
    }
  }

  @Implementation(minSdk = LOLLIPOP)
  public String[] getAccountTypesWithManagementDisabled() {
    return accountTypesWithManagementDisabled.toArray(new String[0]);
  }

  /**
   * Sets organization name.
   *
   * <p>The API can only be called by profile owner since Android N and can be called by both of
   * profile owner and device owner since Android O.
   */
  @Implementation(minSdk = N)
  public void setOrganizationName(ComponentName admin, @Nullable CharSequence name) {
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
    }
    // Throws NameNotFoundException
    context.getPackageManager().getPackageInfo(packageName, 0);
    return suspendedPackages.contains(packageName);
  }

  @Implementation(minSdk = N)
  public void setOrganizationColor(ComponentName admin, int color) {
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
  public CharSequence getOrganizationName(ComponentName admin) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      enforceDeviceOwnerOrProfileOwner(admin);
    } else {
      enforceProfileOwner(admin);
    }

    return organizationName;
  }

  @Implementation(minSdk = N)
  public int getOrganizationColor(ComponentName admin) {
    enforceProfileOwner(admin);
    return organizationColor;
  }

  @Implementation(minSdk = LOLLIPOP)
  public void setAutoTimeRequired(ComponentName admin, boolean required) {
    enforceDeviceOwnerOrProfileOwner(admin);
    isAutoTimeRequired = required;
  }

  @Implementation(minSdk = LOLLIPOP)
  public boolean getAutoTimeRequired() {
    return isAutoTimeRequired;
  }

  /**
   * Sets permitted accessibility services.
   *
   * <p>The API can be called by either a profile or device owner.
   *
   * <p>This method does not check already enabled non-system accessibility services, so will always
   * set the restriction and return true.
   */
  @Implementation(minSdk = LOLLIPOP)
  public boolean setPermittedAccessibilityServices(ComponentName admin, List<String> packageNames) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permittedAccessibilityServices = packageNames;
    return true;
  }

  @Implementation(minSdk = LOLLIPOP)
  @Nullable
  public List<String> getPermittedAccessibilityServices(ComponentName admin) {
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
  @Implementation(minSdk = LOLLIPOP)
  public boolean setPermittedInputMethods(ComponentName admin, List<String> packageNames) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permittedInputMethods = packageNames;
    return true;
  }

  @Implementation(minSdk = LOLLIPOP)
  @Nullable
  public List<String> getPermittedInputMethods(ComponentName admin) {
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
  protected boolean resetPassword(String password, int flags) {
    if (!passwordMeetsRequirements(password)) {
      return false;
    }
    lastSetPassword = password;
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
    return true;
  }

  @Implementation
  protected void setPasswordMinimumLength(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumLength = length;
  }

  @Implementation
  protected void setPasswordMinimumLetters(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumLetters = length;
  }

  @Implementation
  protected void setPasswordMinimumLowerCase(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumLowerCase = length;
  }

  @Implementation
  protected void setPasswordMinimumUpperCase(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumUpperCase = length;
  }

  @Implementation
  protected void setPasswordMinimumNonLetter(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumNonLetter = length;
  }

  @Implementation
  protected void setPasswordMinimumNumeric(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumNumeric = length;
  }

  @Implementation
  protected void setPasswordMinimumSymbols(ComponentName admin, int length) {
    enforceActiveAdmin(admin);
    passwordMinimumSymbols = length;
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

  @Implementation(minSdk = LOLLIPOP)
  protected void addPersistentPreferredActivity(
      ComponentName admin, IntentFilter filter, ComponentName activity) {
    enforceDeviceOwnerOrProfileOwner(admin);

    PackageManager packageManager = context.getPackageManager();
    packageManager.addPreferredActivity(filter, 0, null, activity);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void clearPackagePersistentPreferredActivities(
      ComponentName admin, String packageName) {
    enforceDeviceOwnerOrProfileOwner(admin);
    PackageManager packageManager = context.getPackageManager();
    packageManager.clearPackagePreferredActivities(packageName);
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected void setKeyguardDisabledFeatures(ComponentName admin, int which) {
    enforceActiveAdmin(admin);
    keyguardDisabledFeatures = which;
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected int getKeyguardDisabledFeatures(ComponentName admin) {
    return keyguardDisabledFeatures;
  }

  /**
   * Sets the user provisioning state.
   *
   * @param state to store provisioning state
   */
  public void setUserProvisioningState(int state) {
    userProvisioningState = state;
  }

  /** @return Returns the provisioning state for the current user. */
  @Implementation(minSdk = N)
  protected int getUserProvisioningState() {
    return userProvisioningState;
  }
}
