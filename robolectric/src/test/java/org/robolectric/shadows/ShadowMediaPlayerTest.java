package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
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
import static org.robolectric.shadows.ShadowMediaPlayer.addException;
import static org.robolectric.shadows.util.DataSource.toDataSource;

import android.app.Application;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowMediaPlayer.InvalidStateBehavior;
import org.robolectric.shadows.ShadowMediaPlayer.MediaEvent;
import org.robolectric.shadows.ShadowMediaPlayer.MediaInfo;
import org.robolectric.shadows.ShadowMediaPlayer.State;
import org.robolectric.shadows.util.DataSource;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
public class ShadowMediaPlayerTest {

  private static final String DUMMY_SOURCE = "dummy-source";
  
  private MediaPlayer mediaPlayer;
  private ShadowMediaPlayer shadowMediaPlayer;
  private MediaPlayer.OnCompletionListener completionListener;
  private MediaPlayer.OnErrorListener errorListener;
  private MediaPlayer.OnInfoListener infoListener;
  private MediaPlayer.OnPreparedListener preparedListener;
  private MediaPlayer.OnSeekCompleteListener seekListener;
  private Scheduler scheduler;
  private MediaInfo info;
  private DataSource defaultSource;
  
  @Before
  public void setUp() {
    mediaPlayer = Shadow.newInstanceOf(MediaPlayer.class);
    shadowMediaPlayer = shadowOf(mediaPlayer);

    completionListener = Mockito.mock(MediaPlayer.OnCompletionListener.class);
    mediaPlayer.setOnCompletionListener(completionListener);

    preparedListener = Mockito.mock(MediaPlayer.OnPreparedListener.class);
    mediaPlayer.setOnPreparedListener(preparedListener);

    errorListener = Mockito.mock(MediaPlayer.OnErrorListener.class);
    mediaPlayer.setOnErrorListener(errorListener);

    infoListener = Mockito.mock(MediaPlayer.OnInfoListener.class);
    mediaPlayer.setOnInfoListener(infoListener);

    seekListener = Mockito.mock(MediaPlayer.OnSeekCompleteListener.class);
    mediaPlayer.setOnSeekCompleteListener(seekListener);

    // Scheduler is used in many of the tests to simulate
    // moving forward in time.
    scheduler = Robolectric.getForegroundThreadScheduler();
    scheduler.pause();

    defaultSource = toDataSource(DUMMY_SOURCE);
    info = new MediaInfo();
    ShadowMediaPlayer.addMediaInfo(defaultSource, info);
    shadowMediaPlayer.doSetDataSource(defaultSource);
  }

  @Test
  public void create_withResourceId_shouldSetDataSource() {
    Application context = ApplicationProvider.getApplicationContext();
    ShadowMediaPlayer.addMediaInfo(
        DataSource.toDataSource("android.resource://" + context.getPackageName() + "/123"),
        new ShadowMediaPlayer.MediaInfo(100, 10));

    MediaPlayer mp = MediaPlayer.create(context, 123);
    ShadowMediaPlayer shadow = shadowOf(mp);
    assertThat(shadow.getDataSource())
        .isEqualTo(
            DataSource.toDataSource("android.resource://" + context.getPackageName() + "/123"));
  }

  @Test
  public void testInitialState() {
    assertThat(shadowMediaPlayer.getState()).isEqualTo(IDLE);
  }

  @Test
  public void testCreateListener() {
    ShadowMediaPlayer.CreateListener createListener = Mockito
        .mock(ShadowMediaPlayer.CreateListener.class);
    ShadowMediaPlayer.setCreateListener(createListener);

    MediaPlayer newPlayer = new MediaPlayer();
    ShadowMediaPlayer shadow = shadowOf(newPlayer);

    Mockito.verify(createListener).onCreate(newPlayer, shadow);
  }

  @Test
  public void testResetResetsPosition() {
    shadowMediaPlayer.setCurrentPosition(300);
    mediaPlayer.reset();
    assertThat(shadowMediaPlayer.getCurrentPositionRaw())
      .isEqualTo(0);
  }
  
  @Test
  public void testPrepare() throws IOException {
    int[] testDelays = { 0, 10, 100, 1500 };

    for (int delay : testDelays) {
      final long startTime = scheduler.getCurrentTime();
      info.setPreparationDelay(delay);
      shadowMediaPlayer.setState(INITIALIZED);
      mediaPlayer.prepare();

      assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARED);
      assertThat(scheduler.getCurrentTime()).isEqualTo(startTime + delay);
    }
  }

  @Test
  public void testSetDataSourceString() throws IOException {
    DataSource ds = toDataSource("dummy");
    ShadowMediaPlayer.addMediaInfo(ds, info);
    mediaPlayer.setDataSource("dummy");
    assertThat(shadowMediaPlayer.getDataSource()).named("dataSource").isEqualTo(ds);
  }

  @Test
  public void testSetDataSourceUri() throws IOException {
    Map<String, String> headers = new HashMap<>();
    Uri uri = Uri.parse("file:/test");
    DataSource ds = toDataSource(ApplicationProvider.getApplicationContext(), uri, headers);
    ShadowMediaPlayer.addMediaInfo(ds, info);

    mediaPlayer.setDataSource(ApplicationProvider.getApplicationContext(), uri, headers);

    assertThat(shadowMediaPlayer.getSourceUri()).named("sourceUri").isSameAs(uri);
    assertThat(shadowMediaPlayer.getDataSource()).named("dataSource").isEqualTo(ds);
  }

  @Test
  public void testSetDataSourceFD() throws IOException {
    File tmpFile = File.createTempFile("MediaPlayerTest", null);
    try {
      tmpFile.deleteOnExit();
      FileInputStream is = new FileInputStream(tmpFile);
      try {
        FileDescriptor fd = is.getFD();
        DataSource ds = toDataSource(fd, 23, 524);
        ShadowMediaPlayer.addMediaInfo(ds, info);
        mediaPlayer.setDataSource(fd, 23, 524);
        assertThat(shadowMediaPlayer.getSourceUri()).named("sourceUri").isNull();
        assertThat(shadowMediaPlayer.getDataSource()).named("dataSource")
            .isEqualTo(ds);
      } finally {
        is.close();
      }
    } finally {
      tmpFile.delete();
    }
  }

  @Test
  public void testPrepareAsyncAutoCallback() {
    mediaPlayer.setOnPreparedListener(preparedListener);
    int[] testDelays = { 0, 10, 100, 1500 };

    for (int delay : testDelays) {
      info.setPreparationDelay(delay);
      shadowMediaPlayer.setState(INITIALIZED);
      final long startTime = scheduler.getCurrentTime();
      mediaPlayer.prepareAsync();

      assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARING);
      Mockito.verifyZeroInteractions(preparedListener);
      scheduler.advanceToLastPostedRunnable();
      assertThat(scheduler.getCurrentTime()).named("currentTime").isEqualTo(
          startTime + delay);
      assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARED);
      Mockito.verify(preparedListener).onPrepared(mediaPlayer);
      Mockito.verifyNoMoreInteractions(preparedListener);
      Mockito.reset(preparedListener);
    }
  }

  @Test
  public void testPrepareAsyncManualCallback() {
    mediaPlayer.setOnPreparedListener(preparedListener);
    info.setPreparationDelay(-1);
    
    shadowMediaPlayer.setState(INITIALIZED);
    final long startTime = scheduler.getCurrentTime();
    mediaPlayer.prepareAsync();

    assertThat(scheduler.getCurrentTime()).named("currentTime").isEqualTo(
        startTime);
    assertThat(shadowMediaPlayer.getState()).isSameAs(PREPARING);
    Mockito.verifyZeroInteractions(preparedListener);
    shadowMediaPlayer.invokePreparedListener();
    assertThat(shadowMediaPlayer.getState()).isSameAs(PREPARED);
    Mockito.verify(preparedListener).onPrepared(mediaPlayer);
    Mockito.verifyNoMoreInteractions(preparedListener);
  }

  @Test
  public void testDefaultPreparationDelay() {
    assertThat(info.getPreparationDelay())
        .named("preparationDelay").isEqualTo(0);
  }

  @Test
  public void testIsPlaying() {
    EnumSet<State> nonPlayingStates = EnumSet.of(IDLE, INITIALIZED, PREPARED,
        PAUSED, STOPPED, PLAYBACK_COMPLETED);
    for (State state : nonPlayingStates) {
      shadowMediaPlayer.setState(state);
      assertThat(mediaPlayer.isPlaying()).isFalse();
    }
    shadowMediaPlayer.setState(STARTED);
    assertThat(mediaPlayer.isPlaying()).isTrue();
  }

  @Test
  public void testIsPrepared() {
    EnumSet<State> prepStates = EnumSet.of(PREPARED, STARTED, PAUSED,
        PLAYBACK_COMPLETED);

    for (State state : State.values()) {
      shadowMediaPlayer.setState(state);
      if (prepStates.contains(state)) {
        assertThat(shadowMediaPlayer.isPrepared()).isTrue();
      } else {
        assertThat(shadowMediaPlayer.isPrepared()).isFalse();
      }
    }
  }

  @Test
  public void testPlaybackProgress() {
    shadowMediaPlayer.setState(PREPARED);
    // This time offset is just to make sure that it doesn't work by
    // accident because the offsets are calculated relative to 0.
    scheduler.advanceBy(100);

    mediaPlayer.start();
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(0);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(STARTED);

    scheduler.advanceBy(500);
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(500);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(STARTED);

    scheduler.advanceBy(499);
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(999);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(STARTED);
    Mockito.verifyZeroInteractions(completionListener);

    scheduler.advanceBy(1);
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(1000);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
    Mockito.verifyNoMoreInteractions(completionListener);

    scheduler.advanceBy(1);
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(1000);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testStop() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();
    scheduler.advanceBy(300);

    mediaPlayer.stop();
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(300);

    scheduler.advanceBy(400);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(300);
  }

  @Test
  public void testPauseReschedulesCompletionCallback() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();
    scheduler.advanceBy(200);
    mediaPlayer.pause();
    scheduler.advanceBy(800);

    Mockito.verifyZeroInteractions(completionListener);

    mediaPlayer.start();
    scheduler.advanceBy(799);
    Mockito.verifyZeroInteractions(completionListener);

    scheduler.advanceBy(1);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
    Mockito.verifyNoMoreInteractions(completionListener);

    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testPauseUpdatesPosition() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();

    scheduler.advanceBy(200);
    mediaPlayer.pause();
    scheduler.advanceBy(200);

    assertThat(shadowMediaPlayer.getState()).isEqualTo(PAUSED);
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(200);

    mediaPlayer.start();
    scheduler.advanceBy(200);

    assertThat(shadowMediaPlayer.getState()).isEqualTo(STARTED);
    assertThat(shadowMediaPlayer.getCurrentPosition()).isEqualTo(400);
  }

  @Test
  public void testSeekDuringPlaybackReschedulesCompletionCallback() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();

    scheduler.advanceBy(300);
    mediaPlayer.seekTo(400);
    scheduler.advanceBy(599);
    Mockito.verifyZeroInteractions(completionListener);
    scheduler.advanceBy(1);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
    Mockito.verifyNoMoreInteractions(completionListener);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);

    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testSeekDuringPlaybackUpdatesPosition() {
    shadowMediaPlayer.setState(PREPARED);

    // This time offset is just to make sure that it doesn't work by
    // accident because the offsets are calculated relative to 0.
    scheduler.advanceBy(100);

    mediaPlayer.start();

    scheduler.advanceBy(400);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(400);

    mediaPlayer.seekTo(600);
    scheduler.advanceBy(0);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(600);

    scheduler.advanceBy(300);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(900);

    mediaPlayer.seekTo(100);
    scheduler.advanceBy(0);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(100);

    scheduler.advanceBy(900);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(1000);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);

    scheduler.advanceBy(100);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(1000);
  }

  @Config(minSdk = O)
  @Test
  public void testSeekToMode() {
    shadowMediaPlayer.setState(PREPARED);

    // This time offset is just to make sure that it doesn't work by
    // accident because the offsets are calculated relative to 0.
    scheduler.advanceBy(100);

    mediaPlayer.start();

    scheduler.advanceBy(400);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(400);

    mediaPlayer.seekTo(600, MediaPlayer.SEEK_CLOSEST);
    scheduler.advanceBy(0);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(600);
  }

  @Test
  public void testPendingEventsRemovedOnError() {
    Mockito.when(errorListener.onError(mediaPlayer, 2, 3)).thenReturn(true);
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();
    scheduler.advanceBy(200);

    // We should have a pending completion callback.
    assertThat(scheduler.size()).isEqualTo(1);

    shadowMediaPlayer.invokeErrorListener(2, 3);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testAttachAuxEffectStates() {
    testStates(new MethodSpec("attachAuxEffect", 37), EnumSet.of(IDLE, ERROR),
        onErrorTester, null);
  }

  private static final EnumSet<State> emptyStateSet = EnumSet
      .noneOf(State.class);

  @Test
  public void testGetAudioSessionIdStates() {
    testStates("getAudioSessionId", emptyStateSet, onErrorTester, null);
  }

  @Test
  public void testGetCurrentPositionStates() {
    testStates("getCurrentPosition", EnumSet.of(IDLE, ERROR), onErrorTester,
        null);
  }

  @Test
  public void testGetDurationStates() {
    testStates("getDuration", EnumSet.of(IDLE, INITIALIZED, ERROR),
        onErrorTester, null);
  }

  @Test
  public void testGetVideoHeightAndWidthStates() {
    testStates("getVideoHeight", EnumSet.of(IDLE, ERROR), logTester, null);
    testStates("getVideoWidth", EnumSet.of(IDLE, ERROR), logTester, null);
  }

  @Test
  public void testIsLoopingStates() {
    // isLooping is quite unique as it throws ISE when in END state,
    // even though every other state is legal.
    testStates("isLooping", EnumSet.of(END), iseTester, null);
  }

  @Test
  public void testIsPlayingStates() {
    testStates("isPlaying", EnumSet.of(ERROR), onErrorTester, null);
  }

  @Test
  public void testPauseStates() {
    testStates("pause",
        EnumSet.of(IDLE, INITIALIZED, PREPARED, STOPPED, ERROR), onErrorTester,
        PAUSED);
  }

  @Test
  public void testPrepareStates() {
    testStates("prepare",
        EnumSet.of(IDLE, PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED, ERROR),
        PREPARED);
  }

  @Test
  public void testPrepareAsyncStates() {
    testStates("prepareAsync",
        EnumSet.of(IDLE, PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED, ERROR),
        PREPARING);
  }

  @Test
  public void testReleaseStates() {
    testStates("release", emptyStateSet, END);
  }

  @Test
  public void testResetStates() {
    testStates("reset", EnumSet.of(END), IDLE);
  }

  @Test
  public void testSeekToStates() {
    testStates(new MethodSpec("seekTo", 38),
        EnumSet.of(IDLE, INITIALIZED, STOPPED, ERROR), onErrorTester, null);
  }

  @Test
  public void testSetAudioSessionIdStates() {
    testStates(new MethodSpec("setAudioSessionId", 40), EnumSet.of(INITIALIZED,
        PREPARED, STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED, ERROR),
        onErrorTester, null);
  }

  // NOTE: This test diverges from the spec in the MediaPlayer
  // doc, which says that setAudioStreamType() is valid to call
  // from any state other than ERROR. It mentions that
  // unless you call it before prepare it won't be effective.
  // However, by inspection I found that it actually calls onError
  // and moves into the ERROR state unless invoked from IDLE state,
  // so that is what I have emulated.
  @Test
  public void testSetAudioStreamTypeStates() {
    testStates(new MethodSpec("setAudioStreamType", AudioManager.STREAM_MUSIC),
        EnumSet.of(PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED, ERROR),
        onErrorTester, null);
  }

  @Test
  public void testSetLoopingStates() {
    testStates(new MethodSpec("setLooping", true), EnumSet.of(ERROR),
        onErrorTester, null);
  }

  @Test
  public void testSetVolumeStates() {
    testStates(new MethodSpec("setVolume", new Class<?>[] { float.class,
        float.class }, new Object[] { 1.0f, 1.0f }), EnumSet.of(ERROR),
        onErrorTester, null);
  }

  @Test
  public void testSetDataSourceStates() {
    final EnumSet<State> invalidStates = EnumSet.of(INITIALIZED, PREPARED,
        STARTED, PAUSED, PLAYBACK_COMPLETED, STOPPED, ERROR);

    testStates(new MethodSpec("setDataSource", DUMMY_SOURCE), invalidStates, iseTester, INITIALIZED);
  }

  @Test
  public void testStartStates() {
    testStates("start",
        EnumSet.of(IDLE, INITIALIZED, PREPARING, STOPPED, ERROR),
        onErrorTester, STARTED);
  }

  @Test
  public void testStopStates() {
    testStates("stop", EnumSet.of(IDLE, INITIALIZED, ERROR), onErrorTester,
        STOPPED);
  }

  @Test
  public void testCurrentPosition() {
    int[] positions = { 0, 1, 2, 1024 };
    for (int position : positions) {
      shadowMediaPlayer.setCurrentPosition(position);
      assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(position);
    }
  }

  @Test
  public void testInitialAudioSessionIdIsNotZero() {
    assertThat(mediaPlayer.getAudioSessionId()).named("initial audioSessionId")
        .isNotEqualTo(0);
  }

  private Tester onErrorTester = new OnErrorTester(-38, 0);
  private Tester iseTester = new ExceptionTester(IllegalStateException.class);
  private Tester logTester = new LogTester(null);
  private Tester assertTester = new ExceptionTester(AssertionError.class);

  private void testStates(String methodName, EnumSet<State> invalidStates,
      State nextState) {
    testStates(new MethodSpec(methodName), invalidStates, iseTester, nextState);
  }

  public class MethodSpec {
    public Method method;
    // public String method;
    public Class<?>[] argTypes;
    public Object[] args;

    public MethodSpec(String method) {
      this(method, (Class<?>[]) null, (Object[]) null);
    }

    public MethodSpec(String method, Class<?>[] argTypes, Object[] args) {
      try {
        this.method = MediaPlayer.class.getDeclaredMethod(method, argTypes);
        this.args = args;
      } catch (NoSuchMethodException e) {
        throw new AssertionError("Method lookup failed: " + method, e);
      }
    }

    public MethodSpec(String method, int arg) {
      this(method, new Class<?>[] { int.class }, new Object[] { arg });
    }

    public MethodSpec(String method, boolean arg) {
      this(method, new Class<?>[] { boolean.class }, new Object[] { arg });
    }

    public MethodSpec(String method, Class<?> c) {
      this(method, new Class<?>[] { c }, new Object[] { null });
    }

    public MethodSpec(String method, Object o) {
      this(method, new Class<?>[] { o.getClass() }, new Object[] { o });
    }

    public <T> MethodSpec(String method, T o, Class<T> c) {
      this(method, new Class<?>[] { c }, new Object[] { o });
    }

    public void invoke() throws InvocationTargetException {
      try {
        method.invoke(mediaPlayer, args);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }

    @Override public String toString() {
      return method.toString();
    }
  }

  private void testStates(String method, EnumSet<State> invalidStates,
      Tester tester, State next) {
    testStates(new MethodSpec(method), invalidStates, tester, next);
  }

  private void testStates(MethodSpec method, EnumSet<State> invalidStates,
      Tester tester, State next) {
    final EnumSet<State> invalid = EnumSet.copyOf(invalidStates);

    // The documentation specifies that the behavior of calling any
    // function while in the PREPARING state is undefined. I tried
    // to play it safe but reasonable, by looking at whether the PREPARED or
    // INITIALIZED are allowed (ie, the two states that PREPARING
    // sites between). Only if both these states are allowed is
    // PREPARING allowed too, if either PREPARED or INITALIZED is
    // disallowed then so is PREPARING.
    if (invalid.contains(PREPARED) || invalid.contains(INITIALIZED)) {
      invalid.add(PREPARING);
    }
    shadowMediaPlayer.setInvalidStateBehavior(InvalidStateBehavior.SILENT);
    for (State state : State.values()) {
      shadowMediaPlayer.setState(state);
      testMethodSuccess(method, next);      
    }    
    
    shadowMediaPlayer.setInvalidStateBehavior(InvalidStateBehavior.EMULATE);    
    for (State state : invalid) {
      shadowMediaPlayer.setState(state);
      tester.test(method);
    }
    for (State state : EnumSet.complementOf(invalid)) {
      if (state == END) {
        continue;
      }
      shadowMediaPlayer.setState(state);
      testMethodSuccess(method, next);
    }

    // END state: by inspection we determined that if a method
    // doesn't raise any kind of error in any other state then neither
    // will it raise one in the END state; however if it raises errors
    // in other states of any kind then it will throw
    // IllegalArgumentException when in END.
    shadowMediaPlayer.setState(END);
    if (invalid.isEmpty()) {
      testMethodSuccess(method, END);
    } else {
      iseTester.test(method);
    }

    shadowMediaPlayer.setInvalidStateBehavior(InvalidStateBehavior.ASSERT);
    for (State state : invalid) {
      shadowMediaPlayer.setState(state);
      assertTester.test(method);
    }
    for (State state : EnumSet.complementOf(invalid)) {
      if (state == END) {
        continue;
      }
      shadowMediaPlayer.setState(state);
      testMethodSuccess(method, next);
    }
    shadowMediaPlayer.setState(END);
    if (invalid.isEmpty()) {
      testMethodSuccess(method, END);
    } else {
      assertTester.test(method);
    }
  }

  private interface Tester {
    void test(MethodSpec method);
  }

  private class OnErrorTester implements Tester {
    private int what;
    private int extra;

    public OnErrorTester(int what, int extra) {
      this.what = what;
      this.extra = extra;
    }

    @Override
    public void test(MethodSpec method) {
      final State state = shadowMediaPlayer.getState();
      final boolean wasPaused = scheduler.isPaused();
      scheduler.pause();
      try {
        method.invoke();
      } catch (InvocationTargetException e) {
        throw new RuntimeException("Expected <" + method
            + "> to call onError rather than throw <" + e.getTargetException()
            + "> when called from <" + state + ">", e);
      }
      Mockito.verifyZeroInteractions(errorListener);
      final State finalState = shadowMediaPlayer.getState();
      assertThat(finalState).isSameAs(ERROR);
      scheduler.unPause();
      Mockito.verify(errorListener).onError(mediaPlayer, what, extra);
      Mockito.reset(errorListener);
      if (wasPaused) {
        scheduler.pause();
      }
    }
  }

  private class ExceptionTester implements Tester {
    private Class<? extends Throwable> eClass;

    public ExceptionTester(Class<? extends Throwable> eClass) {
      this.eClass = eClass;
    }

    @Override
    @SuppressWarnings("MissingFail")
    public void test(MethodSpec method) {
      final State state = shadowMediaPlayer.getState();
      boolean success = false;
      try {
        method.invoke();
        success = true;
      } catch (InvocationTargetException e) {
        Throwable cause = e.getTargetException();
        assertThat(cause).isInstanceOf(eClass);
        final State finalState = shadowMediaPlayer.getState();
        assertThat(finalState).isSameAs(state);
      }
      assertThat(success).isFalse();
    }
  }

  private class LogTester implements Tester {
    private State next;

    public LogTester(State next) {
      this.next = next;
    }

    @Override
    public void test(MethodSpec method) {
      testMethodSuccess(method, next);
    }
  }

  private void testMethodSuccess(MethodSpec method, State next) {
    final State state = shadowMediaPlayer.getState();
    try {
      method.invoke();
      final State finalState = shadowMediaPlayer.getState();
      if (next == null) {
        assertThat(finalState).isEqualTo(state);
      } else {
        assertThat(finalState).isEqualTo(next);
      }
    } catch (InvocationTargetException e) {
      Throwable cause = e.getTargetException();
          fail("<" + method + "> should not throw exception when in state <"
              + state + ">" + cause);
    }
  }

  private static final State[] seekableStates = { PREPARED, PAUSED,
      PLAYBACK_COMPLETED, STARTED };

  // It is not 100% clear from the docs if seeking to < 0 should
  // invoke an error. I have assumed from the documentation
  // which says "Successful invoke of this method in a valid
  // state does not change the state" that it doesn't invoke an
  // error. Rounding the seek up to 0 seems to be the sensible
  // alternative behavior.
  @Test
  public void testSeekBeforeStart() {
    shadowMediaPlayer.setSeekDelay(-1);
    for (State state : seekableStates) {
      shadowMediaPlayer.setState(state);
      shadowMediaPlayer.setCurrentPosition(500);

      mediaPlayer.seekTo(-1);
      shadowMediaPlayer.invokeSeekCompleteListener();

      assertThat(mediaPlayer.getCurrentPosition()).named(
          "Current postion while " + state).isEqualTo(0);
      assertThat(shadowMediaPlayer.getState()).named("Final state " + state)
          .isEqualTo(state);
    }
  }

  // Similar comments apply to this test as to
  // testSeekBeforeStart().
  @Test
  public void testSeekPastEnd() {
    shadowMediaPlayer.setSeekDelay(-1);
    for (State state : seekableStates) {
      shadowMediaPlayer.setState(state);
      shadowMediaPlayer.setCurrentPosition(500);
      mediaPlayer.seekTo(1001);
      shadowMediaPlayer.invokeSeekCompleteListener();

      assertThat(mediaPlayer.getCurrentPosition()).named(
          "Current postion while " + state).isEqualTo(1000);
      assertThat(shadowMediaPlayer.getState()).named("Final state " + state)
          .isEqualTo(state);
    }
  }

  @Test
  public void testCompletionListener() {
    shadowMediaPlayer.invokeCompletionListener();

    Mockito.verify(completionListener).onCompletion(mediaPlayer);
  }

  @Test
  public void testCompletionWithoutListenerDoesNotThrowException() {
    mediaPlayer.setOnCompletionListener(null);
    shadowMediaPlayer.invokeCompletionListener();

    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testSeekListener() {
    shadowMediaPlayer.invokeSeekCompleteListener();

    Mockito.verify(seekListener).onSeekComplete(mediaPlayer);
  }

  @Test
  public void testSeekWithoutListenerDoesNotThrowException() {
    mediaPlayer.setOnSeekCompleteListener(null);
    shadowMediaPlayer.invokeSeekCompleteListener();

    Mockito.verifyZeroInteractions(seekListener);
  }

  @Test
  public void testSeekDuringPlaybackDelayedCallback() {
    shadowMediaPlayer.setState(PREPARED);
    shadowMediaPlayer.setSeekDelay(100);

    assertThat(shadowMediaPlayer.getSeekDelay()).isEqualTo(100);

    mediaPlayer.start();
    scheduler.advanceBy(200);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(200);
    mediaPlayer.seekTo(450);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(200);

    scheduler.advanceBy(99);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(200);
    Mockito.verifyZeroInteractions(seekListener);

    scheduler.advanceBy(1);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(450);
    Mockito.verify(seekListener).onSeekComplete(mediaPlayer);

    assertThat(scheduler.advanceToLastPostedRunnable()).isTrue();
    Mockito.verifyNoMoreInteractions(seekListener);
  }

  @Test
  public void testSeekWhilePausedDelayedCallback() {
    shadowMediaPlayer.setState(PAUSED);
    shadowMediaPlayer.setSeekDelay(100);

    scheduler.advanceBy(200);
    mediaPlayer.seekTo(450);
    scheduler.advanceBy(99);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(0);
    Mockito.verifyZeroInteractions(seekListener);

    scheduler.advanceBy(1);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(450);
    Mockito.verify(seekListener).onSeekComplete(mediaPlayer);
    // Check that no completion callback or alternative
    // seek callbacks have been scheduled.
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
  }

  @Test
  public void testSeekWhileSeekingWhilePaused() {
    shadowMediaPlayer.setState(PAUSED);
    shadowMediaPlayer.setSeekDelay(100);

    scheduler.advanceBy(200);
    mediaPlayer.seekTo(450);
    scheduler.advanceBy(50);
    mediaPlayer.seekTo(600);
    scheduler.advanceBy(99);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(0);
    Mockito.verifyZeroInteractions(seekListener);

    scheduler.advanceBy(1);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(600);
    Mockito.verify(seekListener).onSeekComplete(mediaPlayer);
    // Check that no completion callback or alternative
    // seek callbacks have been scheduled.
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
  }

  @Test
  public void testSeekWhileSeekingWhilePlaying() {
    shadowMediaPlayer.setState(PREPARED);
    shadowMediaPlayer.setSeekDelay(100);

    final long startTime = scheduler.getCurrentTime();
    mediaPlayer.start();
    scheduler.advanceBy(200);
    mediaPlayer.seekTo(450);
    scheduler.advanceBy(50);
    mediaPlayer.seekTo(600);
    scheduler.advanceBy(99);

    // Not sure of the correct behavior to emulate here, as the MediaPlayer
    // documentation is not detailed enough. There are three possibilities:
    // 1. Playback is paused for the entire time that a seek is in progress.
    // 2. Playback continues normally until the seek is complete.
    // 3. Somewhere between these two extremes - playback continues for
    // a while and then pauses until the seek is complete.
    // I have decided to emulate the first. I don't think that
    // implementations should depend on any of these particular behaviors
    // and consider the behavior indeterminate.
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(200);
    Mockito.verifyZeroInteractions(seekListener);

    scheduler.advanceBy(1);

    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(600);
    Mockito.verify(seekListener).onSeekComplete(mediaPlayer);
    // Check that the completion callback is scheduled properly
    // but no alternative seek callbacks.
    assertThat(scheduler.advanceToLastPostedRunnable()).isTrue();
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
    Mockito.verifyNoMoreInteractions(seekListener);
    assertThat(scheduler.getCurrentTime()).isEqualTo(startTime + 750);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(1000);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);
  }

  @Test
  public void testSimulatenousEventsAllRun() {
    // Simultaneous events should all run even if
    // one of them stops playback.
    MediaEvent e1 = new MediaEvent() {
      @Override
      public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
        smp.doStop();
      }
    };
    MediaEvent e2 = Mockito.mock(MediaEvent.class);
    
    info.scheduleEventAtOffset(100, e1);
    info.scheduleEventAtOffset(100, e2);
    
    shadowMediaPlayer.setState(INITIALIZED);
    shadowMediaPlayer.doStart();
    scheduler.advanceBy(100);
    // Verify that the first event ran
    assertThat(shadowMediaPlayer.isReallyPlaying()).isFalse();
    Mockito.verify(e2).run(mediaPlayer, shadowMediaPlayer);
  }
  
  @Test
  public void testResetCancelsCallbacks() {
    shadowMediaPlayer.setState(STARTED);
    mediaPlayer.seekTo(100);
    MediaEvent e = Mockito.mock(MediaEvent.class);
    shadowMediaPlayer.postEventDelayed(e, 200);
    mediaPlayer.reset();

    assertThat(scheduler.size()).isEqualTo(0);
  }

  @Test
  public void testReleaseCancelsSeekCallback() {
    shadowMediaPlayer.setState(STARTED);
    mediaPlayer.seekTo(100);
    MediaEvent e = Mockito.mock(MediaEvent.class);
    shadowMediaPlayer.postEventDelayed(e, 200);
    mediaPlayer.release();

    assertThat(scheduler.size()).isEqualTo(0);
  }

  @Test
  public void testSeekManualCallback() {
    // Need to put the player into a state where seeking is allowed
    shadowMediaPlayer.setState(STARTED);
    // seekDelay of -1 signifies that OnSeekComplete won't be
    // invoked automatically by the shadow player itself.
    shadowMediaPlayer.setSeekDelay(-1);

    assertThat(shadowMediaPlayer.getPendingSeek()).named("pendingSeek before")
        .isEqualTo(-1);
    int[] positions = { 0, 5, 2, 999 };
    int prevPos = 0;
    for (int position : positions) {
      mediaPlayer.seekTo(position);

      assertThat(shadowMediaPlayer.getPendingSeek()).named("pendingSeek")
          .isEqualTo(position);
      assertThat(mediaPlayer.getCurrentPosition()).named("pendingSeekCurrentPos")
          .isEqualTo(prevPos);

      shadowMediaPlayer.invokeSeekCompleteListener();

      assertThat(shadowMediaPlayer.getPendingSeek()).isEqualTo(-1);
      assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(position);
      prevPos = position;
    }
  }

  @Test
  public void testPreparedListenerCalled() {
    shadowMediaPlayer.invokePreparedListener();
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARED);
    Mockito.verify(preparedListener).onPrepared(mediaPlayer);
  }

  @Test
  public void testPreparedWithoutListenerDoesNotThrowException() {
    mediaPlayer.setOnPreparedListener(null);
    shadowMediaPlayer.invokePreparedListener();

    assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARED);
    Mockito.verifyZeroInteractions(preparedListener);
  }

  @Test
  public void testInfoListenerCalled() {
    shadowMediaPlayer.invokeInfoListener(21, 32);
    Mockito.verify(infoListener).onInfo(mediaPlayer, 21, 32);
  }

  @Test
  public void testInfoWithoutListenerDoesNotThrowException() {
    mediaPlayer.setOnInfoListener(null);
    shadowMediaPlayer.invokeInfoListener(3, 44);

    Mockito.verifyZeroInteractions(infoListener);
  }

  @Test
  public void testErrorListenerCalledNoOnCompleteCalledWhenReturnTrue() {
    Mockito.when(errorListener.onError(mediaPlayer, 112, 221)).thenReturn(true);

    shadowMediaPlayer.invokeErrorListener(112, 221);

    assertThat(shadowMediaPlayer.getState()).isEqualTo(ERROR);
    Mockito.verify(errorListener).onError(mediaPlayer, 112, 221);
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testErrorListenerCalledOnCompleteCalledWhenReturnFalse() {
    Mockito.when(errorListener.onError(mediaPlayer, 0, 0)).thenReturn(false);

    shadowMediaPlayer.invokeErrorListener(321, 11);

    Mockito.verify(errorListener).onError(mediaPlayer, 321, 11);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
  }

  @Test
  public void testErrorCausesOnCompleteCalledWhenNoErrorListener() {
    mediaPlayer.setOnErrorListener(null);

    shadowMediaPlayer.invokeErrorListener(321, 21);

    Mockito.verifyZeroInteractions(errorListener);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
  }

  @Test
  public void testReleaseStopsScheduler() {
    shadowMediaPlayer.doStart();
    mediaPlayer.release();
    assertThat(scheduler.size()).isEqualTo(0);
  }

  @Test
  public void testResetStopsScheduler() {
    shadowMediaPlayer.doStart();
    mediaPlayer.reset();
    assertThat(scheduler.size()).isEqualTo(0);
  }

  @Test
  public void testDoStartStop() {
    assertThat(shadowMediaPlayer.isReallyPlaying()).isFalse();
    scheduler.advanceBy(100);
    shadowMediaPlayer.doStart();
    assertThat(shadowMediaPlayer.isReallyPlaying()).isTrue();
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(0);
    assertThat(shadowMediaPlayer.getState()).isSameAs(IDLE);

    scheduler.advanceBy(100);
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(100);

    shadowMediaPlayer.doStop();
    assertThat(shadowMediaPlayer.isReallyPlaying()).isFalse();
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(100);
    assertThat(shadowMediaPlayer.getState()).isSameAs(IDLE);

    scheduler.advanceBy(50);
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(100);
  }

  @Test
  public void testScheduleErrorAtOffsetWhileNotPlaying() {
    info.scheduleErrorAtOffset(500, 1, 3);
    shadowMediaPlayer.setState(INITIALIZED);
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();

    scheduler.advanceBy(499);
    Mockito.verifyZeroInteractions(errorListener);

    scheduler.advanceBy(1);
    Mockito.verify(errorListener).onError(mediaPlayer, 1, 3);
    assertThat(shadowMediaPlayer.getState()).isSameAs(ERROR);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(500);
  }

  @Test
  public void testScheduleErrorAtOffsetInPast() {
    info.scheduleErrorAtOffset(200, 1, 2);
    shadowMediaPlayer.setState(INITIALIZED);
    shadowMediaPlayer.setCurrentPosition(400);
    shadowMediaPlayer.setState(PAUSED);
    mediaPlayer.start();
    scheduler.unPause();
    Mockito.verifyZeroInteractions(errorListener);
  }

  @Test
  public void testScheduleBufferUnderrunAtOffset() {
    info.scheduleBufferUnderrunAtOffset(100, 50);
    shadowMediaPlayer.setState(INITIALIZED);
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();

    scheduler.advanceBy(99);

    Mockito.verifyZeroInteractions(infoListener);

    scheduler.advanceBy(1);
    Mockito.verify(infoListener).onInfo(mediaPlayer,
        MediaPlayer.MEDIA_INFO_BUFFERING_START, 0);
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(100);
    assertThat(shadowMediaPlayer.isReallyPlaying()).isFalse();

    scheduler.advanceBy(49);
    Mockito.verifyZeroInteractions(infoListener);

    scheduler.advanceBy(1);
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(100);
    Mockito.verify(infoListener).onInfo(mediaPlayer,
        MediaPlayer.MEDIA_INFO_BUFFERING_END, 0);

    scheduler.advanceBy(100);
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(200);
  }

  @Test
  public void testRemoveEventAtOffset() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();

    scheduler.advanceBy(200);

    MediaEvent e = info.scheduleInfoAtOffset(
        500, 1, 3);

    scheduler.advanceBy(299);
    info.removeEventAtOffset(500, e);
    scheduler.advanceToLastPostedRunnable();
    Mockito.verifyZeroInteractions(infoListener);
  }

  @Test
  public void testRemoveEvent() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();

    scheduler.advanceBy(200);

    MediaEvent e = info.scheduleInfoAtOffset(500, 1, 3);

    scheduler.advanceBy(299);
    shadowMediaPlayer.doStop();
    info.removeEvent(e);
    shadowMediaPlayer.doStart();
    scheduler.advanceToLastPostedRunnable();
    Mockito.verifyZeroInteractions(infoListener);
  }

  @Test
  public void testScheduleMultipleRunnables() {
    shadowMediaPlayer.setState(PREPARED);
    scheduler.advanceBy(25);
    mediaPlayer.start();

    scheduler.advanceBy(200);
    assertThat(scheduler.size()).isEqualTo(1);
    shadowMediaPlayer.doStop();
    info.scheduleInfoAtOffset(250, 2, 4);
    shadowMediaPlayer.doStart();
    assertThat(scheduler.size()).isEqualTo(1);

    MediaEvent e1 = Mockito.mock(MediaEvent.class);

    shadowMediaPlayer.doStop();
    info.scheduleEventAtOffset(400, e1);
    shadowMediaPlayer.doStart();

    scheduler.advanceBy(49);
    Mockito.verifyZeroInteractions(infoListener);
    scheduler.advanceBy(1);
    Mockito.verify(infoListener).onInfo(mediaPlayer, 2, 4);
    scheduler.advanceBy(149);
    shadowMediaPlayer.doStop();
    info.scheduleErrorAtOffset(675, 32, 22);
    shadowMediaPlayer.doStart();
    Mockito.verifyZeroInteractions(e1);
    scheduler.advanceBy(1);
    Mockito.verify(e1).run(mediaPlayer, shadowMediaPlayer);

    mediaPlayer.pause();
    assertThat(scheduler.size()).isEqualTo(0);
    scheduler.advanceBy(324);
    MediaEvent e2 = Mockito.mock(MediaEvent.class);
    info.scheduleEventAtOffset(680, e2);
    mediaPlayer.start();
    scheduler.advanceBy(274);
    Mockito.verifyZeroInteractions(errorListener);

    scheduler.advanceBy(1);
    Mockito.verify(errorListener).onError(mediaPlayer, 32, 22);
    assertThat(scheduler.size()).isEqualTo(0);
    assertThat(shadowMediaPlayer.getCurrentPositionRaw()).isEqualTo(675);
    assertThat(shadowMediaPlayer.getState()).isSameAs(ERROR);
    Mockito.verifyZeroInteractions(e2);
  }

  @Test
  public void testSetDataSourceExceptionWithWrongExceptionTypeAsserts() {
    boolean fail = false;
    Map<DataSource,Exception> exceptions = ReflectionHelpers.getStaticField(ShadowMediaPlayer.class, "exceptions");
    DataSource ds = toDataSource("dummy");
    Exception e = new CloneNotSupportedException(); // just a convenient, non-RuntimeException in java.lang
    exceptions.put(ds, e);

    try {
      shadowMediaPlayer.setDataSource(ds);
      fail = true;
    } catch (AssertionError a) {
    } catch (IOException ioe) {
      fail("Got exception <" + ioe + ">; expecting assertion");
    }
    if (fail) {
      fail("setDataSource() should assert with non-IOException,non-RuntimeException");
    }
  }

  @Test
  public void testSetDataSourceCustomExceptionOverridesIllegalState() {
    shadowMediaPlayer.setState(PREPARED);
    ShadowMediaPlayer.addException(toDataSource("dummy"), new IOException());
    try {
      mediaPlayer.setDataSource("dummy");
      fail("Expecting IOException to be thrown");
    } catch (IOException eThrown) {
    } catch (Exception eThrown) {
      fail(eThrown + " was thrown, expecting IOException");
    }
  }

  @Test
  public void testGetSetLooping() {
    assertThat(mediaPlayer.isLooping()).isFalse();
    mediaPlayer.setLooping(true);
    assertThat(mediaPlayer.isLooping()).isTrue();
    mediaPlayer.setLooping(false);
    assertThat(mediaPlayer.isLooping()).isFalse();
  }

  /**
   * If the looping mode was being set to {@code true}
   * {@link MediaPlayer#setLooping(boolean)}, the MediaPlayer object shall
   * remain in the Started state.
   */
  @Test
  public void testSetLoopingCalledWhilePlaying() {
    shadowMediaPlayer.setState(PREPARED);
    mediaPlayer.start();
    scheduler.advanceBy(200);

    mediaPlayer.setLooping(true);
    scheduler.advanceBy(1100);

    Mockito.verifyZeroInteractions(completionListener);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(300);

    mediaPlayer.setLooping(false);
    scheduler.advanceBy(699);
    Mockito.verifyZeroInteractions(completionListener);

    scheduler.advanceBy(1);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
  }

  @Test
  public void testSetLoopingCalledWhileStartable() {
    final State[] startableStates = { PREPARED, PAUSED };
    for (State state : startableStates) {
      shadowMediaPlayer.setCurrentPosition(500);
      shadowMediaPlayer.setState(state);

      mediaPlayer.setLooping(true);
      mediaPlayer.start();

      scheduler.advanceBy(700);
      Mockito.verifyZeroInteractions(completionListener);
      assertThat(mediaPlayer.getCurrentPosition()).named(state.toString())
          .isEqualTo(200);
    }
  }

  /**
   * While in the PlaybackCompleted state, calling start() can restart the
   * playback from the beginning of the audio/video source.
   */
  @Test
  public void testStartAfterPlaybackCompleted() {
    shadowMediaPlayer.setState(PLAYBACK_COMPLETED);
    shadowMediaPlayer.setCurrentPosition(1000);

    mediaPlayer.start();
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(0);
  }
  
  @Test
  public void testResetStaticState() {
    ShadowMediaPlayer.CreateListener createListener = Mockito
        .mock(ShadowMediaPlayer.CreateListener.class);
    ShadowMediaPlayer.setCreateListener(createListener);
    assertThat(ShadowMediaPlayer.createListener)
      .named("createListener")
      .isSameAs(createListener);
    DataSource dummy = toDataSource("stuff");
    IOException e = new IOException();
    addException(dummy, e);
    
    try {
      shadowMediaPlayer.setState(IDLE);
      shadowMediaPlayer.setDataSource(dummy);
      fail("Expected exception thrown");
    } catch (IOException e2) {
      assertThat(e2).named("thrown exception").isSameAs(e);
    }
    // Check that the mediaInfo was cleared
    shadowMediaPlayer.doSetDataSource(defaultSource);
    assertThat(shadowMediaPlayer.getMediaInfo()).named("mediaInfo:before").isNotNull();
    
    ShadowMediaPlayer.resetStaticState();

    // Check that the listener was cleared.
    assertThat(ShadowMediaPlayer.createListener)
      .named("createListener")
      .isNull();

    // Check that the mediaInfo was cleared.
    try {
      shadowMediaPlayer.doSetDataSource(defaultSource);
      fail("Expected exception thrown");
    } catch (IllegalArgumentException ie) {
      // We expect this if the static state has been cleared.
    }

    // Check that the exception was cleared.
    try {
      shadowMediaPlayer.setState(IDLE);
      ShadowMediaPlayer.addMediaInfo(dummy, info);
      shadowMediaPlayer.setDataSource(dummy);
    } catch (IOException e2) {
      fail("Exception was not cleared by resetStaticState() for <" + dummy + ">" + e2);
    }
  }
  
  @Test
  public void setDataSourceException_withRuntimeException() {
    RuntimeException e = new RuntimeException("some dummy message");
    addException(toDataSource("dummy"), e);
    try {
      mediaPlayer.setDataSource("dummy");
      fail("Expected exception thrown");
    } catch (Exception caught) {
      assertThat(caught).isSameAs(e);
      assertThat(e.getStackTrace()[0].getClassName())
         .named("Stack trace should originate in Shadow")
         .isEqualTo(ShadowMediaPlayer.class.getName());
    }
  }

  @Test
  public void setDataSourceException_withIOException() {
    IOException e = new IOException("some dummy message");
    addException(toDataSource("dummy"), e);
    shadowMediaPlayer.setState(IDLE);
    try {
      mediaPlayer.setDataSource("dummy");
      fail("Expected exception thrown");
    } catch (Exception caught) {
      assertThat(caught).isSameAs(e);
      assertThat(e.getStackTrace()[0].getClassName())
         .named("Stack trace should originate in Shadow")
         .isEqualTo(ShadowMediaPlayer.class.getName());
      assertThat(shadowMediaPlayer.getState()).named(
          "State after " + e + " thrown should be unchanged").isSameAs(IDLE);
    }
  }
  
  @Test
  public void setDataSource_forNoDataSource_asserts() {
    try {
      mediaPlayer.setDataSource("some unspecified data source");
      fail("Expected exception thrown");
    } catch (IllegalArgumentException a) {
      assertThat(a.getMessage()).named("assertionMessage")
          .contains("addException");
      assertThat(a.getMessage()).named("assertionMessage")
          .contains("addMediaInfo");
    } catch (Exception e) {
      throw new RuntimeException("Unexpected exception", e);
    }
  }

  @Test
  public void instantiateOnBackgroundThread() throws ExecutionException, InterruptedException {
    ShadowMediaPlayer shadowMediaPlayer =
        Executors.newSingleThreadExecutor()
            .submit(
                () -> {
                  // This thread does not have a prepared looper, so the main looper is used
                  MediaPlayer mediaPlayer = Shadow.newInstanceOf(MediaPlayer.class);
                  return shadowOf(mediaPlayer);
                })
            .get();
    AtomicBoolean ran = new AtomicBoolean(false);
    shadowMediaPlayer.postEvent(
        new MediaEvent() {
          @Override
          public void run(MediaPlayer mp, ShadowMediaPlayer smp) {
            assertThat(Looper.myLooper()).isSameAs(Looper.getMainLooper());
            ran.set(true);
          }
        });
    scheduler.advanceToLastPostedRunnable();
    assertThat(ran.get()).isTrue();
  }
}
