package org.robolectric.shadows;

import android.view.InputDevice;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * @deprecated use {@link InputDeviceBuilder}.
 */
@Implements(InputDevice.class)
@Deprecated
public class ShadowInputDevice {
  @ReflectorObject private InputDeviceReflector inputDeviceReflector;

  /**
   * @deprecated use {@link InputDeviceBuilder}.
   */
  @Deprecated
  public static InputDevice makeInputDeviceNamed(String deviceName) {
    if (RuntimeEnvironment.getApiLevel() >= U.SDK_INT) {
      InputDevice.Builder builder = new InputDevice.Builder();
      builder.setName(deviceName);
      return builder.build();
    } else {
      InputDevice inputDevice = Shadow.newInstanceOf(InputDevice.class);
      ShadowInputDevice shadowInputDevice = Shadow.extract(inputDevice);
      shadowInputDevice.setDeviceName(deviceName);
      return inputDevice;
    }
  }

  /**
   * @deprecated use {@link InputDeviceBuilder}.
   */
  @Deprecated
  public void setDeviceName(String deviceName) {
    inputDeviceReflector.setName(deviceName);
  }

  /**
   * @deprecated use {@link InputDeviceBuilder}.
   */
  @Deprecated
  public void setProductId(int productId) {
    inputDeviceReflector.setProductId(productId);
  }

  /**
   * @deprecated use {@link InputDeviceBuilder}.
   */
  @Deprecated
  public void setVendorId(int vendorId) {
    inputDeviceReflector.setVendorId(vendorId);
  }

  @ForType(InputDevice.class)
  private interface InputDeviceReflector {
    @Accessor("mName")
    void setName(String name);

    @Accessor("mProductId")
    void setProductId(int productId);

    @Accessor("mVendorId")
    void setVendorId(int vendorId);
  }
}
