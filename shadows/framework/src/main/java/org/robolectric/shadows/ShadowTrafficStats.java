package org.robolectric.shadows;

import android.net.TrafficStats;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(TrafficStats.class)
public class ShadowTrafficStats {

  private static int mobileTxPackets = TrafficStats.UNSUPPORTED;
  private static int mobileRxPackets = TrafficStats.UNSUPPORTED;
  private static int mobileTxBytes = TrafficStats.UNSUPPORTED;
  private static int mobileRxBytes = TrafficStats.UNSUPPORTED;
  private static int totalTxPackets = TrafficStats.UNSUPPORTED;
  private static int totalRxPackets = TrafficStats.UNSUPPORTED;
  private static int totalTxBytes = TrafficStats.UNSUPPORTED;
  private static int totalRxBytes = TrafficStats.UNSUPPORTED;

  private static final ThreadLocal<Integer> threadTag =
      ThreadLocal.withInitial(() -> TrafficStats.UNSUPPORTED);

  @Implementation
  protected static void setThreadStatsTag(int tag) {
    threadTag.set(tag);
  }

  @Implementation
  protected static int getThreadStatsTag() {
    return threadTag.get();
  }

  @Implementation
  protected static void clearThreadStatsTag() {
    threadTag.set(TrafficStats.UNSUPPORTED);
  }

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
    return mobileTxPackets;
  }

  @Implementation
  protected static long getMobileRxPackets() {
    return mobileRxPackets;
  }

  @Implementation
  protected static long getMobileTxBytes() {
    return mobileTxBytes;
  }

  @Implementation
  protected static long getMobileRxBytes() {
    return mobileRxBytes;
  }

  @Implementation
  protected static long getTotalTxPackets() {
    return totalTxPackets;
  }

  @Implementation
  protected static long getTotalRxPackets() {
    return totalRxPackets;
  }

  @Implementation
  protected static long getTotalTxBytes() {
    return totalTxBytes;
  }

  @Implementation
  protected static long getTotalRxBytes() {
    return totalRxBytes;
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

  /** Sets the value returned by {@link #getMobileTxPackets()} for testing */
  public static void setMobileTxPackets(int mobileTxPackets) {
    ShadowTrafficStats.mobileTxPackets = mobileTxPackets;
  }

  /** Sets the value returned by {@link #getMobileRxPackets()} for testing */
  public static void setMobileRxPackets(int mobileRxPackets) {
    ShadowTrafficStats.mobileRxPackets = mobileRxPackets;
  }

  /** Sets the value returned by {@link #getMobileTxBytes()} for testing */
  public static void setMobileTxBytes(int mobileTxBytes) {
    ShadowTrafficStats.mobileTxBytes = mobileTxBytes;
  }

  /** Sets the value returned by {@link #getMobileRxBytes()} for testing */
  public static void setMobileRxBytes(int mobileRxBytes) {
    ShadowTrafficStats.mobileRxBytes = mobileRxBytes;
  }

  /** Sets the value returned by {@link #getTotalTxPackets()} for testing */
  public static void setTotalTxPackets(int totalTxPackets) {
    ShadowTrafficStats.totalTxPackets = totalTxPackets;
  }

  /** Sets the value returned by {@link #getTotalRxPackets()} for testing */
  public static void setTotalRxPackets(int totalRxPackets) {
    ShadowTrafficStats.totalRxPackets = totalRxPackets;
  }

  /** Sets the value returned by {@link #getTotalTxBytes()} for testing */
  public static void setTotalTxBytes(int totalTxBytes) {
    ShadowTrafficStats.totalTxBytes = totalTxBytes;
  }

  /** Sets the value returned by {@link #getTotalRxBytes()} for testing */
  public static void setTotalRxBytes(int totalRxBytes) {
    ShadowTrafficStats.totalRxBytes = totalRxBytes;
  }

  /** Updates all non UID specific fields back to {@link TrafficStats#UNSUPPORTED} */
  @Resetter
  public static void restoreDefaults() {
    mobileTxPackets = TrafficStats.UNSUPPORTED;
    mobileRxPackets = TrafficStats.UNSUPPORTED;
    mobileTxBytes = TrafficStats.UNSUPPORTED;
    mobileRxBytes = TrafficStats.UNSUPPORTED;
    totalTxPackets = TrafficStats.UNSUPPORTED;
    totalRxPackets = TrafficStats.UNSUPPORTED;
    totalTxBytes = TrafficStats.UNSUPPORTED;
    totalRxBytes = TrafficStats.UNSUPPORTED;
    threadTag.remove();
  }
}
