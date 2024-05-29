package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadata;
import android.media.Rating;
import android.media.session.ISessionController;
import android.media.session.MediaController;
import android.media.session.MediaController.PlaybackInfo;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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

    MediaSession.Token token = null;
    if (RuntimeEnvironment.getApiLevel() <= Q) {
      token =
          ReflectionHelpers.callConstructor(
              MediaSession.Token.class, ClassParameter.from(ISessionController.class, binder));
    } else {
      token =
          ReflectionHelpers.callConstructor(
              MediaSession.Token.class,
              ClassParameter.from(int.class, 0),
              ClassParameter.from(ISessionController.class, binder));
    }
    mediaController = new MediaController(context, token);
    shadowMediaController = Shadow.extract(mediaController);
  }

  @Test
  public void setPackageName() {
    shadowMediaController.setPackageName(testPackageName);
    assertEquals(testPackageName, mediaController.getPackageName());
  }

  @Test
  public void setAndGetPlaybackState() {
    PlaybackState playbackState = createPlaybackState();
    shadowMediaController.setPlaybackState(playbackState);
    assertEquals(playbackState, mediaController.getPlaybackState());
  }

  @Test
  public void setAndGetMetadata() {
    MediaMetadata metadata = createMetadata("test");
    shadowMediaController.setMetadata(metadata);
    assertEquals(metadata, mediaController.getMetadata());
  }

  @Test
  public void setAndGetPlaybackInfo() {
    PlaybackInfo playbackInfo =
        PlaybackInfoBuilder.newBuilder()
            .setVolumeType(PlaybackInfo.PLAYBACK_TYPE_LOCAL)
            .setVolumeControl(0)
            .setMaxVolume(0)
            .setCurrentVolume(0)
            .setAudioAttributes(null)
            .build();
    shadowMediaController.setPlaybackInfo(playbackInfo);
    assertEquals(playbackInfo, mediaController.getPlaybackInfo());
  }

  @Test
  public void setInvalidRatingType() {
    int ratingType = Rating.RATING_PERCENTAGE + 1;
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> shadowMediaController.setRatingType(ratingType));
    assertThat(thrown)
        .hasMessageThat()
        .contains(
            "Invalid RatingType value "
                + ratingType
                + ". The valid range is from 0 to "
                + Rating.RATING_PERCENTAGE);
  }

  @Test
  public void getDefaultRatingType() {
    assertThat(mediaController.getRatingType()).isEqualTo(Rating.RATING_NONE);
  }

  @Test
  public void setAndGetRatingType() {
    int ratingType = Rating.RATING_HEART;
    shadowMediaController.setRatingType(ratingType);
    assertThat(mediaController.getRatingType()).isEqualTo(ratingType);
  }

  @Test
  public void setAndGetSessionActivity() {
    Context context = ApplicationProvider.getApplicationContext();
    Intent intent = new Intent("testIntent");
    PendingIntent pi = PendingIntent.getActivity(context, 555, intent, 0);
    shadowMediaController.setSessionActivity(pi);
    assertEquals(pi, mediaController.getSessionActivity());
  }

  @Test
  public void setAndGetExtras() {
    String extraKey = "test.extra.key";
    Bundle extras = new Bundle();
    extras.putBoolean(extraKey, true);
    shadowMediaController.setExtras(extras);
    assertEquals(true, mediaController.getExtras().getBoolean(extraKey, false));
  }

  @Test
  public void registerAndGetCallback() {
    List<MediaController.Callback> mockCallbacks = new ArrayList<>();
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    MediaController.Callback mockCallback1 = mock(MediaController.Callback.class);
    mockCallbacks.add(mockCallback1);
    mediaController.registerCallback(mockCallback1);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    MediaController.Callback mockCallback2 = mock(MediaController.Callback.class);
    mockCallbacks.add(mockCallback2);
    mediaController.registerCallback(mockCallback2);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());
  }

  @Test
  public void registerWithHandlerAndGetCallback() {
    List<MediaController.Callback> mockCallbacks = new ArrayList<>();
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    MediaController.Callback mockCallback1 = mock(MediaController.Callback.class);
    mockCallbacks.add(mockCallback1);
    mediaController.registerCallback(mockCallback1, null);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());

    MediaController.Callback mockCallback2 = mock(MediaController.Callback.class);
    mockCallbacks.add(mockCallback2);
    mediaController.registerCallback(mockCallback2, null);
    assertEquals(mockCallbacks, shadowMediaController.getCallbacks());
  }

  @Test
  public void unregisterCallback() {
    List<MediaController.Callback> mockCallbacks = new ArrayList<>();
    MediaController.Callback mockCallback1 = mock(MediaController.Callback.class);
    mockCallbacks.add(mockCallback1);
    mediaController.registerCallback(mockCallback1);
    MediaController.Callback mockCallback2 = mock(MediaController.Callback.class);
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
  public void executeOnPlaybackStateChanged() {
    ArgumentCaptor<PlaybackState> argument = ArgumentCaptor.forClass(PlaybackState.class);
    MediaController.Callback mockCallback = mock(MediaController.Callback.class);
    PlaybackState playbackState = createPlaybackState();

    mediaController.registerCallback(mockCallback);
    shadowMediaController.executeOnPlaybackStateChanged(playbackState);

    shadowOf(getMainLooper()).idle();

    verify(mockCallback, times(1)).onPlaybackStateChanged(argument.capture());
    assertEquals(argument.getValue(), playbackState);
    assertEquals(mediaController.getPlaybackState(), playbackState);
  }

  @Test
  public void executeOnMetadataChanged() {
    ArgumentCaptor<MediaMetadata> argument = ArgumentCaptor.forClass(MediaMetadata.class);
    MediaController.Callback mockCallback = mock(MediaController.Callback.class);
    MediaMetadata metadata = createMetadata("test");

    mediaController.registerCallback(mockCallback);
    shadowMediaController.executeOnMetadataChanged(metadata);

    shadowOf(getMainLooper()).idle();

    verify(mockCallback, times(1)).onMetadataChanged(argument.capture());
    assertEquals(argument.getValue(), metadata);
    assertEquals(mediaController.getMetadata(), metadata);
  }

  @Test
  public void executeOnSessionDestroyed() {
    MediaController.Callback mockCallback = mock(MediaController.Callback.class);

    mediaController.registerCallback(mockCallback);
    shadowMediaController.executeOnSessionDestroyed();

    shadowOf(getMainLooper()).idle();

    verify(mockCallback, times(1)).onSessionDestroyed();
  }

  private static PlaybackState createPlaybackState() {
    return new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING, 0L, 0f).build();
  }

  private static MediaMetadata createMetadata(String title) {
    MediaMetadata.Builder builder = new MediaMetadata.Builder();

    builder.putString(MediaMetadata.METADATA_KEY_TITLE, title);

    return builder.build();
  }
}
