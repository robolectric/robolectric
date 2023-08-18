package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;
import androidx.annotation.RequiresApi;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Builder class to create instance of {@link ServiceState}. */
public class ServiceStateBuilder {
  private ServiceState serviceState = new ServiceState();

  public static ServiceStateBuilder newBuilder() {
    return new ServiceStateBuilder();
  }

  public static ServiceStateBuilder newBuilder(ServiceState serviceState) {
    ServiceStateBuilder builder = new ServiceStateBuilder();
    builder.serviceState = serviceState;
    return builder;
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

  /**
   * Use this method to control return value of {@link ServiceState#isUsingCarrierAggregation()} (up
   * to P). On APIs > P, use {@link ServiceStateBuilder#setNetworkRegistrationInfoList()}.
   */
  public ServiceStateBuilder setIsUsingCarrierAggregation(boolean value) {
    assertIsAtLeast(P);
    // {@link NetworkRegistrationInfo} was first made a @SystemApi in Q then finally exposed as
    // public in R. For SDK later than Q, call
    // {@link ServiceStateBuilder#setNetworkRegistrationInfoList} to set this value. Downstream test
    // code will have to specify NRIs in the builder to set this value. But the "actual"
    // implementation code under test would still be looking at the non-NRI getters
    // on {@link ServiceState}, assuming it's restricted to only public APIs.
    if (VERSION.SDK_INT >= Q) {
      throw new UnsupportedOperationException(
          "Newer SDKs must specify carrier aggregation by constructing an appropriate "
              + "NetworkRegistrationInfo and calling #setNetworkRegistrationInfoList instead");
    } else {
      reflector(ServiceStateReflector.class, serviceState).setIsUsingCarrierAggregation(value);
    }
    return this;
  }

  @RequiresApi(Q)
  public ServiceStateBuilder setNetworkRegistrationInfoList(List<NetworkRegistrationInfo> value) {
    assertIsAtLeast(Q);
    reflector(ServiceStateReflector.class, serviceState).setNetworkRegistrationInfos(value);
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

    @Accessor("mNetworkRegistrationInfos")
    public void setNetworkRegistrationInfos(List<NetworkRegistrationInfo> value);
  }
}
