package com.xtremelabs.droidsugar.view;

import android.view.ViewGroup;

public class FakeMapView extends FakeViewGroup {
    private boolean satelliteOn;

    public FakeMapView(ViewGroup viewGroup) {
        super(viewGroup);
    }

    public void setSatellite(boolean satelliteOn) {
        this.satelliteOn = satelliteOn;
    }

    public boolean isSatellite() {
        return satelliteOn;
    }
}
