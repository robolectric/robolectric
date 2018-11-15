package org.robolectric.shadows;

import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(BluetoothSocket.class)
public class ShadowBluetoothSocket {
  private final PipedOutputStream inputStreamFeeder = new PipedOutputStream();
  private final PipedInputStream outputStreamSink = new PipedInputStream();
  private final OutputStream outputStream;
  private final InputStream inputStream;

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

  @Implementation
  protected InputStream getInputStream() {
    return inputStream;
  }

  @Implementation
  protected OutputStream getOutputStream() {
    return outputStream;
  }
}
