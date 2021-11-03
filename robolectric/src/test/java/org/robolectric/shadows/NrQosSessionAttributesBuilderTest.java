package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build.VERSION_CODES;
import android.telephony.data.NrQosSessionAttributes;
import java.net.InetSocketAddress;
import java.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Unit tests for {@link NrQosSessionAttributesBuilder}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.S)
public class NrQosSessionAttributesBuilderTest {

  @Test
  public void createDefaultInstance_setsDefaultValues() {
    NrQosSessionAttributes nrQosSessionAttributes =
        NrQosSessionAttributesBuilder.newBuilder().build();
    assertThat(nrQosSessionAttributes.getQosIdentifier()).isEqualTo(0);
    assertThat(nrQosSessionAttributes.getQosFlowIdentifier()).isEqualTo(0);
    assertThat(nrQosSessionAttributes.getMaxDownlinkBitRateKbps()).isEqualTo(0);
    assertThat(nrQosSessionAttributes.getMaxUplinkBitRateKbps()).isEqualTo(0);
    assertThat(nrQosSessionAttributes.getGuaranteedDownlinkBitRateKbps()).isEqualTo(0);
    assertThat(nrQosSessionAttributes.getGuaranteedUplinkBitRateKbps()).isEqualTo(0);
    assertThat(nrQosSessionAttributes.getBitRateWindowDuration()).isEqualTo(Duration.ZERO);
    assertThat(nrQosSessionAttributes.getRemoteAddresses()).isEmpty();
  }

  @Test
  public void createInstanceWithValues_setsNewValues() {
    InetSocketAddress remoteAddress = new InetSocketAddress(/* port= */ 0);
    NrQosSessionAttributes nrQosSessionAttributes =
        NrQosSessionAttributesBuilder.newBuilder()
            .setFiveQi(1)
            .setQfi(2)
            .setMaxDownlinkBitRate(3)
            .setMaxUplinkBitRate(4)
            .setGuaranteedDownlinkBitRate(5)
            .setGuaranteedUplinkBitRate(6)
            .setAveragingWindow(7)
            .addRemoteAddress(remoteAddress)
            .build();
    assertThat(nrQosSessionAttributes.getQosIdentifier()).isEqualTo(1);
    assertThat(nrQosSessionAttributes.getQosFlowIdentifier()).isEqualTo(2);
    assertThat(nrQosSessionAttributes.getMaxDownlinkBitRateKbps()).isEqualTo(3);
    assertThat(nrQosSessionAttributes.getGuaranteedDownlinkBitRateKbps()).isEqualTo(5);
    assertThat(nrQosSessionAttributes.getGuaranteedUplinkBitRateKbps()).isEqualTo(6);
    assertThat(nrQosSessionAttributes.getBitRateWindowDuration()).isEqualTo(Duration.ofMillis(7));
    assertThat(nrQosSessionAttributes.getRemoteAddresses()).contains(remoteAddress);
  }
}
