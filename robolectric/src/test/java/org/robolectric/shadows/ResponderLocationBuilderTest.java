package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.net.wifi.rtt.ResponderLocation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public final class ResponderLocationBuilderTest {

  @Test
  @Config(minSdk = Q)
  public void getNewInstance_wouldHaveEmptySubelements() {
    ResponderLocation responderLocation = ResponderLocationBuilder.newBuilder().build();

    assertThat(responderLocation.isLciSubelementValid()).isFalse();
    assertThat(responderLocation.isZaxisSubelementValid()).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void settingAllLciSubelementFieldsWithNoZaxisFields() {
    ResponderLocation responderLocation =
        ResponderLocationBuilder.newBuilder()
            .setAltitude(498.9)
            .setAltitudeUncertainty(2.0)
            .setLatitude(29.1)
            .setLatitudeUncertainty(3.4)
            .setLongitude(87.1)
            .setLongitudeUncertainty(5.4)
            .setAltitudeType(ResponderLocation.ALTITUDE_UNDEFINED)
            .setLciVersion(ResponderLocation.LCI_VERSION_1)
            .setLciRegisteredLocationAgreement(true)
            .setDatum(1)
            .build();

    assertThat(responderLocation.isLciSubelementValid()).isTrue();
    assertThat(responderLocation.isZaxisSubelementValid()).isFalse();
    assertThrows(IllegalStateException.class, () -> responderLocation.getFloorNumber());
  }

  @Test
  @Config(minSdk = Q)
  public void settingPartsOfLciSubelementFields() {
    ResponderLocation responderLocation =
        ResponderLocationBuilder.newBuilder()
            .setAltitude(498.9)
            .setAltitudeUncertainty(2.0)
            .setLatitude(29.1)
            .setLatitudeUncertainty(3.4)
            .setLongitude(87.1)
            .setLongitudeUncertainty(5.4)
            .setLciVersion(ResponderLocation.LCI_VERSION_1)
            .setLciRegisteredLocationAgreement(true)
            .setDatum(1)
            .build();

    assertThat(responderLocation.isLciSubelementValid()).isFalse();
    assertThat(responderLocation.isZaxisSubelementValid()).isFalse();
    assertThrows(IllegalStateException.class, () -> responderLocation.getAltitude());
    assertThrows(IllegalStateException.class, () -> responderLocation.getFloorNumber());
  }

  @Test
  @Config(minSdk = Q)
  public void settingAllLciSubelementAndZaxisSubelementFields() {
    ResponderLocation responderLocation =
        ResponderLocationBuilder.newBuilder()
            .setAltitude(498.9)
            .setAltitudeUncertainty(2.0)
            .setLatitude(29.1)
            .setLatitudeUncertainty(3.4)
            .setLongitude(87.1)
            .setLongitudeUncertainty(5.4)
            .setAltitudeType(ResponderLocation.ALTITUDE_METERS)
            .setLciVersion(ResponderLocation.LCI_VERSION_1)
            .setLciRegisteredLocationAgreement(true)
            .setDatum(1)
            .setHeightAboveFloorMeters(2.1)
            .setHeightAboveFloorUncertaintyMeters(0.1)
            .setFloorNumber(3.0)
            .setExpectedToMove(1)
            .build();

    assertThat(responderLocation.isLciSubelementValid()).isTrue();
    assertThat(responderLocation.isZaxisSubelementValid()).isTrue();
  }
}
