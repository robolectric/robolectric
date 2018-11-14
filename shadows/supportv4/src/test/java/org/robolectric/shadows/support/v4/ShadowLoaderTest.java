package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;

import android.support.v4.content.Loader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.TestRunnerWithManifest;

/**
 * Tests for support loaders.
 */
@RunWith(TestRunnerWithManifest.class)
public class ShadowLoaderTest {
  private Loader<String> loader;
  private boolean onForceLoadCalled;

  @Before
  public void create() {
    loader = new Loader<String>(RuntimeEnvironment.application) {
      @Override
      protected void onForceLoad() {
        onForceLoadCalled = true;
      }
    };
    onForceLoadCalled = false;
  }

  @Test
  public void shouldCallOnForceLoad() {
    loader.forceLoad();
    assertThat(onForceLoadCalled).isTrue();
  }
}
