package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;
import androidx.test.core.content.pm.PackageInfoBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;

/** Unit tests for {@link org.robolectric.shadows.ShadowRoleManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public final class ShadowRoleManagerTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private final RoleManager roleManager = getApplication().getSystemService(RoleManager.class);

  @Test
  public void isRoleHeld_shouldThrowWithNullArgument() {
    assertThrows(IllegalArgumentException.class, () -> shadowOf(roleManager).isRoleHeld(null));
  }

  @Test
  public void addHeldRole_isPresentInIsRoleHeld() {
    shadowOf(roleManager).addHeldRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleHeld(RoleManager.ROLE_SMS)).isTrue();
  }

  @Test
  public void removeHeldRole_notPresentInIsRoleHeld() {
    shadowOf(roleManager).addHeldRole(RoleManager.ROLE_SMS);
    shadowOf(roleManager).removeHeldRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleHeld(RoleManager.ROLE_SMS)).isFalse();
  }

  @Test
  public void isRoleHeld_noValueByDefault() {
    assertThat(roleManager.isRoleHeld(RoleManager.ROLE_SMS)).isFalse();
  }

  @Test
  public void isRoleAvailable_shouldThrowWithNullArgument() {
    assertThrows(IllegalArgumentException.class, () -> shadowOf(roleManager).isRoleAvailable(null));
  }

  @Test
  public void addAvailableRole_isPresentInIsRoleAvailable() {
    shadowOf(roleManager).addAvailableRole("some.weird.role");
    assertThat(roleManager.isRoleAvailable("some.weird.role")).isTrue();
  }

  @Test
  public void addAvailableRole_shouldThrowWithEmptyArgument() {
    assertThrows(IllegalArgumentException.class, () -> shadowOf(roleManager).addAvailableRole(""));
  }

  @Test
  public void removeAvailableRole_notPresentInIsRoleAvailable() {
    shadowOf(roleManager).addAvailableRole(RoleManager.ROLE_SMS);
    shadowOf(roleManager).removeAvailableRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleAvailable(RoleManager.ROLE_SMS)).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getDefaultApplication_shouldReturnRoleOwner() {
    shadowOf(roleManager).addHeldRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.getDefaultApplication(RoleManager.ROLE_SMS))
        .isEqualTo(getApplication().getPackageName());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getDefaultApplication_shouldReturnPackageSet() {
    shadowOf(roleManager).addAvailableRole(RoleManager.ROLE_SMS);
    shadowOf(getApplication().getPackageManager())
        .installPackage(PackageInfoBuilder.newBuilder().setPackageName("test.app").build());
    AtomicBoolean resultHolder = new AtomicBoolean(false);
    shadowOf(roleManager)
        .setDefaultApplication(
            RoleManager.ROLE_SMS, "test.app", 0, directExecutor(), resultHolder::set);
    assertThat(roleManager.getDefaultApplication(RoleManager.ROLE_SMS)).isEqualTo("test.app");
    assertThat(resultHolder.get()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setDefaultApplication_checksAppInstalled() {
    AtomicBoolean resultHolder = new AtomicBoolean(true);
    shadowOf(roleManager)
        .setDefaultApplication(
            RoleManager.ROLE_SMS, "test.app", 0, directExecutor(), resultHolder::set);
    assertThat(resultHolder.get()).isFalse();
    assertThat(roleManager.getDefaultApplication(RoleManager.ROLE_SMS)).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setDefaultApplication_checksRoleAllowed() {
    shadowOf(getApplication().getPackageManager())
        .installPackage(PackageInfoBuilder.newBuilder().setPackageName("test.app").build());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            shadowOf(roleManager)
                .setDefaultApplication(
                    "bogus.role", "test.app", 0, directExecutor(), result -> {}));
  }

  @Test
  public void roleManager_activityContextEnabled_differentInstancesRetrieveRoles() {
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      RoleManager applicationRoleManager = roleManager;

      Activity activity = controller.get();
      RoleManager activityRoleManager =
          (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);

      assertThat(applicationRoleManager).isNotSameInstanceAs(activityRoleManager);

      boolean applicationRoleHeld = applicationRoleManager.isRoleHeld(RoleManager.ROLE_SMS);
      boolean activityRoleHeld = activityRoleManager.isRoleHeld(RoleManager.ROLE_SMS);

      assertThat(activityRoleHeld).isEqualTo(applicationRoleHeld);
    }
  }

  @Test
  public void addRoleHolder_whenListened_notifysListeners() {
    UserHandle user = UserHandle.of(10);
    OnRoleHoldersChangedListener listener = mock(OnRoleHoldersChangedListener.class);
    roleManager.addOnRoleHoldersChangedListenerAsUser(directExecutor(), listener, user);

    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, "test.app", user);

    verify(listener).onRoleHoldersChanged(RoleManager.ROLE_SMS, user);
    assertThat(roleManager.getRoleHoldersAsUser(RoleManager.ROLE_SMS, user))
        .containsExactly("test.app");
  }

  @Test
  public void addRoleHolder_whenListenedOnAllUser_notifysListeners() {
    UserHandle user = UserHandle.of(10);
    OnRoleHoldersChangedListener listener = mock(OnRoleHoldersChangedListener.class);
    roleManager.addOnRoleHoldersChangedListenerAsUser(directExecutor(), listener, UserHandle.ALL);

    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, "test.app", user);

    verify(listener).onRoleHoldersChanged(RoleManager.ROLE_SMS, user);
  }

  @Test
  public void removeRoleHolder_whenListened_notifysListeners() {
    UserHandle user = UserHandle.of(10);
    OnRoleHoldersChangedListener listener = mock(OnRoleHoldersChangedListener.class);
    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, "test.app", user);
    roleManager.addOnRoleHoldersChangedListenerAsUser(directExecutor(), listener, user);

    ShadowRoleManager.removeRoleHolder(RoleManager.ROLE_SMS, "test.app", user);

    verify(listener).onRoleHoldersChanged(RoleManager.ROLE_SMS, user);
    assertThat(roleManager.getRoleHoldersAsUser(RoleManager.ROLE_SMS, user)).isEmpty();
  }

  @Test
  public void addRoleHolder_whenNoLongerListened_dontNotifysListeners() {
    UserHandle user = UserHandle.of(10);
    OnRoleHoldersChangedListener listener = mock(OnRoleHoldersChangedListener.class);
    roleManager.addOnRoleHoldersChangedListenerAsUser(directExecutor(), listener, user);
    roleManager.removeOnRoleHoldersChangedListenerAsUser(listener, user);

    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, "test.app", user);

    verify(listener, never()).onRoleHoldersChanged(any(), any());
  }

  @Test
  public void addRoleHolder_whenNoLongerListenedOnAllUser_dontNotifysListeners() {
    UserHandle user = UserHandle.of(10);
    OnRoleHoldersChangedListener listener = mock(OnRoleHoldersChangedListener.class);
    roleManager.addOnRoleHoldersChangedListenerAsUser(directExecutor(), listener, UserHandle.ALL);
    roleManager.removeOnRoleHoldersChangedListenerAsUser(listener, UserHandle.ALL);

    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, "test.app", user);

    verify(listener, never()).onRoleHoldersChanged(any(), any());
  }

  @Test
  public void getRoleHoldersAsUser_noHolders_returnEmpty() {
    UserHandle user = UserHandle.of(10);

    assertThat(roleManager.getRoleHoldersAsUser(RoleManager.ROLE_SMS, user)).isEmpty();
  }

  @Test
  public void getRoleHolders_noHolders_returnEmpty() {
    assertThat(roleManager.getRoleHolders(RoleManager.ROLE_SMS)).isEmpty();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R) // createContextAsUser is only available on R+
  public void getRoleHolders_holdersSet_returnRoleHolders() {
    UserHandle user = UserHandle.of(10);
    String roleHolder = "test.app";
    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, roleHolder, user);
    RoleManager userRoleManager =
        getApplication()
            .createContextAsUser(user, /* flags= */ 0)
            .getSystemService(RoleManager.class);
    assertThat(userRoleManager.getRoleHolders(RoleManager.ROLE_SMS)).containsExactly(roleHolder);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.R) // createContextAsUser is only available on R+
  public void getRoleHolders_holdersSetOnAnotherUser_returnEmpty() {
    UserHandle user = UserHandle.of(10);
    UserHandle anotherUser = UserHandle.of(11);
    String roleHolder = "test.app";
    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, roleHolder, anotherUser);
    RoleManager userRoleManager =
        getApplication()
            .createContextAsUser(user, /* flags= */ 0)
            .getSystemService(RoleManager.class);
    assertThat(userRoleManager.getRoleHolders(RoleManager.ROLE_SMS)).isEmpty();
  }

  @Test
  public void getRoleHoldersAsUser_holdersSet_returnRoleHolders() {
    UserHandle user = UserHandle.of(10);
    String roleHolder = "test.app";
    ShadowRoleManager.addRoleHolder(RoleManager.ROLE_SMS, roleHolder, user);
    assertThat(roleManager.getRoleHoldersAsUser(RoleManager.ROLE_SMS, user))
        .containsExactly(roleHolder);
  }
}
