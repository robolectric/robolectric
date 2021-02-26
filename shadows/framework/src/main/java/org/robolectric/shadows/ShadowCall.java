package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.Call.Details;
import android.telecom.Call.RttCall;
import android.telecom.DisconnectCause;
import android.telecom.GatewayInfo;
import android.telecom.InCallAdapter;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import android.util.Log;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Robolectric test for {@link android.telecom.Call}. */
@Implements(value = Call.class, minSdk = VERSION_CODES.LOLLIPOP)
public class ShadowCall {

  private boolean hasSentRttRequest;
  private boolean hasRespondedToRttRequest;

  private final List<Call.Callback> callbacks = new CopyOnWriteArrayList<>();

  @RealObject Call realObject;
  Details details;

  @Implementation(minSdk = VERSION_CODES.P)
  protected void sendRttRequest() {
    hasSentRttRequest = true;
  }

  /**
   * Determines whether sendRttRequest() was called.
   *
   * @return true if sendRttRequest() was called, false otherwise.
   */
  public boolean hasSentRttRequest() {
    return hasSentRttRequest;
  }

  /** "Forgets" that sendRttRequest() was called. */
  public void clearHasSentRttRequest() {
    hasSentRttRequest = false;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected void respondToRttRequest(int id, boolean accept) {
    hasRespondedToRttRequest = true;
  }

  /**
   * Determines whether respondToRttRequest() was called.
   *
   * @return True if respondToRttRequest() was called, false otherwise.
   */
  public boolean hasRespondedToRttRequest() {
    return hasRespondedToRttRequest;
  }

  /** Sets Call.Details of a Call. */
  public void setDetails(Details details) {
    this.details = details;
  }

  /**
   * Simulates a remote user accepting an RTT request after it has been initiated.
   */
  public void remotelyAcceptRttRequest(RttCall rttCall) throws Exception {
    reflector(ReflectorCall.class, realObject).setRttCall(rttCall);
    if (details == null) {
      throw new NullPointerException("Details must be set before state changes");
    }
    ShadowDetails shadowDetails = Shadow.extract(details);
    shadowDetails.setProperties(Details.PROPERTY_RTT);
    for (Call.Callback callback : callbacks) {
      callback.onRttStatusChanged(realObject, true, null);
    }
  }

  @Implementation(minSdk = VERSION_CODES.P)
  protected boolean isRttActive() {
    return realObject.getRttCall() != null && details.hasProperty(Details.PROPERTY_RTT);
  }

  @Implementation(minSdk = VERSION_CODES.N_MR1)
  protected void registerCallback(Call.Callback callback) {
    callbacks.add(callback);
  }

  @ForType(Call.class)
  interface ReflectorCall {
    @Accessor("mRttCall")
    void setRttCall(RttCall rttCall);
  }

  /** Robolectric test for {@link android.telecom.Call.RttCall}. */
  @Implements(value = Call.RttCall.class, minSdk = VERSION_CODES.O_MR1)
  public static class ShadowRttCall {
    private static final String TAG = "ShadowRttCall";
    @RealObject RttCall realRttCallObject;
    PipedOutputStream pipedOutputStream = new PipedOutputStream();

    @Implementation
    protected void __constructor__(
        String telecomCallId,
        InputStreamReader receiveStream,
        OutputStreamWriter transmitStream,
        int mode,
        InCallAdapter inCallAdapter) {
      PipedInputStream pipedInputStream = new PipedInputStream();
      try {
        pipedInputStream.connect(pipedOutputStream);
      } catch (IOException e) {
        Log.w(TAG, "Could not connect streams.");
      }
      invokeConstructor(
          RttCall.class,
          realRttCallObject,
          ClassParameter.from(String.class, telecomCallId),
          ClassParameter.from(InputStreamReader.class, new InputStreamReader(pipedInputStream)),
          ClassParameter.from(OutputStreamWriter.class, transmitStream),
          ClassParameter.from(int.class, mode),
          ClassParameter.from(InCallAdapter.class, inCallAdapter));
    }

    /**
     * Writes a message to the RttCall buffer. This simulates receiving a message from a sender
     * during an RTT call.
     *
     * @param message from sender.
     * @throws IOException if write to buffer fails.
     */
    public void writeRemoteMessage(String message) throws IOException {
      byte[] messageBytes = message.getBytes();
      pipedOutputStream.write(messageBytes, 0, messageBytes.length);
    }
  }

  /** Shadow for {@link Call.Details} */
  @Implements(value = Call.Details.class, minSdk = VERSION_CODES.LOLLIPOP)
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
    int callerNumberVerificationStatus;

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

    @Implementation(minSdk = VERSION_CODES.M)
    public boolean hasProperty(int property) {
      return (this.properties & property) > 0;
    }

    @Implementation(minSdk = VERSION_CODES.LOLLIPOP_MR1)
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

    @Implementation(minSdk = VERSION_CODES.Q)
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

    public Details getDetails() {
      return realObject;
    }

    public void setDisconnectCause(DisconnectCause disconnectCause) {
      reflector(ReflectorDetails.class, realObject).setDisconnectCause(disconnectCause);
    }

    @Implementation(minSdk = VERSION_CODES.M)
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

    public void setConnectTimeMillis() {
      setConnectTimeMillis(ShadowSystemClock.currentTimeMillis());
    }

    public void setConnectTimeMillis(long connectTimeMillis) {
      this.connectTimeMillis = connectTimeMillis;
    }

    @Implementation(minSdk = VERSION_CODES.O)
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

    @Implementation(minSdk = VERSION_CODES.R)
    public int getCallerNumberVerificationStatus() {
      return callerNumberVerificationStatus;
    }

    public void setCallerNumberVerificationStatus(int callerNumberVerificationStatus) {
      this.callerNumberVerificationStatus = callerNumberVerificationStatus;
    }

    @ForType(ShadowDetails.class)
    interface ReflectorDetails {
      @Accessor("mDisconnectCause")
      void setDisconnectCause(DisconnectCause disconnectCause);
    }
  }
}
