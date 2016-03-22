package org.robolectric.shadows;

import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowUserManagerTest {

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.LOLLIPOP,
      Build.VERSION_CODES.LOLLIPOP_MR1,
      Build.VERSION_CODES.M})
  public void shouldGetUserProfiles() {
    UserManager userManager = new UserManager(null, null);
    List<UserHandle> userProfiles = userManager.getUserProfiles();
    assertThat(userProfiles).isNotNull();
  }

  @Test
  @Config(sdk = {
      Build.VERSION_CODES.JELLY_BEAN_MR2,
      Build.VERSION_CODES.KITKAT,
      Build.VERSION_CODES.LOLLIPOP,
      Build.VERSION_CODES.LOLLIPOP_MR1,
      Build.VERSION_CODES.M})
  public void shouldGetApplicationRestrictions() {
    UserManager userManager = new UserManager(null, null);
    Bundle userProfiles = userManager.getApplicationRestrictions("somepackage");
    // Should not NPE
  }

}
