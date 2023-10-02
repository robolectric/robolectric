package org.robolectric;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.concurrent.RejectedExecutionException;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.robolectric.internal.AndroidSandbox;

@RunWith(JUnit4.class)
public class RobolectricTestRunnerCleanupTest {

  @Test
  public void sandboxShutdownShouldCloseClassloader() throws Exception {
    SingleSdkRobolectricTestRunner runner =
        new SingleSdkRobolectricTestRunner(
            TestWithEmptyTest.class, SingleSdkRobolectricTestRunner.defaultInjector().build());

    runner.run(mock(RunNotifier.class));

    AndroidSandbox latestSandbox = runner.getLatestSandbox();
    latestSandbox.shutdown();
    // Try to load a class that has not already been loaded. Closing a ClassLoader will only prevent
    // loading new classes.
    assertThrows(
        ClassNotFoundException.class,
        () ->
            latestSandbox
                .getRobolectricClassLoader()
                .loadClass("com.android.server.am.ActivityManagerService"));
  }

  @Test
  public void sandboxShutdownShouldShutDownMainThreadExecutor() throws Exception {
    SingleSdkRobolectricTestRunner runner =
        new SingleSdkRobolectricTestRunner(
            TestWithEmptyTest.class, SingleSdkRobolectricTestRunner.defaultInjector().build());

    runner.run(mock(RunNotifier.class));

    AndroidSandbox latestSandbox = runner.getLatestSandbox();
    latestSandbox.shutdown();
    // Try to run something on the main thread executor, which has been shut down.
    assertThrows(RejectedExecutionException.class, () -> latestSandbox.runOnMainThread(() -> {}));
  }

  @Ignore
  public static class TestWithEmptyTest {
    @Test
    public void emptyTest() throws Exception {}
  }
}
