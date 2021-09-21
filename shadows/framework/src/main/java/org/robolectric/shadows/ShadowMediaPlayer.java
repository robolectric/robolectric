package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.shadows.ShadowMediaPlayer.State.END;
import static org.robolectric.shadows.ShadowMediaPlayer.State.ERROR;
import static org.robolectric.shadows.ShadowMediaPlayer.State.IDLE;
import static org.robolectric.shadows.ShadowMediaPlayer.State.INITIALIZED;
import static org.robolectric.shadows.ShadowMediaPlayer.State.PAUSED;
import static org.robolectric.shadows.ShadowMediaPlayer.State.PLAYBACK_COMPLETED;
import static org.robolectric.shadows.ShadowMediaPlayer.State.PREPARED;
import static org.robolectric.shadows.ShadowMediaPlayer.State.PREPARING;
import static org.robolectric.shadows.ShadowMediaPlayer.State.STARTED;
import static org.robolectric.shadows.ShadowMediaPlayer.State.STOPPED;
import static org.robolectric.shadows.util.DataSource.toDataSource;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import java.io.FileDescriptor;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.util.DataSource;

/**
 * Automated testing of media playback can be a difficult thing - especially testing that your code
 * properly handles asynchronous errors and events. This near impossible task is made quite
 * straightforward using this implementation of {@link MediaPlayer} with Robolectric.
 *
 * <p>This shadow implementation provides much of the functionality needed to emulate {@link
 * MediaPlayer} initialization and playback behavior without having to play actual media files. A
 * summary of the features included are:
 *
 * <ul>
 *   <li>Construction-time callback hook {@link CreateListener} so that newly-created {@link
 *       MediaPlayer} instances can have their shadows configured before they are used.
 *   <li>Emulation of the {@link android.media.MediaPlayer.OnCompletionListener
 *       OnCompletionListener}, {@link android.media.MediaPlayer.OnErrorListener OnErrorListener},
 *       {@link android.media.MediaPlayer.OnInfoListener OnInfoListener}, {@link
 *       android.media.MediaPlayer.OnPreparedListener OnPreparedListener} and {@link
 *       android.media.MediaPlayer.OnSeekCompleteListener OnSeekCompleteListener}.
 *   <li>Full support of the {@link MediaPlayer} internal states and their transition map.
 *   <li>Configure time parameters such as playback duration, preparation delay and {@link
 *       #setSeekDelay(int)}.
 *   <li>Emulation of asynchronous callback events during playback through Robolectric's scheduling
 *       system using the {@link MediaInfo} inner class.
 *   <li>Emulation of error behavior when methods are called from invalid states, or to throw
 *       assertions when methods are invoked in invalid states (using {@link
 *       #setInvalidStateBehavior}).
 *   <li>Emulation of different playback behaviors based on the current data source, as passed in to
 *       {@link #setDataSource(String) setDataSource()}, using {@link #addMediaInfo} or {@link
 *       #setMediaInfoProvider(MediaInfoProvider)}.
 *   <li>Emulation of exceptions when calling {@link #setDataSource} using {@link #addException}.
 * </ul>
 *
 * <b>Note</b>: One gotcha with this shadow is that you need to either configure an exception using
 * {@link #addException(DataSource, IOException)} or a {@link ShadowMediaPlayer.MediaInfo} instance
 * for that data source using {@link #addMediaInfo(DataSource, MediaInfo)} or {@link
 * #setMediaInfoProvider(MediaInfoProvider)} <i>before</i> calling {@link #setDataSource}, otherwise
 * you'll get an {@link IllegalArgumentException}.
 *
 * <p>The current features of {@code ShadowMediaPlayer} were focused on development for testing
 * playback of audio tracks. Thus support for emulating timed text and video events is incomplete.
 * None of these features would be particularly onerous to add/fix - contributions welcome, of
 * course!
 *
 * @author Fr Jeremy Krieg, Holy Monastery of St Nectarios, Adelaide, Australia
 */
@Implements(MediaPlayer.class)
public class ShadowMediaPlayer extends ShadowPlayerBase {
  @Implementation
  protected static void __staticInitializer__() {
    // don't bind the JNI library
  }

  /** Provides a {@link MediaInfo} for a given {@link DataSource}. */
  public interface MediaInfoProvider {
    MediaInfo get(DataSource dataSource);
  }

  /**
   * Listener that is called when a new MediaPlayer is constructed.
   *
   * @see #setCreateListener(CreateListener)
   */
  protected static CreateListener createListener;

  private static final Map<DataSource, Exception> exceptions = new HashMap<>();
  private static final Map<DataSource, MediaInfo> mediaInfoMap = new HashMap<>();

  private static final MediaInfoProvider DEFAULT_MEDIA_INFO_PROVIDER = mediaInfoMap::get;
  private static MediaInfoProvider mediaInfoProvider = DEFAULT_MEDIA_INFO_PROVIDER;

  @RealObject
  private MediaPlayer player;

  /**
   * Possible states for the media player to be in. These states are as defined
   * in the documentation for {@link android.media.MediaPlayer}.
   */
  public enum State {
    IDLE, INITIALIZED, PREPARING, PREPARED, STARTED, STOPPED, PAUSED, PLAYBACK_COMPLETED, END, ERROR
  }

  /**
   * Possible behavior modes for the media player when a method is invoked in an invalid state.
   *
   * @see #setInvalidStateBehavior
   */
  public enum InvalidStateBehavior {
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
  private static class RunList extends ArrayList<MediaEvent> implements MediaEvent {

    public RunList() {
      // Set the default size to one as most of the time we will
      // only have one event.
      super(1);
    }

    @Override
    public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
      for (MediaEvent e : this) {
        e.run(mp, smp);
      }
    }
  }

  public interface MediaEvent {
    public void run(MediaPlayer mp, ShadowMediaPlayer smp);
  }

  /**
   * Class specifying information for an emulated media object. Used by
   * ShadowMediaPlayer when setDataSource() is called to populate the shadow
   * player with the specified values.
   */
  public static class MediaInfo {
    public int duration;
    private int preparationDelay;

    /** Map that maps time offsets to media events. */
    public TreeMap<Integer, RunList> events = new TreeMap<>();

    /**
     * Creates a new {@code MediaInfo} object with default duration (1000ms)
     * and default preparation delay (0ms).
     */
    public MediaInfo() {
      this(1000, 0);
    }

    /**
     * Creates a new {@code MediaInfo} object with the given duration and preparation delay. A
     * completion callback event is scheduled at {@code duration} ms from the end.
     *
     * @param duration the duration (in ms) of this emulated media. A callback event will be
     *     scheduled at this offset to stop playback simulation and invoke the completion callback.
     * @param preparationDelay the preparation delay (in ms) to emulate for this media. If set to
     *     -1, then {@link #prepare()} will complete instantly but {@link #prepareAsync()} will not
     *     complete automatically; you will need to call {@link #invokePreparedListener()} manually.
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
     * @param preparationDelay the new preparation delay (in ms).
     */
    public void setPreparationDelay(int preparationDelay) {
      this.preparationDelay = preparationDelay;
    }

    /**
     * Schedules a generic event to run at the specified playback offset. Events are run on the
     * thread on which the {@link android.media.MediaPlayer MediaPlayer} was created.
     *
     * @param offset the offset from the start of playback at which this event will run.
     * @param event the event to run.
     */
    public void scheduleEventAtOffset(int offset, MediaEvent event) {
      RunList runList = events.get(offset);
      if (runList == null) {
        // Given that most run lists will only contain one event,
        // we use 1 as the default capacity.
        runList = new RunList();
        events.put(offset, runList);
      }
      runList.add(event);
    }

    /**
     * Schedules an error event to run at the specified playback offset. A reference to the actual
     * MediaEvent that is scheduled is returned, which can be used in a subsequent call to {@link
     * #removeEventAtOffset}.
     *
     * @param offset the offset from the start of playback at which this error will trigger.
     * @param what the value for the {@code what} parameter to use in the call to {@link
     *     android.media.MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int) onError()}.
     * @param extra the value for the {@code extra} parameter to use in the call to {@link
     *     android.media.MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int) onError()}.
     * @return A reference to the MediaEvent object that was created and scheduled.
     */
    public MediaEvent scheduleErrorAtOffset(int offset, int what, int extra) {
      ErrorCallback callback = new ErrorCallback(what, extra);
      scheduleEventAtOffset(offset, callback);
      return callback;
    }

    /**
     * Schedules an info event to run at the specified playback offset. A reference to the actual
     * MediaEvent that is scheduled is returned, which can be used in a subsequent call to {@link
     * #removeEventAtOffset}.
     *
     * @param offset the offset from the start of playback at which this event will trigger.
     * @param what the value for the {@code what} parameter to use in the call to {@link
     *     android.media.MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int) onInfo()}.
     * @param extra the value for the {@code extra} parameter to use in the call to {@link
     *     android.media.MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int) onInfo()}.
     * @return A reference to the MediaEvent object that was created and scheduled.
     */
    public MediaEvent scheduleInfoAtOffset(int offset, final int what, final int extra) {
      MediaEvent callback = new MediaEvent() {
        @Override
        public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
          smp.invokeInfoListener(what, extra);
        }
      };
      scheduleEventAtOffset(offset, callback);
      return callback;
    }

    /**
     * Schedules a simulated buffer underrun event to run at the specified playback offset. A
     * reference to the actual MediaEvent that is scheduled is returned, which can be used in a
     * subsequent call to {@link #removeEventAtOffset}.
     *
     * <p>This event will issue an {@link MediaPlayer.OnInfoListener#onInfo onInfo()} callback with
     * {@link MediaPlayer#MEDIA_INFO_BUFFERING_START} to signal the start of buffering and then call
     * {@link #doStop()} to internally pause playback. Finally it will schedule an event to fire
     * after {@code length} ms which fires a {@link MediaPlayer#MEDIA_INFO_BUFFERING_END} info event
     * and invokes {@link #doStart()} to resume playback.
     *
     * @param offset the offset from the start of playback at which this underrun will trigger.
     * @param length the length of time (in ms) for which playback will be paused.
     * @return A reference to the MediaEvent object that was created and scheduled.
     */
    public MediaEvent scheduleBufferUnderrunAtOffset(int offset, final int length) {
      final MediaEvent restart = new MediaEvent() {
        @Override
        public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
          smp.invokeInfoListener(MediaPlayer.MEDIA_INFO_BUFFERING_END, 0);
          smp.doStart();
        }
      };
      MediaEvent callback = new MediaEvent() {
        @Override
        public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
          smp.doStop();
          smp.invokeInfoListener(MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
          smp.postEventDelayed(restart, length);
        }
      };
      scheduleEventAtOffset(offset, callback);
      return callback;
    }

    /**
     * Removes the specified event from the playback schedule at the given playback offset.
     *
     * @param offset the offset at which the event was scheduled.
     * @param event the event to remove.
     * @see ShadowMediaPlayer.MediaInfo#removeEvent(ShadowMediaPlayer.MediaEvent)
     */
    public void removeEventAtOffset(int offset, MediaEvent event) {
      RunList runList = events.get(offset);
      if (runList != null) {
        runList.remove(event);
        if (runList.isEmpty()) {
          events.remove(offset);
        }
      }
    }

    /**
     * Removes the specified event from the playback schedule at all playback offsets where it has
     * been scheduled.
     *
     * @param event the event to remove.
     * @see ShadowMediaPlayer.MediaInfo#removeEventAtOffset(int,ShadowMediaPlayer.MediaEvent)
     */
    public void removeEvent(MediaEvent event) {
      for (Iterator<Entry<Integer, RunList>> iter = events.entrySet()
          .iterator(); iter.hasNext();) {
        Entry<Integer, RunList> entry = iter.next();
        RunList runList = entry.getValue();
        runList.remove(event);
        if (runList.isEmpty()) {
          iter.remove();
        }
      }
    }
  }

  public void postEvent(MediaEvent e) {
    Message msg = handler.obtainMessage(MEDIA_EVENT, e);
    handler.sendMessage(msg);
  }

  public void postEventDelayed(MediaEvent e, long delay) {
    Message msg = handler.obtainMessage(MEDIA_EVENT, e);
    handler.sendMessageDelayed(msg, delay);
  }

  /**
   * Callback interface for clients that wish to be informed when a new {@link MediaPlayer} instance
   * is constructed.
   *
   * @see #setCreateListener
   */
  public static interface CreateListener {
    /**
     * Method that is invoked when a new {@link MediaPlayer} is created. This method is invoked at
     * the end of the constructor, after all of the default setup has been completed.
     *
     * @param player reference to the newly-created media player object.
     * @param shadow reference to the corresponding shadow object for the newly-created media player
     *     (provided for convenience).
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
  private MediaInfo mediaInfo;

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

  private static final MediaEvent completionCallback = new MediaEvent() {
    @Override
    public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
      if (mp.isLooping()) {
        smp.startOffset = 0;
        smp.doStart();
      } else {
        smp.doStop();
        smp.invokeCompletionListener();
      }
    }
  };

  private static final MediaEvent preparedCallback = new MediaEvent() {
    @Override
    public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
      smp.invokePreparedListener();
    }
  };

  private static final MediaEvent seekCompleteCallback = new MediaEvent() {
    @Override
    public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
      smp.invokeSeekCompleteListener();
    }
  };

  /**
   * Callback to use when a method is invoked from an invalid state. Has
   * {@code what = -38} and {@code extra = 0}, which are values that
   * were determined by inspection.
   */
  private static final ErrorCallback invalidStateErrorCallback = new ErrorCallback(
      -38, 0);

  public static final int MEDIA_EVENT = 1;

  /** Callback to use for scheduled errors. */
  private static class ErrorCallback implements MediaEvent {
    private int what;
    private int extra;

    public ErrorCallback(int what, int extra) {
      this.what = what;
      this.extra = extra;
    }

    @Override
    public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
      smp.invokeErrorListener(what, extra);
    }
  }

  @Implementation
  protected static MediaPlayer create(Context context, int resId) {
    MediaPlayer mp = new MediaPlayer();
    ShadowMediaPlayer shadow = Shadow.extract(mp);
    shadow.sourceResId = resId;
    try {
      shadow.setState(INITIALIZED);
      mp.setDataSource("android.resource://" + context.getPackageName() + "/" + resId);
      mp.prepare();
    } catch (Exception e) {
      return null;
    }

    return mp;
  }

  @Implementation
  protected static MediaPlayer create(Context context, Uri uri) {
    MediaPlayer mp = new MediaPlayer();
    try {
      mp.setDataSource(context, uri);
      mp.prepare();
    } catch (Exception e) {
      return null;
    }

    return mp;
  }

  @Implementation
  protected void __constructor__() {
    // Contract of audioSessionId is that if it is 0 (which represents
    // the master mix) then that's an error. By default it generates
    // an ID that is unique system-wide. We could simulate guaranteed
    // uniqueness (get AudioManager's help?) but it's probably not
    // worth the effort - a random non-zero number will probably do.
    Random random = new Random();
    audioSessionId = random.nextInt(Integer.MAX_VALUE) + 1;
    Looper myLooper = Looper.myLooper();
    if (myLooper != null) {
      handler = getHandler(myLooper);
    } else {
      handler = getHandler(Looper.getMainLooper());
    }
    // This gives test suites a chance to customize the MP instance
    // and its shadow when it is created, without having to modify
    // the code under test in order to do so.
    if (createListener != null) {
      createListener.onCreate(player, this);
    }
    // Ensure that the real object is set up properly.
    Shadow.invokeConstructor(MediaPlayer.class, player);
  }

  private Handler getHandler(Looper looper) {
    return new Handler(looper) {
      @Override
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case MEDIA_EVENT:
            MediaEvent e = (MediaEvent) msg.obj;
            e.run(player, ShadowMediaPlayer.this);
            scheduleNextPlaybackEvent();
            break;
        }
      }
    };
  }

  /**
   * Common code path for all {@code setDataSource()} implementations.
   *
   * <p>* Checks for any specified exceptions for the specified data source and throws them. *
   * Checks the current state and throws an exception if it is in an invalid state. * If no
   * exception is thrown in either of the previous two steps, then {@link
   * #doSetDataSource(DataSource)} is called to set the data source. * Sets the player state to
   * {@code INITIALIZED}. Usually this method would not be called directly, but indirectly through
   * one of the other {@link #setDataSource(String)} implementations, which use {@link
   * DataSource#toDataSource(String)} methods to convert their discrete parameters into a single
   * {@link DataSource} instance.
   *
   * @param dataSource the data source that is being set.
   * @throws IOException if the specified data source has been configured to throw an IO exception.
   * @see #addException(DataSource, IOException)
   * @see #addException(DataSource, RuntimeException)
   * @see #doSetDataSource(DataSource)
   */
  public void setDataSource(DataSource dataSource) throws IOException {
    Exception e = exceptions.get(dataSource);
    if (e != null) {
      e.fillInStackTrace();
      if (e instanceof IOException) {
        throw (IOException) e;
      } else if (e instanceof RuntimeException) {
        throw (RuntimeException) e;
      }
      throw new AssertionError("Invalid exception type for setDataSource: <" + e + '>');
    }
    checkStateException("setDataSource()", idleState);
    doSetDataSource(dataSource);
    state = INITIALIZED;
  }

  @Implementation
  protected void setDataSource(String path) throws IOException {
    setDataSource(toDataSource(path));
  }

  @Implementation(maxSdk = N_MR1)
  protected void setDataSource(Context context, Uri uri) throws IOException {
    setDataSource(context, uri, null, null);
  }

  @Implementation(minSdk = ICE_CREAM_SANDWICH, maxSdk = N_MR1)
  protected void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException {
    setDataSource(context, uri, headers, null);
  }

  @Implementation(minSdk = O)
  protected void setDataSource(
      Context context,
      Uri uri,
      Map<String, String> headers,
      List<HttpCookie> cookies) throws IOException {
    setDataSource(toDataSource(context, uri, headers, cookies));
    sourceUri = uri;
  }

  @Implementation
  protected void setDataSource(String uri, Map<String, String> headers) throws IOException {
    setDataSource(toDataSource(uri, headers));
  }

  @Implementation
  protected void setDataSource(FileDescriptor fd, long offset, long length) throws IOException {
    setDataSource(toDataSource(fd, offset, length));
  }

  @Implementation(minSdk = M)
  protected void setDataSource(MediaDataSource mediaDataSource) throws IOException {
    setDataSource(toDataSource(mediaDataSource));
  }

  @Implementation(minSdk = N)
  protected void setDataSource(AssetFileDescriptor assetFileDescriptor) throws IOException {
    setDataSource(toDataSource(assetFileDescriptor));
  }

  /**
   * Sets the data source without doing any other emulation. Sets the internal data source only.
   * Calling directly can be useful for setting up a {@link ShadowMediaPlayer} instance during
   * specific testing so that you don't have to clutter your tests catching exceptions you know
   * won't be thrown.
   *
   * @param dataSource the data source that is being set.
   * @see #setDataSource(DataSource)
   */
  public void doSetDataSource(DataSource dataSource) {
    MediaInfo mediaInfo = mediaInfoProvider.get(dataSource);
    if (mediaInfo == null) {
      throw new IllegalArgumentException(
          "Don't know what to do with dataSource "
              + dataSource
              + " - either add an exception with addException() or media info with "
              + "addMediaInfo()");
    }
    this.mediaInfo = mediaInfo;
    this.dataSource = dataSource;
  }

  public static MediaInfo getMediaInfo(DataSource dataSource) {
    return mediaInfoProvider.get(dataSource);
  }

  /**
   * Adds a {@link MediaInfo} for a {@link DataSource}.
   *
   * <p>This overrides any {@link MediaInfoProvider} previously set by calling {@link
   * #setMediaInfoProvider}, i.e., the provider will not be used for any {@link DataSource}.
   */
  public static void addMediaInfo(DataSource dataSource, MediaInfo info) {
    ShadowMediaPlayer.mediaInfoProvider = DEFAULT_MEDIA_INFO_PROVIDER;
    mediaInfoMap.put(dataSource, info);
  }

  /**
   * Sets a {@link MediaInfoProvider} to be used to get {@link MediaInfo} for any {@link
   * DataSource}.
   *
   * <p>This overrides any {@link MediaInfo} previously set by calling {@link #addMediaInfo}, i.e.,
   * {@link MediaInfo} provided by this {@link MediaInfoProvider} will be used instead.
   */
  public static void setMediaInfoProvider(MediaInfoProvider mediaInfoProvider) {
    ShadowMediaPlayer.mediaInfoProvider = mediaInfoProvider;
  }

  public static void addException(DataSource dataSource, RuntimeException e) {
    exceptions.put(dataSource, e);
  }

  public static void addException(DataSource dataSource, IOException e) {
    exceptions.put(dataSource, e);
  }

  /**
   * Checks states for methods that only log when there is an error. Such methods throw an {@link
   * IllegalArgumentException} when invoked in the END state, but log an error in other disallowed
   * states. This method will either emulate this behavior or else will generate an assertion if
   * invoked from a disallowed state if {@link #setAssertOnError assertOnError} is set.
   *
   * @param method the name of the method being tested.
   * @param allowedStates the states that this method is allowed to be called from.
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
   * Checks states for methods that asynchronously invoke {@link
   * android.media.MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int) onError()} when
   * invoked in an illegal state. Such methods always throw {@link IllegalStateException} rather
   * than invoke {@code onError()} if they are invoked from the END state.
   *
   * <p>This method will either emulate this behavior by posting an {@code onError()} callback to
   * the current thread's message queue (or throw an {@link IllegalStateException} if invoked from
   * the END state), or else it will generate an assertion if {@link #setAssertOnError
   * assertOnError} is set.
   *
   * @param method the name of the method being tested.
   * @param allowedStates the states that this method is allowed to be called from.
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
        postEvent(invalidStateErrorCallback);
        return false;
      case ASSERT:
        String msg = "Can't call " + method + " from state " + state;
        throw new AssertionError(msg);
      }
    }
    return true;
  }

  /**
   * Checks states for methods that synchronously throw an exception when invoked in an illegal
   * state. This method will likewise throw an {@link IllegalArgumentException} if it determines
   * that the method has been invoked from a disallowed state, or else it will generate an assertion
   * if {@link #setAssertOnError assertOnError} is set.
   *
   * @param method the name of the method being tested.
   * @param allowedStates the states that this method is allowed to be called from.
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
  protected void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
    completionListener = listener;
  }

  @Implementation
  protected void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener listener) {
    seekCompleteListener = listener;
  }

  @Implementation
  protected void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
    preparedListener = listener;
  }

  @Implementation
  protected void setOnInfoListener(MediaPlayer.OnInfoListener listener) {
    infoListener = listener;
  }

  @Implementation
  protected void setOnErrorListener(MediaPlayer.OnErrorListener listener) {
    errorListener = listener;
  }

  @Implementation
  protected boolean isLooping() {
    checkStateException("isLooping()", nonEndStates);
    return looping;
  }

  static private final EnumSet<State> nonEndStates = EnumSet
      .complementOf(EnumSet.of(END));
  static private final EnumSet<State> nonErrorStates = EnumSet
      .complementOf(EnumSet.of(ERROR, END));

  @Implementation
  protected void setLooping(boolean looping) {
    checkStateError("setLooping()", nonErrorStates);
    this.looping = looping;
  }

  @Implementation
  protected void setVolume(float left, float right) {
    checkStateError("setVolume()", nonErrorStates);
    leftVolume = left;
    rightVolume = right;
  }

  @Implementation
  protected boolean isPlaying() {
    checkStateError("isPlaying()", nonErrorStates);
    return state == STARTED;
  }

  private static EnumSet<State> preparableStates = EnumSet.of(INITIALIZED,
      STOPPED);

  /**
   * Simulates {@link MediaPlayer#prepareAsync()}. Sleeps for {@link MediaInfo#getPreparationDelay()
   * preparationDelay} ms by calling {@link SystemClock#sleep(long)} before calling {@link
   * #invokePreparedListener()}.
   *
   * <p>If {@code preparationDelay} is not positive and non-zero, there is no sleep.
   *
   * @see MediaInfo#setPreparationDelay(int)
   * @see #invokePreparedListener()
   */
  @Implementation
  protected void prepare() {
    checkStateException("prepare()", preparableStates);
    MediaInfo info = getMediaInfo();
    if (info.preparationDelay > 0) {
      SystemClock.sleep(info.preparationDelay);
    }
    state = PREPARED;
    postEvent(
        (mp, smp) -> {
          if (preparedListener != null) {
            preparedListener.onPrepared(mp);
          }
        });
  }

  /**
   * Simulates {@link MediaPlayer#prepareAsync()}. Sets state to PREPARING and posts a callback to
   * {@link #invokePreparedListener()} if the current preparation delay for the current media (see
   * {@link #getMediaInfo()}) is &gt;= 0, otherwise the test suite is responsible for calling {@link
   * #invokePreparedListener()} directly if required.
   *
   * @see MediaInfo#setPreparationDelay(int)
   * @see #invokePreparedListener()
   */
  @Implementation
  protected void prepareAsync() {
    checkStateException("prepareAsync()", preparableStates);
    state = PREPARING;
    MediaInfo info = getMediaInfo();
    if (info.preparationDelay >= 0) {
      postEventDelayed(preparedCallback, info.preparationDelay);
    }
  }

  private static EnumSet<State> startableStates = EnumSet.of(PREPARED, STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  /**
   * Simulates private native method {@link MediaPlayer#_start()}. Sets state to STARTED and calls
   * {@link #doStart()} to start scheduling playback callback events.
   *
   * <p>If the current state is PLAYBACK_COMPLETED, the current position is reset to zero before
   * starting playback.
   *
   * @see #doStart()
   */
  @Implementation
  protected void start() {
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
    MediaInfo info = getMediaInfo();
    Entry<Integer, RunList> event = info.events.higherEntry(currentPosition);
    if (event == null) {
      // This means we've "seeked" past the end. Get the last
      // event (which should be the completion event) and
      // invoke that, setting the position to the duration.
      postEvent(completionCallback);
    } else {
      final int runListOffset = event.getKey();
      nextPlaybackEvent = event.getValue();
      postEventDelayed(nextPlaybackEvent, runListOffset - currentPosition);
    }
  }

  /**
   * Tests to see if the player is really playing.
   *
   * <p>The player is defined as "really playing" if simulated playback events (including playback
   * completion) are being scheduled and invoked and {@link #getCurrentPosition currentPosition} is
   * being updated as time passes. Note that while the player will normally be really playing if in
   * the STARTED state, this is not always the case - for example, if a pending seek is in progress,
   * or perhaps a buffer underrun is being simulated.
   *
   * @return {@code true} if the player is really playing or {@code false} if the player is
   *     internally paused.
   * @see #doStart
   * @see #doStop
   */
  public boolean isReallyPlaying() {
    return startTime >= 0;
  }

  /**
   * Starts simulated playback. Until this method is called, the player is not "really playing" (see
   * {@link #isReallyPlaying} for a definition of "really playing").
   *
   * <p>This method is used internally by the various shadow method implementations of the
   * MediaPlayer public API, but may also be called directly by the test suite if you wish to
   * simulate an internal pause. For example, to simulate a buffer underrun (player is in PLAYING
   * state but isn't actually advancing the current position through the media), you could call
   * {@link #doStop()} to mark the start of the buffer underrun and {@link #doStart()} to mark its
   * end and restart normal playback (which is what {@link
   * ShadowMediaPlayer.MediaInfo#scheduleBufferUnderrunAtOffset(int, int)
   * scheduleBufferUnderrunAtOffset()} does).
   *
   * @see #isReallyPlaying()
   * @see #doStop()
   */
  public void doStart() {
    startTime = SystemClock.uptimeMillis();
    scheduleNextPlaybackEvent();
  }

  /**
   * Pauses simulated playback. After this method is called, the player is no longer "really
   * playing" (see {@link #isReallyPlaying} for a definition of "really playing").
   *
   * <p>This method is used internally by the various shadow method implementations of the
   * MediaPlayer public API, but may also be called directly by the test suite if you wish to
   * simulate an internal pause.
   *
   * @see #isReallyPlaying()
   * @see #doStart()
   */
  public void doStop() {
    startOffset = getCurrentPositionRaw();
    if (nextPlaybackEvent != null) {
      handler.removeMessages(MEDIA_EVENT);
      nextPlaybackEvent = null;
    }
    startTime = -1;
  }

  private static final EnumSet<State> pausableStates = EnumSet.of(STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  /**
   * Simulates {@link MediaPlayer#_pause()}. Invokes {@link #doStop()} to suspend playback event
   * callbacks and sets the state to PAUSED.
   *
   * @see #doStop()
   */
  @Implementation
  protected void _pause() {
    if (checkStateError("pause()", pausableStates)) {
      doStop();
      state = PAUSED;
    }
  }

  static final EnumSet<State> allStates = EnumSet.allOf(State.class);

  /**
   * Simulates call to {@link MediaPlayer#_release()}. Calls {@link #doStop()} to suspend playback
   * event callbacks and sets the state to END.
   */
  @Implementation
  protected void _release() {
    checkStateException("release()", allStates);
    doStop();
    state = END;
    handler.removeMessages(MEDIA_EVENT);
  }

  /**
   * Simulates call to {@link MediaPlayer#_reset()}. Calls {@link #doStop()} to suspend playback
   * event callbacks and sets the state to IDLE.
   */
  @Implementation
  protected void _reset() {
    checkStateException("reset()", nonEndStates);
    doStop();
    state = IDLE;
    handler.removeMessages(MEDIA_EVENT);
    startOffset = 0;
  }

  static private final EnumSet<State> stoppableStates = EnumSet.of(PREPARED,
      STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED);

  /**
   * Simulates call to {@link MediaPlayer#release()}. Calls {@link #doStop()} to suspend playback
   * event callbacks and sets the state to STOPPED.
   */
  @Implementation
  protected void _stop() {
    if (checkStateError("stop()", stoppableStates)) {
      doStop();
      state = STOPPED;
    }
  }

  private static final EnumSet<State> attachableStates = EnumSet.of(
      INITIALIZED, PREPARING, PREPARED, STARTED, PAUSED, STOPPED,
      PLAYBACK_COMPLETED);

  @Implementation
  protected void attachAuxEffect(int effectId) {
    checkStateError("attachAuxEffect()", attachableStates);
    auxEffect = effectId;
  }

  @Implementation
  protected int getAudioSessionId() {
    checkStateException("getAudioSessionId()", allStates);
    return audioSessionId;
  }

  /**
   * Simulates call to {@link MediaPlayer#getCurrentPosition()}. Simply does the state validity
   * checks and then invokes {@link #getCurrentPositionRaw()} to calculate the simulated playback
   * position.
   *
   * @return The current offset (in ms) of the simulated playback.
   * @see #getCurrentPositionRaw()
   */
  @Implementation
  protected int getCurrentPosition() {
    checkStateError("getCurrentPosition()", attachableStates);
    return getCurrentPositionRaw();
  }

  /**
   * Simulates call to {@link MediaPlayer#getDuration()}. Retrieves the duration as defined by the
   * current {@link MediaInfo} instance.
   *
   * @return The duration (in ms) of the current simulated playback.
   * @see #addMediaInfo(DataSource, MediaInfo)
   */
  @Implementation
  protected int getDuration() {
    checkStateError("getDuration()", stoppableStates);
    return getMediaInfo().duration;
  }

  @Implementation
  protected int getVideoHeight() {
    checkStateLog("getVideoHeight()", attachableStates);
    return videoHeight;
  }

  @Implementation
  protected int getVideoWidth() {
    checkStateLog("getVideoWidth()", attachableStates);
    return videoWidth;
  }

  private static final EnumSet<State> seekableStates = EnumSet.of(PREPARED,
      STARTED, PAUSED, PLAYBACK_COMPLETED);

  /**
   * Simulates seeking to specified position. The seek will complete after {@link #seekDelay} ms
   * (defaults to 0), or else if seekDelay is negative then the controlling test is expected to
   * simulate seek completion by manually invoking {@link #invokeSeekCompleteListener}.
   *
   * @param seekTo the offset (in ms) from the start of the track to seek to.
   */
  @Implementation
  protected void seekTo(int seekTo) {
    seekTo(seekTo, MediaPlayer.SEEK_PREVIOUS_SYNC);
  }

  @Implementation(minSdk = O)
  protected void seekTo(long seekTo, int mode) {
    boolean success = checkStateError("seekTo()", seekableStates);
    // Cancel any pending seek operations.
    handler.removeMessages(MEDIA_EVENT, seekCompleteCallback);

    if (success) {
      // Need to call doStop() before setting pendingSeek,
      // because if pendingSeek is called it changes
      // the behavior of getCurrentPosition(), which doStop()
      // depends on.
      doStop();
      pendingSeek = (int) seekTo;
      if (seekDelay >= 0) {
        postEventDelayed(seekCompleteCallback, seekDelay);
      }
    }
  }

  static private final EnumSet<State> idleState = EnumSet.of(IDLE);

  @Implementation
  protected void setAudioSessionId(int sessionId) {
    checkStateError("setAudioSessionId()", idleState);
    audioSessionId = sessionId;
  }

  static private final EnumSet<State> nonPlayingStates = EnumSet.of(IDLE,
      INITIALIZED, STOPPED);

  @Implementation
  protected void setAudioStreamType(int audioStreamType) {
    checkStateError("setAudioStreamType()", nonPlayingStates);
    this.audioStreamType = audioStreamType;
  }

  /**
   * Sets a listener that is invoked whenever a new shadowed {@link MediaPlayer} object is
   * constructed.
   *
   * <p>Registering a listener gives you a chance to customize the shadowed object appropriately
   * without needing to modify the application-under-test to provide access to the instance at the
   * appropriate point in its life cycle. This is useful because normally a new {@link MediaPlayer}
   * is created and {@link #setDataSource setDataSource()} is invoked soon after, without a break in
   * the code. Using this callback means you don't have to change this common pattern just so that
   * you can customize the shadow for testing.
   *
   * @param createListener the listener to be invoked
   */
  public static void setCreateListener(CreateListener createListener) {
    ShadowMediaPlayer.createListener = createListener;
  }

  /**
   * Retrieves the {@link Handler} object used by this {@code ShadowMediaPlayer}. Can be used for
   * posting custom asynchronous events to the thread (eg, asynchronous errors). Use this for
   * scheduling events to take place at a particular "real" time (ie, time as measured by the
   * scheduler). For scheduling events to occur at a particular playback offset (no matter how long
   * playback may be paused for, or where you seek to, etc), see {@link
   * MediaInfo#scheduleEventAtOffset(int, ShadowMediaPlayer.MediaEvent)} and its various helpers.
   *
   * @return Handler object that can be used to schedule asynchronous events on this media player.
   */
  public Handler getHandler() {
    return handler;
  }

  /**
   * Retrieves current flag specifying the behavior of the media player when a method is invoked in
   * an invalid state. See {@link #setInvalidStateBehavior(InvalidStateBehavior)} for a discussion
   * of the available modes and their associated behaviors.
   *
   * @return The current invalid state behavior mode.
   * @see #setInvalidStateBehavior
   */
  public InvalidStateBehavior getInvalidStateBehavior() {
    return invalidStateBehavior;
  }

  /**
   * Specifies how the media player should behave when a method is invoked in an invalid state.
   * Three modes are supported (as defined by the {@link InvalidStateBehavior} enum):
   *
   * <h3>{@link InvalidStateBehavior#SILENT SILENT}</h3>
   *
   * No invalid state checking is done at all. All methods can be invoked from any state without
   * throwing any exceptions or invoking the error listener.
   *
   * <p>This mode is provided primarily for backwards compatibility, and for this reason it is the
   * default. For proper testing one of the other two modes is probably preferable.
   *
   * <h3>{@link InvalidStateBehavior#EMULATE EMULATE}</h3>
   *
   * The shadow will attempt to emulate the behavior of the actual {@link MediaPlayer}
   * implementation. This is based on a reading of the documentation and on actual experiments done
   * on a Jelly Bean device. The official documentation is not all that clear, but basically methods
   * fall into three categories:
   *
   * <ul>
   *   <li>Those that log an error when invoked in an invalid state but don't throw an exception or
   *       invoke {@code onError()}. An example is {@link #getVideoHeight()}.
   *   <li>Synchronous error handling: methods always throw an exception (usually {@link
   *       IllegalStateException} but don't invoke {@code onError()}. Examples are {@link
   *       #prepare()} and {@link #setDataSource(String)}.
   *   <li>Asynchronous error handling: methods don't throw an exception but invoke {@code
   *       onError()}.
   * </ul>
   *
   * Additionally, all three methods behave synchronously (throwing {@link IllegalStateException}
   * when invoked from the END state.
   *
   * <p>To complicate matters slightly, the official documentation sometimes contradicts observed
   * behavior. For example, the documentation says it is illegal to call {@link #setDataSource} from
   * the ERROR state - however, in practice it works fine. Conversely, the documentation says that
   * it is legal to invoke {@link #getCurrentPosition()} from the INITIALIZED state, however testing
   * showed that this caused an error. Wherever there is a discrepancy between documented and
   * observed behavior, this implementation has gone with the most conservative implementation (ie,
   * it is illegal to invoke {@link #setDataSource} from the ERROR state and likewise illegal to
   * invoke {@link #getCurrentPosition()} from the INITIALIZED state.
   *
   * <h3>{@link InvalidStateBehavior#ASSERT ASSERT}</h3>
   *
   * The shadow will raise an assertion any time that a method is invoked in an invalid state. The
   * philosophy behind this mode is that to invoke a method in an invalid state is a programming
   * error - a bug, pure and simple. As such it should be discovered and eliminated at development
   * and testing time, rather than anticipated and handled at runtime. Asserting is a way of testing
   * for these bugs during testing.
   *
   * @param invalidStateBehavior the behavior mode for this shadow to use during testing.
   * @see #getInvalidStateBehavior()
   */
  public void setInvalidStateBehavior(InvalidStateBehavior invalidStateBehavior) {
    this.invalidStateBehavior = invalidStateBehavior;
  }

  /**
   * Retrieves the currently selected {@link MediaInfo}. This instance is used to define current
   * duration, preparation delay, exceptions for {@code setDataSource()}, playback events, etc.
   *
   * @return The currently selected {@link MediaInfo}.
   * @see #addMediaInfo
   * @see #setMediaInfoProvider
   * @see #doSetDataSource(DataSource)
   */
  public MediaInfo getMediaInfo() {
    return mediaInfo;
  }

  /**
   * Sets the current position, bypassing the normal state checking. Use with care.
   *
   * @param position the new playback position.
   */
  public void setCurrentPosition(int position) {
    startOffset = position;
  }

  /**
   * Retrieves the current position without doing the state checking that the emulated version of
   * {@link #getCurrentPosition()} does.
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
   * Retrieves the current duration without doing the state checking that the emulated version does.
   *
   * @return The duration of the current clip loaded by the player.
   */
  public int getDurationRaw() {
    return getMediaInfo().duration;
  }

  /**
   * Retrieves the current state of the {@link MediaPlayer}. Uses the states as defined in the
   * {@link MediaPlayer} documentation.
   *
   * @return The current state of the {@link MediaPlayer}, as defined in the MediaPlayer
   *     documentation.
   * @see #setState
   * @see MediaPlayer
   */
  public State getState() {
    return state;
  }

  /**
   * Forces the @link MediaPlayer} into the specified state. Uses the states as defined in the
   * {@link MediaPlayer} documentation.
   *
   * <p>Note that by invoking this method directly you can get the player into an inconsistent state
   * that a real player could not be put in (eg, in the END state but with playback events still
   * happening). Use with care.
   *
   * @param state the new state of the {@link MediaPlayer}, as defined in the MediaPlayer
   *     documentation.
   * @see #getState
   * @see MediaPlayer
   */
  public void setState(State state) {
    this.state = state;
  }

  /**
   * Note: This has a funny name at the moment to avoid having to produce an API-specific shadow -
   * if it were called {@code getAudioStreamType()} then the {@code RobolectricWiringTest} will
   * inform us that it should be annotated with {@link Implementation}, because there is a private
   * method in the later API versions with the same name, however this would fail on earlier
   * versions.
   *
   * @return audioStreamType
   */
  public int getTheAudioStreamType() {
    return audioStreamType;
  }

  /**
   * @return seekDelay
   */
  public int getSeekDelay() {
    return seekDelay;
  }

  /**
   * Sets the length of time (ms) that seekTo() will delay before completing. Default is 0. If set
   * to -1, then seekTo() will not call the OnSeekCompleteListener automatically; you will need to
   * call invokeSeekCompleteListener() manually.
   *
   * @param seekDelay length of time to delay (ms)
   */
  public void setSeekDelay(int seekDelay) {
    this.seekDelay = seekDelay;
  }

  /**
   * Useful for assertions.
   *
   * @return The current {@code auxEffect} setting.
   */
  public int getAuxEffect() {
    return auxEffect;
  }

  /**
   * Retrieves the pending seek setting.
   *
   * @return The position to which the shadow player is seeking for the seek in progress (ie, after
   *     the call to {@link #seekTo} but before a call to {@link #invokeSeekCompleteListener()}).
   *     Returns {@code -1} if no seek is in progress.
   */
  public int getPendingSeek() {
    return pendingSeek;
  }

  /**
   * Retrieves the data source (if any) that was passed in to {@link #setDataSource(DataSource)}.
   *
   * <p>Useful for assertions.
   *
   * @return The source passed in to {@code setDataSource}.
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Retrieves the source path (if any) that was passed in to {@link
   * MediaPlayer#setDataSource(Context, Uri, Map)} or {@link MediaPlayer#setDataSource(Context,
   * Uri)}.
   *
   * @return The source Uri passed in to {@code setDataSource}.
   */
  public Uri getSourceUri() {
    return sourceUri;
  }

  /**
   * Retrieves the resource ID used in the call to {@link #create(Context, int)} (if any).
   *
   * @return The resource ID passed in to {@code create()}, or {@code -1} if a different method of
   *     setting the source was used.
   */
  public int getSourceResId() {
    return sourceResId;
  }

  /**
   * Retrieves the current setting for the left channel volume.
   *
   * @return The left channel volume.
   */
  public float getLeftVolume() {
    return leftVolume;
  }

  /**
   * @return The right channel volume.
   */
  public float getRightVolume() {
    return rightVolume;
  }

  @Implementation(minSdk = P)
  protected boolean native_setOutputDevice(int preferredDeviceId) {
    return true;
  }

  private static EnumSet<State> preparedStates = EnumSet.of(PREPARED, STARTED,
      PAUSED, PLAYBACK_COMPLETED);

  /**
   * Tests to see if the player is in the PREPARED state. This is mainly used for backward
   * compatibility. {@link #getState} may be more useful for new testing applications.
   *
   * @return {@code true} if the MediaPlayer is in the PREPARED state, false otherwise.
   */
  public boolean isPrepared() {
    return preparedStates.contains(state);
  }

  /**
   * @return the OnCompletionListener
   */
  public MediaPlayer.OnCompletionListener getOnCompletionListener() {
    return completionListener;
  }

  /**
   * @return the OnPreparedListener
   */
  public MediaPlayer.OnPreparedListener getOnPreparedListener() {
    return preparedListener;
  }

  /**
   * Allows test cases to simulate 'prepared' state by invoking callback. Sets
   * the player's state to PREPARED and invokes the
   * {@link MediaPlayer.OnPreparedListener#onPrepared preparedListener()}
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
    int duration = getMediaInfo().duration;
    setCurrentPosition(pendingSeek > duration ? duration
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
   * @param what parameter to pass in to {@code what} in {@link
   *     MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int)}.
   * @param extra parameter to pass in to {@code extra} in {@link
   *     MediaPlayer.OnInfoListener#onInfo(MediaPlayer, int, int)}.
   */
  public void invokeInfoListener(int what, int extra) {
    if (infoListener != null) {
      infoListener.onInfo(player, what, extra);
    }
  }

  /**
   * Allows test cases to directly simulate invocation of the OnError event.
   *
   * @param what parameter to pass in to {@code what} in {@link
   *     MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)}.
   * @param extra parameter to pass in to {@code extra} in {@link
   *     MediaPlayer.OnErrorListener#onError(MediaPlayer, int, int)}.
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
    mediaInfoProvider = DEFAULT_MEDIA_INFO_PROVIDER;
    exceptions.clear();
    mediaInfoMap.clear();
  }
}
