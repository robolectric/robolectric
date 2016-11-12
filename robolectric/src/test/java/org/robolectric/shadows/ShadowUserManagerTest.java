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

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowUserManagerTest {

  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldGetUserProfiles() {
    UserManager userManager = new UserManager(null, null);
    List<UserHandle> userProfiles = userManager.getUserProfiles();
    assertThat(userProfiles).isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR2)
  public void shouldGetApplicationRestrictions() {
    UserManager userManager = new UserManager(null, null);
    Bundle userProfiles = userManager.getApplicationRestrictions("somepackage");
    // Should not NPE
  }

}
