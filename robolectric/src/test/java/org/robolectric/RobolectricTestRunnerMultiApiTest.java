package org.robolectric;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.RobolectricTestRunner.defaultInjector;

import android.os.Build;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.plugins.DefaultSdkPicker;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.TestUtil;
import org.robolectric.util.inject.Injector;

@RunWith(JUnit4.class)
public class RobolectricTestRunnerMultiApiTest {

  private static final int[] APIS_FOR_TEST = {LOLLIPOP, LOLLIPOP_MR1, M, N, N_MR1, O};

  private static SdkPicker delegateSdkPicker;
  private static final Injector INJECTOR =
      defaultInjector()
          .bind(SdkPicker.class, (config, usesSdk) -> delegateSdkPicker.selectSdks(config, usesSdk))
          .build();

  private RobolectricTestRunner runner;
  private RunNotifier runNotifier;
  private MyRunListener runListener;

  private int numSupportedApis;
  private String priorResourcesMode;
  private String priorAlwaysInclude;

  private SdkCollection sdkCollection;

  @Before
  public void setUp() {
    numSupportedApis = APIS_FOR_TEST.length;

    runListener = new MyRunListener();
    runNotifier = new RunNotifier();
    runNotifier.addListener(runListener);
    sdkCollection = new SdkCollection(() -> map(APIS_FOR_TEST));
    delegateSdkPicker = new DefaultSdkPicker(sdkCollection, null);

    priorResourcesMode = System.getProperty("robolectric.resourcesMode");

    priorAlwaysInclude = System.getProperty("robolectric.alwaysIncludeVariantMarkersInTestName");
    System.clearProperty("robolectric.alwaysIncludeVariantMarkersInTestName");
  }

  @After
  public void tearDown() throws Exception {
    TestUtil.resetSystemProperty(
        "robolectric.alwaysIncludeVariantMarkersInTestName", priorAlwaysInclude);
  }

  @Test
  public void createChildrenForEachSupportedApi() throws Throwable {
    runner = runnerOf(TestWithNoConfig.class);
    assertThat(apisFor(runner.getChildren()))
        .containsExactly(LOLLIPOP, LOLLIPOP_MR1, M, N, N_MR1, O);
  }

  @Test
  public void withConfigSdkLatest_shouldUseLatestSupported() throws Throwable {
    runner = runnerOf(TestMethodWithNewestSdk.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(O);
  }

  @Test
  public void withConfigSdkAndMinMax_shouldUseMinMax() throws Throwable {
    runner = runnerOf(TestMethodWithSdkAndMinMax.class);
    try {
      runner.getChildren();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage())
          .contains(
              "sdk and minSdk/maxSdk may not be specified together"
                  + " (sdk=[23], minSdk=23, maxSdk=24)");
    }
  }

  @Test
  public void withEnabledSdks_createChildrenForEachSupportedSdk() throws Throwable {
    delegateSdkPicker = new DefaultSdkPicker(new SdkCollection(() -> map(21, 23)), null);

    runner = runnerOf(TestWithNoConfig.class);
    assertThat(runner.getChildren()).hasSize(2);
  }

  @Test
  public void shouldAddApiLevelToNameOfAllButHighestNumberedMethodName() throws Throwable {
    runner = runnerOf(TestMethodUpToAndIncludingN.class);
    assertThat(runner.getChildren().get(0).getName()).isEqualTo("testSomeApiLevel[21]");
    assertThat(runner.getChildren().get(1).getName()).isEqualTo("testSomeApiLevel[22]");
    assertThat(runner.getChildren().get(2).getName()).isEqualTo("testSomeApiLevel[23]");
    assertThat(runner.getChildren().get(3).getName()).isEqualTo("testSomeApiLevel");
  }

  @Test
  public void noConfig() throws Throwable {
    runner = runnerOf(TestWithNoConfig.class);
    assertThat(apisFor(runner.getChildren()))
        .containsExactly(LOLLIPOP, LOLLIPOP_MR1, M, N, N_MR1, O);
    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    assertThat(runListener.finished).hasSize(numSupportedApis);
  }

  @Test
  public void classConfigWithSdkGroup() throws Throwable {
    runner = runnerOf(TestClassConfigWithSdkGroup.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(M, N);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for M and N
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void methodConfigWithSdkGroup() throws Throwable {
    runner = runnerOf(TestMethodConfigWithSdkGroup.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(M, N);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for M, N
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void classConfigMinSdk() throws Throwable {
    runner = runnerOf(TestClassNAndUp.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(N, N_MR1, O);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingLollipop);
  }

  @Test
  public void classConfigMaxSdk() throws Throwable {
    runner = runnerOf(TestClassUpToAndIncludingN.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP, LOLLIPOP_MR1, M, N);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingLollipop = 4;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingLollipop);
  }

  @Test
  public void classConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = runnerOf(TestClassBetweenLollipopMr1AndN.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP_MR1, M, N);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Since test method should only be run once
    int sdksInclusivelyInRange = 3;
    assertThat(runListener.finished).hasSize(sdksInclusivelyInRange);
  }

  @Test
  public void methodConfigMinSdk() throws Throwable {
    runner = runnerOf(TestMethodNAndUp.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(N, N_MR1, O);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingLollipop = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingLollipop);
  }

  @Test
  public void methodConfigMaxSdk() throws Throwable {
    runner = runnerOf(TestMethodUpToAndIncludingN.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP, LOLLIPOP_MR1, M, N);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingLollipop = 4;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingLollipop);
  }

  @Test
  public void methodConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = runnerOf(TestMethodBetweenLollipopMr1AndN.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(LOLLIPOP_MR1, M, N);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksInclusivelyInRange = 3;
    assertThat(runListener.finished).hasSize(sdksInclusivelyInRange);
  }

  ///////////////////////////

  @Nonnull
  private RobolectricTestRunner runnerOf(Class<?> testClass) throws InitializationError {
    return new RobolectricTestRunner(testClass, INJECTOR);
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestWithNoConfig {
    @Test
    public void test() {}
  }

  @Config(sdk = {M, N})
  public static class TestClassConfigWithSdkGroup {
    @Test
    public void testShouldRunApi18() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(M, N));
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodConfigWithSdkGroup {
    @Config(sdk = {M, N})
    @Test
    public void testShouldRunApi16() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(M, N));
    }
  }

  @Config(minSdk = N)
  public static class TestClassNAndUp {
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtLeast(N);
    }
  }

  @Config(maxSdk = N)
  public static class TestClassUpToAndIncludingN {
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtMost(N);
    }
  }

  @Config(minSdk = LOLLIPOP_MR1, maxSdk = N)
  public static class TestClassBetweenLollipopMr1AndN {
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(LOLLIPOP_MR1, N));
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodNAndUp {
    @Config(minSdk = N)
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtLeast(N);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodUpToAndIncludingN {
    @Config(maxSdk = N)
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtMost(N);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodBetweenLollipopMr1AndN {
    @Config(minSdk = LOLLIPOP_MR1, maxSdk = N)
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(LOLLIPOP_MR1, N));
    }
  }

  public static class TestMethodWithNewestSdk {
    @Config(sdk = Config.NEWEST_SDK)
    @Test
    public void testWithLatest() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(O);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodWithSdkAndMinMax {
    @Config(sdk = M, minSdk = M, maxSdk = N)
    @Test
    public void testWithSdkRange() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(M, N));
    }
  }

  private static List<Integer> apisFor(List<FrameworkMethod> children) {
    List<Integer> apis = new ArrayList<>();
    for (FrameworkMethod child : children) {
      apis.add(((RobolectricTestRunner.RobolectricFrameworkMethod) child).getSdk().getApiLevel());
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

  private List<Sdk> map(int... sdkInts) {
    SdkCollection allSdks = TestUtil.getSdkCollection();
    return Arrays.stream(sdkInts).mapToObj(allSdks::getSdk).collect(Collectors.toList());
  }
}
