package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION_CODES;
import android.telecom.Call;
import android.telecom.InCallAdapter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
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
    ReflectionHelpers.setField(call, "mCallbackRecords", new CopyOnWriteArrayList<>());
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
  @Config(minSdk = VERSION_CODES.M)
  public void triggerStateChanged_invokesCallback() {
    Call.Callback mockCallback = mock(Call.Callback.class);
    call.registerCallback(mockCallback);

    shadowCall.triggerStateChanged(Call.STATE_ACTIVE);

    verify(mockCallback).onStateChanged(call, Call.STATE_ACTIVE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void triggerDetailsChanged_invokesCallback() {
    Call.Callback mockCallback = mock(Call.Callback.class);
    call.registerCallback(mockCallback);
    Call.Details details = ReflectionHelpers.callConstructor(Call.Details.class);

    shadowCall.triggerDetailsChanged(details);

    verify(mockCallback).onDetailsChanged(call, details);
  }

  @Test
  @Config(minSdk = VERSION_CODES.M)
  public void triggerCallDestroyed_invokesCallback() {
    Call.Callback mockCallback = mock(Call.Callback.class);
    call.registerCallback(mockCallback);

    shadowCall.triggerCallDestroyed();

    verify(mockCallback).onCallDestroyed(call);
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
  public void setInCallAdapter_updatesAdapter() {
    InCallAdapter adapter = Shadow.newInstanceOf(InCallAdapter.class);
    shadowCall.setInCallAdapter(adapter);

    InCallAdapter internalAdapter = ReflectionHelpers.getField(call, "mInCallAdapter");
    assertThat(internalAdapter).isEqualTo(adapter);
  }
}
