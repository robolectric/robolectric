package com.xtremelabs.droidsugar.fakes;

import android.location.Address;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Address.class)
public class FakeAddress {
    private String addressLine1;
    private String locality;
    private String postalCode;
    private String adminArea;

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
}
