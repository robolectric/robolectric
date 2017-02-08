package org.robolectric.shadows;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.util.List;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

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

}
