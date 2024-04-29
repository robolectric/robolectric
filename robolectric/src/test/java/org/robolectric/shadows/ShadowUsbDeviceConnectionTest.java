package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link ShadowUsbDeviceConnection}. */
@RunWith(AndroidJUnit4.class)
public class ShadowUsbDeviceConnectionTest {
  private static final String DEVICE_NAME = "usb";

  private UsbManager usbManager;

  @Mock private UsbDevice usbDevice;
  @Mock private UsbConfiguration usbConfiguration;
  @Mock private UsbInterface usbInterface;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    usbManager =
        (UsbManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.USB_SERVICE);

    when(usbDevice.getDeviceName()).thenReturn(DEVICE_NAME);
    when(usbDevice.getConfigurationCount()).thenReturn(1);
    when(usbDevice.getConfiguration(0)).thenReturn(usbConfiguration);
    when(usbConfiguration.getInterfaceCount()).thenReturn(1);
    when(usbConfiguration.getInterface(0)).thenReturn(usbInterface);
  }

  @Test
  public void claimInterface() {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);

    assertThat(usbDeviceConnection.claimInterface(usbInterface, /* force= */ false)).isTrue();
    assertThat(usbDeviceConnection.releaseInterface(usbInterface)).isTrue();
  }

  @Test
  public void setInterface() {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);

    assertThat(usbDeviceConnection.setInterface(usbInterface)).isTrue();
  }

  @Test
  public void controlTransfer() {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);
    usbDeviceConnection.claimInterface(usbInterface, /* force= */ false);

    int len = 10;
    assertThat(
            usbDeviceConnection.controlTransfer(
                /* requestType= */ 0,
                /* request= */ 0,
                /* value= */ 0,
                /* index= */ 0,
                /* buffer= */ new byte[len],
                /* length= */ len,
                /* timeout= */ 0))
        .isEqualTo(len);
    assertThat(
            usbDeviceConnection.controlTransfer(
                /* requestType= */ 0,
                /* request= */ 0,
                /* value= */ 0,
                /* index= */ 0,
                /* buffer= */ new byte[len],
                /* offset= */ 0,
                /* length= */ len,
                /* timeout= */ 0))
        .isEqualTo(len);
  }

  @Test
  public void bulkTransfer() throws Exception {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);
    UsbEndpoint usbEndpointOut = getEndpoint(usbInterface, UsbConstants.USB_DIR_OUT);
    usbDeviceConnection.claimInterface(usbInterface, /* force= */ false);
    InputStream outgoingData = shadowOf(usbDeviceConnection).getOutgoingDataStream();

    byte[] msg = "Hello World".getBytes(UTF_8);
    assertThat(usbDeviceConnection.bulkTransfer(usbEndpointOut, msg, msg.length, /* timeout= */ 0))
        .isEqualTo(msg.length);

    byte[] buffer = new byte[1024];
    int read = outgoingData.read(buffer);
    assertThat(Arrays.copyOf(buffer, read)).isEqualTo(msg);

    msg = "Goodbye World".getBytes(UTF_8);
    assertThat(
            usbDeviceConnection.bulkTransfer(
                usbEndpointOut, msg, /* offset= */ 0, msg.length, /* timeout= */ 0))
        .isEqualTo(msg.length);

    buffer = new byte[1024];
    read = outgoingData.read(buffer);
    assertThat(Arrays.copyOf(buffer, read)).isEqualTo(msg);
  }

  @Test
  public void releaseInterface_closesOutgoingDataStream() throws Exception {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);
    UsbEndpoint usbEndpointOut = getEndpoint(usbInterface, UsbConstants.USB_DIR_OUT);
    usbDeviceConnection.claimInterface(usbInterface, /* force= */ false);
    InputStream outgoingData = shadowOf(usbDeviceConnection).getOutgoingDataStream();

    byte[] msg = "Hello World".getBytes(UTF_8);
    assertThat(usbDeviceConnection.bulkTransfer(usbEndpointOut, msg, msg.length, /* timeout= */ 0))
        .isEqualTo(msg.length);
    usbDeviceConnection.releaseInterface(usbInterface);

    byte[] buffer = new byte[1024];
    assertThrows(IOException.class, () -> outgoingData.read(buffer));
  }

  @Nullable
  private static UsbInterface selectInterface(UsbDevice device) {
    for (int i = 0; i < device.getConfigurationCount(); i++) {
      UsbConfiguration configuration = device.getConfiguration(i);
      for (int j = 0; j < configuration.getInterfaceCount(); j++) {
        return configuration.getInterface(i);
      }
    }
    return null;
  }

  @Nullable
  private static UsbEndpoint getEndpoint(UsbInterface usbInterface, int direction) {
    for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
      UsbEndpoint endpoint = usbInterface.getEndpoint(i);
      if (endpoint.getDirection() == direction) {
        return endpoint;
      }
    }
    return null;
  }
}
