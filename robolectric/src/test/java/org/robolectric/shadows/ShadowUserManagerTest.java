package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUserManager.UserState;

@RunWith(RobolectricTestRunner.class)
public class ShadowUserManagerTest {

  private UserManager userManager;
  private Context context;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.application;
    userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldGetUserProfiles() {
    assertThat(userManager.getUserProfiles()).contains(Process.myUserHandle());

    UserHandle anotherProfile = newUserHandle(2);
    shadowOf(userManager).addUserProfile(anotherProfile);

    assertThat(userManager.getUserProfiles()).containsOnly(Process.myUserHandle(), anotherProfile);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testGetApplicationRestrictions() {
    String packageName = context.getPackageName();
    assertThat(userManager.getApplicationRestrictions(packageName).size()).isZero();

    Bundle restrictions = new Bundle();
    restrictions.putCharSequence("test_key", "test_value");
    shadowOf(userManager).setApplicationRestrictions(packageName, restrictions);

    assertThat(userManager.getApplicationRestrictions(packageName).getCharSequence("test_key"))
            .isEqualTo("test_value");
  }

  @Test
  @Config(minSdk = N)
  public void isUserUnlocked() {
    assertThat(userManager.isUserUnlocked()).isTrue();
    shadowOf(userManager).setUserUnlocked(false);
    assertThat(userManager.isUserUnlocked()).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void hasUserRestriction() {
    assertThat(userManager.hasUserRestriction(UserManager.ENSURE_VERIFY_APPS)).isFalse();

    UserHandle userHandle = Process.myUserHandle();
    shadowOf(userManager).setUserRestriction(userHandle, UserManager.ENSURE_VERIFY_APPS, true);

    assertThat(userManager.hasUserRestriction(UserManager.ENSURE_VERIFY_APPS)).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getUserRestrictions() {
    assertThat(userManager.getUserRestrictions().size()).isEqualTo(0);

    UserHandle userHandle = Process.myUserHandle();
    shadowOf(userManager).setUserRestriction(userHandle, UserManager.ENSURE_VERIFY_APPS, true);

    Bundle restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.size()).isEqualTo(1);
    assertThat(restrictions.getBoolean(UserManager.ENSURE_VERIFY_APPS)).isTrue();

    shadowOf(userManager).setUserRestriction(newUserHandle(10), UserManager.DISALLOW_CAMERA, true);

    assertThat(userManager.hasUserRestriction(UserManager.DISALLOW_CAMERA)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void clearUserRestrictions() {
    assertThat(userManager.getUserRestrictions().size()).isEqualTo(0);
    shadowOf(userManager)
        .setUserRestriction(Process.myUserHandle(), UserManager.ENSURE_VERIFY_APPS, true);
    assertThat(userManager.getUserRestrictions().size()).isEqualTo(1);

    shadowOf(userManager).clearUserRestrictions(Process.myUserHandle());
    assertThat(userManager.getUserRestrictions().size()).isEqualTo(0);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isManagedProfile() {
    assertThat(userManager.isManagedProfile()).isFalse();
    shadowOf(userManager).setManagedProfile(true);
    assertThat(userManager.isManagedProfile()).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void enforcePermissionChecks() throws Exception {
    shadowOf(userManager).enforcePermissionChecks(true);

    try {
      userManager.isManagedProfile();
      fail("Expected exception");
    } catch (SecurityException expected) {}

    PackageInfo packageInfo = RuntimeEnvironment.application.getPackageManager()
        .getPackageInfo(RuntimeEnvironment.application.getPackageName(),
            PackageManager.GET_PERMISSIONS);
    packageInfo.requestedPermissions = new String[] { permission.MANAGE_USERS };

    shadowOf(userManager).setManagedProfile(true);

    assertThat(userManager.isManagedProfile()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGetSerialNumberForUser() {
    long serialNumberInvalid = -1L;

    UserHandle userHandle = newUserHandle(10);
    assertThat(userManager.getSerialNumberForUser(userHandle)).isEqualTo(serialNumberInvalid);

    shadowOf(userManager).addUserProfile(userHandle);

    assertThat(userManager.getSerialNumberForUser(userHandle)).isNotEqualTo(serialNumberInvalid);
  }

  @Test
  @Config(minSdk = N_MR1)
  public void isDemoUser() {
    // All methods are based on the current user, so no need to pass a UserHandle.
    assertThat(userManager.isDemoUser()).isFalse();

    shadowOf(userManager).setIsDemoUser(true);
    assertThat(userManager.isDemoUser()).isTrue();

    shadowOf(userManager).setIsDemoUser(false);
    assertThat(userManager.isDemoUser()).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void isUserRunning() {
    UserHandle userHandle = newUserHandle(0);

    assertThat(userManager.isUserRunning(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_UNLOCKED);
    assertThat(userManager.isUserRunning(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_LOCKED);
    assertThat(userManager.isUserRunning(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_UNLOCKING);
    assertThat(userManager.isUserRunning(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_STOPPING);
    assertThat(userManager.isUserRunning(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_BOOTING);
    assertThat(userManager.isUserRunning(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_SHUTDOWN);
    assertThat(userManager.isUserRunning(userHandle)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void isUserRunningOrStopping() {
    UserHandle userHandle = newUserHandle(0);

    assertThat(userManager.isUserRunningOrStopping(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_UNLOCKED);
    assertThat(userManager.isUserRunningOrStopping(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_LOCKED);
    assertThat(userManager.isUserRunningOrStopping(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_UNLOCKING);
    assertThat(userManager.isUserRunningOrStopping(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_STOPPING);
    assertThat(userManager.isUserRunningOrStopping(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_BOOTING);
    assertThat(userManager.isUserRunningOrStopping(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_SHUTDOWN);
    assertThat(userManager.isUserRunningOrStopping(userHandle)).isFalse();
  }

  // Create user handle from parcel since UserHandle.of() was only added in later APIs.
  private static UserHandle newUserHandle(int uid) {
    Parcel userParcel = Parcel.obtain();
    userParcel.writeInt(uid);
    userParcel.setDataPosition(0);
    return new UserHandle(userParcel);
  }
}
