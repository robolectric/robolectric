package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.telecom.Call;
import android.telecom.DisconnectCause;
import android.telecom.PhoneAccountHandle;
import android.telecom.VideoProfile;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.S)
public class CallDetailsBuilderTest {

  @Test
  public void newBuilder_createsEmptyDetails() {
    Call.Details details = CallDetailsBuilder.newBuilder().build();
    assertThat(details.getState()).isEqualTo(0);
    assertThat(details.getAccountHandle()).isNull();
    assertThat(details.getContactDisplayName()).isNull();
  }

  @Test
  public void newBuilder_setsAllFields() {
    PhoneAccountHandle handle = new PhoneAccountHandle(new ComponentName("pkg", "cls"), "id");
    Uri uri = Uri.parse("tel:12345");
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

  @Test
  public void newBuilder_copyConstructor_preservesAllFields() {
    PhoneAccountHandle handle = new PhoneAccountHandle(new ComponentName("pkg", "cls"), "id");
    Uri uri = Uri.parse("tel:12345");
    DisconnectCause cause = new DisconnectCause(DisconnectCause.LOCAL);
    Bundle extras = new Bundle();

    Call.Details originalDetails =
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

    Call.Details copiedDetails = CallDetailsBuilder.newBuilder(originalDetails).build();

    assertThat(copiedDetails.getState()).isEqualTo(Call.STATE_ACTIVE);
    assertThat(copiedDetails.getCallDirection()).isEqualTo(Call.Details.DIRECTION_INCOMING);
    assertThat(copiedDetails.getVideoState()).isEqualTo(VideoProfile.STATE_AUDIO_ONLY);
    assertThat(copiedDetails.getAccountHandle()).isEqualTo(handle);
    assertThat(copiedDetails.getContactDisplayName()).isEqualTo("Test Caller");
    assertThat(copiedDetails.getHandle()).isEqualTo(uri);
    assertThat(copiedDetails.getConnectTimeMillis()).isEqualTo(12345L);
    assertThat(copiedDetails.getDisconnectCause()).isEqualTo(cause);
    assertThat(copiedDetails.getCallProperties()).isEqualTo(Call.Details.PROPERTY_HIGH_DEF_AUDIO);
    assertThat(copiedDetails.getExtras()).isEqualTo(extras);
  }

  @Test
  public void newBuilder_copyConstructor_allowsMutation() {
    PhoneAccountHandle handle1 = new PhoneAccountHandle(new ComponentName("pkg", "cls1"), "id1");
    PhoneAccountHandle handle2 = new PhoneAccountHandle(new ComponentName("pkg", "cls2"), "id2");

    Call.Details originalDetails =
        CallDetailsBuilder.newBuilder()
            .setState(Call.STATE_ACTIVE)
            .setAccountHandle(handle1)
            .setContactDisplayName("Caller 1")
            .build();

    Call.Details mutatedDetails =
        CallDetailsBuilder.newBuilder(originalDetails)
            .setAccountHandle(handle2)
            .setContactDisplayName("Caller 2")
            .build();

    assertThat(mutatedDetails.getState()).isEqualTo(Call.STATE_ACTIVE);
    assertThat(mutatedDetails.getAccountHandle()).isEqualTo(handle2);
    assertThat(mutatedDetails.getContactDisplayName()).isEqualTo("Caller 2");
  }
}
