package org.robolectric.shadows;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.util.DataSource;

import com.google.android.collect.Maps;

import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowMediaPlayer.State.*;
import static org.robolectric.shadows.util.DataSource.toDataSource;

/**
 * Shadows the Android {@code MediaPlayer} class.
 * 
 * Automated testing of media playback can be a difficult thing - especially
 * testing that your code properly handles asynchronous errors and events. This
 * near impossible task is made quite straightforward using this implementation
 * of <code>ShadowMediaPlayer</code> with Robolectric.
 * 
 * This shadow implementation provides much of the functionality needed to
 * emulate {@link MediaPlayer} initialization & playback behavior without having
 * to play actual media files. A summary of the features included are:
 * 
 * <ul>
 * <li>Construction-time callback hook {@link CreateListener} so that
 * newly-created {@link MediaPlayer} instances can have their shadows configured
 * before they are used.</li>
 * <li>Emulation of the {@link android.media.MediaPlayer.OnCompletionListener
 * OnCompletionListener}, {@link android.media.MediaPlayer.OnErrorListener
 * OnErrorListener}, {@link android.media.MediaPlayer.OnInfoListener
 * OnInfoListener}, {@link android.media.MediaPlayer.OnPreparedListener
 * OnPreparedListener} and
 * {@link android.media.MediaPlayer.OnSeekCompleteListener
 * OnSeekCompleteListener}.</li>
 * <li>Full support of the {@link MediaPlayer} internal states and their
 * transition map.</li>
 * <li>Configure time parameters such as playback duration, preparation delay
 * and (@link #setSeekDelay seek delay}.</li>
 * <li>Emulation of asynchronous callback events during playback through
 * Robolectric's scheduling system using the {@link MediaInfo} inner class.</li>
 * <li>Emulation of error behavior when methods are called from invalid states,
 * or to throw assertions when methods are invoked in invalid states (using
 * {@link #setAssertOnError}).</li>
 * <li>Emulation of different playback behaviors based on the current data
 * source, as passed in to {@link #setDataSource(String)}, using
 * {@link #setDataSourceMap}.</li>
 * <li>Emulation of exceptions when calling {@link #setDataSource} using
 * {@link MediaInfo#setDataSourceException}.</li>
 * </ul>
 * 
 * Known gaps in the current feature set are:
 * 
 * <ul>
 * <li>The current features of <code>ShadowMediaPlayer</code> were developed for
 * testing playback of audio tracks. Thus support for emulating timed text and
 * video events is incomplete.</li>
 * <li>The {@link #setDataSourceMap(Map) dataSourceMap} currently only supports
 * the {@link #setDataSource(String)} method. Other overloaded forms of
 * <code>setDataSource()</code> are not yet supported.</li>
 * <li>Due to what appears to be a bug in Robolectric's {@link Scheduler}/
 * {@link ShadowHandler} implementation, some events (including manually
 * scheduled events) can fire after the {@link MediaPlayer} is
 * {@link MediaPlayer#release()}d.</li>
 * feature-complete while reducing the maintenance overhead.</li>
 * </ul>
 * 
 * None of these features/bugs would be particularly onerous to add/fix - contributions
 * welcome.
 * 
 * @author Fr Jeremy Krieg, Holy Monastery of St Nectarios, Adelaide, Australia
 */
@Implements(MediaPlayer.class)
public class ShadowMediaPlayer {
  public static void __staticInitializer__() {
    // don't bind the JNI library
  }

  /**
   * Listener that is called when a new MediaPlayer is constructed.
   * 
   * @see #setCreateListener(CreateListener)
   */
  protected static CreateListener createListener;
  
  private static final Map<DataSource, Exception> exceptions = Maps.newHashMap();

  @RealObject
  private MediaPlayer player;

  /**
   * Possible states for the media player to be in. These states are as defined
   * in the documentation for {@link android.media.MediaPlayer}.
   */
  public static enum State {
    IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, STOPPED, PAUSED, PLAYBACK_COMPLETED, END, ERROR
  }

  /**
   * Possible behavior modes for the media player when a method is invoked in an
   * invalid state.
   * 
   * @see #setInvalidStateBehavior
   */
  public static enum InvalidStateBehavior {
    SILENT, EMULATE, ASSERT
  }

  /**
   * Reference to the next playback event scheduled to run. We keep a reference
   * to this handy in case we need to cancel it.
   */
  private RunList nextPlaybackEvent;

  /**
   * Class for grouping events that are meant to fire at the same time. Also
   * schedules the next event to run.
   */
  @SuppressWarnings("serial")
  private class RunList extends ArrayList<Runnable> implements Runnable {

    public RunList() {
      // Set the default size to one as most of the time we will
      // only have one event.
      super(1);
    }

    public void run() {
      for (Runnable r : this) {
        r.run();
      }
      scheduleNextPlaybackEvent();
    }
  }

  /**
   * Class specifying information for an emulated media object. Used by
   * ShadowMediaPlayer when setDataSource() is called to populate the shadow
   * player with the specified values.
   */
  public class MediaInfo {
    public int duration;
    private int preparationDelay;

    /** Exception to throw when {@link #setDataSource} is called. */
    private Exception setDataSourceException;

    /** Map that maps time offsets to runnable events. */
    public TreeMap<Integer, RunList> events = new TreeMap<Integer, RunList>();

    /**
     * Creates a new <code>MediaInfo</code> object with the given duration and
     * preparation delay. A completion callback event is scheduled at
     * <code>duration</code> ms from the end.
     * 
     * @param duration
     *          the duration (in ms) of this emulated media. A callback event
     *          will be scheduled at this offset to stop playback simulation &
     *          invoke the completion callback.
     * @param preparationDelay
     *          the preparation delay (in ms) to emulate for this media. If set
     *          to -1, then {@link #prepare()} will complete instantly but
     *          {@link #prepareAsync()} will not complete automatically; you
     *          will need to call {@link #invokePreparedListener()} manually.
     */
    public MediaInfo(int duration, int preparationDelay) {
      this.duration = duration;
      this.preparationDelay = preparationDelay;

      scheduleEventAtOffset(duration, completionCallback);
    }

    /**
     * Retrieves the current preparation delay for this media.
     * 
     * @return The current preparation delay (in ms).
     */
    public int getPreparationDelay() {
      return preparationDelay;
    }

    /**
     * Sets the current preparation delay for this media.
     * 
     * @param preparationDelay
     *          the new preparation delay (in ms).
     */
    public void setPreparationDelay(int preparationDelay) {
      this.preparationDelay = preparationDelay;
    }

    /**
     * Gets the exception that will be thrown when setDataSource() is called.
     * 
     * @return The exception that will be thrown when setDataSource() is called.
     */
    public Exception getSetDataSourceException() {
      return setDataSourceException;
    }

    /**
     * Sets the exception to throw when setDataSource() is called.
     * <code>null</code> means no exception will be thrown. Note that emulation
     * of IllegalStateException is already handled by the shadow implementation
     * of setDataSource() and does not need to be emulated using this method.
     * 
     * @param e
     *          the exception to be thrown.
     */
    public void setSetDataSourceException(Exception e) {
      if (e != null && !(e instanceof IOException)
          && !(e instanceof RuntimeException)) {
        throw new AssertionError("Invalid exception type: " + e);
      }
      setDataSourceException = e;
    }

    /**
     * Schedules a generic event to run at the specified playback offset. Events
     * are run on the thread on which the {@link android.media.MediaPlayer
     * MediaPlayer} was created.
     * 
     * @param offset
     *          the offset from the start of playback at which this event will
     *          run.
     * @param event
     *          the event to run.
     */
    public void scheduleEventAtOffset(int offset, Runnable event) {
      RunList runList = events.get(offset);
      final boolean reallyPlaying = isReallyPlaying();
      if (reallyPlaying) {
        // Need to stop while modifying the collections
        // otherwise we can cause a ConcurrentModificationException
        doStop();
      }
      if (runList == null) {
        // Given that most run lists will only contain one event,
        // we use 1 as the default capacity.
        runList = new RunList();
        events.put(offset, runList);
      }
      runList.add(event);
      // If we were playing make sure we restart again to reschedule
      // the new events.
      if (reallyPlaying) {
        doStart();
      }
    }

    /**
     * Schedules an error event to run at the specified playback offset. A
     * reference to the actual Runnable that is scheduled is returned, which can
     * be used in a subsequent call to {@link #removeEventAtOffset}.
     * 
     * @param offset
     *          the offset from the start of playback at which this error will
     *          trigger.
     * @param what
     *          the value for the <code>what</code> parameter to use in the call
     *          to
     *          {@link android.media.MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)
     *          onError()}.
     * @param extra
     *          the value for the <code>extra</code> parameter to use in the
     *          call to
     *          {@link android.media.MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)
     *          onError()}.
     * @return A reference to the Runnable object that was created & scheduled.
     */
    public Runnable scheduleErrorAtOffset(int offset, int what, int extra) {
      ErrorCallback callback = new ErrorCallback(what, extra);
      scheduleEventAtOffset(offset, callback);
      return callback;
    }

    /**
     * Schedules an info event to run at the specified playback offset. A
     * reference to the actual Runnable that is scheduled is returned, which can
     * be used in a subsequent call to {@link #removeEventAtOffset}.
     * 
     * @param offset
     *          the offset from the start of playback at which this event will
     *          trigger.
     * @param what
     *          the value for the <code>what</code> parameter to use in the call
     *          to
     *          {@link android.media.MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int)
     *          onInfo()}.
     * @param extra
     *          the value for the <code>extra</code> parameter to use in the
     *          call to
     *          {@link android.media.MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int)
     *          onInfo()}.
     * @return A reference to the Runnable object that was created & scheduled.
     */
    public Runnable scheduleInfoAtOffset(int offset, final int what,
        final int extra) {
      Runnable callback = new Runnable() {
        public void run() {
          invokeInfoListener(what, extra);
        }
      };
      scheduleEventAtOffset(offset, callback);
      return callback;
    }

    /**
     * Schedules a simulated buffer underrun event to run at the specified
     * playback offset. A reference to the actual Runnable that is scheduled is
     * returned, which can be used in a subsequent call to
     * {@link #removeEventAtOffset}.
     * 
     * This event will issue an {@link MediaPlayer.OnInfoListener#onInfo
     * onInfo()} callback with {@link MediaPlayer#MEDIA_INFO_BUFFERING_START} to
     * signal the start of buffering and then call {@link #doStop()} to
     * internally pause playback. Finally it will schedule an event to fire
     * after <code>length</code> ms which fires a
     * {@link MediaPlayer#MEDIA_INFO_BUFFERING_END} info event and invokes
     * {@link #doStart()} to resume playback.
     * 
     * @param offset
     *          the offset from the start of playback at which this underrun
     *          will trigger.
     * @param length
     *          the length of time (in ms) for which playback will be paused.
     * @return A reference to the Runnable object that was created & scheduled.
     */
    public Runnable scheduleBufferUnderrunAtOffset(int offset, final int length) {
      final Runnable restart = new Runnable() {
        public void run() {
          invokeInfoListener(MediaPlayer.MEDIA_INFO_BUFFERING_END, 0);
          doStart();
        }
      };
      Runnable callback = new Runnable() {
        public void run() {
          doStop();
          invokeInfoListener(MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
          handler.postDelayed(restart, length);
        }
      };
      scheduleEventAtOffset(offset, callback);
      return callback;
    }

    /**
     * Removes the specified event from the playback schedule at the given
     * playback offset.
     * 
     * @param offset
     *          the offset at which the event was scheduled.
     * @param event
     *          the event to remove.
     * @see #removeEvent(Runnable)
     */
    public void removeEventAtOffset(int offset, Runnable event) {
      ArrayList<Runnable> runList = events.get(offset);
      if (runList != null) {
        final boolean reallyPlaying = isReallyPlaying();
        if (reallyPlaying) {
          // Need to stop while modifying the collections
          // otherwise we can cause a ConcurrentModificationException
          doStop();
        }
        runList.remove(event);
        if (runList.isEmpty()) {
          events.remove(offset);
        }
        if (reallyPlaying) {
          // If we were playing make sure we restart again to reschedule
          // the new events.
          doStart();
        }
      }
    }

    /**
     * Removes the specified event from the playback schedule at all playback
     * offsets where it has been scheduled.
     * 
     * @param event
     *          the event to remove.
     * @see #removeEventAtOffset(int,Runnable)
     */
    public void removeEvent(Runnable event) {
      final boolean reallyPlaying = isReallyPlaying();
      if (reallyPlaying) {
        // Need to stop while modifying the collections
        // otherwise we can cause a ConcurrentModificationException
        doStop();
      }
      for (Iterator<Entry<Integer, RunList>> iter = events.entrySet()
          .iterator(); iter.hasNext();) {
        Entry<Integer, RunList> entry = iter.next();
        RunList runList = entry.getValue();
        runList.remove(event);
        if (runList.isEmpty()) {
          iter.remove();
        }
      }
      // If we were playing make sure we restart again to reschedule
      // the new events.
      if (reallyPlaying) {
        doStart();
      }
    }
  }

  /**
   * Callback interface for clients that wish to be informed when a new
   * {@link MediaPlayer} instance is constructed.
   * 
   * @see #setCreateListener
   */
  public static interface CreateListener {
    /**
     * Method that is invoked when a new {@link MediaPlayer} is created. This
     * method is invoked at the end of the constructor, after all of the default
     * setup has been completed.
     * 
     * @param player
     *          reference to the newly-created media player object.
     * @param shadow
     *          reference to the corresponding shadow object for the
     *          newly-created media player (provided for convenience).
     */
    public void onCreate(MediaPlayer player, ShadowMediaPlayer shadow);
  }

  /** Current state of the media player. */
  private State state = IDLE;

  /** Delay for calls to {@link #seekTo} (in ms). */
  private int seekDelay = 0;

  private int auxEffect;
  private int audioSessionId;
  private int audioStreamType;
  private boolean looping;
  private int pendingSeek = -1;
  /** Various source variables from setDataSource() */
  private Uri sourceUri;
  private int sourceResId;
  private DataSource dataSource;

  /** The time (in ms) at which playback was last started/resumed. */
  private long startTime = -1;

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
  private float leftVolume;
  private float rightVolume;
  private MediaPlayer.OnCompletionListener completionListener;
  private MediaPlayer.OnSeekCompleteListener seekCompleteListener;
  private MediaPlayer.OnPreparedListener preparedListener;
  private MediaPlayer.OnInfoListener infoListener;
  private MediaPlayer.OnErrorListener errorListener;

  /**
   * Flag indicating how the shadow media player should behave when a method is
   * invoked in an invalid state.
   */
  private InvalidStateBehavior invalidStateBehavior = InvalidStateBehavior.SILENT;
  private Handler handler;

  private final Runnable completionCallback = new Runnable() {
    public void run() {
      if (looping) {
        startOffset = 0;
        doStart();
      } else {
        doStop();
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
   * Callback to use when a method is invoked from an invalid state. Has
   * <code>what = -38</code> and <code>extra = 0</code>, which are values that
   * were determined by inspection.
   */
  private final ErrorCallback invalidStateErrorCallback = new ErrorCallback(
      -38, 0);

  /** Callback to use for scheduled errors. */
  private class ErrorCallback implements Runnable {
    private int what;
    private int extra;

    public ErrorCallback(int what, int extra) {
      this.what = what;
      this.extra = extra;
    }

    public void run() {
      invokeErrorListener(what, extra);
    }
  }

  /**
   * Map of strings to {@link #MediaInfo} instances. Used by
   * {@link #setDataSource(String)} to set the {@link #currentMediaInfo} field
   * based on the current data source.
   */
  private Map<String, MediaInfo> dataSourceMap;

  /**
   * The MediaInfo object describing the default playback schedule currently
   * selected. If there is no overriding {@link #MediaInfo} instance invoked by
   * {@link #setDataSource(String)}, then this instance is used.
   */
  private MediaInfo defaultMediaInfo = new MediaInfo(1000, 0);

  /** The MediaInfo object describing the playback schedule currently selected. */
  private MediaInfo mediaInfo = defaultMediaInfo;

  @Implementation
  public static MediaPlayer create(Context context, int resId) {
    MediaPlayer mp = new MediaPlayer();
    ShadowMediaPlayer shadow = shadowOf(mp);
    shadow.sourceResId = resId;
    try {
      shadow.setState(INITIALIZED);
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
    // worth the effort - a random non-zero number will probably do.
    Random random = new Random();
    audioSessionId = random.nextInt(Integer.MAX_VALUE) + 1;
    handler = new Handler();
    // This gives test suites a chance to customize the MP instance
    // and its shadow when it is created, without having to modify
    // the code under test in order to do so.
    if (createListener != null) {
      createListener.onCreate(player, this);
    }
    // Ensure that the real object is set up properly.
    Shadow.invokeConstructor(MediaPlayer.class, player);
  }

  public void setDataSource(DataSource dataSource) throws IOException {
    Exception e = exceptions.get(dataSource);
    if (e != null) {
      e.fillInStackTrace();
      if (e instanceof IOException) {
        throw (IOException)e;
      } else if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new AssertionError("Invalid exception type for setDataSource: <" + e + '>');
    }
    checkStateException("setDataSource()", idleState);
    this.dataSource = dataSource;
    state = INITIALIZED;
  }
  
  @Implementation
  public void setDataSource(String path) throws IOException {
    setDataSource(toDataSource(path));
  }

  @Implementation
  public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException {
    setDataSource(toDataSource(context, uri, headers));
    sourceUri = uri;
  }

  @Implementation
  public void setDataSource(String uri, Map<String, String> headers) throws IOException {
    setDataSource(toDataSource(uri, headers));
  }

  @Implementation
  public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException {
    setDataSource(toDataSource(fd, offset, length));
  }

//  @Implementation
//  public void setDataSource(String path) throws IOException {
//    MediaInfo info = dataSourceMap == null ? null : dataSourceMap.get(path);
//    setCurrentMediaInfo(info);
//    doSetDataSourceExceptions();
//    this.sourcePath = path;
//  }
//
//  @Implementation
//  public void setDataSource(Context context, Uri uri,
//      Map<String, String> headers) throws IOException {
//    doSetDataSourceExceptions();
//    this.sourceUri = uri;
//    this.sourceHeaders = headers;
//  }
//
//  @Implementation
//  public void setDataSource(FileDescriptor fd, long offset, long length)
//      throws IOException {
//    doSetDataSourceExceptions();
//    this.sourceFD = fd;
//    this.sourceOffset = offset;
//    this.sourceLength = length;
//  }
//
  private void doSetDataSourceExceptions() throws IOException {
    Exception setDataSourceException = mediaInfo.getSetDataSourceException();
    // By inspection I determined that the state check is one
    // of the last things to happen (once you get into native
    // code land) - there is plenty of code in Java land for
    // another type of exception to be thrown before then.
    if (setDataSourceException != null) {
      if (setDataSourceException instanceof IOException) {
        throw (IOException) setDataSourceException;
      }
      if (setDataSourceException instanceof RuntimeException) {
        throw (RuntimeException) setDataSourceException;
      }
      throw new AssertionError("Invalid exception type specified: "
          + setDataSourceException);
    }
  }

  public static void addException(DataSource dataSource, RuntimeException e) {
    exceptions.put(dataSource, e);
  }
  
  public static void addException(DataSource dataSource, IOException e) {
    exceptions.put(dataSource, e);
  }
  
  /**
   * Checks states for methods that only log when there is an error. Such
   * methods throw an {@link IllegalArgumentException} when invoked in the END
   * state, but log an error in other disallowed states. This method will either
   * emulate this behavior or else will generate an assertion if invoked from a
   * disallowed state if {@link #setAssertOnError assertOnError} is set.
   * 
   * @param method
   *          the name of the method being tested.
   * @param allowedStates
   *          the states that this method is allowed to be called from.
   * @see #setAssertOnError
   * @see #checkStateError(String, EnumSet)
   * @see #checkStateException(String, EnumSet)
   */
  private void checkStateLog(String method, EnumSet<State> allowedStates) {
    switch (invalidStateBehavior) {
    case SILENT:
      break;
    case EMULATE:
      if (state == END) {
        String msg = "Can't call " + method + " from state " + state;
        throw new IllegalStateException(msg);
      }
      break;
    case ASSERT:
      if (!allowedStates.contains(state) || state == END) {
        String msg = "Can't call " + method + " from state " + state;
        throw new AssertionError(msg);
      }
    }
  }

  /**
   * Checks states for methods that asynchronously invoke
   * {@link android.media.MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)
   * onError()} when invoked in an illegal state. Such methods always throw
   * {@link IllegalStateException} rather than invoke <code>onError()</code> if
   * they are invoked from the END state.
   * 
   * This method will either emulate this behavior by posting an
   * <code>onError()</code> callback to the current thread's message queue (or
   * throw an {@link IllegalStateException} if invoked from the END state), or
   * else it will generate an assertion if {@link #setAssertOnError
   * assertOnError} is set.
   * 
   * @param method
   *          the name of the method being tested.
   * @param allowedStates
   *          the states that this method is allowed to be called from.
   * @see #getHandler
   * @see #setAssertOnError
   * @see #checkStateLog(String, EnumSet)
   * @see #checkStateException(String, EnumSet)
   */
  private boolean checkStateError(String method, EnumSet<State> allowedStates) {
    if (!allowedStates.contains(state)) {
      switch (invalidStateBehavior) {
      case SILENT:
        break;
      case EMULATE:
        if (state == END) {
          String msg = "Can't call " + method + " from state " + state;
          throw new IllegalStateException(msg);
        }
        state = ERROR;
        handler.post(invalidStateErrorCallback);
        return false;
      case ASSERT:
        String msg = "Can't call " + method + " from state " + state;
        throw new AssertionError(msg);
      }
    }
    return true;
  }

  /**
   * Checks states for methods that synchronously throw an exception when
   * invoked in an illegal state. This method will likewise throw an
   * {@link IllegalArgumentException} if it determines that the method has been
   * invoked from a disallowed state, or else it will generate an assertion if
   * {@link #setAssertOnError assertOnError} is set.
   * 
   * @param method
   *          the name of the method being tested.
   * @param allowedStates
   *          the states that this method is allowed to be called from.
   * @see #setAssertOnError
   * @see #checkStateLog(String, EnumSet)
   * @see #checkStateError(String, EnumSet)
   */
  private void checkStateException(String method, EnumSet<State> allowedStates) {
    if (!allowedStates.contains(state)) {
      String msg = "Can't call " + method + " from state " + state;
      switch (invalidStateBehavior) {
      case SILENT:
        break;
      case EMULATE:
        throw new IllegalStateException(msg);
      case ASSERT:
        throw new AssertionError(msg);
      }
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
  public void setOnInfoListener(MediaPlayer.OnInfoListener listener) {
    infoListener = listener;
  }

  @Implementation
  public void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
    errorListener = listener;
  }

  @Implementation
  public boolean isLooping() {
    checkStateException("isLooping()", nonEndStates);
    return looping;
  }

  static private final EnumSet<State> nonEndStates = EnumSet
      .complementOf(EnumSet.of(END));
  static private final EnumSet<State> nonErrorStates = EnumSet
      .complementOf(EnumSet.of(ERROR, END));

  @Implementation
  public void setLooping(boolean looping) {
    checkStateError("setLooping()", nonErrorStates);
    this.looping = looping;
  }

  @Implementation
  public void setVolume(float left, float right) {
    checkStateError("setVolume()", nonErrorStates);
    leftVolume = left;
    rightVolume = right;
  }

  @Implementation
  public boolean isPlaying() {
    checkStateError("isPlaying()", nonErrorStates);
    return state == STARTED;
  }

  private static EnumSet<State> preparableStates = EnumSet.of(INITIALIZED,
      STOPPED);

  /**
   * Simulates {@link MediaPlayer#prepareAsync()}. Sleeps for
   * {@link MediaInfo#getPreparationDelay() preparationDelay} ms by calling
   * {@link SystemClock#sleep(long)} before calling
   * {@link #invokePreparedListener()}.
   * 
   * If <code>preparationDelay</code> is not positive and non-zero, there is no
   * sleep.
   * 
   * @see MediaInfo#setPreparationDelay(int)
   * @see #invokePreparedListener()
   */
  @Implementation
  public void prepare() {
    checkStateException("prepare()", preparableStates);
    if (mediaInfo.preparationDelay > 0) {
      SystemClock.sleep(mediaInfo.preparationDelay);
    }
    invokePreparedListener();
  }

  /**
   * Simulates {@link MediaPlayer#prepareAsync()}. Sets state to PREPARING and
   * posts a callback to {@link #invokePreparedListener()} if the current
   * preparation delay for the current media (see {@link #getMediaInfo()}) is >=
   * 0, otherwise the test suite is responsible for calling
   * {@link #invokePreparedListener()} directly if required.
   * 
   * @see MediaInfo#setPreparationDelay(int)
   * @see #invokePreparedListener()
   */
  @Implementation
  public void prepareAsync() {
    checkStateException("prepareAsync()", preparableStates);
    state = PREPARING;
    if (mediaInfo.preparationDelay >= 0) {
      handler.postDelayed(preparedCallback, mediaInfo.preparationDelay);
    }
  }

  private static EnumSet<State> startableStates = EnumSet.of(PREPARED, STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  /**
   * Simulates private native method {@link MediaPlayer#_start()}. Sets state to STARTED and calls
   * {@link #doStart()} to start scheduling playback callback events.
   * 
   * If the current state is PLAYBACK_COMPLETED, the current position is reset
   * to zero before starting playback.
   * 
   * @see #doStart()
   */
  @Implementation
  public void _start() {
    if (checkStateError("start()", startableStates)) {
      if (state == PLAYBACK_COMPLETED) {
        startOffset = 0;
      }
      state = STARTED;
      doStart();
    }
  }

  private void scheduleNextPlaybackEvent() {
    if (!isReallyPlaying()) {
      return;
    }
    final int currentPosition = getCurrentPositionRaw();
    Entry<Integer, RunList> event = mediaInfo.events
        .higherEntry(currentPosition);
    if (event == null) {
      // This means we've "seeked" past the end. Get the last
      // event (which should be the completion event) and
      // invoke that, setting the position to the duration.
      handler.post(completionCallback);
    } else {
      final int runListOffset = event.getKey();
      nextPlaybackEvent = event.getValue();
      handler.postDelayed(nextPlaybackEvent, runListOffset - currentPosition);
    }
  }

  /**
   * Tests to see if the player is really playing.
   * 
   * The player is defined as "really playing" if simulated playback events
   * (including playback completion) are being scheduled & invoked and
   * {@link #getCurrentPosition currentPosition} is being updated as time
   * passes. Note that while the player will normally be really playing if in
   * the STARTED state, this is not always the case - for example, if a pending
   * seek is in progress, or perhaps a buffer underrun is being simulated.
   * 
   * @return <code>true</code> if the player is really playing or
   *         <code>false</code> if the player is internally paused.
   * @see #doStart
   * @see #doStop
   */
  public boolean isReallyPlaying() {
    return startTime >= 0;
  }

  /**
   * Starts simulated playback. Until this method is called, the player is not
   * "really playing" (see {@link #isReallyPlaying} for a definition of
   * "really playing").
   * 
   * This method is used internally by the various shadow method implementations
   * of the MediaPlayer public API, but may also be called directly by the test
   * suite if you wish to simulate an internal pause.
   * 
   * @see #isReallyPlaying()
   * @see #doStop()
   */
  public void doStart() {
    startTime = SystemClock.uptimeMillis();
    scheduleNextPlaybackEvent();
  }

  /**
   * Pauses simulated playback. After this method is called, the player is no
   * longer "really playing" (see {@link #isReallyPlaying} for a definition of
   * "really playing").
   * 
   * This method is used internally by the various shadow method implementations
   * of the MediaPlayer public API, but may also be called directly by the test
   * suite if you wish to simulate an internal pause.
   * 
   * @see #isReallyPlaying()
   * @see #doStart()
   */
  public void doStop() {
    startOffset = getCurrentPositionRaw();
    if (nextPlaybackEvent != null) {
      handler.removeCallbacks(nextPlaybackEvent);
      nextPlaybackEvent = null;
    }
    startTime = -1;
  }

  private static final EnumSet<State> pausableStates = EnumSet.of(STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  /**
   * Simulates {@link MediaPlayer#_pause()}. Invokes {@link #doStop()} to suspend
   * playback event callbacks and sets the state to PAUSED.
   * 
   * @see #doStop()
   */
  @Implementation
  public void _pause() {
    if (checkStateError("pause()", pausableStates)) {
      doStop();
      state = PAUSED;
    }
  }

  static final EnumSet<State> allStates = EnumSet.allOf(State.class);

  /**
   * Simulates call to {@link MediaPlayer#_release()}. Calls {@link #doStop()} to
   * suspend playback event callbacks and sets the state to END.
   */
  @Implementation
  public void _release() {
    checkStateException("release()", allStates);
    doStop();
    state = END;
    // FIXME: This doesn't actually do the job due to a bug
    // in the ShadowHandler implementation - the callbacks are
    // removed from the Handler but not from the Scheduler, so
    // they run anyway. Need to fix - otherwise (eg) a pending
    // seek callback can be invoked some time after reset()
    // has completed, which is not realistic.
    handler.removeCallbacksAndMessages(null);
  }

  /**
   * Simulates call to {@link MediaPlayer#_reset()}. Calls {@link #doStop()} to
   * suspend playback event callbacks and sets the state to IDLE.
   */
  @Implementation
  public void _reset() {
    checkStateException("reset()", nonEndStates);
    doStop();
    state = IDLE;
    // FIXME: This doesn't actually do the job due to a bug
    // in the ShadowHandler implementation - the callbacks are
    // removed from the Handler but not from the Scheduler, so
    // they run anyway. Need to fix - otherwise (eg) a pending
    // seek callback can be invoked some time after reset()
    // has completed, which is not realistic.
    handler.removeCallbacksAndMessages(null);
    startOffset = 0;
  }

  static private final EnumSet<State> stoppableStates = EnumSet.of(PREPARED,
      STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED);

  /**
   * Simulates call to {@link MediaPlayer#release()}. Calls {@link #doStop()} to
   * suspend playback event callbacks and sets the state to STOPPED.
   */
  @Implementation
  public void _stop() {
    if (checkStateError("stop()", stoppableStates)) {
      doStop();
      state = STOPPED;
    }
  }

  private static final EnumSet<State> attachableStates = EnumSet.of(
      INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED,
      PLAYBACK_COMPLETED);

  @Implementation
  public void attachAuxEffect(int effectId) {
    checkStateError("attachAuxEffect()", attachableStates);
    auxEffect = effectId;
  }

  @Implementation
  public int getAudioSessionId() {
    checkStateException("getAudioSessionId()", allStates);
    return audioSessionId;
  }

  /**
   * Simulates call to {@link MediaPlayer#getCurrentPosition()}. Simply does the
   * state validity checks and then invokes {@link #getCurrentPositionRaw()} to
   * calculate the simulated playback position.
   * 
   * @return The current offset (in ms) of the simulated playback.
   * @see #getCurrentPositionRaw()
   */
  @Implementation
  public int getCurrentPosition() {
    checkStateError("getCurrentPosition()", attachableStates);
    return getCurrentPositionRaw();
  }

  /**
   * Simulates call to {@link MediaPlayer#getDuration()}. Retrieves the duration
   * as defined by the current {@link MediaInfo} instance.
   * 
   * @return The duration (in ms) of the current simulated playback.
   * @see #getCurrentMediaInfo()
   */
  @Implementation
  public int getDuration() {
    checkStateError("getDuration()", stoppableStates);
    return mediaInfo.duration;
  }

  @Implementation
  public int getVideoHeight() {
    checkStateLog("getVideoHeight()", attachableStates);
    return videoHeight;
  }

  @Implementation
  public int getVideoWidth() {
    checkStateLog("getVideoWidth()", attachableStates);
    return videoWidth;
  }

  private static final EnumSet<State> seekableStates = EnumSet.of(PREPARED,
      STARTED, PAUSED, PLAYBACK_COMPLETED);

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
    boolean success = checkStateError("seekTo()", seekableStates);
    // Cancel any pending seek operations.
    handler.removeCallbacks(seekCompleteCallback);

    if (success) {
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
  }

  static private final EnumSet<State> idleState = EnumSet.of(IDLE);

  @Implementation
  public void setAudioSessionId(int sessionId) {
    checkStateError("setAudioSessionId()", idleState);
    audioSessionId = sessionId;
  }

  static private final EnumSet<State> nonPlayingStates = EnumSet.of(IDLE,
      INITIALIZED, STOPPED);

  @Implementation
  public void setAudioStreamType(int audioStreamType) {
    checkStateError("setAudioStreamType()", nonPlayingStates);
    this.audioStreamType = audioStreamType;
  }

  /**
   * Sets a listener that is invoked whenever a new shadowed {@link MediaPlayer}
   * object is constructed. Registering a listener gives you a chance to
   * customize the shadowed object appropriately (eg, invoking
   * {@link #setDefaultMediaInfo setDefaultMediaInfo()} or
   * {@link #setDataSourceMap setDataSourceMap()} without needing to modify the
   * application-under-test to provide access to the instance at the appropriate
   * point in its life cycle. This is useful because normally a new
   * {@link MediaPlayer} is created and {@link #setDataSource setDataSource()}
   * is invoked soon after, without a break in the code. Using this callback
   * means you don't have to change this common pattern just so that you can
   * customize the shadow for testing.
   * 
   * @param createListener
   *          the listener to be invoked
   */
  public static void setCreateListener(CreateListener createListener) {
    ShadowMediaPlayer.createListener = createListener;
  }

  /**
   * Retrieves the {@link Handler} object used by this
   * <code>ShadowMediaPlayer</code>. Can be used for posting custom asynchronous
   * events to the thread (eg, asynchronous errors). Use this for scheduling
   * events to take place at a particular "real" time (ie, time as measured by
   * the scheduler). For scheduling events to occur at a particular playback
   * offset (no matter how long playback may be paused for, or where you seek
   * to, etc), see {@link MediaInfo#scheduleEventAtOffset(int, Runnable)} and
   * its various helpers.
   * 
   * @return Handler object that can be used to schedule asynchronous events on
   *         this media player.
   */
  public Handler getHandler() {
    return handler;
  }

  /**
   * Retrieves current flag specifying the behavior of the media player when a
   * method is invoked in an invalid state. See
   * {@link #setInvalidStateBehavior(InvalidStateBehavior)} for a discussion of
   * the available modes and their associated behaviors.
   * 
   * @return The current invalid state behavior mode.
   * @see #setInvalidStateBehavior
   */
  public InvalidStateBehavior getInvalidStateBehavior() {
    return invalidStateBehavior;
  }

  /**
   * Specifies how the media player should behave when a method is invoked in an
   * invalid state. Three modes are supported (as defined by the
   * {@link InvalidStateBehavior} enum):
   * 
   * <ul>
   * <li>SILENT: no invalid state checking is done at all. All methods can be
   * invoked from any state without throwing any exceptions or invoking the
   * error listener.
   * 
   * This mode is provided primarily for backwards compatibility, and for this
   * reason it is the default. For proper testing one of the other two modes is
   * probably preferable.</li>
   * <li>EMULATE: the shadow will attempt to emulate the behavior of the actual
   * {@link MediaPlayer} implementation. This is based on a reading of the
   * documentation and on actual experiments done on a Jelly Bean device. The
   * official documentation is not all that clear, but basically methods fall
   * into three categories:
   * <ul>
   * <li>Those that log an error when invoked in an invalid state but don't
   * throw an exception or invoke <code>onError()</code>. An example is
   * {@link #getVideoHeight()}.</li>
   * <li>Synchronous error handling: methods always throw an exception (usually
   * {@link IllegalStateException} but don't invoke <code>onError()</code>.
   * Examples are {@link #prepare()} and {@link #setDataSource(String)}.</li>
   * <li>Asynchronous error handling: methods don't throw an exception but
   * invoke <code>onError()</code>.</li>
   * </ul>
   * Additionally, all three methods behave synchronously (throwing
   * {@link IllegalStateException} when invoked from the END state.
   * 
   * To complicate matters slightly, the official documentation sometimes
   * contradicts observed behavior. For example, the documentation says it is
   * illegal to call {@link #setDataSource} from the ERROR state - however, in
   * practice it works fine. Conversely, the documentation says that it is legal
   * to invoke {@link #getCurrentPosition()} from the INITIALIZED state, however
   * testing showed that this caused an error. Wherever there is a discrepancy
   * between documented and observed behavior, this implementation has gone with
   * the most conservative implementation (ie, it is illegal to invoke
   * {@link #setDataSource} from the ERROR state and likewise illegal to invoke
   * {@link #getCurrentPosition()} from the INITIALIZED state.
   * <li>ASSERT: the shadow will raise an assertion any time that a method is
   * invoked in an invalid state. The philosophy behind this mode is that to
   * invoke a method in an invalid state is a programming error - a bug, pure
   * and simple. As such it should be discovered & eliminated at development &
   * testing time, rather than anticipated and handled at runtime. Asserting is
   * a way of testing for these bugs during testing.</li>
   * </ul>
   * 
   * @param invalidStateBehavior
   *          the behavior mode for this shadow to use during testing.
   * @see #getInvalidStateBehavior()
   */
  public void setInvalidStateBehavior(InvalidStateBehavior invalidStateBehavior) {
    this.invalidStateBehavior = invalidStateBehavior;
  }

  /**
   * Retrieves the default {@link MediaInfo}. This is the {@link MediaInfo}
   * instance that will be used if it is not overridden by something in the
   * {@link #setDataSourceMap dataSourceMap}.
   * 
   * @return The default {@link MediaInfo} instance.
   * @see #setDefaultMediaInfo(MediaInfo)
   */
  public MediaInfo getDefaultMediaInfo() {
    return defaultMediaInfo;
  }

  /**
   * Sets the default {@link MediaInfo} instance. See
   * {@link #getDefaultMediaInfo()} for details.
   * 
   * @param defaultMediaInfo
   *          the new default {@link MediaInfo} instance.
   * @throws NullPointerException
   *           if <code>defaultMediaInfo</code> is <code>null</code>.
   * @see #getDefaultMediaInfo()
   */
  public void setDefaultMediaInfo(MediaInfo defaultMediaInfo) {
    if (defaultMediaInfo == null) {
      throw new NullPointerException();
    }
    this.defaultMediaInfo = defaultMediaInfo;
  }

  /**
   * Retrieves the currently selected {@link MediaInfo}. This instance is used
   * to define current duration, preparation delay, exceptions for
   * <code>setDataSource()</code>, playback events, etc.
   * 
   * @return The currently selected {@link MediaInfo}.
   * @see #setCurrentMediaInfo(MediaInfo)
   */
  public MediaInfo getCurrentMediaInfo() {
    return mediaInfo;
  }

  /**
   * Sets the current media info and configures playback simulation
   * appropriately. Non-Android setter. Calling this during a playback
   * simulation will have unpredictable results.
   * 
   * @param mediaInfo
   *          the new {@link MediaInfo} instance to describe current playback
   *          behavior. If <code>null</code>, will be set to
   *          {@link #getDefaultMediaInfo defaultMediaInfo}.
   * @see #getCurrentMediaInfo()
   */
  public void setCurrentMediaInfo(MediaInfo mediaInfo) {
    if (mediaInfo == null) {
      this.mediaInfo = defaultMediaInfo;
    } else {
      this.mediaInfo = mediaInfo;
    }
  }

  /**
   * Constructs a new {@link MediaInfo} object with the specified duration and
   * preparation delay.
   * 
   * @param duration
   *          the duration (in ms) for the MediaInfo object.
   * @param preparationDelay
   *          the preparation delay (in ms) for the MediaInfo object.
   * @return The newly created MediaInfo instance with the specified initial
   *         values.
   */
  public MediaInfo buildMediaInfo(int duration, int preparationDelay) {
    return new MediaInfo(duration, preparationDelay);
  }

  /**
   * Sets the current position, bypassing the normal state checking. Use with
   * care. Non-Android setter.
   * 
   * @param position
   *          the new playback position.
   */
  public void setCurrentPosition(int position) {
    startOffset = position;
  }

  /**
   * Non-Android setter. Sets a map from String datasource to a MediaInfo
   * instance containing info to use for that data source (eg, duration,
   * preparation delay, etc).
   */
  public void setDataSourceMap(Map<String, MediaInfo> map) {
    this.dataSourceMap = map;
  }

  /**
   * Retrieves the current position without doing the state checking that the
   * emulated version of {@link #getCurrentPosition()} does. Non-Android
   * accessor.
   * 
   * @return The current playback position within the current clip.
   */
  public int getCurrentPositionRaw() {
    int currentPos = startOffset;
    if (isReallyPlaying()) {
      currentPos += (int) (SystemClock.uptimeMillis() - startTime);
    }
    return currentPos;
  }

  /**
   * Retrieves the current duration without doing the state checking that the
   * emulated version does. Non-Android accessor.
   * 
   * @return The duration of the current clip loaded by the player.
   */
  public int getDurationRaw() {
    return mediaInfo.duration;
  }

  /**
   * Retrieves the current state of the {@link MediaPlayer}. Uses the states as
   * defined in the {@link MediaPlayer} documentation. Non-Android accessor.
   * Used for assertions.
   * 
   * @return The current state of the {@link MediaPlayer}, as defined in the
   *         MediaPlayer documentation.
   * @see #setState
   * @see MediaPlayer
   */
  public State getState() {
    return state;
  }

  /**
   * Forces the @link MediaPlayer} into the specified state. Uses the states as
   * defined in the {@link MediaPlayer} documentation.
   * 
   * Note that by invoking this method directly you can get the player into an
   * inconsistent state that a real player could not be put in (eg, in the END
   * state but with playback events still happening). Use with care.
   * 
   * @param state
   *          the new state of the {@link MediaPlayer}, as defined in the
   *          MediaPlayer documentation.
   * @see #getState
   * @see MediaPlayer
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * Non-Android accessor.
   * 
   * @return audioStreamType
   */
  public int getAudioStreamType() {
    return audioStreamType;
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
   * Retrieves the pending seek setting.
   * 
   * @return The position to which the shadow player is seeking for the seek in
   *         progress (ie, after the call to {@link #seekTo} but before a call
   *         to {@link #invokeSeekCompleteListener()}). Returns <code>-1</code>
   *         if no seek is in progress.
   */
  public int getPendingSeek() {
    return pendingSeek;
  }

  /**
   * Retrieves the data source (if any) that was passed in to
   * {@link MediaPlayer#setDataSource(DataSource)}.
   * 
   * Non-Android accessor. Use for assertions.
   * 
   * @return The source passed in to <code>setDataSource</code>.
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Retrieves the source path (if any) that was passed in to
   * {@link MediaPlayer#setDataSource(Context, Uri, Map)} or
   * {@link MediaPlayer#setDataSource(Context, Uri)}.
   * 
   * Non-Android accessor. Use for assertions.
   * 
   * @return The source Uri passed in to <code>setDataSource</code>.
   */
  public Uri getSourceUri() {
    return sourceUri;
  }

  /**
   * Retrieves the resource ID used in the call to {@link #create(Context, int)}
   * (if any).
   * 
   * Non-Android accessor. Use for assertions.
   * 
   * @return The resource ID passed in to <code>create()</code>, or
   *         <code>-1</code> if a different method of setting the source was
   *         used.
   */
  public int getSourceResId() {
    return sourceResId;
  }

  /**
   * Retrieves the current setting for the left channel volume.
   * 
   * Non-Android accessor. Use for assertions.
   * 
   * @return The left channel volume.
   */
  public float getLeftVolume() {
    return leftVolume;
  }

  /**
   * Non-Android accessor. Use for assertions.
   * 
   * @return The right channel volume.
   */
  public float getRightVolume() {
    return rightVolume;
  }

  private static EnumSet<State> preparedStates = EnumSet.of(PREPARED, STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  /**
   * Tests to see if the player is in the PREPARED state. Non-Android accessor.
   * Use for assertions. This is mainly used for backward compatibility.
   * {@link #getState} may be more useful for new testing applications.
   * 
   * @return <code>true</code> if the MediaPlayer is in the PREPARED state,
   *         false otherwise.
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
   * Allows test cases to simulate 'prepared' state by invoking callback. Sets
   * the player's state to PREPARED and invokes the
   * {@link MediaPlayer.OnPreparedListener#onPrepare preparedListener()
   */
  public void invokePreparedListener() {
    state = PREPARED;
    if (preparedListener == null)
      return;
    preparedListener.onPrepared(player);
  }

  /**
   * Simulates end-of-playback. Changes the player into PLAYBACK_COMPLETED state
   * and calls
   * {@link MediaPlayer.OnCompletionListener#onCompletion(MediaPlayer)
   * onCompletion()} if a listener has been set.
   */
  public void invokeCompletionListener() {
    state = PLAYBACK_COMPLETED;
    if (completionListener == null)
      return;
    completionListener.onCompletion(player);
  }

  /**
   * Allows test cases to simulate seek completion by invoking callback.
   */
  public void invokeSeekCompleteListener() {
    setCurrentPosition(pendingSeek > mediaInfo.duration ? mediaInfo.duration
        : pendingSeek < 0 ? 0 : pendingSeek);
    pendingSeek = -1;
    if (state == STARTED) {
      doStart();
    }
    if (seekCompleteListener == null) {
      return;
    }
    seekCompleteListener.onSeekComplete(player);
  }

  /**
   * Allows test cases to directly simulate invocation of the OnInfo event.
   * 
   * @param what
   *          parameter to pass in to <code>what</code> in
   *          {@link OnInfoListener#onInfo onInfo()}.
   * @param extra
   *          parameter to pass in to <code>extra</code> in
   *          {@link OnInfoListener#onInfo onInfo()}.
   */
  public void invokeInfoListener(int what, int extra) {
    if (infoListener != null) {
      infoListener.onInfo(player, what, extra);
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
    // Calling doStop() un-schedules the next event and
    // stops normal event flow from continuing.
    doStop();
    state = ERROR;
    boolean handled = errorListener != null
        && errorListener.onError(player, what, extra);
    if (!handled) {
      // The documentation isn't very clear if onCompletion is
      // supposed to be called from non-playing states
      // (ie, states other than STARTED or PAUSED). Testing
      // revealed that onCompletion is invoked even if playback
      // hasn't started or is not in progress.
      invokeCompletionListener();
      // Need to set this again because
      // invokeCompletionListener() will set the state
      // to PLAYBACK_COMPLETED
      state = ERROR;
    }
  }

  @Resetter
  public static void resetStaticState() {
    createListener = null;
    exceptions.clear();
  }
}
