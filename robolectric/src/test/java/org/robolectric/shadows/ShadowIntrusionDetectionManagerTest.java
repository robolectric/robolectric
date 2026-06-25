package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.security.intrusiondetection.IntrusionDetectionManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** A placeholder starting test used to verify IntrusionDetectionManager support in Robolectric. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public class ShadowIntrusionDetectionManagerTest {

  @Test
  public void intrusionDetectionManager() {
    IntrusionDetectionManager intrusionDetectionManager =
        (IntrusionDetectionManager)
            getApplicationContext().getSystemService(Context.INTRUSION_DETECTION_SERVICE);

    assertThat(intrusionDetectionManager).isNotNull();
  }
}
