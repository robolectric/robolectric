package org.robolectric.shadows;

import android.app.Activity;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowCookieSyncManagerTest {

  @Test
  public void testCreateInstance() {
    assertThat(CookieSyncManager.createInstance(new Activity())).isNotNull();
  }

  @Test
  public void testGetInstance() {
    CookieSyncManager.createInstance(new Activity());
    assertThat(CookieSyncManager.getInstance()).isNotNull();
  }
}
