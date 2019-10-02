package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.media.session.ISessionController;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowMediaController}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowMediaControllerTest {

  private MediaController mediaController;
  private ShadowMediaController shadowMediaController;
  private final String testPackageName = "FOO";

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    ISessionController binder = mock(ISessionController.class);
    MediaSession.Token token = new MediaSession.Token(binder);
    mediaController = new MediaController(context, token);
    shadowMediaController = Shadow.extract(mediaController);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setPackageName() {
    shadowMediaController.setPackageName(testPackageName);
    assertEquals(testPackageName, mediaController.getPackageName());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void executeOnPlaybackStateChanged() {
    ArgumentCaptor<PlaybackState> argument = ArgumentCaptor.forClass(PlaybackState.class);
    MediaController.Callback mockCallback = mock(MediaController.Callback.class);
    PlaybackState playbackState = createPlaybackState();

    mediaController.registerCallback(mockCallback);
    shadowMediaController.executeOnPlaybackStateChanged(playbackState);

    shadowOf(getMainLooper()).idle();

    verify(mockCallback, times(1)).onPlaybackStateChanged(argument.capture());
    assertEquals(argument.getValue(), playbackState);
  }

  private static PlaybackState createPlaybackState() {
       return new PlaybackState.Builder()
        .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
        .build();
  }
}
