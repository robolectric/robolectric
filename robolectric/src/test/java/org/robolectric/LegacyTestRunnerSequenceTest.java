package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.junit.Assert.fail;

import android.app.Application;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;

@RunWith(JUnit4.class)
public class LegacyTestRunnerSequenceTest {

  public static class StateHolder {
    public static List<String> transcript;
  }

  @Before
  public void setUp() throws Exception {
    assume().that(Boolean.getBoolean("robolectric.useLegacySandboxFlow")).isTrue();
    StateHolder.transcript = new ArrayList<>();
  }

  @Test
  public void shouldRunThingsInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(SimpleTest.class)));
    assertThat(StateHolder.transcript)
        .containsExactly(
            "configureSandbox",
            "application.onCreate",
            "beforeTest",
            "application.beforeTest",
            "prepareTest",
            "application.prepareTest",
            "TEST!",
            "application.onTerminate",
            "afterTest",
            "application.afterTest");
    StateHolder.transcript.clear();
  }

  @Test
  public void whenNoAppManifest_shouldRunThingsInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(SimpleTest.class) {}));
    assertThat(StateHolder.transcript)
        .containsExactly(
            "configureSandbox",
            "application.onCreate",
            "beforeTest",
            "application.beforeTest",
            "prepareTest",
            "application.prepareTest",
            "TEST!",
            "application.onTerminate",
            "afterTest",
            "application.afterTest");
    StateHolder.transcript.clear();
  }

  @Ignore
  @Config(application = LegacyTestRunnerSequenceTest.MyApplication.class)
  public static class SimpleTest {
    @Test
    public void shouldDoNothingMuch() throws Exception {
      StateHolder.transcript.add("TEST!");
    }
  }

  private Result run(Runner runner) throws InitializationError {
    RunNotifier notifier = new RunNotifier();
    Result result = new Result();
    notifier.addListener(result.createListener());
    runner.run(notifier);
    return result;
  }

  private void assertNoFailures(Result result) {
    if (!result.wasSuccessful()) {
      for (Failure failure : result.getFailures()) {
        fail(failure.getMessage());
      }
    }
  }

  public static class Runner extends SingleSdkRobolectricTestRunner {

    Runner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Nonnull
    @Override
    protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
      InstrumentationConfiguration.Builder builder =
          new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
      builder.doNotAcquireClass(StateHolder.class);
      return builder.build();
    }

    @Nonnull
    @Override
    protected Class<? extends TestLifecycle> getTestLifecycleClass() {
      return MyTestLifecycle.class;
    }

    @Override
    protected void configureSandbox(Sandbox sandbox, FrameworkMethod frameworkMethod) {
      StateHolder.transcript.add("configureSandbox");
      super.configureSandbox(sandbox, frameworkMethod);
    }
  }

  public static class MyTestLifecycle extends DefaultTestLifecycle {

    @Override
    public void beforeTest(Method method) {
      StateHolder.transcript.add("beforeTest");
      super.beforeTest(method);
    }

    @Override
    public void prepareTest(Object test) {
      StateHolder.transcript.add("prepareTest");
      super.prepareTest(test);
    }

    @Override
    public void afterTest(Method method) {
      StateHolder.transcript.add("afterTest");
      super.afterTest(method);
    }
  }

  public static class MyApplication extends Application implements TestLifecycleApplication {
    @Override
    public void onCreate() {
      StateHolder.transcript.add("application.onCreate");
    }

    @Override
    public void beforeTest(Method method) {
      StateHolder.transcript.add("application.beforeTest");
    }

    @Override
    public void prepareTest(Object test) {
      StateHolder.transcript.add("application.prepareTest");
    }

    @Override
    public void afterTest(Method method) {
      StateHolder.transcript.add("application.afterTest");
    }

    @Override
    public void onTerminate() {
      StateHolder.transcript.add("application.onTerminate");
    }
  }
}
