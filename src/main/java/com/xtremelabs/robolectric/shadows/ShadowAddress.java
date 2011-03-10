package com.xtremelabs.robolectric.shadows;

import android.location.Address;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


@SuppressWarnings({"UnusedDeclaration", "JavaDoc"})
@Implements(Address.class)
public class ShadowAddress {

    private Locale mLocale;

    private String mFeatureName;
    private HashMap<Integer, String> mAddressLines;
    private int mMaxAddressLineIndex = -1;
    private String mAdminArea;
    private String mSubAdminArea;
    private String mLocality;
    private String mSubLocality;
    private String mThoroughfare;
    private String mSubThoroughfare;
    private String mPremises;
    private String mPostalCode;
    private String mCountryCode;
    private String mCountryName;
    private double mLatitude;
    private double mLongitude;
    private boolean mHasLatitude = false;
    private boolean mHasLongitude = false;
    private String mPhone;
    private String mUrl;
    private Bundle mExtras = null;

    /**
     * Constructs a new Address object set to the given Locale and with all
     * other fields initialized to null or false.
     */
    public void __constructor__(Locale locale) {
        mLocale = locale;
    }

    /**
     * Returns the Locale associated with this address.
     */
    @Implementation
    public Locale getLocale() {
        return mLocale;
    }

    /**
     * Returns the largest index currently in use to specify an address line.
     * If no address lines are specified, -1 is returned.
     */
    @Implementation
    public int getMaxAddressLineIndex() {
        return mMaxAddressLineIndex;
    }

    /**
     * Returns a line of the address numbered by the given index
     * (starting at 0), or null if no such line is present.
     *
     * @throws IllegalArgumentException if index < 0
     */
    @Implementation
    public String getAddressLine(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " < 0");
        }
        return mAddressLines == null? null :  mAddressLines.get(index);
    }

    /**
     * Sets the line of the address numbered by index (starting at 0) to the
     * given String, which may be null.
     *
     * @throws IllegalArgumentException if index < 0
     */
    @Implementation
    public void setAddressLine(int index, String line) {
        if (index < 0) {
            throw new IllegalArgumentException("index = " + index + " < 0");
        }
        if (mAddressLines == null) {
            mAddressLines = new HashMap<Integer, String>();
        }
        mAddressLines.put(index, line);

        if (line == null) {
            // We've eliminated a line, recompute the max index
            mMaxAddressLineIndex = -1;
            for (Integer i : mAddressLines.keySet()) {
                mMaxAddressLineIndex = Math.max(mMaxAddressLineIndex, i);
            }
        } else {
            mMaxAddressLineIndex = Math.max(mMaxAddressLineIndex, index);
        }
    }

    /**
     * Returns the feature name of the address, for example, "Golden Gate Bridge", or null
     * if it is unknown
     */
    @Implementation
    public String getFeatureName() {
        return mFeatureName;
    }

    /**
     * Sets the feature name of the address to the given String, which may be null
     */
    @Implementation
    public void setFeatureName(String featureName) {
        mFeatureName = featureName;
    }

    /**
     * Returns the administrative area name of the address, for example, "CA", or null if
     * it is unknown
     */
    @Implementation
    public String getAdminArea() {
        return mAdminArea;
    }

    /**
     * Sets the administrative area name of the address to the given String, which may be null
     */
    @Implementation
    public void setAdminArea(String adminArea) {
        this.mAdminArea = adminArea;
    }

    /**
     * Returns the sub-administrative area name of the address, for example, "Santa Clara County",
     * or null if it is unknown
     */
    @Implementation
    public String getSubAdminArea() {
        return mSubAdminArea;
    }

    /**
     * Sets the sub-administrative area name of the address to the given String, which may be null
     */
    @Implementation
    public void setSubAdminArea(String subAdminArea) {
        this.mSubAdminArea = subAdminArea;
    }

    /**
     * Returns the locality of the address, for example "Mountain View", or null if it is unknown.
     */
    @Implementation
    public String getLocality() {
        return mLocality;
    }

    /**
     * Sets the locality of the address to the given String, which may be null.
     */
    @Implementation
    public void setLocality(String locality) {
        mLocality = locality;
    }

    /**
     * Returns the sub-locality of the address, or null if it is unknown.
     * For example, this may correspond to the neighborhood of the locality.
     */
    @Implementation
    public String getSubLocality() {
        return mSubLocality;
    }

    /**
     * Sets the sub-locality of the address to the given String, which may be null.
     */
    @Implementation
    public void setSubLocality(String sublocality) {
        mSubLocality = sublocality;
    }

    /**
     * Returns the thoroughfare name of the address, for example, "1600 Ampitheater Parkway",
     * which may be null
     */
    @Implementation
    public String getThoroughfare() {
        return mThoroughfare;
    }

    /**
     * Sets the thoroughfare name of the address, which may be null.
     */
    @Implementation
    public void setThoroughfare(String thoroughfare) {
        this.mThoroughfare = thoroughfare;
    }

    /**
     * Returns the sub-thoroughfare name of the address, which may be null.
     * This may correspond to the street number of the address.
     */
    @Implementation
    public String getSubThoroughfare() {
        return mSubThoroughfare;
    }

    /**
     * Sets the sub-thoroughfare name of the address, which may be null.
     */
    @Implementation
    public void setSubThoroughfare(String subthoroughfare) {
        this.mSubThoroughfare = subthoroughfare;
    }

    /**
     * Returns the premises of the address, or null if it is unknown.
     */
    @Implementation
    public String getPremises() {
        return mPremises;
    }

    /**
     * Sets the premises of the address to the given String, which may be null.
     */
    @Implementation
    public void setPremises(String premises) {
        mPremises = premises;
    }

    /**
     * Returns the postal code of the address, for example "94110",
     * or null if it is unknown.
     */
    @Implementation
    public String getPostalCode() {
        return mPostalCode;
    }

    /**
     * Sets the postal code of the address to the given String, which may
     * be null.
     */
    @Implementation
    public void setPostalCode(String postalCode) {
        mPostalCode = postalCode;
    }

    /**
     * Returns the country code of the address, for example "US",
     * or null if it is unknown.
     */
    @Implementation
    public String getCountryCode() {
        return mCountryCode;
    }

    /**
     * Sets the country code of the address to the given String, which may
     * be null.
     */
    @Implementation
    public void setCountryCode(String countryCode) {
        mCountryCode = countryCode;
    }

    /**
     * Returns the localized country name of the address, for example "Iceland",
     * or null if it is unknown.
     */
    @Implementation
    public String getCountryName() {
        return mCountryName;
    }

    /**
     * Sets the country name of the address to the given String, which may
     * be null.
     */
    @Implementation
    public void setCountryName(String countryName) {
        mCountryName = countryName;
    }

    /**
     * Returns true if a latitude has been assigned to this Address,
     * false otherwise.
     */
    @Implementation
    public boolean hasLatitude() {
        return mHasLatitude;
    }

    /**
     * Returns the latitude of the address if known.
     *
     * @throws IllegalStateException if this Address has not been assigned
     * a latitude.
     */
    @Implementation
    public double getLatitude() {
        if (mHasLatitude) {
            return mLatitude;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Sets the latitude associated with this address.
     */
    @Implementation
    public void setLatitude(double latitude) {
        mLatitude = latitude;
        mHasLatitude = true;
    }

    /**
     * Removes any latitude associated with this address.
     */
    @Implementation
    public void clearLatitude() {
        mHasLatitude = false;
    }

    /**
     * Returns true if a longitude has been assigned to this Address,
     * false otherwise.
     */
    @Implementation
    public boolean hasLongitude() {
        return mHasLongitude;
    }

    /**
     * Returns the longitude of the address if known.
     *
     * @throws IllegalStateException if this Address has not been assigned
     * a longitude.
     */
    @Implementation
    public double getLongitude() {
        if (mHasLongitude) {
            return mLongitude;
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * Sets the longitude associated with this address.
     */
    @Implementation
    public void setLongitude(double longitude) {
        mLongitude = longitude;
        mHasLongitude = true;
    }

    /**
     * Removes any longitude associated with this address.
     */
    @Implementation
    public void clearLongitude() {
        mHasLongitude = false;
    }

    /**
     * Returns the phone number of the address if known,
     * or null if it is unknown.
     *
     * @throws IllegalStateException if this Address has not been assigned
     * a latitude.
     */
    @Implementation
    public String getPhone() {
        return mPhone;
    }

    /**
     * Sets the phone number associated with this address.
     */
    @Implementation
    public void setPhone(String phone) {
        mPhone = phone;
    }

    /**
     * Returns the public URL for the address if known,
     * or null if it is unknown.
     */
    @Implementation
    public String getUrl() {
        return mUrl;
    }

    /**
     * Sets the public URL associated with this address.
     */
    @Implementation
    public void setUrl(String Url) {
        mUrl = Url;
    }

    /**
     * Returns additional provider-specific information about the
     * address as a Bundle.  The keys and values are determined
     * by the provider.  If no additional information is available,
     * null is returned.
     *
     * <!--
     * <p> A number of common key/value pairs are listed
     * below. Providers that use any of the keys on this list must
     * provide the corresponding value as described below.
     *
     * <ul>
     * </ul>
     * -->
     */
    @Implementation
    public Bundle getExtras() {
        return mExtras;
    }

    /**
     * Sets the extra information associated with this fix to the
     * given Bundle.
     */
    @Implementation
    public void setExtras(Bundle extras) {
        mExtras = (extras == null) ? null : new Bundle(extras);
    }

    @Override
    @Implementation
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Address[addressLines=[");
        for (int i = 0; i <= mMaxAddressLineIndex; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(i);
            sb.append(':');
            String line = mAddressLines.get(i);
            if (line == null) {
                sb.append("null");
            } else {
                sb.append('\"');
                sb.append(line);
                sb.append('\"');
            }
        }
        sb.append(']');
        sb.append(",feature=");
        sb.append(mFeatureName);
        sb.append(",admin=");
        sb.append(mAdminArea);
        sb.append(",sub-admin=");
        sb.append(mSubAdminArea);
        sb.append(",locality=");
        sb.append(mLocality);
        sb.append(",thoroughfare=");
        sb.append(mThoroughfare);
        sb.append(",postalCode=");
        sb.append(mPostalCode);
        sb.append(",countryCode=");
        sb.append(mCountryCode);
        sb.append(",countryName=");
        sb.append(mCountryName);
        sb.append(",hasLatitude=");
        sb.append(mHasLatitude);
        sb.append(",latitude=");
        sb.append(mLatitude);
        sb.append(",hasLongitude=");
        sb.append(mHasLongitude);
        sb.append(",longitude=");
        sb.append(mLongitude);
        sb.append(",phone=");
        sb.append(mPhone);
        sb.append(",url=");
        sb.append(mUrl);
        sb.append(",extras=");
        sb.append(mExtras);
        sb.append(']');
        return sb.toString();
    }

    public static final Parcelable.Creator<Address> CREATOR =
        new Parcelable.Creator<Address>() {
        public Address createFromParcel(Parcel in) {
            String language = in.readString();
            String country = in.readString();
            Locale locale = country.length() > 0 ?
                new Locale(language, country) :
                new Locale(language);
            Address addr = new Address(locale);
            ShadowAddress a = Robolectric.shadowOf(addr);

            int N = in.readInt();
            if (N > 0) {
                a.mAddressLines = new HashMap<Integer, String>(N);
                for (int i = 0; i < N; i++) {
                    int index = in.readInt();
                    String line = in.readString();
                    a.mAddressLines.put(index, line);
                    a.mMaxAddressLineIndex =
                        Math.max(a.mMaxAddressLineIndex, index);
                }
            } else {
                a.mAddressLines = null;
                a.mMaxAddressLineIndex = -1;
            }
            a.mFeatureName = in.readString();
            a.mAdminArea = in.readString();
            a.mSubAdminArea = in.readString();
            a.mLocality = in.readString();
            a.mSubLocality = in.readString();
            a.mThoroughfare = in.readString();
            a.mSubThoroughfare = in.readString();
            a.mPremises = in.readString();
            a.mPostalCode = in.readString();
            a.mCountryCode = in.readString();
            a.mCountryName = in.readString();
            a.mHasLatitude = in.readInt() != 0;
            if (a.mHasLatitude) {
                a.mLatitude = in.readDouble();
            }
            a.mHasLongitude = in.readInt() != 0;
            if (a.mHasLongitude) {
                a.mLongitude = in.readDouble();
            }
            a.mPhone = in.readString();
            a.mUrl = in.readString();
            a.mExtras = in.readBundle();
            return addr;
        }

        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    @Implementation
    public int describeContents() {
        return (mExtras != null) ? mExtras.describeContents() : 0;
    }

    @Implementation
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mLocale.getLanguage());
        parcel.writeString(mLocale.getCountry());
        if (mAddressLines == null) {
            parcel.writeInt(0);
        } else {
            Set<Map.Entry<Integer, String>> entries = mAddressLines.entrySet();
            parcel.writeInt(entries.size());
            for (Map.Entry<Integer, String> e : entries) {
                parcel.writeInt(e.getKey());
                parcel.writeString(e.getValue());
            }
        }
        parcel.writeString(mFeatureName);
        parcel.writeString(mAdminArea);
        parcel.writeString(mSubAdminArea);
        parcel.writeString(mLocality);
        parcel.writeString(mSubLocality);
        parcel.writeString(mThoroughfare);
        parcel.writeString(mSubThoroughfare);
        parcel.writeString(mPremises);
        parcel.writeString(mPostalCode);
        parcel.writeString(mCountryCode);
        parcel.writeString(mCountryName);
        parcel.writeInt(mHasLatitude ? 1 : 0);
        if (mHasLatitude) {
            parcel.writeDouble(mLatitude);
        }
        parcel.writeInt(mHasLongitude ? 1 : 0);
        if (mHasLongitude){
            parcel.writeDouble(mLongitude);
        }
        parcel.writeString(mPhone);
        parcel.writeString(mUrl);
        parcel.writeBundle(mExtras);
    }
}
