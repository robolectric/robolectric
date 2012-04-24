package com.xtremelabs.robolectric.shadows;

import android.location.Location;
import android.location.LocationManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;

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
        location.setBearing(0.5f);
        location.setSpeed(5.5f);
        location.setAltitude(3.0d);
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

    @Test
    public void removeFieldShouldReportHasFieldAsFalse()
    {
    	assertTrue(location.hasAccuracy());
    	location.removeAccuracy();
    	assertFalse(location.hasAccuracy());
    	
    	assertTrue(location.hasBearing());
    	location.removeBearing();
    	assertFalse(location.hasBearing());
    	
    	assertTrue(location.hasAltitude());
    	location.removeAltitude();
    	assertFalse(location.hasAltitude());
    	
    	assertTrue(location.hasSpeed());
    	location.removeSpeed();
    	assertFalse(location.hasSpeed());
    }
    
    @Test
    public void defaultLocationShouldNotReportFieldsAsAvailable()
    {
    	Location defaultLocation = new Location(LocationManager.GPS_PROVIDER);
    	assertFalse(defaultLocation.hasAccuracy());    	
    	assertFalse(defaultLocation.hasBearing());    	
    	assertFalse(defaultLocation.hasAltitude());    	
    	assertFalse(defaultLocation.hasSpeed());
    	
    	assertEquals(0.0d, defaultLocation.getLatitude());
        assertEquals(0.0d, defaultLocation.getLongitude());
        assertEquals(0.0f, defaultLocation.getAccuracy());
        assertEquals(0.0f, defaultLocation.getBearing());
        assertEquals(0.0f, defaultLocation.getSpeed());
        assertEquals(0.0d, defaultLocation.getAltitude());
    }
    
    @Test
    public void settingFieldShouldMakeHasFieldReturnTrue()
    {
    	Location l = new Location(LocationManager.GPS_PROVIDER);
    	assertFalse(l.hasAccuracy());
    	l.setAccuracy(0.5f);
    	assertTrue(l.hasAccuracy());
    	
    	assertFalse(l.hasBearing());
    	l.setBearing(1);
    	assertTrue(l.hasBearing());
    	
    	assertFalse(l.hasAltitude());
    	l.setAltitude(1);
    	assertTrue(l.hasAltitude());
    	
    	assertFalse(l.hasSpeed());
    	l.setSpeed(5);
    	assertTrue(l.hasSpeed());
    }
    
    private void assertLocationFieldsFromTestSetup(Location l) {
        assertEquals(1, l.getTime());
        assertEquals(2.0, l.getLatitude());
        assertEquals(3.0, l.getLongitude());
        assertEquals(4f, l.getAccuracy());
        assertEquals(0.5f, l.getBearing());
        assertEquals(5.5f, l.getSpeed());
        assertEquals(3.0d, l.getAltitude());
        assertEquals(LocationManager.GPS_PROVIDER, l.getProvider());

        assertTrue(l.hasAltitude());
        assertTrue(l.hasAccuracy());
        assertTrue(l.hasBearing());
        assertTrue(l.hasSpeed());
        
        assertEquals(location, l);
    }
}
