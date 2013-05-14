package org.robolectric.shadows;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class) @Config(manifest = Config.NONE)
public class NonAppLibraryTest {
  @Test public void shouldStillCreateAnApplication() throws Exception {
    assertThat(Robolectric.application).isExactlyInstanceOf(Application.class);
  }

  @Test public void applicationShouldHaveSomeReasonableConfig() throws Exception {
    assertThat(Robolectric.application.getPackageName()).isEqualTo("some.package.name");
  }
}
