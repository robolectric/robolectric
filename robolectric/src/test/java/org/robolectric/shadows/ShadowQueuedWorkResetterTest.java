package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.QueuedWork;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowQueuedWork.QueuedWorkReflector;

/** A specialized test for verifying that QueuedWork state is cleared properly between tests. */
@RunWith(JUnit4.class)
public class ShadowQueuedWorkResetterTest {
  private final RunNotifier runNotifier = new RunNotifier();

  @Before
  public void setup() {
    runNotifier.addListener(
        new RunListener() {
          @Override
          public void testFailure(Failure failure) throws Exception {
            throw new AssertionError("Unexpected test failure: " + failure, failure.getException());
          }
        });
  }

  /**
   * Basic test class that interacts with QueuedWork in two different tests, to ensure QueuedWork
   * remains functional after reset and doesn't leak threads/loopers.
   */
  public static class BasicQueuedWorkTest {

    private static final AtomicInteger initialLooperCount = new AtomicInteger(-1);

    private void doPostToQueuedWorkTest() throws InterruptedException {
      AtomicBoolean wasRun = new AtomicBoolean(false);
      if (RuntimeEnvironment.getApiLevel() < O) {
        reflector(QueuedWorkReflector.class).add(() -> wasRun.set(true));
      } else {
        QueuedWork.addFinisher(() -> wasRun.set(true));
      }
      QueuedWork.waitToFinish();
      initialLooperCount.compareAndSet(-1, ShadowLooper.getAllLoopers().size());

      assertThat(wasRun.get()).isTrue();
      assertThat(ShadowLooper.getAllLoopers()).hasSize(initialLooperCount.get());
    }

    @Test
    public void postToQueuedTest() throws InterruptedException {
      doPostToQueuedWorkTest();
    }

    @Test
    public void anotherPostToQueuedWorkTest() throws InterruptedException {
      doPostToQueuedWorkTest();
    }
  }

  @Test
  public void basicPostAndRun() throws InitializationError {
    Runner runner = new RobolectricTestRunner(BasicQueuedWorkTest.class);

    // run and assert no failures
    runner.run(runNotifier);
  }
}
