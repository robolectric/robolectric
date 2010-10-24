package com.xtremelabs.robolectric.shadows;

import android.app.Application;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DogfoodRobolectricTestRunner.class)
public class MapActivityTest {
    @Before
    public void setUp() throws Exception {
        DogfoodRobolectricTestRunner.addGenericProxies();

        Robolectric.application = new Application();
    }

    @Test
    public void onDestroy_shouldNotComplainIfLifecycleIsCorrect() throws Exception {
        MyMapActivity mapActivity = new MyMapActivity();
        mapActivity.onCreate(null);
        mapActivity.onResume();
        mapActivity.onPause();
        mapActivity.onDestroy();
    }

    @Test(expected = IllegalStateException.class)
    public void onDestroy_shouldComplainIfPauseIsNotCalled() throws Exception {
        MyMapActivity mapActivity = new MyMapActivity();
        mapActivity.onCreate(null);
        mapActivity.onResume();
        mapActivity.onDestroy();
    }

    private static class MyMapActivity extends MapActivity {
        @Override protected void onCreate(Bundle bundle) {
            super.onCreate(bundle);
        }

        @Override protected void onDestroy() {
            super.onDestroy();
        }

        @Override protected void onPause() {
            super.onPause();
        }

        @Override protected void onResume() {
            super.onResume();
        }

        @Override protected boolean isRouteDisplayed() {
            return false;
        }
    }
}
