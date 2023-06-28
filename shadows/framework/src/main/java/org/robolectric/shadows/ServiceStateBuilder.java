package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION;
import android.telephony.ServiceState;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Builder class to create instance of {@link ServiceState}. */
public class ServiceStateBuilder {
  private final ServiceState serviceState = new ServiceState();

  public static ServiceStateBuilder newBuilder() {
    return new ServiceStateBuilder();
  }

  public ServiceState build() {
    return serviceState;
  }

  public ServiceStateBuilder setVoiceRegState(int value) {
    serviceState.setVoiceRegState(value);
    return this;
  }

  public ServiceStateBuilder setDataRegState(int value) {
    serviceState.setDataRegState(value);
    return this;
  }

  public ServiceStateBuilder setNrFrequencyRange(int value) {
    assertIsAtLeast(Q);
    serviceState.setNrFrequencyRange(value);
    return this;
  }

  public ServiceStateBuilder setIsManualSelection(boolean value) {
    serviceState.setIsManualSelection(value);
    return this;
  }

  public ServiceStateBuilder setOperatorName(String longName, String shortName, String numeric) {
    assertIsAtLeast(R);
    serviceState.setOperatorName(longName, shortName, numeric);
    return this;
  }

  public ServiceStateBuilder setIwlanPreferred(boolean value) {
    assertIsAtLeast(R);
    serviceState.setIwlanPreferred(value);
    return this;
  }

  public ServiceStateBuilder setEmergencyOnly(boolean value) {
    serviceState.setEmergencyOnly(value);
    return this;
  }

  public ServiceStateBuilder setDataRoamingFromRegistration(boolean value) {
    assertIsAtLeast(R);
    serviceState.setDataRoamingFromRegistration(value);
    return this;
  }

  public ServiceStateBuilder setIsUsingCarrierAggregation(boolean value) {
    assertIsAtLeast(P);
    if (VERSION.SDK_INT >= Q) {
      // TODO Find a proper way to set with NRI for later SDKs.
      throw new UnsupportedOperationException(
          "Newer SDKs must specify carrier aggregation by constructing an appropriate "
              + "NetworkRegistrationInfo and calling #setNetworkRegistrationInfoList instead");
    } else if (VERSION.SDK_INT == P) {
      reflector(ServiceStateReflector.class, serviceState).setIsUsingCarrierAggregation(value);
    }
    return this;
  }

  public ServiceStateBuilder setRoaming(boolean value) {
    serviceState.setRoaming(value);
    return this;
  }

  public ServiceStateBuilder setChannelNumber(int value) {
    serviceState.setChannelNumber(value);
    return this;
  }

  public ServiceStateBuilder setCellBandwidths(int[] value) {
    serviceState.setCellBandwidths(value);
    return this;
  }

  // TODO Find a proper way to set radio tech values.

  private void assertIsAtLeast(int sdk) {
    if (VERSION.SDK_INT < sdk) {
      throw new IllegalStateException(
          "This method is not available on SDK : " + RuntimeEnvironment.getApiLevel());
    }
  }

  @ForType(ServiceState.class)
  private interface ServiceStateReflector {

    @Accessor("mIsUsingCarrierAggregation")
    public void setIsUsingCarrierAggregation(boolean value);
  }
}
