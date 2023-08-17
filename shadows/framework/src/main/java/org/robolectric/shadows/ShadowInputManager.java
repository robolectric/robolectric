package org.robolectric.shadows;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.input.InputManager;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VerifiedKeyEvent;
import android.view.VerifiedMotionEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for {@link InputManager} */
@Implements(value = InputManager.class, looseSignatures = true)
public class ShadowInputManager {

  @RealObject InputManager realInputManager;

  @Implementation
  protected boolean injectInputEvent(InputEvent event, int mode) {
    // ignore
    return true;
  }

  @Implementation(minSdk = KITKAT)
  protected boolean[] deviceHasKeys(int id, int[] keyCodes) {
    return new boolean[keyCodes.length];
  }

  /** Used in {@link InputDevice#getDeviceIds()} */
  @Implementation
  protected int[] getInputDeviceIds() {
    return new int[0];
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
      final int[] ids = realInputManager.getInputDeviceIds();

      SparseArray<InputDevice> inputDevices = new SparseArray<>();
      for (int i = 0; i < ids.length; i++) {
        inputDevices.put(ids[i], null);
      }
      setInputDevices(inputDevices);
    }
  }

  private SparseArray<InputDevice> getInputDevices() {
    return reflector(InputManagerReflector.class, realInputManager).getInputDevices();
  }

  private void setInputDevices(SparseArray<InputDevice> devices) {
    reflector(InputManagerReflector.class, realInputManager).setInputDevices(devices);
  }

  /**
   * Provides a local java implementation, since the real implementation is in system server +
   * native code.
   */
  @Implementation(minSdk = R)
  protected Object verifyInputEvent(Object inputEvent) {
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
  }

  @ForType(InputManager.class)
  interface InputManagerReflector {
    @Accessor("mInputDevices")
    SparseArray<InputDevice> getInputDevices();

    @Accessor("mInputDevices")
    void setInputDevices(SparseArray<InputDevice> devices);
  }
}
