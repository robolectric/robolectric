package com.xtremelabs.droidsugar.fakes;

import android.location.Location;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Location.class)
public class FakeLocation {
    private long time;
    private String provider;
    private double latitude;
    private double longitude;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FakeLocation that = (FakeLocation) o;

        if (Double.compare(that.latitude, latitude) != 0) return false;
        if (Double.compare(that.longitude, longitude) != 0) return false;
        if (time != that.time) return false;
        if (provider != null ? !provider.equals(that.provider) : that.provider != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (time ^ (time >>> 32));
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        temp = latitude != +0.0d ? Double.doubleToLongBits(latitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = longitude != +0.0d ? Double.doubleToLongBits(longitude) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override public String toString() {
        return "Location{" +
                "time=" + time +
                ", provider='" + provider + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
