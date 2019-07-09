package org.robolectric.android.util.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link InlineExecutorService}
 */
@RunWith(JUnit4.class)
public class InlineExecutorServiceTest {
  private List<String> executedTasksRecord;
  private InlineExecutorService executorService;

  @Before
  public void setUp() throws Exception {
    executedTasksRecord = new ArrayList<>();
    executorService = new InlineExecutorService();
  }

  @Test
  public void executionRunsInBackgroundThread() {
    final Thread testThread = Thread.currentThread();
    executorService.execute(
        new Runnable() {
          @Override
          public void run() {
            assertThat(Thread.currentThread()).isNotSameInstanceAs(testThread);
            executedTasksRecord.add("task ran");
          }
        });
    assertThat(executedTasksRecord).containsExactly("task ran");
  }

  @Test
  public void submit() throws Exception {
    Runnable runnable = () -> executedTasksRecord.add("background event ran");
    Future<String> future = executorService.submit(runnable, "foo");

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
  public void whenAwaitingTerminationAfterShutdown_TrueIsReturned() throws InterruptedException {
    executorService.shutdown();

    assertThat(executorService.awaitTermination(0, TimeUnit.MILLISECONDS)).isTrue();
  }

  @Test
  public void exceptionsPropagated() {
    Callable<Void> throwingCallable = new Callable<Void>() {

      @Override
      public Void call() throws Exception {
        throw new IllegalStateException("I failed");
      }
    };
    try {
      executorService.submit(throwingCallable);
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

    assertThat(executedTasksRecord).containsExactly("first", "second", "third").inOrder();
  }
}
