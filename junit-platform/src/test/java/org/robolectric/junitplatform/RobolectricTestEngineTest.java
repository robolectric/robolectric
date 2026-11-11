package org.robolectric.junitplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import android.app.Application;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Looper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.junit.jupiter.RobolectricExtension;
import org.robolectric.junit.platform.RobolectricTestEngine;

/**
 * Tests for {@link RobolectricTestEngine} and {@link RobolectricExtension}.
 *
 * <p>The fixtures are static nested classes, which the Gradle test task does not run directly. They
 * are in a package that Robolectric sandboxes load, unlike the engine's own packages.
 */
class RobolectricTestEngineTest {

  @BeforeAll
  static void enableAllSdks() {
    // The fixtures use fixed SDKs, which CI might not enable.
    System.clearProperty("robolectric.enabledSdks");
  }

  @Test
  void runsTestsInSandbox() {
    executeTests(selectClass(RunsInSandbox.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(4).succeeded(4));
  }

  @Test
  void runsTestsOncePerSdk() {
    EngineExecutionResults results = executeTests(selectClass(MultipleSdks.class));

    results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
    assertThat(reportedSdkBySandbox(results)).containsExactly("sdk=33", "33", "sdk=34", "34");
  }

  @Test
  void methodLevelConfig_usesItsOwnSandbox() {
    EngineExecutionResults results = executeTests(selectClass(MethodLevelConfig.class));

    results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
    assertThat(reportedSdkBySandbox(results)).containsExactly("sdk=33", "33", "sdk=34", "34");
  }

  @Test
  void supportsJupiterFeatures() {
    executeTests(selectClass(JupiterFeatures.class))
        .testEvents()
        // 1 test + 1 nested test + 3 parameterized invocations + 2 repetitions + 2 dynamic tests
        .assertStatistics(stats -> stats.started(9).succeeded(9).skipped(1));
  }

  @Test
  void reportsFailuresAndAbortedTests() {
    EngineExecutionResults results = executeTests(selectClass(FailingTests.class));

    results.testEvents().assertStatistics(stats -> stats.started(2).failed(1).aborted(1));
    results
        .testEvents()
        .failed()
        .assertThatEvents()
        .haveExactly(1, finishedWithFailure(message("expected: <1> but was: <2>")));
  }

  @Test
  void resetsAndroidEnvironmentBetweenTests() {
    executeTests(selectClass(EnvironmentReset.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(2).succeeded(2));
  }

  @Test
  void perClassLifecycle_setsUpEachTest() {
    executeTests(selectClass(PerClassLifecycle.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(2).succeeded(2));
  }

  @Test
  void beforeAll_runsInSandbox() {
    executeTests(selectClass(UsesSandboxInBeforeAll.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(1).succeeded(1));
  }

  @Test
  void methodSelector_runsOnlySelectedMethod() {
    executeTests(selectMethod(RunsInSandbox.class, "runsOnSdk"))
        .testEvents()
        .assertStatistics(stats -> stats.started(1).succeeded(1));
  }

  @Test
  void uniqueIdSelector_runsOnlySelectedTest() {
    UniqueId testId =
        executeTests(selectClass(MultipleSdks.class)).testEvents().succeeded().stream()
            .map(event -> event.getTestDescriptor().getUniqueId())
            .filter(id -> id.toString().contains("sdk=34"))
            .findFirst()
            .get();

    EngineExecutionResults results = executeTests(selectUniqueId(testId));

    results.testEvents().assertStatistics(stats -> stats.started(1).succeeded(1));
    assertThat(reportedSdkBySandbox(results)).containsExactly("sdk=34", "34");
  }

  @Test
  void unsupportedSdk_failsClass() {
    EngineTestKit.engine(new RobolectricTestEngine())
        .selectors(selectClass(UnsupportedSdk.class))
        .execute()
        .allEvents()
        .assertThatEvents()
        .haveAtLeastOne(finishedWithFailure());
  }

  @Test
  void extension_failsClassWhenNotRunByRobolectricEngine() {
    EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(RunsInSandbox.class))
        .execute()
        .containerEvents()
        .assertThatEvents()
        .haveExactly(
            1,
            finishedWithFailure(
                message(m -> m.contains("is not running in a Robolectric sandbox"))));
  }

  @Test
  void launcher_runsRobolectricTestClassesOnlyWithRobolectricEngine() {
    TestPlan testPlan =
        LauncherFactory.create()
            .discover(
                LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectClass(RunsInSandbox.class))
                    .build());

    assertThat(testsOfEngine(testPlan, "robolectric")).hasSize(4);
    assertThat(testsOfEngine(testPlan, "junit-jupiter")).isEmpty();
  }

  @Test
  void launcher_appliesTagFilters() {
    TestPlan testPlan =
        LauncherFactory.create()
            .discover(
                LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectClass(RunsInSandbox.class))
                    .filters(TagFilter.includeTags("tagged"))
                    .build());

    assertThat(testsOfEngine(testPlan, "robolectric")).hasSize(1);
  }

  private static EngineExecutionResults executeTests(
      org.junit.platform.engine.DiscoverySelector selector) {
    return EngineTestKit.engine(new RobolectricTestEngine()).selectors(selector).execute();
  }

  /** Maps the sandbox of each test (its SDK) to the SDK the test reported. */
  private static Map<String, String> reportedSdkBySandbox(EngineExecutionResults results) {
    Map<String, String> result = new TreeMap<>();
    for (Event event : results.allEvents().reportingEntryPublished().list()) {
      String sandbox = sandboxSdk(event.getTestDescriptor());
      String sdk =
          event
              .getPayload(org.junit.platform.engine.reporting.ReportEntry.class)
              .get()
              .getKeyValuePairs()
              .get("sdk");
      result.put(sandbox, sdk);
    }
    return result;
  }

  private static String sandboxSdk(TestDescriptor descriptor) {
    for (UniqueId.Segment segment : descriptor.getUniqueId().getSegments()) {
      if (segment.getType().equals("sandbox")) {
        String value = segment.getValue();
        return value.substring(0, value.indexOf(','));
      }
    }
    throw new AssertionError("Not in a sandbox: " + descriptor.getUniqueId());
  }

  private static List<TestIdentifier> testsOfEngine(TestPlan testPlan, String engineId) {
    List<TestIdentifier> result = new ArrayList<>();
    for (TestIdentifier root : testPlan.getRoots()) {
      if (root.getUniqueIdObject().getEngineId().equals(Optional.of(engineId))) {
        testPlan.getDescendants(root).stream().filter(TestIdentifier::isTest).forEach(result::add);
      }
    }
    return result;
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34)
  static class RunsInSandbox {
    // Field initializers run after the application is set up.
    private final Application application = RuntimeEnvironment.getApplication();

    @Test
    void runsOnSdk() {
      assertEquals(34, Build.VERSION.SDK_INT);
    }

    @Test
    void runsOnMainThread() {
      assertSame(Looper.getMainLooper().getThread(), Thread.currentThread());
    }

    @Test
    void isLoadedBySandbox() {
      assertTrue(getClass().getClassLoader() instanceof SandboxClassLoader);
    }

    @Test
    @Tag("tagged")
    void applicationIsSetUpBeforeConstruction() {
      assertSame(application, RuntimeEnvironment.getApplication());
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = {33, 34})
  static class MultipleSdks {
    @Test
    void reportsSdk(TestReporter reporter) {
      reporter.publishEntry("sdk", String.valueOf(Build.VERSION.SDK_INT));
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34)
  static class MethodLevelConfig {
    @Test
    void usesClassConfig(TestReporter reporter) {
      reporter.publishEntry("sdk", String.valueOf(Build.VERSION.SDK_INT));
    }

    @Test
    @Config(sdk = 33)
    void usesMethodConfig(TestReporter reporter) {
      reporter.publishEntry("sdk", String.valueOf(Build.VERSION.SDK_INT));
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34)
  static class JupiterFeatures {
    @Test
    void test() {
      assertEquals(34, Build.VERSION.SDK_INT);
    }

    @Test
    @Disabled
    void disabled() {}

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void parameterized(int value) {
      assertTrue(value > 0);
      assertSame(Looper.getMainLooper().getThread(), Thread.currentThread());
    }

    @RepeatedTest(2)
    void repeated() {
      assertEquals(34, Build.VERSION.SDK_INT);
    }

    @TestFactory
    Stream<DynamicTest> dynamic() {
      return Stream.of("a", "b")
          .map(
              name -> DynamicTest.dynamicTest(name, () -> assertEquals(34, Build.VERSION.SDK_INT)));
    }

    @Nested
    class NestedTests {
      @Test
      void runsInSandbox() {
        assertTrue(getClass().getClassLoader() instanceof SandboxClassLoader);
      }
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34)
  static class FailingTests {
    @Test
    void fails() {
      assertEquals(1, 2);
    }

    @Test
    void isAborted() {
      Assumptions.assumeTrue(false);
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  static class EnvironmentReset {
    private static Application firstApplication;

    @Test
    @Order(1)
    void first() {
      firstApplication = RuntimeEnvironment.getApplication();
    }

    @Test
    @Order(2)
    void second() {
      assertNotSame(firstApplication, RuntimeEnvironment.getApplication());
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34)
  @TestInstance(Lifecycle.PER_CLASS)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  static class PerClassLifecycle {
    private Application firstApplication;

    @Test
    @Order(1)
    void first() {
      firstApplication = RuntimeEnvironment.getApplication();
      assertNotNull(firstApplication);
    }

    @Test
    @Order(2)
    void second() {
      assertNotSame(firstApplication, RuntimeEnvironment.getApplication());
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34, instrumentedPackages = "org.robolectric.junitplatform.beforeall")
  static class UsesSandboxInBeforeAll {
    private static Matrix matrix;

    @BeforeAll
    static void beforeAll() {
      // Matrix's shadow is picked based on the test configuration.
      matrix = new Matrix();
    }

    @Test
    void matrixIsUsable() {
      assertTrue(matrix.isIdentity());
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 1)
  static class UnsupportedSdk {
    @Test
    void test() {}
  }
}
