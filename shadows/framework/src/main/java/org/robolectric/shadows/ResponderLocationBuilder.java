package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.net.wifi.rtt.ResponderLocation;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Builder for {@link ResponderLocation} */
@SuppressWarnings("CanIgnoreReturnValueSuggester")
public class ResponderLocationBuilder {
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

  private ResponderLocationBuilder() {}

  public static ResponderLocationBuilder newBuilder() {
    return new ResponderLocationBuilder();
  }

  public ResponderLocationBuilder setAltitude(double altitude) {
    this.altitude = altitude;
    return this;
  }

  public ResponderLocationBuilder setAltitudeUncertainty(double altitudeUncertainty) {
    this.altitudeUncertainty = altitudeUncertainty;
    return this;
  }

  public ResponderLocationBuilder setAltitudeType(int altitudeType) {
    this.altitudeType = altitudeType;
    return this;
  }

  public ResponderLocationBuilder setLatitude(double latitudeDegrees) {
    this.latitudeDegrees = latitudeDegrees;
    return this;
  }

  public ResponderLocationBuilder setLatitudeUncertainty(double latitudeUncertainty) {
    this.latitudeUncertainty = latitudeUncertainty;
    return this;
  }

  public ResponderLocationBuilder setLongitude(double longitudeDegrees) {
    this.longitudeDegrees = longitudeDegrees;
    return this;
  }

  public ResponderLocationBuilder setLongitudeUncertainty(double longitudeUncertainty) {
    this.longitudeUncertainty = longitudeUncertainty;
    return this;
  }

  public ResponderLocationBuilder setDatum(int datum) {
    this.datum = datum;
    return this;
  }

  public ResponderLocationBuilder setLciVersion(int lciVersion) {
    this.lciVersion = lciVersion;
    return this;
  }

  public ResponderLocationBuilder setLciRegisteredLocationAgreement(
      Boolean lciRegisteredLocationAgreement) {
    this.lciRegisteredLocationAgreement = lciRegisteredLocationAgreement;
    return this;
  }

  public ResponderLocationBuilder setHeightAboveFloorMeters(double heightAboveFloorMeters) {
    this.heightAboveFloorMeters = heightAboveFloorMeters;
    return this;
  }

  public ResponderLocationBuilder setHeightAboveFloorUncertaintyMeters(
      double heightAboveFloorUncertaintyMeters) {
    this.heightAboveFloorUncertaintyMeters = heightAboveFloorUncertaintyMeters;
    return this;
  }

  public ResponderLocationBuilder setExpectedToMove(int expectedToMove) {
    this.expectedToMove = expectedToMove;
    return this;
  }

  public ResponderLocationBuilder setFloorNumber(double floorNumber) {
    this.floorNumber = floorNumber;
    return this;
  }

  public ResponderLocation build() {
    ResponderLocation result = Shadow.newInstanceOf(ResponderLocation.class);

    ResponderLocationReflector locationResponderReflector =
        reflector(ResponderLocationReflector.class, result);

    locationResponderReflector.setAltitude(this.altitude == null ? 0 : this.altitude);
    locationResponderReflector.setAltitudeType(this.altitudeType == null ? 0 : this.altitudeType);
    locationResponderReflector.setAltitudeUncertainty(
        this.altitudeUncertainty == null ? 0 : this.altitudeUncertainty);
    locationResponderReflector.setLatitude(this.latitudeDegrees == null ? 0 : this.latitudeDegrees);
    locationResponderReflector.setLatitudeUncertainty(
        this.latitudeUncertainty == null ? 0 : this.latitudeUncertainty);
    locationResponderReflector.setLongitude(
        this.longitudeDegrees == null ? 0 : this.longitudeDegrees);
    locationResponderReflector.setLongitudeUncertainty(
        this.longitudeUncertainty == null ? 0 : this.longitudeUncertainty);
    locationResponderReflector.setDatum(this.datum == null ? 0 : this.datum);
    locationResponderReflector.setLciVersion(this.lciVersion == null ? 0 : this.lciVersion);
    locationResponderReflector.setLciRegisteredLocationAgreement(
        this.lciRegisteredLocationAgreement != null && this.lciRegisteredLocationAgreement);
    locationResponderReflector.setHeightAboveFloorMeters(
        this.heightAboveFloorMeters == null ? 0 : this.heightAboveFloorMeters);
    locationResponderReflector.setHeightAboveFloorUncertaintyMeters(
        this.heightAboveFloorUncertaintyMeters == null
            ? 0
            : this.heightAboveFloorUncertaintyMeters);
    locationResponderReflector.setExpectedToMove(
        this.expectedToMove == null ? 0 : this.expectedToMove);
    locationResponderReflector.setFloorNumber(this.floorNumber == null ? 0 : this.floorNumber);

    locationResponderReflector.setIsLciValid(
        this.altitude != null
            && this.latitudeDegrees != null
            && this.latitudeUncertainty != null
            && this.longitudeDegrees != null
            && this.longitudeUncertainty != null
            && this.datum != null
            && this.lciVersion != null
            && this.lciRegisteredLocationAgreement != null
            && this.altitudeType != null);

    locationResponderReflector.setIsZValid(
        this.heightAboveFloorMeters != null
            && this.floorNumber != null
            && this.expectedToMove != null
            && this.heightAboveFloorUncertaintyMeters != null);

    return result;
  }

  @ForType(ResponderLocation.class)
  interface ResponderLocationReflector {

    @Accessor("mAltitude")
    void setAltitude(double altitude);

    @Accessor("mAltitudeUncertainty")
    void setAltitudeUncertainty(double altitudeUncertainty);

    @Accessor("mAltitudeType")
    void setAltitudeType(int altitudeType);

    @Accessor("mLatitude")
    void setLatitude(double latitudeDegrees);

    @Accessor("mLatitudeUncertainty")
    void setLatitudeUncertainty(double latitudeUncertainty);

    @Accessor("mLongitude")
    void setLongitude(double longitudeDegrees);

    @Accessor("mLongitudeUncertainty")
    void setLongitudeUncertainty(double longitudeUncertainty);

    @Accessor("mDatum")
    void setDatum(int datum);

    @Accessor("mLciVersion")
    void setLciVersion(int lciVersion);

    @Accessor("mLciRegisteredLocationAgreement")
    void setLciRegisteredLocationAgreement(boolean lciRegisteredLocationAgreement);

    @Accessor("mHeightAboveFloorMeters")
    void setHeightAboveFloorMeters(double heightAboveFloorMeters);

    @Accessor("mHeightAboveFloorUncertaintyMeters")
    void setHeightAboveFloorUncertaintyMeters(double heightAboveFloorUncertaintyMeters);

    @Accessor("mExpectedToMove")
    void setExpectedToMove(int expectedToMove);

    @Accessor("mFloorNumber")
    void setFloorNumber(double floorNumber);

    @Accessor("mIsLciValid")
    void setIsLciValid(boolean isLciValid);

    @Accessor("mIsZValid")
    void setIsZValid(boolean isZValid);
  }
}
