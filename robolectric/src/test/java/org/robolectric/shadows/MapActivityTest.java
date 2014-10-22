package org.robolectric.shadows;

import android.os.Bundle;
import com.google.android.maps.MapActivity;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

@RunWith(TestRunners.WithDefaults.class)
public class MapActivityTest {
  @Test
  public void onDestroy_shouldNotComplainIfLifecycleIsCorrect() throws Exception {
    MyMapActivity mapActivity = new MyMapActivity();
    mapActivity.onCreate(null);
    mapActivity.onResume();
    mapActivity.onPause();
    mapActivity.onDestroy();
  }

  @Ignore("maybe not a valid test in the 2.0 world?") // todo 2.0-cleanup
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
