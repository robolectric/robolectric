package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link android.telecom.InCallService}. */
@Implements(InCallService.class)
public class ShadowInCallService extends ShadowService {
  @RealObject private InCallService inCallService;
  private static final int MSG_ADD_CALL = 2;
  private static final int MSG_SET_POST_DIAL_WAIT = 4;
  private static final int MSG_ON_CONNECTION_EVENT = 9;

  private boolean canAddCall;
  private boolean muted;
  private int audioRoute = CallAudioState.ROUTE_EARPIECE;
  private BluetoothDevice bluetoothDevice;
  private int supportedRouteMask;
  private final List<BluetoothDevice> supportedBluetoothDevices = new ArrayList<>();

  /* Starting in Android V, the InCallService does not allow setting an InCallAdapter if Phone
   * was already set. This is how the InCallService should be instantiated in tests:
   * ```
   * InCallServiceController serviceController =
   *   Robolectric.buildService(InCallServiceImpl.class, intent).create();
   * IInCallService.Stub inCallServiceBinder =
   *   (IInCallService.Stub) serviceController.get().onBind(intent);
   * inCallServiceBinder.setInCallAdapter(new InCallAdapterImpl());
   * ```
   * Do not rely on reflection for this and use the public APIs instead. */
  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
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
    ReflectionHelpers.setField(inCallService, "mPhone", phone);
    reflector(ReflectorPhone.class, phone).setCallAudioState(createCallAudioState());
    invokeConstructor(InCallService.class, inCallService);
  }

  /**
   * @deprecated Please add calls by adding a Call using {@link
   *     android.telecom.InCallService.InCallServiceBinder}.
   */
  @Deprecated
  public void addCall(Call call) {
    getShadowPhone().addCall(call);
  }

  public void addCall(ParcelableCall parcelableCall) {
    getHandler().obtainMessage(MSG_ADD_CALL, parcelableCall).sendToTarget();
  }

  /**
   * Exposes {@link IIInCallService.Stub#setPostDialWait}. This is normally invoked by Telecom but
   * in Robolectric, Telecom doesn't exist, so tests can invoke this to simulate Telecom's actions.
   */
  public void setPostDialWait(String callId, String remaining) {
    Object args;
    // Due to repackaging of SomeArgs in the Telecom stack in higher SDKs (SDK 37), we need to use
    // com.android.internal.telecom.jarjar.com.android.internal.os.SomeArgs in 37+ and use the
    // standard SomeArgs in older SDKs.
    if (VERSION.SDK_INT >= 37) {
      args = reflector(ReflectorJarJarSomeArgs.class).obtain();
      ReflectorJarJarSomeArgs reflector = reflector(ReflectorJarJarSomeArgs.class, args);
      reflector.setArg1(callId);
      reflector.setArg2(remaining);
    } else {
      SomeArgs standardSomeArgs = SomeArgs.obtain();
      standardSomeArgs.arg1 = callId;
      standardSomeArgs.arg2 = remaining;
      args = standardSomeArgs;
    }
    getHandler().obtainMessage(MSG_SET_POST_DIAL_WAIT, args).sendToTarget();
  }

  /**
   * Exposes {@link IIInCallService.Stub#onConnectionEvent}. This is normally invoked by Telecom but
   * in Robolectric, Telecom doesn't exist, so tests can invoke this to simulate Telecom's actions.
   */
  public void onConnectionEvent(String callId, String event, Bundle extras) {
    Object args;
    // Due to repackaging of SomeArgs in the Telecom stack in higher SDKs (SDK 37), we need to use
    // com.android.internal.telecom.jarjar.com.android.internal.os.SomeArgs in 37+ and use the
    // standard SomeArgs in older SDKs.
    if (VERSION.SDK_INT >= 37) {
      args = reflector(ReflectorJarJarSomeArgs.class).obtain();
      ReflectorJarJarSomeArgs reflector = reflector(ReflectorJarJarSomeArgs.class, args);
      reflector.setArg1(callId);
      reflector.setArg2(event);
      reflector.setArg3(extras);
    } else {
      SomeArgs standardSomeArgs = SomeArgs.obtain();
      standardSomeArgs.arg1 = callId;
      standardSomeArgs.arg2 = event;
      standardSomeArgs.arg3 = extras;
      args = standardSomeArgs;
    }
    getHandler().obtainMessage(MSG_ON_CONNECTION_EVENT, args).sendToTarget();
  }

  /**
   * @deprecated Please remove calls by invoking {@link Call#disconnect()}.
   */
  @Deprecated
  public void removeCall(Call call) {
    getShadowPhone().removeCall(call);
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
    updatePhoneCallAudioState();
    if (isInCallAdapterSet()) {
      reflector(ReflectorInCallService.class, inCallService).setMuted(muted);
    }
  }

  @Implementation
  protected void setAudioRoute(int audioRoute) {
    this.audioRoute = audioRoute;
    updatePhoneCallAudioState();
    if (isInCallAdapterSet()) {
      reflector(ReflectorInCallService.class, inCallService).setAudioRoute(audioRoute);
    }
  }

  @Implementation
  protected CallAudioState getCallAudioState() {
    if (isInCallAdapterSet()) {
      return reflector(ReflectorInCallService.class, inCallService).getCallAudioState();
    }
    return createCallAudioState();
  }

  public void setSupportedRouteMask(int mask) {
    this.supportedRouteMask = mask;
    updatePhoneCallAudioState();
  }

  /** Sets the {@link CallAudioState} returned by {@link InCallService#getCallAudioState()}. */
  public void setCallAudioState(CallAudioState callAudioState) {
    Objects.requireNonNull(callAudioState, "callAudioState must not be null");
    this.muted = callAudioState.isMuted();
    this.audioRoute = callAudioState.getRoute();
    this.supportedRouteMask = callAudioState.getSupportedRouteMask();
    if (VERSION.SDK_INT >= P) {
      this.bluetoothDevice = callAudioState.getActiveBluetoothDevice();
      this.supportedBluetoothDevices.clear();
      if (callAudioState.getSupportedBluetoothDevices() != null) {
        for (BluetoothDevice device : callAudioState.getSupportedBluetoothDevices()) {
          if (device != null && !this.supportedBluetoothDevices.contains(device)) {
            this.supportedBluetoothDevices.add(device);
          }
        }
      }
    } else {
      this.bluetoothDevice = null;
      this.supportedBluetoothDevices.clear();
    }
    updatePhoneCallAudioState();
  }

  /** Sets the supported Bluetooth devices returned by {@link InCallService#getCallAudioState()}. */
  @RequiresApi(P)
  public void setSupportedBluetoothDevices(Collection<BluetoothDevice> supportedBluetoothDevices) {
    this.supportedBluetoothDevices.clear();
    if (supportedBluetoothDevices != null) {
      for (BluetoothDevice device : supportedBluetoothDevices) {
        if (device != null && !this.supportedBluetoothDevices.contains(device)) {
          this.supportedBluetoothDevices.add(device);
        }
      }
    }
    if (this.bluetoothDevice != null
        && !this.supportedBluetoothDevices.contains(this.bluetoothDevice)) {
      this.bluetoothDevice = null;
      if (this.audioRoute == CallAudioState.ROUTE_BLUETOOTH) {
        this.audioRoute = CallAudioState.ROUTE_EARPIECE;
      }
    }
    updatePhoneCallAudioState();
  }

  /** Adds a supported Bluetooth device returned by {@link InCallService#getCallAudioState()}. */
  @RequiresApi(P)
  public void addSupportedBluetoothDevice(BluetoothDevice bluetoothDevice) {
    if (bluetoothDevice != null && !this.supportedBluetoothDevices.contains(bluetoothDevice)) {
      this.supportedBluetoothDevices.add(bluetoothDevice);
      updatePhoneCallAudioState();
    }
  }

  /**
   * Clears the supported Bluetooth devices returned by {@link InCallService#getCallAudioState()}.
   */
  @RequiresApi(P)
  public void clearSupportedBluetoothDevices() {
    this.supportedBluetoothDevices.clear();
    this.bluetoothDevice = null;
    if (this.audioRoute == CallAudioState.ROUTE_BLUETOOTH) {
      this.audioRoute = CallAudioState.ROUTE_EARPIECE;
    }
    updatePhoneCallAudioState();
  }

  /** Returns the supported Bluetooth devices. */
  @RequiresApi(P)
  public List<BluetoothDevice> getSupportedBluetoothDevices() {
    return Collections.unmodifiableList(supportedBluetoothDevices);
  }

  /** Sets the active Bluetooth device returned by {@link InCallService#getCallAudioState()}. */
  @RequiresApi(P)
  public void setActiveBluetoothDevice(BluetoothDevice bluetoothDevice) {
    this.bluetoothDevice = bluetoothDevice;
    if (bluetoothDevice != null) {
      this.audioRoute = CallAudioState.ROUTE_BLUETOOTH;
      if (!this.supportedBluetoothDevices.contains(bluetoothDevice)) {
        this.supportedBluetoothDevices.add(bluetoothDevice);
      }
    } else {
      if (this.audioRoute == CallAudioState.ROUTE_BLUETOOTH) {
        this.audioRoute = CallAudioState.ROUTE_EARPIECE;
      }
    }
    updatePhoneCallAudioState();
  }

  /**
   * @return the active Bluetooth device in {@link CallAudioState}, or the last value provided to
   *     {@code requestBluetoothAudio()}.
   */
  @RequiresApi(P)
  public BluetoothDevice getActiveBluetoothDevice() {
    return bluetoothDevice;
  }

  @Implementation(minSdk = P)
  protected void requestBluetoothAudio(BluetoothDevice bluetoothDevice) {
    this.bluetoothDevice = bluetoothDevice;
    if (bluetoothDevice != null) {
      this.audioRoute = CallAudioState.ROUTE_BLUETOOTH;
      if (!this.supportedBluetoothDevices.contains(bluetoothDevice)) {
        this.supportedBluetoothDevices.add(bluetoothDevice);
      }
    }
    updatePhoneCallAudioState();
    if (isInCallAdapterSet()) {
      reflector(ReflectorInCallService.class, inCallService).requestBluetoothAudio(bluetoothDevice);
    }
  }

  /**
   * @return the last value provided to {@code requestBluetoothAudio()}.
   */
  @RequiresApi(P)
  public BluetoothDevice getBluetoothAudio() {
    return bluetoothDevice;
  }

  /**
   * Creates a new {@link CallAudioState}. The real 5-argument constructor of {@link CallAudioState}
   * is hidden.
   */
  public static CallAudioState newCallAudioState(
      boolean muted,
      int route,
      int supportedRouteMask,
      BluetoothDevice activeBluetoothDevice,
      Collection<BluetoothDevice> supportedBluetoothDevices) {
    if (VERSION.SDK_INT >= P) {
      List<BluetoothDevice> devices = new ArrayList<>();
      if (supportedBluetoothDevices != null) {
        for (BluetoothDevice device : supportedBluetoothDevices) {
          if (device != null && !devices.contains(device)) {
            devices.add(device);
          }
        }
      }
      return ReflectionHelpers.callConstructor(
          CallAudioState.class,
          ClassParameter.from(boolean.class, muted),
          ClassParameter.from(int.class, route),
          ClassParameter.from(int.class, supportedRouteMask),
          ClassParameter.from(BluetoothDevice.class, activeBluetoothDevice),
          ClassParameter.from(Collection.class, devices));
    }
    return new CallAudioState(muted, route, supportedRouteMask);
  }

  private CallAudioState createCallAudioState() {
    return newCallAudioState(
        muted, audioRoute, supportedRouteMask, bluetoothDevice, supportedBluetoothDevices);
  }

  private void updatePhoneCallAudioState() {
    Phone phone = reflector(ReflectorInCallService.class, inCallService).getPhone();
    if (phone != null) {
      reflector(ReflectorPhone.class, phone).setCallAudioState(createCallAudioState());
    }
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
    if (phone == null) {
      return false;
    }
    InCallAdapter inCallAdapter = reflector(ReflectorPhone.class, phone).getInCallAdapter();
    Object internalAdapter =
        reflector(ReflectorInCallAdapter.class, inCallAdapter).getInternalInCallAdapter();
    return internalAdapter != null;
  }

  private ShadowPhone getShadowPhone() {
    if (reflector(ReflectorInCallService.class, inCallService).getPhone() == null) {
      setPhone();
    }
    Phone phone = reflector(ReflectorInCallService.class, inCallService).getPhone();
    return Shadow.extract(phone);
  }

  private void setPhone() {
    InCallAdapter adapter = Shadow.newInstanceOf(InCallAdapter.class);
    Phone phone;
    if (VERSION.SDK_INT > N_MR1) {
      phone = reflector(ReflectorPhone.class).newInstance(adapter, "", 0);
    } else {
      phone = reflector(ReflectorPhone.class).newInstance(adapter);
    }
    ReflectionHelpers.setField(inCallService, "mPhone", phone);
    reflector(ReflectorPhone.class, phone).setCallAudioState(createCallAudioState());
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

    @Accessor("mCallAudioState")
    void setCallAudioState(CallAudioState callAudioState);

    @Constructor
    Phone newInstance(InCallAdapter inCallAdapter, String name, int type);

    @Constructor
    Phone newInstance(InCallAdapter inCallAdapter);
  }

  @ForType(InCallAdapter.class)
  interface ReflectorInCallAdapter {
    @Accessor("mAdapter")
    Object getInternalInCallAdapter();
  }

  @ForType(className = "com.android.internal.telecom.jarjar.com.android.internal.os.SomeArgs")
  interface ReflectorJarJarSomeArgs {
    @Static
    Object obtain();

    @Accessor("arg1")
    void setArg1(Object arg1);

    @Accessor("arg2")
    void setArg2(Object arg2);

    @Accessor("arg3")
    void setArg3(Object arg3);
  }
}
