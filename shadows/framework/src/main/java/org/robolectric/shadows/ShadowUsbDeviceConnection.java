package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
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

  /**
   * No-op on Robolectrict. The real implementation would return false on Robolectric and make it
   * impossible to test callers that expect a successful result. Always returns {@code true}.
   */
  @Implementation
  protected boolean setInterface(UsbInterface intf) {
    return true;
  }

  @Implementation
  protected int controlTransfer(
      int requestType, int request, int value, int index, byte[] buffer, int length, int timeout) {
    return length;
  }

  @Implementation
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

  @Implementation
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

  /**
   * Fills the buffer with data that was written by UsbDeviceConnection#bulkTransfer.
   *
   * @deprecated prefer {@link #getOutgoingDataStream()}, which allows callers to know how much data
   *     has been read and when the {@link UsbDeviceConnection} closes.
   */
  @Deprecated
  public void readOutgoingData(byte[] buffer) throws IOException {
    getOutgoingDataStream().read(buffer);
  }

  /**
   * Provides an {@link InputStream} that allows reading data written by
   * UsbDeviceConnection#bulkTransfer. Closing this stream has no effect. It is effectively closed
   * during {@link UsbDeviceConnection#releaseInterface(UsbInterface)}.
   */
  public InputStream getOutgoingDataStream() {
    return new FilterInputStream(outgoingDataPipedInputStream) {
      @Override
      public void close() throws IOException {
        // Override close() to prevent clients from closing the piped stream and causing unexpected
        // side-effects if further writes happen.
      }
    };
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
