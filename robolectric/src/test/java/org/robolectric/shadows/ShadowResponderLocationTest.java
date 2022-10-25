package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Shadows.shadowOf;

import android.net.wifi.rtt.ResponderLocation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public final class ShadowResponderLocationTest {

  @Test
  @Config(minSdk = Q)
  public void getNewInstance_wouldHaveEmptySubelements() {
    ResponderLocation responderLocation = ShadowResponderLocation.getNewInstance();

    assertThat(responderLocation.isLciSubelementValid()).isFalse();
    assertThat(responderLocation.isZaxisSubelementValid()).isFalse();
  }

  @Test
  @Config(minSdk = Q)
  public void settingAllLciSubelementFieldsWithNoZaxisFields() {
    ResponderLocation responderLocation = ShadowResponderLocation.getNewInstance();
    ShadowResponderLocation shadowResponderLocation = shadowOf(responderLocation);
    shadowResponderLocation.setAltitude(498.9);
    shadowResponderLocation.setAltitudeUncertainty(2.0);
    shadowResponderLocation.setLatitude(29.1);
    shadowResponderLocation.setLatitudeUncertainty(3.4);
    shadowResponderLocation.setLongitude(87.1);
    shadowResponderLocation.setLongitudeUncertainty(5.4);
    shadowResponderLocation.setAltitudeType(ResponderLocation.ALTITUDE_UNDEFINED);
    shadowResponderLocation.setLciVersion(ResponderLocation.LCI_VERSION_1);
    shadowResponderLocation.setLciRegisteredLocationAgreement(true);
    shadowResponderLocation.setDatum(1);

    assertThat(responderLocation.isLciSubelementValid()).isTrue();
    assertThat(responderLocation.isZaxisSubelementValid()).isFalse();
    assertThrows(IllegalStateException.class, () -> responderLocation.getFloorNumber());
    assertLciFieldsAreEqual(responderLocation, shadowResponderLocation);
  }

  @Test
  @Config(minSdk = Q)
  public void settingPartsOfLciSubelementFields() {
    ResponderLocation responderLocation = ShadowResponderLocation.getNewInstance();
    ShadowResponderLocation shadowResponderLocation = shadowOf(responderLocation);
    shadowResponderLocation.setAltitude(498.9);
    shadowResponderLocation.setAltitudeUncertainty(2.0);
    shadowResponderLocation.setLatitude(29.1);
    shadowResponderLocation.setLatitudeUncertainty(3.4);
    shadowResponderLocation.setLongitude(87.1);
    shadowResponderLocation.setLongitudeUncertainty(5.4);
    shadowResponderLocation.setLciVersion(ResponderLocation.LCI_VERSION_1);
    shadowResponderLocation.setLciRegisteredLocationAgreement(true);
    shadowResponderLocation.setDatum(1);

    assertThat(responderLocation.isLciSubelementValid()).isFalse();
    assertThat(responderLocation.isZaxisSubelementValid()).isFalse();
    assertThrows(IllegalStateException.class, () -> responderLocation.getAltitude());
    assertThrows(IllegalStateException.class, () -> responderLocation.getFloorNumber());
  }

  @Test
  @Config(minSdk = Q)
  public void settingAllLciSubelementAndZaxisSubelementFields() {
    ResponderLocation responderLocation = ShadowResponderLocation.getNewInstance();
    ShadowResponderLocation shadowResponderLocation = shadowOf(responderLocation);
    shadowResponderLocation.setAltitude(498.9);
    shadowResponderLocation.setAltitudeUncertainty(2.0);
    shadowResponderLocation.setLatitude(29.1);
    shadowResponderLocation.setLatitudeUncertainty(3.4);
    shadowResponderLocation.setLongitude(87.1);
    shadowResponderLocation.setLongitudeUncertainty(5.4);
    shadowResponderLocation.setAltitudeType(ResponderLocation.ALTITUDE_METERS);
    shadowResponderLocation.setLciVersion(ResponderLocation.LCI_VERSION_1);
    shadowResponderLocation.setLciRegisteredLocationAgreement(true);
    shadowResponderLocation.setDatum(1);
    shadowResponderLocation.setHeightAboveFloorMeters(2.1);
    shadowResponderLocation.setHeightAboveFloorUncertaintyMeters(0.1);
    shadowResponderLocation.setFloorNumber(3.0);
    shadowResponderLocation.setExpectedToMove(1);

    assertThat(responderLocation.isLciSubelementValid()).isTrue();
    assertThat(responderLocation.isZaxisSubelementValid()).isTrue();
    assertZaxisFieldsAreEqual(responderLocation, shadowResponderLocation);
  }

  private void assertLciFieldsAreEqual(
      ResponderLocation responderLocation, ShadowResponderLocation shadowResponderLocation) {
    assertThat(responderLocation.getAltitude()).isEqualTo(shadowResponderLocation.getAltitude());
    assertThat(responderLocation.getAltitudeUncertainty())
        .isEqualTo(shadowResponderLocation.getAltitudeUncertainty());
    assertThat(responderLocation.getLatitude()).isEqualTo(shadowResponderLocation.getLatitude());
    assertThat(responderLocation.getLatitudeUncertainty())
        .isEqualTo(shadowResponderLocation.getLatitudeUncertainty());
    assertThat(responderLocation.getLongitude()).isEqualTo(shadowResponderLocation.getLongitude());
    assertThat(responderLocation.getLongitudeUncertainty())
        .isEqualTo(shadowResponderLocation.getLongitudeUncertainty());
    assertThat(responderLocation.getAltitudeType())
        .isEqualTo(shadowResponderLocation.getAltitudeType());
    assertThat(responderLocation.getLciVersion())
        .isEqualTo(shadowResponderLocation.getLciVersion());
    assertThat(responderLocation.getRegisteredLocationAgreementIndication())
        .isEqualTo(shadowResponderLocation.getRegisteredLocationAgreementIndication());
    assertThat(responderLocation.getDatum()).isEqualTo(shadowResponderLocation.getDatum());
  }

  private void assertZaxisFieldsAreEqual(
      ResponderLocation responderLocation, ShadowResponderLocation shadowResponderLocation) {
    assertThat(responderLocation.getHeightAboveFloorMeters())
        .isEqualTo(shadowResponderLocation.getHeightAboveFloorMeters());
    assertThat(responderLocation.getHeightAboveFloorUncertaintyMeters())
        .isEqualTo(shadowResponderLocation.getHeightAboveFloorUncertaintyMeters());
    assertThat(responderLocation.getFloorNumber())
        .isEqualTo(shadowResponderLocation.getFloorNumber());
    assertThat(responderLocation.getExpectedToMove())
        .isEqualTo(shadowResponderLocation.getExpectedToMove());
  }
}
