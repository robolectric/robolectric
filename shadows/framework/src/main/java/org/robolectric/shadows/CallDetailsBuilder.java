package org.robolectric.shadows;

import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import java.lang.reflect.Field;
import org.robolectric.util.ReflectionHelpers;

/** Builder for {@link android.telecom.Call.Details}. */
public class CallDetailsBuilder {
  private final Call.Details details;

  private CallDetailsBuilder(Call.Details original) {
    this.details = ReflectionHelpers.callConstructor(Call.Details.class);
    if (original != null) {
      for (Field field : Call.Details.class.getDeclaredFields()) {
        try {
          field.setAccessible(true);
          field.set(this.details, field.get(original));
        } catch (Exception e) {
          throw new RuntimeException("Failed to copy field " + field.getName(), e);
        }
      }
    }
  }

  public static CallDetailsBuilder newBuilder() {
    return new CallDetailsBuilder(null);
  }

  public static CallDetailsBuilder newBuilder(Call.Details original) {
    return new CallDetailsBuilder(original);
  }

  private void setFieldSafely(String fieldName, Object value) {
    try {
      ReflectionHelpers.setField(details, fieldName, value);
    } catch (RuntimeException e) {
      if (e.toString().contains("NoSuchFieldException")
          || e.toString().contains("NoSuchFieldError")) {
        // Ignored. Field does not exist on this SDK level.
      } else {
        throw e;
      }
    }
  }

  public CallDetailsBuilder setState(int state) {
    setFieldSafely("mState", state);
    return this;
  }

  public CallDetailsBuilder setCallDirection(int callDirection) {
    setFieldSafely("mCallDirection", callDirection);
    return this;
  }

  public CallDetailsBuilder setVideoState(int videoState) {
    setFieldSafely("mVideoState", videoState);
    return this;
  }

  public CallDetailsBuilder setAccountHandle(PhoneAccountHandle handle) {
    setFieldSafely("mAccountHandle", handle);
    return this;
  }

  public CallDetailsBuilder setContactDisplayName(String name) {
    setFieldSafely("mContactDisplayName", name);
    return this;
  }

  public CallDetailsBuilder setHandle(Uri handle) {
    setFieldSafely("mHandle", handle);
    return this;
  }

  @SuppressWarnings("GoodTime")
  public CallDetailsBuilder setConnectTimeMillis(long connectTimeMillis) {
    setFieldSafely("mConnectTimeMillis", connectTimeMillis);
    return this;
  }

  public CallDetailsBuilder setDisconnectCause(DisconnectCause disconnectCause) {
    setFieldSafely("mDisconnectCause", disconnectCause);
    return this;
  }

  public CallDetailsBuilder setCallProperties(int properties) {
    setFieldSafely("mCallProperties", properties);
    return this;
  }

  public CallDetailsBuilder setExtras(Bundle extras) {
    setFieldSafely("mExtras", extras);
    return this;
  }

  public Call.Details build() {
    Call.Details result = ReflectionHelpers.callConstructor(Call.Details.class);
    for (Field field : Call.Details.class.getDeclaredFields()) {
      try {
        field.setAccessible(true);
        field.set(result, field.get(this.details));
      } catch (Exception e) {
        throw new RuntimeException("Failed to copy field " + field.getName(), e);
      }
    }
    return result;
  }
}
