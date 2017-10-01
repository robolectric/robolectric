package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Unit tests for {@link ShadowUsbManager}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowUsbManagerTest {
  private static final String DEVICE_NAME_1 = "usb1";
  private static final String DEVICE_NAME_2 = "usb2";
  private UsbManager usbManager;
  private ShadowUsbManager shadowUsbManager;

  @Mock UsbDevice usbDevice1;
  @Mock UsbDevice usbDevice2;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    usbManager = (UsbManager) RuntimeEnvironment.application.getSystemService(Context.USB_SERVICE);
    shadowUsbManager = shadowOf(usbManager);

    when(usbDevice1.getDeviceName()).thenReturn(DEVICE_NAME_1);
    when(usbDevice2.getDeviceName()).thenReturn(DEVICE_NAME_2);
  }

  @Test
  public void getDeviceList() {
    assertThat(usbManager.getDeviceList()).isEmpty();
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice1, true);
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice2, true);
    assertThat(usbManager.getDeviceList().values())
        .containsExactlyInAnyOrder(usbDevice1, usbDevice2);
  }

  @Test
  public void hasPermission() {
    assertThat(usbManager.hasPermission(usbDevice1)).isFalse();

    shadowUsbManager.addOrUpdateUsbDevice(usbDevice1, false);
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice2, false);

    assertThat(usbManager.hasPermission(usbDevice1)).isFalse();
    assertThat(usbManager.hasPermission(usbDevice2)).isFalse();

    shadowUsbManager.addOrUpdateUsbDevice(usbDevice1, true);

    assertThat(usbManager.hasPermission(usbDevice1)).isTrue();
    assertThat(usbManager.hasPermission(usbDevice2)).isFalse();
  }

  @Test
  public void removeDevice() {
    assertThat(usbManager.getDeviceList()).isEmpty();
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice1, false);
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice2, false);

    assertThat(usbManager.getDeviceList().values())
        .containsExactlyInAnyOrder(usbDevice1, usbDevice2);

    shadowUsbManager.removeUsbDevice(usbDevice1);
    assertThat(usbManager.getDeviceList().values()).containsExactlyInAnyOrder(usbDevice2);
  }

  @Test
  public void reset() {
    assertThat(usbManager.getDeviceList()).isEmpty();
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice1, false);
    shadowUsbManager.addOrUpdateUsbDevice(usbDevice2, false);

    assertThat(usbManager.getDeviceList().values())
        .containsExactlyInAnyOrder(usbDevice1, usbDevice2);

    shadowUsbManager.reset();
    assertThat(usbManager.getDeviceList()).isEmpty();
  }

  @Test
  public void openAccessory() {
    UsbAccessory usbAccessory = new UsbAccessory("", "", "", "", "", "");
    assertThat(usbManager.openAccessory(usbAccessory)).isNotNull();
  }
}
