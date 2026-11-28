package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.input.InputManagerGlobal;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for new InputManagerGlobal introduced in android U. */
@Implements(value = InputManagerGlobal.class, isInAndroidSdk = false, minSdk = UPSIDE_DOWN_CAKE)
public class ShadowInputManagerGlobal {

  @RealObject InputManagerGlobal realInputManager;

  @Implementation
  protected void populateInputDevicesLocked() throws ClassNotFoundException {
    if (ReflectionHelpers.getField(realInputManager, "mInputDevicesChangedListener") == null) {
      ReflectionHelpers.setField(
          realInputManager,
          "mInputDevicesChangedListener",
          ReflectionHelpers.callConstructor(
              Class.forName(
                  "android.hardware.input.InputManagerGlobal$InputDevicesChangedListener")));
    }
    SparseArray<InputDevice> inputDevices = getInputDevices();
    if (inputDevices == null) {
      setInputDevices(new SparseArray<>());
    }
  }

  SparseArray<InputDevice> getInputDevices() {
    return reflector(InputManagerReflector.class, realInputManager).getInputDevices();
  }

  private void setInputDevices(SparseArray<InputDevice> devices) {
    reflector(InputManagerReflector.class, realInputManager).setInputDevices(devices);
  }

  /**
   * Dispatches the event to the window it targets on the main thread, as {@link
   * android.app.UiAutomation#injectInputEvent} does under Robolectric, waiting for the dispatch
   * whatever the mode. There is no input service to send it to.
   */
  @Implementation
  protected boolean injectInputEvent(InputEvent event, int mode) {
    return ShadowUiAutomation.injectInputEvent(event);
  }

  /** As {@link #injectInputEvent(InputEvent, int)}; the target UID is not checked. */
  @Implementation
  protected boolean injectInputEvent(InputEvent event, int mode, int targetUid) {
    return ShadowUiAutomation.injectInputEvent(event);
  }

  @Implementation
  protected boolean[] deviceHasKeys(int deviceId, int[] keyCodes) {
    return ShadowInputManager.deviceHasKeysImpl(deviceId, keyCodes);
  }

  @ForType(InputManagerGlobal.class)
  interface InputManagerReflector {
    @Accessor("mInputDevices")
    SparseArray<InputDevice> getInputDevices();

    @Accessor("mInputDevices")
    void setInputDevices(SparseArray<InputDevice> devices);
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(InputManagerGlobal.class, "sInstance", null);
  }
}
