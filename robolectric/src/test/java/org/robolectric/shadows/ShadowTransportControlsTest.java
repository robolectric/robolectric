package org.robolectric.shadows;

import static android.media.session.PlaybackState.ACTION_PAUSE;
import static android.media.session.PlaybackState.ACTION_PLAY;
import static android.media.session.PlaybackState.ACTION_PLAY_FROM_SEARCH;
import static android.media.session.PlaybackState.ACTION_PLAY_FROM_URI;
import static android.media.session.PlaybackState.ACTION_PREPARE_FROM_SEARCH;
import static android.media.session.PlaybackState.ACTION_PREPARE_FROM_URI;
import static android.media.session.PlaybackState.ACTION_SEEK_TO;
import static android.media.session.PlaybackState.ACTION_SET_RATING;
import static android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT;
import static android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS;
import static android.media.session.PlaybackState.ACTION_SKIP_TO_QUEUE_ITEM;
import static android.media.session.PlaybackState.ACTION_STOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadow.api.Shadow.extract;

import android.media.Rating;
import android.media.session.MediaController.TransportControls;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowTransportControls}. */
@RunWith(AndroidJUnit4.class)
public class ShadowTransportControlsTest {
  TransportControls transportControls;
  private ShadowTransportControls shadowTransportControls;

  @Before
  public void setup() {
    MediaSession mediaSession =
        new MediaSession(ApplicationProvider.getApplicationContext(), "TestMediaSession");
    transportControls = mediaSession.getController().getTransportControls();
    shadowTransportControls = extract(transportControls);
  }

  @Test
  public void testPause_lastPerformedActionIsPause() {
    transportControls.pause();

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_PAUSE);
  }

  @Test
  public void testPlay_lastPerformedActionIsPlay() {
    transportControls.play();

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_PLAY);
  }

  @Test
  public void testPlayFromSearch_lastPerformedActionIsPlayFromSearch() {
    transportControls.playFromSearch("query", new Bundle());

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_PLAY_FROM_SEARCH);
  }

  @Test
  @Config(minSdk = M)
  public void testPlayFromUri_lastPerformedActionIsPlayFromUri() {
    Uri uri = Uri.parse("test://address");
    transportControls.playFromUri(uri, new Bundle());

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_PLAY_FROM_URI);
    assertThat(shadowTransportControls.getUri()).isEqualTo(uri);
  }

  @Test
  @Config(minSdk = N)
  public void testPrepareFromSearch_lastPerformedActionIsPrepareFromSearch() {
    transportControls.prepareFromSearch("query", new Bundle());

    assertThat(shadowTransportControls.getLastPerformedAction())
        .isEqualTo(ACTION_PREPARE_FROM_SEARCH);
  }

  @Test
  @Config(minSdk = N)
  public void testPrepareFromUri_lastPerformedActionIsPrepareFromUri() {
    Uri uri = Uri.parse("test://address");
    transportControls.prepareFromUri(uri, new Bundle());

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_PREPARE_FROM_URI);
    assertThat(shadowTransportControls.getUri()).isEqualTo(uri);
  }

  @Test
  public void testSeekTo_lastPerformedActionIsSeekTo() {
    transportControls.seekTo(50);

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_SEEK_TO);
    assertThat(shadowTransportControls.getSeekToPositionMs()).isEqualTo(50);
  }

  @Test
  public void testSendCustomAction_customActionAndArgsAreRecorded() {
    Bundle customActionArgs = new Bundle();
    customActionArgs.putInt("test", 5);
    transportControls.sendCustomAction("action", customActionArgs);

    assertThat(shadowTransportControls.getCustomAction()).isEqualTo("action");
    assertThat(shadowTransportControls.getCustomActionArgs()).isEqualTo(customActionArgs);
  }

  @Test
  public void testSetRating_lastPerformedActionIsSetRating() {
    Rating rating = Rating.newPercentageRating(30F);
    transportControls.setRating(rating);

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_SET_RATING);
    assertThat(shadowTransportControls.getRating()).isEqualTo(rating);
  }

  @Test
  public void testSkipToNext_lastPerformedActionIsSkipToNext() {
    transportControls.skipToNext();

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_SKIP_TO_NEXT);
  }

  @Test
  public void testSkipToPrevious_lastPerformedActionIsSkipToPrevious() {
    transportControls.skipToPrevious();

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_SKIP_TO_PREVIOUS);
  }

  @Test
  public void testSkipToPrevious_lastPerformedActionIsSkipToQueueItem() {
    transportControls.skipToQueueItem(5);

    assertThat(shadowTransportControls.getLastPerformedAction())
        .isEqualTo(ACTION_SKIP_TO_QUEUE_ITEM);
    assertThat(shadowTransportControls.getQueueItemId()).isEqualTo(5);
  }

  @Test
  public void testStop_lastPerformedActionIsStop() {
    transportControls.stop();

    assertThat(shadowTransportControls.getLastPerformedAction()).isEqualTo(ACTION_STOP);
  }
}
