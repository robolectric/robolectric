package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.app.Activity;
import android.webkit.CookieSyncManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

@RunWith(TestRunners.MultiApiSelfTest.class)
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
