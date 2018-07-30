package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static org.robolectric.RuntimeEnvironment.application;

import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbPort;
import android.hardware.usb.UsbPortStatus;
import android.os.ParcelFileDescriptor;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Robolectric implementation of {@link android.hardware.usb.UsbManager}. */
@Implements(value = UsbManager.class, looseSignatures = true)
public class ShadowUsbManager {
  /**
   * A mapping from the package names to a list of USB devices for which permissions are granted.
   */
  private final HashMap<String, List<UsbDevice>> grantedPermissions = new HashMap<>();

  /**
   * A mapping from the USB device names to the USB device instances.
   *
   * @see UsbManager#getDeviceList()
   */
  private final HashMap<String, UsbDevice> usbDevices = new HashMap<>();

  /** A mapping from USB port to the status of that port. */
  private final HashMap<UsbPort, UsbPortStatus> usbPorts = new HashMap<>();

  private UsbAccessory attachedUsbAccessory = null;

  /** Returns true if the caller has permission to access the device. */
  @Implementation
  protected boolean hasPermission(UsbDevice device) {
    return hasPermissionForPackage(device, RuntimeEnvironment.application.getPackageName());
  }

  /** Returns true if the given package has permission to access the device. */
  public boolean hasPermissionForPackage(UsbDevice device, String packageName) {
    List<UsbDevice> usbDevices = grantedPermissions.get(packageName);
    return usbDevices != null && usbDevices.contains(device);
  }

  @Implementation(minSdk = N)
  @HiddenApi
  protected void grantPermission(UsbDevice device) {
    grantPermission(device, RuntimeEnvironment.application.getPackageName());
  }

  @Implementation(minSdk = N_MR1)
  @HiddenApi // SystemApi
  protected void grantPermission(UsbDevice device, String packageName) {
    List<UsbDevice> usbDevices = grantedPermissions.get(packageName);
    if (usbDevices == null) {
      usbDevices = new ArrayList<>();
      grantedPermissions.put(packageName, usbDevices);
    }
    usbDevices.add(device);
  }

  /**
   * Revokes permission to a USB device granted to a package. This method does nothing if the
   * package doesn't have permission to access the device.
   */
  public void revokePermission(UsbDevice device, String packageName) {
    List<UsbDevice> usbDevices = grantedPermissions.get(packageName);
    if (usbDevices != null) {
      usbDevices.remove(device);
    }
  }

  /**
   * Returns a HashMap containing all USB devices currently attached. USB device name is the key for
   * the returned HashMap. The result will be empty if no devices are attached, or if USB host mode
   * is inactive or unsupported.
   */
  @Implementation
  protected HashMap<String, UsbDevice> getDeviceList() {
    return new HashMap<>(usbDevices);
  }

  @Implementation
  protected UsbAccessory[] getAccessoryList() {
    // Currently Android only supports having a single accessory attached, and if nothing
    // is attached, this method actually returns null in the real implementation.
    if (attachedUsbAccessory == null) {
      return null;
    }

    return new UsbAccessory[] {attachedUsbAccessory};
  }

  /** Sets the currently attached Usb accessory returned in #getAccessoryList. */
  public void setAttachedUsbAccessory(UsbAccessory usbAccessory) {
    this.attachedUsbAccessory = usbAccessory;
  }

  /**
   * Adds a USB device into available USB devices map with permission value. If the USB device
   * already exists, updates the USB device with new permission value.
   */
  public void addOrUpdateUsbDevice(UsbDevice usbDevice, boolean hasPermission) {
    Preconditions.checkNotNull(usbDevice);
    Preconditions.checkNotNull(usbDevice.getDeviceName());
    usbDevices.put(usbDevice.getDeviceName(), usbDevice);
    if (hasPermission) {
      grantPermission(usbDevice);
    } else {
      revokePermission(usbDevice, RuntimeEnvironment.application.getPackageName());
    }
  }

  /** Removes a USB device from available USB devices map. */
  public void removeUsbDevice(UsbDevice usbDevice) {
    Preconditions.checkNotNull(usbDevice);
    usbDevices.remove(usbDevice.getDeviceName());
    revokePermission(usbDevice, RuntimeEnvironment.application.getPackageName());
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected /* UsbPort[] */ Object[] getPorts() {
    return usbPorts.keySet().toArray(new UsbPort[usbPorts.size()]);
  }

  /** Remove all added ports from UsbManager. */
  public void clearPorts() {
    usbPorts.clear();
  }

  /** Adds a USB port to UsbManager. */
  public void addPort(String portId) {
    usbPorts.put(
        new UsbPort(portId, UsbPort.MODE_DUAL),
        new UsbPortStatus(UsbPort.MODE_DUAL, UsbPort.POWER_ROLE_SINK, UsbPort.DATA_ROLE_DEVICE, 0));
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected /* UsbPortStatus */ Object getPortStatus(/* UsbPort */ Object port) {
    return usbPorts.get(port);
  }

  @Implementation(minSdk = M)
  @HiddenApi
  protected void setPortRoles(
      /* UsbPort */ Object port, /* int */ Object powerRole, /* int */ Object dataRole) {
    UsbPortStatus status = usbPorts.get(port);
    usbPorts.put(
        (UsbPort) port,
        new UsbPortStatus(
            status.getCurrentMode(),
            (int) powerRole,
            (int) dataRole,
            status.getSupportedRoleCombinations()));
    application.sendBroadcast(new Intent(UsbManager.ACTION_USB_PORT_CHANGED));
  }

  /** Opens a file descriptor from a temporary file. */
  @Implementation
  protected ParcelFileDescriptor openAccessory(UsbAccessory accessory) {
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
