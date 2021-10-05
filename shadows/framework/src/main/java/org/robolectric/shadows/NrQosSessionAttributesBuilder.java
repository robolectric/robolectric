package org.robolectric.shadows;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.telephony.data.NrQosSessionAttributes;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/** Class to build {@link NrQosSessionAttributes}. */
@TargetApi(VERSION_CODES.S)
public final class NrQosSessionAttributesBuilder {

  private int fiveQi;
  private int qfi;
  private long maxDownlinkBitRate;
  private long maxUplinkBitRate;
  private long guaranteedDownlinkBitRate;
  private long guaranteedUplinkBitRate;
  private long averagingWindow;
  private final List<InetSocketAddress> remoteAddresses = new ArrayList<>();

  public static NrQosSessionAttributesBuilder newBuilder() {
    return new NrQosSessionAttributesBuilder();
  }

  public NrQosSessionAttributesBuilder setFiveQi(int fiveQi) {
    this.fiveQi = fiveQi;
    return this;
  }

  public NrQosSessionAttributesBuilder setQfi(int qfi) {
    this.qfi = qfi;
    return this;
  }

  public NrQosSessionAttributesBuilder setMaxDownlinkBitRate(long maxDownlinkBitRate) {
    this.maxDownlinkBitRate = maxDownlinkBitRate;
    return this;
  }

  public NrQosSessionAttributesBuilder setMaxUplinkBitRate(long maxUplinkBitRate) {
    this.maxUplinkBitRate = maxUplinkBitRate;
    return this;
  }

  public NrQosSessionAttributesBuilder setGuaranteedDownlinkBitRate(
      long guaranteedDownlinkBitRate) {
    this.guaranteedDownlinkBitRate = guaranteedDownlinkBitRate;
    return this;
  }

  public NrQosSessionAttributesBuilder setGuaranteedUplinkBitRate(long guaranteedUplinkBitRate) {
    this.guaranteedUplinkBitRate = guaranteedUplinkBitRate;
    return this;
  }

  public NrQosSessionAttributesBuilder setAveragingWindow(long averagingWindow) {
    this.averagingWindow = averagingWindow;
    return this;
  }

  public NrQosSessionAttributesBuilder addRemoteAddress(InetSocketAddress remoteAddress) {
    this.remoteAddresses.add(remoteAddress);
    return this;
  }

  public NrQosSessionAttributes build() {
    return new NrQosSessionAttributes(
        fiveQi,
        qfi,
        maxDownlinkBitRate,
        maxUplinkBitRate,
        guaranteedDownlinkBitRate,
        guaranteedUplinkBitRate,
        averagingWindow,
        remoteAddresses);
  }
}
