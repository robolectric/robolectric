package org.robolectric.shadows;

import android.net.TrafficStats;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@SuppressWarnings("unused")
@Implements(TrafficStats.class)
public class ShadowTrafficStats {

  private ShadowTrafficStats() { }

  @Implementation
  public static void setThreadStatsTag(int tag) { }

  @Implementation
  public static int getThreadStatsTag() { return TrafficStats.UNSUPPORTED; }

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
  public static long getMobileTxPackets() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getMobileRxPackets() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getMobileTxBytes() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getMobileRxBytes() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getTotalTxPackets() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getTotalRxPackets() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getTotalTxBytes() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getTotalRxBytes() { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidTxBytes(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidRxBytes(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidTxPackets(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidRxPackets(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidTcpTxBytes(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidTcpRxBytes(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidUdpTxBytes(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidUdpRxBytes(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidTcpTxSegments(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidTcpRxSegments(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidUdpTxPackets(int i) { return TrafficStats.UNSUPPORTED; }

  @Implementation
  public static long getUidUdpRxPackets(int i) { return TrafficStats.UNSUPPORTED; }
}

