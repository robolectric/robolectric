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
  protected void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
    preparedListener = l;
  }

  @Implementation
  protected void setOnErrorListener(MediaPlayer.OnErrorListener l) {
    errorListener = l;
  }

  @Implementation
  protected void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
    completionListner = l;
  }

  @Implementation
  protected void setVideoPath(String path) {
    this.path = path;
  }

  @Implementation
  protected void setVideoURI(Uri uri) {
    this.uri = uri;
  }

  @Implementation
  protected void start() {
    savePrevState();
    currentState = ShadowVideoView.START;
  }

  @Implementation
  protected void stopPlayback() {
    savePrevState();
    currentState = ShadowVideoView.STOP;
  }

  @Implementation
  protected void suspend() {
    savePrevState();
    currentState = ShadowVideoView.SUSPEND;
  }

  @Implementation
  protected void pause() {
    savePrevState();
    currentState = ShadowVideoView.PAUSE;
  }

  @Implementation
  protected void resume() {
    savePrevState();
    currentState = ShadowVideoView.RESUME;
  }

  @Implementation
  protected boolean isPlaying() {
    return (currentState == ShadowVideoView.START);
  }

  @Implementation
  protected boolean canPause() {
    return (currentState != ShadowVideoView.PAUSE &&
        currentState != ShadowVideoView.STOP &&
        currentState != ShadowVideoView.SUSPEND);
  }

  @Implementation
  protected void seekTo(int msec) {
    currentPosition = msec;
  }

  @Implementation
  protected int getCurrentPosition() {
    return currentPosition;
  }

  @Implementation
  protected int getDuration() {
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
