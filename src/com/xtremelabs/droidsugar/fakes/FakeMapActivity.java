package com.xtremelabs.droidsugar.fakes;

import android.app.Activity;
import com.google.android.maps.MapActivity;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapActivity.class)
public class FakeMapActivity extends FakeActivity {
    public FakeMapActivity(Activity realActivity) {
        super(realActivity);
    }

    protected boolean isRouteDisplayed() {
        return false;
    }
}
