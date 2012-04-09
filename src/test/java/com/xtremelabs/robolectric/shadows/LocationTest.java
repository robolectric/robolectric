package com.xtremelabs.robolectric.shadows;

import android.location.Location;
import android.location.LocationManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class LocationTest {

    private Location location;

    @Before
    public void setUp() throws Exception {
        location = new Location(LocationManager.GPS_PROVIDER);
        location.setTime(1);
        location.setLatitude(2);
        location.setLongitude(3);
        location.setAccuracy(4);
    }

    @Test
    public void test_copyConstructor() throws Exception {
        Location copiedLocation = new Location(location);

        assertLocationFieldsFromTestSetup(copiedLocation);
    }

    @Test
    public void test_set() throws Exception {
        Location newLocation = new Location(LocationManager.NETWORK_PROVIDER);
        newLocation.set(location);

        assertLocationFieldsFromTestSetup(newLocation);
    }

    private void assertLocationFieldsFromTestSetup(Location l) {
        assertEquals(1, l.getTime());
        assertEquals(2.0, l.getLatitude());
        assertEquals(3.0, l.getLongitude());
        assertEquals(4f, l.getAccuracy());
        assertEquals(LocationManager.GPS_PROVIDER, l.getProvider());

        assertEquals(location, l);
    }
}
