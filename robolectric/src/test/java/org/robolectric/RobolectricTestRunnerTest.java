package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.robolectric.util.ReflectionHelpers.callConstructor;

import android.app.Application;
import android.os.Build;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner.ResourcesMode;
import org.robolectric.RobolectricTestRunner.RobolectricFrameworkMethod;
import org.robolectric.RobolectricTestRunnerTest.TestWithBrokenAppCreate.MyTestApplication;
import org.robolectric.android.internal.ParallelUniverse;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.PerfStatsCollector.Metric;
import org.robolectric.util.PerfStatsReporter;
import org.robolectric.util.TestUtil;

@RunWith(JUnit4.class)
public class RobolectricTestRunnerTest {

  private RunNotifier notifier;
  private List<String> events;
  private String priorEnabledSdks;
  private String priorAlwaysInclude;

  @Before
  public void setUp() throws Exception {
    notifier = new RunNotifier();
    events = new ArrayList<>();
    notifier.addListener(new RunListener() {
      @Override
      public void testIgnored(Description description) throws Exception {
        events.add("ignored: " + description.getDisplayName());
      }

      @Override
      public void testFailure(Failure failure) throws Exception {
        events.add("failure: " + failure.getMessage());
      }
    });

    priorEnabledSdks = System.getProperty("robolectric.enabledSdks");
    System.clearProperty("robolectric.enabledSdks");

    priorAlwaysInclude = System.getProperty("robolectric.alwaysIncludeVariantMarkersInTestName");
    System.clearProperty("robolectric.alwaysIncludeVariantMarkersInTestName");
  }

  @After
  public void tearDown() throws Exception {
    TestUtil.resetSystemProperty(
        "robolectric.alwaysIncludeVariantMarkersInTestName", priorAlwaysInclude);
    TestUtil.resetSystemProperty("robolectric.enabledSdks", priorEnabledSdks);
  }

  @Test
  public void ignoredTestCanSpecifyUnsupportedSdkWithoutExploding() throws Exception {
    RobolectricTestRunner runner = new MyRobolectricTestRunner(TestWithOldSdk.class);
    runner.run(notifier);
    assertThat(events).containsExactly(
        "failure: Robolectric does not support API level 11.",
        "ignored: ignoredOldSdkMethod(org.robolectric.RobolectricTestRunnerTest$TestWithOldSdk)"
    );
  }

  @Test
  public void failureInResetterDoesntBreakAllTests() throws Exception {
    RobolectricTestRunner runner =
        new MyRobolectricTestRunner(TestWithTwoMethods.class) {
          @Override
          ParallelUniverseInterface getHooksInterface(SdkEnvironment sdkEnvironment) {
            Class<? extends ParallelUniverseInterface> clazz =
                sdkEnvironment.bootstrappedClass(MyParallelUniverseWithFailingSetUp.class);
            return callConstructor(clazz);
          }
        };
    runner.run(notifier);
    assertThat(events).containsExactly(
        "failure: fake error in setUpApplicationState",
        "failure: fake error in setUpApplicationState"
    );
  }

  @Test
  public void failureInAppOnCreateDoesntBreakAllTests() throws Exception {
    RobolectricTestRunner runner = new MyRobolectricTestRunner(TestWithBrokenAppCreate.class);
    runner.run(notifier);
    System.out.println("events = " + events);
    assertThat(events)
        .containsExactly(
            "failure: fake error in application.onCreate",
            "failure: fake error in application.onCreate");
  }

  @Test
  public void equalityOfRobolectricFrameworkMethod() throws Exception {
    Method method = TestWithTwoMethods.class.getMethod("first");
    RobolectricFrameworkMethod rfm16 =
        new RobolectricFrameworkMethod(
            method,
            mock(AndroidManifest.class),
            new SdkConfig(16),
            mock(Config.class),
            ResourcesMode.legacy,
            ResourcesMode.legacy,
            false);
    RobolectricFrameworkMethod rfm17 =
        new RobolectricFrameworkMethod(
            method,
            mock(AndroidManifest.class),
            new SdkConfig(17),
            mock(Config.class),
            ResourcesMode.legacy,
            ResourcesMode.legacy,
            false);
    RobolectricFrameworkMethod rfm16b =
        new RobolectricFrameworkMethod(
            method,
            mock(AndroidManifest.class),
            new SdkConfig(16),
            mock(Config.class),
            ResourcesMode.legacy,
            ResourcesMode.legacy,
            false);
    RobolectricFrameworkMethod rfm16c =
        new RobolectricFrameworkMethod(
            method,
            mock(AndroidManifest.class),
            new SdkConfig(16),
            mock(Config.class),
            ResourcesMode.binary,
            ResourcesMode.legacy,
            false);

    assertThat(rfm16).isNotEqualTo(rfm17);
    assertThat(rfm16).isEqualTo(rfm16b);
    assertThat(rfm16).isNotEqualTo(rfm16c);

    assertThat(rfm16.hashCode()).isEqualTo((rfm16b.hashCode()));
  }

  @Test
  public void shouldReportPerfStats() throws Exception {
    List<Metric> metrics = new ArrayList<>();
    PerfStatsReporter reporter = (metadata, metrics1) -> metrics.addAll(metrics1);

    RobolectricTestRunner runner = new MyRobolectricTestRunner(TestWithTwoMethods.class) {
      @Nonnull
      @Override
      protected Iterable<PerfStatsReporter> getPerfStatsReporters() {
        return singletonList(reporter);
      }
    };

    runner.run(notifier);

    Set<String> metricNames = metrics.stream().map(Metric::getName).collect(toSet());
    assertThat(metricNames).contains("initialization");
  }

  /////////////////////////////

  public static class MyParallelUniverseWithFailingSetUp extends ParallelUniverse {

    @Override
    public void setUpApplicationState(ApkLoader apkLoader, Method method,
        Config config, AndroidManifest appManifest, SdkEnvironment environment) {
      throw new RuntimeException("fake error in setUpApplicationState");
    }
  }

  @Ignore
  public static class TestWithOldSdk {
    @Config(sdk = Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void oldSdkMethod() throws Exception {
      fail("I should not be run!");
    }

    @Ignore("This test shouldn't run, and shouldn't cause the test runner to fail")
    @Config(sdk = Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void ignoredOldSdkMethod() throws Exception {
      fail("I should not be run!");
    }
  }

  @Ignore
  @FixMethodOrder(MethodSorters.NAME_ASCENDING)
  public static class TestWithTwoMethods {
    @Test
    public void first() throws Exception {
    }

    @Test
    public void second() throws Exception {
    }
  }

  @Ignore
  @FixMethodOrder(MethodSorters.NAME_ASCENDING)
  @Config(application = MyTestApplication.class)
  public static class TestWithBrokenAppCreate {
    @Test
    public void first() throws Exception {}

    @Test
    public void second() throws Exception {}

    public static class MyTestApplication extends Application {
      @Override
      public void onCreate() {
        throw new RuntimeException("fake error in application.onCreate");
      }
    }
  }

  @Ignore
  @FixMethodOrder(MethodSorters.NAME_ASCENDING)
  @Config(application = MyTestApplication.class)
  public static class TestWithBrokenAppTerminate {
    @Test
    public void first() throws Exception {}

    @Test
    public void second() throws Exception {}

    public static class MyTestApplication extends Application {
      @Override
      public void onTerminate() {
        throw new RuntimeException("fake error in application.onTerminate");
      }
    }
  }

  private static class MyRobolectricTestRunner extends RobolectricTestRunner {
    public MyRobolectricTestRunner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Nonnull
    @Override
    protected SdkPicker createSdkPicker() {
      return new SdkPicker(asList(new SdkConfig(SdkConfig.MAX_SDK_VERSION)), null);
    }

    @Override
    ResourcesMode getResourcesMode() {
      return ResourcesMode.legacy;
    }
  }
}
