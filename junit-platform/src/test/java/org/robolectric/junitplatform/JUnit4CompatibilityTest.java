package org.robolectric.junitplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import android.graphics.Matrix;
import android.os.Build;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.junit.jupiter.RobolectricExtension;
import org.robolectric.junit.platform.RobolectricTestEngine;

/** Tests for running JUnit 4 Robolectric test classes with the robolectric engine. */
class JUnit4CompatibilityTest {

  @BeforeAll
  static void enableAllSdks() {
    // The fixtures use fixed SDKs, which CI might not enable.
    System.clearProperty("robolectric.enabledSdks");
  }

  @Test
  void runsJUnit4Classes() {
    executeTests(selectClass(JUnit4Test.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(2).succeeded(2).skipped(2));
  }

  @Test
  void runsSelectedMethod() {
    executeTests(selectMethod(JUnit4Test.class, "works"))
        .testEvents()
        .assertStatistics(stats -> stats.started(2).succeeded(2).skipped(0));
  }

  @Test
  void runsParameterizedRobolectricTestRunner() {
    executeTests(selectClass(ParameterizedTest.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(2).succeeded(2));
  }

  @Test
  void runsAndroidJUnit4() {
    executeTests(selectClass(AndroidJUnit4Test.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(1).succeeded(1));
  }

  @Test
  void reportsFailuresAndAssumptions() {
    executeTests(selectClass(FailingTest.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(2).failed(1).aborted(1));
  }

  @Test
  void beforeClass_canUseConfigurationDependentShadows() {
    executeTests(selectClass(UsesGraphicsInBeforeClass.class))
        .testEvents()
        .assertStatistics(stats -> stats.started(1).succeeded(1));
  }

  @Test
  void jupiterAndJUnit4Classes_shareSandbox() {
    EngineExecutionResults results =
        EngineTestKit.engine(new RobolectricTestEngine())
            .selectors(
                selectClass(SharedSandboxJupiterTest.class),
                selectClass(SharedSandboxJUnit4Test.class))
            .execute();

    results.testEvents().assertStatistics(stats -> stats.started(2).succeeded(2));
    assertThat(
            results.containerEvents().started().stream()
                .map(event -> event.getTestDescriptor().getUniqueId().getLastSegment().getType())
                .filter("sandbox"::equals)
                .count())
        .isEqualTo(1);
  }

  @Test
  void canBeDisabled() {
    EngineTestKit.engine(new RobolectricTestEngine())
        .configurationParameter("robolectric.junit.platform.junit4.enabled", "false")
        .selectors(selectClass(JUnit4Test.class))
        .execute()
        .testEvents()
        .assertStatistics(stats -> stats.started(0));
  }

  @Test
  void launcher_runsJUnit4ClassesOnlyWithRobolectricEngine() {
    TestPlan testPlan =
        LauncherFactory.create()
            .discover(
                LauncherDiscoveryRequestBuilder.request()
                    .selectors(selectClass(JUnit4Test.class))
                    .build());

    assertThat(testsOfEngine(testPlan, "robolectric")).hasSize(4);
    assertThat(testsOfEngine(testPlan, "junit-vintage")).isEmpty();
  }

  private static EngineExecutionResults executeTests(
      org.junit.platform.engine.DiscoverySelector selector) {
    return EngineTestKit.engine(new RobolectricTestEngine()).selectors(selector).execute();
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

  @RunWith(RobolectricTestRunner.class)
  @Config(sdk = {33, 34})
  public static class JUnit4Test {
    private static boolean beforeClassRan;

    @Rule public final TestName testName = new TestName();

    @BeforeClass
    public static void beforeClass() {
      beforeClassRan = true;
    }

    @org.junit.Test
    public void works() {
      assertTrue(beforeClassRan);
      assertTrue(Build.VERSION.SDK_INT == 33 || Build.VERSION.SDK_INT == 34);
      assertTrue(testName.getMethodName().startsWith("works"));
      assertSame(Looper.getMainLooper().getThread(), Thread.currentThread());
    }

    @org.junit.Test
    @Ignore
    public void ignored() {}
  }

  @RunWith(ParameterizedRobolectricTestRunner.class)
  @Config(sdk = 34)
  public static class ParameterizedTest {
    private final int value;

    public ParameterizedTest(int value) {
      this.value = value;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
      return Arrays.asList(new Object[] {1}, new Object[] {2});
    }

    @org.junit.Test
    public void works() {
      assertTrue(value > 0);
      assertEquals(34, Build.VERSION.SDK_INT);
    }
  }

  @RunWith(AndroidJUnit4.class)
  @Config(sdk = 34)
  public static class AndroidJUnit4Test {
    @org.junit.Test
    public void runsInSandbox() {
      assertTrue(getClass().getClassLoader() instanceof SandboxClassLoader);
      assertEquals(34, Build.VERSION.SDK_INT);
    }
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(sdk = 34)
  public static class FailingTest {
    @org.junit.Test
    public void fails() {
      assertEquals(1, 2);
    }

    @org.junit.Test
    public void assumptionFails() {
      Assume.assumeTrue(false);
    }
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(sdk = 34, instrumentedPackages = "org.robolectric.junitplatform.junit4beforeclass")
  public static class UsesGraphicsInBeforeClass {
    private static Matrix matrix;

    @BeforeClass
    public static void beforeClass() {
      matrix = new Matrix();
    }

    @org.junit.Test
    public void matrixIsUsable() {
      assertTrue(matrix.isIdentity());
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = 34, instrumentedPackages = "org.robolectric.junitplatform.shared")
  static class SharedSandboxJupiterTest {
    @Test
    void works() {
      assertTrue(getClass().getClassLoader() instanceof SandboxClassLoader);
    }
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(sdk = 34, instrumentedPackages = "org.robolectric.junitplatform.shared")
  public static class SharedSandboxJUnit4Test {
    @org.junit.Test
    public void works() {
      assertTrue(getClass().getClassLoader() instanceof SandboxClassLoader);
    }
  }
}
