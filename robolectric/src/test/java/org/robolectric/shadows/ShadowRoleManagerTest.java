package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertThrows;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.os.Build;
import androidx.test.core.content.pm.PackageInfoBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestActivity;

/** Unit tests for {@link org.robolectric.shadows.ShadowRoleManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public final class ShadowRoleManagerTest {
  private RoleManager roleManager;

  @Before
  public void setUp() {
    roleManager = (RoleManager) getApplication().getSystemService(Context.ROLE_SERVICE);
  }

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
            RoleManager.ROLE_SMS,
            "test.app",
            0,
            directExecutor(),
            result -> resultHolder.set(result));
    assertThat(roleManager.getDefaultApplication(RoleManager.ROLE_SMS)).isEqualTo("test.app");
    assertThat(resultHolder.get()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setDefaultApplication_checksAppInstalled() {
    AtomicBoolean resultHolder = new AtomicBoolean(true);
    shadowOf(roleManager)
        .setDefaultApplication(
            RoleManager.ROLE_SMS,
            "test.app",
            0,
            directExecutor(),
            result -> resultHolder.set(result));
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
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      RoleManager applicationRoleManager = roleManager;

      activity = Robolectric.setupActivity(TestActivity.class);
      RoleManager activityRoleManager =
          (RoleManager) activity.getSystemService(Context.ROLE_SERVICE);

      assertThat(applicationRoleManager).isNotSameInstanceAs(activityRoleManager);

      boolean applicationRoleHeld = applicationRoleManager.isRoleHeld(RoleManager.ROLE_SMS);
      boolean activityRoleHeld = activityRoleManager.isRoleHeld(RoleManager.ROLE_SMS);

      assertThat(activityRoleHeld).isEqualTo(applicationRoleHeld);
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
