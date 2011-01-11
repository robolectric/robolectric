package com.xtremelabs.robolectric;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.xtremelabs.robolectric.shadows.ShadowGeoPoint;
import com.xtremelabs.robolectric.shadows.ShadowItemizedOverlay;
import com.xtremelabs.robolectric.shadows.ShadowMapController;
import com.xtremelabs.robolectric.shadows.ShadowMapView;

public class RobolectricForMaps {
    public static ShadowGeoPoint shadowOf(GeoPoint instance) {
        return (ShadowGeoPoint) Robolectric.shadowOf_(instance);
    }

    public static ShadowMapView shadowOf(MapView instance) {
        return (ShadowMapView) Robolectric.shadowOf_(instance);
    }

    public static ShadowMapController shadowOf(MapController instance) {
        return (ShadowMapController) Robolectric.shadowOf_(instance);
    }

    public static ShadowItemizedOverlay shadowOf(ItemizedOverlay instance) {
        return (ShadowItemizedOverlay) Robolectric.shadowOf_(instance);
    }
}
