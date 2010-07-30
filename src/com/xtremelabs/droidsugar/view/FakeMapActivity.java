package com.xtremelabs.droidsugar.view;

import android.app.Activity;

@SuppressWarnings({"UnusedDeclaration"})
public class FakeMapActivity extends FakeActivity {
    public FakeMapActivity(Activity realActivity) {
        super(realActivity);
    }

    protected boolean isRouteDisplayed() {
        return false;
    }
}
