package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.telephony.data.EpsBearerQosSessionAttributes;
import java.net.InetSocketAddress;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link EpsBearerQosSessionAttributesBuilder}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.S)
public class EpsBearerQosSessionAttributesBuilderTest {

  @Test
  public void createDefaultInstance_setsDefaultValues() {
    EpsBearerQosSessionAttributes epsBearerQosSessionAttributes =
        EpsBearerQosSessionAttributesBuilder.newBuilder().build();
    assertThat(epsBearerQosSessionAttributes.getQosIdentifier()).isEqualTo(0);
    assertThat(epsBearerQosSessionAttributes.getMaxUplinkBitRateKbps()).isEqualTo(0);
    assertThat(epsBearerQosSessionAttributes.getMaxDownlinkBitRateKbps()).isEqualTo(0);
    assertThat(epsBearerQosSessionAttributes.getGuaranteedUplinkBitRateKbps()).isEqualTo(0);
    assertThat(epsBearerQosSessionAttributes.getGuaranteedDownlinkBitRateKbps()).isEqualTo(0);
    assertThat(epsBearerQosSessionAttributes.getRemoteAddresses()).isEmpty();
  }

  @Test
  public void createInstanceWithValues_setsNewValues() {
    InetSocketAddress remoteAddress = new InetSocketAddress(/* port= */ 0);
    EpsBearerQosSessionAttributes epsBearerQosSessionAttributes =
        EpsBearerQosSessionAttributesBuilder.newBuilder()
            .setQci(1)
            .setMaxDownlinkBitRate(2)
            .setMaxUplinkBitRate(3)
            .setGuaranteedDownlinkBitRate(4)
            .setGuaranteedUplinkBitRate(5)
            .addRemoteAddress(remoteAddress)
            .build();
    assertThat(epsBearerQosSessionAttributes.getQosIdentifier()).isEqualTo(1);
    assertThat(epsBearerQosSessionAttributes.getMaxUplinkBitRateKbps()).isEqualTo(2);
    assertThat(epsBearerQosSessionAttributes.getMaxDownlinkBitRateKbps()).isEqualTo(3);
    assertThat(epsBearerQosSessionAttributes.getGuaranteedUplinkBitRateKbps()).isEqualTo(4);
    assertThat(epsBearerQosSessionAttributes.getGuaranteedDownlinkBitRateKbps()).isEqualTo(5);
    assertThat(epsBearerQosSessionAttributes.getRemoteAddresses()).containsExactly(remoteAddress);
  }

  @Test
  public void createInstanceWithMultipleRemoteAddresses_allAreSet() {
    InetSocketAddress remoteAddress1 = new InetSocketAddress(/* port= */ 0);
    InetSocketAddress remoteAddress2 = new InetSocketAddress(/* port= */ 1);
    InetSocketAddress remoteAddress3 = new InetSocketAddress(/* port= */ 2);
    EpsBearerQosSessionAttributes epsBearerQosSessionAttributes =
        EpsBearerQosSessionAttributesBuilder.newBuilder()
            .addRemoteAddress(remoteAddress1)
            .addRemoteAddress(remoteAddress2)
            .addRemoteAddress(remoteAddress3)
            .build();
    assertThat(epsBearerQosSessionAttributes.getRemoteAddresses())
        .containsExactly(remoteAddress1, remoteAddress2, remoteAddress3);
  }
}
