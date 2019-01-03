package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest.permission;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUserManager.UserState;

@RunWith(AndroidJUnit4.class)
public class ShadowUserManagerTest {

  private UserManager userManager;
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldGetUserProfiles() {
    assertThat(userManager.getUserProfiles()).contains(Process.myUserHandle());

    UserHandle anotherProfile = newUserHandle(2);
    shadowOf(userManager).addUserProfile(anotherProfile);

    assertThat(userManager.getUserProfiles()).containsExactly(Process.myUserHandle(), anotherProfile);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void testGetApplicationRestrictions() {
    String packageName = context.getPackageName();
    assertThat(userManager.getApplicationRestrictions(packageName).size()).isEqualTo(0);

    Bundle restrictions = new Bundle();
    restrictions.putCharSequence("test_key", "test_value");
    shadowOf(userManager).setApplicationRestrictions(packageName, restrictions);

    assertThat(
            userManager
                .getApplicationRestrictions(packageName)
                .getCharSequence("test_key")
                .toString())
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

    // make sure that the bundle is not an internal state
    restrictions.putBoolean("something", true);
    restrictions = userManager.getUserRestrictions();
    assertThat(restrictions.size()).isEqualTo(1);

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

    Application context = ApplicationProvider.getApplicationContext();
    PackageInfo packageInfo =
        shadowOf(context.getPackageManager())
            .getInternalMutablePackageInfo(context.getPackageName());
    packageInfo.requestedPermissions = new String[] {permission.MANAGE_USERS};
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
  @Config(minSdk = M)
  public void isSystemUser() {
    assertThat(userManager.isSystemUser()).isTrue();

    shadowOf(userManager).setIsSystemUser(false);
    assertThat(userManager.isSystemUser()).isFalse();

    shadowOf(userManager).setIsSystemUser(true);
    assertThat(userManager.isSystemUser()).isTrue();
  }

  @Test
  @Config(minSdk = N)
  public void isPrimaryUser() {
    assertThat(userManager.isPrimaryUser()).isTrue();

    shadowOf(userManager).setIsPrimaryUser(false);
    assertThat(userManager.isPrimaryUser()).isFalse();

    shadowOf(userManager).setIsPrimaryUser(true);
    assertThat(userManager.isPrimaryUser()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void isLinkedUser() {
    assertThat(userManager.isLinkedUser()).isFalse();

    shadowOf(userManager).setIsLinkedUser(true);
    assertThat(userManager.isLinkedUser()).isTrue();

    shadowOf(userManager).setIsLinkedUser(false);
    assertThat(userManager.isLinkedUser()).isFalse();
  }

  @Test
  @Config(minSdk = KITKAT_WATCH)
  public void isGuestUser() {
    assertThat(userManager.isGuestUser()).isFalse();

    shadowOf(userManager).setIsGuestUser(true);
    assertThat(userManager.isGuestUser()).isTrue();

    shadowOf(userManager).setIsGuestUser(false);
    assertThat(userManager.isGuestUser()).isFalse();
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

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void addSecondaryUser() {
    assertThat(userManager.getUserCount()).isEqualTo(1);
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(userManager.getUserCount()).isEqualTo(2);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void removeSecondaryUser() {
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(shadowOf(userManager).removeUser(10)).isTrue();
    assertThat(userManager.getUserCount()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void switchToSecondaryUser() {
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    shadowOf(userManager).switchUser(10);
    assertThat(UserHandle.myUserId()).isEqualTo(10);
  }

  @Test
  @Config(minSdk = N)
  public void canSwitchUser() {
    assertThat(shadowOf(userManager).canSwitchUsers()).isFalse();
    shadowOf(userManager).setCanSwitchUser(true);
    assertThat(shadowOf(userManager).canSwitchUsers()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getUsers() {
    assertThat(userManager.getUsers()).hasSize(1);
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(userManager.getUsers()).hasSize(2);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getUserInfo() {
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(userManager.getUserInfo(10)).isNotNull();
    assertThat(userManager.getUserInfo(10).name).isEqualTo("secondary_user");
  }

  @Test
  @Config(minSdk = N)
  public void switchToUserNotAddedShouldThrowException() {
    try {
      shadowOf(userManager).switchUser(10);
      fail("Switching to the user that was never added should throw UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      assertThat(e).hasMessageThat().isEqualTo("Must add user before switching to it");
    }
  }

  // Create user handle from parcel since UserHandle.of() was only added in later APIs.
  private static UserHandle newUserHandle(int uid) {
    Parcel userParcel = Parcel.obtain();
    userParcel.writeInt(uid);
    userParcel.setDataPosition(0);
    return new UserHandle(userParcel);
  }
}
