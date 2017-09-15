package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;
import android.os.UserManager;
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

  private ComponentName deviceOwner;
  private ComponentName profileOwner;
  private List<ComponentName> deviceAdmins = new ArrayList<>();
  private Map<String, Bundle> applicationRestrictionsMap = new HashMap<>();

  private final Set<String> hiddenPackages = new HashSet<>();
  private final Set<String> wasHiddenPackages = new HashSet<>();
  private final Set<String> accountTypesWithManagementDisabled = new HashSet<>();
  private final Set<String> systemAppsEnabled = new HashSet<>();
  private final Set<String> uninstallBlockedPackages = new HashSet<>();

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
   * <p>The new {@code applicationRestrictions} always completely overwrites any existing ones.
   */
  public void setApplicationRestrictions(String packageName, Bundle applicationRestrictions) {
    applicationRestrictionsMap.put(packageName, applicationRestrictions);
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
}
