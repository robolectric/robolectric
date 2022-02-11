package org.robolectric.fakes;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.webkit.WebMessage;
import android.webkit.WebMessagePort;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test RoboWebMessagePort. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.M)
public class RoboWebMessagePortTest {
  private RoboWebMessagePort[] ports;

  @Before
  public void setUp() {
    ports = RoboWebMessagePort.createPair();
  }

  @Test
  public void testDefaults() {
    assertThat(ports[0].getReceivedMessages()).isEmpty();
    assertThat(ports[0].getConnectedPort()).isNotNull();
    assertThat(ports[0].getWebMessageCallback()).isNull();
    assertThat(ports[0].isClosed()).isFalse();
    assertThat(ports[1].getReceivedMessages()).isEmpty();
    assertThat(ports[1].getConnectedPort()).isNotNull();
    assertThat(ports[1].getWebMessageCallback()).isNull();
    assertThat(ports[1].isClosed()).isFalse();
  }

  @Test
  public void testPostMessage() {
    String message = "message";
    ports[0].postMessage(new WebMessage(message));

    assertThat(ports[0].getOutgoingMessages()).containsExactly(message);
    assertThat(ports[1].getReceivedMessages()).containsExactly(message);
  }

  @Test
  public void testPostMessageOnClosedPort() {
    ports[0].close();
    ports[0].postMessage(new WebMessage("message"));

    assertThat(ports[0].getOutgoingMessages()).isEmpty();
  }

  @Test
  public void testSetWebMessageCallback() {
    WebMessagePort.WebMessageCallback callback =
        new WebMessagePort.WebMessageCallback() {
          @Override
          public void onMessage(WebMessagePort port, WebMessage message) {
            // some logic
          }
        };

    ports[0].setWebMessageCallback(callback);

    assertThat(ports[0].getWebMessageCallback()).isEqualTo(callback);
  }

  @Test
  public void testSetConnectedPort() {
    RoboWebMessagePort port1 = new RoboWebMessagePort();
    RoboWebMessagePort port2 = new RoboWebMessagePort();

    port1.setConnectedPort(port2);
    port2.setConnectedPort(port1);

    assertThat(port1.getConnectedPort()).isEqualTo(port2);
    assertThat(port2.getConnectedPort()).isEqualTo(port1);
  }

  @Test
  public void testSetConnectedNullPort() {
    ports[0].setConnectedPort(null);

    assertThat(ports[0].getConnectedPort()).isNull();
  }

  @Test
  public void testClose() {
    ports[0].close();

    assertThat(ports[0].isClosed()).isTrue();
  }
}
