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

  public static final int STOP = 0;
  public static final int START = 1;
  public static final int SUSPEND = 2;
  public static final int PAUSE = 3;
  public static final int RESUME = 4;

  private int currentState = -1;
  private int prevState;

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

  /**
   * Non-Android accessor.
   */
  public MediaPlayer.OnPreparedListener getOnPreparedListener() {
    return preparedListener;
  }

  /**
   * Non-Android accessor.
   * @return
   */
  public MediaPlayer.OnErrorListener getOnErrorListener() {
    return errorListener;
  }

  /**
   * Non-Android accessor.
   * @return
   */
  public MediaPlayer.OnCompletionListener getOnCompletionListener() {
    return completionListner;
  }

  /**
   * Non-Android accessor.
   * @return
   */
  public String getVideoPath() {
    return path;
  }

  /**
   * Non-Android accessor.
   * @return
   */
  public String getVideoURIString() {
    return uri == null ? null : uri.toString();
  }

  /**
   * Non-Android accessor.
   * @return
   */
  public int getCurrentVideoState() {
    return currentState;
  }

  /**
   * Non-Android accessor.
   * @return
   */
  public int getPrevVideoState() {
    return prevState;
  }

  /**
   * Non-Android accessor.
   */
  private void savePrevState() {
    prevState = currentState;
  }

}
