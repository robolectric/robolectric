package org.robolectric.shadows;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import org.assertj.core.api.Assertions;
import org.fest.reflect.method.Invoker;
import org.fest.reflect.method.MethodParameterTypes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;
import org.robolectric.shadows.ShadowMediaPlayer.MediaInfo;
import org.robolectric.shadows.ShadowMediaPlayer.State;
import org.robolectric.util.Scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.shadows.ShadowMediaPlayer.State.*;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowMediaPlayerTest {

  private MediaPlayer mediaPlayer;
  private ShadowMediaPlayer shadowMediaPlayer;
  private MediaPlayer.OnCompletionListener completionListener;
  private MediaPlayer.OnErrorListener errorListener;
  private MediaPlayer.OnPreparedListener preparedListener;
  private MediaPlayer.OnSeekCompleteListener seekListener;
  private Scheduler scheduler;

  @Before
  public void setUp() throws Exception {
    mediaPlayer = Shadow.newInstanceOf(MediaPlayer.class);
    shadowMediaPlayer = Shadows.shadowOf(mediaPlayer);
    shadowMediaPlayer.setDuration(1000);

    completionListener = Mockito.mock(MediaPlayer.OnCompletionListener.class);
    mediaPlayer.setOnCompletionListener(completionListener);

    preparedListener = Mockito.mock(MediaPlayer.OnPreparedListener.class);
    mediaPlayer.setOnPreparedListener(preparedListener);

    errorListener = Mockito.mock(MediaPlayer.OnErrorListener.class);
    mediaPlayer.setOnErrorListener(errorListener);

    seekListener = Mockito.mock(MediaPlayer.OnSeekCompleteListener.class);
    mediaPlayer.setOnSeekCompleteListener(seekListener);

    // Scheduler is used in many of the tests to simulate
    // moving forward in time.
    scheduler = Robolectric.getUiThreadScheduler();
  }

  @Test
  public void testInitialState() {
    assertThat(shadowMediaPlayer.getState()).isEqualTo(IDLE);
  }

  @Test
  public void testCreateListener() {
    ShadowMediaPlayer.CreateListener createListener = 
        Mockito.mock(ShadowMediaPlayer.CreateListener.class);
    ShadowMediaPlayer.setCreateListener(createListener);

    MediaPlayer newPlayer = new MediaPlayer();
    ShadowMediaPlayer shadow = Robolectric.shadowOf(newPlayer);
    
    Mockito.verify(createListener).onCreate(newPlayer, shadow);
    ShadowMediaPlayer.setCreateListener(null);
  }
  
  @Test
  public void testPrepare() throws IOException {
    int[] testDelays = { 0, 10, 100, 1500 };
    scheduler.pause();

    for (int delay : testDelays) {
      final long startTime = scheduler.getCurrentTime();
      shadowMediaPlayer.setState(INITIALIZED);
      shadowMediaPlayer.setPreparationDelay(delay);
      mediaPlayer.prepare();

      assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARED);
      assertThat(scheduler.getCurrentTime()).isEqualTo(startTime + delay);
    }
  }

  @Test
  public void testDataSourceMap() throws IOException {
    Map<String, MediaInfo> map = new HashMap<String, MediaInfo>();
    map.put("clip", new MediaInfo(100, 200));
    shadowMediaPlayer.setDataSourceMap(map);
    shadowMediaPlayer.setDataSource("clip");
    
    assertThat(shadowMediaPlayer.getDurationRaw())
        .as("duration")
        .isEqualTo(100);
    assertThat(shadowMediaPlayer.getPreparationDelay())
      .as("preparationDelay")
      .isEqualTo(200);
  }
  
  @Test
  public void testPrepareAsyncAutoCallback() {
    mediaPlayer.setOnPreparedListener(preparedListener);
    int[] testDelays = { 0, 10, 100, 1500 };
    scheduler.pause();

    for (int delay : testDelays) {
      shadowMediaPlayer.setState(INITIALIZED);
      shadowMediaPlayer.setPreparationDelay(delay);
      final long startTime = scheduler.getCurrentTime();
      mediaPlayer.prepareAsync();

      assertThat(shadowMediaPlayer.getState()).isEqualTo(PREPARING);
      Mockito.verifyZeroInteractions(preparedListener);
      scheduler.advanceToLastPostedRunnable();
      assertThat(scheduler.getCurrentTime()).as("currentTime").isEqualTo(
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

    shadowMediaPlayer.setState(INITIALIZED);
    shadowMediaPlayer.setPreparationDelay(-1);
    final long startTime = scheduler.getCurrentTime();
    mediaPlayer.prepareAsync();

    assertThat(scheduler.getCurrentTime()).as("currentTime").isEqualTo(
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
    assertThat(shadowMediaPlayer.getPreparationDelay()).as("preparationDelay")
        .isEqualTo(0);
  }

  @Test
  public void testIsPlaying() {
    EnumSet<State> nonPlayingStates = EnumSet.of(IDLE, INITIALIZED, PREPARED, PAUSED, STOPPED, PLAYBACK_COMPLETED);
    for (State state : nonPlayingStates) {
      shadowMediaPlayer.setState(state);
      assertThat(mediaPlayer.isPlaying())
        .overridingErrorMessage(
          "In state <%s>, expected isPlaying() to be false", state)
        .isFalse();
    }
    shadowMediaPlayer.setState(STARTED);
    assertThat(mediaPlayer.isPlaying())
      .overridingErrorMessage("In state <STARTED>, expected isPlaying() to be true")
      .isTrue();
  }

  @Test
  public void testIsPrepared() {
    EnumSet<State> prepStates = EnumSet.of(PREPARED, STARTED, PAUSED,
        PLAYBACK_COMPLETED);

    for (State state : State.values()) {
      shadowMediaPlayer.setState(state);
      if (prepStates.contains(state)) {
        assertThat(shadowMediaPlayer.isPrepared()).overridingErrorMessage(
            "In state <%s>, expected isPrepared() to be true", state).isTrue();
      } else {
        assertThat(shadowMediaPlayer.isPrepared()).overridingErrorMessage(
            "In state <%s>, expected isPrepared() to be false", state)
            .isFalse();
      }
    }
  }

  @Test
  public void testPlaybackProgress() {
    scheduler.pause();

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
    scheduler.pause();
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
    scheduler.pause();
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
    scheduler.pause();
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
    scheduler.pause();
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
    scheduler.pause();
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

  @Test
  public void testPendingCompletionRemovedOnError() {
    Mockito.when(errorListener.onError(mediaPlayer, 2, 3)).thenReturn(true);
    shadowMediaPlayer.setState(PREPARED);
    scheduler.pause();
    mediaPlayer.start();
    scheduler.advanceBy(200);

    // We should have a pending completion callback.
    assertThat(scheduler.enqueuedTaskCount()).isEqualTo(1);

    shadowMediaPlayer.invokeErrorListener(2, 3);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testAttachAuxEffectStates() {
    testStates(new MethodSpec("attachAuxEffect", 37),
               EnumSet.of(IDLE, ERROR),
               onErrorTester,
               null);
  }

  private static final EnumSet<State> emptyStateSet = EnumSet
      .noneOf(State.class);

  @Test
  public void testGetAudioSessionIdStates() {
    testStates("getAudioSessionId", emptyStateSet, onErrorTester, null);
  }

  @Test
  public void testGetCurrentPositionStates() {
    testStates("getCurrentPosition",
               EnumSet.of(IDLE, ERROR),
               onErrorTester,
               null);
  }

  @Test
  public void testGetDurationStates() {
    testStates("getDuration", EnumSet.of(IDLE, INITIALIZED, ERROR), onErrorTester, null);
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
        EnumSet.of(IDLE, INITIALIZED, PREPARED, STOPPED, ERROR),
        onErrorTester,
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
    // Must pause the scheduler else it will transition straight
    // into the PREPARED state.
    scheduler.pause();
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
        EnumSet.of(IDLE, INITIALIZED, STOPPED, ERROR),
        onErrorTester,
        null);
  }

  @Test
  public void testSetAudioSessionIdStates() {
    testStates(new MethodSpec("setAudioSessionId", 40),
        EnumSet.of(INITIALIZED, PREPARED, STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED, ERROR),
        onErrorTester,
        null);
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
    testStates(new MethodSpec("setAudioStreamType",
                              AudioManager.STREAM_MUSIC),
        EnumSet.of(PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED, ERROR),
        onErrorTester,
        null);
  }

  @Test
  public void testSetLoopingStates() {
    testStates(new MethodSpec("setLooping", true),
               EnumSet.of(ERROR),
               onErrorTester, null);
  }

  @Test
  public void testSetVolumeStates() {
    testStates(new MethodSpec("setVolume",
                              new Class<?>[] { float.class, float.class },
                              new Object[] {1.0f, 1.0f}),
               EnumSet.of(ERROR),
               onErrorTester, null);
  }

  @Test
  public void testSetDataSourceStates() {
    final EnumSet<State> invalidStates = EnumSet.of(INITIALIZED, PREPARED, STARTED, PAUSED, PLAYBACK_COMPLETED, STOPPED, ERROR);
    final MethodSpec[] methodSpecs = {
        new MethodSpec("setDataSource", "dummyFile"),
        new MethodSpec("setDataSource",
            new Class<?>[] { Context.class, Uri.class },
            new Object[] { null, null } ),
        new MethodSpec("setDataSource",
            new Class<?>[] { Context.class, Uri.class, Map.class },
            new Object[] { null, null, null } ),
        new MethodSpec("setDataSource", FileDescriptor.class),        
        new MethodSpec("setDataSource",
            new Class<?>[] { FileDescriptor.class, long.class, long.class },
            new Object[] { null, 1L, 10L } )
    };

    for (MethodSpec methodSpec : methodSpecs) {
      testStates(methodSpec, invalidStates, iseTester, INITIALIZED);
    }
  }

  @Test
  public void testStartStates() {
    // Must pause the scheduler else it will transition straight
    // into the PLAYBACK_COMPLETED state.
    scheduler.pause();
    testStates("start",
        EnumSet.of(IDLE, INITIALIZED, PREPARING, STOPPED, ERROR),
        onErrorTester,
        STARTED);
  }

  @Test
  public void testStopStates() {
    testStates("stop",
               EnumSet.of(IDLE, INITIALIZED, ERROR),
               onErrorTester,
               STOPPED);
  }

  @Test
  public void testCurrentPosition() {
    int[] positions = { 0, 1, 2, 1024 };
    scheduler.pause();
    for (int position : positions) {
      shadowMediaPlayer.setCurrentPosition(position);
      assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(position);
    }
  }

  @Test
  public void testInitialAudioSessionIdIsNotZero() {
    assertThat(mediaPlayer.getAudioSessionId()).as("initial audioSessionId")
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
//    public String method;
    public Class<?>[] argTypes;
    public Object[] args;
    
    public MethodSpec(String method) {
      this(method, (Class<?>[])null, (Object[])null);
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
      this(method,
           new Class<?>[] { int.class },
           new Object[] { arg });
    }
    public MethodSpec(String method, boolean arg) {
      this(method,
           new Class<?>[] { boolean.class },
           new Object[] { arg });
    }
    public MethodSpec(String method, Class<?> c) {
      this(method,
           new Class<?>[] { c },
           new Object[] { null });
    }
    public MethodSpec(String method, Object o) {
      this(method,
           new Class<?>[] { o.getClass() },
           new Object[] { o });
    }
    public <T> MethodSpec(String method, T o, Class<T> c) {
      this(method,
           new Class<?>[] { c },
           new Object[] { o });
    }

    public void invoke() throws InvocationTargetException {
      try {
        method.invoke(mediaPlayer, args);
      } catch (IllegalAccessException e) {
        throw new AssertionError(e);
      }
    }
    
    public String toString() {
      return method.toString();
    }
  }

  private void testStates(String method,
      EnumSet<State> invalidStates,
      Tester tester,
      State next) {
    testStates(new MethodSpec(method), invalidStates, tester, next);
  }                      
  
  private void testStates(MethodSpec method,
                          EnumSet<State> invalidStates,
                          Tester tester,
                          State next) {
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

    shadowMediaPlayer.setAssertOnError(false);
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
    shadowMediaPlayer.setAssertOnError(true);
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
    public void test(MethodSpec method);
  }

  private class OnErrorTester implements Tester {
    private int what;
    private int extra;
    
    public OnErrorTester(int what, int extra) {
      this.what = what;
      this.extra = extra;
    }
 
    public void test(MethodSpec method) {
      final State state = shadowMediaPlayer.getState();
      final boolean wasPaused = scheduler.isPaused();
      scheduler.pause();
      try {
        method.invoke();
      } catch (InvocationTargetException e) {
        Assertions.fail("Expected <"
            + method + "> to call onError rather than throw <" + e.getTargetException() + "> when called from <" + state + ">", e);
      }
      Mockito.verifyZeroInteractions(errorListener);
      final State finalState = shadowMediaPlayer.getState();
      assertThat(finalState)
        .overridingErrorMessage("Expected state to change to ERROR when <%s> called from state <%s>, was <%s>", method, state, finalState)
        .isSameAs(ERROR);
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
    
    public void test(MethodSpec method) {
      final State state = shadowMediaPlayer.getState();
      boolean success = false;
      try {
        method.invoke();
        success = true;
      } catch (InvocationTargetException e) {
        Throwable cause = e.getTargetException();
        assertThat(cause)
          .overridingErrorMessage("Unexpected exception <%s> thrown when <%s> called from state <%s>, expecting <%s>",
                                cause, method, state, eClass)
          .isInstanceOf(eClass);
        final State finalState = shadowMediaPlayer.getState();
        assertThat(finalState).overridingErrorMessage(
            "Expected player to remain in <%s> state when <%s> called, was <%s>",
            state, method, finalState).isSameAs(state);
      }
      assertThat(success)
        .overridingErrorMessage("No exception thrown, expected <%s> when <%s> called from state <%s>", eClass, method, state)
        .isFalse();
    }
  }
  
  private class LogTester implements Tester {
    private State next;
    
    public LogTester(State next) {
      this.next = next;
    }
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
        assertThat(finalState)
            .overridingErrorMessage(
                "Expected state <%s> to remain unchanged when <%s> called, was <%s>",
                state, method, finalState).isEqualTo(state);
      } else {
        assertThat(finalState).overridingErrorMessage(
            "Expected <%s> to change state from <%s> to <%s>, was <%s>",
            method, state, next, finalState).isEqualTo(next);
      }
    } catch (InvocationTargetException e) {
      Throwable cause = e.getTargetException();
      Assertions.fail("<" + method
          + "> should not throw Exception when in state <"
          + state + ">", cause);
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

      assertThat(mediaPlayer.getCurrentPosition()).as(
          "Current postion while " + state).isEqualTo(0);
      assertThat(shadowMediaPlayer.getState()).as("Final state " + state)
          .isEqualTo(state);
    }
  }

  // Similar comments apply to this test as to
  // testSeekBeforeStart().
  @Test
  public void testSeekPastEnd() {
    scheduler.pause();
    shadowMediaPlayer.setSeekDelay(-1);
    for (State state : seekableStates) {
      shadowMediaPlayer.setState(state);
      shadowMediaPlayer.setCurrentPosition(500);
      mediaPlayer.seekTo(1001);
      shadowMediaPlayer.invokeSeekCompleteListener();

      assertThat(mediaPlayer.getCurrentPosition()).as(
          "Current postion while " + state).isEqualTo(1000);
      assertThat(shadowMediaPlayer.getState()).as("Final state " + state)
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
    scheduler.pause();
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
    scheduler.pause();
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
    scheduler.pause();
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
    scheduler.pause();
    shadowMediaPlayer.setSeekDelay(100);

    mediaPlayer.start();
    scheduler.advanceBy(200);
    mediaPlayer.seekTo(450);
    scheduler.advanceBy(50);
    mediaPlayer.seekTo(600);
    scheduler.advanceBy(99);

    // Not sure of the correct behaviour to emulate here, as the MediaPlayer
    // documentation is not detailed enough. There are three possibilities:
    // 1. Playback is paused for the entire time that a seek is in progress.
    // 2. Playback continues normally until the seek is complete.
    // 3. Somewhere between these two extremes - playback continues for
    // a while and then pauses until the seek is complete.
    // I have decided to emulate the first. I don't think that
    // implementations should depend on any of these particular behaviours
    // and consider the behaviour indeterminate.
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
    assertThat(scheduler.getCurrentTime()).isEqualTo(750);
    assertThat(mediaPlayer.getCurrentPosition()).isEqualTo(1000);
    assertThat(shadowMediaPlayer.getState()).isEqualTo(PLAYBACK_COMPLETED);
  }

  @Test
  public void testSeekManualCallback() {
    // Need to put the player into a state where seeking is allowed
    shadowMediaPlayer.setState(STARTED);
    // seekDelay of -1 signifies that OnSeekComplete won't be
    // invoked automatically by the shadow player itself.
    shadowMediaPlayer.setSeekDelay(-1);

    assertThat(shadowMediaPlayer.getPendingSeek()).as("pendingSeek before")
        .isEqualTo(-1);
    int[] positions = { 0, 5, 2, 999 };
    int prevPos = 0;
    for (int position : positions) {
      mediaPlayer.seekTo(position);

      assertThat(shadowMediaPlayer.getPendingSeek()).as("pendingSeek")
          .isEqualTo(position);
      assertThat(mediaPlayer.getCurrentPosition()).as("pendingSeekCurrentPos")
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
  public void testErrorListenerCalledNoOnCompleteCalledWhenReturnTrue() {
    Mockito.when(errorListener.onError(mediaPlayer, 0, 0)).thenReturn(true);

    shadowMediaPlayer.invokeErrorListener(0, 0);

    assertThat(shadowMediaPlayer.getState()).isEqualTo(ERROR);
    Mockito.verify(errorListener).onError(mediaPlayer, 0, 0);
    Mockito.verifyZeroInteractions(completionListener);
  }

  @Test
  public void testErrorListenerCalledOnCompleteCalledWhenReturnFalse() {
    Mockito.when(errorListener.onError(mediaPlayer, 0, 0)).thenReturn(false);

    shadowMediaPlayer.invokeErrorListener(0, 0);

    Mockito.verify(errorListener).onError(mediaPlayer, 0, 0);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
  }

  @Test
  public void testErrorCausesOnCompleteCalledWhenNoErrorListener() {
    mediaPlayer.setOnErrorListener(null);

    shadowMediaPlayer.invokeErrorListener(0, 0);

    Mockito.verifyZeroInteractions(errorListener);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
  }
  
  @Test
  public void testScheduleErrorAtOffsetWhileNotPlaying() {
    shadowMediaPlayer.scheduleErrorAtOffset(500, 1, 3);
    shadowMediaPlayer.setState(PREPARED);
    scheduler.pause();
    mediaPlayer.start();
    
    scheduler.advanceBy(499);
    Mockito.verifyZeroInteractions(errorListener);

    scheduler.advanceBy(1);
    Mockito.verify(errorListener).onError(mediaPlayer, 1, 3);
    assertThat(shadowMediaPlayer.getState()).isSameAs(ERROR);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
  }

  @Test
  public void testScheduleErrorAtOffsetWhilePlaying() {
    shadowMediaPlayer.setState(PREPARED);
    scheduler.pause();
    mediaPlayer.start();

    scheduler.advanceBy(200);
    
    shadowMediaPlayer.scheduleErrorAtOffset(500, 1, 3);

    scheduler.advanceBy(299);
    Mockito.verifyZeroInteractions(errorListener);

    scheduler.advanceBy(1);
    Mockito.verify(errorListener).onError(mediaPlayer, 1, 3);
    Mockito.verify(completionListener).onCompletion(mediaPlayer);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
  }
  
  @Test
  public void testRescheduleErrorAtOffsetWhileNotPlaying() {
    shadowMediaPlayer.setState(PREPARED);
    shadowMediaPlayer.scheduleErrorAtOffset(300, 2, 3);
    scheduler.pause();
    shadowMediaPlayer.scheduleErrorAtOffset(400, 3, 4);
    mediaPlayer.start();
    scheduler.advanceBy(300);
    
    Mockito.verifyZeroInteractions(errorListener);

    scheduler.advanceBy(100);
    
    Mockito.verify(errorListener).onError(mediaPlayer, 3, 4);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
  }
  
  @Test
  public void testRescheduleErrorAtOffsetWhilePlaying() {
    shadowMediaPlayer.setState(PREPARED);
    shadowMediaPlayer.scheduleErrorAtOffset(300, 2, 3);
    scheduler.pause();
    mediaPlayer.start();
    scheduler.advanceBy(200);
    shadowMediaPlayer.scheduleErrorAtOffset(400, 3, 4);
    scheduler.advanceBy(100);
    
    Mockito.verifyZeroInteractions(errorListener);

    scheduler.advanceBy(100);
    
    Mockito.verify(errorListener).onError(mediaPlayer, 3, 4);
    assertThat(scheduler.advanceToLastPostedRunnable()).isFalse();
  }
  
  @Test
  public void testScheduleErrorAtOffsetInPast() {
    scheduler.pause();
    shadowMediaPlayer.setCurrentPosition(400);
    shadowMediaPlayer.setState(PAUSED);
    shadowMediaPlayer.scheduleErrorAtOffset(200,  1,  2);
    shadowMediaPlayer.start();
    scheduler.unPause();
    Mockito.verifyZeroInteractions(errorListener);
  }

  @Test
  public void testSetSetDataSourceExceptionWithWrongExceptionTypeAsserts() {
    boolean fail = false;
    try {
      shadowMediaPlayer.setSetDataSourceException(new CloneNotSupportedException());
      fail = true;
    }
    catch (AssertionError e) {
    }
    assertThat(fail)
      .overridingErrorMessage("setSetDataSourceException() should assert with non-IOException,non-RuntimeException")
      .isFalse();
  }
  
  @Test
  public void testSetDataSourceThrowsAllowedException() {
    Exception[] exceptions = {
      new IOException(),
      new SecurityException(),
      new IllegalArgumentException()
    };
    for (Exception e : exceptions) {
      shadowMediaPlayer.setSetDataSourceException(e);

      shadowMediaPlayer.setState(IDLE);
      try {
        mediaPlayer.setDataSource((FileDescriptor)null);
        Assertions.fail("Expecting " + e + " to be thrown");
      } catch (Exception eThrown) {
        assertThat(eThrown).isSameAs(e);
      }
      assertThat(shadowMediaPlayer.getState())
        .as("State shouldn't change when " + e + " thrown")
        .isSameAs(IDLE);

      // Test all three flavors of setDataSource()
      shadowMediaPlayer.setState(IDLE);
      try {
        mediaPlayer.setDataSource(null, null, null);
        Assertions.fail("Expecting " + e + " to be thrown");
      } catch (Exception eThrown) {
        assertThat(eThrown).isSameAs(e);
      }
      assertThat(shadowMediaPlayer.getState())
        .as("State shouldn't change when " + e + " thrown")
        .isSameAs(IDLE);

      shadowMediaPlayer.setState(IDLE);
      try {
        mediaPlayer.setDataSource((String)null);
        Assertions.fail("Expecting " + e + " to be thrown");
      } catch (Exception eThrown) {
        assertThat(eThrown).isSameAs(e);
      }
      assertThat(shadowMediaPlayer.getState())
        .as("State shouldn't change when " + e + " thrown")
        .isSameAs(IDLE);
    } 
  }
  
  @Test
  public void testSetDataSourceCustomeExceptionOveridesIllegalState() {
    shadowMediaPlayer.setState(PREPARED);
    shadowMediaPlayer.setSetDataSourceException(new IOException());
    try {
      mediaPlayer.setDataSource((String)null);
      Assertions.fail("Expecting IOException to be thrown");
    } catch (IOException eThrown) {
    } catch (Exception eThrown) {
      Assertions.fail(eThrown + " was thrown, expecting IOException");
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
   * If the looping mode was being set to <code>true</code> {@link MediaPlayer#setLooping(boolean)}, the
   * MediaPlayer object shall remain in the Started state.
   */
  @Test
  public void testSetLoopingCalledWhilePlaying() {
    shadowMediaPlayer.setState(PREPARED);
    scheduler.pause();
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
    final State[] startableStates = {PREPARED, PAUSED};
    for (State state : startableStates) {
      shadowMediaPlayer.setCurrentPosition(500);
      shadowMediaPlayer.setState(state);
      scheduler.pause();

      mediaPlayer.setLooping(true);
      mediaPlayer.start();

      scheduler.advanceBy(700);
      Mockito.verifyZeroInteractions(completionListener);
      assertThat(mediaPlayer.getCurrentPosition()).as(state.toString()).isEqualTo(200);
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
}
