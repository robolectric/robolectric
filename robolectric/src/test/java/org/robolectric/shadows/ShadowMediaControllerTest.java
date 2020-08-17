package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.ISessionController;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowMediaController}. */
@RunWith(AndroidJUnit4.class)
@Config(maxSdk = Q)
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
  public void setAndGetPlaybackState() {
    PlaybackState playbackState = createPlaybackState();
    shadowMediaController.setPlaybackState(playbackState);
    assertEquals(playbackState, mediaController.getPlaybackState());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void setAndGetMetadata() {
    MediaMetadata metadata = createMetadata("test");
    shadowMediaController.setMetadata(metadata);
    assertEquals(metadata, mediaController.getMetadata());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void registerAndGetCallback() {
    List<Callback> mockCallbacks = new ArrayList<>();
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    Callback mockCallback1 = mock(Callback.class);
    mockCallbacks.add(mockCallback1);
    mediaController.registerCallback(mockCallback1);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    Callback mockCallback2 = mock(Callback.class);
    mockCallbacks.add(mockCallback2);
    mediaController.registerCallback(mockCallback2);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void unregisterCallback() {
    List<Callback> mockCallbacks = new ArrayList<>();
    Callback mockCallback1 = mock(Callback.class);
    mockCallbacks.add(mockCallback1);
    mediaController.registerCallback(mockCallback1);
    Callback mockCallback2 = mock(Callback.class);
    mockCallbacks.add(mockCallback2);
    mediaController.registerCallback(mockCallback2);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    mockCallbacks.remove(mockCallback1);
    mediaController.unregisterCallback(mockCallback1);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    mockCallbacks.remove(mockCallback2);
    mediaController.unregisterCallback(mockCallback2);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void executeOnPlaybackStateChanged() {
    ArgumentCaptor<PlaybackState> argument = ArgumentCaptor.forClass(PlaybackState.class);
    Callback mockCallback = mock(Callback.class);
    PlaybackState playbackState = createPlaybackState();

    mediaController.registerCallback(mockCallback);
    shadowMediaController.executeOnPlaybackStateChanged(playbackState);

    shadowOf(getMainLooper()).idle();

    verify(mockCallback, times(1)).onPlaybackStateChanged(argument.capture());
    assertEquals(argument.getValue(), playbackState);
    assertEquals(mediaController.getPlaybackState(), playbackState);
  }

  @Test
  @Config(minSdk = LOLLIPOP)
  public void executeOnMetadataChanged() {
    ArgumentCaptor<MediaMetadata> argument = ArgumentCaptor.forClass(MediaMetadata.class);
    Callback mockCallback = mock(Callback.class);
    MediaMetadata metadata = createMetadata("test");

    mediaController.registerCallback(mockCallback);
    shadowMediaController.executeOnMetadataChanged(metadata);

    shadowOf(getMainLooper()).idle();

    verify(mockCallback, times(1)).onMetadataChanged(argument.capture());
    assertEquals(argument.getValue(), metadata);
    assertEquals(mediaController.getMetadata(), metadata);
  }

  private static PlaybackState createPlaybackState() {
       return new PlaybackState.Builder()
        .setState(PlaybackState.STATE_PLAYING, 0L, 0f)
        .build();
  }

  private static MediaMetadata createMetadata(String title) {
    MediaMetadata.Builder builder = new MediaMetadata.Builder();

    builder.putString(MediaMetadata.METADATA_KEY_TITLE, title);

    return builder.build();
  }
}
