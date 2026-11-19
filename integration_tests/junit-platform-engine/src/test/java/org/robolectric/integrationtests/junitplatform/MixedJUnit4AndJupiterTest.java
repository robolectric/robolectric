package org.robolectric.integrationtests.junitplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.junit.jupiter.RobolectricExtension;

/**
 * A module can mix JUnit 4 and JUnit Jupiter test classes: JUnit Vintage runs the JUnit 4 ones, the
 * robolectric engine runs the Jupiter ones, and tests with the same configuration share a sandbox.
 */
class MixedJUnit4AndJupiterTest {
  private static final String CLASS_LOADER_PROPERTY = "robolectric.integrationtests.classLoader.";

  @Test
  void junit4AndJupiterClasses_runOnTheirEngines_andShareSandbox() {
    System.clearProperty(CLASS_LOADER_PROPERTY + JUnit4Test.class.getName());
    System.clearProperty(CLASS_LOADER_PROPERTY + JupiterTest.class.getName());
    Map<String, String> enginesByClass = new HashMap<>();

    LauncherFactory.create()
        .execute(
            LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(JUnit4Test.class), selectClass(JupiterTest.class))
                .build(),
            new TestExecutionListener() {
              @Override
              public void executionFinished(TestIdentifier test, TestExecutionResult result) {
                if (test.isTest() && test.getSource().orElse(null) instanceof MethodSource) {
                  assertThat(result.getStatus()).isEqualTo(TestExecutionResult.Status.SUCCESSFUL);
                  enginesByClass.put(
                      ((MethodSource) test.getSource().get()).getClassName(),
                      test.getUniqueIdObject().getEngineId().orElseThrow());
                }
              }
            });

    // Neither runs on CI shards that don't enable the newest SDK.
    assumeFalse(enginesByClass.isEmpty());
    assertThat(enginesByClass)
        .containsExactly(
            JUnit4Test.class.getName(), "junit-vintage",
            JupiterTest.class.getName(), "robolectric");
    assertThat(System.getProperty(CLASS_LOADER_PROPERTY + JUnit4Test.class.getName()))
        .isEqualTo(System.getProperty(CLASS_LOADER_PROPERTY + JupiterTest.class.getName()));
  }

  private static void recordClassLoader(Class<?> testClass) {
    // System properties are shared by all class loaders, unlike the sandboxed classes' statics.
    System.setProperty(
        CLASS_LOADER_PROPERTY + testClass.getName(),
        Integer.toHexString(System.identityHashCode(testClass.getClassLoader())));
  }

  @RunWith(RobolectricTestRunner.class)
  @Config(sdk = Config.NEWEST_SDK)
  public static class JUnit4Test {
    @org.junit.Test
    public void recordsSandbox() {
      recordClassLoader(JUnit4Test.class);
    }
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = Config.NEWEST_SDK)
  static class JupiterTest {
    @Test
    void recordsSandbox() {
      recordClassLoader(JupiterTest.class);
    }
  }
}
