package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.app.time.Capabilities;
import android.app.time.ExternalTimeSuggestion;
import android.app.time.TimeCapabilities;
import android.app.time.TimeCapabilitiesAndConfig;
import android.app.time.TimeConfiguration;
import android.app.time.TimeManager;
import android.app.time.TimeZoneCapabilitiesAndConfig;
import android.app.time.TimeZoneConfiguration;
import android.os.Build;
import android.os.UserHandle;
import androidx.test.core.app.ApplicationProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Tests for {@link ShadowTimeManager} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.S)
public final class ShadowTimeManagerTest {
  private static final int CAPABILITY_SUPPORTED = 4;

  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

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
    setSystemPropertyRule.set("robolectric.createActivityContexts", "true");

    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      TimeManager applicationTimeManager =
          ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
      Activity activity = controller.get();
      TimeManager activityTimeManager = activity.getSystemService(TimeManager.class);

      TimeZoneConfiguration timeZoneConfiguration = new TimeZoneConfiguration.Builder().build();
      applicationTimeManager.updateTimeZoneConfiguration(timeZoneConfiguration);

      assertThat(applicationTimeManager).isNotSameInstanceAs(activityTimeManager);

      TimeZoneCapabilitiesAndConfig applicationCapabilities =
          applicationTimeManager.getTimeZoneCapabilitiesAndConfig();
      TimeZoneCapabilitiesAndConfig activityCapabilities =
          activityTimeManager.getTimeZoneCapabilitiesAndConfig();

      assertThat(activityCapabilities).isEqualTo(applicationCapabilities);
    }
  }

  @Test
  public void setCapabilityState_updatesTimeZoneCapabilities() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    // updateTimeZoneConfiguration must be called so configuration is not null.
    TimeZoneConfiguration configuration = new TimeZoneConfiguration.Builder().build();
    timeManager.updateTimeZoneConfiguration(configuration);

    shadowTimeManager.setCapabilityState(
        ShadowTimeManager.CONFIGURE_GEO_DETECTION_CAPABILITY, CAPABILITY_SUPPORTED);

    assertThat(
            timeManager
                .getTimeZoneCapabilitiesAndConfig()
                .getCapabilities()
                .getConfigureGeoDetectionEnabledCapability())
        .isEqualTo(CAPABILITY_SUPPORTED);
  }

  @Test
  public void setTimeZoneCapabilityState_updatesConfigureNotificationsEnabledCapability() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    TimeZoneConfiguration configuration = new TimeZoneConfiguration.Builder().build();
    timeManager.updateTimeZoneConfiguration(configuration);

    shadowTimeManager.setTimeZoneCapabilityState(
        ShadowTimeManager.CONFIGURE_NOTIFICATIONS_ENABLED_CAPABILITY, CAPABILITY_SUPPORTED);

    Object capabilities = timeManager.getTimeZoneCapabilitiesAndConfig().getCapabilities();

    // Use reflection to call getters as these methods might not be available on all SDK levels
    // supported by this shadow. Check if the method exists before calling it to avoid
    // NoSuchMethodError on older SDKs.
    if (!ReflectionHelpers.hasMethod(
        capabilities.getClass(), "getConfigureNotificationsEnabledCapability")) {
      return; // Method not available on this SDK level, ignore.
    }
    int value =
        ReflectionHelpers.callInstanceMethod(
            capabilities, "getConfigureNotificationsEnabledCapability");
    assertThat(value).isEqualTo(CAPABILITY_SUPPORTED);
  }

  @Test
  public void
      setTimeZoneCapabilityState_updatesConfigureTimeZoneOffsetChangeNotificationsEnabledCapability() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    TimeZoneConfiguration configuration = new TimeZoneConfiguration.Builder().build();
    timeManager.updateTimeZoneConfiguration(configuration);

    shadowTimeManager.setTimeZoneCapabilityState(
        ShadowTimeManager.CONFIGURE_TIME_ZONE_OFFSET_CHANGE_NOTIFICATIONS_ENABLED_CAPABILITY,
        CAPABILITY_SUPPORTED);

    Object capabilities = timeManager.getTimeZoneCapabilitiesAndConfig().getCapabilities();

    // Use reflection to call getters as these methods might not be available on all SDK levels
    // supported by this shadow. Check if the method exists before calling it to avoid
    // NoSuchMethodError on older SDKs.
    if (!ReflectionHelpers.hasMethod(
        capabilities.getClass(),
        "getConfigureTimeZoneOffsetChangeNotificationsEnabledCapability")) {
      return; // Method not available on this SDK level, ignore.
    }
    int value =
        ReflectionHelpers.callInstanceMethod(
            capabilities, "getConfigureTimeZoneOffsetChangeNotificationsEnabledCapability");
    assertThat(value).isEqualTo(CAPABILITY_SUPPORTED);
  }

  @Test
  @Config(maxSdk = Build.VERSION_CODES.S_V2)
  public void classInitialization_onOlderSdk_doesNotCrash() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    assertThat(timeManager).isNotNull();

    // Trigger access to verify NoClassDefFoundError is mitigated or loaded correctly.
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);
    assertThat(shadowTimeManager).isNotNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setTimeCapabilityState_updatesTimeCapabilities() throws Exception {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    shadowTimeManager.setTimeCapabilityState(
        ShadowTimeManager.CONFIGURE_TIME_AUTO_DETECTION_ENABLED_CAPABILITY, CAPABILITY_SUPPORTED);

    assertThat(
            ((TimeCapabilitiesAndConfig) shadowTimeManager.getTimeCapabilitiesAndConfig())
                .getCapabilities()
                .getConfigureAutoDetectionEnabledCapability())
        .isEqualTo(CAPABILITY_SUPPORTED);
  }

  @Test
  public void setTimeZoneCapabilityState_invalidCapability_throwsException() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            shadowTimeManager.setTimeZoneCapabilityState(
                "invalid_capability", CAPABILITY_SUPPORTED));
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setTimeCapabilityState_invalidCapability_throwsException() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    assertThrows(
        IllegalArgumentException.class,
        () -> shadowTimeManager.setTimeCapabilityState("invalid_capability", CAPABILITY_SUPPORTED));
  }

  @Test
  public void updateTimeConfiguration_updatesState() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    TimeConfiguration configuration =
        new TimeConfiguration.Builder().setAutoDetectionEnabled(true).build();

    timeManager.updateTimeConfiguration(configuration);

    assertThat(shadowTimeManager.getTimeConfiguration()).isEqualTo(configuration);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getTimeCapabilitiesAndConfig_returnsDefaults() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);

    TimeCapabilitiesAndConfig config = timeManager.getTimeCapabilitiesAndConfig();
    assertThat(config).isNotNull();

    // Verify default TimeConfiguration (covers mutant at line 545)
    assertThat(config.getConfiguration().isAutoDetectionEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void updateTimeCapabilities_apiU_updatesStateWithDetectorStatus() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    shadowTimeManager.setTimeCapabilityState(
        ShadowTimeManager.CONFIGURE_TIME_AUTO_DETECTION_ENABLED_CAPABILITY, CAPABILITY_SUPPORTED);

    assertThat(
            timeManager
                .getTimeCapabilitiesAndConfig()
                .getCapabilities()
                .getConfigureAutoDetectionEnabledCapability())
        .isEqualTo(CAPABILITY_SUPPORTED);
  }

  @Test
  public void addRemoveTimeZoneDetectorListener_updatesListeners() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    AtomicBoolean listenerCalled = new AtomicBoolean(false);
    TimeManager.TimeZoneDetectorListener listener = () -> listenerCalled.set(true);

    shadowTimeManager.addTimeZoneDetectorListener(Runnable::run, listener);
    shadowTimeManager.triggerTimeZoneDetectorListeners();
    assertThat(listenerCalled.get()).isTrue();

    listenerCalled.set(false);
    shadowTimeManager.removeTimeZoneDetectorListener(listener);
    shadowTimeManager.triggerTimeZoneDetectorListeners();
    assertThat(listenerCalled.get()).isFalse();
  }

  @Test
  public void suggestExternalTime_cachesLastSuggestion() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    ExternalTimeSuggestion suggestion = new ExternalTimeSuggestion(1000L, 2000L);

    timeManager.suggestExternalTime(suggestion);

    assertThat(shadowTimeManager.getLastExternalTimeSuggestion()).isEqualTo(suggestion);
  }

  @Test
  public void confirmTime_cachesLastConfirmedTime() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    Object unixEpochTime = new Object();

    shadowTimeManager.confirmTime(unixEpochTime);

    assertThat(shadowTimeManager.getLastConfirmedTime()).isEqualTo(unixEpochTime);
  }

  @Test
  public void setManualTime_cachesLastManualTime() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    Object unixEpochTime = new Object();

    shadowTimeManager.setManualTime(unixEpochTime);

    assertThat(shadowTimeManager.getLastManualTime()).isEqualTo(unixEpochTime);
  }

  @Test
  public void confirmTimeZone_cachesLastConfirmedTimeZone() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    String timeZoneId = "America/New_York";

    shadowTimeManager.confirmTimeZone(timeZoneId);

    assertThat(shadowTimeManager.getLastConfirmedTimeZone()).isEqualTo(timeZoneId);
  }

  @Test
  public void setManualTimeZone_cachesLastManualTimeZone() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    String timeZoneId = "America/New_York";

    shadowTimeManager.setManualTimeZone(timeZoneId);

    assertThat(shadowTimeManager.getLastManualTimeZone()).isEqualTo(timeZoneId);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void reset_restoresDefaultState() throws Exception {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    // 1. Initialize configurations so read doesn't crash on start
    TimeZoneConfiguration geoConfig = new TimeZoneConfiguration.Builder().build();
    timeManager.updateTimeZoneConfiguration(geoConfig);

    TimeConfiguration timeConfig =
        new TimeConfiguration.Builder().setAutoDetectionEnabled(true).build();
    timeManager.updateTimeConfiguration(timeConfig);

    // 2. Capture default values
    int defaultAutoTimeCapability =
        ((TimeCapabilitiesAndConfig) shadowTimeManager.getTimeCapabilitiesAndConfig())
            .getCapabilities()
            .getConfigureAutoDetectionEnabledCapability();
    boolean defaultAutoTimeEnabled =
        shadowTimeManager.getTimeConfiguration().isAutoDetectionEnabled();

    // 3. Modify states to different values
    shadowTimeManager.setTimeZoneCapabilityState(
        ShadowTimeManager.CONFIGURE_GEO_DETECTION_CAPABILITY, CAPABILITY_SUPPORTED);
    shadowTimeManager.setTimeCapabilityState(
        ShadowTimeManager.CONFIGURE_TIME_AUTO_DETECTION_ENABLED_CAPABILITY, CAPABILITY_SUPPORTED);

    TimeConfiguration modifiedTimeConfiguration =
        new TimeConfiguration.Builder().setAutoDetectionEnabled(!defaultAutoTimeEnabled).build();
    timeManager.updateTimeConfiguration(modifiedTimeConfiguration);

    // 4. Call reset
    ShadowTimeManager.reset();

    // 5. Verify states are restored to captured defaults or nullified

    // timeZoneConfiguration is reset to null, throwing NullPointerException on access
    // Use anonymous class to avoid synthetic lambda methods capturing `TimeManager`
    // which triggers NoClassDefFoundError during host Class inspection.
    assertThrows(
        NullPointerException.class,
        new ThrowingRunnable() {
          @Override
          public void run() throws Throwable {
            timeManager.getTimeZoneCapabilitiesAndConfig();
          }
        });
    assertThat(
            ((TimeCapabilitiesAndConfig) shadowTimeManager.getTimeCapabilitiesAndConfig())
                .getCapabilities()
                .getConfigureAutoDetectionEnabledCapability())
        .isEqualTo(defaultAutoTimeCapability);
    assertThat(shadowTimeManager.getTimeConfiguration().isAutoDetectionEnabled())
        .isEqualTo(defaultAutoTimeEnabled);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void getTimeCapabilitiesAndConfig_defaultsToAutoDetectionEnabled() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);

    TimeCapabilitiesAndConfig config = timeManager.getTimeCapabilitiesAndConfig();

    assertThat(config.getConfiguration().isAutoDetectionEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void updateTimeConfiguration_updatesConfiguration() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    TimeConfiguration configuration =
        new TimeConfiguration.Builder().setAutoDetectionEnabled(false).build();

    boolean result = timeManager.updateTimeConfiguration(configuration);

    assertThat(result).isTrue();
    TimeCapabilitiesAndConfig config = timeManager.getTimeCapabilitiesAndConfig();
    assertThat(config.getConfiguration().isAutoDetectionEnabled()).isFalse();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void setTimeCapabilityState_updatesCapabilities() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);

    shadowTimeManager.setTimeCapabilityState(
        ShadowTimeManager.CONFIGURE_TIME_AUTO_DETECTION_ENABLED_CAPABILITY,
        Capabilities.CAPABILITY_NOT_SUPPORTED);
    shadowTimeManager.setTimeCapabilityState(
        ShadowTimeManager.SET_MANUAL_TIME_CAPABILITY, Capabilities.CAPABILITY_NOT_SUPPORTED);

    TimeCapabilitiesAndConfig config = timeManager.getTimeCapabilitiesAndConfig();
    assertThat(config.getCapabilities().getConfigureAutoDetectionEnabledCapability())
        .isEqualTo(Capabilities.CAPABILITY_NOT_SUPPORTED);
    assertThat(config.getCapabilities().getSetManualTimeCapability())
        .isEqualTo(Capabilities.CAPABILITY_NOT_SUPPORTED);
  }

  @Test
  public void triggerTimeZoneDetectorListeners_invokesListeners() {
    TimeManager timeManager =
        ApplicationProvider.getApplicationContext().getSystemService(TimeManager.class);
    ShadowTimeManager shadowTimeManager = Shadow.extract(timeManager);
    AtomicBoolean observed = new AtomicBoolean(false);
    TimeManager.TimeZoneDetectorListener listener = () -> observed.set(true);

    timeManager.addTimeZoneDetectorListener(Runnable::run, listener);
    shadowTimeManager.triggerTimeZoneDetectorListeners();

    assertThat(observed.get()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S, maxSdk = Build.VERSION_CODES.S_V2)
  public void timeCapabilitiesBuilder_onOlderSdk_worksFine() {
    TimeCapabilitiesBuilder builder =
        TimeCapabilitiesBuilder.newBuilder(
            ReflectionHelpers.getStaticField(UserHandle.class, "CURRENT"));
    builder.setConfigureAutoDetectionEnabledCapability(Capabilities.CAPABILITY_POSSESSED);
    builder.setSetManualTimeCapability(Capabilities.CAPABILITY_POSSESSED);

    TimeCapabilities capabilities = builder.build();
    assertThat(capabilities).isNotNull();
  }
}
