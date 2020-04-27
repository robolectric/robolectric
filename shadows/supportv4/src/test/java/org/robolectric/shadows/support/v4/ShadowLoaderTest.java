package org.robolectric.shadows.support.v4;

import static com.google.common.truth.Truth.assertThat;

import androidx.loader.content.Loader;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

/** Tests for support loaders. */
@RunWith(AndroidJUnit4.class)
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
