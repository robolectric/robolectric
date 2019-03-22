package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.config.ConfigurationRegistry;

/**
 * Unit tests for classes annotated with @LooperMode.
 */
@RunWith(AndroidJUnit4.class)
@LooperMode(Mode.PAUSED)
public class LooperModeConfigurerClassTest {

  @Test
  public void defaultsToClass() {
    assertThat(ConfigurationRegistry.get(LooperMode.Mode.class)).isSameAs(Mode.PAUSED);
  }

  @Test
  @LooperMode(Mode.LEGACY)
  public void overriddenAtMethod() {
    assertThat(ConfigurationRegistry.get(LooperMode.Mode.class)).isSameAs(Mode.LEGACY);
  }

  @Test
  @LooperMode(Mode.LEGACY)
  public void shouldUseLegacyShadows() {
    assertThat(ConfigurationRegistry.get(LooperMode.Mode.class)).isSameAs(Mode.LEGACY);

    // TODO: uncomment when ShadowRealisticLooper is introduced
    // ShadowBaseLooper looper = Shadow.extract(Looper.getMainLooper());
    // assertThat(looper).isInstanceOf(ShadowRealisticLooper.class);
  }

  @Test
  @LooperMode(Mode.PAUSED)
  public void shouldUseRealisticShadows() {
    assertThat(ConfigurationRegistry.get(LooperMode.Mode.class)).isSameAs(Mode.PAUSED);

    // TODO: uncomment when ShadowRealisticLooper is introduced
    // ShadowBaseLooper looper = Shadow.extract(Looper.getMainLooper());
    // assertThat(looper).isInstanceOf(ShadowRealisticLooper.class);
  }

}
