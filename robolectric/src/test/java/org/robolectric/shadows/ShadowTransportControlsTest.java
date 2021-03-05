package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowTransportControls}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.LOLLIPOP)
public final class ShadowTransportControlsTest {
  private ShadowTransportControls shadowTransportControls;
  private Bundle testBundle;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    MediaSession mediaSession = new MediaSession(context, "test_media_session");
    MediaController mediaController = new MediaController(context, mediaSession.getSessionToken());
    shadowTransportControls = Shadow.extract(mediaController.getTransportControls());
    testBundle = new Bundle();
    testBundle.putFloat(/* key= */ "test_key", /* defaultValue= */ 1.0f);
  }

  @Test
  public void fastForward_transportControlFastForward() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("fastForward"));
    shadowTransportControls.fastForward();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void pause_transportControlPause() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("pause"));
    shadowTransportControls.pause();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void play_transportControlPlay() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("play"));
    shadowTransportControls.play();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void playFromMediaId_transportControlPlayFromMediaId() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(
        ImmutableList.of("playFromMediaId", /* mediaId */ "media_id", /* extras */ testBundle));
    shadowTransportControls.playFromMediaId(/* mediaId= */ "media_id", /* extras= */ testBundle);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void playFromSearch_transportControlPlayFromSearch() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(
        ImmutableList.of("playFromSearch", /* query */ "query", /* extras */ testBundle));
    shadowTransportControls.playFromSearch(/* query= */ "query", /* extras= */ testBundle);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void rewind_transportControlRewind() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("rewind"));
    shadowTransportControls.rewind();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void seekTo_transportControlSeekTo() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("seekTo", /* pos */ 1L));
    shadowTransportControls.seekTo(/* pos= */ 1L);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void sendCustomAction_customActionArgs_transportControlSendCustomAction() {
    PlaybackState.CustomAction customAction =
        new PlaybackState.CustomAction.Builder(
                /* action= */ "action", /* name= */ "name", /* icon= */ 1)
            .build();
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(
        ImmutableList.of(
            "sendCustomAction", /* customAction */ customAction, /* args */ testBundle));
    shadowTransportControls.sendCustomAction(
        /* customAction= */ customAction, /* args= */ testBundle);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void sendCustomAction_stringArgs_transportControlSendCustomAction() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(
        ImmutableList.of("sendCustomAction", /* action */ "action", /* args */ testBundle));
    shadowTransportControls.sendCustomAction(/* action= */ "action", /* args= */ testBundle);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void setRating_transportControlSetRating() {
    Rating rating = Rating.newHeartRating(true);
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("setRating", /* rating */ rating));
    shadowTransportControls.setRating(/* rating= */ rating);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void skipToNext_transportControlSkipToNext() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("skipToNext"));
    shadowTransportControls.skipToNext();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void skipToPrevious_transportControlSkipToPrevious() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("skipToPrevious"));
    shadowTransportControls.skipToPrevious();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void skipToQueueItem_transportControlSkipToQueueItem() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("skipToQueueItem", /* id */ 1L));
    shadowTransportControls.skipToQueueItem(/* id= */ 1L);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void stop_transportControlStop() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("stop"));
    shadowTransportControls.stop();
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void setRepeatMode_transportControlSetRepeatMode() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("setRepeatMode", /* REPEAT_MODE_ALL */ 1));
    shadowTransportControls.setRepeatMode(/* REPEAT_MODE_ALL */ 1);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }

  @Test
  public void setShuffleMode_transportControlSetShuffleMode() {
    List<Object> listTransportControls = new ArrayList<>();
    listTransportControls.add(ImmutableList.of("setShuffleMode", /* SHUFFLE_MODE_ALL */ 1));
    shadowTransportControls.setShuffleMode(/* SHUFFLE_MODE_ALL */ 1);
    assertThat(shadowTransportControls.getListTransportControls()).isEqualTo(listTransportControls);
  }
}
