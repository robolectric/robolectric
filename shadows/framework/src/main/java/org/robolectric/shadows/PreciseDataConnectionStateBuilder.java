package org.robolectric.shadows;

import android.net.LinkProperties;
import android.os.Build.VERSION_CODES;
import android.telephony.PreciseDataConnectionState;
import android.telephony.data.ApnSetting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
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
  private int networkValidationStatus;

  private PreciseDataConnectionStateBuilder() {}

  public static PreciseDataConnectionStateBuilder newBuilder() {
    return new PreciseDataConnectionStateBuilder();
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setDataState(int dataState) {
    this.dataState = dataState;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setNetworkType(int networkType) {
    this.networkType = networkType;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setTransportType(int transportType) {
    this.transportType = transportType;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setLinkProperties(LinkProperties linkProperties) {
    this.linkProperties = linkProperties;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setId(int id) {
    this.id = id;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setApnSetting(ApnSetting apnSetting) {
    this.apnSetting = apnSetting;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setDataFailCause(int dataFailCause) {
    this.dataFailCause = dataFailCause;
    return this;
  }

  @CanIgnoreReturnValue
  public PreciseDataConnectionStateBuilder setNetworkValidationStatus(int networkValidationStatus) {
    this.networkValidationStatus = networkValidationStatus;
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
      PreciseDataConnectionState.Builder builder =
          new PreciseDataConnectionState.Builder()
              .setTransportType(transportType)
              .setId(id)
              .setState(dataState)
              .setNetworkType(networkType)
              .setLinkProperties(linkProperties)
              .setFailCause(dataFailCause)
              .setApnSetting(apnSetting);

      if (apiLevel >= VERSION_CODES.VANILLA_ICE_CREAM) {
        builder = builder.setNetworkValidationStatus(networkValidationStatus);
      }

      return builder.build();
    }
  }
}
