package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.O;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeoutException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Robolectric implementation of {@link android.hardware.usb.UsbDeviceConnection}. */
@Implements(value = UsbDeviceConnection.class)
public class ShadowUsbDeviceConnection {

  private PipedInputStream outgoingDataPipedInputStream;
  private PipedOutputStream outgoingDataPipedOutputStream;

  private DataListener dataListener;

  @Implementation
  protected boolean claimInterface(UsbInterface intf, boolean force) {
    try {
      this.outgoingDataPipedInputStream = new PipedInputStream();
      this.outgoingDataPipedOutputStream = new PipedOutputStream(outgoingDataPipedInputStream);
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  @Implementation
  protected boolean releaseInterface(UsbInterface intf) {
    try {
      outgoingDataPipedInputStream.close();
    } catch (IOException e) {
      // ignored
    }
    try {
      outgoingDataPipedOutputStream.close();
    } catch (IOException e) {
      // ignored
    }

    return true;
  }

  @Implementation(minSdk = KITKAT)
  protected int controlTransfer(
      int requestType, int request, int value, int index, byte[] buffer, int length, int timeout) {
    return length;
  }

  @Implementation(minSdk = KITKAT)
  protected int controlTransfer(
      int requestType,
      int request,
      int value,
      int index,
      byte[] buffer,
      int offset,
      int length,
      int timeout) {
    return length;
  }

  @Implementation
  protected UsbRequest requestWait() {
    if (dataListener == null) {
      throw new IllegalStateException("No UsbRequest initialized for this UsbDeviceConnection");
    }

    return dataListener.getUsbRequest();
  }

  @Implementation(minSdk = O)
  protected UsbRequest requestWait(long timeout) throws TimeoutException {
    return requestWait();
  }

  @Implementation(minSdk = JELLY_BEAN_MR2)
  protected int bulkTransfer(
      UsbEndpoint endpoint, byte[] buffer, int offset, int length, int timeout) {
    try {
      outgoingDataPipedOutputStream.write(buffer, offset, length);
      return length;
    } catch (IOException e) {
      return -1;
    }
  }

  @Implementation
  protected int bulkTransfer(UsbEndpoint endpoint, byte[] buffer, int length, int timeout) {
    try {
      outgoingDataPipedOutputStream.write(buffer, /* off= */ 0, length);
      return length;
    } catch (IOException e) {
      return -1;
    }
  }

  /** Fills the buffer with data that was written by UsbDeviceConnection#bulkTransfer. */
  public void readOutgoingData(byte[] buffer) throws IOException {
    outgoingDataPipedInputStream.read(buffer);
  }

  /** Passes data that can then be read by an initialized UsbRequest#queue(ByteBuffer). */
  public void writeIncomingData(byte[] data) {
    if (dataListener == null) {
      throw new IllegalStateException("No UsbRequest initialized for this UsbDeviceConnection");
    }

    dataListener.onDataReceived(data);
  }

  void registerDataListener(DataListener dataListener) {
    this.dataListener = dataListener;
  }

  void unregisterDataListener() {
    this.dataListener = null;
  }

  interface DataListener {
    void onDataReceived(byte[] data);

    UsbRequest getUsbRequest();
  }
}
