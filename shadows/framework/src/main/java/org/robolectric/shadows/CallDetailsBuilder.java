package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link android.telecom.Call.Details}. */
public class CallDetailsBuilder {
  private final Call.Details details;

  /**
   * Creates a new instance of CallDetailsBuilder.
   *
   * @param original the original details to copy from, or null to start fresh.
   */
  private CallDetailsBuilder(Call.Details original) {
    this.details = ReflectionHelpers.callConstructor(Call.Details.class);
    if (original != null) {
      CallDetailsReflector originalReflector = reflector(CallDetailsReflector.class, original);
      CallDetailsReflector newReflector = reflector(CallDetailsReflector.class, this.details);
      // Explicit API checks (No try-catch blocks)
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S) {
        newReflector.setState(originalReflector.getState());
        newReflector.setCallDirection(originalReflector.getCallDirection());
      }
      if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.R) {
        newReflector.setContactDisplayName(originalReflector.getContactDisplayName());
      }

      newReflector.setVideoState(originalReflector.getVideoState());
      newReflector.setAccountHandle(originalReflector.getAccountHandle());
      newReflector.setHandle(originalReflector.getHandle());
      newReflector.setConnectTimeMillis(originalReflector.getConnectTimeMillis());
      newReflector.setDisconnectCause(originalReflector.getDisconnectCause());
      newReflector.setCallProperties(originalReflector.getCallProperties());
      newReflector.setExtras(originalReflector.getExtras());
    }
  }

  /** Creates a new, empty builder for {@link Call.Details}. */
  public static CallDetailsBuilder newBuilder() {
    return new CallDetailsBuilder(null);
  }

  /** Creates a new builder populated with data from the given {@link Call.Details}. */
  public static CallDetailsBuilder newBuilder(Call.Details original) {
    return new CallDetailsBuilder(original);
  }

  /**
   * Sets the state of the call.
   *
   * @param state the call state (e.g., {@link android.telecom.Call#STATE_ACTIVE})
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setState(int state) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.S) {
      reflector(CallDetailsReflector.class, details).setState(state);
    }
    return this;
  }

  /**
   * Sets the direction of the call (e.g., incoming or outgoing).
   *
   * @param callDirection the call direction (e.g., {@link
   *     android.telecom.Call.Details#DIRECTION_INCOMING})
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setCallDirection(int callDirection) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q) {
      reflector(CallDetailsReflector.class, details).setCallDirection(callDirection);
    }
    return this;
  }

  /**
   * Sets the video state of the call.
   *
   * @param videoState the video state (e.g., {@link android.telecom.VideoProfile#STATE_AUDIO_ONLY})
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setVideoState(int videoState) {
    reflector(CallDetailsReflector.class, details).setVideoState(videoState);
    return this;
  }

  /**
   * Sets the phone account handle associated with the call.
   *
   * @param handle the phone account handle for the call
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setAccountHandle(PhoneAccountHandle handle) {
    reflector(CallDetailsReflector.class, details).setAccountHandle(handle);
    return this;
  }

  /**
   * Sets the display name of the contact.
   *
   * @param name the display name of the contact
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setContactDisplayName(String name) {
    if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.R) {
      reflector(CallDetailsReflector.class, details).setContactDisplayName(name);
    }
    return this;
  }

  /**
   * Sets the handle (e.g. phone number) for the call.
   *
   * @param handle the handle for the call
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setHandle(Uri handle) {
    reflector(CallDetailsReflector.class, details).setHandle(handle);
    return this;
  }

  /**
   * Sets the time the call connected in milliseconds.
   *
   * @param connectTimeMillis the time the call connected in milliseconds
   * @return this builder instance
   */
  @SuppressWarnings("GoodTime")
  @CanIgnoreReturnValue
  public CallDetailsBuilder setConnectTimeMillis(long connectTimeMillis) {
    reflector(CallDetailsReflector.class, details).setConnectTimeMillis(connectTimeMillis);
    return this;
  }

  /**
   * Sets the disconnect cause for the call.
   *
   * @param disconnectCause the disconnect cause for the call
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setDisconnectCause(DisconnectCause disconnectCause) {
    reflector(CallDetailsReflector.class, details).setDisconnectCause(disconnectCause);
    return this;
  }

  /**
   * Sets properties associated with the call.
   *
   * @param properties the properties associated with the call
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setCallProperties(int properties) {
    reflector(CallDetailsReflector.class, details).setCallProperties(properties);
    return this;
  }

  /**
   * Sets the extras bundle for the call details.
   *
   * @param extras the extras bundle for the call details
   * @return this builder instance
   */
  @CanIgnoreReturnValue
  public CallDetailsBuilder setExtras(Bundle extras) {
    reflector(CallDetailsReflector.class, details).setExtras(extras);
    return this;
  }

  /**
   * Builds and returns a new {@link Call.Details} instance with the configured values.
   *
   * @return the constructed {@link Call.Details} object.
   */
  public Call.Details build() {
    return new CallDetailsBuilder(this.details).details;
  }

  /** Reflector interface for {@link Call.Details} required to safely access hidden fields. */
  @ForType(Call.Details.class)
  private interface CallDetailsReflector {
    @Accessor("mState")
    int getState();

    @Accessor("mState")
    void setState(int state);

    @Accessor("mCallDirection")
    int getCallDirection();

    @Accessor("mCallDirection")
    void setCallDirection(int callDirection);

    @Accessor("mContactDisplayName")
    String getContactDisplayName();

    @Accessor("mContactDisplayName")
    void setContactDisplayName(String name);

    @Accessor("mVideoState")
    int getVideoState();

    @Accessor("mVideoState")
    void setVideoState(int videoState);

    @Accessor("mAccountHandle")
    PhoneAccountHandle getAccountHandle();

    @Accessor("mAccountHandle")
    void setAccountHandle(PhoneAccountHandle handle);

    @Accessor("mHandle")
    Uri getHandle();

    @Accessor("mHandle")
    void setHandle(Uri handle);

    @Accessor("mConnectTimeMillis")
    long getConnectTimeMillis();

    @Accessor("mConnectTimeMillis")
    void setConnectTimeMillis(long connectTimeMillis);

    @Accessor("mDisconnectCause")
    DisconnectCause getDisconnectCause();

    @Accessor("mDisconnectCause")
    void setDisconnectCause(DisconnectCause disconnectCause);

    @Accessor("mCallProperties")
    int getCallProperties();

    @Accessor("mCallProperties")
    void setCallProperties(int properties);

    @Accessor("mExtras")
    Bundle getExtras();

    @Accessor("mExtras")
    void setExtras(Bundle extras);
  }
}
