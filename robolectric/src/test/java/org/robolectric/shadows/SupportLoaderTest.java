package org.robolectric.shadows;

import android.support.v4.content.Loader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for support loaders.
 */
@RunWith(TestRunners.WithDefaults.class)
public class SupportLoaderTest {

  private Loader<String> loader;

  private boolean onForceLoadCalled;

  @Before
  public void create() {
    loader = new Loader<String>(Robolectric.application) {
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
