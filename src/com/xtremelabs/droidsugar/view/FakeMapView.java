package com.xtremelabs.droidsugar.view;

import android.view.ViewGroup;
import com.google.android.maps.MapController;

import static org.mockito.Mockito.mock;

public class FakeMapView extends FakeViewGroup {
    private boolean satelliteOn;
    public MapController mapController;

    public FakeMapView(ViewGroup viewGroup) {
        super(viewGroup);

        mapController = mock(MapController.class);
    }

    public void setSatellite(boolean satelliteOn) {
        this.satelliteOn = satelliteOn;
    }

    public boolean isSatellite() {
        return satelliteOn;
    }

    public MapController getController() {
        return mapController;
    }
}
