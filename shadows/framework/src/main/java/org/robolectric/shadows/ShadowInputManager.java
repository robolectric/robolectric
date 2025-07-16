package org.robolectric.shadows;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.input.InputManager;
import android.hardware.input.InputManagerGlobal;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VerifiedKeyEvent;
import android.view.VerifiedMotionEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link InputManager} */
@Implements(InputManager.class)
public class ShadowInputManager {

  @RealObject InputManager realInputManager;

  private static final SetMultimap<Integer, Integer> deviceKeys = HashMultimap.create();

  protected static boolean[] deviceHasKeysImpl(int deviceId, int[] keyCodes) {
    boolean[] result = new boolean[keyCodes.length];
    for (int i = 0; i < keyCodes.length; i++) {
      result[i] = deviceKeys.containsEntry(deviceId, keyCodes[i]);
    }
    return result;
  }

  /** On U and above, this method delegates to {@link android.hardware.input.InputManagerGlobal}. */
  @Implementation(maxSdk = TIRAMISU)
  protected boolean injectInputEvent(InputEvent event, int mode) {
    // ignore
    return true;
  }

  /** On U and above, this method delegates to {@link android.hardware.input.InputManagerGlobal}. */
  @Implementation(maxSdk = TIRAMISU)
  protected boolean[] deviceHasKeys(int id, int[] keyCodes) {
    return deviceHasKeysImpl(id, keyCodes);
  }

  @Implementation(maxSdk = TIRAMISU)
  protected void populateInputDevicesLocked() throws ClassNotFoundException {
    if (ReflectionHelpers.getField(realInputManager, "mInputDevicesChangedListener") == null) {
      ReflectionHelpers.setField(
          realInputManager,
          "mInputDevicesChangedListener",
          ReflectionHelpers.callConstructor(
              Class.forName("android.hardware.input.InputManager$InputDevicesChangedListener")));
    }
    if (getInputDevices() == null) {
      setInputDevices(new SparseArray<>());
    }
  }

  private SparseArray<InputDevice> getInputDevices() {
    return reflector(InputManagerReflector.class, realInputManager).getInputDevices();
  }

  private void setInputDevices(SparseArray<InputDevice> devices) {
    reflector(InputManagerReflector.class, realInputManager).setInputDevices(devices);
  }

  void addDeviceKeys(int deviceId, int[] keyCodes) {
    for (int keyCode : keyCodes) {
      deviceKeys.put(deviceId, keyCode);
    }
  }

  public void addInputDevice(InputDevice inputDevice) {
    if (realInputManager.getInputDevice(inputDevice.getId()) == null) {
      // Add the input device to the list of input devices.
      SparseArray<InputDevice> inputDevices =
          RuntimeEnvironment.getApiLevel() < UPSIDE_DOWN_CAKE
              ? getInputDevices()
              : ((ShadowInputManagerGlobal) Shadow.extract(InputManagerGlobal.getInstance()))
                  .getInputDevices();

      inputDevices.put(inputDevice.getId(), inputDevice);
    }
  }

  public void addInputDeviceKeys(int deviceId, int[] keyCodes) {
    Preconditions.checkNotNull(keyCodes);
    Preconditions.checkArgument(
        realInputManager.getInputDevice(deviceId) != null,
        "Unknown InputDevice with id %s",
        deviceId);
    if (keyCodes != null) {
      addDeviceKeys(deviceId, keyCodes);
    }
  }

  /**
   * Provides a local java implementation, since the real implementation is in system server +
   * native code.
   */
  @Implementation(minSdk = R)
  protected @ClassName("android.view.VerifiedInputEvent") Object verifyInputEvent(
      InputEvent inputEvent) {
    if (inputEvent instanceof MotionEvent) {
      MotionEvent motionEvent = (MotionEvent) inputEvent;
      return new VerifiedMotionEvent(
          motionEvent.getDeviceId(),
          MILLISECONDS.toNanos(motionEvent.getEventTime()),
          motionEvent.getSource(),
          motionEvent.getDisplayId(),
          motionEvent.getRawX(),
          motionEvent.getRawY(),
          motionEvent.getActionMasked(),
          MILLISECONDS.toNanos(motionEvent.getDownTime()),
          motionEvent.getFlags(),
          motionEvent.getMetaState(),
          motionEvent.getButtonState());
    } else if (inputEvent instanceof KeyEvent) {
      KeyEvent keyEvent = (KeyEvent) inputEvent;
      return new VerifiedKeyEvent(
          keyEvent.getDeviceId(),
          MILLISECONDS.toNanos(keyEvent.getEventTime()),
          keyEvent.getSource(),
          keyEvent.getDisplayId(),
          keyEvent.getAction(),
          MILLISECONDS.toNanos(keyEvent.getDownTime()),
          keyEvent.getFlags(),
          keyEvent.getKeyCode(),
          keyEvent.getScanCode(),
          keyEvent.getMetaState(),
          keyEvent.getRepeatCount());
    } else {
      throw new IllegalArgumentException("unknown input event: " + inputEvent.getClass().getName());
    }
  }

  @Resetter
  public static void reset() {
    if (SDK_INT < U.SDK_INT) {
      ReflectionHelpers.setStaticField(InputManager.class, "sInstance", null);
    }
    deviceKeys.clear();
  }

  @ForType(InputManager.class)
  interface InputManagerReflector {
    @Accessor("mInputDevices")
    SparseArray<InputDevice> getInputDevices();

    @Accessor("mInputDevices")
    void setInputDevices(SparseArray<InputDevice> devices);
  }
}
