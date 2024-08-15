package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.time.TimeManager;
import android.app.time.TimeZoneCapabilitiesAndConfig;
import android.app.time.TimeZoneConfiguration;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowTimeManager} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.S)
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

  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  public void timeManager_activityContextEnabled_differentInstancesRetrieveTimeZoneCapabilities() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    Activity activity = null;
    try {
      TimeManager applicationTimeManager =
          ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
      activity = Robolectric.setupActivity(Activity.class);
      TimeManager activityTimeManager = activity.getSystemService(TimeManager.class);

      TimeZoneConfiguration timeZoneConfiguration = new TimeZoneConfiguration.Builder().build();
      applicationTimeManager.updateTimeZoneConfiguration(timeZoneConfiguration);

      assertThat(applicationTimeManager).isNotSameInstanceAs(activityTimeManager);

      TimeZoneCapabilitiesAndConfig applicationCapabilities =
          applicationTimeManager.getTimeZoneCapabilitiesAndConfig();
      TimeZoneCapabilitiesAndConfig activityCapabilities =
          activityTimeManager.getTimeZoneCapabilitiesAndConfig();

      assertThat(activityCapabilities).isEqualTo(applicationCapabilities);
    } finally {
      if (activity != null) {
        activity.finish();
      }
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
