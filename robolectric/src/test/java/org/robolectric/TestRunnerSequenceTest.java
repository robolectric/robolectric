package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.app.Application;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.annotation.experimental.LazyApplication.LazyLoad;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;

@RunWith(JUnit4.class)
public class TestRunnerSequenceTest {

  public static class StateHolder {
    public static final List<String> transcript = new ArrayList<>();

    public static void add(String s) {
      transcript.add(s);
    }

    public static void reset() {
      transcript.clear();
    }
  }

  @Before
  public void setUp() throws Exception {
    StateHolder.reset();
  }

  @Test
  public void shouldRunThingsInTheRightOrder_simpleTest() throws Exception {
    assertNoFailures(run(new Runner(SimpleTest.class)));
    assertThat(StateHolder.transcript)
        .containsExactly(
            "configureSandbox",
            "@ClassRule apply",
            "@ClassRule before",
            "@BeforeClass",
            "configureSandbox",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "@AfterClass",
            "@ClassRule after");
  }

  @Test
  public void shouldRunMultiConfigTestInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(MultiConfigTest.class)));
    // As the order is not deterministic, we just check that the expected sequence is a substring
    // of the actual one.
    assertThat(StateHolder.transcript)
        // using containsAtLeast due to the test order being non-deterministic
        .containsAtLeast(
            "configureSandbox",
            "@ClassRule apply",
            "@ClassRule before",
            "@BeforeClass",
            "configureSandbox",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "@AfterClass",
            "@ClassRule after");
    assertThat(StateHolder.transcript)
        // using containsAtLeast due to the test order being non-deterministic
        .containsAtLeast(
            "configureSandbox",
            "@ClassRule apply",
            "@ClassRule before",
            "@BeforeClass",
            "configureSandbox",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "@AfterClass",
            "@ClassRule after");
  }

  @Test
  public void whenNoAppManifest_shouldRunMultiConfigTestInTheRightOrder() throws Exception {
    assertNoFailures(run(new Runner(MultiConfigTest.class) {}));
    // Default SDK = 30 Sandbox
    assertThat(StateHolder.transcript)
        // using containsAtLeast due to the test order being non-deterministic
        .containsAtLeast(
            "configureSandbox",
            "@ClassRule apply",
            "@ClassRule before",
            "@BeforeClass",
            "configureSandbox",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "configureSandbox",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "@AfterClass",
            "@ClassRule after");

    assertThat(StateHolder.transcript)
        // using containsAtLeast due to the test order being non-deterministic
        .containsAtLeast(
            "configureSandbox",
            "@ClassRule apply",
            "@ClassRule before",
            "@BeforeClass",
            "configureSandbox",
            "Android environment set up",
            "application.onCreate()",
            "TestLifecycle.beforeTest()",
            "application.beforeTest()",
            "TestLifecycle.prepareTest()",
            "application.prepareTest()",
            "@Rule apply",
            "@Rule before",
            "@Before",
            "TEST!",
            "@After",
            "@Rule after",
            "application.onTerminate()",
            "TestLifecycle.afterTest()",
            "application.afterTest()",
            "Android environment resetState",
            "@AfterClass",
            "@ClassRule after");
  }

  @RunWith(Runner.class)
  @Config(application = TestRunnerSequenceTest.MyApplication.class)
  @LazyApplication(LazyLoad.OFF)
  public static class SimpleTest {
    @ClassRule
    public static TestRule rule =
        new TestRule() {
          @Override
          public Statement apply(final Statement base, Description description) {
            add("@ClassRule apply");
            return new Statement() {
              @Override
              public void evaluate() throws Throwable {
                try {
                  add("@ClassRule before");
                  base.evaluate();
                } finally {
                  add("@ClassRule after");
                }
              }
            };
          }
        };

    @Rule
    public MethodRule junitRule =
        new MethodRule() {
          @Override
          public Statement apply(final Statement base, FrameworkMethod method, Object target) {
            add("@Rule apply");
            return new Statement() {
              @Override
              public void evaluate() throws Throwable {
                add("@Rule before");
                base.evaluate();
                add("@Rule after");
              }
            };
          }
        };

    @BeforeClass
    public static void beforeClass() throws Exception {
      add("@BeforeClass");
    }

    @Before
    public void setUp() throws Exception {
      add("@Before");
    }

    @Test
    public void shouldDoNothingMuch() throws Exception {
      add("TEST!");
    }

    @After
    public void tearDown() throws Exception {
      add("@After");
    }

    @AfterClass
    public static void afterClass() throws Exception {
      add("@AfterClass");
    }

    private static void add(String s) {
      StateHolder.add(s + extraInfo());
    }

    private static String extraInfo() {
      return "";
    }
  }

  @Config(application = TestRunnerSequenceTest.MyApplication.class)
  @LazyApplication(LazyLoad.OFF)
  public static class MultiConfigTest extends SimpleTest {
    @Test
    public void shouldDoNothingMuch2() throws Exception {
      StateHolder.add("TEST!");
    }

    @Config(instrumentedPackages = "some.package.b" /* creates a new sandbox */)
    @Test
    public void shouldDoEvenLess() throws Exception {
      StateHolder.add("TEST!");
    }
  }

  private Result run(Runner runner) {
    JUnitCore runnerCore = new JUnitCore();
    return runnerCore.run(runner);
  }

  private void assertNoFailures(Result result) {
    if (!result.wasSuccessful()) {
      for (Failure failure : result.getFailures()) {
        fail(failure.getMessage() + failure.getException());
      }
    }
  }

  public static class Runner extends SingleSdkRobolectricTestRunner {
    public Runner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override
    protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod)
        throws Throwable {
      StateHolder.add("Android environment set up" + extraInfo());
      super.beforeTest(sandbox, method, bootstrappedMethod);
    }

    @Override
    protected void finallyAfterTest(FrameworkMethod method) {
      StateHolder.add("Android environment resetState" + extraInfo());
      super.finallyAfterTest(method);
    }

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
      StateHolder.add("configureSandbox");
      super.configureSandbox(sandbox, frameworkMethod);
    }

    private static String extraInfo() {
      return "";
    }
  }

  public static class MyTestLifecycle extends DefaultTestLifecycle {

    @Override
    public void beforeTest(Method method) {
      StateHolder.add("TestLifecycle.beforeTest()");
      super.beforeTest(method);
    }

    @Override
    public void prepareTest(Object test) {
      StateHolder.add("TestLifecycle.prepareTest()");
      super.prepareTest(test);
    }

    @Override
    public void afterTest(Method method) {
      StateHolder.add("TestLifecycle.afterTest()");
      super.afterTest(method);
    }
  }

  public static class MyApplication extends Application implements TestLifecycleApplication {
    @Override
    public void onCreate() {
      StateHolder.add("application.onCreate()");
    }

    @Override
    public void beforeTest(Method method) {
      StateHolder.add("application.beforeTest()");
    }

    @Override
    public void prepareTest(Object test) {
      StateHolder.add("application.prepareTest()");
    }

    @Override
    public void afterTest(Method method) {
      StateHolder.add("application.afterTest()");
    }

    @Override
    public void onTerminate() {
      StateHolder.add("application.onTerminate()");
    }
  }
}
