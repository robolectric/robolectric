package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class LocationManagerTest {
    private LocationManager locationManager;

    @Before
    public void setUp() throws Exception {
        locationManager = (LocationManager) new Application().getSystemService(Context.LOCATION_SERVICE);

        shadowOf(locationManager).setProviderEnabled("provider1", true);
        shadowOf(locationManager).setProviderEnabled("provider2", false);
    }

    @Test
    public void getAllProviders() throws Exception {
        assertEquals(new HashSet<String>(asList("provider1", "provider2")),
                new HashSet<String>(locationManager.getAllProviders()));
    }

    @Test
    public void isProviderEnabled_returnsValueIfGiven() throws Exception {
        assertTrue(locationManager.isProviderEnabled("provider1"));
        assertFalse(locationManager.isProviderEnabled("provider2"));
    }

    @Test
    public void isProviderEnabled_returnsTrueByDefault() throws Exception {
        assertTrue(locationManager.isProviderEnabled("provider3"));
    }
}
