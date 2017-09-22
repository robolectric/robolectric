package org.robolectric.shadows;

import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.VideoView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(VideoView.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ShadowVideoView extends ShadowSurfaceView {
  private MediaPlayer.OnCompletionListener completionListner;
  private MediaPlayer.OnErrorListener errorListener;
  private MediaPlayer.OnPreparedListener preparedListener;

  private Uri uri;
  private String path;
  private int duration = 0;

  public static final int STOP = 0;
  public static final int START = 1;
  public static final int SUSPEND = 2;
  public static final int PAUSE = 3;
  public static final int RESUME = 4;

  private int currentState = -1;
  private int prevState;
  private int currentPosition;

  @Implementation
  public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
    preparedListener = l;
  }

  @Implementation
  public void setOnErrorListener(MediaPlayer.OnErrorListener l) {
    errorListener = l;
  }

  @Implementation
  public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
    completionListner = l;
  }

  @Implementation
  public void setVideoPath(String path) {
    this.path = path;
  }

  @Implementation
  public void setVideoURI(Uri uri) {
    this.uri = uri;
  }

  @Implementation
  public void start() {
    savePrevState();
    currentState = ShadowVideoView.START;
  }

  @Implementation
  public void stopPlayback() {
    savePrevState();
    currentState = ShadowVideoView.STOP;
  }

  @Implementation
  public void suspend() {
    savePrevState();
    currentState = ShadowVideoView.SUSPEND;
  }

  @Implementation
  public void pause() {
    savePrevState();
    currentState = ShadowVideoView.PAUSE;
  }

  @Implementation
  public void resume() {
    savePrevState();
    currentState = ShadowVideoView.RESUME;
  }

  @Implementation
  public boolean isPlaying() {
    return (currentState == ShadowVideoView.START);
  }

  @Implementation
  public boolean canPause() {
    return (currentState != ShadowVideoView.PAUSE &&
        currentState != ShadowVideoView.STOP &&
        currentState != ShadowVideoView.SUSPEND);
  }

  @Implementation
  public void seekTo(int msec) {
    currentPosition = msec;
  }

  @Implementation
  public int getCurrentPosition() {
    return currentPosition;
  }

  @Implementation
  public int getDuration() {
    return duration;
  }

  /**
   * @return On prepared listener.
   */
  public MediaPlayer.OnPreparedListener getOnPreparedListener() {
    return preparedListener;
  }

  /**
   * @return On error listener.
   */
  public MediaPlayer.OnErrorListener getOnErrorListener() {
    return errorListener;
  }

  /**
   * @return On completion listener.
   */
  public MediaPlayer.OnCompletionListener getOnCompletionListener() {
    return completionListner;
  }

  /**
   * @return Video path.
   */
  public String getVideoPath() {
    return path;
  }

  /**
   * @return Video URI.
   */
  public String getVideoURIString() {
    return uri == null ? null : uri.toString();
  }

  /**
   * @return Current video state.
   */
  public int getCurrentVideoState() {
    return currentState;
  }

  /**
   * @return Previous video state.
   */
  public int getPrevVideoState() {
    return prevState;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  private void savePrevState() {
    prevState = currentState;
  }
}
