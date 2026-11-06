package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallAdapter;
import android.telecom.InCallService;
import android.telecom.Phone;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.android.internal.telecom.IInCallAdapter;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowInCallService.ReflectorInCallService;
import org.robolectric.shadows.ShadowInCallService.ReflectorPhone;
import org.robolectric.util.ReflectionHelpers;

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
  public void addTwoCalls_removeOne_getCallListOne() {
    Call call1 = Shadow.newInstanceOf(Call.class);
    Call call2 = Shadow.newInstanceOf(Call.class);
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.addCall(call1);
    shadowInCallService.addCall(call2);
    shadowInCallService.removeCall(call2);

    assertThat(inCallService.getCalls()).containsExactly(call1);
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
  @Config(minSdk = P)
  public void requestBluetoothAudio_getBluetoothAudio() {
    BluetoothDevice bluetoothDevice = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    inCallService.requestBluetoothAudio(bluetoothDevice);

    assertThat(shadowInCallService.getBluetoothAudio()).isEqualTo(bluetoothDevice);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void setSupportedBluetoothDevices_getCallAudioState() {
    BluetoothDevice device1 = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    BluetoothDevice device2 = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:CC");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setSupportedBluetoothDevices(Arrays.asList(device1, device2));

    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices())
        .containsExactly(device1, device2);
    assertThat(shadowInCallService.getSupportedBluetoothDevices())
        .containsExactly(device1, device2);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void setSupportedBluetoothDevices_null_clearsList() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.addSupportedBluetoothDevice(device);
    shadowInCallService.setSupportedBluetoothDevices(null);

    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices()).isEmpty();
    assertThat(shadowInCallService.getSupportedBluetoothDevices()).isEmpty();
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void addSupportedBluetoothDevice_getCallAudioState() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.addSupportedBluetoothDevice(device);

    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices())
        .containsExactly(device);
    assertThat(shadowInCallService.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void addSupportedBluetoothDevice_ignoresDuplicatesAndNull() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.addSupportedBluetoothDevice(device);
    shadowInCallService.addSupportedBluetoothDevice(device);
    shadowInCallService.addSupportedBluetoothDevice(null);

    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices())
        .containsExactly(device);
    assertThat(shadowInCallService.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void clearSupportedBluetoothDevices() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.addSupportedBluetoothDevice(device);
    shadowInCallService.clearSupportedBluetoothDevices();

    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices()).isEmpty();
    assertThat(shadowInCallService.getSupportedBluetoothDevices()).isEmpty();
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void getSupportedBluetoothDevices_unmodifiable() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    List<BluetoothDevice> supportedDevices = shadowInCallService.getSupportedBluetoothDevices();

    assertThrows(UnsupportedOperationException.class, () -> supportedDevices.add(device));
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void getCallAudioState_returnsDefensiveSnapshot() {
    BluetoothDevice device1 = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    BluetoothDevice device2 = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:CC");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.addSupportedBluetoothDevice(device1);
    CallAudioState snapshot = inCallService.getCallAudioState();
    shadowInCallService.addSupportedBluetoothDevice(device2);

    assertThat(snapshot.getSupportedBluetoothDevices()).containsExactly(device1);
    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices())
        .containsExactly(device1, device2);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void setActiveBluetoothDevice_getCallAudioState() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setActiveBluetoothDevice(device);

    CallAudioState state = inCallService.getCallAudioState();
    assertThat(state.getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(state.getRoute()).isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
    assertThat(state.getSupportedBluetoothDevices()).containsExactly(device);
    assertThat(shadowInCallService.getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(shadowInCallService.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void requestBluetoothAudio_updatesActiveBluetoothDeviceAndRoute() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");

    inCallService.requestBluetoothAudio(device);

    CallAudioState state = inCallService.getCallAudioState();
    assertThat(state.getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(state.getRoute()).isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
    assertThat(state.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void setCallAudioState_sdkAtLeastP() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    CallAudioState callAudioState =
        ShadowInCallService.newCallAudioState(
            /* muted= */ true,
            CallAudioState.ROUTE_BLUETOOTH,
            CallAudioState.ROUTE_BLUETOOTH | CallAudioState.ROUTE_SPEAKER,
            device,
            Arrays.asList(device));
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setCallAudioState(callAudioState);

    CallAudioState actual = inCallService.getCallAudioState();
    assertThat(actual.isMuted()).isTrue();
    assertThat(actual.getRoute()).isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
    assertThat(actual.getSupportedRouteMask())
        .isEqualTo(CallAudioState.ROUTE_BLUETOOTH | CallAudioState.ROUTE_SPEAKER);
    assertThat(actual.getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(actual.getSupportedBluetoothDevices()).containsExactly(device);
    assertThat(shadowInCallService.getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(shadowInCallService.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  public void setCallAudioState_null_throwsNullPointerException() {
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    assertThrows(NullPointerException.class, () -> shadowInCallService.setCallAudioState(null));
  }

  @Test
  @Config(maxSdk = O_MR1)
  public void setCallAudioState_sdkLtP() {
    CallAudioState callAudioState =
        new CallAudioState(
            /* muted= */ true,
            CallAudioState.ROUTE_EARPIECE,
            CallAudioState.ROUTE_EARPIECE | CallAudioState.ROUTE_SPEAKER);
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setCallAudioState(callAudioState);

    CallAudioState actual = inCallService.getCallAudioState();
    assertThat(actual.isMuted()).isTrue();
    assertThat(actual.getRoute()).isEqualTo(CallAudioState.ROUTE_EARPIECE);
    assertThat(actual.getSupportedRouteMask())
        .isEqualTo(CallAudioState.ROUTE_EARPIECE | CallAudioState.ROUTE_SPEAKER);
  }

  @Test
  @Config(maxSdk = UPSIDE_DOWN_CAKE)
  public void setCallAudioState_withInCallAdapterSet_updatesCallAudioState() {
    Phone phone = reflector(ReflectorInCallService.class, inCallService).getPhone();
    InCallAdapter inCallAdapter = reflector(ReflectorPhone.class, phone).getInCallAdapter();
    ReflectionHelpers.setField(
        inCallAdapter, "mAdapter", ReflectionHelpers.createNullProxy(IInCallAdapter.class));
    CallAudioState callAudioState =
        new CallAudioState(
            /* muted= */ true, CallAudioState.ROUTE_SPEAKER, CallAudioState.ROUTE_SPEAKER);
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setCallAudioState(callAudioState);

    assertThat(inCallService.getCallAudioState()).isEqualTo(callAudioState);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P, maxSdk = UPSIDE_DOWN_CAKE)
  public void setActiveBluetoothDevice_withInCallAdapterSet_updatesCallAudioState() {
    Phone phone = reflector(ReflectorInCallService.class, inCallService).getPhone();
    InCallAdapter inCallAdapter = reflector(ReflectorPhone.class, phone).getInCallAdapter();
    ReflectionHelpers.setField(
        inCallAdapter, "mAdapter", ReflectionHelpers.createNullProxy(IInCallAdapter.class));
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setActiveBluetoothDevice(device);

    assertThat(inCallService.getCallAudioState().getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(inCallService.getCallAudioState().getRoute())
        .isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P, maxSdk = UPSIDE_DOWN_CAKE)
  public void setSupportedBluetoothDevices_withInCallAdapterSet_updatesCallAudioState() {
    Phone phone = reflector(ReflectorInCallService.class, inCallService).getPhone();
    InCallAdapter inCallAdapter = reflector(ReflectorPhone.class, phone).getInCallAdapter();
    ReflectionHelpers.setField(
        inCallAdapter, "mAdapter", ReflectionHelpers.createNullProxy(IInCallAdapter.class));
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setSupportedBluetoothDevices(Arrays.asList(device));

    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices())
        .containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void setActiveBluetoothDevice_null_resetsRouteToEarpiece() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setActiveBluetoothDevice(device);
    assertThat(inCallService.getCallAudioState().getRoute())
        .isEqualTo(CallAudioState.ROUTE_BLUETOOTH);

    shadowInCallService.setActiveBluetoothDevice(null);
    assertThat(inCallService.getCallAudioState().getActiveBluetoothDevice()).isNull();
    assertThat(inCallService.getCallAudioState().getRoute())
        .isEqualTo(CallAudioState.ROUTE_EARPIECE);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void clearSupportedBluetoothDevices_withActiveDevice_clearsActiveDeviceAndResetsRoute() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setActiveBluetoothDevice(device);
    assertThat(inCallService.getCallAudioState().getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(inCallService.getCallAudioState().getRoute())
        .isEqualTo(CallAudioState.ROUTE_BLUETOOTH);

    shadowInCallService.clearSupportedBluetoothDevices();
    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices()).isEmpty();
    assertThat(inCallService.getCallAudioState().getActiveBluetoothDevice()).isNull();
    assertThat(inCallService.getCallAudioState().getRoute())
        .isEqualTo(CallAudioState.ROUTE_EARPIECE);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void
      setSupportedBluetoothDevices_excludingActiveDevice_clearsActiveDeviceAndResetsRoute() {
    BluetoothDevice device1 = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    BluetoothDevice device2 = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:CC");
    ShadowInCallService shadowInCallService = shadowOf(inCallService);

    shadowInCallService.setActiveBluetoothDevice(device1);
    shadowInCallService.setSupportedBluetoothDevices(Arrays.asList(device2));

    assertThat(inCallService.getCallAudioState().getActiveBluetoothDevice()).isNull();
    assertThat(inCallService.getCallAudioState().getRoute())
        .isEqualTo(CallAudioState.ROUTE_EARPIECE);
    assertThat(inCallService.getCallAudioState().getSupportedBluetoothDevices())
        .containsExactly(device2);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void newCallAudioState_withDuplicatesAndNulls_filtersCorrectly() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    CallAudioState callAudioState =
        ShadowInCallService.newCallAudioState(
            /* muted= */ false,
            CallAudioState.ROUTE_BLUETOOTH,
            CallAudioState.ROUTE_BLUETOOTH,
            device,
            Arrays.asList(device, null, device));

    assertThat(callAudioState.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void newCallAudioState_sdkAtLeastP() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    CallAudioState callAudioState =
        ShadowInCallService.newCallAudioState(
            /* muted= */ false,
            CallAudioState.ROUTE_BLUETOOTH,
            CallAudioState.ROUTE_BLUETOOTH,
            device,
            Arrays.asList(device));

    assertThat(callAudioState.isMuted()).isFalse();
    assertThat(callAudioState.getRoute()).isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
    assertThat(callAudioState.getSupportedRouteMask()).isEqualTo(CallAudioState.ROUTE_BLUETOOTH);
    assertThat(callAudioState.getActiveBluetoothDevice()).isEqualTo(device);
    assertThat(callAudioState.getSupportedBluetoothDevices()).containsExactly(device);
  }

  @Test
  @TargetApi(P)
  @Config(minSdk = P)
  public void newCallAudioState_nullSupportedBluetoothDevices_createsEmptyCollection() {
    BluetoothDevice device = ShadowBluetoothDevice.newInstance("00:11:22:33:AA:BB");
    CallAudioState callAudioState =
        ShadowInCallService.newCallAudioState(
            /* muted= */ false,
            CallAudioState.ROUTE_BLUETOOTH,
            CallAudioState.ROUTE_BLUETOOTH,
            device,
            /* supportedBluetoothDevices= */ null);

    assertThat(callAudioState.getSupportedBluetoothDevices()).isEmpty();
  }

  @Test
  @Config(maxSdk = O_MR1)
  public void newCallAudioState_sdkLtP() {
    CallAudioState callAudioState =
        ShadowInCallService.newCallAudioState(
            /* muted= */ false,
            CallAudioState.ROUTE_EARPIECE,
            CallAudioState.ROUTE_EARPIECE,
            /* activeBluetoothDevice= */ null,
            /* supportedBluetoothDevices= */ null);

    assertThat(callAudioState.isMuted()).isFalse();
    assertThat(callAudioState.getRoute()).isEqualTo(CallAudioState.ROUTE_EARPIECE);
    assertThat(callAudioState.getSupportedRouteMask()).isEqualTo(CallAudioState.ROUTE_EARPIECE);
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
