package com.xtremelabs.droidsugar.fakes;

import com.google.android.maps.MapActivity;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(MapActivity.class)
public class FakeMapActivity extends FakeActivity {
    public FakeMapActivity(MapActivity realActivity) {
        super(realActivity);
    }

    @Implementation
    protected boolean isRouteDisplayed() {
        return false;
    }
}
