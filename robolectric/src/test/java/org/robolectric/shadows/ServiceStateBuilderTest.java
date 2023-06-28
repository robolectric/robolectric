package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.telephony.ServiceState;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Test for {@link ShadowServiceState}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = P)
public class ServiceStateBuilderTest {

  static final int INT_VALUE = 10;
  static final String STRING_VALUE = "string";
  static final int[] INT_ARRAY = {1, 2, 3};

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setVoiceRegStateAndBuild_isSetInResultingObject() {
    // These public APIs expected to be available in all SDKs in range.
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setVoiceRegState(INT_VALUE).build();
    assertThat(serviceState.getVoiceRegState()).isEqualTo(INT_VALUE);
  }

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setDataRegStateAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setDataRegState(INT_VALUE).build();
    assertThat(serviceState.getDataRegState()).isEqualTo(INT_VALUE);
  }

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setIsManualSelectionAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setIsManualSelection(true).build();
    assertThat(serviceState.getIsManualSelection()).isTrue();
  }

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setRoamingAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setRoaming(true).build();
    assertThat(serviceState.getRoaming()).isTrue();
  }

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setEmergencyOnlyAndBuild_isSetInResultingObject() {
    ServiceState serviceState = ServiceStateBuilder.newBuilder().setEmergencyOnly(true).build();
    assertThat(serviceState.isEmergencyOnly()).isTrue();
  }

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setChannelNumberAndBuild_isSetInResultingObject() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setChannelNumber(INT_VALUE).build();
    assertThat(serviceState.getChannelNumber()).isEqualTo(INT_VALUE);
  }

  @Config(minSdk = P)
  @Test
  public void testServiceStateBuilder_setCellBandwidthsAndBuild_isSetInResultingObject() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setCellBandwidths(INT_ARRAY).build();
    assertThat(serviceState.getCellBandwidths()).isEqualTo(INT_ARRAY);
  }

  @Config(minSdk = Q)
  @Test
  public void testServiceStateBuilder_setNrFrequencyRangeAndBuild_isSetInResultingObject() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder().setNrFrequencyRange(INT_VALUE).build();
    assertThat(serviceState.getNrFrequencyRange()).isEqualTo(INT_VALUE);
  }

  @Config(minSdk = R)
  @Test
  public void testServiceStateBuilder_setOperatorNameAndBuild_isSetInResultingObject() {
    ServiceState serviceState =
        ServiceStateBuilder.newBuilder()
            .setOperatorName(STRING_VALUE, STRING_VALUE, STRING_VALUE)
            .build();
    assertThat(serviceState.getOperatorAlphaLong()).isEqualTo(STRING_VALUE);
    assertThat(serviceState.getOperatorAlphaShort()).isEqualTo(STRING_VALUE);
    assertThat(serviceState.getOperatorNumeric()).isEqualTo(STRING_VALUE);
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
}
