package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Build;
import android.telephony.ims.ImsRegistrationAttributes;
import android.telephony.ims.SipDetails;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test for {@link ImsRegistrationAttributesBuilder} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.S)
public class ImsRegistrationAttributesBuilderTest {

  private static final int REGISTRATION_TECH = ImsRegistrationImplBase.REGISTRATION_TECH_LTE;
  private static final ImmutableSet<String> FEATURE_TAGS = ImmutableSet.of("mmtel");

  @Test
  public void build_defaultValues() {
    ImsRegistrationAttributes attributes =
        ImsRegistrationAttributesBuilder.newBuilder(REGISTRATION_TECH).build();

    assertThat(attributes.getRegistrationTechnology()).isEqualTo(REGISTRATION_TECH);
    assertThat(attributes.getFeatureTags()).isEmpty();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S, maxSdk = Build.VERSION_CODES.TIRAMISU)
  public void build_preSdkU_setsAllFields() {
    ImsRegistrationAttributes attributes =
        ImsRegistrationAttributesBuilder.newBuilder(REGISTRATION_TECH)
            .setFeatureTags(FEATURE_TAGS)
            .build();

    assertThat(attributes.getRegistrationTechnology()).isEqualTo(REGISTRATION_TECH);
    assertThat(attributes.getFeatureTags()).containsExactlyElementsIn(FEATURE_TAGS);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void build_sdkU_setsSipDetails() {
    SipDetails sipDetails = new SipDetails.Builder(SipDetails.METHOD_REGISTER).build();

    ImsRegistrationAttributes attributes =
        ImsRegistrationAttributesBuilder.newBuilder(REGISTRATION_TECH)
            .setFeatureTags(FEATURE_TAGS)
            .setSipDetails(sipDetails)
            .build();

    assertThat(attributes.getRegistrationTechnology()).isEqualTo(REGISTRATION_TECH);
    assertThat(attributes.getFeatureTags()).containsExactlyElementsIn(FEATURE_TAGS);
    assertThat(attributes.getSipDetails()).isEqualTo(sipDetails);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.VANILLA_ICE_CREAM)
  public void build_fromSdkV() {
    SipDetails sipDetails = new SipDetails.Builder(SipDetails.METHOD_REGISTER).build();

    ImsRegistrationAttributes attributes =
        ImsRegistrationAttributesBuilder.newBuilder(REGISTRATION_TECH)
            .setFeatureTags(FEATURE_TAGS)
            .setSipDetails(sipDetails)
            .setFlagRegistrationTypeEmergency()
            .setFlagVirtualRegistrationForEmergencyCall()
            .build();

    assertThat(attributes.getRegistrationTechnology()).isEqualTo(REGISTRATION_TECH);
    assertThat(attributes.getFeatureTags()).containsExactlyElementsIn(FEATURE_TAGS);
    assertThat(attributes.getSipDetails()).isEqualTo(sipDetails);
    // These are populated by the real builder due to setFlagRegistrationTypeEmergency() and
    // setFlagVirtualRegistrationForEmergencyCall()
    int expectedFlags =
        ImsRegistrationAttributes.ATTR_REGISTRATION_TYPE_EMERGENCY
            | ImsRegistrationAttributes.ATTR_VIRTUAL_FOR_ANONYMOUS_EMERGENCY_CALL;
    assertThat(attributes.getAttributeFlags()).isEqualTo(expectedFlags);
  }
}
