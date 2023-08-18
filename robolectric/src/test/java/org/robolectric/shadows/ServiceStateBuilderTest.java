package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.telephony.NetworkRegistrationInfo;
import android.telephony.ServiceState;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Test for {@link ShadowServiceState}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ServiceStateBuilderTest {

  static final int[] INT_ARRAY = {1, 2, 3};

  @Test
  public void testServiceStateBuilder_setVoiceRegStateAndBuild_isSetInResultingObject() {
    // These public APIs expected to be available in all SDKs in range.
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setVoiceRegState(10).build();
    assertThat(serviceState.getVoiceRegState()).isEqualTo(10);
  }

  @Test
  public void testServiceStateBuilder_setDataRegStateAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setDataRegState(10).build();
    assertThat(serviceState.getDataRegState()).isEqualTo(10);
  }

  @Test
  public void testServiceStateBuilder_setIsManualSelectionAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setIsManualSelection(true).build();
    assertThat(serviceState.getIsManualSelection()).isTrue();
  }

  @Test
  public void testServiceStateBuilder_setRoamingAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setRoaming(true).build();
    assertThat(serviceState.getRoaming()).isTrue();
  }

  @Test
  public void testServiceStateBuilder_setEmergencyOnlyAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setEmergencyOnly(true).build();
    assertThat(serviceState.isEmergencyOnly()).isTrue();
  }

  @Test
  public void testServiceStateBuilder_setChannelNumberAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setChannelNumber(10).build();
    assertThat(serviceState.getChannelNumber()).isEqualTo(10);
  }

  @Test
  public void testServiceStateBuilder_setCellBandwidthsAndBuild_isSetInResultingObject() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setCellBandwidths(INT_ARRAY).build();
    assertThat(serviceState.getCellBandwidths()).isEqualTo(INT_ARRAY);
  }

  @Config(minSdk = Q)
  @Test
  public void testServiceStateBuilder_setNrFrequencyRangeAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setNrFrequencyRange(10).build();
    assertThat(serviceState.getNrFrequencyRange()).isEqualTo(10);
  }

  @Config(minSdk = R)
  @Test
  public void testServiceStateBuilder_setOperatorNameAndBuild_isSetInResultingObject() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setOperatorName("string", "string", "string").build();
    assertThat(serviceState.getOperatorAlphaLong()).isEqualTo("string");
    assertThat(serviceState.getOperatorAlphaShort()).isEqualTo("string");
    assertThat(serviceState.getOperatorNumeric()).isEqualTo("string");
  }

  @Config(minSdk = R)
  @Test
  public void testServiceStateBuilder_setIwlanPreferredAndBuild_isSetInResultingObjectField() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setIwlanPreferred(true).build();
    assertThat((boolean) ReflectionHelpers.getField(serviceState, "mIsIwlanPreferred")).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void
      testServiceStateBuilder_setDataRoamingFromRegistrationAndBuild_isSetInResultingObjectField() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setDataRoamingFromRegistration(true).build();
    assertThat((boolean) ReflectionHelpers.getField(serviceState, "mIsDataRoamingFromRegistration"))
        .isTrue();
  }

  @Config(sdk = {P})
  @Test
  public void
      testServiceStateBuilder_setIsUsingCarrierAggregationAndBuild_isSetInResultingObjectField() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setIsUsingCarrierAggregation(true).build();
    assertThat((boolean) ReflectionHelpers.getField(serviceState, "mIsUsingCarrierAggregation"))
        .isTrue();
  }

  @Config(minSdk = Q)
  @Test
  public void
      testServiceStateBuilder_setNetworkRegistrationInfoListAndBuild_isSetInResultingObjectField() {
    List<NetworkRegistrationInfo> networkRegistrationInfoList = new ArrayList<>();
    networkRegistrationInfoList.add(new NetworkRegistrationInfo.Builder().build());
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder()
            .setNetworkRegistrationInfoList(networkRegistrationInfoList)
            .build();
    assertThat(serviceState.getNetworkRegistrationInfoList())
        .isEqualTo(networkRegistrationInfoList);
  }
}
