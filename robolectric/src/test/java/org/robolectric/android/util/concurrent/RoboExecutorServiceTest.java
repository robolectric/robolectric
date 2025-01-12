package org.robolectric.android.util.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
@LooperMode(LEGACY)
public class RoboExecutorServiceTest {
  private List<String> transcript;
  private RoboExecutorService executorService;
  private Runnable runnable;

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
    executorService = new RoboExecutorService();

    Scheduler backgroundScheduler = Robolectric.getBackgroundThreadScheduler();
    backgroundScheduler.pause();

    runnable = () -> transcript.add("background event ran");
  }

  @Test
  public void execute_shouldRunStuffOnBackgroundThread() {
    executorService.execute(runnable);

    assertThat(transcript).isEmpty();

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).containsExactly("background event ran");
  }

  @Test
  public void submitRunnable_shouldRunStuffOnBackgroundThread() throws Exception {
    Future<String> future = executorService.submit(runnable, "foo");

    assertThat(transcript).isEmpty();
    assertThat(future.isDone()).isFalse();

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).containsExactly("background event ran");
    assertThat(future.isDone()).isTrue();

    assertThat(future.get()).isEqualTo("foo");
  }

  @Test
  public void submitCallable_shouldRunStuffOnBackgroundThread() throws Exception {
    Future<String> future =
        executorService.submit(
            () -> {
              runnable.run();
              return "foo";
            });

    assertThat(transcript).isEmpty();
    assertThat(future.isDone()).isFalse();

    ShadowApplication.runBackgroundTasks();
    assertThat(transcript).containsExactly("background event ran");
    assertThat(future.isDone()).isTrue();

    assertThat(future.get()).isEqualTo("foo");
  }

  @Test
  public void byDefault_IsNotShutdown() {
    assertThat(executorService.isShutdown()).isFalse();
  }

  @Test
  public void byDefault_IsNotTerminated() {
    assertThat(executorService.isTerminated()).isFalse();
  }

  @Test
  public void whenShutdownBeforeSubmittedTasksAreExecuted_TaskIsNotInTranscript() {
    executorService.execute(runnable);

    executorService.shutdown();
    ShadowApplication.runBackgroundTasks();

    assertThat(transcript).isEmpty();
  }

  @Test
  public void whenShutdownNow_ReturnedListContainsOneRunnable() {
    executorService.execute(runnable);

    List<Runnable> notExecutedRunnables = executorService.shutdownNow();
    ShadowApplication.runBackgroundTasks();

    assertThat(transcript).isEmpty();
    assertThat(notExecutedRunnables).hasSize(1);
  }

  @Test(timeout = 500)
  public void whenGettingFutureValue_FutureRunnableIsExecuted() throws Exception {
    Future<String> future = executorService.submit(runnable, "foo");

    assertThat(future.get()).isEqualTo("foo");
    assertThat(future.isDone()).isTrue();
  }

  @Test
  public void whenAwaitingTerminationAfterShutdown_TrueIsReturned() throws InterruptedException {
    executorService.shutdown();

    assertThat(executorService.awaitTermination(0, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  public void whenAwaitingTermination_AllTasksAreRunByDefault() throws Exception {
    executorService.execute(runnable);

    assertThat(executorService.awaitTermination(500, TimeUnit.MILLISECONDS)).isFalse();
    assertThat(transcript).isEmpty();
  }
}
