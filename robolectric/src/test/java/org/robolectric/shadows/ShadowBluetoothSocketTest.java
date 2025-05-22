package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothSocket;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothSocketTest {
  BluetoothSocket bluetoothSocket;

  private static final byte[] DATA = new byte[] {1, 2, 3, 42, 96, 127};

  @Before
  public void setUp() throws Exception {
    bluetoothSocket = Shadow.newInstanceOf(BluetoothSocket.class);
  }

  @Test
  public void getInputStreamFeeder() throws Exception {
    shadowOf(bluetoothSocket).getInputStreamFeeder().write(DATA);

    InputStream inputStream = bluetoothSocket.getInputStream();
    byte[] b = new byte[1024];
    int len = inputStream.read(b);
    assertThat(Arrays.copyOf(b, len)).isEqualTo(DATA);
  }

  @Test
  public void getOutputStreamSink() throws Exception {
    bluetoothSocket.getOutputStream().write(DATA);

    byte[] b = new byte[1024];
    int len = shadowOf(bluetoothSocket).getOutputStreamSink().read(b);
    assertThat(Arrays.copyOf(b, len)).isEqualTo(DATA);
  }

  private static class SocketVerifier extends PipedOutputStream {
    boolean success = false;

    @Override
    public void write(byte[] b, int off, int len) {
      success = true;
    }
  }

  @Test
  public void setOutputStream_withWrite_observable() throws Exception {
    SocketVerifier socketVerifier = new SocketVerifier();
    shadowOf(bluetoothSocket).setOutputStream(socketVerifier);

    bluetoothSocket.getOutputStream().write(DATA);

    assertThat(socketVerifier.success).isTrue();
  }

  @Test
  public void close() throws Exception {
    bluetoothSocket.close();

    assertThrows(IOException.class, () -> bluetoothSocket.connect());
  }

  @Test
  public void unblockConnect_withoutBlocking_throws() throws Exception {
    assertThrows(IllegalStateException.class, () -> shadowOf(bluetoothSocket).unblockConnect());
  }

  @Test
  public void blockConnect_afterAlreadyBlocking_throws() throws Exception {
    shadowOf(bluetoothSocket).blockConnect();
    assertThrows(IllegalStateException.class, () -> shadowOf(bluetoothSocket).blockConnect());
  }

  @Test
  public void blockConnect_afterUnblocked_throws() throws Exception {
    shadowOf(bluetoothSocket).blockConnect();
    shadowOf(bluetoothSocket).unblockConnect();
    assertThrows(IllegalStateException.class, () -> shadowOf(bluetoothSocket).blockConnect());
  }

  @Test
  public void blockConnect_afterConnectAlreadyDone_completesWithoutBlocking() throws Exception {
    bluetoothSocket.connect();
    shadowOf(bluetoothSocket).blockConnect();
    // Test succeeds if blockConnect() completes.
  }

  @Test
  public void connect_withoutBlocking_succeeds() throws Exception {
    assertThat(bluetoothSocket.isConnected()).isFalse();
    bluetoothSocket.connect();
    assertThat(bluetoothSocket.isConnected()).isTrue();
    bluetoothSocket.close();
    assertThat(bluetoothSocket.isConnected()).isFalse();
  }

  @Test
  public void connect_afterUnblocking_succeeds() throws Exception {
    shadowOf(bluetoothSocket).blockConnect();
    shadowOf(bluetoothSocket).unblockConnect();

    bluetoothSocket.connect();
    assertThat(bluetoothSocket.isConnected()).isTrue();
    bluetoothSocket.close();
    assertThat(bluetoothSocket.isConnected()).isFalse();
  }

  @Test
  public void unblockConnect_isIdempotent() throws Exception {
    shadowOf(bluetoothSocket).blockConnect();
    shadowOf(bluetoothSocket).unblockConnect();
    shadowOf(bluetoothSocket).unblockConnect();

    bluetoothSocket.connect();
    assertThat(bluetoothSocket.isConnected()).isTrue();
  }

  @Test
  public void connect_whileBlocked_blockedUntilShadowUnblocksAndDoesNotThrow() throws Exception {
    shadowOf(bluetoothSocket).blockConnect();

    AtomicBoolean connectCompletedExceptionally = new AtomicBoolean(false);
    CountDownLatch aboutToConnectLatch = new CountDownLatch(1);
    CountDownLatch completedConnectLatch = new CountDownLatch(1);
    Future<?> unused =
        Executors.newSingleThreadExecutor()
            .submit(
                () -> {
                  aboutToConnectLatch.countDown();
                  try {
                    bluetoothSocket.connect();
                  } catch (IOException e) {
                    connectCompletedExceptionally.set(true);
                  }
                  completedConnectLatch.countDown();
                });
    aboutToConnectLatch.await();

    assertThat(completedConnectLatch.await(100, MILLISECONDS)).isFalse();
    shadowOf(bluetoothSocket).unblockConnect();
    assertThat(completedConnectLatch.await(100, MILLISECONDS)).isTrue();
    assertThat(connectCompletedExceptionally.get()).isFalse();
    assertThat(bluetoothSocket.isConnected()).isTrue();
  }

  @Test
  public void connect_whileBlocked_blockedUntilCloseAndThrows() throws Exception {
    shadowOf(bluetoothSocket).blockConnect();

    AtomicBoolean connectCompletedExceptionally = new AtomicBoolean(false);
    CountDownLatch aboutToConnectLatch = new CountDownLatch(1);
    CountDownLatch completedConnectLatch = new CountDownLatch(1);
    Future<?> unused =
        Executors.newSingleThreadExecutor()
            .submit(
                () -> {
                  aboutToConnectLatch.countDown();
                  try {
                    bluetoothSocket.connect();
                  } catch (IOException e) {
                    connectCompletedExceptionally.set(true);
                  }
                  completedConnectLatch.countDown();
                });
    aboutToConnectLatch.await();

    assertThat(completedConnectLatch.await(100, MILLISECONDS)).isFalse();
    bluetoothSocket.close();
    assertThat(completedConnectLatch.await(100, MILLISECONDS)).isTrue();
    assertThat(connectCompletedExceptionally.get()).isTrue();
    assertThat(bluetoothSocket.isConnected()).isFalse();
  }

  @Test
  public void connect_customException_throws() throws Exception {
    IOException customException = new IOException("custom exception");
    shadowOf(bluetoothSocket).setConnectException(customException);

    bluetoothSocket.close();
    IOException thrownException = assertThrows(IOException.class, () -> bluetoothSocket.connect());

    assertThat(thrownException).isSameInstanceAs(customException);
  }
}
