package com.xtremelabs.robolectric.shadows;

import android.location.Location;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertArrayEquals;

@RunWith(WithTestDefaultsRunner.class)
public class LocationTest {

    @Before
    public void setUp() {
        ShadowLocation.setDistanceBetween(null);
    }

    @After
    public void tearDown() {
        ShadowLocation.setDistanceBetween(null);
    }

    @Test
    public void testDistanceBetween_preventNPE() {
        Location.distanceBetween(1.0, 1.0, 1.0, 1.0, new float[2]);
    }

    @Test
    public void testDistanceBetween_shouldBeMockable() {
        float[] expectedDistance = {2.2f, 5.2f};
        ShadowLocation.setDistanceBetween(expectedDistance);
        float[] actualDistance = new float[2];
        Location.distanceBetween(1.0, 1.0, 1.0, 1.0, actualDistance);
        assertArrayEquals(expectedDistance, actualDistance, 0f);
    }

    @Test
    public void testDistanceBetweenMocking_requiresArraysOfEqualLength() {
        float[] expectedDistance = {2.2f, 5.2f};
        ShadowLocation.setDistanceBetween(expectedDistance);
        float[] actualDistance = new float[1];
        Location.distanceBetween(1.0, 1.0, 1.0, 1.0, actualDistance);
        assertArrayEquals(new float[]{0.0f}, actualDistance, 0f);
    }


}
