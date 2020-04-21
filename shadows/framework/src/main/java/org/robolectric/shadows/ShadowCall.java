package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.telecom.Call;
import android.telecom.Call.Details;
import android.telecom.DisconnectCause;
import android.telecom.GatewayInfo;
import android.telecom.InCallService;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Robolectric shadow for {@link android.telecom.Call}. */
@Implements(value = Call.class, minSdk = Q)
public class ShadowCall {

  @RealObject Call realObject;

  Details details;

  @Nullable private Call.Callback callback;

  private InCallService.VideoCall videoCall;

  @Nullable private Call parent = null;
  private List<Call> children = new ArrayList<>();

  @Nullable private Character currentlyPlayingDtmfDigit;

  private CallEvent lastCallEvent;

  private boolean isRttActive;
  private boolean hasSentRttRequest;
  private boolean hasRespondedToRttRequest;

  public void setDetails(Details details) {
    this.details = details;
  }

  public void setState(int state) {
    ReflectionHelpers.setField(realObject, "mState", state);
    if (details == null) {
      throw new NullPointerException("Details must be set before state changes");
    }
    if (state == Call.STATE_ACTIVE) {
      ShadowDetails shadowDetails = Shadow.extract(details);
      shadowDetails.setConnectTimeMillis(System.currentTimeMillis());
    }

    if (callback != null) {
      callback.onStateChanged(realObject, state);
    }
  }

  public void setVideoCall(InCallService.VideoCall videoCall) {
    this.videoCall = videoCall;
  }

  public void setParent(Call parent) {
    this.parent = parent;
  }

  public void setChildren(List<Call> children) {
    this.children = children;
  }

  public void setIsRttActive(boolean isRttActive) {
    this.isRttActive = isRttActive;
  }

  public boolean hasRespondedToRttRequest() {
    return hasRespondedToRttRequest;
  }

  @Implementation
  public Details getDetails() {
    return details;
  }

  @Implementation
  public Call getParent() {
    return parent;
  }

  @Implementation
  public List<Call> getChildren() {
    return children;
  }

  @Implementation
  public List<Call> getConferenceableCalls() {
    return new ArrayList<>();
  }

  @Implementation
  public InCallService.VideoCall getVideoCall() {
    return videoCall;
  }

  @Implementation
  public void registerCallback(Call.Callback callback) {
    this.callback = callback;
  }

  @Implementation
  public void registerCallback(Call.Callback callback, Handler handler) {
    this.callback = callback;
  }

  @Implementation
  public void unregisterCallback(Call.Callback callback) {
    this.callback = null;
  }

  @Implementation
  public void unhold() {
    setState(Call.STATE_ACTIVE);
  }

  @Implementation
  public void hold() {
    setState(Call.STATE_HOLDING);
  }

  @Implementation
  public void disconnect() {
    ((ShadowDetails) Shadow.extract(getDetails()))
        .setDisconnectCause(new DisconnectCause(DisconnectCause.LOCAL));
    setState(Call.STATE_DISCONNECTED);
  }

  @Implementation
  public void answer(int videoState) {
    setState(Call.STATE_ACTIVE);
    ((ShadowDetails) Shadow.extract(getDetails())).setVideoState(videoState);
  }

  @Implementation
  public void reject(boolean rejectWithMessage, String textMessage) {
    setState(Call.STATE_DISCONNECTED);
  }

  public Call.Callback getCallback() {
    return callback;
  }

  @Implementation
  public void playDtmfTone(char digit) {
    currentlyPlayingDtmfDigit = digit;
  }

  @Implementation
  public void stopDtmfTone() {
    currentlyPlayingDtmfDigit = null;
  }

  /** Returns the digit whose tone is currently playing or null if no tone is being played */
  @Nullable
  public Character getCurrentlyPlayingDtmfDigit() {
    return currentlyPlayingDtmfDigit;
  }

  @Implementation
  public void sendCallEvent(String event, Bundle extras) {
    this.lastCallEvent = CallEvent.create(event, extras);
  }

  public String getLastCallEventEvent() {
    return lastCallEvent.event();
  }

  public Bundle getLastCallEventExtras() {
    return lastCallEvent.extras();
  }

  @Implementation
  public boolean isRttActive() {
    return isRttActive;
  }

  @Implementation
  public void respondToRttRequest(int id, boolean accept) {
    hasRespondedToRttRequest = true;
  }

  @Implementation
  public void sendRttRequest() {
    hasSentRttRequest = true;
  }

  public boolean hasSentRttRequest() {
    return hasSentRttRequest;
  }

  public void stopRtt() {
    isRttActive = false;
  }

  /** Shadow for {@link Call.Details} */
  @Implements(value = Call.Details.class, minSdk = Q)
  public static class ShadowDetails {

    @RealObject Details realObject;

    int properties;
    int capabilities;
    int videoState;
    Uri handle;
    int handlePresentation;
    PhoneAccountHandle accountHandle;
    Bundle intentExtras;
    Bundle extras;
    GatewayInfo gatewayInfo;
    String callerDisplayName;
    long connectTimeMillis;
    long creationTimeMillis;
    int callDirection;
    StatusHints statusHints;

    public void setProperties(int properties) {
      this.properties = properties;
    }

    public void setCapabilities(int capabilities) {
      this.capabilities = capabilities;
    }

    @Implementation
    public int getCallCapabilities() {
      return this.capabilities;
    }

    @Implementation
    public boolean hasProperty(int property) {
      return (this.properties & property) > 0;
    }

    @Implementation
    public int getCallProperties() {
      return properties;
    }

    @Implementation
    public boolean can(int capability) {
      return (this.capabilities & capability) > 0;
    }

    public void setHandle(Uri handle, int presentation) {
      this.handle = handle;
      this.handlePresentation = presentation;
    }

    public void setCallDirection(int callDirection) {
      this.callDirection = callDirection;
    }

    @Implementation
    public int getCallDirection() {
      return callDirection;
    }

    @Implementation
    public Uri getHandle() {
      return handle;
    }

    @Implementation
    public int getHandlePresentation() {
      return handlePresentation;
    }

    public void setAccountHandle(PhoneAccountHandle accountHandle) {
      this.accountHandle = accountHandle;
    }

    public void setVideoState(int videoState) {
      this.videoState = videoState;
    }

    @Implementation
    public int getVideoState() {
      return videoState;
    }

    @Implementation
    public PhoneAccountHandle getAccountHandle() {
      return accountHandle;
    }

    public void setDisconnectCause(DisconnectCause disconnectCause) {
      ReflectionHelpers.setField(realObject, "mDisconnectCause", disconnectCause);
    }

    @Implementation
    public Bundle getIntentExtras() {
      return intentExtras;
    }

    public void setIntentExtras(Bundle intentExtras) {
      this.intentExtras = intentExtras;
    }

    @Implementation
    public Bundle getExtras() {
      return extras;
    }

    public void setExtras(Bundle extras) {
      this.extras = extras;
    }

    @Implementation
    public GatewayInfo getGatewayInfo() {
      return gatewayInfo;
    }

    public void setGatewayInfo(GatewayInfo gatewayInfo) {
      this.gatewayInfo = gatewayInfo;
    }

    @Implementation
    public String getCallerDisplayName() {
      return callerDisplayName;
    }

    public void setCallerDisplayName(String callerDisplayName) {
      this.callerDisplayName = callerDisplayName;
    }

    @Implementation
    public long getConnectTimeMillis() {
      return connectTimeMillis;
    }

    public void setConnectTimeMillis(long connectTimeMillis) {
      this.connectTimeMillis = connectTimeMillis;
    }

    @Implementation
    public long getCreationTimeMillis() {
      return creationTimeMillis;
    }

    public void setCreationTimeMillis(long creationTimeMillis) {
      this.creationTimeMillis = creationTimeMillis;
    }

    @Implementation
    public StatusHints getStatusHints() {
      return statusHints;
    }

    public void setStatusHints(StatusHints statusHints) {
      this.statusHints = statusHints;
    }
  }

  @AutoValue
  abstract static class CallEvent {

    static CallEvent create(String event, Bundle extras) {
      return new AutoValue_ShadowCall_CallEvent(event, extras);
    }

    abstract String event();

    abstract Bundle extras();
  }
}
