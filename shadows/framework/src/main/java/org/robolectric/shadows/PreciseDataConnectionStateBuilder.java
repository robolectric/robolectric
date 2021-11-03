package org.robolectric.shadows;

import android.net.LinkProperties;
import android.os.Build.VERSION_CODES;
import android.telephony.PreciseDataConnectionState;
import android.telephony.data.ApnSetting;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Builder for {@link PreciseDataConnectionState} */
public class PreciseDataConnectionStateBuilder {

  private int dataState;
  private int networkType;
  private int transportType;
  private int id;
  private LinkProperties linkProperties;
  private ApnSetting apnSetting;
  private int dataFailCause;

  private PreciseDataConnectionStateBuilder() {}

  public static PreciseDataConnectionStateBuilder newBuilder() {
    return new PreciseDataConnectionStateBuilder();
  }

  public PreciseDataConnectionStateBuilder setDataState(int dataState) {
    this.dataState = dataState;
    return this;
  }

  public PreciseDataConnectionStateBuilder setNetworkType(int networkType) {
    this.networkType = networkType;
    return this;
  }

  public PreciseDataConnectionStateBuilder setTransportType(int transportType) {
    this.transportType = networkType;
    return this;
  }

  public PreciseDataConnectionStateBuilder setLinkProperties(LinkProperties linkProperties) {
    this.linkProperties = linkProperties;
    return this;
  }

  public PreciseDataConnectionStateBuilder setId(int id) {
    this.id = id;
    return this;
  }

  public PreciseDataConnectionStateBuilder setApnSetting(ApnSetting apnSetting) {
    this.apnSetting = apnSetting;
    return this;
  }

  public PreciseDataConnectionStateBuilder setDataFailCause(int dataFailCause) {
    this.dataFailCause = dataFailCause;
    return this;
  }

  public PreciseDataConnectionState build() {
    int apiLevel = RuntimeEnvironment.getApiLevel();
    if (apiLevel <= VERSION_CODES.R) {
      return ReflectionHelpers.callConstructor(
          PreciseDataConnectionState.class,
          ClassParameter.from(int.class, dataState),
          ClassParameter.from(int.class, networkType),
          ClassParameter.from(
              int.class,
              apnSetting == null ? ApnSetting.TYPE_DEFAULT : apnSetting.getApnTypeBitmask()),
          ClassParameter.from(String.class, apnSetting == null ? "" : apnSetting.getApnName()),
          ClassParameter.from(LinkProperties.class, linkProperties),
          ClassParameter.from(int.class, dataFailCause),
          ClassParameter.from(ApnSetting.class, apnSetting));
    } else {
      return new PreciseDataConnectionState.Builder()
          .setTransportType(transportType)
          .setId(id)
          .setState(dataState)
          .setNetworkType(networkType)
          .setLinkProperties(linkProperties)
          .setFailCause(dataFailCause)
          .setApnSetting(apnSetting)
          .build();
    }
  }
}
