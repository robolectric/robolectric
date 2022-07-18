package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
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
import android.hardware.usb.UsbRequest;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowUsbRequest}. */
@RunWith(AndroidJUnit4.class)
public class ShadowUsbRequestTest {
  private static final String DEVICE_NAME = "usb";

  private UsbManager usbManager;
  private UsbRequest dataRequest;

  @Mock private UsbDevice usbDevice;
  @Mock private UsbConfiguration usbConfiguration;
  @Mock private UsbInterface usbInterface;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    usbManager =
        (UsbManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.USB_SERVICE);
    dataRequest = new UsbRequest();

    when(usbDevice.getDeviceName()).thenReturn(DEVICE_NAME);
    when(usbDevice.getConfigurationCount()).thenReturn(1);
    when(usbDevice.getConfiguration(0)).thenReturn(usbConfiguration);
    when(usbConfiguration.getInterfaceCount()).thenReturn(1);
    when(usbConfiguration.getInterface(0)).thenReturn(usbInterface);
    when(usbConfiguration.getInterface(0)).thenReturn(usbInterface);

    shadowOf(usbManager).addOrUpdateUsbDevice(usbDevice, /*hasPermission=*/ true);
  }

  @After
  public void tearDown() {
    dataRequest.close();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void initialize() {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);
    UsbEndpoint usbEndpointIn = getEndpoint(usbInterface, UsbConstants.USB_DIR_IN);

    assertThat(dataRequest.initialize(usbDeviceConnection, usbEndpointIn)).isTrue();
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void queue() {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);
    UsbEndpoint usbEndpointIn = getEndpoint(usbInterface, UsbConstants.USB_DIR_IN);
    dataRequest.initialize(usbDeviceConnection, usbEndpointIn);

    byte[] msg = "Hello World".getBytes(UTF_8);
    shadowOf(usbDeviceConnection).writeIncomingData(msg);

    byte[] buffer = new byte[msg.length];
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

    dataRequest.queue(byteBuffer, buffer.length);
    assertThat(usbDeviceConnection.requestWait()).isEqualTo(dataRequest);
    assertThat(buffer).isEqualTo(msg);
  }

  @Test
  @Config(sdk = LOLLIPOP)
  // Before P, there's a limitation on the size of data that can be exchanged. Data over this limit
  // is cropped.
  public void queue_outOfSize() {
    UsbDeviceConnection usbDeviceConnection = usbManager.openDevice(usbDevice);
    UsbInterface usbInterface = selectInterface(usbDevice);
    UsbEndpoint usbEndpointIn = getEndpoint(usbInterface, UsbConstants.USB_DIR_IN);
    dataRequest.initialize(usbDeviceConnection, usbEndpointIn);

    byte[] helloWorld = "Hello World".getBytes(UTF_8);
    ByteBuffer msg = ByteBuffer.allocate(16384 + 1);
    msg.position(msg.capacity() - helloWorld.length);
    msg.put(helloWorld);
    new Thread(() -> shadowOf(usbDeviceConnection).writeIncomingData(msg.array())).start();

    byte[] buffer = new byte[msg.capacity()];
    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

    dataRequest.queue(byteBuffer, buffer.length);
    assertThat(usbDeviceConnection.requestWait()).isEqualTo(dataRequest);
    assertThat(buffer).isNotEqualTo(msg.array());
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
