package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import android.content.ComponentName;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.Call.Details;
import android.telecom.DisconnectCause;
import android.telecom.GatewayInfo;
import android.telecom.InCallService;
import android.telecom.PhoneAccountHandle;
import android.telecom.StatusHints;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowCall.ShadowDetails;

/** Test that {@link ShadowCall} properly emulates the expected behavior of {@link Call}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class ShadowCallTest {

  private Call call;
  private ShadowCall shadowCall;
  private Details callDetails;
  private ShadowDetails shadowCallDetails;

  @Before
  public void setup() {
    call = Shadow.newInstanceOf(Call.class);
    shadowCall = Shadow.extract(call);
    callDetails = Shadow.newInstanceOf(Details.class);
    shadowCallDetails = Shadow.extract(callDetails);
    shadowCall.setDetails(callDetails);
  }

  @Test
  public void testSetDetails() {
    assertThat(call.getDetails()).isEqualTo(callDetails);
  }

  @Test
  public void testSetState() {
    shadowCall.setState(Call.STATE_ACTIVE);
    assertThat(call.getState()).isEqualTo(Call.STATE_ACTIVE);
    shadowCall.setState(Call.STATE_CONNECTING);
    assertThat(call.getState()).isEqualTo(Call.STATE_CONNECTING);
  }

  @Test
  public void testSetVideoCall() {
    InCallService.VideoCall videoCall = mock(InCallService.VideoCall.class);
    shadowCall.setVideoCall(videoCall);
    assertThat(call.getVideoCall()).isEqualTo(videoCall);
  }

  @Test
  public void testSetParent() {
    Call parentCall = Shadow.newInstanceOf(Call.class);
    shadowCall.setParent(parentCall);
    assertThat(call.getParent()).isEqualTo(parentCall);
  }

  @Test
  public void testSetChildren() {
    Call childCall = Shadow.newInstanceOf(Call.class);
    List<Call> children = new ArrayList<>();
    children.add(childCall);
    shadowCall.setChildren(children);
    assertThat(call.getChildren()).isEqualTo(children);
  }

  @Test
  public void testSetIsRttActive() {
    assertThat(call.isRttActive()).isFalse();
    shadowCall.setIsRttActive(true);
    assertThat(call.isRttActive()).isTrue();
  }

  @Test
  public void testDetailsSetProperties() {
    int properties = 1001;
    assertThat(callDetails.getCallProperties()).isEqualTo(0);
    shadowCallDetails.setProperties(properties);
    assertThat(callDetails.getCallProperties()).isEqualTo(properties);
    assertThat(callDetails.hasProperty(properties)).isTrue();
  }

  @Test
  public void testDetailsSetCapabilities() {
    int capabilities = 1001;
    assertThat(callDetails.getCallCapabilities()).isEqualTo(0);
    shadowCallDetails.setCapabilities(capabilities);
    assertThat(callDetails.getCallCapabilities()).isEqualTo(capabilities);
  }

  @Test
  public void testDetailsSetHandle() {
    Uri handle = new Uri.Builder().path("uri_path").build();
    int presentation = 1001;
    assertThat(callDetails.getHandle()).isNull();
    assertThat(callDetails.getHandlePresentation()).isEqualTo(0);
    shadowCallDetails.setHandle(handle, presentation);
    assertThat(callDetails.getHandle()).isEqualTo(handle);
    assertThat(callDetails.getHandlePresentation()).isEqualTo(presentation);
  }

  @Test
  public void testDetailsSetCallDirection() {
    int callDirection = 1001;
    assertThat(callDetails.getCallDirection()).isEqualTo(0);
    shadowCallDetails.setCallDirection(callDirection);
    assertThat(callDetails.getCallDirection()).isEqualTo(callDirection);
  }

  @Test
  public void testDetailsSetAccountHandle() {
    PhoneAccountHandle phoneAccountHandle =
        new PhoneAccountHandle(new ComponentName("package", "class"), "account_id");
    assertThat(callDetails.getAccountHandle()).isNull();
    shadowCallDetails.setAccountHandle(phoneAccountHandle);
    assertThat(callDetails.getAccountHandle()).isEqualTo(phoneAccountHandle);
  }

  @Test
  public void testDetailsSetVideoState() {
    int videoState = 1001;
    assertThat(callDetails.getVideoState()).isEqualTo(0);
    shadowCallDetails.setVideoState(videoState);
    assertThat(callDetails.getVideoState()).isEqualTo(videoState);
  }

  @Test
  public void testDetailsSetDisconnectCause() {
    DisconnectCause disconnectCause = new DisconnectCause(/* code= */ 0);
    assertThat(callDetails.getDisconnectCause()).isNull();
    shadowCallDetails.setDisconnectCause(disconnectCause);
    assertThat(callDetails.getDisconnectCause()).isEqualTo(disconnectCause);
  }

  @Test
  public void testDetailsSetIntentExtras() {
    Bundle intentExtras = new Bundle();
    assertThat(callDetails.getIntentExtras()).isNull();
    shadowCallDetails.setIntentExtras(intentExtras);
    assertThat(callDetails.getIntentExtras()).isEqualTo(intentExtras);
  }

  @Test
  public void testDetailsSetExtras() {
    Bundle extras = new Bundle();
    assertThat(callDetails.getExtras()).isNull();
    shadowCallDetails.setExtras(extras);
    assertThat(callDetails.getExtras()).isEqualTo(extras);
  }

  @Test
  public void testDetailsSetGatewayInfo() {
    Uri uri = new Uri.Builder().path("uri_path").build();
    GatewayInfo gatewayInfo = new GatewayInfo("packageName", uri, uri);
    assertThat(callDetails.getGatewayInfo()).isNull();
    shadowCallDetails.setGatewayInfo(gatewayInfo);
    assertThat(callDetails.getGatewayInfo()).isEqualTo(gatewayInfo);
  }

  @Test
  public void testDetailsSetCallerDisplayName() {
    String displayName = "displayName";
    assertThat(callDetails.getCallerDisplayName()).isNull();
    shadowCallDetails.setCallerDisplayName(displayName);
    assertThat(callDetails.getCallerDisplayName()).isEqualTo(displayName);
  }

  @Test
  public void testDetailsSetConnectTimeMillis() {
    long currentTime = 100001;
    assertThat(callDetails.getConnectTimeMillis()).isEqualTo(0);
    shadowCallDetails.setConnectTimeMillis(currentTime);
    assertThat(callDetails.getConnectTimeMillis()).isEqualTo(currentTime);
  }

  @Test
  public void testDetailsSetCreationTimeMillis() {
    long currentTime = 100001;
    assertThat(callDetails.getCreationTimeMillis()).isEqualTo(0);
    shadowCallDetails.setCreationTimeMillis(currentTime);
    assertThat(callDetails.getCreationTimeMillis()).isEqualTo(currentTime);
  }

  @Test
  public void testDetailsSetStatusHints() {
    StatusHints statusHints =
        new StatusHints("label", Icon.createWithContentUri("uri"), new Bundle());
    assertThat(callDetails.getStatusHints()).isNull();
    shadowCallDetails.setStatusHints(statusHints);
    assertThat(callDetails.getStatusHints()).isEqualTo(statusHints);
  }
}
