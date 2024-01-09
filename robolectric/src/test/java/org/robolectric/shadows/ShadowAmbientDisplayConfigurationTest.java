package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Tests for {@link ShadowAmbientDisplayConfiguration}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = VERSION_CODES.R)
public final class ShadowAmbientDisplayConfigurationTest {

  private Object instanceAmbientDisplayConfiguration;

  @Before
  public void setUp() throws Exception {
    instanceAmbientDisplayConfiguration =
        ReflectionHelpers.callConstructor(
            Class.forName("android.hardware.display.AmbientDisplayConfiguration"),
            ClassParameter.from(Context.class, RuntimeEnvironment.getApplication()));
  }

  @Test
  public void ambientDisplayComponent_shouldReturnNullByDefault() throws Exception {
    String component =
        ReflectionHelpers.callInstanceMethod(
            instanceAmbientDisplayConfiguration, "ambientDisplayComponent");
    assertThat(component).isNull();
  }

  @Test
  public void ambientDisplayComponent_whenValidDozeComponentIsSet_shouldReturnDozeComponent()
      throws Exception {
    ShadowAmbientDisplayConfiguration.setDozeComponent(
        "com.google.android.aod/.TestDozeAlwaysOnDisplay");

    String component =
        ReflectionHelpers.callInstanceMethod(
            instanceAmbientDisplayConfiguration, "ambientDisplayComponent");
    assertThat(component).isEqualTo("com.google.android.aod/.TestDozeAlwaysOnDisplay");
  }

  @Test
  public void ambientDisplayAvailable_whenValidDozeComponentIsSet_shouldReturnTrue()
      throws Exception {
    ShadowAmbientDisplayConfiguration.setDozeComponent(
        "com.google.android.aod/.TestDozeAlwaysOnDisplay");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "ambientDisplayAvailable"))
        .isTrue();
  }

  @Test
  public void ambientDisplayAvailable_whenInvalidDozeComponentIsSet_shouldReturnFalse()
      throws Exception {
    ShadowAmbientDisplayConfiguration.setDozeComponent("");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "ambientDisplayAvailable"))
        .isFalse();
  }

  @Test
  public void
      alwaysOnDisplayAvailable_whenOverrideDozeAlwaysOnDisplayAvailableStateToTrue_shouldReturnTrue()
          throws Exception {
    ShadowAmbientDisplayConfiguration.setDozeAlwaysOnDisplayAvailable(
        /* dozeAlwaysOnDisplayAvailable= */ true);

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnDisplayAvailable"))
        .isTrue();
  }

  @Test
  public void
      alwaysOnDisplayAvailable_whenOverrideDozeAlwaysOnDisplayAvailableStateToFalse_shouldReturnFalse()
          throws Exception {
    ShadowAmbientDisplayConfiguration.setDozeAlwaysOnDisplayAvailable(
        /* dozeAlwaysOnDisplayAvailable= */ false);

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnDisplayAvailable"))
        .isFalse();
  }

  @Test
  public void
      alwaysOnDisplayDebuggingEnabled_whenBothDebuggableAndAodSystemPropertyAreSet_shouldReturnTrue()
          throws Exception {
    ShadowSystemProperties.override("ro.debuggable", "1");
    ShadowSystemProperties.override("debug.doze.aod", "true");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnDisplayDebuggingEnabled"))
        .isTrue();
  }

  @Test
  public void
      alwaysOnDisplayDebuggingEnabled_whenOnlyDebuggableSystemPropertyIsSet_shouldReturnFalse()
          throws Exception {
    ShadowSystemProperties.override("ro.debuggable", "1");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnDisplayDebuggingEnabled"))
        .isFalse();
  }

  @Test
  public void alwaysOnDisplayDebuggingEnabled_whenOnlyAodSystemPropertyIsSet_shouldReturnFalse()
      throws Exception {
    ShadowSystemProperties.override("debug.doze.aod", "true");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnDisplayDebuggingEnabled"))
        .isFalse();
  }

  @Test
  public void
      alwaysOnAvailable_whenValidSystemPropertiesAndValidDozeComponentAreSet_shouldReturnTrue()
          throws Exception {
    ShadowSystemProperties.override("ro.debuggable", "1");
    ShadowSystemProperties.override("debug.doze.aod", "true");

    ShadowAmbientDisplayConfiguration.setDozeAlwaysOnDisplayAvailable(
        /* dozeAlwaysOnDisplayAvailable= */ false);
    ShadowAmbientDisplayConfiguration.setDozeComponent(
        "com.google.android.aod/.TestDozeAlwaysOnDisplay");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnAvailable"))
        .isTrue();
  }

  @Test
  public void
      alwaysOnAvailable_whenOverrideDozeAlwaysOnDisplayAvailableStateAndValidDozeComponentAreSet_shouldReturnTrue()
          throws Exception {
    ShadowSystemProperties.override("ro.debuggable", "0");
    ShadowSystemProperties.override("debug.doze.aod", "false");

    ShadowAmbientDisplayConfiguration.setDozeAlwaysOnDisplayAvailable(
        /* dozeAlwaysOnDisplayAvailable= */ true);
    ShadowAmbientDisplayConfiguration.setDozeComponent(
        "com.google.android.aod/.TestDozeAlwaysOnDisplay");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnAvailable"))
        .isTrue();
  }

  @Test
  public void alwaysOnAvailable_whenInvalidDozeComponentIsSet_shouldReturnFalse() throws Exception {
    ShadowSystemProperties.override("ro.debuggable", "1");
    ShadowSystemProperties.override("debug.doze.aod", "true");

    ShadowAmbientDisplayConfiguration.setDozeAlwaysOnDisplayAvailable(
        /* dozeAlwaysOnDisplayAvailable= */ true);
    ShadowAmbientDisplayConfiguration.setDozeComponent("");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnAvailable"))
        .isFalse();
  }

  @Test
  public void
      alwaysOnAvailable_whenInvalidSystemPropertiesAreSetAndOverrideDozeAlwaysOnDisplayAvailableStateToFalse_shouldReturnFalse()
          throws Exception {
    ShadowSystemProperties.override("ro.debuggable", "0");
    ShadowSystemProperties.override("debug.doze.aod", "false");

    ShadowAmbientDisplayConfiguration.setDozeAlwaysOnDisplayAvailable(
        /* dozeAlwaysOnDisplayAvailable= */ false);
    ShadowAmbientDisplayConfiguration.setDozeComponent(
        "com.google.android.aod/.TestDozeAlwaysOnDisplay");

    assertThat(
            (Boolean)
                ReflectionHelpers.callInstanceMethod(
                    instanceAmbientDisplayConfiguration, "alwaysOnAvailable"))
        .isFalse();
  }
}
