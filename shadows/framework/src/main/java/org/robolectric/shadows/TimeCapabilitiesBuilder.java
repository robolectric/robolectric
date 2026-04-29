package org.robolectric.shadows;

import android.app.time.Capabilities.CapabilityState;
import android.app.time.TimeCapabilities;
import android.os.Build;
import android.os.UserHandle;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link TimeCapabilities} to support version-dependent methods. */
public class TimeCapabilitiesBuilder {

  private final TimeCapabilities.Builder builder;

  private TimeCapabilitiesBuilder(UserHandle userHandle) {
    this.builder = new TimeCapabilities.Builder(userHandle);
  }

  private TimeCapabilitiesBuilder(TimeCapabilities capabilities) {
    this.builder = new TimeCapabilities.Builder(capabilities);
  }

  public static TimeCapabilitiesBuilder newBuilder(UserHandle userHandle) {
    return new TimeCapabilitiesBuilder(userHandle);
  }

  public static TimeCapabilitiesBuilder from(TimeCapabilities capabilities) {
    return new TimeCapabilitiesBuilder(capabilities);
  }

  @CanIgnoreReturnValue
  public TimeCapabilitiesBuilder setConfigureAutoDetectionEnabledCapability(
      @CapabilityState int value) {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      TimeCapabilities.Builder unused = builder.setConfigureAutoDetectionEnabledCapability(value);
    } else if (hasMethod("setConfigureAutoDetectionEnabledCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder,
          "setConfigureAutoDetectionEnabledCapability",
          ClassParameter.from(int.class, value));
    } else if (hasMethod("setConfigureAutoTimeDetectionEnabledCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder,
          "setConfigureAutoTimeDetectionEnabledCapability",
          ClassParameter.from(int.class, value));
    }
    return this;
  }

  @CanIgnoreReturnValue
  public TimeCapabilitiesBuilder setSetManualTimeCapability(@CapabilityState int value) {
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      TimeCapabilities.Builder unused = builder.setSetManualTimeCapability(value);
    } else if (hasMethod("setSuggestManualTimeCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder, "setSuggestManualTimeCapability", ClassParameter.from(int.class, value));
    } else if (hasMethod("setSuggestTimeManuallyCapability", int.class)) {
      ReflectionHelpers.callInstanceMethod(
          builder, "setSuggestTimeManuallyCapability", ClassParameter.from(int.class, value));
    }

    return this;
  }

  public TimeCapabilities build() {
    return builder.build();
  }

  public static boolean hasMethod(String methodName, Class<?>... parameterTypes) {
    try {
      TimeCapabilities.Builder.class.getMethod(methodName, parameterTypes);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }
}
