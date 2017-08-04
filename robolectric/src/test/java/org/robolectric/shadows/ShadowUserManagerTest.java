package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowUserManagerTest {

  private UserManager userManager;

  @Before
  public void setUp() {
    userManager = (UserManager) RuntimeEnvironment.application.getSystemService(Context.USER_SERVICE);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldGetUserProfiles() {
    List<UserHandle> userProfiles = userManager.getUserProfiles();
    assertThat(userProfiles).isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void shouldGetApplicationRestrictions() {
    Bundle userProfiles = userManager.getApplicationRestrictions("somepackage");
    // Should not NPE
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

    shadowOf(userManager).setUserRestriction(Process.myUserHandle(), UserManager.ENSURE_VERIFY_APPS, true);

    assertThat(userManager.hasUserRestriction(UserManager.ENSURE_VERIFY_APPS)).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void getUserRestrictions() {
    assertThat(userManager.getUserRestrictions().size()).isEqualTo(0);

    shadowOf(userManager).setUserRestriction(Process.myUserHandle(), UserManager.ENSURE_VERIFY_APPS, true);

    assertThat(userManager.getUserRestrictions().size()).isEqualTo(1);
    assertThat(userManager.getUserRestrictions().getBoolean(UserManager.ENSURE_VERIFY_APPS)).isTrue();

    shadowOf(userManager).setUserRestriction(newUserHandle(10), UserManager.DISALLOW_CAMERA, true);

    assertThat(userManager.hasUserRestriction(UserManager.DISALLOW_CAMERA)).isFalse();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void isManagedProfile() {
    assertThat(userManager.isManagedProfile()).isFalse();
    shadowOf(userManager).setManagedProfile(true);
    assertThat(userManager.isManagedProfile()).isTrue();
  }

  // Create user handle from parcel since UserHandle.of() was only added in later APIs.
  private static UserHandle newUserHandle(int uid) {
    Parcel userParcel = Parcel.obtain();
    userParcel.writeInt(uid);
    userParcel.setDataPosition(0);
    return new UserHandle(userParcel);
  }
}
