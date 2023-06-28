package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION;
import android.telephony.CellIdentity;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.VoiceSpecificRegistrationInfo;
import androidx.annotation.RequiresApi;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Builder class to create instance of {@link NetworkRegistrationInfo}.
 *
 * <p>NRI was first made a @SystemApi in Q then finally exposed as public in R.
 *
 * <p>This builder class does not extend {@link NetworkRegistrationInfo.Builder}. It uses {@link
 * NetworkRegistrationInfo.Builder} and some additional APIs to set NRI private fields.
 */
@RequiresApi(Q)
public class NetworkRegistrationInfoTestBuilder {

  private final NetworkRegistrationInfo.Builder buider = new NetworkRegistrationInfo.Builder();

  private VoiceSpecificRegistrationInfo voiceSpecificInfo;
  private DataSpecificRegistrationInfo dataSpecificInfo;
  private int roamingType;

  public static NetworkRegistrationInfoTestBuilder newBuilder() {
    return new NetworkRegistrationInfoTestBuilder();
  }

  public NetworkRegistrationInfo build() {
    NetworkRegistrationInfo networkRegistrationInfo = buider.build();
    if (VERSION.SDK_INT < Q) {
      throw new IllegalStateException(
          "NetworkRegistrationInfo not available on SDK : " + RuntimeEnvironment.getApiLevel());
    } else if (VERSION.SDK_INT < TIRAMISU) {
      reflector(NetworkRegistrationInfoReflector.class, networkRegistrationInfo)
          .setVoiceSpecificInfo(voiceSpecificInfo);
      reflector(NetworkRegistrationInfoReflector.class, networkRegistrationInfo)
          .setDataSpecificInfo(dataSpecificInfo);
    }
    networkRegistrationInfo.setRoamingType(roamingType);
    return networkRegistrationInfo;
  }

  public NetworkRegistrationInfoTestBuilder setAccessNetworkTechnology(int value) {
    buider.setAccessNetworkTechnology(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setAvailableServices(List<Integer> value) {
    buider.setAvailableServices(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setCellIdentity(CellIdentity value) {
    buider.setCellIdentity(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setDomain(int value) {
    buider.setDomain(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setEmergencyOnly(boolean value) {
    buider.setEmergencyOnly(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setRegisteredPlmn(String value) {
    if (VERSION.SDK_INT == Q) {
      throw new IllegalStateException(
          "Registered PLMN is not available on SDK : " + RuntimeEnvironment.getApiLevel());
    } else {
      buider.setRegisteredPlmn(value);
    }
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setRegistrationState(int value) {
    buider.setRegistrationState(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setRejectCause(int value) {
    buider.setRejectCause(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setTransportType(int value) {
    buider.setTransportType(value);
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setDataSpecificInfo(
      DataSpecificRegistrationInfo value) {
    if (VERSION.SDK_INT >= TIRAMISU) {
      buider.setDataSpecificInfo(value);
    } else {
      dataSpecificInfo = value;
    }
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setVoiceSpecificInfo(
      VoiceSpecificRegistrationInfo value) {
    if (VERSION.SDK_INT >= TIRAMISU) {
      buider.setVoiceSpecificInfo(value);
    } else {
      voiceSpecificInfo = value;
    }
    return this;
  }

  public NetworkRegistrationInfoTestBuilder setRoamingType(int value) {
    roamingType = value;
    return this;
  }

  @ForType(NetworkRegistrationInfo.class)
  private interface NetworkRegistrationInfoReflector {

    @Accessor("mDataSpecificInfo")
    public void setDataSpecificInfo(DataSpecificRegistrationInfo value);

    @Accessor("mVoiceSpecificInfo")
    public void setVoiceSpecificInfo(VoiceSpecificRegistrationInfo value);
  }
}
