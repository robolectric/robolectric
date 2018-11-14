package org.robolectric.shadows;

import android.media.session.MediaSession;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for robolectric functionality around {@link MediaSession}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowMediaSessionTest {
  @Test
  public void mediaSessionCompat_creation() throws Exception {
    // Should not result in an exception.
    new MediaSession(ApplicationProvider.getApplicationContext(), "test");
  }
}
