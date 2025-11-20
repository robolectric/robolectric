package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.metrics.BundleSession;
import android.media.metrics.EditingSession;
import android.media.metrics.LogSessionId;
import android.media.metrics.MediaMetricsManager;
import android.media.metrics.NetworkEvent;
import android.media.metrics.PlaybackErrorEvent;
import android.media.metrics.PlaybackMetrics;
import android.media.metrics.PlaybackSession;
import android.media.metrics.PlaybackStateEvent;
import android.media.metrics.RecordingSession;
import android.media.metrics.TrackChangeEvent;
import android.media.metrics.TranscodingSession;
import android.os.Build;
import android.os.PersistableBundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowMediaMetricsManager} */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.S)
public class ShadowMediaMetricsManagerTest {

  private MediaMetricsManager mediaMetricsManager;

  @Before
  public void setUp() throws Exception {
    mediaMetricsManager =
        (MediaMetricsManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.MEDIA_METRICS_SERVICE);
  }

  @Test
  public void createPlaybackSession_returnsWorkingPlaybackSession() {
    try (PlaybackSession playbackSession = mediaMetricsManager.createPlaybackSession()) {
      // Call typical methods to assert that none of them throw.
      playbackSession.reportPlaybackMetrics(
          new PlaybackMetrics.Builder().setPlaybackType(PlaybackMetrics.PLAYBACK_TYPE_VOD).build());
      playbackSession.reportPlaybackErrorEvent(
          new PlaybackErrorEvent.Builder()
              .setErrorCode(PlaybackErrorEvent.ERROR_DECODING_FAILED)
              .setException(new IOException())
              .build());
      playbackSession.reportNetworkEvent(
          new NetworkEvent.Builder().setNetworkType(NetworkEvent.NETWORK_TYPE_3G).build());
      playbackSession.reportPlaybackStateEvent(
          new PlaybackStateEvent.Builder().setState(PlaybackStateEvent.STATE_BUFFERING).build());
      playbackSession.reportTrackChangeEvent(
          new TrackChangeEvent.Builder(TrackChangeEvent.TRACK_TYPE_AUDIO).setBitrate(1234).build());
    }
  }

  @Test
  public void createPlaybackSession_multipleCalls_createsNewLogSessionIds() {
    LogSessionId sessionId1;
    LogSessionId sessionId2;
    LogSessionId sessionId3;

    try (PlaybackSession playbackSession = mediaMetricsManager.createPlaybackSession()) {
      sessionId1 = playbackSession.getSessionId();
    }
    try (PlaybackSession playbackSession = mediaMetricsManager.createPlaybackSession()) {
      sessionId2 = playbackSession.getSessionId();
    }
    try (PlaybackSession playbackSession = mediaMetricsManager.createPlaybackSession()) {
      sessionId3 = playbackSession.getSessionId();
    }

    assertThat(sessionId1).isNotEqualTo(sessionId2);
    assertThat(sessionId2).isNotEqualTo(sessionId3);
    assertThat(sessionId1).isNotEqualTo(sessionId3);
  }

  @Test
  public void createRecordingSession_multipleCalls_createsNewLogSessionIds() {
    LogSessionId sessionId1;
    LogSessionId sessionId2;
    LogSessionId sessionId3;

    try (RecordingSession recordingSession = mediaMetricsManager.createRecordingSession()) {
      sessionId1 = recordingSession.getSessionId();
    }
    try (RecordingSession recordingSession = mediaMetricsManager.createRecordingSession()) {
      sessionId2 = recordingSession.getSessionId();
    }
    try (RecordingSession recordingSession = mediaMetricsManager.createRecordingSession()) {
      sessionId3 = recordingSession.getSessionId();
    }

    assertThat(sessionId1).isNotEqualTo(sessionId2);
    assertThat(sessionId2).isNotEqualTo(sessionId3);
    assertThat(sessionId1).isNotEqualTo(sessionId3);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.TIRAMISU)
  public void createTranscodingSession_multipleCalls_createsNewLogSessionIds() {
    LogSessionId sessionId1;
    LogSessionId sessionId2;
    LogSessionId sessionId3;

    try (TranscodingSession transcodingSession = mediaMetricsManager.createTranscodingSession()) {
      sessionId1 = transcodingSession.getSessionId();
    }
    try (TranscodingSession transcodingSession = mediaMetricsManager.createTranscodingSession()) {
      sessionId2 = transcodingSession.getSessionId();
    }
    try (TranscodingSession transcodingSession = mediaMetricsManager.createTranscodingSession()) {
      sessionId3 = transcodingSession.getSessionId();
    }

    assertThat(sessionId1).isNotEqualTo(sessionId2);
    assertThat(sessionId2).isNotEqualTo(sessionId3);
    assertThat(sessionId1).isNotEqualTo(sessionId3);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.TIRAMISU)
  public void createEditingSession_multipleCalls_createsNewLogSessionIds() {
    LogSessionId sessionId1;
    LogSessionId sessionId2;
    LogSessionId sessionId3;

    try (EditingSession editingSession = mediaMetricsManager.createEditingSession()) {
      sessionId1 = editingSession.getSessionId();
    }
    try (EditingSession editingSession = mediaMetricsManager.createEditingSession()) {
      sessionId2 = editingSession.getSessionId();
    }
    try (EditingSession editingSession = mediaMetricsManager.createEditingSession()) {
      sessionId3 = editingSession.getSessionId();
    }

    assertThat(sessionId1).isNotEqualTo(sessionId2);
    assertThat(sessionId2).isNotEqualTo(sessionId3);
    assertThat(sessionId1).isNotEqualTo(sessionId3);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.TIRAMISU)
  public void createBundleSession_returnsWorkingBundleSession() {
    try (BundleSession bundleSession = mediaMetricsManager.createBundleSession()) {
      // Call typical methods to assert that none of them throw.
      bundleSession.reportBundleMetrics(new PersistableBundle());
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.TIRAMISU)
  public void createBundleSession_multipleCalls_createsNewLogSessionIds() {
    LogSessionId sessionId1;
    LogSessionId sessionId2;
    LogSessionId sessionId3;

    try (BundleSession bundleSession = mediaMetricsManager.createBundleSession()) {
      sessionId1 = bundleSession.getSessionId();
    }
    try (BundleSession bundleSession = mediaMetricsManager.createBundleSession()) {
      sessionId2 = bundleSession.getSessionId();
    }
    try (BundleSession bundleSession = mediaMetricsManager.createBundleSession()) {
      sessionId3 = bundleSession.getSessionId();
    }

    assertThat(sessionId1).isNotEqualTo(sessionId2);
    assertThat(sessionId2).isNotEqualTo(sessionId3);
    assertThat(sessionId1).isNotEqualTo(sessionId3);
  }
}
