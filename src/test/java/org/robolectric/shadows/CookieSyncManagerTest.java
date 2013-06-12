package org.robolectric.shadows;

import android.app.Activity;
import android.webkit.CookieSyncManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class CookieSyncManagerTest {

  @Test
  public void testCreateInstance() {
    assertThat(CookieSyncManager.createInstance(new Activity())).isNotNull();
  }

  @Test
  public void testGetInstance() {
    CookieSyncManager.createInstance(new Activity());
    assertThat(CookieSyncManager.getInstance()).isNotNull();
  }

  @Test
  public void testSyncAndReset() {
    CookieSyncManager.createInstance(new Activity());
    CookieSyncManager mgr = CookieSyncManager.getInstance();

    ShadowCookieSyncManager shadowManager = Robolectric.shadowOf(mgr);
    assertThat(shadowManager.synced()).isFalse();
    mgr.sync();
    assertThat(shadowManager.synced()).isTrue();
    shadowManager.reset();
    assertThat(shadowManager.synced()).isFalse();
  }
}
