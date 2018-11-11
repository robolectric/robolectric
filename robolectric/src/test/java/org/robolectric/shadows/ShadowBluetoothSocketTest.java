package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.bluetooth.BluetoothSocket;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.InputStream;
import java.util.Arrays;
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
}
