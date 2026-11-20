package org.robolectric.integrationtests.junitplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.robolectric.annotation.Config;
import org.robolectric.junit.jupiter.RobolectricExtension;

/**
 * A Jupiter class that still extends JUnit 3's {@link TestCase}, e.g. for its assertions, runs only
 * on the robolectric engine, although JUnit Vintage also finds it as a JUnit 3 suite.
 */
class TestCaseSubclassTest {
  @Test
  void testCaseSubclass_runsOnlyOnRobolectricEngine() {
    List<String> results = new ArrayList<>();

    LauncherFactory.create()
        .execute(
            LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(ExtendsTestCase.class))
                .build(),
            new TestExecutionListener() {
              @Override
              public void executionFinished(TestIdentifier test, TestExecutionResult result) {
                if (test.isTest()) {
                  results.add(
                      test.getUniqueIdObject().getEngineId().orElseThrow()
                          + " "
                          + ((MethodSource) test.getSource().orElseThrow()).getMethodName()
                          + " "
                          + result.getStatus());
                }
              }
            });

    // Nothing runs on CI shards that don't enable the newest SDK.
    assumeFalse(results.isEmpty());
    assertThat(results).containsExactly("robolectric runs SUCCESSFUL");
  }

  @ExtendWith(RobolectricExtension.class)
  @Config(sdk = Config.NEWEST_SDK)
  public static class ExtendsTestCase extends TestCase {
    @Test
    void runs() {
      assertTrue(getClass().getClassLoader().getClass().getName().contains("Sandbox"));
    }

    // JUnit 3 would report this as a failing "warning" test, since it is not public.
    @SuppressWarnings("unused")
    private void testHelper() {}
  }
}
