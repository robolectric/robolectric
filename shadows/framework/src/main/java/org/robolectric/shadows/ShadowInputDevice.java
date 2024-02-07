package org.robolectric.shadows;

import android.view.InputDevice;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(InputDevice.class)
public class ShadowInputDevice {
  private String deviceName;
  private int productId;
  private int vendorId;

  public static InputDevice makeInputDeviceNamed(String deviceName) {
    InputDevice inputDevice = Shadow.newInstanceOf(InputDevice.class);
    ShadowInputDevice shadowInputDevice = Shadow.extract(inputDevice);
    shadowInputDevice.setDeviceName(deviceName);
    return inputDevice;
  }

  @Implementation
  protected String getName() {
    return deviceName;
  }

  @Implementation
  protected int getProductId() {
    return productId;
  }

  @Implementation
  protected int getVendorId() {
    return vendorId;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public void setProductId(int productId) {
    this.productId = productId;
  }

  public void setVendorId(int vendorId) {
    this.vendorId = vendorId;
  }
}
