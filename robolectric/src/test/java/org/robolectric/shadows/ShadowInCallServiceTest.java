package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallService;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Robolectric test for {@link ShadowInCallService}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class ShadowInCallServiceTest {

  private InCallService inCallService;

  @Before
  public void setUp() {
    inCallService = new InCallService() {};
  }

  @Test
  public void setCallListEmpty_getCallListEmpty() {
    Call[] calls = new Call[] {};
    testSetCallListGetCallList(calls);
  }

  @Test
  public void setCallListOne_getCallListOne() {
    Call call = Shadow.newInstanceOf(Call.class);
    Call[] calls = new Call[] {call};
    testSetCallListGetCallList(calls);
  }

  @Test
  public void setCallListTwo_getCallListTwo() {
    Call call1 = Shadow.newInstanceOf(Call.class);
    Call call2 = Shadow.newInstanceOf(Call.class);
    Call[] calls = new Call[] {call1, call2};
    testSetCallListGetCallList(calls);
  }

  @Test
  public void setAudioRoute_getAudioRoute() {
    testSetAudioGetAudio(CallAudioState.ROUTE_EARPIECE);
    testSetAudioGetAudio(CallAudioState.ROUTE_SPEAKER);
    testSetAudioGetAudio(CallAudioState.ROUTE_BLUETOOTH);
    testSetAudioGetAudio(CallAudioState.ROUTE_WIRED_HEADSET);
  }

  public void testSetCallListGetCallList(Call[] calls) {
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    for (Call call : calls) {
      shadowInCallService.addCall(call);
    }

    List<Call> callList = inCallService.getCalls();

    for (int i = 0; i < calls.length; i++) {
      assertThat(callList.get(i)).isEqualTo(calls[i]);
    }
  }

  private void testSetAudioGetAudio(int audioRoute) {
    inCallService.setAudioRoute(audioRoute);

    assertThat(inCallService.getCallAudioState().getRoute()).isEqualTo(audioRoute);
  }
}
