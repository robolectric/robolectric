package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.os.Build.VERSION_CODES;
import android.telecom.Call;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Test of ShadowCall. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.LOLLIPOP)
public final class ShadowCallTest {
  Call call;
  ShadowCall shadowCall;

  @Before
  public void setUp() throws Exception {
    call = ReflectionHelpers.callConstructor(Call.class);
    shadowCall = shadowOf(call);
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
}
