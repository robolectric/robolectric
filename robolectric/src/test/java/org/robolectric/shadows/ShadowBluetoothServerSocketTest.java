package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit tests for {@link ShadowBluetoothServerSocket}. */
@RunWith(AndroidJUnit4.class)
public class ShadowBluetoothServerSocketTest {

  private static final UUID DUMMY_UUID = UUID.fromString("00000000-1111-2222-3333-444444444444");

  private BluetoothServerSocket serverSocket;

  @Before
  public void setUp() {
    serverSocket = ShadowBluetoothServerSocket.newInstance(BluetoothSocket.TYPE_RFCOMM,
            /*auth=*/ false, /*encrypt=*/ false, new ParcelUuid(DUMMY_UUID));
  }

  @Test
  public void accept() throws Exception {
    BluetoothDevice btDevice = ShadowBluetoothDevice.newInstance("DE:AD:BE:EE:EE:EF");
    shadowOf(serverSocket).deviceConnected(btDevice);

    BluetoothSocket clientSocket = serverSocket.accept();
    assertThat(clientSocket.getRemoteDevice()).isSameInstanceAs(btDevice);
  }

  @Test
  public void accept_timeout() {
    try {
      serverSocket.accept(200);
      fail();
    } catch (IOException expected) {
      // Expected
    }
  }

  @Test
  public void close() throws Exception {
    serverSocket.close();

    try {
      serverSocket.accept();
      fail();
    } catch (IOException expected) {
      // Expected.
    }
  }
}