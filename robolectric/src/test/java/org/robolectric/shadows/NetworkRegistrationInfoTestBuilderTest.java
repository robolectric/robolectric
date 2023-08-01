package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;

import android.telephony.CellIdentity;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.VoiceSpecificRegistrationInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

/** Test for {@link NetworkRegistrationInfoTestBuilder}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public class NetworkRegistrationInfoTestBuilderTest {

  private final List<Integer> intList = new ArrayList<>();
  @Mock protected CellIdentity cellIdentity;

  @Test
  public void testSetAccessNetworkTechnology_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setAccessNetworkTechnology(10).build();
    assertThat(networkRegistrationInfo.getAccessNetworkTechnology()).isEqualTo(10);
  }

  @Test
  public void testSetAvailableServices_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setAvailableServices(intList).build();
    assertThat(networkRegistrationInfo.getAvailableServices()).isEqualTo(intList);
  }

  @Test
  public void testSetCellIdentity_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setCellIdentity(cellIdentity).build();
    assertThat(networkRegistrationInfo.getCellIdentity()).isEqualTo(cellIdentity);
  }

  @Test
  public void testSetDataSpecificInfo_isSetInResultingObject() {
    DataSpecificRegistrationInfo dataSpecificRegistrationInfo =
        ReflectionHelpers.callConstructor(DataSpecificRegistrationInfo.class);
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder()
            .setDataSpecificInfo(dataSpecificRegistrationInfo)
            .build();
    assertThat(networkRegistrationInfo.getDataSpecificInfo())
        .isEqualTo(dataSpecificRegistrationInfo);
  }

  @Test
  public void testSetDomain_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setDomain(10).build();
    assertThat(networkRegistrationInfo.getDomain()).isEqualTo(10);
  }

  @Test
  public void testSetEmergencyOnly_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setEmergencyOnly(true).build();
    assertThat(networkRegistrationInfo.isEmergencyEnabled()).isEqualTo(true);
  }

  @Test
  public void testSetRegistrationState_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setRegistrationState(10).build();
    assertThat(networkRegistrationInfo.getRegistrationState()).isEqualTo(10);
  }

  @Test
  public void testSetRejectCause_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setRejectCause(10).build();
    assertThat(networkRegistrationInfo.getRejectCause()).isEqualTo(10);
  }

  @Test
  public void testSetRoamingType_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setRoamingType(10).build();
    assertThat(networkRegistrationInfo.getRoamingType()).isEqualTo(10);
  }

  @Test
  @Config(minSdk = R)
  public void testSetRegisteredPlmn_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setRegisteredPlmn("string").build();
    assertThat(networkRegistrationInfo.getRegisteredPlmn()).isEqualTo("string");
  }

  @Test
  public void testSetTransportType_isSetInResultingObject() {
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder().setTransportType(10).build();
    assertThat(networkRegistrationInfo.getTransportType()).isEqualTo(10);
  }

  @Test
  public void testSetVoiceSpecificInfo_isSetInResultingObject() {
    VoiceSpecificRegistrationInfo voiceSpecificRegistrationInfo =
        ReflectionHelpers.callConstructor(VoiceSpecificRegistrationInfo.class);
    NetworkRegistrationInfo networkRegistrationInfo =
        NetworkRegistrationInfoTestBuilder.newBuilder()
            .setVoiceSpecificInfo(voiceSpecificRegistrationInfo)
            .build();
    assertThat(networkRegistrationInfo.getVoiceSpecificInfo())
        .isEqualTo(voiceSpecificRegistrationInfo);
  }
}
