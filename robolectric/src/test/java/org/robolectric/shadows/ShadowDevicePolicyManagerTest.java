package org.robolectric.shadows;

import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE;
import static android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED;
import static android.app.admin.DevicePolicyManager.STATE_USER_SETUP_COMPLETE;
import static android.app.admin.DevicePolicyManager.STATE_USER_SETUP_INCOMPLETE;
import static android.app.admin.DevicePolicyManager.STATE_USER_UNMANAGED;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.UserManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowDevicePolicyManager}. */
@RunWith(RobolectricTestRunner.class)
public final class ShadowDevicePolicyManagerTest {

  private DevicePolicyManager devicePolicyManager;
  private UserManager userManager;
  private ComponentName testComponent;
  private PackageManager packageManager;

  @Before
  public void setUp() {
    devicePolicyManager =
        (DevicePolicyManager)
            RuntimeEnvironment.application.getSystemService(Context.DEVICE_POLICY_SERVICE);

    userManager =
        (UserManager) RuntimeEnvironment.application.getSystemService(Context.USER_SERVICE);

    testComponent = new ComponentName("com.example.app", "DeviceAdminReceiver");

    packageManager = RuntimeEnvironment.application.getPackageManager();
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
    assertThat(devicePolicyManager.getOrganizationName(testComponent)).isEqualTo(organizationName);
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
    assertThat(devicePolicyManager.getOrganizationName(testComponent)).isEqualTo(organizationName);
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
}
