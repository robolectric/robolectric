package com.xtremelabs.robolectric.shadows;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(OverlayItem.class)
public class ShadowOverlayItem {
    private GeoPoint geoPoint;

    public void __constructor__(GeoPoint geoPoint, String title, String snippet) {
        this.geoPoint = geoPoint;
    }

    @Implementation
    public GeoPoint getPoint() {
        return geoPoint;
    }
}
