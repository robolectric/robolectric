package com.xtremelabs.robolectric.shadows;

import android.location.Address;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;


@SuppressWarnings({"UnusedDeclaration"})
@Implements(Address.class)
public class ShadowAddress {
    private String addressLine1;
    private String locality;
    private String postalCode;
    private String adminArea;
    private String countryCode;
    private double longitude;
    private double latitude;
    private boolean hasLatitude;
    private boolean hasLongitude;

    @Implementation
    public double getLatitude() {
        return latitude;
    }

    @Implementation
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Implementation
    public double getLongitude() {
        return longitude;
    }

    @Implementation
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Implementation
    public void setAddressLine(int index, String line) {
        addressLine1 = line;
    }

    @Implementation
    public String getAddressLine(int index) {
        return addressLine1;
    }

    @Implementation
    public void setLocality(String locality) {
        this.locality = locality;
    }

    @Implementation
    public String getLocality() {
        return locality;
    }

    @Implementation
    public String getAdminArea() {
        return adminArea;
    }

    @Implementation
    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    @Implementation
    public String getPostalCode() {
        return postalCode;
    }

    @Implementation
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Implementation
    public String getCountryCode() {
        return countryCode;
    }

    @Implementation
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    @Implementation
    public boolean hasLatitude() {
        return hasLatitude;
    }

    @Implementation
    public boolean hasLongitude() {
        return hasLongitude;
    }

    public void setSimulatedHasLatLong(boolean hasLatitude, boolean hasLongitude) {
        this.hasLatitude = hasLatitude;
        this.hasLongitude = hasLongitude;
    }
}
