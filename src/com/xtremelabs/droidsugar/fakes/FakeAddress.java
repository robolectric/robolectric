package com.xtremelabs.droidsugar.fakes;

import android.location.Address;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Address.class)
public class FakeAddress {
    private String addressLine1;
    private String locality;
    private String postalCode;
    private String adminArea;

    public void setAddressLine(int index, String line) {
        addressLine1 = line;
    }

    public String getAddressLine(int index) {
        return addressLine1;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getLocality() {
        return locality;
    }

    public String getAdminArea() {
        return adminArea;
    }

    public void setAdminArea(String adminArea) {
        this.adminArea = adminArea;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
}
