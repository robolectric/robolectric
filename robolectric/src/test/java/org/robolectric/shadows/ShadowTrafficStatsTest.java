package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.net.TrafficStats;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowTrafficStatsTest {

  @Test
  public void allUidSpecificAccessorsAreStubbed() throws Exception {
    int anything = -2;

    assertThat(TrafficStats.getUidTxBytes(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidRxBytes(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidTxPackets(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidRxPackets(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidTcpTxBytes(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidTcpRxBytes(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidUdpTxBytes(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidUdpRxBytes(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidTcpTxSegments(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidTcpRxSegments(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidUdpTxPackets(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
    assertThat(TrafficStats.getUidUdpRxPackets(anything)).isEqualTo(TrafficStats.UNSUPPORTED);
  }

  @Test
  public void getThreadStatsTagIsStubbed() {
    assertThat(TrafficStats.getThreadStatsTag()).isEqualTo(TrafficStats.UNSUPPORTED);
  }

  @Test
  public void setMobileTxPacketsUpdatesMobileTxPackets() throws Exception {
    assertThat(TrafficStats.getMobileTxPackets()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setMobileTxPackets(1);
    assertThat(TrafficStats.getMobileTxPackets()).isEqualTo(1);
  }

  @Test
  public void setMobileRxPacketsUpdatesMobileRxPackets() throws Exception {
    assertThat(TrafficStats.getMobileRxPackets()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setMobileRxPackets(2);
    assertThat(TrafficStats.getMobileRxPackets()).isEqualTo(2);
  }

  @Test
  public void setMobileTxBytesUpdatesMobileTxBytes() throws Exception {
    assertThat(TrafficStats.getMobileTxBytes()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setMobileTxBytes(3);
    assertThat(TrafficStats.getMobileTxBytes()).isEqualTo(3);
  }

  @Test
  public void setMobileRxBytesUpdatesMobileRxBytes() throws Exception {
    assertThat(TrafficStats.getMobileRxBytes()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setMobileRxBytes(4);
    assertThat(TrafficStats.getMobileRxBytes()).isEqualTo(4);
  }

  @Test
  public void setTotalTxPacketsUpdatesTotalTxPackets() throws Exception {
    assertThat(TrafficStats.getTotalTxPackets()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setTotalTxPackets(5);
    assertThat(TrafficStats.getTotalTxPackets()).isEqualTo(5);
  }

  @Test
  public void setTotalRxPacketsUpdatesTotalRxPackets() throws Exception {
    assertThat(TrafficStats.getTotalRxPackets()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setTotalRxPackets(6);
    assertThat(TrafficStats.getTotalRxPackets()).isEqualTo(6);
  }

  @Test
  public void setTotalTxBytesUpdatesTotalTxBytes() throws Exception {
    assertThat(TrafficStats.getTotalTxBytes()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setTotalTxBytes(7);
    assertThat(TrafficStats.getTotalTxBytes()).isEqualTo(7);
  }

  @Test
  public void setTotalRxBytesUpdatesTotalRxBytes() throws Exception {
    assertThat(TrafficStats.getTotalRxBytes()).isEqualTo(TrafficStats.UNSUPPORTED);

    ShadowTrafficStats.setTotalRxBytes(8);
    assertThat(TrafficStats.getTotalRxBytes()).isEqualTo(8);
  }
}
