package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;
import static org.robolectric.annotation.LooperMode.Mode.INSTRUMENTATION_TEST;

import android.content.Context;
import android.se.omapi.Channel;
import android.se.omapi.Reader;
import android.se.omapi.SEService;
import android.se.omapi.Session;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowSecureElementService.MockApplet;

/** Tests for {@link ShadowMediaCodec}. */
@LooperMode(INSTRUMENTATION_TEST)
@RunWith(AndroidJUnit4.class)
public final class ShadowSecureElementServiceTest {

  private final Context context = ApplicationProvider.getApplicationContext();
  private SEService seService;

  @Before
  public void setup() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    seService = new SEService(context, Executors.newSingleThreadExecutor(), latch::countDown);
    latch.await();
  }

  @Test
  @Config(minSdk = R)
  public void oneReader_oneChannel_openTransmitTwiceClose_success() throws Exception {
    MockApplet mockApplet = new MockApplet("eSE", "aid".getBytes(UTF_8));
    mockApplet.setSelectResponse("selected".getBytes(UTF_8));
    mockApplet.addApduResponse("firstResponse".getBytes(UTF_8));
    mockApplet.addApduResponse("secondResponse".getBytes(UTF_8));
    ShadowSecureElementService.addMockApplet(mockApplet);

    Reader[] readers = seService.getReaders();
    assertThat(readers).hasLength(1);
    assertThrows(IllegalArgumentException.class, () -> seService.getUiccReader(1));

    Reader reader = readers[0];
    assertThat(reader.getName()).isEqualTo("eSE");
    assertThat(reader.getSEService()).isEqualTo(seService);
    assertThat(reader.isSecureElementPresent()).isTrue();

    Session session = reader.openSession();
    assertThat(session).isNotNull();
    assertThat(session.isClosed()).isFalse();

    Channel channel = session.openLogicalChannel("aid".getBytes(UTF_8));
    assertThat(channel).isNotNull();

    assertThat(channel.getSelectResponse()).isEqualTo("selected".getBytes(UTF_8));
    assertThat(channel.isBasicChannel()).isFalse();
    assertThat(channel.isOpen()).isTrue();
    assertThat(channel.getSession()).isEqualTo(session);

    byte[] firstResponse = channel.transmit("firstCommand".getBytes(UTF_8));
    assertThat(firstResponse).isEqualTo("firstResponse".getBytes(UTF_8));

    byte[] secondResponse = channel.transmit("secondCommand".getBytes(UTF_8));
    assertThat(secondResponse).isEqualTo("secondResponse".getBytes(UTF_8));

    channel.close();
    assertThat(channel.isOpen()).isFalse();

    List<byte[]> requests = mockApplet.getAdpuRequests();
    assertThat(requests).hasSize(2);
    assertThat(requests.get(0)).isEqualTo("firstCommand".getBytes(UTF_8));
    assertThat(requests.get(1)).isEqualTo("secondCommand".getBytes(UTF_8));

    seService.shutdown();
    assertThat(seService.isConnected()).isFalse();
    assertThat(session.isClosed()).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void twoReaders_alsoWorks() throws Exception {
    ShadowSecureElementService.addMockApplet(new MockApplet("SIM1", "foo".getBytes(UTF_8)));
    ShadowSecureElementService.addMockApplet(new MockApplet("SIM2", "bar".getBytes(UTF_8)));

    Reader[] readers = seService.getReaders();
    assertThat(readers).hasLength(2);
    assertThat(seService.getUiccReader(1)).isEqualTo(readers[0]);
    assertThat(seService.getUiccReader(2)).isEqualTo(readers[1]);

    Reader fooReader = readers[0];
    Session fooSession = fooReader.openSession();
    Channel fooChannel = fooSession.openBasicChannel("foo".getBytes(UTF_8));
    assertThat(fooChannel).isNotNull();
    assertThat(fooChannel.getSelectResponse()).isEqualTo(new byte[] {(byte) 0x90, 0x00});
    assertThat(fooSession.openBasicChannel("bar".getBytes(UTF_8))).isNull();

    Reader barReader = readers[1];
    Session barSession = barReader.openSession();
    Channel barChannel = barSession.openBasicChannel("bar".getBytes(UTF_8));
    assertThat(barChannel).isNotNull();
    assertThat(barSession.openBasicChannel("foo".getBytes(UTF_8))).isNull();
  }
}
