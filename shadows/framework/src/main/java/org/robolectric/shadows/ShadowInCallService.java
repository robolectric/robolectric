package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build.VERSION;
import android.os.Handler;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallAdapter;
import android.telecom.InCallService;
import android.telecom.ParcelableCall;
import android.telecom.Phone;
import com.android.internal.telecom.IInCallAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.telecom.InCallService}. */
@Implements(value = InCallService.class, minSdk = M)
public class ShadowInCallService extends ShadowService {
  @RealObject private InCallService inCallService;
  private static final int MSG_SET_IN_CALL_ADAPTER = 1;
  private static final int MSG_ADD_CALL = 2;
  private static final int MSG_UPDATE_CALL = 3;

  private ShadowPhone shadowPhone;
  private boolean canAddCall;
  private boolean muted;
  private int audioRoute = CallAudioState.ROUTE_EARPIECE;
  private BluetoothDevice bluetoothDevice;

  @Implementation
  protected void __constructor__() {
    InCallAdapter adapter = Shadow.newInstanceOf(InCallAdapter.class);
    Phone phone;
    if (VERSION.SDK_INT > N_MR1) {
      phone =
          ReflectionHelpers.callConstructor(
              Phone.class,
              ClassParameter.from(InCallAdapter.class, adapter),
              ClassParameter.from(String.class, ""),
              ClassParameter.from(int.class, 0));
    } else {
      phone =
          ReflectionHelpers.callConstructor(
              Phone.class, ClassParameter.from(InCallAdapter.class, adapter));
    }
    shadowPhone = Shadow.extract(phone);
    ReflectionHelpers.setField(inCallService, "mPhone", phone);
    invokeConstructor(InCallService.class, inCallService);
  }

  public void updateCall(ParcelableCall parcelableCall) {
    getHandler().obtainMessage(MSG_UPDATE_CALL, parcelableCall).sendToTarget();
  }

  public void setInCallAdapter(IInCallAdapter inCallAdapter) {
    getHandler().obtainMessage(MSG_SET_IN_CALL_ADAPTER, inCallAdapter).sendToTarget();
  }

  public void addCall(Call call) {
    shadowPhone.addCall(call);
  }

  public void addCall(ParcelableCall parcelableCall) {
    getHandler().obtainMessage(MSG_ADD_CALL, parcelableCall).sendToTarget();
  }

  public void removeCall(Call call) {
    shadowPhone.removeCall(call);
  }

  @Implementation
  protected boolean canAddCall() {
    return canAddCall;
  }

  /** Set the value that {@code canAddCall()} method should return. */
  public void setCanAddCall(boolean canAddCall) {
    this.canAddCall = canAddCall;
  }

  @Implementation
  protected void setMuted(boolean muted) {
    this.muted = muted;
  }

  @Implementation
  protected void setAudioRoute(int audioRoute) {
    this.audioRoute = audioRoute;
  }

  @Implementation
  protected CallAudioState getCallAudioState() {
    return new CallAudioState(
        muted,
        audioRoute,
        CallAudioState.ROUTE_EARPIECE
            | CallAudioState.ROUTE_BLUETOOTH
            | CallAudioState.ROUTE_WIRED_HEADSET
            | CallAudioState.ROUTE_SPEAKER);
  }

  @Implementation(minSdk = P)
  protected void requestBluetoothAudio(BluetoothDevice bluetoothDevice) {
    this.bluetoothDevice = bluetoothDevice;
  }

  /** @return the last value provided to {@code requestBluetoothAudio()}. */
  @TargetApi(P)
  public BluetoothDevice getBluetoothAudio() {
    return bluetoothDevice;
  }

  private Handler getHandler() {
    return reflector(ReflectorInCallService.class, inCallService).getHandler();
  }

  @ForType(InCallService.class)
  interface ReflectorInCallService {
    @Accessor("mHandler")
    Handler getHandler();
  }
}
