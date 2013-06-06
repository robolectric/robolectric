package org.robolectric.shadows;

import android.net.TrafficStats;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings("unused")
@Implements(TrafficStats.class)
public class ShadowTrafficStats {

    public static final int UNSUPPORTED = -1;

    private ShadowTrafficStats() { }

    @Implementation
    public static void setThreadStatsTag(int tag) { }

    @Implementation
    public static int getThreadStatsTag() { return -1; }

    @Implementation
    public static void clearThreadStatsTag() { }

    @Implementation
    public static void tagSocket(java.net.Socket socket) throws java.net.SocketException { }

    @Implementation
    public static void untagSocket(java.net.Socket socket) throws java.net.SocketException { }

    @Implementation
    public static void incrementOperationCount(int operationCount) { }

    @Implementation
    public static void incrementOperationCount(int tag, int operationCount) { }

    @Implementation
    public static long getMobileTxPackets() { return -1; }

    @Implementation
    public static long getMobileRxPackets() { return -1; }

    @Implementation
    public static long getMobileTxBytes() { return -1; }

    @Implementation
    public static long getMobileRxBytes() { return -1; }

    @Implementation
    public static long getTotalTxPackets() { return -1; }

    @Implementation
    public static long getTotalRxPackets() { return -1; }

    @Implementation
    public static long getTotalTxBytes() { return -1; }

    @Implementation
    public static long getTotalRxBytes() { return -1; }

    @Implementation
    public static long getUidTxBytes(int i) { return -1; }

    @Implementation
    public static long getUidRxBytes(int i) { return -1; }

    @Implementation
    public static long getUidTxPackets(int i) { return -1; }

    @Implementation
    public static long getUidRxPackets(int i) { return -1; }

    @Implementation
    public static long getUidTcpTxBytes(int i) { return -1; }

    @Implementation
    public static long getUidTcpRxBytes(int i) { return -1; }

    @Implementation
    public static long getUidUdpTxBytes(int i) { return -1; }

    @Implementation
    public static long getUidUdpRxBytes(int i) { return -1; }

    @Implementation
    public static long getUidTcpTxSegments(int i) { return -1; }

    @Implementation
    public static long getUidTcpRxSegments(int i) { return -1; }

    @Implementation
    public static long getUidUdpTxPackets(int i) { return -1; }

    @Implementation
    public static long getUidUdpRxPackets(int i) { return -1; }
}

