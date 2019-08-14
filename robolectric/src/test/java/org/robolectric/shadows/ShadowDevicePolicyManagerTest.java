package org.robolectric.shadows;

import static android.app.admin.DeviceAdminInfo.USES_ENCRYPTED_STORAGE;
import static android.app.admin.DeviceAdminInfo.USES_POLICY_DISABLE_CAMERA;
import static android.app.admin.DeviceAdminInfo.USES_POLICY_EXPIRE_PASSWORD;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED;
import static android.app.admin.DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH;
import static android.app.admin.DevicePolicyManager.STATE_USER_SETUP_COMPLETE;
import static android.app.admin.DevicePolicyManager.STATE_USER_SETUP_INCOMPLETE;
import static android.app.admin.DevicePolicyManager.STATE_USER_UNMANAGED;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowDevicePolicyManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowDevicePolicyManagerTest {

  private DevicePolicyManager devicePolicyManager;
  private UserManager userManager;
  private ComponentName testComponent;
  private PackageManager packageManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    devicePolicyManager =
        (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);

    userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);

    testComponent = new ComponentName("com.example.app", "DeviceAdminReceiver");

    packageManager = context.getPackageManager();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void isDeviceOwnerAppShouldReturnFalseForNonDeviceOwnerApp() {
    // GIVEN an test package which is not the device owner app of the device
    String testPackage = testComponent.getPackageName();

    // WHEN DevicePolicyManager#isDeviceOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isDeviceOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isDeviceOwnerShouldReturnFalseForProfileOwner() {
    // GIVEN an test package which is the profile owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#isDeviceOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isDeviceOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void isDeviceOwnerShouldReturnTrueForDeviceOwner() {
    // GIVEN an test package which is the device owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#isDeviceOwnerApp is called with it
    // THEN the method should return true
    assertThat(devicePolicyManager.isDeviceOwnerApp(testPackage)).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getDeviceOwnerShouldReturnDeviceOwnerPackageName() {
    // GIVEN an test package which is the device owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#getDeviceOwner is called
    // THEN the method should return the package name
    assertThat(devicePolicyManager.getDeviceOwner()).isEqualTo(testPackage);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getDeviceOwnerShouldReturnNullWhenThereIsNoDeviceOwner() {
    // WHEN DevicePolicyManager#getProfileOwner is called without a device owner
    // THEN the method should return null
    assertThat(devicePolicyManager.getDeviceOwner()).isNull();
  }

  @Test
  @Config(minSdk = N)
  public void isDeviceManagedShouldReturnTrueWhenThereIsADeviceOwner() {
    // GIVEN a test component is the device owner app of the device
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#isDeviceManaged is called
    // THEN the method should return true
    assertThat(devicePolicyManager.isDeviceManaged()).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void isDeviceManagedShouldReturnFalseWhenThereIsNoDeviceOwner() {
    // WHEN DevicePolicyManager#isDeviceManaged is called without a device owner
    // THEN the method should return false
    assertThat(devicePolicyManager.isDeviceManaged()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isProfileOwnerAppShouldReturnFalseForNonProfileOwnerApp() {
    // GIVEN an test package which is not the profile owner app of the device
    String testPackage = testComponent.getPackageName();

    // WHEN DevicePolicyManager#isProfileOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isProfileOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isProfileOwnerShouldReturnFalseForDeviceOwner() {
    // GIVEN an test package which is the device owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#isProfileOwnerApp is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isProfileOwnerApp(testPackage)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isProfileOwnerShouldReturnTrueForProfileOwner() {
    // GIVEN an test package which is the profile owner app of the device
    String testPackage = testComponent.getPackageName();
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#isProfileOwnerApp is called with it
    // THEN the method should return true
    assertThat(devicePolicyManager.isProfileOwnerApp(testPackage)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getProfileOwnerShouldReturnDeviceOwnerComponentName() {
    // GIVEN an test package which is the profile owner app of the device
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#getProfileOwner is called
    // THEN the method should return the component
    assertThat(devicePolicyManager.getProfileOwner()).isEqualTo(testComponent);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getProfileOwnerShouldReturnNullWhenThereIsNoProfileOwner() {
    // WHEN DevicePolicyManager#getProfileOwner is called without a profile owner
    // THEN the method should return null
    assertThat(devicePolicyManager.getProfileOwner()).isNull();
  }

  @Test
  public void isAdminActiveShouldReturnFalseForNonAdminDevice() {
    // GIVEN a test component which is not an active admin of the device
    // WHEN DevicePolicyManager#isAdminActive is called with it
    // THEN the method should return false
    assertThat(devicePolicyManager.isAdminActive(testComponent)).isFalse();
  }

  @Test
  public void isAdminActiveShouldReturnTrueForAnyDeviceAdminDevice() {
    // GIVEN a test component which is an active admin of the device
    shadowOf(devicePolicyManager).setActiveAdmin(testComponent);

    // WHEN DevicePolicyManager#isAdminActive is called with it
    // THEN the method should return true
    assertThat(devicePolicyManager.isAdminActive(testComponent)).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getActiveAdminsShouldReturnDeviceOwner() {
    // GIVEN an test package which is the device owner app of the device
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#getActiveAdmins is called
    // THEN the return of the method should include the device owner app
    assertThat(devicePolicyManager.getActiveAdmins()).contains(testComponent);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getActiveAdminsShouldReturnProfileOwner() {
    // GIVEN an test package which is the profile owner app of the device
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#getActiveAdmins is called
    // THEN the return of the method should include the profile owner app
    assertThat(devicePolicyManager.getActiveAdmins()).contains(testComponent);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addUserRestrictionShouldWorkAsIntendedForDeviceOwner() {
    // GIVEN a user restriction to set
    String restrictionKey = "restriction key";

    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN DevicePolicyManager#addUserRestriction is called with the key
    devicePolicyManager.addUserRestriction(testComponent, restrictionKey);

    // THEN the restriction should be set for the current user
    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.getBoolean(restrictionKey)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void addUserRestrictionShouldWorkAsIntendedForProfileOwner() {
    // GIVEN a user restriction to set
    String restrictionKey = "restriction key";

    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN DevicePolicyManager#addUserRestriction is called with the key
    devicePolicyManager.addUserRestriction(testComponent, restrictionKey);

    // THEN the restriction should be set for the current user
    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.getBoolean(restrictionKey)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clearUserRestrictionShouldWorkAsIntendedForActiveAdmins() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN a user restriction has set
    String restrictionKey = "restriction key";
    devicePolicyManager.addUserRestriction(testComponent, restrictionKey);

    // WHEN DevicePolicyManager#clearUserRestriction is called with the key
    devicePolicyManager.clearUserRestriction(testComponent, restrictionKey);

    // THEN the restriction should be cleared for the current user
    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.getBoolean(restrictionKey)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnTrueForNotExistingApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN package that is not installed
    String app = "com.example.non.existing";

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return true
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnFalseForAppsByDefault() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it's never be set hidden or non hidden
    String app = "com.example.non.hidden";
    shadowOf(packageManager).addPackage(app);

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return false
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnTrueForHiddenApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it is hidden
    String hiddenApp = "com.example.hidden";
    shadowOf(packageManager).addPackage(hiddenApp);
    devicePolicyManager.setApplicationHidden(testComponent, hiddenApp, true);

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return true
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, hiddenApp)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isApplicationHiddenShouldReturnFalseForNonHiddenApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it is not hidden
    String nonHiddenApp = "com.example.non.hidden";
    shadowOf(packageManager).addPackage(nonHiddenApp);
    devicePolicyManager.setApplicationHidden(testComponent, nonHiddenApp, false);

    // WHEN DevicePolicyManager#isApplicationHidden is called on the app
    // THEN it should return false
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, nonHiddenApp)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationHiddenShouldBeAbleToUnhideHiddenApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it is hidden
    String app = "com.example.hidden";
    shadowOf(packageManager).addPackage(app);
    devicePolicyManager.setApplicationHidden(testComponent, app, true);

    // WHEN DevicePolicyManager#setApplicationHidden is called on the app to unhide it
    devicePolicyManager.setApplicationHidden(testComponent, app, false);

    // THEN the app shouldn't be hidden anymore
    assertThat(devicePolicyManager.isApplicationHidden(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationHiddenShouldReturnFalseForNotExistingApps() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN an app is not installed
    String app = "com.example.not.installed";

    // THEN DevicePolicyManager#setApplicationHidden returns false
    assertThat(devicePolicyManager.setApplicationHidden(testComponent, app, true)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void wasPackageEverHiddenShouldReturnFalseForPackageNeverHidden() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it's never be set hidden or non hidden
    String app = "com.example.non.hidden";
    shadowOf(packageManager).addPackage(app);

    // WHEN ShadowDevicePolicyManager#wasPackageEverHidden is called with the app
    // THEN it should return false
    assertThat(shadowOf(devicePolicyManager).wasPackageEverHidden(app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void wasPackageEverHiddenShouldReturnTrueForPackageWhichIsHidden() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it's hidden
    String hiddenApp = "com.example.hidden";
    shadowOf(packageManager).addPackage(hiddenApp);
    devicePolicyManager.setApplicationHidden(testComponent, hiddenApp, true);

    // WHEN ShadowDevicePolicyManager#wasPackageEverHidden is called with the app
    // THEN it should return true
    assertThat(shadowOf(devicePolicyManager).wasPackageEverHidden(hiddenApp)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void wasPackageEverHiddenShouldReturnTrueForPackageWhichWasHidden() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app and it was hidden
    String app = "com.example.hidden";
    shadowOf(packageManager).addPackage(app);
    devicePolicyManager.setApplicationHidden(testComponent, app, true);
    devicePolicyManager.setApplicationHidden(testComponent, app, false);

    // WHEN ShadowDevicePolicyManager#wasPackageEverHidden is called with the app
    // THEN it should return true
    assertThat(shadowOf(devicePolicyManager).wasPackageEverHidden(app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void enableSystemAppShouldWorkForActiveAdmins() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN a system app
    String app = "com.example.system";

    // WHEN DevicePolicyManager#enableSystemApp is called with the app
    devicePolicyManager.enableSystemApp(testComponent, app);

    // THEN the app should be enabled
    assertThat(shadowOf(devicePolicyManager).wasSystemAppEnabled(app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isUninstallBlockedShouldReturnFalseForAppsNeverBeingBlocked() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app
    String app = "com.example.app";

    // WHEN DevicePolicyManager#isUninstallBlocked is called with the app
    // THEN it should return false
    assertThat(devicePolicyManager.isUninstallBlocked(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isUninstallBlockedShouldReturnTrueForAppsBeingUnblocked() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app which is blocked from being uninstalled
    String app = "com.example.app";
    devicePolicyManager.setUninstallBlocked(testComponent, app, true);

    // WHEN DevicePolicyManager#UninstallBlocked is called with the app
    // THEN it should return true
    assertThat(devicePolicyManager.isUninstallBlocked(testComponent, app)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isUninstallBlockedShouldReturnFalseForAppsBeingBlocked() {
    // GIVEN the caller is the device owner, and thus an active admin
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app which is unblocked from being uninstalled
    String app = "com.example.app";
    devicePolicyManager.setUninstallBlocked(testComponent, app, true);
    devicePolicyManager.setUninstallBlocked(testComponent, app, false);

    // WHEN DevicePolicyManager#UninstallBlocked is called with the app
    // THEN it should return false
    assertThat(devicePolicyManager.isUninstallBlocked(testComponent, app)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationRestrictionsShouldWorkAsIntendedForDeviceOwner() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an application restriction bundle
    Bundle restrictions = new Bundle();
    restrictions.putString("key", "value");

    // GIVEN an app which the restriction is set to
    String app = "com.example.app";

    // WHEN DevicePolicyManager#setApplicationRestrictions is called to set the restrictions to the
    // app
    devicePolicyManager.setApplicationRestrictions(testComponent, app, restrictions);

    // THEN the restrictions should be set correctly
    Bundle actualRestrictions = devicePolicyManager.getApplicationRestrictions(testComponent, app);
    assertThat(actualRestrictions.getString("key", "default value")).isEqualTo("value");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setApplicationRestrictionsShouldWorkAsIntendedForProfileOwner() {
    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // GIVEN an application restriction bundle
    Bundle restrictions = new Bundle();
    restrictions.putString("key", "value");

    // GIVEN an app which the restriction is set to
    String app = "com.example.app";

    // WHEN DevicePolicyManager#setApplicationRestrictions is called to set the restrictions to the
    // app
    devicePolicyManager.setApplicationRestrictions(testComponent, app, restrictions);

    // THEN the restrictions should be set correctly
    Bundle actualRestrictions = devicePolicyManager.getApplicationRestrictions(testComponent, app);
    assertThat(actualRestrictions.getString("key", "default value")).isEqualTo("value");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getApplicationRestrictionsShouldReturnEmptyBundleIfAppHasNone() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN an app has no restrictions
    String app = "com.example.app";

    // WHEN DevicePolicyManager#getApplicationRestrictions is called to get the restrictions of the
    // app
    // THEN it should return the empty bundle
    assertThat(devicePolicyManager.getApplicationRestrictions(testComponent, app).isEmpty())
        .isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAccountTypesWithManagementDisabledShouldReturnNothingWhenNoAccountIsDislabed() {
    // GIVEN no account type has ever been disabled

    // WHEN get disabled account types using
    // DevicePolicyManager#getAccountTypesWithManagementDisabled
    // THEN it should be empty
    assertThat(devicePolicyManager.getAccountTypesWithManagementDisabled()).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAccountTypesWithManagementDisabledShouldReturnDisabledAccountTypesIfAny() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN a disabled account type
    String disabledAccountType = "com.example.account.type";
    devicePolicyManager.setAccountManagementDisabled(testComponent, disabledAccountType, true);

    // WHEN get disabled account types using
    // DevicePolicyManager#getAccountTypesWithManagementDisabled
    // THEN it should contain the disabled account type
    assertThat(devicePolicyManager.getAccountTypesWithManagementDisabled())
        .isEqualTo(new String[] {disabledAccountType});
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAccountTypesWithManagementDisabledShouldNotReturnReenabledAccountTypesIfAny() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // GIVEN a re-enabled account type
    String reenabledAccountType = "com.example.account.type";
    devicePolicyManager.setAccountManagementDisabled(testComponent, reenabledAccountType, true);
    devicePolicyManager.setAccountManagementDisabled(testComponent, reenabledAccountType, false);

    // WHEN get disabled account types using
    // DevicePolicyManager#getAccountTypesWithManagementDisabled
    // THEN it should not contain the re-enabled account type
    assertThat(devicePolicyManager.getAccountTypesWithManagementDisabled()).isEmpty();
  }

  @Test
  @Config(minSdk = N)
  public void setOrganizationNameShouldWorkForPoSinceN() {
    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN setting an organization name
    String organizationName = "TestOrg";
    devicePolicyManager.setOrganizationName(testComponent, organizationName);

    // THEN the name should be set properly
    assertThat(devicePolicyManager.getOrganizationName(testComponent).toString())
        .isEqualTo(organizationName);
  }

  @Test
  @Config(minSdk = N)
  public void setOrganizationNameShouldClearNameWithEmptyNameForPoSinceN() {
    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // GIVEN that the profile has already set the name TestOrg
    String organizationName = "TestOrg";
    devicePolicyManager.setOrganizationName(testComponent, organizationName);

    // WHEN setting an organization name to empty
    devicePolicyManager.setOrganizationName(testComponent, "");

    // THEN the name should be cleared
    assertThat(devicePolicyManager.getOrganizationName(testComponent)).isNull();
  }

  @Test
  @Config(sdk = N)
  public void setOrganizationNameShouldNotWorkForDoInN() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setting an organization name
    // THEN the method should throw SecurityException
    String organizationName = "TestOrg";
    try {
      devicePolicyManager.setOrganizationName(testComponent, organizationName);
      fail("expected SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = O)
  public void setOrganizationNameShouldWorkForDoSinceO() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setting an organization name
    String organizationName = "TestOrg";
    devicePolicyManager.setOrganizationName(testComponent, organizationName);

    // THEN the name should be set properly
    assertThat(devicePolicyManager.getOrganizationName(testComponent).toString())
        .isEqualTo(organizationName);
  }

  @Test
  @Config(minSdk = N)
  public void setOrganizationColorShouldWorkForPoSinceN() {
    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN setting an organization color
    int color = 0xFFFF00FF;
    devicePolicyManager.setOrganizationColor(testComponent, color);

    // THEN the color should be set properly
    assertThat(devicePolicyManager.getOrganizationColor(testComponent)).isEqualTo(color);
  }

  @Test
  @Config(minSdk = N)
  public void getOrganizationColorShouldReturnDefaultColorIfNothingSet() {
    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN getting an organization color without setting it
    // THEN the color returned should be the default color
    assertThat(devicePolicyManager.getOrganizationColor(testComponent)).isEqualTo(0xFF008080);
  }

  @Test
  @Config(minSdk = N)
  public void setOrganizationColorShouldNotWorkForDo() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setting an organization color
    // THEN the method should throw SecurityException
    int color = 0xFFFF00FF;
    try {
      devicePolicyManager.setOrganizationColor(testComponent, color);
      fail("expected SecurityException");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAutoTimeRequiredShouldWorkAsIntendedForDeviceOwner() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setAutoTimeRequired is called with true
    devicePolicyManager.setAutoTimeRequired(testComponent, true);

    // THEN getAutoTimeRequired should return true
    assertThat(devicePolicyManager.getAutoTimeRequired()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAutoTimeRequiredShouldWorkAsIntendedForProfileOwner() {
    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN setAutoTimeRequired is called with false
    devicePolicyManager.setAutoTimeRequired(testComponent, false);

    // THEN getAutoTimeRequired should return false
    assertThat(devicePolicyManager.getAutoTimeRequired()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getAutoTimeRequiredShouldReturnFalseIfNotSet() {
    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setAutoTimeRequired has not been called
    // THEN getAutoTimeRequired should return false
    assertThat(devicePolicyManager.getAutoTimeRequired()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getPermittedAccessibilityServicesShouldWorkAsIntendedForDeviceOwner() {
    List<String> accessibilityServices =
        Arrays.asList("com.example.accessibility1", "com.example.accessibility2");

    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setPermittedAccessibilityServices is called with a valid list
    devicePolicyManager.setPermittedAccessibilityServices(testComponent, accessibilityServices);

    // THEN getAutoTimeRequired should return the list
    assertThat(devicePolicyManager.getPermittedAccessibilityServices(testComponent))
        .isEqualTo(accessibilityServices);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getPermittedAccessibilityServicesShouldWorkAsIntendedForProfileOwner() {
    List<String> accessibilityServices = new ArrayList<>();

    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN setPermittedAccessibilityServices is called with an empty list
    devicePolicyManager.setPermittedAccessibilityServices(testComponent, accessibilityServices);

    // THEN getAutoTimeRequired should return an empty list
    assertThat(devicePolicyManager.getPermittedAccessibilityServices(testComponent)).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getPermittedAccessibilityServicesShouldReturnNullIfNullIsSet() {
    List<String> accessibilityServices = null;

    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setPermittedAccessibilityServices is called with a null list
    devicePolicyManager.setPermittedAccessibilityServices(testComponent, accessibilityServices);

    // THEN getAutoTimeRequired should return null
    assertThat(devicePolicyManager.getPermittedAccessibilityServices(testComponent)).isNull();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getPermittedInputMethodsShouldWorkAsIntendedForDeviceOwner() {
    List<String> inputMethods = Arrays.asList("com.example.input1", "com.example.input2");

    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setPermittedInputMethods is called with a valid list
    devicePolicyManager.setPermittedInputMethods(testComponent, inputMethods);

    // THEN getAutoTimeRequired should return the list
    assertThat(devicePolicyManager.getPermittedInputMethods(testComponent)).isEqualTo(inputMethods);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getPermittedInputMethodsShouldWorkAsIntendedForProfileOwner() {
    List<String> inputMethods = new ArrayList<>();

    // GIVEN the caller is the profile owner
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    // WHEN setPermittedInputMethods is called with an empty list
    devicePolicyManager.setPermittedInputMethods(testComponent, inputMethods);

    // THEN getAutoTimeRequired should return an empty list
    assertThat(devicePolicyManager.getPermittedInputMethods(testComponent)).isEmpty();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getPermittedInputMethodsShouldReturnNullIfNullIsSet() {
    List<String> inputMethods = null;

    // GIVEN the caller is the device owner
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    // WHEN setPermittedInputMethods is called with a null list
    devicePolicyManager.setPermittedInputMethods(testComponent, inputMethods);

    // THEN getAutoTimeRequired should return null
    assertThat(devicePolicyManager.getPermittedInputMethods(testComponent)).isNull();
  }

  @Test
  public void getStorageEncryptionStatus_defaultValueIsUnsupported() {
    final int status = devicePolicyManager.getStorageEncryptionStatus();
    assertThat(status).isEqualTo(ENCRYPTION_STATUS_UNSUPPORTED);
  }

  @Test
  public void setStorageEncryptionStatus_IllegalValue() {
    try {
      shadowOf(devicePolicyManager).setStorageEncryptionStatus(-1);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).isEqualTo("Unknown status: -1");
    }
  }

  @Test
  public void setStorageEncryptionStatus_Unsupported() {
    shadowOf(devicePolicyManager).setStorageEncryptionStatus(ENCRYPTION_STATUS_UNSUPPORTED);
    assertThat(devicePolicyManager.getStorageEncryptionStatus())
        .isEqualTo(ENCRYPTION_STATUS_UNSUPPORTED);
  }

  @Test
  public void setStorageEncryptionStatus_Active() {
    shadowOf(devicePolicyManager).setStorageEncryptionStatus(ENCRYPTION_STATUS_ACTIVE);
    assertThat(devicePolicyManager.getStorageEncryptionStatus())
        .isEqualTo(ENCRYPTION_STATUS_ACTIVE);
  }

  @Test
  public void setStorageEncryptionStatus_Inactive() {
    shadowOf(devicePolicyManager).setStorageEncryptionStatus(ENCRYPTION_STATUS_INACTIVE);
    assertThat(devicePolicyManager.getStorageEncryptionStatus())
        .isEqualTo(ENCRYPTION_STATUS_INACTIVE);
  }

  @Test
  public void setStorageEncryptionStatus_Activating() {
    shadowOf(devicePolicyManager).setStorageEncryptionStatus(ENCRYPTION_STATUS_ACTIVATING);
    assertThat(devicePolicyManager.getStorageEncryptionStatus())
        .isEqualTo(ENCRYPTION_STATUS_ACTIVATING);
  }

  @Test
  @Config(minSdk = M)
  public void setStorageEncryptionStatus_ActiveDefaultKey() {
    shadowOf(devicePolicyManager).setStorageEncryptionStatus(ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY);
    assertThat(devicePolicyManager.getStorageEncryptionStatus())
        .isEqualTo(ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY);
  }

  @Test
  @Config(minSdk = N)
  public void setStorageEncryptionStatus_ActivePerUser() {
    shadowOf(devicePolicyManager).setStorageEncryptionStatus(ENCRYPTION_STATUS_ACTIVE_PER_USER);
    assertThat(devicePolicyManager.getStorageEncryptionStatus())
        .isEqualTo(ENCRYPTION_STATUS_ACTIVE_PER_USER);
  }

  @Test
  public void setPasswordQuality_Complex() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    devicePolicyManager.setPasswordQuality(
        testComponent, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
    devicePolicyManager.setPasswordMinimumLength(testComponent, 7);
    devicePolicyManager.setPasswordMinimumLetters(testComponent, 2);
    devicePolicyManager.setPasswordMinimumUpperCase(testComponent, 1);

    assertThat(devicePolicyManager.resetPassword("aaaa", 0)).isFalse();
    assertThat(devicePolicyManager.resetPassword("aA2!", 0)).isFalse();
    assertThat(devicePolicyManager.resetPassword("aaaA123", 0)).isFalse();
    assertThat(devicePolicyManager.resetPassword("AAAA123", 0)).isFalse();
    assertThat(devicePolicyManager.resetPassword("!!AAAaaa", 0)).isFalse();
    assertThat(devicePolicyManager.resetPassword("aaAA123!", 0)).isTrue();
  }

  @Test
  public void setPasswordQuality() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    devicePolicyManager.setPasswordQuality(
        testComponent, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);

    assertThat(devicePolicyManager.getPasswordQuality(testComponent))
        .isEqualTo(DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
  }

  @Test
  public void getPasswordQuality_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    devicePolicyManager.setPasswordQuality(
        testComponent, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);

    assertThat(devicePolicyManager.getPasswordQuality(/* admin= */ null))
        .isEqualTo(DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
  }

  @Test
  public void setPasswordMinimumLength() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int length = 6;
    devicePolicyManager.setPasswordMinimumLength(testComponent, length);

    assertThat(devicePolicyManager.getPasswordMinimumLength(testComponent)).isEqualTo(length);
  }

  @Test
  public void getPasswordMinimumLength_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int length = 6;
    devicePolicyManager.setPasswordMinimumLength(testComponent, length);

    assertThat(devicePolicyManager.getPasswordMinimumLength(/* admin= */ null)).isEqualTo(length);
  }

  @Test
  public void setPasswordMinimumLetters() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minLetters = 3;
    devicePolicyManager.setPasswordMinimumLetters(testComponent, minLetters);

    assertThat(devicePolicyManager.getPasswordMinimumLetters(testComponent)).isEqualTo(minLetters);
  }

  @Test
  public void getPasswordMinimumLetters_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minLetters = 3;
    devicePolicyManager.setPasswordMinimumLetters(testComponent, minLetters);

    assertThat(devicePolicyManager.getPasswordMinimumLetters(/* admin= */ null))
        .isEqualTo(minLetters);
  }

  @Test
  public void setPasswordMinimumLowerCase() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minLowerCase = 3;
    devicePolicyManager.setPasswordMinimumLowerCase(testComponent, minLowerCase);

    assertThat(devicePolicyManager.getPasswordMinimumLowerCase(testComponent))
        .isEqualTo(minLowerCase);
  }

  @Test
  public void getPasswordMinimumLowerCase_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minLowerCase = 3;
    devicePolicyManager.setPasswordMinimumLowerCase(testComponent, minLowerCase);

    assertThat(devicePolicyManager.getPasswordMinimumLowerCase(/* admin= */ null))
        .isEqualTo(minLowerCase);
  }

  @Test
  public void setPasswordMinimumUpperCase() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minUpperCase = 3;
    devicePolicyManager.setPasswordMinimumUpperCase(testComponent, minUpperCase);

    assertThat(devicePolicyManager.getPasswordMinimumUpperCase(testComponent))
        .isEqualTo(minUpperCase);
  }

  @Test
  public void getPasswordMinimumUpperCase_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minUpperCase = 3;
    devicePolicyManager.setPasswordMinimumUpperCase(testComponent, minUpperCase);

    assertThat(devicePolicyManager.getPasswordMinimumUpperCase(/* admin= */ null))
        .isEqualTo(minUpperCase);
  }

  @Test
  public void setPasswordMinimumNonLetter() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minNonLetters = 1;
    devicePolicyManager.setPasswordMinimumNonLetter(testComponent, minNonLetters);

    assertThat(devicePolicyManager.getPasswordMinimumNonLetter(testComponent))
        .isEqualTo(minNonLetters);
  }

  @Test
  public void getPasswordMinimumNonLetter_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minNonLetters = 1;
    devicePolicyManager.setPasswordMinimumNonLetter(testComponent, minNonLetters);

    assertThat(devicePolicyManager.getPasswordMinimumNonLetter(/* admin= */ null))
        .isEqualTo(minNonLetters);
  }

  @Test
  public void setPasswordMinimumNumeric() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minNumeric = 5;
    devicePolicyManager.setPasswordMinimumNumeric(testComponent, minNumeric);

    assertThat(devicePolicyManager.getPasswordMinimumNumeric(testComponent)).isEqualTo(minNumeric);
  }

  @Test
  public void getPasswordMinimumNumeric_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minNumeric = 5;
    devicePolicyManager.setPasswordMinimumNumeric(testComponent, minNumeric);

    assertThat(devicePolicyManager.getPasswordMinimumNumeric(/* admin= */ null))
        .isEqualTo(minNumeric);
  }

  @Test
  public void setPasswordMinimumSymbols() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minSymbols = 1;
    devicePolicyManager.setPasswordMinimumSymbols(testComponent, minSymbols);

    assertThat(devicePolicyManager.getPasswordMinimumSymbols(testComponent)).isEqualTo(minSymbols);
  }

  @Test
  public void getPasswordMinimumSymbols_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int minSymbols = 1;
    devicePolicyManager.setPasswordMinimumSymbols(testComponent, minSymbols);

    assertThat(devicePolicyManager.getPasswordMinimumSymbols(/* admin= */ null))
        .isEqualTo(minSymbols);
  }

  @Test
  public void setMaximumFailedPasswordsForWipe() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int maxAttempts = 10;
    devicePolicyManager.setMaximumFailedPasswordsForWipe(testComponent, maxAttempts);

    assertThat(devicePolicyManager.getMaximumFailedPasswordsForWipe(testComponent))
        .isEqualTo(maxAttempts);
  }

  @Test
  public void getMaximumFailedPasswordsForWipe_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int maxAttempts = 10;
    devicePolicyManager.setMaximumFailedPasswordsForWipe(testComponent, maxAttempts);

    assertThat(devicePolicyManager.getMaximumFailedPasswordsForWipe(/* admin= */ null))
        .isEqualTo(maxAttempts);
  }

  @Test
  public void setCameraDisabled() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    devicePolicyManager.setCameraDisabled(testComponent, true);

    assertThat(devicePolicyManager.getCameraDisabled(testComponent)).isTrue();
  }

  @Test
  public void getCameraDisabled_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    devicePolicyManager.setCameraDisabled(testComponent, true);

    assertThat(devicePolicyManager.getCameraDisabled(/* admin= */ null)).isTrue();
  }

  @Test
  public void setPasswordExpirationTimeout() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    long timeMs = 600000;
    devicePolicyManager.setPasswordExpirationTimeout(testComponent, timeMs);

    assertThat(devicePolicyManager.getPasswordExpirationTimeout(testComponent)).isEqualTo(timeMs);
  }

  @Test
  public void getPasswordExpirationTimeout_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    long timeMs = 600000;
    devicePolicyManager.setPasswordExpirationTimeout(testComponent, timeMs);

    assertThat(devicePolicyManager.getPasswordExpirationTimeout(/* admin= */ null))
        .isEqualTo(timeMs);
  }

  @Test
  public void getPasswordExpiration() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    long timeMs = 600000;
    shadowOf(devicePolicyManager).setPasswordExpiration(testComponent, timeMs);

    assertThat(devicePolicyManager.getPasswordExpiration(testComponent)).isEqualTo(timeMs);
  }

  @Test
  public void getPasswordExpiration_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    long timeMs = 600000;
    shadowOf(devicePolicyManager).setPasswordExpiration(testComponent, timeMs);

    assertThat(devicePolicyManager.getPasswordExpiration(/* admin= */ null)).isEqualTo(timeMs);
  }

  @Test
  public void setMaximumTimeToLock() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    long timeMs = 600000;
    devicePolicyManager.setMaximumTimeToLock(testComponent, timeMs);

    assertThat(devicePolicyManager.getMaximumTimeToLock(testComponent)).isEqualTo(timeMs);
  }

  @Test
  public void getMaximumTimeToLock_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    long timeMs = 600000;
    devicePolicyManager.setMaximumTimeToLock(testComponent, timeMs);

    assertThat(devicePolicyManager.getMaximumTimeToLock(/* admin= */ null)).isEqualTo(timeMs);
  }

  @Test
  public void setPasswordHistoryLength() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int length = 100;
    devicePolicyManager.setPasswordHistoryLength(testComponent, length);

    assertThat(devicePolicyManager.getPasswordHistoryLength(testComponent)).isEqualTo(length);
  }

  @Test
  public void getPasswordHistoryLength_nullAdmin() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    int length = 100;
    devicePolicyManager.setPasswordHistoryLength(testComponent, length);

    assertThat(devicePolicyManager.getPasswordHistoryLength(/* admin= */ null)).isEqualTo(length);
  }

  @Test
  public void isActivePasswordSufficient() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(devicePolicyManager).setActivePasswordSufficient(true);

    assertThat(devicePolicyManager.isActivePasswordSufficient()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void getPasswordComplexity() {
    shadowOf(devicePolicyManager).setPasswordComplexity(PASSWORD_COMPLEXITY_HIGH);

    assertThat(devicePolicyManager.getPasswordComplexity()).isEqualTo(PASSWORD_COMPLEXITY_HIGH);
  }

  @Test
  public void setStorageEncryption() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    assertThat(devicePolicyManager.getStorageEncryption(testComponent)).isFalse();

    devicePolicyManager.setStorageEncryption(testComponent, true);

    assertThat(devicePolicyManager.getStorageEncryption(testComponent)).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void setPackagesSuspended_suspendsPossible() throws Exception {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(packageManager).addPackage("installed");
    String[] packages = new String[] {"installed", "not.installed"};

    assertThat(devicePolicyManager.setPackagesSuspended(testComponent, packages, true))
        .isEqualTo(new String[] {"not.installed"});
  }

  @Test
  @Config(minSdk = N)
  public void setPackagesSuspended_activateActive() throws Exception {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(packageManager).addPackage("package");

    assertThat(
            devicePolicyManager.setPackagesSuspended(
                testComponent, new String[] {"package"}, false))
        .isEmpty();
    assertThat(devicePolicyManager.isPackageSuspended(testComponent, "package")).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void setPackagesSuspended_cycleSuspension() throws Exception {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(packageManager).addPackage("package");

    devicePolicyManager.setPackagesSuspended(testComponent, new String[] {"package"}, true);
    devicePolicyManager.setPackagesSuspended(testComponent, new String[] {"package"}, false);

    assertThat(devicePolicyManager.isPackageSuspended(testComponent, "package")).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void isPackagesSuspended_defaultsFalse() throws Exception {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(packageManager).addPackage("package");

    assertThat(devicePolicyManager.isPackageSuspended(testComponent, "package")).isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void isPackagesSuspended_trueForSuspended() throws Exception {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(packageManager).addPackage("package");

    devicePolicyManager.setPackagesSuspended(testComponent, new String[] {"package"}, true);

    assertThat(devicePolicyManager.isPackageSuspended(testComponent, "package")).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void isPackagesSuspended_notInstalledPackage() throws Exception {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    try {
      devicePolicyManager.isPackageSuspended(testComponent, "not.installed");
      fail("expected NameNotFoundException");
    } catch (NameNotFoundException expected) {
      // expected
    }
  }

  @Test
  @Config(minSdk = N)
  public void isLinkedUser() {
    assertThat(devicePolicyManager.getUserProvisioningState()).isEqualTo(STATE_USER_UNMANAGED);

    shadowOf(devicePolicyManager).setUserProvisioningState(STATE_USER_SETUP_COMPLETE);
    assertThat(devicePolicyManager.getUserProvisioningState()).isEqualTo(STATE_USER_SETUP_COMPLETE);

    shadowOf(devicePolicyManager).setUserProvisioningState(STATE_USER_SETUP_INCOMPLETE);
    assertThat(devicePolicyManager.getUserProvisioningState())
        .isEqualTo(STATE_USER_SETUP_INCOMPLETE);

    shadowOf(devicePolicyManager).setUserProvisioningState(STATE_USER_UNMANAGED);
    assertThat(devicePolicyManager.getUserProvisioningState()).isEqualTo(STATE_USER_UNMANAGED);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getProfileOwnerNameAsUser() {
    int userId = 0;
    String orgName = "organization";
    assertThat(devicePolicyManager.getProfileOwnerNameAsUser(userId)).isNull();

    shadowOf(devicePolicyManager).setProfileOwnerName(userId, orgName);

    assertThat(devicePolicyManager.getProfileOwnerNameAsUser(userId)).isEqualTo(orgName);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setPersistentPreferrecActivity_exists() {
    ComponentName randomActivity = new ComponentName("random.package", "Activity");
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);

    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.activityInfo = new ActivityInfo();
    resolveInfo.activityInfo.name = randomActivity.getClassName();
    resolveInfo.activityInfo.applicationInfo = new ApplicationInfo();
    resolveInfo.activityInfo.applicationInfo.packageName = randomActivity.getPackageName();

    ResolveInfo resolveInfo2 = new ResolveInfo();
    resolveInfo2.activityInfo = new ActivityInfo(resolveInfo.activityInfo);
    resolveInfo.activityInfo.name = "OtherActivity";
    shadowOf(packageManager).setResolveInfosForIntent(
        new Intent(Intent.ACTION_MAIN), Arrays.asList(resolveInfo, resolveInfo2));
    shadowOf(packageManager).setShouldShowActivityChooser(true);

    ResolveInfo resolvedActivity =
        packageManager.resolveActivity(new Intent(Intent.ACTION_MAIN), 0);

    assertThat(resolvedActivity.activityInfo.packageName)
        .isNotEqualTo(randomActivity.getPackageName());

    devicePolicyManager.addPersistentPreferredActivity(
        testComponent, new IntentFilter(Intent.ACTION_MAIN), randomActivity);

    resolvedActivity =
        packageManager.resolveActivity(new Intent(Intent.ACTION_MAIN), 0);

    assertThat(resolvedActivity.activityInfo.packageName)
        .isEqualTo(randomActivity.getPackageName());
    assertThat(resolvedActivity.activityInfo.name).isEqualTo(randomActivity.getClassName());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clearPersistentPreferredActivity_packageNotAdded() {
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);
    devicePolicyManager.clearPackagePersistentPreferredActivities(testComponent, "package");

    int preferredActivitiesCount =
        shadowOf(packageManager)
            .getPersistentPreferredActivities(
                new ArrayList<>(), new ArrayList<>(), testComponent.getPackageName());

    assertThat(preferredActivitiesCount).isEqualTo(0);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void clearPersistentPreferredActivity_packageAdded() {
    shadowOf(devicePolicyManager).setDeviceOwner(testComponent);
    ComponentName randomActivity = new ComponentName("random.package", "Activity");
    devicePolicyManager.addPersistentPreferredActivity(
        testComponent, new IntentFilter("Action"), randomActivity);

    int countOfPreferred =
        shadowOf(packageManager)
            .getPersistentPreferredActivities(new ArrayList<>(), new ArrayList<>(), null);

    assertThat(countOfPreferred).isEqualTo(1);

    devicePolicyManager.clearPackagePersistentPreferredActivities(
        testComponent, randomActivity.getPackageName());

    countOfPreferred =
        shadowOf(packageManager)
            .getPersistentPreferredActivities(new ArrayList<>(), new ArrayList<>(), null);
    assertThat(countOfPreferred).isEqualTo(0);
  }

  @Test
  public void grantPolicy_true_onePolicy() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(devicePolicyManager).grantPolicy(testComponent, USES_ENCRYPTED_STORAGE);

    assertThat(devicePolicyManager.hasGrantedPolicy(testComponent, USES_ENCRYPTED_STORAGE))
        .isTrue();
  }

  @Test
  public void grantPolicy_true_twoPolicy() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    shadowOf(devicePolicyManager).grantPolicy(testComponent, USES_ENCRYPTED_STORAGE);
    shadowOf(devicePolicyManager).grantPolicy(testComponent, USES_POLICY_EXPIRE_PASSWORD);

    assertThat(devicePolicyManager.hasGrantedPolicy(testComponent, USES_ENCRYPTED_STORAGE))
        .isTrue();
    assertThat(devicePolicyManager.hasGrantedPolicy(testComponent, USES_POLICY_EXPIRE_PASSWORD))
        .isTrue();
    // USES_POLICY_DISABLE_CAMERA was not granted
    assertThat(devicePolicyManager.hasGrantedPolicy(testComponent, USES_POLICY_DISABLE_CAMERA))
        .isFalse();
  }

  @Test
  public void grantPolicy_false() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    assertThat(devicePolicyManager.hasGrantedPolicy(testComponent, USES_ENCRYPTED_STORAGE))
        .isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getLockTaskPackages_notOwner() {
    try {
      devicePolicyManager.getLockTaskPackages(testComponent);
      fail();
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setLockTaskPackages_notOwner() {
    try {
      devicePolicyManager.setLockTaskPackages(testComponent, new String[] {"allowed.package"});
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getSetLockTaskPackages() {
    shadowOf(devicePolicyManager).setProfileOwner(testComponent);

    assertThat(devicePolicyManager.getLockTaskPackages(testComponent)).isEmpty();

    devicePolicyManager.setLockTaskPackages(testComponent, new String[] {"allowed.package"});

    assertThat(devicePolicyManager.getLockTaskPackages(testComponent))
        .asList()
        .containsExactly("allowed.package");
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isLockTaskPermitted() {
    assertThat(devicePolicyManager.isLockTaskPermitted("allowed.package")).isFalse();

    shadowOf(devicePolicyManager).setProfileOwner(testComponent);
    devicePolicyManager.setLockTaskPackages(testComponent, new String[] {"allowed.package"});

    assertThat(devicePolicyManager.isLockTaskPermitted("allowed.package")).isTrue();
  }

  @Test
  @Config(minSdk = M)
  public void getSystemUpdatePolicyShouldReturnCorrectSetValue_nullAdmin() {
    SystemUpdatePolicy policy = SystemUpdatePolicy.createAutomaticInstallPolicy();
    devicePolicyManager.setSystemUpdatePolicy(null, policy);

    assertThat(devicePolicyManager.getSystemUpdatePolicy()).isEqualTo(policy);
  }

  @Test
  @Config(minSdk = M)
  public void getSystemUpdatePolicyShouldReturnCorrectSetValue_nonNullAdmin() {
    SystemUpdatePolicy policy = SystemUpdatePolicy.createAutomaticInstallPolicy();
    devicePolicyManager.setSystemUpdatePolicy(new ComponentName("testPkg", "testCls"), policy);

    assertThat(devicePolicyManager.getSystemUpdatePolicy()).isEqualTo(policy);
  }

  @Test
  @Config(minSdk = M)
  public void getSystemUpdatePolicyShouldReturnCorrectDefaultValue() {
    assertThat(devicePolicyManager.getSystemUpdatePolicy()).isNull();
  }

  @Test
  @Config(minSdk = M)
  public void getSystemUpdatePolicyShadowShouldReturnCorrectSetValue() {
    SystemUpdatePolicy policy = SystemUpdatePolicy.createAutomaticInstallPolicy();
    shadowOf(devicePolicyManager).setSystemUpdatePolicy(policy);

    assertThat(devicePolicyManager.getSystemUpdatePolicy()).isEqualTo(policy);
  }
}
