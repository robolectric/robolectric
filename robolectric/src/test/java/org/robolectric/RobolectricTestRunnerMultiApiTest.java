package org.robolectric;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.os.Build;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.util.TestUtil;

@RunWith(JUnit4.class)
public class RobolectricTestRunnerMultiApiTest {

  private final static int[] APIS_FOR_TEST = {
      JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP, LOLLIPOP_MR1, M,
  };

  private RobolectricTestRunner runner;
  private RunNotifier runNotifier;
  private MyRunListener runListener;

  private int numSupportedApis;
  private SdkPicker sdkPicker;
  private String priorResourcesMode;
  private String priorAlwaysInclude;

  @Before
  public void setUp() {
    numSupportedApis = APIS_FOR_TEST.length;

    runListener = new MyRunListener();
    runNotifier = new RunNotifier();
    runNotifier.addListener(runListener);
    sdkPicker = new SdkPicker(SdkPicker.map(APIS_FOR_TEST), null);

    priorResourcesMode = System.getProperty("robolectric.resourcesMode");
    System.setProperty("robolectric.resourcesMode", "legacy");

    priorAlwaysInclude = System.getProperty("robolectric.alwaysIncludeVariantMarkersInTestName");
    System.clearProperty("robolectric.alwaysIncludeVariantMarkersInTestName");
  }

  @After
  public void tearDown() throws Exception {
    TestUtil.resetSystemProperty(
        "robolectric.alwaysIncludeVariantMarkersInTestName", priorAlwaysInclude);
    TestUtil.resetSystemProperty("robolectric.resourcesMode", priorResourcesMode);
  }

  @Test
  public void createChildrenForEachSupportedApi() throws Throwable {
    runner = runnerOf(TestWithNoConfig.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(
        JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP, LOLLIPOP_MR1, M);
  }

  @Test
  public void withConfigSdkLatest_shouldUseLatestSupported() throws Throwable {
    runner = runnerOf(TestMethodWithNewestSdk.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(M);
  }

  @Test
  public void withConfigSdkAndMinMax_shouldUseMinMax() throws Throwable {
    runner = runnerOf(TestMethodWithSdkAndMinMax.class);
    try {
      runner.getChildren();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).contains("sdk and minSdk/maxSdk may not be specified together" +
          " (sdk=[16], minSdk=19, maxSdk=21)");
    }
  }

  @Test
  public void withEnabledSdks_createChildrenForEachSupportedSdk() throws Throwable {
    sdkPicker = new SdkPicker(SdkPicker.map(16, 17), null);

    runner = runnerOf(TestWithNoConfig.class);
    assertThat(runner.getChildren()).hasSize(2);
  }

  @Test
  public void shouldAddApiLevelToNameOfAllButHighestNumberedMethodName() throws Throwable {
    runner = runnerOf(TestMethodUpToAndIncludingLollipop.class);
    assertThat(runner.getChildren().get(0).getName()).isEqualTo("testSomeApiLevel[16]");
    assertThat(runner.getChildren().get(1).getName()).isEqualTo("testSomeApiLevel[17]");
    assertThat(runner.getChildren().get(2).getName()).isEqualTo("testSomeApiLevel[18]");
    assertThat(runner.getChildren().get(3).getName()).isEqualTo("testSomeApiLevel[19]");
    assertThat(runner.getChildren().get(4).getName()).isEqualTo("testSomeApiLevel");
  }

  @Test
  public void noConfig() throws Throwable {
    runner = runnerOf(TestWithNoConfig.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(
        JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP, LOLLIPOP_MR1, M);
    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    assertThat(runListener.finished).hasSize(numSupportedApis);
  }

  @Test
  public void classConfigWithSdkGroup() throws Throwable {
    runner = runnerOf(TestClassConfigWithSdkGroup.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for JellyBean and Lollipop
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void methodConfigWithSdkGroup() throws Throwable {
    runner = runnerOf(TestMethodConfigWithSdkGroup.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for JellyBean and Lollipop
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void classConfigMinSdk() throws Throwable {
    runner = runnerOf(TestClassLollipopAndUp.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP, LOLLIPOP_MR1, M);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingLollipop);
  }

  @Test
  public void classConfigMaxSdk() throws Throwable {
    runner = runnerOf(TestClassUpToAndIncludingLollipop.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingLollipop = 5;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingLollipop);
  }

  @Test
  public void classConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = runnerOf(TestClassBetweenJellyBeanMr2AndLollipop.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Since test method should only be run once
    int sdksInclusivelyBetweenJellyBeanMr2AndLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksInclusivelyBetweenJellyBeanMr2AndLollipop);
  }

  @Test
  public void methodConfigMinSdk() throws Throwable {
    runner = runnerOf(TestMethodLollipopAndUp.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP, LOLLIPOP_MR1, M);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingLollipop);
  }

  @Test
  public void methodConfigMaxSdk() throws Throwable {
    runner = runnerOf(TestMethodUpToAndIncludingLollipop.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingLollipop = 5;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingLollipop);
  }

  @Test
  public void methodConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = runnerOf(TestMethodBetweenJellyBeanMr2AndLollipop.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(JELLY_BEAN_MR2, KITKAT, LOLLIPOP);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksInclusivelyBetweenJellyBeanMr2AndLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksInclusivelyBetweenJellyBeanMr2AndLollipop);
  }

  ///////////////////////////

  @Nonnull
  private RobolectricTestRunner runnerOf(Class<?> testClass) throws InitializationError {
    return new RobolectricTestRunner(testClass) {
      @Nonnull @Override
      protected SdkPicker createSdkPicker() {
        return sdkPicker;
      }
    };
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestWithNoConfig {
    @Test public void test() {}
  }

  @Config(sdk = {JELLY_BEAN, LOLLIPOP})
  public static class TestClassConfigWithSdkGroup {
    @Test public void testShouldRunApi18() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(JELLY_BEAN, LOLLIPOP));
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodConfigWithSdkGroup {
    @Config(sdk = {JELLY_BEAN, LOLLIPOP})
    @Test public void testShouldRunApi16() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(JELLY_BEAN, LOLLIPOP));
    }
  }

  @Config(minSdk = LOLLIPOP)
  public static class TestClassLollipopAndUp {
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtLeast(LOLLIPOP);
    }
  }

  @Config(maxSdk = LOLLIPOP)
  public static class TestClassUpToAndIncludingLollipop {
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtMost(LOLLIPOP);
    }
  }

  @Config(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP)
  public static class TestClassBetweenJellyBeanMr2AndLollipop {
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(JELLY_BEAN_MR2, LOLLIPOP));
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodLollipopAndUp {
    @Config(minSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtLeast(LOLLIPOP);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodUpToAndIncludingLollipop {
    @Config(maxSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtMost(LOLLIPOP);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodBetweenJellyBeanMr2AndLollipop {
    @Config(minSdk = JELLY_BEAN_MR2, maxSdk = LOLLIPOP)
    @Test public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(JELLY_BEAN_MR2, LOLLIPOP));
    }
  }

  public static class TestMethodWithNewestSdk {
    @Config(sdk = Config.NEWEST_SDK)
    @Test
    public void testWithLatest() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(M);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodWithSdkAndMinMax {
    @Config(sdk = JELLY_BEAN, minSdk = KITKAT, maxSdk = LOLLIPOP)
    @Test public void testWithKitKatAndLollipop() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(KITKAT, LOLLIPOP));
    }
  }

  private static List<Integer> apisFor(List<FrameworkMethod> children) {
    List<Integer> apis = new ArrayList<>();
    for (FrameworkMethod child : children) {
      apis.add(
          ((RobolectricTestRunner.RobolectricFrameworkMethod) child).sdkConfig.getApiLevel());
    }
    return apis;
  }

  private static class MyRunListener extends RunListener {
    private List<String> started = new ArrayList<>();
    private List<String> finished = new ArrayList<>();
    private List<String> ignored = new ArrayList<>();

    @Override
    public void testStarted(Description description) throws Exception {
      started.add(description.getDisplayName());
    }

    @Override
    public void testFinished(Description description) throws Exception {
      finished.add(description.getDisplayName());
    }

    @Override
    public void testIgnored(Description description) throws Exception {
      ignored.add(description.getDisplayName());
    }
  }
}
