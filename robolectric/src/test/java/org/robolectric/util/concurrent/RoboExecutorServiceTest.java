package org.robolectric.util.concurrent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.Scheduler;
import org.robolectric.util.Transcript;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RoboExecutorServiceTest {
  private final Transcript transcript = new Transcript();
  private final RoboExecutorService executorService = new RoboExecutorService();
  private final Scheduler backgroundScheduler = Robolectric.getBackgroundThreadScheduler();
  private Runnable runnable;

  @Before
  public void setUp() throws Exception {
    backgroundScheduler.pause();
    runnable = new Runnable() {
      @Override
      public void run() {
        transcript.add("background event ran");
      }
    };
  }

  @Test
  public void execute_shouldRunStuffOnBackgroundThread() throws Exception {
    executorService.execute(runnable);

    transcript.assertNoEventsSoFar();

    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("background event ran");
  }

  @Test
  public void submitRunnable_shouldRunStuffOnBackgroundThread() throws Exception {
    Future<String> future = executorService.submit(runnable, "foo");

    transcript.assertNoEventsSoFar();
    assertThat(future.isDone()).isFalse();

    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("background event ran");
    assertThat(future.isDone()).isTrue();

    assertThat(future.get()).isEqualTo("foo");
  }

  @Test
  public void submitCallable_shouldRunStuffOnBackgroundThread() throws Exception {
    Future<String> future = executorService.submit(new Callable<String>() {
      @Override
      public String call() throws Exception {
        runnable.run();
        return "foo";
      }
    });

    transcript.assertNoEventsSoFar();
    assertThat(future.isDone()).isFalse();

    ShadowApplication.runBackgroundTasks();
    transcript.assertEventsSoFar("background event ran");
    assertThat(future.isDone()).isTrue();

    assertThat(future.get()).isEqualTo("foo");
  }
}