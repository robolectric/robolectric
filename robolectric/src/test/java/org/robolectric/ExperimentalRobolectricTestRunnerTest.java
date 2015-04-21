package org.robolectric;


import android.os.Build;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class ExperimentalRobolectricTestRunnerTest {

  private int numSupportedApis;

  @Before
  public void setUp() {
    numSupportedApis = SdkConfig.getSupportedApis().size();
  }

  @Test
  public void createChildrenForEachSupportedApi() throws Throwable {
    ExperimentalRobolectricTestRunner runner = new ExperimentalRobolectricTestRunner(TestWithNoConfig.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    for (Runner o : runner.getChildren()) {
      assertThat(o.testCount()).isEqualTo(1);
    }
  }

  @Test
  public void noConfig() throws Throwable {
    ExperimentalRobolectricTestRunner runner = new ExperimentalRobolectricTestRunner(TestWithNoConfig.class);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    verify(runListener, times(numSupportedApis)).testFinished(any(Description.class));
  }

  @Test
  public void classConfig() throws Throwable {
    ExperimentalRobolectricTestRunner runner = new ExperimentalRobolectricTestRunner(TestWithClassConfig.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    // The test should be ignored on all but the single API runner that corresponds to its
    // @Config value
    verify(runListener, times(4)).testIgnored(any(Description.class));
    verify(runListener, times(1)).testFinished(any(Description.class));
  }

  @Test
  public void methodConfig() throws Throwable {
    ExperimentalRobolectricTestRunner runner = new ExperimentalRobolectricTestRunner(TestWithMethodConfig.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    // Each one of the five methods should be ignored for all runners except the one for the
    // corresponding API level
    verify(runListener, times(20)).testIgnored(any(Description.class));
    verify(runListener, times(5)).testFinished(any(Description.class));
  }

  @RunWith(ExperimentalRobolectricTestRunner.class)
  public class TestWithNoConfig {

    @Test public void test() {}
  }

  @RunWith(ExperimentalRobolectricTestRunner.class)
  @Config(emulateSdk = 18)
  public class TestWithClassConfig {

    @Test public void testShouldRunApi18() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(18);
    }
  }

  @RunWith(ExperimentalRobolectricTestRunner.class)
  public class TestWithMethodConfig {

    @Config(emulateSdk = 16)
    @Test public void testShouldRunApi16() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(16);
    }

    @Config(emulateSdk = 17)
    @Test public void testShouldRunApi17() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(17);
    }

    @Config(emulateSdk = 18)
    @Test public void testShouldRunApi18() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(18);
    }

    @Config(emulateSdk = 19)
    @Test public void testShouldRunApi19() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(19);
    }

    @Config(emulateSdk = 21)
    @Test public void testShouldRunApi21() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(21);
    }
  }

}
