package org.robolectric.shadows;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.telephony.data.EpsBearerQosSessionAttributes;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/** Class to build {@link EpsBearerQosSessionAttributes}. */
@TargetApi(VERSION_CODES.S)
public final class EpsBearerQosSessionAttributesBuilder {

  private int qci;
  private long maxDownlinkBitRate;
  private long maxUplinkBitRate;
  private long guaranteedDownlinkBitRate;
  private long guaranteedUplinkBitRate;
  private final List<InetSocketAddress> remoteAddresses = new ArrayList<>();

  private EpsBearerQosSessionAttributesBuilder() {}

  public static EpsBearerQosSessionAttributesBuilder newBuilder() {
    return new EpsBearerQosSessionAttributesBuilder();
  }

  public EpsBearerQosSessionAttributesBuilder setQci(int qci) {
    this.qci = qci;
    return this;
  }

  public EpsBearerQosSessionAttributesBuilder setMaxDownlinkBitRate(long maxDownlinkBitRate) {
    this.maxDownlinkBitRate = maxDownlinkBitRate;
    return this;
  }

  public EpsBearerQosSessionAttributesBuilder setMaxUplinkBitRate(long maxUplinkBitRate) {
    this.maxUplinkBitRate = maxUplinkBitRate;
    return this;
  }

  public EpsBearerQosSessionAttributesBuilder setGuaranteedDownlinkBitRate(
      long guaranteedDownlinkBitRate) {
    this.guaranteedDownlinkBitRate = guaranteedDownlinkBitRate;
    return this;
  }

  public EpsBearerQosSessionAttributesBuilder setGuaranteedUplinkBitRate(
      long guaranteedUplinkBitRate) {
    this.guaranteedUplinkBitRate = guaranteedUplinkBitRate;
    return this;
  }

  public EpsBearerQosSessionAttributesBuilder addRemoteAddress(InetSocketAddress remoteAddress) {
    this.remoteAddresses.add(remoteAddress);
    return this;
  }

  public EpsBearerQosSessionAttributes build() {
    return new EpsBearerQosSessionAttributes(
        qci,
        maxUplinkBitRate,
        maxDownlinkBitRate,
        guaranteedUplinkBitRate,
        guaranteedDownlinkBitRate,
        remoteAddresses);
  }
}
