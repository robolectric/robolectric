package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.nfc.tech.IsoDep;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ShadowIsoDep}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public final class ShadowIsoDepTest {

  private IsoDep isoDep;

  @Before
  public void setUp() {
    isoDep = ShadowIsoDep.newInstance();
  }

  @Test
  public void transceive() throws Exception {
    shadowOf(isoDep).setTransceiveResponse(new byte[] {1, 2, 3});
    assertThat(isoDep.transceive(new byte[0])).isEqualTo(new byte[] {1, 2, 3});
  }

  @Test
  public void nextTransceive() throws Exception {
    shadowOf(isoDep).setNextTransceiveResponse(new byte[] {1, 2, 3});
    assertThat(isoDep.transceive(new byte[0])).isEqualTo(new byte[] {1, 2, 3});
    assertThrows(IOException.class, () -> isoDep.transceive(new byte[0]));
  }

  @Test
  public void timeout() {
    isoDep.setTimeout(1000);
    assertThat(isoDep.getTimeout()).isEqualTo(1000);
  }

  @Test
  public void maxTransceiveLength() {
    shadowOf(isoDep).setMaxTransceiveLength(1000);
    assertThat(isoDep.getMaxTransceiveLength()).isEqualTo(1000);
  }

  @Test
  public void isExtendedLengthApduSupported() {
    shadowOf(isoDep).setExtendedLengthApduSupported(true);
    assertThat(isoDep.isExtendedLengthApduSupported()).isTrue();
    shadowOf(isoDep).setExtendedLengthApduSupported(false);
    assertThat(isoDep.isExtendedLengthApduSupported()).isFalse();
  }

  private static <T extends Throwable> void assertThrows(Class<T> clazz, Callable<?> callable) {
    try {
      callable.call();
    } catch (Throwable t) {
      if (clazz.isInstance(t)) {
        // expected
        return;
      } else {
        fail("did not throw " + clazz.getName() + ", threw " + t + " instead");
      }
    }
    fail("did not throw " + clazz.getName());
  }
}
