package org.robolectric.shadows;

import android.net.TrafficStats;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowTrafficStatsTest {

    @Test
    public void allQueriesAreStubbed() throws Exception {
        int anything = -2;

        assertEquals(-1, TrafficStats.getThreadStatsTag());
        assertEquals(-1, TrafficStats.getMobileTxPackets());
        assertEquals(-1, TrafficStats.getMobileRxPackets());
        assertEquals(-1, TrafficStats.getMobileTxBytes());
        assertEquals(-1, TrafficStats.getMobileRxBytes());
        assertEquals(-1, TrafficStats.getTotalTxPackets());
        assertEquals(-1, TrafficStats.getTotalRxPackets());
        assertEquals(-1, TrafficStats.getTotalTxBytes());
        assertEquals(-1, TrafficStats.getTotalRxBytes());
        assertEquals(-1, TrafficStats.getUidTxBytes(anything));
        assertEquals(-1, TrafficStats.getUidRxBytes(anything));
        assertEquals(-1, TrafficStats.getUidTxPackets(anything));
        assertEquals(-1, TrafficStats.getUidRxPackets(anything));
        assertEquals(-1, TrafficStats.getUidTcpTxBytes(anything));
        assertEquals(-1, TrafficStats.getUidTcpRxBytes(anything));
        assertEquals(-1, TrafficStats.getUidUdpTxBytes(anything));
        assertEquals(-1, TrafficStats.getUidUdpRxBytes(anything));
        assertEquals(-1, TrafficStats.getUidTcpTxSegments(anything));
        assertEquals(-1, TrafficStats.getUidTcpRxSegments(anything));
        assertEquals(-1, TrafficStats.getUidUdpTxPackets(anything));
        assertEquals(-1, TrafficStats.getUidUdpRxPackets(anything));
    }

}
