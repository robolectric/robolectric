package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.Shadows.shadowOf;

import android.annotation.Nullable;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.UserManager;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link DevicePolicyManager} */
@Implements(DevicePolicyManager.class)
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

  private final Set<String> hiddenPackages = new HashSet<>();
  private final Set<String> wasHiddenPackages = new HashSet<>();
  private final Set<String> accountTypesWithManagementDisabled = new HashSet<>();
  private final Set<String> systemAppsEnabled = new HashSet<>();
  private final Set<String> uninstallBlockedPackages = new HashSet<>();

  public ShadowDevicePolicyManager() {
    organizationColor = DEFAULT_ORGANIZATION_COLOR;
  }

  @Implementation
  public boolean isDeviceOwnerApp(String packageName) {
    return deviceOwner != null && deviceOwner.getPackageName().equals(packageName);
  }

  @Implementation
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

  @Implementation
  public void addUserRestriction(ComponentName admin, String key) {
    enforceActiveAdmin(admin);
    getShadowUserManager().setUserRestriction(Process.myUserHandle(), key, true);
  }

  @Implementation
  public void clearUserRestriction(ComponentName admin, String key) {
    enforceActiveAdmin(admin);
    getShadowUserManager().setUserRestriction(Process.myUserHandle(), key, false);
  }

  @Implementation
  public void setApplicationHidden(ComponentName admin, String packageName, boolean hidden) {
    enforceActiveAdmin(admin);
    if (hidden) {
      hiddenPackages.add(packageName);
      wasHiddenPackages.add(packageName);
    } else {
      hiddenPackages.remove(packageName);
    }
  }

  @Implementation
  public boolean isApplicationHidden(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    return hiddenPackages.contains(packageName);
  }

  /** Returns {@code true} if the given {@code packageName} was ever hidden. */
  public boolean wasPackageEverHidden(String packageName) {
    return wasHiddenPackages.contains(packageName);
  }

  @Implementation
  public int enableSystemApp(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    systemAppsEnabled.add(packageName);
    return 1;
  }

  /** Returns {@code true} if the given {@code packageName} was a system app and was enabled. */
  public boolean wasSystemAppEnabled(String packageName) {
    return systemAppsEnabled.contains(packageName);
  }

  @Implementation
  public void setUninstallBlocked(
      ComponentName admin, String packageName, boolean uninstallBlocked) {
    enforceActiveAdmin(admin);
    if (uninstallBlocked) {
      uninstallBlockedPackages.add(packageName);
    } else {
      uninstallBlockedPackages.remove(packageName);
    }
  }

  @Implementation
  public boolean isUninstallBlocked(ComponentName admin, String packageName) {
    enforceActiveAdmin(admin);
    return uninstallBlockedPackages.contains(packageName);
  }

  private ShadowUserManager getShadowUserManager() {
    return shadowOf(
        (UserManager) RuntimeEnvironment.application.getSystemService(Context.USER_SERVICE));
  }

  /** Sets the admin as active admin and device owner. */
  public void setDeviceOwner(ComponentName admin) {
    setActiveAdmin(admin);
    deviceOwner = admin;
  }

  /** Sets the admin as active admin and profile owner. */
  public void setProfileOwner(ComponentName admin) {
    setActiveAdmin(admin);
    profileOwner = admin;
  }

  /** Sets the given {@code componentName} as one of the active admins. */
  public void setActiveAdmin(ComponentName componentName) {
    deviceAdmins.add(componentName);
  }

  @Implementation
  public Bundle getApplicationRestrictions(ComponentName admin, String packageName) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return getApplicationRestrictions(packageName);
  }

  /** Returns all application restrictions of the {@code packageName} in a {@link Bundle}. */
  public Bundle getApplicationRestrictions(String packageName) {
    Bundle bundle = applicationRestrictionsMap.get(packageName);
    // If no restrictions were saved, DPM method should return an empty Bundle as per JavaDoc.
    return bundle != null ? bundle : Bundle.EMPTY;
  }

  @Implementation
  public void setApplicationRestrictions(
      ComponentName admin, String packageName, Bundle applicationRestrictions) {
    enforceDeviceOwnerOrProfileOwner(admin);
    setApplicationRestrictions(packageName, applicationRestrictions);
  }

  /**
   * Sets the application restrictions of the {@code packageName}.
   *
   * The new {@code applicationRestrictions} always completely overwrites any existing ones.
   */
  public void setApplicationRestrictions(String packageName, Bundle applicationRestrictions) {
    applicationRestrictionsMap.put(packageName, applicationRestrictions);
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

  @Implementation
  public void setAccountManagementDisabled(
      ComponentName admin, String accountType, boolean disabled) {
    enforceDeviceOwnerOrProfileOwner(admin);
    if (disabled) {
      accountTypesWithManagementDisabled.add(accountType);
    } else {
      accountTypesWithManagementDisabled.remove(accountType);
    }
  }

  @Implementation
  public String[] getAccountTypesWithManagementDisabled() {
    return accountTypesWithManagementDisabled.toArray(new String[0]);
  }

  /**
   * Sets organization name.
   *
   * The API can only be called by profile owner since Android N and can be called by both of
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
  public void setOrganizationColor(ComponentName admin, int color) {
    enforceProfileOwner(admin);
    organizationColor = color;
  }

  /**
   * Returns organization name.
   *
   * The API can only be called by profile owner since Android N.
   *
   * Android framework has a hidden API for getting the organization name for device owner since
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

  @Implementation
  public void setAutoTimeRequired(ComponentName admin, boolean required) {
    enforceDeviceOwnerOrProfileOwner(admin);
    isAutoTimeRequired = required;
  }

  @Implementation
  public boolean getAutoTimeRequired() {
    return isAutoTimeRequired;
  }

  /**
   * Sets permitted accessibility services.
   *
   * The API can be called by either a profile or device owner.
   *
   * This method does not check already enabled non-system accessibility services, so will always
   * set the restriction and return true.
   */
  @Implementation
  public boolean setPermittedAccessibilityServices(ComponentName admin, List<String> packageNames) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permittedAccessibilityServices = packageNames;
    return true;
  }

  @Implementation
  @Nullable
  public List<String> getPermittedAccessibilityServices(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return permittedAccessibilityServices;
  }

  /**
   * Sets permitted input methods.
   *
   * The API can be called by either a profile or device owner.
   *
   * This method does not check already enabled non-system input methods, so will always set the
   * restriction and return true.
   */
  @Implementation
  public boolean setPermittedInputMethods(ComponentName admin, List<String> packageNames) {
    enforceDeviceOwnerOrProfileOwner(admin);
    permittedInputMethods = packageNames;
    return true;
  }

  @Implementation
  @Nullable
  public List<String> getPermittedInputMethods(ComponentName admin) {
    enforceDeviceOwnerOrProfileOwner(admin);
    return permittedInputMethods;
  }
}
