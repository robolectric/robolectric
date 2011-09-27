package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.location.Geocoder;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class GeocoderTest {

    @Test
    public void shouldRecordLastLocationName() throws Exception {
        Geocoder geocoder = new Geocoder(new Activity());
        geocoder.getFromLocationName("731 Market St, San Francisco, CA 94103", 1);
        String lastLocationName = shadowOf(geocoder).getLastLocationName();
        
        assertEquals("731 Market St, San Francisco, CA 94103", lastLocationName);
    }
}
