package org.robolectric;

import android.app.Application;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.manifest.AndroidManifest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.TestUtil.resourceFile;

public class TestRunnerSequenceTest {
  public static class StateHolder {
    public final static List<String> transcript = new ArrayList<>();
    public static int nestLevel = 0;

    public static void nest(String s) {
      add(s);
      nestLevel++;
    }

    public static void add(String s) {
      StringBuilder buf = new StringBuilder();
      for (int i = 0; i < nestLevel; i++) buf.append("| ");
      buf.append(s);
      transcript.add(buf.toString());
    }

    public static void unnest(String s) {
      nestLevel--;
      add(s);
    }

    public static void reset() {
      nestLevel = 0;
      transcript.clear();
    }
  }

  @Before
  public void setUp() throws Exception {
    StateHolder.reset();
  }

  @Test public void shouldRunThingsInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(SimpleTest.class)));
    assertThat(StateHolder.transcript).containsExactly(
        "@ClassRule apply",
        "@ClassRule before",
        "| @BeforeClass",
        "| | configureShadows",
        "| | set up Android environment",
        "| | | TestLifecycle.createApplication()",
        "| | | application.onCreate()",
        "| | | | TestLifecycle.beforeTest()",
        "| | | | | application.beforeTest()",
        "| | | | | | TestLifecycle.prepareTest()",
        "| | | | | | application.prepareTest()",
        "| | | | | | @Rule apply",
        "| | | | | | @Rule before",
        "| | | | | | | @Before",
        "| | | | | | | | TEST!",
        "| | | | | | | @After",
        "| | | | | | @Rule after",
        "| | | | | application.onTerminate()",
        "| | | | TestLifecycle.afterTest()",
        "| | | application.afterTest()",
        "| | tear down Android environment",
        "| @AfterClass",
        "@ClassRule after"
    );
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
        "@ClassRule apply",
        "@ClassRule before",
        "| @BeforeClass",
        "| | configureShadows",
        "| | set up Android environment",
        "| | | TestLifecycle.createApplication()",
        "| | | application.onCreate()",
        "| | | | TestLifecycle.beforeTest()",
        "| | | | | application.beforeTest()",
        "| | | | | | TestLifecycle.prepareTest()",
        "| | | | | | application.prepareTest()",
        "| | | | | | @Rule apply",
        "| | | | | | @Rule before",
        "| | | | | | | @Before",
        "| | | | | | | | TEST!",
        "| | | | | | | @After",
        "| | | | | | @Rule after",
        "| | | | | application.onTerminate()",
        "| | | | TestLifecycle.afterTest()",
        "| | | application.afterTest()",
        "| | tear down Android environment",
        "| @AfterClass",
        "@ClassRule after"
    );
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

  public static class SimpleTest {
    @ClassRule public static TestRule rule = new TestRule() {
      @Override public Statement apply(final Statement base, Description description) {
        add("@ClassRule apply");
        return new Statement() {
          @Override public void evaluate() throws Throwable {
            nest("@ClassRule before");
            base.evaluate();
            unnest("@ClassRule after");
          }
        };
      }
    };

    @Rule
    public MethodRule junitRule = new MethodRule() {
      @Override
      public Statement apply(final Statement base, FrameworkMethod method, Object target) {
        add("@Rule apply");
        return new Statement() {
          @Override
          public void evaluate() throws Throwable {
            nest("@Rule before");
            base.evaluate();
            unnest("@Rule after");
          }
        };
      }
    };

    @BeforeClass
    public static void beforeClass() throws Exception {
      nest("@BeforeClass");
    }

    @Before
    public void setUp() throws Exception {
      nest("@Before");
    }

    @Test public void shouldDoNothingMuch() throws Exception {
      add("TEST!");
    }

    @Config(sdk=17)
    @Test public void shouldDoEvenLess() throws Exception {
      add("TEST!");
    }

    @After
    public void tearDown() throws Exception {
      unnest("@After");
    }

    @AfterClass
    public static void afterClass() throws Exception {
      unnest("@AfterClass");
    }

    private static void nest(String s) {
      StateHolder.nest(s + " " + extraInfo());
    }

    private static void add(String s) {
      StateHolder.add(s + " " + extraInfo());
    }

    private static void unnest(String s) {
      StateHolder.unnest(s + " " + extraInfo());
    }

    private static String extraInfo() {
      return "sdk=" + RuntimeEnvironment.getApiLevel() + "; cl=" + SimpleTest.class.getClassLoader().toString();
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

    @Override
    protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod) throws Throwable {
      StateHolder.nest("set up Android environment");
      super.beforeTest(sandbox, method, bootstrappedMethod);
    }

    @Override
    protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
      super.afterTest(method, bootstrappedMethod);
      StateHolder.unnest("tear down Android environment");
    }

    @NotNull
    @Override
    protected SdkPicker createSdkPicker() {
      return new SdkPicker(singletonList(new SdkConfig(JELLY_BEAN)), new Properties());
    }

    @NotNull
    @Override
    protected InstrumentationConfiguration createClassLoaderConfig(FrameworkMethod method) {
      InstrumentationConfiguration.Builder builder = new InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method));
      builder.doNotAcquireClass(StateHolder.class);
      return builder.build();
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
      return new AndroidManifest(resourceFile("TestAndroidManifest.xml"), resourceFile("res"), resourceFile("assets"));
    }

    @NotNull
    @Override protected Class<? extends TestLifecycle> getTestLifecycleClass() {
      return MyTestLifecycle.class;
    }

    @Override protected void configureShadows(FrameworkMethod frameworkMethod, Sandbox sandbox) {
      StateHolder.add("configureShadows");
      super.configureShadows(frameworkMethod, sandbox);
    }
  }

  @DoNotInstrument
  public static class MyTestLifecycle extends DefaultTestLifecycle {
    @Override public Application createApplication(Method method, AndroidManifest appManifest, Config config) {
      StateHolder.add("TestLifecycle.createApplication()");
      return new MyApplication();
    }

    @Override public void beforeTest(Method method) {
      StateHolder.nest("TestLifecycle.beforeTest()");
      super.beforeTest(method);
    }

    @Override public void prepareTest(Object test) {
      StateHolder.add("TestLifecycle.prepareTest()");
      super.prepareTest(test);
    }

    @Override public void afterTest(Method method) {
      StateHolder.unnest("TestLifecycle.afterTest()");
      super.afterTest(method);
    }

    private static class MyApplication extends Application implements TestLifecycleApplication {
      @Override public void onCreate() {
        StateHolder.nest("application.onCreate()");
      }

      @Override public void beforeTest(Method method) {
        StateHolder.nest("application.beforeTest()");
      }

      @Override public void prepareTest(Object test) {
        StateHolder.add("application.prepareTest()");
      }

      @Override public void afterTest(Method method) {
        StateHolder.unnest("application.afterTest()");
      }

      @Override public void onTerminate() {
        StateHolder.unnest("application.onTerminate()");
      }
    }
  }
}
