package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
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
  public void setCanAddCall_canAddCall() {
    testSetCanAddCallGetCanAddCall(true);
    testSetCanAddCallGetCanAddCall(false);
  }

  @Test
  public void setMuted_getMuted() {
    testSetMutedGetMuted(true);
    testSetMutedGetMuted(false);
  }

  @Test
  public void setAudioRoute_getCallAudioState() {
    testSetAudioRouteGetCallAudioState(CallAudioState.ROUTE_EARPIECE);
    testSetAudioRouteGetCallAudioState(CallAudioState.ROUTE_SPEAKER);
    testSetAudioRouteGetCallAudioState(CallAudioState.ROUTE_BLUETOOTH);
    testSetAudioRouteGetCallAudioState(CallAudioState.ROUTE_WIRED_HEADSET);
  }

  @Test
  @TargetApi(P)
  @Config(
      minSdk = P,
      shadows = {ShadowBluetoothDevice.class})
  public void requestBluetoothAudio_getBluetoothAudio() {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    inCallService.requestBluetoothAudio(bluetoothDevice);

    assertThat(shadowInCallService.getBluetoothAudio()).isEqualTo(bluetoothDevice);
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

  private void testSetCanAddCallGetCanAddCall(boolean canAddCall) {
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setCanAddCall(canAddCall);

    assertThat(inCallService.canAddCall()).isEqualTo(canAddCall);
  }

  private void testSetMutedGetMuted(boolean muted) {
    inCallService.setMuted(muted);

    assertThat(inCallService.getCallAudioState().isMuted()).isEqualTo(muted);
  }

  private void testSetAudioRouteGetCallAudioState(int audioRoute) {
    inCallService.setAudioRoute(audioRoute);

    assertThat(inCallService.getCallAudioState().getRoute()).isEqualTo(audioRoute);
  }
}
