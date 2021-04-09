package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.RuntimeEnvironment.getApplication;
import static org.robolectric.Shadows.shadowOf;

import android.app.role.RoleManager;
import android.content.Context;
import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link org.robolectric.shadows.ShadowRoleManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.Q)
public final class ShadowRoleManagerTest {
  private RoleManager roleManager;

  @Before
  public void setUp() {
    roleManager = (RoleManager) getApplication().getSystemService(Context.ROLE_SERVICE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void isRoleHeld_shouldThrowWithNullArgument() {
    shadowOf(roleManager).isRoleHeld(null);
  }

  @Test()
  public void addHeldRole_isPresentInIsRoleHeld() {
    shadowOf(roleManager).addHeldRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleHeld(RoleManager.ROLE_SMS)).isTrue();
  }

  @Test()
  public void removeHeldRole_notPresentInIsRoleHeld() {
    shadowOf(roleManager).addHeldRole(RoleManager.ROLE_SMS);
    shadowOf(roleManager).removeHeldRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleHeld(RoleManager.ROLE_SMS)).isFalse();
  }

  @Test()
  public void isRoleHeld_noValueByDefault() {
    assertThat(roleManager.isRoleHeld(RoleManager.ROLE_SMS)).isFalse();
  }

  @Test
  public void isRoleAvailable_shouldThrowWithNullArgument() {
    assertThrows(IllegalArgumentException.class, () -> shadowOf(roleManager).isRoleAvailable(null));
  }

  @Test()
  public void addAvailableRole_isPresentInIsRoleAvailable() {
    shadowOf(roleManager).addAvailableRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleAvailable(RoleManager.ROLE_SMS)).isTrue();
  }

  @Test()
  public void addAvailableRole_shouldThrowWithEmptyArgument() {
    assertThrows(IllegalArgumentException.class, () -> shadowOf(roleManager).addAvailableRole(""));
  }

  @Test()
  public void removeAvailableRole_notPresentInIsRoleAvailable() {
    shadowOf(roleManager).addAvailableRole(RoleManager.ROLE_SMS);
    shadowOf(roleManager).removeAvailableRole(RoleManager.ROLE_SMS);
    assertThat(roleManager.isRoleAvailable(RoleManager.ROLE_SMS)).isFalse();
  }

  @Test()
  public void isRoleAvailable_noValueByDefault() {
    assertThat(roleManager.isRoleAvailable(RoleManager.ROLE_SMS)).isFalse();
  }
}
