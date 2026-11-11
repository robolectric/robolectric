package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.telecom.CallAudioState;
import android.telecom.InCallAdapter;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Robolectric test for {@link ShadowInCallAdapter}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Config.OLDEST_SDK)
public class ShadowInCallAdapterTest {

  @Test
  public void setAudioRoute_getAudioRoute() {
    testSetAudioGetAudio(CallAudioState.ROUTE_EARPIECE);
    testSetAudioGetAudio(CallAudioState.ROUTE_SPEAKER);
    testSetAudioGetAudio(CallAudioState.ROUTE_BLUETOOTH);
    testSetAudioGetAudio(CallAudioState.ROUTE_WIRED_HEADSET);
  }

  private static void testSetAudioGetAudio(int audioRoute) {
    InCallAdapter adapter = Shadow.newInstanceOf(InCallAdapter.class);
    adapter.setAudioRoute(audioRoute);
    assertThat(((ShadowInCallAdapter) Shadow.extract(adapter)).getAudioRoute())
        .isEqualTo(audioRoute);
  }
}
