package org.robolectric;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.singletonList;
import static org.junit.Assert.fail;
import static org.robolectric.util.TestUtil.resourceFile;

import android.app.Application;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.manifest.AndroidManifest;

@RunWith(JUnit4.class)
public class TestRunnerSequenceTest {

  public static class StateHolder {
    public static List<String> transcript;
  }

  private String priorResourcesMode;

  @Before
  public void setUp() throws Exception {
    StateHolder.transcript = new ArrayList<>();

    priorResourcesMode = System.getProperty("robolectric.resourcesMode");
    System.setProperty("robolectric.resourcesMode", "legacy");
  }

  @After
  public void tearDown() throws Exception {
    if (priorResourcesMode == null) {
      System.clearProperty("robolectric.resourcesMode");
    } else {
      System.setProperty("robolectric.resourcesMode", priorResourcesMode);
    }
  }

  @Test public void shouldRunThingsInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(SimpleTest.class)));
    assertThat(StateHolder.transcript).containsExactly(
        "configureSandbox",
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
    StateHolder.transcript.clear();
  }

  @Test public void whenNoAppManifest_shouldRunThingsInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(SimpleTest.class) {
      @Override protected AndroidManifest getAppManifest(Config config) {
        return new AndroidManifest(null, null, null, "package") {
          @Override
          public int getTargetSdkVersion() {
            return SdkConfig.FALLBACK_SDK_VERSION;
          }
        };
      }
    }));
    assertThat(StateHolder.transcript).containsExactly(
        "configureSandbox",
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
    StateHolder.transcript.clear();
  }

  @Test public void shouldReleaseAllStateAfterClassSoWeDontLeakMemory() throws Exception {
    final List<RobolectricTestRunner.RobolectricFrameworkMethod> methods = new ArrayList<>();

    RobolectricTestRunner robolectricTestRunner = new Runner(SimpleTest.class) {
      @Override
      protected void finallyAfterTest(FrameworkMethod method) {
        super.finallyAfterTest(method);

        RobolectricFrameworkMethod roboMethod = (RobolectricFrameworkMethod) method;
        assertThat(roboMethod.parallelUniverseInterface).isNull();
        assertThat(roboMethod.testLifecycle).isNull();
        methods.add(roboMethod);
      }
    };

    robolectricTestRunner.run(new RunNotifier());
    assertThat(methods).isNotEmpty();
  }

  @Config(application = TestRunnerSequenceTest.MyApplication.class)
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
        fail(failure.getMessage());
      }
    }
  }

  public static class Runner extends RobolectricTestRunner {
    public Runner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Nonnull
    @Override
    protected SdkPicker createSdkPicker() {
      return new SdkPicker(singletonList(new SdkConfig(JELLY_BEAN)), null);
    }

    @Nonnull
    @Override
    protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
      InstrumentationConfiguration.Builder builder = new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
      builder.doNotAcquireClass(StateHolder.class);
      return builder.build();
    }

    protected AndroidManifest getAppManifest(Config config) {
      return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
    }

    @Nonnull
    @Override protected Class<? extends TestLifecycle> getTestLifecycleClass() {
      return MyTestLifecycle.class;
    }

    @Override protected void configureSandbox(Sandbox sandbox, FrameworkMethod frameworkMethod) {
      StateHolder.transcript.add("configureSandbox");
      super.configureSandbox(sandbox, frameworkMethod);
    }
  }

  @DoNotInstrument
  public static class MyTestLifecycle extends DefaultTestLifecycle {

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
  }

  public static class MyApplication extends Application implements TestLifecycleApplication {
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
