package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.RuntimeEnvironment.getApiLevel;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(BluetoothSocket.class)
public class ShadowBluetoothSocket {
  private final PipedOutputStream inputStreamFeeder = new PipedOutputStream();
  private final PipedInputStream outputStreamSink = new PipedInputStream();
  private OutputStream outputStream;
  private final InputStream inputStream;

  // One permit allows connect() without a prior blockConnect() to complete immediately.
  private final Semaphore connectSemaphore = new Semaphore(1);
  private final AtomicBoolean wasBlockRequested = new AtomicBoolean(false);

  private enum SocketState {
    INIT,
    CONNECTED,
    CLOSED,
  }

  private volatile SocketState state = SocketState.INIT;
  @Nullable private IOException connectExceptionOverride = null;

  public ShadowBluetoothSocket() {
    try {
      outputStream = new PipedOutputStream(outputStreamSink);
      inputStream = new PipedInputStream(inputStreamFeeder);
    } catch (IOException e) {
      // Shouldn't happen. Rethrow as an unchecked exception.
      throw new RuntimeException(e);
    }
  }

  /**
   * Set the output stream. {@code write()} operations on this stream can be observed to verify
   * expected behavior.
   */
  public void setOutputStream(PipedOutputStream outputStream) {
    this.outputStream = outputStream;
  }

  /**
   * Returns {@link PipedOutputStream} that controls <b>input</b> stream of the {@link
   * BluetoothSocket}.
   */
  public PipedOutputStream getInputStreamFeeder() {
    return inputStreamFeeder;
  }

  /**
   * Returns {@link PipedInputStream} that controls <b>output</b> stream of the {@link
   * BluetoothSocket}.
   */
  public PipedInputStream getOutputStreamSink() {
    return outputStreamSink;
  }

  /**
   * Causes calls to {@link #connect()} to block until either {@link #unblockConnect()} or {@link
   * #close()} are called. Note that the real implementation of {@link #connect()} is expected to
   * unblock when the socket is closed from a different thread.
   *
   * <p>This method may only be called once per instance (a socket is only expected to be connected
   * once).
   */
  public void blockConnect() {
    if (!wasBlockRequested.compareAndSet(/* expectedValue= */ false, /* newValue= */ true)) {
      throw new IllegalStateException("blockConnect() was previously called");
    }
    acquireConnectSemaphore();
  }

  /**
   * Causes calls to {@link #connect()} to unblock. This may be called while {@link #connect()} is
   * being called from another thread, or before {@link #connect()} is called.
   *
   * <p>{@link #blockConnect()} must be called before calling this method (otherwise, the socket is
   * already unblocked, so there is no need to call this method).
   */
  public void unblockConnect() {
    if (!wasBlockRequested.get()) {
      throw new IllegalStateException("blockConnect() was not called");
    }
    connectSemaphore.release();
  }

  /**
   * Set the exception that {@link #connect()} will throw if the socket is closed. This can be used
   * to test situations where {@link android.bluetooth.BluetoothSocketException} is thrown.
   */
  public void setConnectException(IOException connectException) {
    this.connectExceptionOverride = connectException;
  }

  @Implementation
  protected InputStream getInputStream() {
    return inputStream;
  }

  @Implementation
  protected OutputStream getOutputStream() {
    return outputStream;
  }

  @Implementation
  protected boolean isConnected() {
    return state == SocketState.CONNECTED;
  }

  @Implementation
  protected void connect() throws IOException {
    // If already closed, throw immediately. The state cannot become CONNECTED again.
    throwIfClosed();

    // Set state before blocking so if the client calls close() to unblock connect(), the final
    // state will be CLOSED.
    state = SocketState.CONNECTED;

    // If blockConnect() was called, execution will halt until the semaphore has been released.
    // Release is called right afterward to avoid side effects on a later call to blockConnect().
    acquireConnectSemaphore();
    connectSemaphore.release();

    // Throw if the socket was closed while waiting for the connect to complete. This reflects the
    // behavior of the real BluetoothSocket.
    throwIfClosed();
  }

  private void acquireConnectSemaphore() {
    try {
      connectSemaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("Interrupted while waiting for connect semaphore", e);
    }
  }

  private void throwIfClosed() throws IOException {
    if (state == SocketState.CLOSED) {
      if (connectExceptionOverride != null) {
        throw connectExceptionOverride;
      }
      throw new IOException("socket closed");
    }
  }

  @Implementation
  protected void close() throws IOException {
    state = SocketState.CLOSED;
    connectSemaphore.release();
  }

  static BluetoothSocket create(BluetoothDevice device) {
    BluetoothSocket newSocket = Shadow.newInstanceOf(BluetoothSocket.class);
    // TODO: remove field check once SDK is released that has mRemoteDevice
    if (getApiLevel() > BAKLAVA
        && ReflectionHelpers.hasField(BluetoothSocket.class, "mRemoteDevice")) {
      reflector(BluetoothSocketReflector.class, newSocket).setRemoteDevice(Optional.of(device));
    } else {
      reflector(BluetoothSocketReflector.class, newSocket).setDevice(device);
    }
    return newSocket;
  }

  @ForType(BluetoothSocket.class)
  private interface BluetoothSocketReflector {
    @Accessor("mRemoteDevice")
    void setRemoteDevice(Optional<BluetoothDevice> remoteDevice);

    @Accessor("mDevice")
    void setDevice(BluetoothDevice device);
  }
}
