package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.telecom.CallAudioState;
import android.telecom.InCallAdapter;
import android.telecom.InCallService;
import android.telecom.ParcelableCall;
import android.telecom.Phone;
import com.android.internal.os.SomeArgs;
import com.android.internal.telecom.IInCallAdapter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.telecom.InCallService}. */
@Implements(value = InCallService.class, minSdk = M)
public class ShadowInCallService extends ShadowService {
  @RealObject private InCallService inCallService;
  private static final int MSG_ADD_CALL = 2;
  private static final int MSG_SET_POST_DIAL_WAIT = 4;
  private static final int MSG_ON_CONNECTION_EVENT = 9;

  private ShadowPhone shadowPhone;
  private boolean canAddCall;
  private boolean muted;
  private int audioRoute = CallAudioState.ROUTE_EARPIECE;
  private BluetoothDevice bluetoothDevice;
  private int supportedRouteMask;

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

  public void addCall(Call call) {
    shadowPhone.addCall(call);
  }

  public void addCall(ParcelableCall parcelableCall) {
    getHandler().obtainMessage(MSG_ADD_CALL, parcelableCall).sendToTarget();
  }

  /**
   * Exposes {@link IIInCallService.Stub#setPostDialWait}. This is normally invoked by Telecom but
   * in Robolectric, Telecom doesn't exist, so tests can invoke this to simulate Telecom's actions.
   */
  public void setPostDialWait(String callId, String remaining) {
    SomeArgs args = SomeArgs.obtain();
    args.arg1 = callId;
    args.arg2 = remaining;
    getHandler().obtainMessage(MSG_SET_POST_DIAL_WAIT, args).sendToTarget();
  }

  /**
   * Exposes {@link IIInCallService.Stub#onConnectionEvent}. This is normally invoked by Telecom but
   * in Robolectric, Telecom doesn't exist, so tests can invoke this to simulate Telecom's actions.
   */
  public void onConnectionEvent(String callId, String event, Bundle extras) {
    SomeArgs args = SomeArgs.obtain();
    args.arg1 = callId;
    args.arg2 = event;
    args.arg3 = extras;
    getHandler().obtainMessage(MSG_ON_CONNECTION_EVENT, args).sendToTarget();
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
    if (isInCallAdapterSet()) {
      reflector(ReflectorInCallService.class, inCallService).setMuted(muted);
    }
  }

  @Implementation
  protected void setAudioRoute(int audioRoute) {
    this.audioRoute = audioRoute;
    if (isInCallAdapterSet()) {
      reflector(ReflectorInCallService.class, inCallService).setAudioRoute(audioRoute);
    }
  }

  @Implementation
  protected CallAudioState getCallAudioState() {
    if (isInCallAdapterSet()) {
      return reflector(ReflectorInCallService.class, inCallService).getCallAudioState();
    }
    return new CallAudioState(muted, audioRoute, supportedRouteMask);
  }

  public void setSupportedRouteMask(int mask) {
    this.supportedRouteMask = mask;
  }

  @Implementation(minSdk = P)
  protected void requestBluetoothAudio(BluetoothDevice bluetoothDevice) {
    this.bluetoothDevice = bluetoothDevice;
    if (isInCallAdapterSet()) {
      reflector(ReflectorInCallService.class, inCallService).requestBluetoothAudio(bluetoothDevice);
    }
  }

  /** @return the last value provided to {@code requestBluetoothAudio()}. */
  @TargetApi(P)
  public BluetoothDevice getBluetoothAudio() {
    return bluetoothDevice;
  }

  private Handler getHandler() {
    return reflector(ReflectorInCallService.class, inCallService).getHandler();
  }

  /**
   * Checks if the InCallService was bound using {@link
   * com.android.internal.telecom.IInCallService#setInCallAdapter(IInCallAdapter)}.
   *
   * <p>If it was bound using this interface, the internal InCallAdapter will be set and it will
   * forward invocations to FakeTelecomServer.
   *
   * <p>Otherwise, invoking these methods will yield NullPointerExceptions, so we will avoid
   * forwarding the calls to the real objects.
   */
  private boolean isInCallAdapterSet() {
    Phone phone = reflector(ReflectorInCallService.class, inCallService).getPhone();
    InCallAdapter inCallAdapter = reflector(ReflectorPhone.class, phone).getInCallAdapter();
    Object internalAdapter =
        reflector(ReflectorInCallAdapter.class, inCallAdapter).getInternalInCallAdapter();
    return internalAdapter != null;
  }

  @ForType(InCallService.class)
  interface ReflectorInCallService {
    @Accessor("mHandler")
    Handler getHandler();

    @Accessor("mPhone")
    Phone getPhone();

    @Direct
    void requestBluetoothAudio(BluetoothDevice bluetoothDevice);

    @Direct
    void setAudioRoute(int audioRoute);

    @Direct
    void setMuted(boolean muted);

    @Direct
    CallAudioState getCallAudioState();
  }

  @ForType(Phone.class)
  interface ReflectorPhone {
    @Accessor("mInCallAdapter")
    InCallAdapter getInCallAdapter();
  }

  @ForType(InCallAdapter.class)
  interface ReflectorInCallAdapter {
    @Accessor("mAdapter")
    Object getInternalInCallAdapter();
  }
}
