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

import static android.os.Build.VERSION_CODES.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(JUnit4.class)
public class MultiApiRobolectricTestRunnerTest {

  private int numSupportedApis;

  @Before
  public void setUp() {
    numSupportedApis = SdkConfig.getSupportedApis().size();
  }

  @Test
  public void createChildrenForEachSupportedApi() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestWithNoConfig.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    for (Runner o : runner.getChildren()) {
      assertThat(o.testCount()).isEqualTo(1);
    }
  }

  @Test
  public void noConfig() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestWithNoConfig.class);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    verify(runListener, times(numSupportedApis)).testFinished(any(Description.class));
  }

  @Test
  public void classConfigWithSdkGroup() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestClassConfigWithSdkGroup.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    // Test method should be run for JellyBean and Lollipop
    verify(runListener, times(2)).testFinished(any(Description.class));
  }

  @Test
  public void methodConfigWithSdkGroup() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestMethodConfigWithSdkGroup.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    // Test method should be run for JellyBean and Lollipop
    verify(runListener, times(2)).testFinished(any(Description.class));
  }

  @Test
  public void classConfigMinSdk() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestClassLollipopAndUp.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    int sdksAfterAndIncludingLollipop = 3;
    verify(runListener, times(sdksAfterAndIncludingLollipop)).testFinished(any(Description.class));
  }

  @Test
  public void classConfigMaxSdk() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestClassUpToAndIncludingLollipop.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    int sdksUpToAndIncludingLollipop = 5;
    verify(runListener, times(sdksUpToAndIncludingLollipop)).testFinished(any(Description.class));
  }

  @Test
  public void classConfigWithMinSdkAndMaxSdk() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestClassBetweenJellyBeanMr2AndLollipop.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    // Since test method should only be run once
    int sdksInclusivelyBetweenJellyBeanMr2AndLollipop = 3;
    verify(runListener, times(sdksInclusivelyBetweenJellyBeanMr2AndLollipop)).testFinished(any(Description.class));
  }

  @Test
  public void methodConfigMinSdk() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestMethodLollipopAndUp.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    int sdksAfterAndIncludingLollipop = 3;
    verify(runListener, times(sdksAfterAndIncludingLollipop)).testFinished(any(Description.class));
  }

  @Test
  public void methodConfigMaxSdk() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestMethodUpToAndIncludingLollipop.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    int sdksUpToAndIncludingLollipop = 5;
    verify(runListener, times(sdksUpToAndIncludingLollipop)).testFinished(any(Description.class));
  }

  @Test
  public void methodConfigWithMinSdkAndMaxSdk() throws Throwable {
    MultiApiRobolectricTestRunner runner = new MultiApiRobolectricTestRunner(TestMethodBetweenJellyBeanMr2AndLollipop.class);

    assertThat(runner.getChildren()).hasSize(numSupportedApis);

    RunNotifier runNotifier = new RunNotifier();
    RunListener runListener = mock(RunListener.class);
    runNotifier.addListener(runListener);
    runner.run(runNotifier);

    verify(runListener, never()).testIgnored(any(Description.class));
    int sdksInclusivelyBetweenJellyBeanMr2AndLollipop = 3;
    verify(runListener, times(sdksInclusivelyBetweenJellyBeanMr2AndLollipop)).testFinished(any(Description.class));
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public class TestWithNoConfig {

    @Test public void test() {}
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(sdk = {JELLY_BEAN, LOLLIPOP})
  public class TestClassConfigWithSdkGroup {

    @Test public void testShouldRunApi18() {
      assertThat(Build.VERSION.SDK_INT).isIn(JELLY_BEAN, LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public class TestMethodConfigWithSdkGroup {

    @Config(sdk = {JELLY_BEAN, LOLLIPOP})
    @Test public void testShouldRunApi16() {
      assertThat(Build.VERSION.SDK_INT).isIn(JELLY_BEAN, LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(minSdk = LOLLIPOP)
  public class TestClassLollipopAndUp {

    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isGreaterThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(maxSdk = LOLLIPOP)
  public class TestClassUpToAndIncludingLollipop {

    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isLessThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  @Config(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP)
  public class TestClassBetweenJellyBeanMr2AndLollipop {

    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isBetween(JELLY_BEAN_MR2, LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public class TestMethodLollipopAndUp {

    @Config(minSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isGreaterThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public class TestMethodUpToAndIncludingLollipop {

    @Config(maxSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isLessThanOrEqualTo(LOLLIPOP);
    }
  }

  @RunWith(MultiApiRobolectricTestRunner.class)
  public class TestMethodBetweenJellyBeanMr2AndLollipop {

    @Config(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isBetween(JELLY_BEAN_MR2, LOLLIPOP);
    }
  }
}
