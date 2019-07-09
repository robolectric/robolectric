package org.robolectric.android.util.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link PausedExecutorService}
 */
@RunWith(JUnit4.class)
public class PausedExecutorServiceTest {
  private List<String> executedTasksRecord;
  private PausedExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    executedTasksRecord = new ArrayList<>();
    executorService = new PausedExecutorService();
  }

  @Test
  public void executionRunsInBackgroundThread() throws ExecutionException, InterruptedException {
    final Thread testThread = Thread.currentThread();
    executorService.execute(
        () -> {
          assertThat(Thread.currentThread()).isNotSameInstanceAs(testThread);
          executedTasksRecord.add("task ran");
        });
    executorService.runAll();
    assertThat(executedTasksRecord).containsExactly("task ran");
  }

  @Test
  public void runAll() throws Exception {
    executorService.execute(() -> executedTasksRecord.add("background event ran"));

    assertThat(executedTasksRecord).isEmpty();

    executorService.runAll();
    assertThat(executedTasksRecord).containsExactly("background event ran");
  }

  @Test
  public void runAll_inOrder() throws Exception {
    executorService.execute(() -> executedTasksRecord.add("first"));
    executorService.execute(() -> executedTasksRecord.add("second"));
    assertThat(executedTasksRecord).isEmpty();

    executorService.runAll();
    assertThat(executedTasksRecord).containsExactly("first", "second").inOrder();
  }

  @Test
  public void runNext() throws Exception {
    executorService.execute(() -> executedTasksRecord.add("first"));
    executorService.execute(() -> executedTasksRecord.add("second"));
    assertThat(executedTasksRecord).isEmpty();

    assertThat(executorService.runNext()).isTrue();
    assertThat(executedTasksRecord).containsExactly("first").inOrder();
    assertThat(executorService.runNext()).isTrue();
    assertThat(executedTasksRecord).containsExactly("first", "second").inOrder();
    assertThat(executorService.runNext()).isFalse();
  }

  @Test
  public void runAll_clearsQueuedTasks() throws Exception {
    executorService.execute(() -> executedTasksRecord.add("background event ran"));

    assertThat(executedTasksRecord).isEmpty();

    executorService.runAll();
    assertThat(executedTasksRecord).containsExactly("background event ran");

    executedTasksRecord.clear();

    executorService.runAll();
    assertThat(executedTasksRecord).isEmpty();
  }

  @Test
  public void submit() throws Exception {
    Runnable runnable = () -> executedTasksRecord.add("background event ran");
    Future<String> future = executorService.submit(runnable, "foo");

    executorService.runAll();
    assertThat(executedTasksRecord).containsExactly("background event ran");
    assertThat(future.isDone()).isTrue();
    assertThat(future.get()).isEqualTo("foo");
  }

  @Test
  public void submitCallable() throws Exception {
    Runnable runnable = () -> executedTasksRecord.add("background event ran");
    Future<String> future = executorService.submit(() -> {
      runnable.run();
      return "foo";
    });
    executorService.runAll();

    assertThat(executedTasksRecord).containsExactly("background event ran");
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
  public void whenShutdownBeforeSubmittedTasksAreExecuted_TaskIsNotInTranscript()
      throws ExecutionException, InterruptedException {
    executorService.execute(() -> executedTasksRecord.add("background event ran"));

    executorService.shutdown();
    executorService.runAll();

    assertThat(executedTasksRecord).isEmpty();
  }

  @Test
  public void whenShutdownNow_ReturnedListContainsOneRunnable()
      throws ExecutionException, InterruptedException {
    executorService.execute(() -> executedTasksRecord.add("background event ran"));

    List<Runnable> notExecutedRunnables = executorService.shutdownNow();
    executorService.runAll();

    assertThat(executedTasksRecord).isEmpty();
    assertThat(notExecutedRunnables).hasSize(1);
  }

  @Test
  public void whenAwaitingTerminationAfterShutdown_TrueIsReturned() throws InterruptedException {
    executorService.shutdown();

    assertThat(executorService.awaitTermination(0, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  public void whenAwaitingTermination_AllTasksAreNotRun() throws Exception {
    executorService.execute(() -> executedTasksRecord.add("background event ran"));

    assertThat(executorService.awaitTermination(500, TimeUnit.MILLISECONDS)).isFalse();
    assertThat(executedTasksRecord).isEmpty();
  }

  @Test
  @SuppressWarnings("FutureReturnValueIgnored")
  public void exceptionsPropagated() throws ExecutionException, InterruptedException {
    Callable<Void> throwingCallable = () -> {
      throw new IllegalStateException("I failed");
    };
    try {
      executorService.submit(throwingCallable);
      executorService.runAll();
      fail("did not propagate exception");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void postingTasks() throws Exception {
    Runnable postingRunnable = new Runnable() {
      @Override
      public void run() {
        executedTasksRecord.add("first");
        executorService.execute(() -> executedTasksRecord.add("third"));
        executedTasksRecord.add("second");
      }
    };
    executorService.execute(postingRunnable);
    executorService.runAll();

    assertThat(executedTasksRecord).containsExactly("first", "second", "third").inOrder();
  }
}
