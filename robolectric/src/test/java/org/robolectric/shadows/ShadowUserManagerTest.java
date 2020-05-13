package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest.permission;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.UserInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowUserManager.UserState;

@RunWith(AndroidJUnit4.class)
public class ShadowUserManagerTest {

  private UserManager userManager;
  private Context context;

  private static final int TEST_USER_HANDLE = 0;
  private static final int PROFILE_USER_HANDLE = 2;
  private static final String PROFILE_USER_NAME = "profile";
  private static final String SEED_ACCOUNT_TYPE = "seed_account_type";
  private static final int PROFILE_USER_FLAGS = 0;

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
  @Config(minSdk = N)
  public void isManagedProfileWithHandle() {
    shadowOf(userManager).addUser(12, "secondary user", 0);
    shadowOf(userManager)
        .addProfile(12, 13, "another managed profile", ShadowUserManager.FLAG_MANAGED_PROFILE);
    assertThat(userManager.isManagedProfile(13)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void enforcePermissionChecks() throws Exception {
    shadowOf(userManager).enforcePermissionChecks(true);

    try {
      userManager.isManagedProfile();
      fail("Expected exception");
    } catch (SecurityException expected) {}

    setPermissions(permission.MANAGE_USERS);

    shadowOf(userManager).setManagedProfile(true);

    assertThat(userManager.isManagedProfile()).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGetSerialNumberForUser() {
    long serialNumberInvalid = -1L;

    UserHandle userHandle = newUserHandle(10);
    assertThat(userManager.getSerialNumberForUser(userHandle)).isEqualTo(serialNumberInvalid);
    assertThat(userManager.getUserSerialNumber(userHandle.getIdentifier()))
        .isEqualTo(serialNumberInvalid);

    shadowOf(userManager).addUserProfile(userHandle);

    assertThat(userManager.getSerialNumberForUser(userHandle)).isNotEqualTo(serialNumberInvalid);
    assertThat(userManager.getUserSerialNumber(userHandle.getIdentifier()))
        .isNotEqualTo(serialNumberInvalid);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getUserForNonExistSerialNumber() {
    long nonExistSerialNumber = 121;
    assertThat(userManager.getUserForSerialNumber(nonExistSerialNumber)).isNull();
    assertThat(userManager.getUserHandle((int) nonExistSerialNumber)).isEqualTo(-1);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGetSerialNumberForProfile() {
    long serialNumberInvalid = -1L;

    assertThat(userManager.getUserSerialNumber(11)).isEqualTo(serialNumberInvalid);
    shadowOf(userManager).addProfile(10, 11, "profile", 0);
    assertThat(userManager.getUserSerialNumber(11)).isNotEqualTo(serialNumberInvalid);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGetUserHandleFromSerialNumberForProfile() {
    long serialNumberInvalid = -1L;

    shadowOf(userManager).addProfile(10, 11, "profile", 0);
    long serialNumber = userManager.getUserSerialNumber(11);
    assertThat(serialNumber).isNotEqualTo(serialNumberInvalid);
    assertThat(userManager.getUserHandle((int) serialNumber)).isEqualTo(11);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getUserHandle() {
    UserHandle expectedUserHandle = shadowOf(userManager).addUser(10, "secondary_user", 0);

    long serialNumber = userManager.getUserSerialNumber(10);
    int actualUserHandle = shadowOf(userManager).getUserHandle((int) serialNumber);
    assertThat(actualUserHandle).isEqualTo(expectedUserHandle.getIdentifier());
  }

  @Test
  @Config(minSdk = N_MR1, maxSdk = Q)
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
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void isRestrictedProfile() {
    assertThat(userManager.isRestrictedProfile()).isFalse();

    shadowOf(userManager).setIsRestrictedProfile(true);
    assertThat(userManager.isRestrictedProfile()).isTrue();

    shadowOf(userManager).setIsRestrictedProfile(false);
    assertThat(userManager.isRestrictedProfile()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.Q)
  public void setSeedAccountType() {
    assertThat(userManager.getSeedAccountType()).isNull();

    shadowOf(userManager).setSeedAccountType(SEED_ACCOUNT_TYPE);
    assertThat(userManager.getSeedAccountType()).isEqualTo(SEED_ACCOUNT_TYPE);
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
  @Config(minSdk = 24)
  public void isUserUnlockedByUserHandle() {
    UserHandle userHandle = newUserHandle(0);

    assertThat(userManager.isUserUnlocked(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_UNLOCKED);
    assertThat(userManager.isUserUnlocked(userHandle)).isTrue();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_LOCKED);
    assertThat(userManager.isUserUnlocked(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_RUNNING_UNLOCKING);
    assertThat(userManager.isUserUnlocked(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_STOPPING);
    assertThat(userManager.isUserUnlocked(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_BOOTING);
    assertThat(userManager.isUserUnlocked(userHandle)).isFalse();

    shadowOf(userManager).setUserState(userHandle, UserState.STATE_SHUTDOWN);
    assertThat(userManager.isUserUnlocked(userHandle)).isFalse();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void addSecondaryUser() {
    assertThat(userManager.getUserCount()).isEqualTo(1);
    UserHandle userHandle = shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(userHandle.getIdentifier()).isEqualTo(10);
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
  @Config(minSdk = Q)
  public void removeSecondaryUser_withUserHandle() {
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(shadowOf(userManager).removeUser(UserHandle.of(10))).isTrue();
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
  public void canSwitchUsers() {
    shadowOf(userManager).setCanSwitchUser(false);
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
    shadowOf(userManager).addProfile(10, 11, "profile", 0);
    assertThat(userManager.getUsers()).hasSize(3);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getUserInfo() {
    shadowOf(userManager).addUser(10, "secondary_user", 0);
    assertThat(userManager.getUserInfo(10)).isNotNull();
    assertThat(userManager.getUserInfo(10).name).isEqualTo("secondary_user");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getUserInfoOfProfile() {
    shadowOf(userManager).addProfile(10, 11, "profile_user", 0);
    shadowOf(userManager).addProfile(10, 12, "profile_user_2", 0);
    shadowOf(userManager).addProfile(13, 14, "profile_user_3", 0);
    assertThat(userManager.getUserInfo(11)).isNotNull();
    assertThat(userManager.getUserInfo(11).name).isEqualTo("profile_user");
    assertThat(userManager.getUserInfo(12)).isNotNull();
    assertThat(userManager.getUserInfo(12).name).isEqualTo("profile_user_2");
    assertThat(userManager.getUserInfo(14)).isNotNull();
    assertThat(userManager.getUserInfo(14).name).isEqualTo("profile_user_3");
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

  @Test
  @Config(minSdk = LOLLIPOP)
  public void getProfiles_addedProfile_containsProfile() {
    shadowOf(userManager).addUser(TEST_USER_HANDLE, "", 0);
    shadowOf(userManager).addProfile(
        TEST_USER_HANDLE, PROFILE_USER_HANDLE, PROFILE_USER_NAME, PROFILE_USER_FLAGS);

    // getProfiles(userId) include user itself and asssociated profiles.
    assertThat(userManager.getProfiles(TEST_USER_HANDLE).get(0).id).isEqualTo(TEST_USER_HANDLE);
    assertThat(userManager.getProfiles(TEST_USER_HANDLE).get(1).id).isEqualTo(PROFILE_USER_HANDLE);
  }

  @Test
  @Config(minSdk = N)
  public void supportsMultipleUsers() {
    assertThat(UserManager.supportsMultipleUsers()).isFalse();

    shadowOf(userManager).setSupportsMultipleUsers(true);
    assertThat(UserManager.supportsMultipleUsers()).isTrue();
  }


  @Test
  @Config(minSdk = Q)
  public void getUserSwitchability_shouldReturnLastSetSwitchability() {
    assertThat(userManager.getUserSwitchability()).isEqualTo(UserManager.SWITCHABILITY_STATUS_OK);
    shadowOf(userManager)
        .setUserSwitchability(UserManager.SWITCHABILITY_STATUS_USER_SWITCH_DISALLOWED);
    assertThat(userManager.getUserSwitchability())
        .isEqualTo(UserManager.SWITCHABILITY_STATUS_USER_SWITCH_DISALLOWED);
    shadowOf(userManager)
        .setUserSwitchability(UserManager.SWITCHABILITY_STATUS_OK);
    assertThat(userManager.getUserSwitchability()).isEqualTo(UserManager.SWITCHABILITY_STATUS_OK);
  }

  @Test
  @Config(minSdk = Q)
  public void setCanSwitchUser_shouldChangeSwitchabilityState() {
    shadowOf(userManager).setCanSwitchUser(false);
    assertThat(userManager.getUserSwitchability())
        .isEqualTo(UserManager.SWITCHABILITY_STATUS_USER_SWITCH_DISALLOWED);
    shadowOf(userManager).setCanSwitchUser(true);
    assertThat(userManager.getUserSwitchability()).isEqualTo(UserManager.SWITCHABILITY_STATUS_OK);
  }

  @Test
  @Config(minSdk = N, maxSdk = Q)
  public void canSwitchUser_shouldReflectSwitchabilityState() {
    shadowOf(userManager)
        .setUserSwitchability(UserManager.SWITCHABILITY_STATUS_USER_SWITCH_DISALLOWED);
    assertThat(userManager.canSwitchUsers()).isFalse();
    shadowOf(userManager)
        .setUserSwitchability(UserManager.SWITCHABILITY_STATUS_OK);
    assertThat(userManager.canSwitchUsers()).isTrue();
  }

  @Test
  @Config(minSdk = Q)
  public void getUserName_shouldReturnSetUserName() {
    shadowOf(userManager).setUserSwitchability(UserManager.SWITCHABILITY_STATUS_OK);
    shadowOf(userManager).addUser(10, PROFILE_USER_NAME, /* flags = */ 0);
    shadowOf(userManager).switchUser(10);

    assertThat(userManager.getUserName()).isEqualTo(PROFILE_USER_NAME);
  }

  @Test
  @Config(minSdk = O)
  public void isQuietModeEnabled_shouldReturnFalse() {
    assertThat(userManager.isQuietModeEnabled(Process.myUserHandle())).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void isQuietModeEnabled_withProfile_shouldReturnFalse() {
    shadowOf(userManager).addProfile(0, 10, "Work profile", UserInfo.FLAG_MANAGED_PROFILE);

    assertThat(userManager.isQuietModeEnabled(new UserHandle(10))).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void requestQuietModeEnabled_withoutPermission_shouldThrowException() {
    shadowOf(userManager).enforcePermissionChecks(true);

    shadowOf(userManager).addProfile(0, 10, "Work profile", UserInfo.FLAG_MANAGED_PROFILE);

    UserHandle workHandle = new UserHandle(10);
    try {
      userManager.requestQuietModeEnabled(true, workHandle);
      fail("Expected SecurityException.");
    } catch (SecurityException expected) {
    }
  }

  @Test
  @Config(minSdk = Q)
  public void requestQuietModeEnabled_withManagedProfile_shouldStopProfileAndEmitBroadcast() {
    shadowOf(userManager).enforcePermissionChecks(true);
    setPermissions(permission.MODIFY_QUIET_MODE);

    UserHandle workHandle =
        shadowOf(userManager).addUser(10, "Work profile", UserInfo.FLAG_MANAGED_PROFILE);
    shadowOf(userManager).setUserState(workHandle, UserState.STATE_RUNNING_UNLOCKED);

    final AtomicReference<String> receivedAction = new AtomicReference<>();
    final AtomicReference<UserHandle> receivedHandle = new AtomicReference<>();

    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            receivedAction.set(intent.getAction());
            receivedHandle.set(intent.getParcelableExtra(Intent.EXTRA_USER));
          }
        };
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
    intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
    context.registerReceiver(receiver, intentFilter);

    assertThat(userManager.requestQuietModeEnabled(true, workHandle)).isFalse();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(userManager.isQuietModeEnabled(workHandle)).isTrue();
    assertThat(userManager.isUserRunning(workHandle)).isFalse();
    assertThat(userManager.getUserInfo(10).flags & UserInfo.FLAG_QUIET_MODE)
        .isEqualTo(UserInfo.FLAG_QUIET_MODE);
    assertThat(receivedAction.get()).isEqualTo(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
    assertThat(receivedHandle.get()).isEqualTo(workHandle);
  }

  @Test
  @Config(minSdk = Q)
  public void requestQuietModeDisabled_withManagedProfile_shouldStartProfileAndEmitBroadcast() {
    shadowOf(userManager).enforcePermissionChecks(true);
    setPermissions(permission.MODIFY_QUIET_MODE);

    UserHandle workHandle =
        shadowOf(userManager)
            .addUser(10, "Work profile", UserInfo.FLAG_MANAGED_PROFILE | UserInfo.FLAG_QUIET_MODE);
    shadowOf(userManager).setUserState(workHandle, UserState.STATE_SHUTDOWN);

    final AtomicReference<String> receivedAction = new AtomicReference<>();
    final AtomicReference<UserHandle> receivedHandle = new AtomicReference<>();

    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            receivedAction.set(intent.getAction());
            receivedHandle.set(intent.getParcelableExtra(Intent.EXTRA_USER));
          }
        };
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
    intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
    context.registerReceiver(receiver, intentFilter);

    assertThat(userManager.requestQuietModeEnabled(false, workHandle)).isFalse();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(userManager.isQuietModeEnabled(workHandle)).isFalse();
    assertThat(userManager.isUserRunning(workHandle)).isTrue();
    assertThat(userManager.getUserInfo(10).flags & UserInfo.FLAG_QUIET_MODE).isEqualTo(0);
    assertThat(receivedAction.get()).isEqualTo(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
    assertThat(receivedHandle.get()).isEqualTo(workHandle);
  }

  @Test
  @Config(minSdk = Q)
  public void requestQuietModeDisabled_withLockedManagedProfile_shouldNotDoAnything() {
    shadowOf(userManager).enforcePermissionChecks(true);
    setPermissions(permission.MODIFY_QUIET_MODE);

    UserHandle workHandle =
        shadowOf(userManager)
            .addUser(10, "Work profile", UserInfo.FLAG_MANAGED_PROFILE | UserInfo.FLAG_QUIET_MODE);

    final AtomicReference<String> receivedAction = new AtomicReference<>();
    final AtomicReference<UserHandle> receivedHandle = new AtomicReference<>();

    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            receivedAction.set(intent.getAction());
            receivedHandle.set(intent.getParcelableExtra(Intent.EXTRA_USER));
          }
        };
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_AVAILABLE);
    intentFilter.addAction(Intent.ACTION_MANAGED_PROFILE_UNAVAILABLE);
    context.registerReceiver(receiver, intentFilter);

    shadowOf(userManager).setProfileIsLocked(workHandle, true);

    assertThat(userManager.requestQuietModeEnabled(false, workHandle)).isTrue();
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(userManager.isQuietModeEnabled(workHandle)).isTrue();
    assertThat(userManager.isUserRunning(workHandle)).isFalse();
    assertThat(userManager.getUserInfo(10).flags & UserInfo.FLAG_QUIET_MODE)
        .isEqualTo(UserInfo.FLAG_QUIET_MODE);
    assertThat(receivedAction.get()).isNull();
    assertThat(receivedHandle.get()).isNull();
  }

  // Create user handle from parcel since UserHandle.of() was only added in later APIs.
  private static UserHandle newUserHandle(int uid) {
    Parcel userParcel = Parcel.obtain();
    userParcel.writeInt(uid);
    userParcel.setDataPosition(0);
    return new UserHandle(userParcel);
  }

  private static void setPermissions(String... permissions) {
    Application context = ApplicationProvider.getApplicationContext();
    PackageInfo packageInfo =
        shadowOf(context.getPackageManager())
            .getInternalMutablePackageInfo(context.getPackageName());
    packageInfo.requestedPermissions = permissions;
  }
}
