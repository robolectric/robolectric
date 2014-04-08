package org.robolectric.shadows;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;

import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowMediaPlayer.State.*;

/**
 * Shadows the Android {@code MediaPlayer} class.
 */
@Implements(MediaPlayer.class)
public class ShadowMediaPlayer {
  @SuppressWarnings("UnusedDeclaration")
  public static void __staticInitializer__() {
    // don't bind the JNI library
  }

  @RealObject
  private MediaPlayer player;

  /** Possible states for the media player to be in. */
  public static enum State {
    IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, STOPPED, PAUSED, PLAYBACK_COMPLETED, END, ERROR
  }

  /** Current state of the media player. */
  private State state = IDLE;

  /** Delay for calls to {@link #prepare} and {@link #prepareAsync} (in ms). */
  private int preparationDelay = 0;

  /** Delay for calls to {@link #seekTo} (in ms). */
  private int seekDelay = 0;

  private int auxEffect;
  private int audioSessionId;
  private int duration;
  private boolean looping;
  private int pendingSeek = -1;
  /** Various source variables from setDataSource() */
  private String sourcePath;
  private Uri sourceUri;
  private Map<String, String> sourceHeaders;
  private int sourceResId;
  private FileDescriptor sourceFd;
  private long sourceOffset = -1;
  private long sourceLength = -1;

  /** The time (in ms) at which playback was last started/resumed. */
  private long startTime = 0;

  /**
   * The offset (in ms) from the start of the current clip at which the last
   * call to seek/pause was. If the MediaPlayer is not in the STARTED state,
   * then this is equal to currentPosition; if it is in the STARTED state and no
   * seek is pending then you need to add the number of ms since start() was
   * called to get the current position (see {@link #startTime}).
   */
  private int startOffset = 0;

  private int videoHeight;
  private int videoWidth;
  private MediaPlayer.OnCompletionListener completionListener;
  private MediaPlayer.OnSeekCompleteListener seekCompleteListener;
  private MediaPlayer.OnPreparedListener preparedListener;
  private MediaPlayer.OnErrorListener errorListener;
  private Handler handler;

  private final Runnable completionCallback = new Runnable() {
    public void run() {
      if (looping) {
        startOffset = 0;
        doStart();
      } else {
        invokeCompletionListener();
      }
    }
  };

  private final Runnable preparedCallback = new Runnable() {
    public void run() {
      invokePreparedListener();
    }
  };

  private final Runnable seekCompleteCallback = new Runnable() {
    public void run() {
      invokeSeekCompleteListener();
    }
  };

  /**
   * Playback offset at which to fire the scheduled error. Measured in ms
   * relative to the start of playback. Negative indicates no error scheduled.
   */
  private int errorOffset = -1;

  private class ErrorCallback implements Runnable {
    public int what;
    public int extra;

    public void run() {
      invokeErrorListener(what, extra);
    }
  }

  /** Callback to use for scheduled errors. */
  private final ErrorCallback errorCallback = new ErrorCallback();

  /** Exception to throw when {@link #setDataSource} is called. */
  private Exception setDataSourceException;

  @Implementation
  public static MediaPlayer create(Context context, int resId) {
    MediaPlayer mp = new MediaPlayer();
    Shadows.shadowOf(mp).sourceResId = resId;
    try {
      mp.prepare();
    } catch (Exception e) {
      return null;
    }

    return mp;
  }

  @Implementation
  public static MediaPlayer create(Context context, Uri uri) {
    MediaPlayer mp = new MediaPlayer();
    try {
      mp.setDataSource(context, uri);
      mp.prepare();
    } catch (Exception e) {
      return null;
    }

    return mp;
  }

  public void __constructor__() {
    // Contract of audioSessionId is that if it is 0 (which represents
    // the master mix) then that's an error. By default it generates
    // an ID that is unique system-wide. We could simulate guaranteed
    // uniqueness (get AudioManager's help?) but it's probably not
    // worth the effort.
    Random random = new Random();
    audioSessionId = random.nextInt(Integer.MAX_VALUE) + 1;
    handler = new Handler();
  }

  @Implementation
  public void setDataSource(String path) throws IOException {
    doSetDataSourceExceptions();
    this.sourcePath = path;
  }

  @Implementation
  public void setDataSource(Context context, Uri uri,
      Map<String, String> headers) throws IOException {
    doSetDataSourceExceptions();
    this.sourceUri = uri;
    this.sourceHeaders = headers;
  }

  @Implementation
  public void setDataSource(FileDescriptor fd, long offset, long length)
    throws IOException {
    doSetDataSourceExceptions();
    this.sourceFd = fd;
    this.sourceOffset = offset;
    this.sourceLength = length;
  }

  static private final EnumSet<State> idleState = EnumSet.of(IDLE); 
  private void doSetDataSourceExceptions() throws IOException {
    checkState("setDataSource()", idleState);
    if (setDataSourceException != null) {
      state = ERROR;
      if (setDataSourceException instanceof IOException) {
        throw (IOException)setDataSourceException;
      }
      if (setDataSourceException instanceof RuntimeException) {
        throw (RuntimeException)setDataSourceException;
      }  
      throw new AssertionError("Invalid exception type specified: " + setDataSourceException);
    }    
    state = INITIALIZED;
  }

  private void checkState(String method, EnumSet<State> allowedStates) {
    if (state == END) {
      String msg = "Can't call " + method + " from state " + state;
      throw new IllegalStateException(msg);      
    }
    if (!allowedStates.contains(state)) {
      String msg = "Can't call " + method + " from state " + state;
      state = ERROR;
      throw new IllegalStateException(msg);
    }
  }
  
  @Implementation
  public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
    completionListener = listener;
  }

  @Implementation
  public void setOnSeekCompleteListener(
      MediaPlayer.OnSeekCompleteListener listener) {
    seekCompleteListener = listener;
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
  public boolean isLooping() {
    return looping;
  }

  static private final EnumSet<State> nonErrorStates =
    EnumSet.complementOf(EnumSet.of(ERROR));
  @Implementation
  public void setLooping(boolean looping) {
    checkState("setLooping()", nonErrorStates);
    this.looping = looping;
  }

  @Implementation
  public boolean isPlaying() {
    checkState("setLooping()", nonErrorStates);
    return state == STARTED;
  }

  private static EnumSet<State> preparableStates =
      EnumSet.of(INITIALIZED, STOPPED);

  @Implementation
  public void prepare() {
    checkState("prepare()", preparableStates);
    if (preparationDelay > 0) {
      SystemClock.sleep(preparationDelay);
    }
    invokePreparedListener();
  }

  /**
   * Test cases are expected to simulate completion of the 'prepare' phase by
   * manually invoking {@code #invokePreparedListener}.
   */
  @Implementation
  public void prepareAsync() {
    checkState("prepareAsync()", preparableStates);
    state = PREPARING;
    if (preparationDelay >= 0) {
      handler.postDelayed(preparedCallback, preparationDelay);
    }
  }

  private static EnumSet<State> startableStates =
    EnumSet.of(PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED);

  @Implementation
  public void start() {
    checkState("start()", startableStates);
    if (state == PLAYBACK_COMPLETED) {
      startOffset = 0;
    }
    state = STARTED;
    doStart();
  }

  private void doStart() {
    startTime = SystemClock.uptimeMillis();
    if (errorOffset >= startOffset) {
      handler.postDelayed(errorCallback, errorOffset - startOffset);
    } else {
      handler.postDelayed(completionCallback, duration - startOffset);
    }
  }

  private void doStop() {
    startOffset = getCurrentPosition();
    handler.removeCallbacks(completionCallback);
    handler.removeCallbacks(errorCallback);
  }

  private static final EnumSet<State> pausableStates = EnumSet.of(STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  @Implementation
  public void pause() {
    checkState("pause()", pausableStates);
    doStop();
    state = PAUSED;
  }

  static final EnumSet<State> allStates = EnumSet.allOf(State.class);
  @Implementation
  public void release() {
    checkState("release()", allStates);
    state = END;
  }

  @Implementation
  public void reset() {
    checkState("reset()", allStates);
    state = IDLE;
  }

  static private final EnumSet<State> stoppableStates =
      EnumSet.of(PREPARED, STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED);

  @Implementation
  public void stop() {
    checkState("stop()", stoppableStates);
    doStop();
    state = STOPPED;
  }

  private static final EnumSet<State> attachableStates = EnumSet
      .of(INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED,
          PLAYBACK_COMPLETED);

  @Implementation
  public void attachAuxEffect(int effectId) {
    checkState("attachAuxEffect()", attachableStates);
    auxEffect = effectId;
  }

  @Implementation
  public int getAudioSessionId() {
    checkState("getAudioSessionId()", allStates);
    return audioSessionId;
  }

  @Implementation
  public int getCurrentPosition() {
    checkState("getCurrentPosition()", nonErrorStates);
    int currentPos = startOffset;
    if (state == STARTED && pendingSeek < 0) {
      currentPos += (int) (SystemClock.uptimeMillis() - startTime);
    }
    return currentPos;
  }

  @Implementation
  public int getDuration() {
    checkState("getDuration()", stoppableStates);
    return duration;
  }

  @Implementation
  public int getVideoHeight() {
    checkState("getVideoHeight()", nonErrorStates);
    return videoHeight;
  }

  @Implementation
  public int getVideoWidth() {
    checkState("getVideoWidth()", nonErrorStates);
    return videoWidth;
  }

  private static final EnumSet<State> seekableStates =
    EnumSet.of(PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED);

  /**
   * Simulates seeking to specified position. The seek will complete after
   * {@link #seekDelay} ms (defaults to 0), or else if seekDelay is negative
   * then the controlling test is expected to simulate seek completion by
   * manually invoking {@link #invokeSeekCompletedListener}.
   * 
   * @param seekTo
   *          the offset (in ms) from the start of the track to seek to.
   */
  @Implementation
  public void seekTo(int seekTo) {
    checkState("seekTo()", seekableStates);
    // Cancel any pending seek operations.
    handler.removeCallbacks(seekCompleteCallback);
    // Need to call doStop() before setting pendingSeek,
    // because if pendingSeek is called it changes
    // the behavior of getCurrentPosition(), which doStop()
    // depends on.
    doStop();
    pendingSeek = seekTo;
    if (seekDelay >= 0) {
      handler.postDelayed(seekCompleteCallback, seekDelay);
    }
  }

  @Implementation
  public void setAudioSessionId(int sessionId) {
    checkState("setAudioSessionId()", idleState);
    audioSessionId = sessionId;
  }

  /**
   * Retrieves the Handler object used by this <code>ShadowMediaPlayer</code>.
   * Can be used for posting custom asynchronous events to the thread (eg,
   * asynchronous errors). Use this for scheduling events to take place at a
   * particular "real" time (ie, time as measured by the scheduler). For
   * scheduling errors to occur at a particular point in playback (no matter how
   * long playback may be paused for, or where you seek to, etc), see
   * {@link #scheduleErrorAtOffset}. Non-Android accessor.
   * 
   * @return Handler object that can be used to schedule asynchronous events on
   *         this media player.
   */
  public Handler getHandler() {
    return handler;
  }

  /**
   * Sets the exception to throw when setDataSource() is called.
   * <code>null</code> means no exception will be thrown. Note that emulation of
   * IllegalStateException is already handled by the shadow implementation of
   * setDataSource() and does not need to be emulated using this method.
   * 
   * @param e the exception to be thrown.
   */
  public void setSetDataSourceException(Exception e) {
    if (e != null
        && !(e instanceof IOException)
        && !(e instanceof RuntimeException) ) {
      throw new AssertionError("Invalid exception type: " + e);
    }
    setDataSourceException = e;
  }

  /**
   * Non-Android setter.
   * 
   * @param position
   */
  public void setCurrentPosition(int position) {
    startOffset = position;
  }

  /**
   * Non-Android setter.
   * 
   * @param duration
   */

  public void setDuration(int duration) {
    this.duration = duration;
  }

  /**
   * Non-Android accessor. Used for assertions.
   * 
   * @return The current state of the {@link MediaPlayer}, as defined in the
   *         MediaPlayer documentation.
   * @see MediaPlayer
   */
  public State getState() {
    return state;
  }

  /**
   * Non-Android setter.
   * 
   * @param state
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * Non-Android accessor.
   * 
   * @return preparationDelay
   */
  public int getPreparationDelay() {
    return preparationDelay;
  }

  /**
   * Sets the length of time that prepare()/prepareAsync() will wait for before
   * completing. Default is 0. If set to -1, then prepare() will complete
   * instantly but prepareAsync() will not call the OnPreparedListener
   * automatically; you will need to call invokePreparedListener() manually.
   * 
   * @param preparationDelay
   */
  public void setPreparationDelay(int preparationDelay) {
    this.preparationDelay = preparationDelay;
  }

  /**
   * Non-Android accessor.
   * 
   * @return seekDelay
   */
  public int getSeekDelay() {
    return seekDelay;
  }

  /**
   * Sets the length of time (ms) that seekTo() will delay before completing.
   * Default is 0. If set to -1, then seekTo() will not call the
   * OnSeekCompleteListener automatically; you will need to call
   * invokeSeekCompleteListener() manually.
   * 
   * @param seekDelay
   *          length of time to delay (ms)
   */
  public void setSeekDelay(int seekDelay) {
    this.seekDelay = seekDelay;
  }

  /**
   * Non-Android accessor. Used for assertions.
   * 
   * @return
   */
  public int getAuxEffect() {
    return auxEffect;
  }

  /**
   * Non-Android accessor. Used for assertions.
   * 
   * @return the position to which the shadow player is seeking for the seek in
   *         progress (ie, after the call to {@link #seekTo} but before a call
   *         to invokeOnSeekCompleteListener). Returns -1 if no seek is in
   *         progress.
   */
  public int getPendingSeek() {
    return pendingSeek;
  }

  /**
   * Non-Android accessor. Use for assertions.
   * 
   * @return
   */
  public String getSourcePath() {
    return sourcePath;
  }

  /**
   * Non-Android accessor. Use for assertions.
   * 
   * @return
   */
  public Uri getSourceUri() {
    return sourceUri;
  }

  /**
   * Non-Android accessor. Use for assertions.
   * 
   * @return
   */
  public Map<String, String> getSourceHeaders() {
    return sourceHeaders;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   *
   * @return The Source Res ID
   */
  public int getSourceResId() {
    return sourceResId;
  }

  private static EnumSet<State> preparedStates =
    EnumSet.of(PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED);

  /**
   * Non-Android accessor. Use for assertions. This is mainly used for backward
   * compatibility. {@link #getState} may be more useful.
   * 
   * @return true if the MediaPlayer is in the PREPARED state, false otherwise.
   */
  public boolean isPrepared() {
    return preparedStates.contains(state);
  }

  /**
   * Non-Android accessor.  Use for assertions.
   *
   * @return the OnCompletionListener
   */
  public MediaPlayer.OnCompletionListener getOnCompletionListener() {
    return completionListener;
  }

  /**
   * Non-Android accessor.  Use for assertions.
   * @return the OnPreparedListener
   */
  public MediaPlayer.OnPreparedListener getOnPreparedListener() {
    return preparedListener;
  }

  /**
   * Allows test cases to simulate 'prepared' state by invoking callback.
   */
  public void invokePreparedListener() {
    state = PREPARED;
    if (preparedListener == null)
      return;
    preparedListener.onPrepared(player);
  }

  /**
   * Allows test cases to simulate 'completed' state by invoking callback.
   */
  public void invokeCompletionListener() {
    state = PLAYBACK_COMPLETED;
    setCurrentPosition(duration);
    if (completionListener == null)
      return;
    completionListener.onCompletion(player);
  }

  /**
   * Allows test cases to simulate seek completion by invoking callback.
   */
  public void invokeSeekCompleteListener() {
    setCurrentPosition(pendingSeek > duration ? duration : pendingSeek < 0 ? 0
        : pendingSeek);
    pendingSeek = -1;
    if (state == STARTED) {
      doStart();
    }
    if (seekCompleteListener == null)
      return;
    seekCompleteListener.onSeekComplete(player);
  }

  /**
   * Schedules a callback to {@link #invokeErrorListener(int, int) invoke
   * ErrorListener()} at the given playback offset.
   * 
   * @param offset
   *          the offset (in ms) from the start of playback at which to fire the
   *          error. Setting to a negative number effectively disabled the
   *          scheduled error.
   * @param what
   *          parameter to pass in to <code>what</code> in
   *          {@link OnErrorListener#onError()}.
   * @param extra
   *          parameter to pass in to <code>extra</code> in
   *          {@link OnErrorListener#onError()}.
   */
  public void scheduleErrorAtOffset(int offset, int what, int extra) {
    errorOffset = offset;
    errorCallback.what = what;
    errorCallback.extra = extra;
    if (state == STARTED) {
      // If we're already in the STARTED state then we need
      // to reschedule the pending error/completion callback.
      doStop();
      doStart();
    }
  }

  /**
   * Allows test cases to directly simulate invocation of the OnError event.
   * 
   * @param what
   *          parameter to pass in to <code>what</code> in
   *          {@link OnErrorListener#onError()}.
   * @param extra
   *          parameter to pass in to <code>extra</code> in
   *          {@link OnErrorListener#onError()}.
   */
  public void invokeErrorListener(int what, int extra) {
    state = ERROR;
    handler.removeCallbacks(completionCallback);
    boolean handled = errorListener != null
        && errorListener.onError(player, what, extra);
    if (!handled) {
      // XXX
      // The documentation isn't very clear if onCompletion is
      // supposed to be called from non-playing states
      // (ie, states other than STARTED or PAUSED). It
      // wouldn't seem to make sense to me to notify
      // of completion before it has started...
      invokeCompletionListener();
      // Need to set this again because
      // invokeCompletionListener() will set the state
      // to PLAYBACK_COMPLETED
      state = ERROR;
    }
  }
}
