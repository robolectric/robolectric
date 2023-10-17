package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.hardware.input.InputManagerGlobal;
import android.util.SparseArray;
import android.view.InputDevice;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow for new InputManagerGlobal introduced in android U. */
@Implements(value = InputManagerGlobal.class, isInAndroidSdk = false, minSdk = U.SDK_INT)
public class ShadowInputManagerGlobal {

  @RealObject InputManagerGlobal realInputManager;

  /** Used in {@link InputDevice#getDeviceIds()} */
  @Implementation
  protected int[] getInputDeviceIds() {
    return new int[0];
  }

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

  @ForType(InputManagerGlobal.class)
  interface InputManagerReflector {
    @Accessor("mInputDevices")
    SparseArray<InputDevice> getInputDevices();

    @Accessor("mInputDevices")
    void setInputDevices(SparseArray<InputDevice> devices);
  }
}
