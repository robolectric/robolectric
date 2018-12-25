package org.robolectric.shadows;

import android.net.TrafficStats;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(TrafficStats.class)
public class ShadowTrafficStats {

  @Implementation
  protected static void setThreadStatsTag(int tag) {}

  @Implementation
  protected static int getThreadStatsTag() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static void clearThreadStatsTag() {}

  @Implementation
  protected static void tagSocket(java.net.Socket socket) throws java.net.SocketException {}

  /** No-op in tests. */
  @Implementation(minSdk = Build.VERSION_CODES.N)
  protected static void tagDatagramSocket(java.net.DatagramSocket socket)
      throws java.net.SocketException {}

  @Implementation
  protected static void untagSocket(java.net.Socket socket) throws java.net.SocketException {}

  @Implementation
  protected static void incrementOperationCount(int operationCount) {}

  @Implementation
  protected static void incrementOperationCount(int tag, int operationCount) {}

  @Implementation
  protected static long getMobileTxPackets() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getMobileRxPackets() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getMobileTxBytes() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getMobileRxBytes() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getTotalTxPackets() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getTotalRxPackets() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getTotalTxBytes() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getTotalRxBytes() {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidTxBytes(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidRxBytes(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidTxPackets(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidRxPackets(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidTcpTxBytes(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidTcpRxBytes(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidUdpTxBytes(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidUdpRxBytes(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidTcpTxSegments(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidTcpRxSegments(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidUdpTxPackets(int i) {
    return TrafficStats.UNSUPPORTED;
  }

  @Implementation
  protected static long getUidUdpRxPackets(int i) {
    return TrafficStats.UNSUPPORTED;
  }
}
