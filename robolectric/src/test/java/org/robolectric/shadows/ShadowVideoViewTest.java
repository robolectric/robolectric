package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowVideoViewTest {

  private VideoView view;

  @Before public void setUp() throws Exception {
    view = new VideoView(RuntimeEnvironment.application);
  }

  @Test
  public void shouldSetOnPreparedListener() throws Exception {
    TestPreparedListener l = new TestPreparedListener();
    view.setOnPreparedListener(l);
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat((TestPreparedListener) (shadowVideoView.getOnPreparedListener())).isSameAs(l);
  }

  @Test
  public void shouldSetOnErrorListener() throws Exception {
    TestErrorListener l = new TestErrorListener();
    view.setOnErrorListener(l);
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat((TestErrorListener) (shadowVideoView.getOnErrorListener())).isSameAs(l);
  }

  @Test
  public void shouldSetOnCompletionListener() throws Exception {
    TestCompletionListener l = new TestCompletionListener();
    view.setOnCompletionListener(l);
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat((TestCompletionListener) (shadowVideoView.getOnCompletionListener())).isSameAs(l);
  }

  @Test
  public void shouldSetVideoPath() throws Exception {
    view.setVideoPath("video.mp4");
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getVideoPath()).isEqualTo("video.mp4");
    view.setVideoPath(null);
    assertThat(shadowVideoView.getVideoPath()).isNull();
  }

  @Test
  public void shouldSetVideoURI() throws Exception {
    view.setVideoURI(Uri.parse("video.mp4"));
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getVideoURIString()).isEqualTo("video.mp4");
    view.setVideoURI(null);
    assertThat(shadowVideoView.getVideoURIString()).isNull();
  }

  @Test
  public void shouldSetVideoDuration() throws Exception {
    assertThat(view.getDuration()).isEqualTo(0);
    ShadowVideoView shadowVideoView = shadowOf(view);
    shadowVideoView.setDuration(10);
    assertThat(view.getDuration()).isEqualTo(10);
  }

  @Test
  public void shouldDetermineIsPlaying() throws Exception {
    assertThat(view.isPlaying()).isFalse();
    view.start();
    assertThat(view.isPlaying()).isTrue();
    view.stopPlayback();
    assertThat(view.isPlaying()).isFalse();
  }

  @Test
  public void shouldStartPlaying() throws Exception {
    view.start();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.START);
  }

  @Test
  public void shouldStopPlayback() throws Exception {
    view.stopPlayback();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.STOP);
  }

  @Test
  public void shouldSuspendPlaying() throws Exception {
    view.start();
    view.suspend();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getPrevVideoState()).isEqualTo(ShadowVideoView.START);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.SUSPEND);
  }

  @Test
  public void shouldResumePlaying() throws Exception {
    view.start();
    view.suspend();
    view.resume();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getPrevVideoState()).isEqualTo(ShadowVideoView.SUSPEND);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.RESUME);
  }

  @Test
  public void shouldPausePlaying() throws Exception {
    view.start();
    view.pause();
    ShadowVideoView shadowVideoView = shadowOf(view);
    assertThat(shadowVideoView.getPrevVideoState()).isEqualTo(ShadowVideoView.START);
    assertThat(shadowVideoView.getCurrentVideoState()).isEqualTo(ShadowVideoView.PAUSE);
  }

  @Test
  public void shouldDetermineIfPausable() throws Exception {
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
  public void shouldSeekToSpecifiedPosition() throws Exception {
    assertThat(view.getCurrentPosition()).isZero();
    view.seekTo(10000);
    assertThat(view.getCurrentPosition()).isEqualTo(10000);
  }

  private static class TestPreparedListener implements MediaPlayer.OnPreparedListener {
    @Override
    public void onPrepared(MediaPlayer mp) {}
  }

  private static class TestErrorListener implements MediaPlayer.OnErrorListener  {
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
