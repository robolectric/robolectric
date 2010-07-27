package com.xtremelabs.droidsugar.view;

import android.view.ViewGroup;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeMapView extends FakeViewGroup {
    private boolean satelliteOn;
    public MapController mapController;
    private ArrayList<Overlay> overlays = new ArrayList<Overlay>();

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

    public List<Overlay> getOverlays() {
       return overlays;
    }
}
