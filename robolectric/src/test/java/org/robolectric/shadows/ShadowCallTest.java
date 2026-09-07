package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.VideoProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Test of ShadowCall. */
@RunWith(AndroidJUnit4.class)
public final class ShadowCallTest {
  Call call;
  ShadowCall shadowCall;

  @Before
  public void setUp() throws Exception {
    call = ReflectionHelpers.callConstructor(Call.class);
    shadowCall = shadowOf(call);
    shadowCall.setCallbackRecords(new CopyOnWriteArrayList<>());
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void sendRttRequest() {
    call.sendRttRequest();

    assertThat(shadowCall.hasSentRttRequest()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void clearHasSentRttRequest() {
    call.sendRttRequest();

    shadowCall.clearHasSentRttRequest();

    assertThat(shadowCall.hasSentRttRequest()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void hasRespondedToRttRequest() {
    call.respondToRttRequest(0, true);

    assertThat(shadowCall.hasRespondedToRttRequest()).isTrue();
  }

  @Test
  public void getCallbacks_initiallyEmpty() {
    assertThat(shadowCall.getCallbacks()).isEmpty();
  }

  @Test
  public void getCallbacks_withRegisteredCallback() {
    Call.Callback mockCallback = mock(Call.Callback.class);
    call.registerCallback(mockCallback);

    assertThat(shadowCall.getCallbacks()).containsExactly(mockCallback);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void setState_updatesState() {
    shadowCall.setState(Call.STATE_ACTIVE);
    assertThat(call.getState()).isEqualTo(Call.STATE_ACTIVE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void setDetails_updatesDetails() {
    Call.Details details = ReflectionHelpers.callConstructor(Call.Details.class);
    shadowCall.setDetails(details);

    assertThat(call.getDetails()).isEqualTo(details);
  }

  @Test
  @Config(minSdk = VERSION_CODES.S)
  public void setDetailsProperties_updatesCallDetails() {
    PhoneAccountHandle handle = new PhoneAccountHandle(new ComponentName("pkg", "cls"), "id");
    Uri uri = Uri.parse("tel:1234");
    DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
    Bundle extras = new Bundle();

    Call.Details details =
        CallDetailsBuilder.newBuilder()
            .setState(Call.STATE_ACTIVE)
            .setCallDirection(Call.Details.DIRECTION_INCOMING)
            .setVideoState(VideoProfile.STATE_AUDIO_ONLY)
            .setAccountHandle(handle)
            .setContactDisplayName("Test Caller")
            .setHandle(uri)
            .setConnectTimeMillis(12345L)
            .setDisconnectCause(cause)
            .setCallProperties(Call.Details.PROPERTY_HIGH_DEF_AUDIO)
            .setExtras(extras)
            .build();

    assertThat(details.getState()).isEqualTo(Call.STATE_ACTIVE);
    assertThat(details.getCallDirection()).isEqualTo(Call.Details.DIRECTION_INCOMING);
    assertThat(details.getVideoState()).isEqualTo(VideoProfile.STATE_AUDIO_ONLY);
    assertThat(details.getAccountHandle()).isEqualTo(handle);
    assertThat(details.getContactDisplayName()).isEqualTo("Test Caller");
    assertThat(details.getHandle()).isEqualTo(uri);
    assertThat(details.getConnectTimeMillis()).isEqualTo(12345L);
    assertThat(details.getDisconnectCause()).isEqualTo(cause);
    assertThat(details.getCallProperties()).isEqualTo(Call.Details.PROPERTY_HIGH_DEF_AUDIO);
    assertThat(details.getExtras()).isEqualTo(extras);
  }
}
