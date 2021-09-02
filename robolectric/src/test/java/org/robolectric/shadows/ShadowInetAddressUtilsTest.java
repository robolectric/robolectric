package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import java.net.InetAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for ShadowInetAddressUtils to check addresses are parsed to InetAddress. */
@RunWith(JUnit4.class)
public final class ShadowInetAddressUtilsTest {

  @Test
  public void parseNumericAddress_ipv4() {
    String input = "192.168.0.1";
    InetAddress result = ShadowInetAddressUtils.parseNumericAddressNoThrow(input);
    assertThat(result).isNotNull();
  }

  @Test
  public void parseNumericAddress_ipv6() {
    String input = "2001:4860:800d::68";
    InetAddress result = ShadowInetAddressUtils.parseNumericAddressNoThrow(input);
    assertThat(result).isNotNull();
  }
}
