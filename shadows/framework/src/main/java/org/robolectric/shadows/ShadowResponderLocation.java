package org.robolectric.shadows;

import android.net.wifi.rtt.ResponderLocation;
import android.os.Build.VERSION_CODES;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link android.net.wifi.rtt.ResponderLocation}. */
@Implements(value = ResponderLocation.class, minSdk = VERSION_CODES.Q)
public class ShadowResponderLocation {
  public static ResponderLocation getNewInstance() {
    return ReflectionHelpers.callConstructor(
        ResponderLocation.class,
        ReflectionHelpers.ClassParameter.from(byte[].class, new byte[0]),
        ReflectionHelpers.ClassParameter.from(byte[].class, new byte[0]));
  }
  // LCI Subelement LCI state
  private Double altitude;
  private Double altitudeUncertainty;
  private Integer altitudeType;
  private Double latitudeDegrees;
  private Double latitudeUncertainty;
  private Double longitudeDegrees;
  private Double longitudeUncertainty;
  private Integer datum;
  private Integer lciVersion;
  private Boolean lciRegisteredLocationAgreement;

  // LCI Subelement Z state
  private Double heightAboveFloorMeters;
  private Double heightAboveFloorUncertaintyMeters;
  private Integer expectedToMove;
  private Double floorNumber;

  public void setAltitude(Double altitude) {
    this.altitude = altitude;
  }

  public void setAltitudeUncertainty(Double altitudeUncertainty) {
    this.altitudeUncertainty = altitudeUncertainty;
  }

  public void setAltitudeType(Integer altitudeType) {
    this.altitudeType = altitudeType;
  }

  public void setLatitude(Double latitudeDegrees) {
    this.latitudeDegrees = latitudeDegrees;
  }

  public void setLatitudeUncertainty(Double latitudeUncertainty) {
    this.latitudeUncertainty = latitudeUncertainty;
  }

  public void setLongitude(Double longitudeDegrees) {
    this.longitudeDegrees = longitudeDegrees;
  }

  public void setLongitudeUncertainty(Double longitudeUncertainty) {
    this.longitudeUncertainty = longitudeUncertainty;
  }

  public void setDatum(Integer datum) {
    this.datum = datum;
  }

  public void setLciVersion(Integer lciVersion) {
    this.lciVersion = lciVersion;
  }

  public void setLciRegisteredLocationAgreement(Boolean lciRegisteredLocationAgreement) {
    this.lciRegisteredLocationAgreement = lciRegisteredLocationAgreement;
  }

  public void setHeightAboveFloorMeters(Double heightAboveFloorMeters) {
    this.heightAboveFloorMeters = heightAboveFloorMeters;
  }

  public void setHeightAboveFloorUncertaintyMeters(Double heightAboveFloorUncertaintyMeters) {
    this.heightAboveFloorUncertaintyMeters = heightAboveFloorUncertaintyMeters;
  }

  public void setExpectedToMove(Integer expectedToMove) {
    this.expectedToMove = expectedToMove;
  }

  public void setFloorNumber(Double floorNumber) {
    this.floorNumber = floorNumber;
  }

  @Implementation
  public double getAltitude() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getAltitude(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.altitude;
  }

  @Implementation
  public double getAltitudeUncertainty() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getAltitudeUncertainty(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.altitudeUncertainty;
  }

  @Implementation
  public int getAltitudeType() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getAltitudeType(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.altitudeType;
  }

  @Implementation
  public double getLatitude() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getLatitude(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.latitudeDegrees;
  }

  @Implementation
  public double getLatitudeUncertainty() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getLatitudeUncertainty(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.latitudeUncertainty;
  }

  @Implementation
  public double getLongitude() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getLongitude(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.longitudeDegrees;
  }

  @Implementation
  public double getLongitudeUncertainty() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getLongitudeUncertainty(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.longitudeUncertainty;
  }

  @Implementation
  public int getDatum() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getDatum(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.datum;
  }

  @Implementation
  public int getLciVersion() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getLciVersion(): invoked on an invalid result: mIsLciValid = false.");
    }
    return this.lciVersion;
  }

  @Implementation
  public boolean getRegisteredLocationAgreementIndication() {
    if (!isLciSubelementValid()) {
      throw new IllegalStateException(
          "getRegisteredLocationAgreementIndication(): invoked on an invalid result: mIsLciValid ="
              + " false.");
    }
    return this.lciRegisteredLocationAgreement;
  }

  @Implementation
  public double getHeightAboveFloorMeters() {
    if (!isZaxisSubelementValid()) {
      throw new IllegalStateException(
          "getHeightAboveFloorMeters(): invoked on an invalid result: mIsZValid = false.");
    }
    return this.heightAboveFloorMeters;
  }

  @Implementation
  public double getHeightAboveFloorUncertaintyMeters() {
    if (!isZaxisSubelementValid()) {
      throw new IllegalStateException(
          "getHeightAboveFloorUncertaintyMeters(): invoked on an invalid result: mIsZValid ="
              + " false.");
    }
    return this.heightAboveFloorUncertaintyMeters;
  }

  @Implementation
  public int getExpectedToMove() {
    if (!isZaxisSubelementValid()) {
      throw new IllegalStateException(
          "getExpectedToMove(): invoked on an invalid result: mIsZValid =" + " false.");
    }
    return this.expectedToMove;
  }

  @Implementation
  public double getFloorNumber() {
    if (!isZaxisSubelementValid()) {
      throw new IllegalStateException(
          "getFloorNumber(): invoked on an invalid result: mIsZValid =" + " false.");
    }
    return this.floorNumber;
  }

  @Implementation
  public boolean isLciSubelementValid() {
    return this.altitude != null
        && this.latitudeDegrees != null
        && this.longitudeDegrees != null
        && this.longitudeUncertainty != null
        && this.latitudeDegrees != null
        && this.latitudeUncertainty != null
        && this.datum != null
        && this.lciVersion != null
        && this.lciRegisteredLocationAgreement != null
        && this.altitudeType != null;
  }

  @Implementation
  public boolean isZaxisSubelementValid() {
    return this.heightAboveFloorMeters != null
        && this.floorNumber != null
        && this.expectedToMove != null
        && this.heightAboveFloorUncertaintyMeters != null;
  }
}
