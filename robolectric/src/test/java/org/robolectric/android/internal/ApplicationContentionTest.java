package org.robolectric.android.internal;

import android.app.Application;
import android.os.Build.VERSION_CODES;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameter;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Test to ensure Robolectric correctly handles calls to getApplication() when there is the
 * possibility for contention
 */
// Sticking to one SDK for simplicity. The code being tested is SDK-agnostic, and this also saves
// having to worry about increased runtimes as the number of SDKs goes up
@Config(sdk = VERSION_CODES.Q)
@RunWith(ParameterizedRobolectricTestRunner.class)
public class ApplicationContentionTest {
  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  @SuppressWarnings("unused") // Just here to create contention on getApplication()
  private final Application application = RuntimeEnvironment.getApplication();

  @Parameter public Integer n;

  @Parameters(name = "n: {0}")
  public static Collection<?> parameters() {
    return IntStream.range(1, 1000).boxed().collect(Collectors.toList());
  }

  @Test
  @SuppressWarnings("FutureReturnValueIgnored")
  public void testDeferGetApplication() {
    // defer to potentially disrupt subsequent application creation
    executorService.submit(RuntimeEnvironment::getApplication);
  }
}
