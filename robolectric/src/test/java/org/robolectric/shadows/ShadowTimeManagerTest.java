package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.time.TimeManager;
import android.app.time.TimeZoneCapabilitiesAndConfig;
import android.app.time.TimeZoneConfiguration;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowTimeManager} */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.S)
public final class ShadowTimeManagerTest {

  @Test
  public void registeredAsSystemService() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    assertThat(timeManager).isNotNull();
  }

  @Test
  public void updateConfigurationOverridesPreviousValues() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    Assume.assumeNotNull(timeManager);

    TimeZoneConfiguration configuration =
        new TimeZoneConfiguration.Builder()
            .setAutoDetectionEnabled(false)
            .setGeoDetectionEnabled(false)
            .build();

    timeManager.updateTimeZoneConfiguration(configuration);

    TimeZoneCapabilitiesAndConfig capabilitiesAndConfig =
        timeManager.getTimeZoneCapabilitiesAndConfig();

    assertThat(capabilitiesAndConfig.getConfiguration()).isEqualTo(configuration);

    TimeZoneConfiguration updatedConfiguration =
        new TimeZoneConfiguration.Builder()
            .setAutoDetectionEnabled(true)
            .setGeoDetectionEnabled(true)
            .build();

    timeManager.updateTimeZoneConfiguration(updatedConfiguration);

    capabilitiesAndConfig = timeManager.getTimeZoneCapabilitiesAndConfig();

    assertThat(capabilitiesAndConfig.getConfiguration()).isEqualTo(updatedConfiguration);
  }
}
