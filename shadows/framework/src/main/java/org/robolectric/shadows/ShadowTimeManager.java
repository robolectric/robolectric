package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.annotation.SystemApi;
import android.app.time.Capabilities;
import android.app.time.Capabilities.CapabilityState;
import android.app.time.ExternalTimeSuggestion;
import android.app.time.TimeCapabilities;
import android.app.time.TimeConfiguration;
import android.app.time.TimeManager;
import android.app.time.TimeState;
import android.app.time.TimeZoneCapabilities;
import android.app.time.TimeZoneCapabilitiesAndConfig;
import android.app.time.TimeZoneConfiguration;
import android.app.time.TimeZoneState;
import android.os.Build.VERSION_CODES;
import android.os.UserHandle;
import com.google.errorprone.annotations.InlineMe;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for internal Android {@code TimeManager} class introduced in S. */
@Implements(value = TimeManager.class, minSdk = VERSION_CODES.S, isInAndroidSdk = false)
// Static fields are strictly required to be non-final to maintain and reset shadow state setups.
@SuppressWarnings("NonFinalStaticField")
public class ShadowTimeManager {

  // TimeZone capability constants
  public static final String CONFIGURE_AUTO_DETECTION_ENABLED_CAPABILITY =
      "configure_auto_detection_enabled_capability";
  public static final String CONFIGURE_GEO_DETECTION_CAPABILITY =
      "configure_geo_detection_capability";

  public static final String SET_MANUAL_TIME_ZONE_CAPABILITY = "set_manual_time_zone_capability";
  public static final String SUGGEST_MANUAL_TIME_ZONE_CAPABILITY =
      "suggest_manual_time_zone_capability";

  public static final String CONFIGURE_NOTIFICATIONS_ENABLED_CAPABILITY =
      "configure_notifications_enabled_capability";
  public static final String CONFIGURE_TIME_ZONE_OFFSET_CHANGE_NOTIFICATIONS_ENABLED_CAPABILITY =
      "configure_time_zone_offset_change_notifications_enabled_capability";

  // Time capability constants
  public static final String CONFIGURE_TIME_AUTO_DETECTION_ENABLED_CAPABILITY =
      "configure_time_auto_detection_enabled_capability";
  public static final String SET_MANUAL_TIME_CAPABILITY = "set_manual_time_capability";
  private static TimeZoneCapabilities timeZoneCapabilities = createDefaultTimeZoneCapabilities();
  private static TimeZoneConfiguration timeZoneConfiguration;
  private static TimeCapabilities timeCapabilities = createDefaultTimeCapabilities();
  private static TimeConfiguration timeConfiguration;
  private static ExternalTimeSuggestion lastExternalTimeSuggestion;
  private static String lastConfirmedTimeZone;
  private static String lastManualTimeZone;

  private static final ConcurrentHashMap<TimeManager.TimeZoneDetectorListener, Executor> listeners =
      new ConcurrentHashMap<>();

  // Softened to Object to avoid NoClassDefFoundError on API 31/32 sandboxes
  // where these classes do not exist (added in API 34+).
  private static Object timeState;
  private static Object timeZoneState;
  private static Object lastConfirmedTime;
  private static Object lastManualTime;

  /** Returns the last {@link TimeConfiguration} set via {@link #updateTimeConfiguration}. */
  public TimeConfiguration getTimeConfiguration() {
    return timeConfiguration;
  }

  /** Sets the {@link TimeState} returned by {@link #getTimeState()}. */
  public void setTimeState(Object state) {
    timeState = state;
  }

  /** Sets the {@link TimeZoneState} returned by {@link #getTimeZoneState()}. */
  public void setTimeZoneState(Object state) {
    timeZoneState = state;
  }

  /**
   * Sets the state of a specific Time Zone capability.
   *
   * <p>Capabilities are predefined and not controlled by the user via TimeManager API, so this
   * method allows tests to simulate different capability states.
   */
  public void setTimeZoneCapabilityState(String capability, @CapabilityState int value) {
    TimeZoneCapabilitiesBuilder builder = TimeZoneCapabilitiesBuilder.from(timeZoneCapabilities);

    switch (capability) {
      case CONFIGURE_GEO_DETECTION_CAPABILITY:
        builder.setConfigureGeoDetectionEnabledCapability(value);
        break;
      case CONFIGURE_AUTO_DETECTION_ENABLED_CAPABILITY:
        builder.setConfigureAutoDetectionEnabledCapability(value);
        break;
      case SET_MANUAL_TIME_ZONE_CAPABILITY:
        builder.setSetManualTimeZoneCapability(value);
        break;
      case SUGGEST_MANUAL_TIME_ZONE_CAPABILITY:
        builder.setSuggestManualTimeZoneCapability(value);
        break;
      case CONFIGURE_NOTIFICATIONS_ENABLED_CAPABILITY:
        builder.setConfigureNotificationsEnabledCapability(value);
        break;
      case CONFIGURE_TIME_ZONE_OFFSET_CHANGE_NOTIFICATIONS_ENABLED_CAPABILITY:
        builder.setConfigureTimeZoneOffsetChangeNotificationsEnabledCapability(value);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized time zone capability=" + capability);
    }
    timeZoneCapabilities = builder.build();
  }

  /**
   * @deprecated Use {@link #setTimeZoneCapabilityState(String, int)}.
   */
  @Deprecated
  @InlineMe(replacement = "this.setTimeZoneCapabilityState(capability, value)")
  public final void setCapabilityState(String capability, @CapabilityState int value) {
    setTimeZoneCapabilityState(capability, value);
  }

  /**
   * Sets the state of a specific Time capability.
   *
   * <p>Capabilities are predefined and not controlled by the user via TimeManager API, so this
   * method allows tests to simulate different capability states.
   */
  public void setTimeCapabilityState(String capability, @CapabilityState int value) {
    TimeCapabilitiesBuilder builder = TimeCapabilitiesBuilder.from(timeCapabilities);

    switch (capability) {
      case CONFIGURE_TIME_AUTO_DETECTION_ENABLED_CAPABILITY:
        builder.setConfigureAutoDetectionEnabledCapability(value);
        break;
      case SET_MANUAL_TIME_CAPABILITY:
        builder.setSetManualTimeCapability(value);
        break;
      default:
        throw new IllegalArgumentException("Unrecognized time capability=" + capability);
    }
    timeCapabilities = builder.build();
  }

  @Implementation
  @SystemApi
  protected TimeZoneCapabilitiesAndConfig getTimeZoneCapabilitiesAndConfig()
      throws ClassNotFoundException {
    Objects.requireNonNull(
        timeZoneConfiguration, "timeZoneConfiguration must be set before calling this method");

    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      Object telephonyAlgoStatus =
          ReflectionHelpers.callConstructor(
              Class.forName("android.app.time.TelephonyTimeZoneAlgorithmStatus"),
              ClassParameter.from(int.class, 3));
      Object locationAlgoStatus =
          ReflectionHelpers.callConstructor(
              Class.forName("android.app.time.LocationTimeZoneAlgorithmStatus"),
              ClassParameter.from(int.class, 3),
              ClassParameter.from(int.class, 3),
              ClassParameter.from(
                  Class.forName("android.service.timezone.TimeZoneProviderStatus"), null),
              ClassParameter.from(int.class, 3),
              ClassParameter.from(
                  Class.forName("android.service.timezone.TimeZoneProviderStatus"), null));

      Object timeZoneDetectorStatus =
          ReflectionHelpers.callConstructor(
              Class.forName("android.app.time.TimeZoneDetectorStatus"),
              ClassParameter.from(int.class, 0),
              ClassParameter.from(
                  Class.forName("android.app.time.TelephonyTimeZoneAlgorithmStatus"),
                  telephonyAlgoStatus),
              ClassParameter.from(
                  Class.forName("android.app.time.LocationTimeZoneAlgorithmStatus"),
                  locationAlgoStatus));
      return ReflectionHelpers.callConstructor(
          TimeZoneCapabilitiesAndConfig.class,
          ClassParameter.from(
              Class.forName("android.app.time.TimeZoneDetectorStatus"), timeZoneDetectorStatus),
          ClassParameter.from(
              Class.forName("android.app.time.TimeZoneCapabilities"), timeZoneCapabilities),
          ClassParameter.from(
              Class.forName("android.app.time.TimeZoneConfiguration"), timeZoneConfiguration));
    } else {
      return ReflectionHelpers.callConstructor(
          TimeZoneCapabilitiesAndConfig.class,
          ClassParameter.from(
              Class.forName("android.app.time.TimeZoneCapabilities"), timeZoneCapabilities),
          ClassParameter.from(
              Class.forName("android.app.time.TimeZoneConfiguration"), timeZoneConfiguration));
    }
  }

  @Implementation
  @SystemApi
  protected @ClassName("android.app.time.TimeCapabilitiesAndConfig") Object
      getTimeCapabilitiesAndConfig() throws ClassNotFoundException {
    if (timeConfiguration == null) {
      timeConfiguration = new TimeConfiguration.Builder().setAutoDetectionEnabled(true).build();
    }

    return ReflectionHelpers.callConstructor(
        Class.forName("android.app.time.TimeCapabilitiesAndConfig"),
        ClassParameter.from(TimeCapabilities.class, timeCapabilities),
        ClassParameter.from(TimeConfiguration.class, timeConfiguration));
  }

  @Implementation
  protected boolean updateTimeConfiguration(TimeConfiguration configuration) {
    Objects.requireNonNull(configuration);
    timeConfiguration = configuration;
    // Real service would return false if update failed, but in shadow we'll assume success.
    return true;
  }

  @Implementation
  @SystemApi
  protected boolean updateTimeZoneConfiguration(TimeZoneConfiguration configuration) {
    Objects.requireNonNull(configuration);
    timeZoneConfiguration = configuration;
    // Real service would return false if update failed, but in shadow we'll assume success.
    return true;
  }

  @Implementation
  protected void addTimeZoneDetectorListener(
      Executor executor, TimeManager.TimeZoneDetectorListener listener) {
    Objects.requireNonNull(executor);
    Objects.requireNonNull(listener);
    listeners.put(listener, executor);
  }

  @Implementation
  protected void removeTimeZoneDetectorListener(TimeManager.TimeZoneDetectorListener listener) {
    Objects.requireNonNull(listener);
    listeners.remove(listener);
  }

  @Implementation
  protected void suggestExternalTime(ExternalTimeSuggestion timeSuggestion) {
    Objects.requireNonNull(timeSuggestion);
    lastExternalTimeSuggestion = timeSuggestion;
  }

  @Implementation
  protected @ClassName("android.app.time.TimeState") Object getTimeState() {
    return timeState;
  }

  @Implementation
  protected boolean confirmTime(@ClassName("android.app.time.UnixEpochTime") Object unixEpochTime) {
    Objects.requireNonNull(unixEpochTime);
    lastConfirmedTime = unixEpochTime;
    return true;
  }

  @Implementation
  protected boolean setManualTime(
      @ClassName("android.app.time.UnixEpochTime") Object unixEpochTime) {
    Objects.requireNonNull(unixEpochTime);
    lastManualTime = unixEpochTime;
    return true;
  }

  @Implementation
  protected @ClassName("android.app.time.TimeZoneState") Object getTimeZoneState() {
    return timeZoneState;
  }

  @Implementation
  protected boolean confirmTimeZone(String timeZoneId) {
    Objects.requireNonNull(timeZoneId);
    lastConfirmedTimeZone = timeZoneId;
    return true;
  }

  @Implementation
  protected boolean setManualTimeZone(String timeZoneId) {
    Objects.requireNonNull(timeZoneId);
    lastManualTimeZone = timeZoneId;
    return true;
  }

  /** Returns the last time suggestion passed to {@link #suggestExternalTime}. */
  public ExternalTimeSuggestion getLastExternalTimeSuggestion() {
    return lastExternalTimeSuggestion;
  }

  /** Returns the last time passed to {@link #confirmTime}. */
  public Object getLastConfirmedTime() {
    return lastConfirmedTime;
  }

  /** Returns the last time passed to {@link #setManualTime}. */
  public Object getLastManualTime() {
    return lastManualTime;
  }

  /** Returns the last time zone ID passed to {@link #confirmTimeZone}. */
  public String getLastConfirmedTimeZone() {
    return lastConfirmedTimeZone;
  }

  /** Returns the last time zone ID passed to {@link #setManualTimeZone}. */
  public String getLastManualTimeZone() {
    return lastManualTimeZone;
  }

  /** Simulates triggering all registered {@link TimeManager.TimeZoneDetectorListener}s. */
  public void triggerTimeZoneDetectorListeners() {
    listeners.forEach(
        (listener, executor) ->
            executor.execute(
                () -> {
                  try {
                    Method[] methods =
                        TimeManager.TimeZoneDetectorListener.class.getDeclaredMethods();
                    Method target = null;
                    for (Method m : methods) {
                      if (Modifier.isAbstract(m.getModifiers())) {
                        target = m;
                        break;
                      }
                    }
                    if (target != null) {
                      target.invoke(listener);
                    } else {
                      throw new RuntimeException(
                          "No abstract method found in TimeZoneDetectorListener");
                    }
                  } catch (Exception e) {
                    throw new RuntimeException("Failed to trigger TimeZoneDetectorListener", e);
                  }
                }));
  }

  private static TimeZoneCapabilities createDefaultTimeZoneCapabilities() {
    return TimeZoneCapabilitiesBuilder.newBuilder(UserHandle.CURRENT)
        .setConfigureAutoDetectionEnabledCapability(Capabilities.CAPABILITY_POSSESSED)
        .setConfigureGeoDetectionEnabledCapability(Capabilities.CAPABILITY_POSSESSED)
        .setConfigureNotificationsEnabledCapability(Capabilities.CAPABILITY_POSSESSED)
        .setConfigureTimeZoneOffsetChangeNotificationsEnabledCapability(
            Capabilities.CAPABILITY_POSSESSED)
        .setUseLocationEnabled(true)
        .setSetManualTimeZoneCapability(Capabilities.CAPABILITY_POSSESSED)
        .build();
  }

  private static TimeCapabilities createDefaultTimeCapabilities() {
    return TimeCapabilitiesBuilder.newBuilder(UserHandle.CURRENT)
        .setConfigureAutoDetectionEnabledCapability(Capabilities.CAPABILITY_POSSESSED)
        .setSetManualTimeCapability(Capabilities.CAPABILITY_POSSESSED)
        .build();
  }

  @Resetter
  public static void reset() {
    timeZoneCapabilities = createDefaultTimeZoneCapabilities();
    timeZoneConfiguration = null;
    timeCapabilities = createDefaultTimeCapabilities();
    timeConfiguration = null;
    timeState = null;
    timeZoneState = null;
    lastExternalTimeSuggestion = null;
    listeners.clear();
    lastConfirmedTime = null;
    lastManualTime = null;
    lastConfirmedTimeZone = null;
    lastManualTimeZone = null;
  }
}
