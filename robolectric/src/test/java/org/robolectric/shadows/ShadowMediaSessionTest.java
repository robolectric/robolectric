package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.session.MediaSession;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for robolectric functionality around {@link MediaSession}. */
@RunWith(AndroidJUnit4.class)
public class ShadowMediaSessionTest {
  @Test
  public void mediaSessionCompat_creation() {
    // Should not result in an exception.
    new MediaSession(ApplicationProvider.getApplicationContext(), "test");
  }

  @Test
  public void getPackageNameForToken() {
    Context context = ApplicationProvider.getApplicationContext();
    MediaSession session = new MediaSession(context, "test");
    assertThat(ShadowMediaSession.getPackageNameForToken(session.getSessionToken()))
        .isEqualTo(context.getPackageName());
  }
}
