package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static java.lang.Math.min;
import static org.robolectric.Shadows.shadowOf;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/** Robolectric implementation of {@link android.hardware.usb.UsbRequest}. */
@Implements(value = UsbRequest.class)
public class ShadowUsbRequest {

  @RealObject private UsbRequest realUsbRequest;

  private UsbDeviceConnection usbDeviceConnection;

  private PipedInputStream incomingDataPipedInputStream;
  private PipedOutputStream incomingDataPipedOutputStream;

  private final ShadowUsbDeviceConnection.DataListener dataListener =
      new ShadowUsbDeviceConnection.DataListener() {
        @Override
        public void onDataReceived(byte[] data) {
          try {
            incomingDataPipedOutputStream.write(data);
          } catch (IOException e) {
            // ignored
          }
        }

        @Override
        public UsbRequest getUsbRequest() {
          return realUsbRequest;
        }
      };

  @Implementation
  protected boolean initialize(UsbDeviceConnection connection, UsbEndpoint endpoint) {
    try {
      this.incomingDataPipedInputStream = new PipedInputStream();
      this.incomingDataPipedOutputStream = new PipedOutputStream(incomingDataPipedInputStream);
    } catch (IOException e) {
      return false;
    }

    shadowOf(connection).registerDataListener(dataListener);
    this.usbDeviceConnection = connection;
    return true;
  }

  @Implementation
  protected void close() {
    if (usbDeviceConnection != null) {
      shadowOf(usbDeviceConnection).unregisterDataListener();
      usbDeviceConnection = null;

      try {
        incomingDataPipedInputStream.close();
      } catch (IOException e) {
        // ignored
      }
      try {
        incomingDataPipedOutputStream.close();
      } catch (IOException e) {
        // ignored
      }
    }
  }

  @Implementation
  protected boolean queue(ByteBuffer buffer, int length) {
    if (Build.VERSION.SDK_INT < P) {
      length = min(length, 16384);
    }

    byte[] bytes = new byte[length];
    try {
      int totalBytesRead = 0;
      while (totalBytesRead < length) {
        int bytesRead =
            incomingDataPipedInputStream.read(
                bytes, /*off=*/ totalBytesRead, /*len=*/ length - totalBytesRead);
        if (bytesRead < 0) {
          return false;
        }
        totalBytesRead += bytesRead;
      }
    } catch (IOException e) {
      return false;
    }
    buffer.put(bytes);
    return true;
  }

  @Implementation(minSdk = O)
  protected boolean queue(ByteBuffer buffer) {
    return queue(buffer, buffer.remaining());
  }
}
