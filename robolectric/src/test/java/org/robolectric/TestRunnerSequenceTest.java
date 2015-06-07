package org.robolectric;

import android.app.Application;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;
import org.robolectric.util.Transcript;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.TestUtil.resourceFile;

public class TestRunnerSequenceTest {
  public static class StateHolder {
    public static Transcript transcript;
  }

  @Test public void shouldRunThingsInTheRightOrder() throws Exception {
    StateHolder.transcript = new Transcript();
    assertNoFailures(run(new Runner(SimpleTest.class)));
    StateHolder.transcript.assertEventsSoFar(
        "configureShadows",
//                "resetStaticState", // no longer an overridable hook
//                "setupApplicationState", // no longer an overridable hook
        "createApplication",
        "application.onCreate",
        "beforeTest",
        "application.beforeTest",
        "prepareTest",
        "application.prepareTest",
        "TEST!",
        "application.onTerminate",
        "afterTest",
        "application.afterTest"
    );
  }

  @Test public void whenNoAppManifest_shouldRunThingsInTheRightOrder() throws Exception {
    StateHolder.transcript = new Transcript();
    assertNoFailures(run(new Runner(SimpleTest.class) {
      @Override protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName) {
        return null;
      }
    }));
    StateHolder.transcript.assertEventsSoFar(
        "configureShadows",
        "createApplication",
        "application.onCreate",
        "beforeTest",
        "application.beforeTest",
        "prepareTest",
        "application.prepareTest",
        "TEST!",
        "application.onTerminate",
        "afterTest",
        "application.afterTest"
    );
  }

  @Test public void shouldReleaseAllStateAfterClassSoWeDontLeakMemory() throws Exception {
    RobolectricTestRunner robolectricTestRunner = new Runner(SimpleTest.class);
    robolectricTestRunner.run(new RunNotifier());
    assertTrue(robolectricTestRunner.allStateIsCleared());
  }

  public static class SimpleTest {
    @Test public void shouldDoNothingMuch() throws Exception {
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
        fail(failure.getMessage(), failure.getException());
      }
    }
  }

  public static class Runner extends RobolectricTestRunner {
    public Runner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override public InstrumentationConfiguration createClassLoaderConfig() {
      return InstrumentationConfiguration.newBuilder()
          .doNotAquireClass(StateHolder.class.getName())
          .build();
    }

    @Override
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetDir, String packageName) {
      return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
    }

    @Override protected Class<? extends TestLifecycle> getTestLifecycleClass() {
      return MyTestLifecycle.class;
    }

    @Override protected void configureShadows(SdkEnvironment sdkEnvironment, Config config) {
      StateHolder.transcript.add("configureShadows");
      super.configureShadows(sdkEnvironment, config);
    }
  }

  public static class MyTestLifecycle extends DefaultTestLifecycle {
    @Override public Application createApplication(Method method, AndroidManifest appManifest, Config config) {
      StateHolder.transcript.add("createApplication");
      return new MyApplication();
    }

    @Override public void beforeTest(Method method) {
      StateHolder.transcript.add("beforeTest");
      super.beforeTest(method);
    }

    @Override public void prepareTest(Object test) {
      StateHolder.transcript.add("prepareTest");
      super.prepareTest(test);
    }

    @Override public void afterTest(Method method) {
      StateHolder.transcript.add("afterTest");
      super.afterTest(method);
    }

    private static class MyApplication extends Application implements TestLifecycleApplication {
      @Override public void onCreate() {
        StateHolder.transcript.add("application.onCreate");
      }

      @Override public void beforeTest(Method method) {
        StateHolder.transcript.add("application.beforeTest");
      }

      @Override public void prepareTest(Object test) {
        StateHolder.transcript.add("application.prepareTest");
      }

      @Override public void afterTest(Method method) {
        StateHolder.transcript.add("application.afterTest");
      }

      @Override public void onTerminate() {
        StateHolder.transcript.add("application.onTerminate");
      }
    }
  }
}
