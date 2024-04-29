package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.telephony.emergency.EmergencyNumber;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link EmergencyNumberBuilder}. */
@Config(minSdk = Q)
@RunWith(AndroidJUnit4.class)
public final class EmergencyNumberBuilderTest {

  @Test
  public void testBuildEmergencyNumber() {
    EmergencyNumber emergencyNumber =
        EmergencyNumberBuilder.newBuilder("911", "us", "30")
            .setEmergencyServiceCategories(EmergencyNumber.EMERGENCY_SERVICE_CATEGORY_POLICE)
            .addEmergencyUrn("urn")
            .setEmergencyNumberSources(EmergencyNumber.EMERGENCY_NUMBER_SOURCE_DATABASE)
            .setEmergencyCallRouting(EmergencyNumber.EMERGENCY_CALL_ROUTING_NORMAL)
            .build();

    assertThat(emergencyNumber.getNumber()).isEqualTo("911");
    assertThat(emergencyNumber.getCountryIso()).isEqualTo("us");
    assertThat(emergencyNumber.getMnc()).isEqualTo("30");
    assertThat(emergencyNumber.getEmergencyServiceCategories())
        .containsExactly(EmergencyNumber.EMERGENCY_SERVICE_CATEGORY_POLICE);
    assertThat(emergencyNumber.getEmergencyUrns()).containsExactly("urn");
    assertThat(emergencyNumber.getEmergencyNumberSources())
        .containsExactly(EmergencyNumber.EMERGENCY_NUMBER_SOURCE_DATABASE);
    assertThat(emergencyNumber.getEmergencyCallRouting())
        .isEqualTo(EmergencyNumber.EMERGENCY_CALL_ROUTING_NORMAL);
  }
}
