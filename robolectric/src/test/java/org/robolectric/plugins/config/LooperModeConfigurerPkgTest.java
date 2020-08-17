package org.robolectric.plugins.config;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.config.ConfigurationRegistry;

/**
 * Unit tests for packages annotated with @LooperMode.
 */
@RunWith(AndroidJUnit4.class)
public class LooperModeConfigurerPkgTest {

  @Test
  public void fromPkg() {
    assertThat(ConfigurationRegistry.get(LooperMode.Mode.class)).isSameInstanceAs(Mode.PAUSED);
  }

  @Test
  @LooperMode(Mode.LEGACY)
  public void overriddenAtMethod() {
    assertThat(ConfigurationRegistry.get(LooperMode.Mode.class)).isSameInstanceAs(Mode.LEGACY);
  }
}
