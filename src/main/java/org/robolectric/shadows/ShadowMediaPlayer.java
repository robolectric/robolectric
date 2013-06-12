package org.robolectric.shadows;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Robolectric.shadowOf;

/**
 * Shadows the Android {@code MediaPlayer} class.
 */
@Implements(MediaPlayer.class)
public class ShadowMediaPlayer {
  @SuppressWarnings("UnusedDeclaration")
  public static void __staticInitializer__() {
    // don't bind the JNI library
  }

  @RealObject private MediaPlayer player;

  private boolean playing;
  private boolean prepared;
  private int currentPosition;
  private Uri sourceUri;
  private int sourceResId;
  private MediaPlayer.OnCompletionListener completionListener;
  private MediaPlayer.OnPreparedListener preparedListener;
  private MediaPlayer.OnErrorListener errorListener;

  @Implementation
  public static MediaPlayer create(Context context, int resId) {
    MediaPlayer mp = new MediaPlayer();
    shadowOf(mp).sourceResId = resId;
    try {
      mp.prepare();
    } catch (Exception e) { return null; }

    return mp;
  }

  @Implementation
  public static MediaPlayer create(Context context, Uri uri) {
    MediaPlayer mp = new MediaPlayer();
    try {
      mp.setDataSource(context, uri);
      mp.prepare();
    } catch (Exception e) { return null; }

    return mp;
  }

  public void __constructor__() {
    playing = true;
  }

  @Implementation
  public void  setDataSource(Context context, Uri uri) {
    this.sourceUri = uri;
  }

  @Implementation
  public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
    completionListener = listener;
  }

  @Implementation
  public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
    preparedListener = listener;
  }

  @Implementation
  public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
    errorListener = listener;
  }



  @Implementation
  public boolean isPlaying() {
    return playing;
  }

  @Implementation
  public void prepare() {
    prepared = true;
    invokePreparedListener();
  }

  /**
   * Test cases are expected to simulate completion of the 'prepare' phase
   * by manually invoking {@code #invokePreparedListener}.
   */
  @Implementation
  public void prepareAsync() {
    prepared = true;
  }

  @Implementation
  public void start() {
    playing = true;
  }

  @Implementation
  public void pause() {
    playing = false;
  }

  @Implementation
  public void release() {
    playing = false;
    prepared = false;
  }

  @Implementation
  public void reset() {
    playing = false;
    prepared = false;
  }

  @Implementation
  public void stop() {
    playing = false;
  }

  @Implementation
  public int getCurrentPosition() {
    return currentPosition;
  }

  public void setCurrentPosition(int position) {
    currentPosition = position;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   * @return
   */
  public Uri getSourceUri() {
    return sourceUri;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   * @return
   */
  public int getSourceResId() {
    return sourceResId;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   * @return
   */
  public boolean isPrepared() {
    return prepared;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   * @return
   */
  public MediaPlayer.OnCompletionListener getOnCompletionListener() {
    return completionListener;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   * @return
   */
  public MediaPlayer.OnPreparedListener getOnPreparedListener() {
    return preparedListener;
  }

  /**
   * Allows test cases to simulate 'prepared' state by invoking callback.
   */
  public void invokePreparedListener() {
    if (preparedListener == null) return;
    preparedListener.onPrepared( player );
  }

  /**
   * Allows test cases to simulate 'completed' state by invoking callback.
   */
  public void invokeCompletionListener() {
    if (completionListener == null) return;
    completionListener.onCompletion( player );
  }


  public void invokeErrorListener(int what,int extra){
    playing = false;
    prepared = false;
    if (errorListener == null) return;
    boolean handled = errorListener.onError(player,what,extra);
    if (!handled){
      invokeCompletionListener();
    }

  }
}
