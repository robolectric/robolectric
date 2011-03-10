/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (C) 2011 Eric Bowman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xtremelabs.robolectric.shadows;

import android.location.Address;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test the main functionalities of the AddressTest.
 */
@RunWith(WithTestDefaultsRunner.class)
public class AddressTest {

    @Test
    public void testConstructor() {
        new Address(Locale.ENGLISH);

        new Address(Locale.FRANCE);

        new Address(null);
    }

    @Test
    public void testDescribeContents() {
        Address address = new Address(Locale.GERMAN);

        assertEquals(0, address.describeContents());

        Bundle extras = new Bundle();
        extras.putParcelable("key1", new MockParcelable());
        address.setExtras(extras);

        assertEquals(extras.describeContents(), address.describeContents());
    }

    @Test
    public void testAccessAdminArea() {
        Address address = new Address(Locale.ITALY);

        String adminArea = "CA";
        address.setAdminArea(adminArea);
        assertEquals(adminArea, address.getAdminArea());

        address.setAdminArea(null);
        assertNull(address.getAdminArea());
    }

    @Test
    public void testAccessCountryCode() {
        Address address = new Address(Locale.JAPAN);

        String countryCode = "US";
        address.setCountryCode(countryCode);
        assertEquals(countryCode, address.getCountryCode());

        address.setCountryCode(null);
        assertNull(address.getCountryCode());
    }

    @Test
    public void testAccessCountryName() {
        Address address = new Address(Locale.KOREA);

        String countryName = "China";
        address.setCountryName(countryName);
        assertEquals(countryName, address.getCountryName());

        address.setCountryName(null);
        assertNull(address.getCountryName());
    }

    @Test
    public void testAccessExtras() {
        Address address = new Address(Locale.TAIWAN);

        Bundle extras = new Bundle();
        extras.putBoolean("key1", false);
        byte b = 10;
        extras.putByte("key2", b);

        address.setExtras(extras);
        Bundle actual = address.getExtras();
        assertFalse(actual.getBoolean("key1"));
        assertEquals(b, actual.getByte("key2"));

        address.setExtras(null);
        assertNull(address.getExtras());
    }

    @Test
    public void testAccessFeatureName() {
        Address address = new Address(Locale.SIMPLIFIED_CHINESE);

        String featureName = "Golden Gate Bridge";
        address.setFeatureName(featureName);
        assertEquals(featureName, address.getFeatureName());

        address.setFeatureName(null);
        assertNull(address.getFeatureName());
    }

    @Test
    public void testAccessLatitude() {
        Address address = new Address(Locale.CHINA);
        assertFalse(address.hasLatitude());

        double latitude = 1.23456789;
        address.setLatitude(latitude);
        assertTrue(address.hasLatitude());
        assertEquals(latitude, address.getLatitude(), 0);

        address.clearLatitude();
        assertFalse(address.hasLatitude());
        try {
            address.getLatitude();
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testAccessLongitude() {
        Address address = new Address(Locale.CHINA);
        assertFalse(address.hasLongitude());

        double longitude = 1.23456789;
        address.setLongitude(longitude);
        assertTrue(address.hasLongitude());
        assertEquals(longitude, address.getLongitude(), 0);

        address.clearLongitude();
        assertFalse(address.hasLongitude());
        try {
            address.getLongitude();
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testAccessPhone() {
        Address address = new Address(Locale.CHINA);

        String phone = "+86-13512345678";
        address.setPhone(phone);
        assertEquals(phone, address.getPhone());

        address.setPhone(null);
        assertNull(address.getPhone());
    }

    @Test
    public void testAccessPostalCode() {
        Address address = new Address(Locale.CHINA);

        String postalCode = "93110";
        address.setPostalCode(postalCode);
        assertEquals(postalCode, address.getPostalCode());

        address.setPostalCode(null);
        assertNull(address.getPostalCode());
    }

    @Test
    public void testAccessThoroughfare() {
        Address address = new Address(Locale.CHINA);

        String thoroughfare = "1600 Ampitheater Parkway";
        address.setThoroughfare(thoroughfare);
        assertEquals(thoroughfare, address.getThoroughfare());

        address.setThoroughfare(null);
        assertNull(address.getThoroughfare());
    }

    @Test
    public void testAccessUrl() {
        Address address = new Address(Locale.CHINA);

        String Url = "Url";
        address.setUrl(Url);
        assertEquals(Url, address.getUrl());

        address.setUrl(null);
        assertNull(address.getUrl());
    }

    @Test
    public void testAccessSubAdminArea() {
        Address address = new Address(Locale.CHINA);

        String subAdminArea = "Santa Clara County";
        address.setSubAdminArea(subAdminArea);
        assertEquals(subAdminArea, address.getSubAdminArea());

        address.setSubAdminArea(null);
        assertNull(address.getSubAdminArea());
    }

    @Test
    public void testToString() {
        Address address = new Address(Locale.CHINA);

        address.setUrl("www.google.com");
        address.setPostalCode("95120");
        String expected = "Address[addressLines=[],feature=null,admin=null,sub-admin=null," +
                "locality=null,thoroughfare=null,postalCode=95120,countryCode=null," +
                "countryName=null,hasLatitude=false,latitude=0.0,hasLongitude=false," +
                "longitude=0.0,phone=null,url=www.google.com,extras=null]";
        assertEquals(expected, address.toString());
    }

    @Test
    public void testAddressLine() {
        Address address = new Address(Locale.CHINA);

        try {
            address.setAddressLine(-1, null);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        try {
            address.getAddressLine(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        address.setAddressLine(0, null);
        assertNull(address.getAddressLine(0));
        assertEquals(0, address.getMaxAddressLineIndex());

        final String line1 = "1";
        address.setAddressLine(0, line1);
        assertEquals(line1, address.getAddressLine(0));
        assertEquals(0, address.getMaxAddressLineIndex());

        final String line2 = "2";
        address.setAddressLine(5, line2);
        assertEquals(line2, address.getAddressLine(5));
        assertEquals(5, address.getMaxAddressLineIndex());

        address.setAddressLine(2, null);
        assertNull(address.getAddressLine(2));
        assertEquals(5, address.getMaxAddressLineIndex());
    }

    @Test
    public void testGetLocale() {
        Locale locale = Locale.US;
        Address address = new Address(locale);
        assertSame(locale, address.getLocale());

        locale = Locale.UK;
        address = new Address(locale);
        assertSame(locale, address.getLocale());

        address = new Address(null);
        assertNull(address.getLocale());
    }

    @Test
    public void testAccessLocality() {
        Address address = new Address(Locale.PRC);

        String locality = "Hollywood";
        address.setLocality(locality);
        assertEquals(locality, address.getLocality());

        address.setLocality(null);
        assertNull(address.getLocality());
    }

    @Test
    public void testWriteToParcel() {
        Locale locale = Locale.KOREA;
        Address address = new Address(locale);

        Parcel parcel = Parcel.obtain();
        address.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        assertEquals(locale.getLanguage(), parcel.readString());
        assertEquals(locale.getCountry(), parcel.readString());
        assertEquals(0, parcel.readInt());
        assertEquals(address.getFeatureName(), parcel.readString());
        assertEquals(address.getAdminArea(), parcel.readString());
        assertEquals(address.getSubAdminArea(), parcel.readString());
        assertEquals(address.getLocality(), parcel.readString());
        assertEquals(address.getSubLocality(), parcel.readString());
        assertEquals(address.getThoroughfare(), parcel.readString());
        assertEquals(address.getSubThoroughfare(), parcel.readString());
        assertEquals(address.getPremises(), parcel.readString());
        assertEquals(address.getPostalCode(), parcel.readString());
        assertEquals(address.getCountryCode(), parcel.readString());
        assertEquals(address.getCountryName(), parcel.readString());
        assertEquals(0, parcel.readInt());
        assertEquals(0, parcel.readInt());
        assertEquals(address.getPhone(), parcel.readString());
        assertEquals(address.getUrl(), parcel.readString());
        assertEquals(address.getExtras(), parcel.readBundle());
    }

    private class MockParcelable implements Parcelable {
        public int describeContents() {
            return Parcelable.CONTENTS_FILE_DESCRIPTOR;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }
    }
}
