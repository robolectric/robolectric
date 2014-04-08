package org.robolectric.shadows;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Map;

import android.content.Context;
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
    for (State state : State.values()) {
      shadowMediaPlayer.setState(state);
      boolean playing;
      try {
        playing = shadowMediaPlayer.isPlaying();
        final State nextState = shadowMediaPlayer.getState();
        assertThat(nextState)
            .overridingErrorMessage(
                "Expected state to remain unchanged <%s> when <isPlaying()> called, was <%s>",
                state, nextState).isEqualTo(state);

        if (state == STARTED) {
          assertThat(playing).overridingErrorMessage(
              "In state <%s>, expected isPlaying() to be true", state).isTrue();
        } else if (state == ERROR || state == END) {
          Assertions
              .fail("Expected IllegalStateException to be thrown when <isPlaying()> called from state <"
                  + state + ">");
        } else {
          assertThat(shadowMediaPlayer.isPlaying()).overridingErrorMessage(
              "In state <%s>, expected isPlaying() to be false", state)
              .isFalse();
        }
      } catch (IllegalStateException e) {
        assertThat(state)
            .overridingErrorMessage(
                "<isPlaying()> should not throw IllegalStateException when in state <%s>",
                state).isIn(ERROR, END);
        final State nextState = shadowMediaPlayer.getState();
        if (state == END) {
          assertThat(nextState)
            .overridingErrorMessage(
              "Expected <isPlaying()> to leave state in <END>, was <%s>",
              nextState).isSameAs(END);
        }
        else {
          assertThat(nextState)
            .overridingErrorMessage(
                "Expected <isPlaying()> to change/leave state to <ERROR>, was <%s>",
                nextState).isSameAs(ERROR);
        }
      }
    }
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
    testStates("attachAuxEffect(int)", method("attachAuxEffect")
        .withParameterTypes(int.class), EnumSet.of(IDLE, ERROR), 37);
  }

  private static final EnumSet<State> emptyStateSet = EnumSet
      .noneOf(State.class);

  @Test
  public void testGetAudioSessionIdStates() {
    testStates("getAudioSessionId", emptyStateSet);
  }

  @Test
  public void testGetCurrentPositionStates() {
    testStates("getCurrentPosition", EnumSet.of(ERROR));
  }

  @Test
  public void testGetDurationStates() {
    testStates("getDuration", EnumSet.of(IDLE, INITIALIZED, ERROR));
  }

  @Test
  public void testGetVideoHeightAndWidth() {
    testStates("getVideoHeight", EnumSet.of(ERROR));
    testStates("getVideoWidth", EnumSet.of(ERROR));
  }

  @Test
  public void testPauseStates() {
    testStates("pause",
        EnumSet.of(IDLE, INITIALIZED, PREPARED, STOPPED, ERROR), PAUSED);
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
    testStates("reset", emptyStateSet, IDLE);
  }

  @Test
  public void testSeekToStates() {
    testStates("seekTo()", method("seekTo").withParameterTypes(int.class),
        EnumSet.of(IDLE, INITIALIZED, STOPPED, ERROR), 38);
  }

  @Test
  public void testSetAudioSessionIdStates() {
    testStates("setAudioSessionId()", method("setAudioSessionId")
        .withParameterTypes(int.class), EnumSet.of(INITIALIZED, PREPARED,
        STARTED, PAUSED, STOPPED, PLAYBACK_COMPLETED, ERROR), 38);
  }

  @Test
  public void testSetLoopingStates() {
    testStates("setLooping()", method("setLooping")
        .withParameterTypes(boolean.class), EnumSet.of(ERROR), true);
  }

  @Test
  public void testSetDataSourceStates() {
    final EnumSet<State> invalidStates = EnumSet.complementOf(EnumSet.of(IDLE));
    testStatesWithNext("setDataSource(String)", method("setDataSource")
        .withParameterTypes(String.class), invalidStates, INITIALIZED,
        "dummyFile");
    testStatesWithNext("setDataSource(Context,Uri)", method("setDataSource")
        .withParameterTypes(Context.class, Uri.class), invalidStates,
        INITIALIZED, null, null);
    testStatesWithNext(
        "setDataSource(Context,Uri,Map)",
        method("setDataSource").withParameterTypes(Context.class, Uri.class,
            Map.class), invalidStates, INITIALIZED, null, null, null);
    testStatesWithNext("setDataSource(FileDescriptor)", method("setDataSource")
        .withParameterTypes(FileDescriptor.class), invalidStates, INITIALIZED,
        (FileDescriptor) null);
    testStatesWithNext(
        "setDataSource(FileDescriptor,long,long)",
        method("setDataSource").withParameterTypes(FileDescriptor.class,
            long.class, long.class), invalidStates, INITIALIZED, null, 1L, 10L);
  }

  @Test
  public void testStartStates() {
    // Must pause the scheduler else it will transition straight
    // into the PLAYBACK_COMPLETED state.
    scheduler.pause();
    testStates("start",
        EnumSet.of(IDLE, INITIALIZED, PREPARING, STOPPED, ERROR), STARTED);
  }

  @Test
  public void testStopStates() {
    testStates("stop", EnumSet.of(IDLE, INITIALIZED, ERROR), STOPPED);
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

  private void testStates(String methodName, MethodParameterTypes<?> params,
      EnumSet<State> invalidStates, Object... args) {
    testStates(methodName, params.in(mediaPlayer), invalidStates, null, args);
  }

  private void testStatesWithNext(String methodName,
      MethodParameterTypes<?> params, EnumSet<State> invalidStates,
      State nextState, Object... args) {
    testStates(methodName, params.in(mediaPlayer), invalidStates, nextState,
        args);
  }

  private void testStates(String methodName, EnumSet<State> invalidStates) {
    testStates(methodName + "()", method(methodName).in(mediaPlayer),
        invalidStates, null);
  }

  private void testStates(String methodName, EnumSet<State> invalidStates,
      State nextState) {
    testStates(methodName + "()", method(methodName).in(mediaPlayer),
        invalidStates, nextState);
  }

  private void testStates(String methodName, Invoker<?> invoker,
      EnumSet<State> invalidStates, State next, Object... args) {
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
    invalid.add(END);

    for (State state : State.values()) {
      shadowMediaPlayer.setState(state);
      if (invalid.contains(state)) {
        try {
          invoker.invoke(args);
          Assertions.fail("Expected IllegalStateException to be thrown when <"
              + methodName + "> called from state <" + state + ">");
        } catch (IllegalStateException e) {
          final State finalState = shadowMediaPlayer.getState();
          if (state == END) {
            assertThat(finalState).overridingErrorMessage(
              "Expected player to remain in END state when <%s> called, was <%s>",
              methodName, finalState).isSameAs(END);
          } else {
            assertThat(finalState).overridingErrorMessage(
              "Expected ERROR state when <%s> called in state <%s>, was <%s>",
              methodName, state, finalState).isSameAs(ERROR);
          }
        }
      } else {
        try {
          invoker.invoke(args);
          final State finalState = shadowMediaPlayer.getState();
          if (next == null) {
            assertThat(finalState)
                .overridingErrorMessage(
                    "Expected state <%s> to remain unchanged when <%s> called, was <%s>",
                    state, methodName, finalState).isEqualTo(state);
          } else {
            assertThat(finalState).overridingErrorMessage(
                "Expected <%s> to change state from <%s> to <%s>, was <%s>",
                methodName, state, next, finalState).isEqualTo(next);
          }
        } catch (IllegalStateException e) {
          Assertions.fail("<" + methodName
              + "> should not throw IllegalStateException when in state <"
              + state + ">", e);
        }
      }
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
      assertThat(shadowMediaPlayer.getState()).as(e.toString()).isSameAs(ERROR);

      // Test all three flavors of setDataSource()
      shadowMediaPlayer.setState(IDLE);
      try {
        mediaPlayer.setDataSource(null, null, null);
        Assertions.fail("Expecting " + e + " to be thrown");
      } catch (Exception eThrown) {
        assertThat(eThrown).isSameAs(e);
      }
      assertThat(shadowMediaPlayer.getState()).as(e.toString()).isSameAs(ERROR);

      shadowMediaPlayer.setState(IDLE);
      try {
        mediaPlayer.setDataSource((String)null);
        Assertions.fail("Expecting " + e + " to be thrown");
      } catch (Exception eThrown) {
        assertThat(eThrown).isSameAs(e);
      }
      assertThat(shadowMediaPlayer.getState()).as(e.toString()).isSameAs(ERROR);
    } 
  }
  
  @Test
  public void testSetDataSourceIllegalStateOverridesCustomException() {
    shadowMediaPlayer.setState(PREPARED);
    shadowMediaPlayer.setSetDataSourceException(new IOException());
    try {
      mediaPlayer.setDataSource((String)null);
      Assertions.fail("Expecting IllegalStateException to be thrown");
    } catch (IllegalStateException eThrown) {
    } catch (Exception eThrown) {
      Assertions.fail(eThrown + " was thrown, expecting IllegalStateException");
    }
    assertThat(shadowMediaPlayer.getState()).isSameAs(ERROR);
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
