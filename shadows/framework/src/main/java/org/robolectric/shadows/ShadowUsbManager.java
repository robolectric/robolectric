package org.robolectric.shadows;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;
import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Robolectric implementation of {@link android.hardware.usb.UsbManager}. */
@Implements(value = UsbManager.class)
public class ShadowUsbManager {
  private HashMap<UsbDevice, Boolean> usbDevicesPermissionMap = new HashMap<>();

  /** Returns true if the caller has permission to access the device. */
  @Implementation
  public boolean hasPermission(UsbDevice device) {
    return usbDevicesPermissionMap.containsKey(device)
        ? usbDevicesPermissionMap.get(device)
        : false;
  }

  /**
   * Returns a HashMap containing all USB devices currently attached. USB device name is the key for
   * the returned HashMap. The result will be empty if no devices are attached, or if USB host mode
   * is inactive or unsupported.
   */
  @Implementation
  public HashMap<String, UsbDevice> getDeviceList() {
    HashMap<String, UsbDevice> usbDeviceMap = new HashMap<>();
    for (UsbDevice usbDevice : usbDevicesPermissionMap.keySet()) {
      usbDeviceMap.put(usbDevice.getDeviceName(), usbDevice);
    }
    return usbDeviceMap;
  }

  /** Clears all the data of {@link ShadowUsbManager}. */
  public void reset() {
    usbDevicesPermissionMap.clear();
  }

  /**
   * Adds a USB device into available USB devices map with permission value. If the USB device
   * already exists, updates the USB device with new permission value.
   */
  public void addOrUpdateUsbDevice(UsbDevice usbDevice, boolean hasPermission) {
    Preconditions.checkNotNull(usbDevice);
    Preconditions.checkNotNull(usbDevice.getDeviceName());
    usbDevicesPermissionMap.put(usbDevice, hasPermission);
  }

  /** Removes a USB device from available USB devices map. */
  public void removeUsbDevice(UsbDevice usbDevice) {
    Preconditions.checkNotNull(usbDevice);
    usbDevicesPermissionMap.remove(usbDevice);
  }

  /**
   * Opens a file descriptor from a temporary file.
   */
  @Implementation
  public ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
    try {
      File tmpUsbDir =
          RuntimeEnvironment.getTempDirectory().createIfNotExists("usb-accessory").toFile();
      return ParcelFileDescriptor.open(
          new File(tmpUsbDir, "usb-accessory-file"), ParcelFileDescriptor.MODE_READ_WRITE);
    } catch (FileNotFoundException error) {
      throw new RuntimeException("Error shadowing openAccessory", error);
    }
  }
}
