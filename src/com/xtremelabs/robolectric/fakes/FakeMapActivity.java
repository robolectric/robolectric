package com.xtremelabs.robolectric.fakes;

import com.google.android.maps.MapActivity;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

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
