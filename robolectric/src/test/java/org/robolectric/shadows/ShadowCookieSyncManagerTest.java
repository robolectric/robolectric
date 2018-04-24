package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.webkit.CookieSyncManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
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
