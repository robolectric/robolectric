package org.robolectric.shadows;

import android.app.time.Capabilities.CapabilityState;
import android.app.time.TimeZoneCapabilities;
import android.os.Build;
import android.os.UserHandle;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link TimeZoneCapabilities} to support version-dependent methods. */
public class TimeZoneCapabilitiesBuilder {

  private final TimeZoneCapabilities.Builder builder;

  private TimeZoneCapabilitiesBuilder(UserHandle userHandle) {
    this.builder = new TimeZoneCapabilities.Builder(userHandle);
  }

  private TimeZoneCapabilitiesBuilder(TimeZoneCapabilities capabilities) {
    this.builder = new TimeZoneCapabilities.Builder(capabilities);
  }

  public static TimeZoneCapabilitiesBuilder newBuilder(UserHandle userHandle) {
    return new TimeZoneCapabilitiesBuilder(userHandle);
  }

  public static TimeZoneCapabilitiesBuilder from(TimeZoneCapabilities capabilities) {
    return new TimeZoneCapabilitiesBuilder(capabilities);
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setConfigureAutoDetectionEnabledCapability(
      @CapabilityState int value) {
    TimeZoneCapabilities.Builder unused = builder.setConfigureAutoDetectionEnabledCapability(value);
    return this;
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setConfigureGeoDetectionEnabledCapability(
      @CapabilityState int value) {
    TimeZoneCapabilities.Builder unused = builder.setConfigureGeoDetectionEnabledCapability(value);
    return this;
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setUseLocationEnabled(boolean value) {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      TimeZoneCapabilities.Builder unused = builder.setUseLocationEnabled(value);
    }
    return this;
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setSetManualTimeZoneCapability(@CapabilityState int value) {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      TimeZoneCapabilities.Builder unused = builder.setSetManualTimeZoneCapability(value);
    } else if (hasMethod("setSuggestManualTimeZoneCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder, "setSuggestManualTimeZoneCapability", ClassParameter.from(int.class, value));
    }
    return this;
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setSuggestManualTimeZoneCapability(
      @CapabilityState int value) {
    // Both mapped to same candidate iteration generally
    return setSetManualTimeZoneCapability(value);
  }

  public TimeZoneCapabilities build() {
    return builder.build();
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setConfigureNotificationsEnabledCapability(
      @CapabilityState int value) {
    if (hasMethod("setConfigureNotificationsEnabledCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder,
          "setConfigureNotificationsEnabledCapability",
          ClassParameter.from(int.class, value));
    }
    return this;
  }

  @CanIgnoreReturnValue
  public TimeZoneCapabilitiesBuilder setConfigureTimeZoneOffsetChangeNotificationsEnabledCapability(
      @CapabilityState int value) {
    if (hasMethod("setConfigureTimeZoneOffsetChangeNotificationsEnabledCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder,
          "setConfigureTimeZoneOffsetChangeNotificationsEnabledCapability",
          ClassParameter.from(int.class, value));
    }
    return this;
  }

  public static boolean hasMethod(String methodName, Class<?>... parameterTypes) {
    try {
      TimeZoneCapabilities.Builder.class.getMethod(methodName, parameterTypes);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
