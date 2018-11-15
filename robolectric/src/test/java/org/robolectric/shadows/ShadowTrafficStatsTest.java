package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;

import android.net.TrafficStats;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowTrafficStatsTest {

  @Test
  public void allQueriesAreStubbed() throws Exception {
    int anything = -2;

    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getThreadStatsTag());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getMobileTxPackets());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getMobileRxPackets());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getMobileTxBytes());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getMobileRxBytes());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getTotalTxPackets());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getTotalRxPackets());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getTotalTxBytes());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getTotalRxBytes());
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidTxBytes(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidRxBytes(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidTxPackets(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidRxPackets(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidTcpTxBytes(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidTcpRxBytes(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidUdpTxBytes(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidUdpRxBytes(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidTcpTxSegments(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidTcpRxSegments(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidUdpTxPackets(anything));
    assertEquals(TrafficStats.UNSUPPORTED, TrafficStats.getUidUdpRxPackets(anything));
  }

}
