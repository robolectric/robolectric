package com.xtremelabs.droidsugar.fakes;

import android.location.LocationManager;
import com.xtremelabs.droidsugar.util.Implementation;
import com.xtremelabs.droidsugar.util.Implements;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(LocationManager.class)
public class FakeLocationManager {
    private final Map<String, Boolean> providersEnabled = new HashMap<String, Boolean>();

    @Implementation
    public boolean isProviderEnabled(String provider) {
        Boolean isEnabled = providersEnabled.get(provider);
        return isEnabled == null ? true : isEnabled;
    }

    public void setProviderEnabled(String provider, boolean isEnabled) {
        providersEnabled.put(provider, isEnabled);
    }
}
