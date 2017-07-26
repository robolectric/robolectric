package org.robolectric.shadows.maps;

import android.location.Address;
import android.location.Geocoder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(Geocoder.class)
public class ShadowGeocoder {
  private String addressLine1;
  private String city;
  private String state;
  private String zip;
  private String countryCode;
  private boolean wasCalled;
  private double lastLatitude;
  private double lastLongitude;
  private String lastLocationName;
  private double simulatedLatitude;
  private double simulatedLongitude;
  private boolean shouldSimulateGeocodeException;
  private boolean hasLatitude;
  private boolean hasLongitude;
  private boolean returnNoResults = false;
  private boolean didResolution;

  @Implementation
  public List<Address> getFromLocation(double latitude, double longitude, int maxResults) throws IOException {
    wasCalled = true;
    this.lastLatitude = latitude;
    this.lastLongitude = longitude;
    if (shouldSimulateGeocodeException) {
      throw new IOException("Simulated geocode exception");
    }
    Address address = makeAddress();
    address.setAddressLine(0, addressLine1);
    address.setLocality(city);
    address.setAdminArea(state);
    address.setPostalCode(zip);
    address.setCountryCode(countryCode);
    return Arrays.asList(address);
  }

  @Implementation
  public List<Address> getFromLocationName(String locationName, int maxResults) throws IOException {
    didResolution = true;
    this.lastLocationName = locationName;
    if (shouldSimulateGeocodeException) {
      throw new IOException("Simulated geocode exception");
    }
    if (returnNoResults) {
      return new ArrayList<>();
    } else {
      return Arrays.asList(makeAddress());
    }
  }

  private Address makeAddress() {
    Address address = new Address(Locale.getDefault());
    address.setLatitude(simulatedLatitude);
    address.setLongitude(simulatedLongitude);
    ReflectionHelpers.setField(address, "mHasLatitude", hasLatitude);
    ReflectionHelpers.setField(address, "mHasLongitude", hasLongitude);
    return address;
  }

  /**
   * Sets up a simulated response for {@link #getFromLocation(double, double, int)}
   *
   * @param address     the address for the response
   * @param city        the city for the response
   * @param state       the state for the response
   * @param zip         the zip code for the response
   * @param countryCode the country code for the response
   */
  public void setSimulatedResponse(String address, String city, String state, String zip, String countryCode) {
    this.addressLine1 = address;
    this.city = city;
    this.state = state;
    this.zip = zip;
    this.countryCode = countryCode;
  }


  /**
   * Sets up a simulated response for {@link #getFromLocationName(String, int)}}
   *
   * @param lat latitude for simulated response
   * @param lng longitude for simulated response
   */
  public void setSimulatedLatLong(double lat, double lng) {
    this.simulatedLatitude = lat;
    this.simulatedLongitude = lng;
  }

  /**
   * Sets a flag to indicate whether or not {@link #getFromLocationName(String, int)} should throw an exception to
   * simulate a failure.
   *
   * @param shouldSimulateException whether or not an exception should be thrown from {@link #getFromLocationName(String, int)}
   */
  public void setShouldSimulateGeocodeException(boolean shouldSimulateException) {
    this.shouldSimulateGeocodeException = true;
  }

  /**
   * Indicates whether {@link #getFromLocation(double, double, int)} was called.
   *
   * @return whether {@link #getFromLocation(double, double, int)} was called.
   */
  public boolean wasGetFromLocationCalled() {
    return wasCalled;
  }

  public double getLastLongitude() {
    return lastLongitude;
  }

  public double getLastLatitude() {
    return lastLatitude;
  }

  public String getLastLocationName() {
    return lastLocationName;
  }

  public void setSimulatedHasLatLong(boolean hasLatitude, boolean hasLongitude) {
    this.hasLatitude = hasLatitude;
    this.hasLongitude = hasLongitude;
  }

  public void setReturnNoResults(boolean returnNoResults) {
    this.returnNoResults = returnNoResults;
  }

  public boolean didResolution() {
    return didResolution;
  }
}
