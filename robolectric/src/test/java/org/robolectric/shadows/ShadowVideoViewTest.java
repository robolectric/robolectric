package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowVideoViewTest {

  private VideoView view;

  @Before
  public void setUp() throws Exception {
    view = new VideoView(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void shouldSetOnPreparedListener() {
    TestPreparedListener l = new TestPreparedListener();
    view.setOnPreparedListener(l);
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getOnPreparedListener()).isSameInstanceAs(l);
  }

  @Test
  public void shouldSetOnErrorListener() {
    TestErrorListener l = new TestErrorListener();
    view.setOnErrorListener(l);
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getOnErrorListener()).isSameInstanceAs(l);
  }

  @Test
  public void shouldSetOnCompletionListener() {
    TestCompletionListener l = new TestCompletionListener();
    view.setOnCompletionListener(l);
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getOnCompletionListener()).isSameInstanceAs(l);
  }

  @Test
  public void shouldSetVideoPath() {
    view.setVideoPath("video.mp4");
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getVideoPath()).isEqualTo("video.mp4");
    view.setVideoPath(null);
    assertThat(shadowVideoView.getVideoPath()).isNull();
  }

  @Test
  public void shouldSetVideoURI() {
    view.setVideoURI(Uri.parse("video.mp4"));
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getVideoURIString()).isEqualTo("video.mp4");
    view.setVideoURI(null);
    assertThat(shadowVideoView.getVideoURIString()).isNull();
  }

  @Test
  public void shouldSetVideoDuration() {
    assertThat(view.getDuration()).isEqualTo(0);
    ShadowVideoView shadowVideoView = shadowOf(view);
    shadowVideoView.setDuration(10);
    assertThat(view.getDuration()).isEqualTo(10);
  }

  @Test
  public void shouldDetermineIsPlaying() {
    assertThat(view.isPlaying()).isFalse();
    view.start();
    assertThat(view.isPlaying()).isTrue();
    view.stopPlayback();
    assertThat(view.isPlaying()).isFalse();
  }

  @Test
  public void shouldStartPlaying() {
    view.start();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.START);
  }

  @Test
  public void shouldStopPlayback() {
    view.stopPlayback();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.STOP);
  }

  @Test
  public void shouldSuspendPlaying() {
    view.start();
    view.suspend();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getPrevVideoState()).isEqualTo(ShadowVideoView.START);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.SUSPEND);
  }

  @Test
  public void shouldResumePlaying() {
    view.start();
    view.suspend();
    view.resume();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getPrevVideoState()).isEqualTo(ShadowVideoView.SUSPEND);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.RESUME);
  }

  @Test
  public void shouldPausePlaying() {
    view.start();
    view.pause();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getPrevVideoState()).isEqualTo(ShadowVideoView.START);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.PAUSE);
  }

  @Test
  public void shouldDetermineIfPausable() {
    view.start();
    assertThat(view.canPause()).isTrue();

    view.pause();
    assertThat(view.canPause()).isFalse();

    view.resume();
    assertThat(view.canPause()).isTrue();

    view.suspend();
    assertThat(view.canPause()).isFalse();
  }

  @Test
  public void shouldSeekToSpecifiedPosition() {
    assertThat(view.getCurrentPosition()).isEqualTo(0);
    view.seekTo(10000);
    assertThat(view.getCurrentPosition()).isEqualTo(10000);
  }

  private static class TestPreparedListener implements MediaPlayer.OnPreparedListener {
    @Override
    public void onPrepared(MediaPlayer mp) {}
  }

  private static class TestErrorListener implements MediaPlayer.OnErrorListener {
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
      return false;
    }
  }

  private static class TestCompletionListener implements MediaPlayer.OnCompletionListener {
    @Override
    public void onCompletion(MediaPlayer mp) {}
  }
}
