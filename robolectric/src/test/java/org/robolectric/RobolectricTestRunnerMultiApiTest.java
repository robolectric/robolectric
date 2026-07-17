package org.robolectric;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.plugins.DefaultSdkPicker;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.util.TestUtil;
import org.robolectric.util.inject.Injector;

@RunWith(JUnit4.class)
public class RobolectricTestRunnerMultiApiTest {

  private static final int[] APIS_FOR_TEST = {P, Q, R, S};

  private static SdkPicker delegateSdkPicker;
  private static final Injector INJECTOR =
      defaultInjector()
          .bind(SdkPicker.class, (config, usesSdk) -> delegateSdkPicker.selectSdks(config, usesSdk))
          .build();

  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  private RobolectricTestRunner runner;
  private RunNotifier runNotifier;
  private MyRunListener runListener;

  private int numSupportedApis;

  @Before
  public void setUp() {
    numSupportedApis = APIS_FOR_TEST.length;

    runListener = new MyRunListener();
    runNotifier = new RunNotifier();
    runNotifier.addListener(runListener);
    SdkCollection sdkCollection = new SdkCollection(() -> map(APIS_FOR_TEST));
    delegateSdkPicker = new DefaultSdkPicker(sdkCollection, null);
    setSystemPropertyRule.clear("robolectric.alwaysIncludeVariantMarkersInTestName");
  }

  @Test
  public void createChildrenForEachSupportedApi() throws Throwable {
    runner = runnerOf(TestWithNoConfig.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q, R, S);
  }

  @Test
  public void withConfigSdkLatest_shouldUseLatestSupported() throws Throwable {
    runner = runnerOf(TestMethodWithNewestSdk.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(S);
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
                  + " (sdk=[28], minSdk=28, maxSdk=29)");
    }
  }

  @Test
  public void withEnabledSdks_createChildrenForEachSupportedSdk() throws Throwable {
    delegateSdkPicker = new DefaultSdkPicker(new SdkCollection(() -> map(28, 30)), null);

    runner = runnerOf(TestWithNoConfig.class);
    assertThat(runner.getChildren()).hasSize(2);
  }

  @Test
  public void shouldAddApiLevelToNameOfAllButHighestNumberedMethodName() throws Throwable {
    runner = runnerOf(TestMethodUpToAndIncludingQ.class);
    assertThat(runner.getChildren().get(0).getName()).isEqualTo("testSomeApiLevel[28]");
    assertThat(runner.getChildren().get(1).getName()).isEqualTo("testSomeApiLevel");
  }

  @Test
  public void noConfig() throws Throwable {
    runner = runnerOf(TestWithNoConfig.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q, R, S);
    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    assertThat(runListener.finished).hasSize(numSupportedApis);
  }

  @Test
  public void classConfigWithSdkGroup() throws Throwable {
    runner = runnerOf(TestClassConfigWithSdkGroup.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for P and Q
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void methodConfigWithSdkGroup() throws Throwable {
    runner = runnerOf(TestMethodConfigWithSdkGroup.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Test method should be run for P, Q
    assertThat(runListener.finished).hasSize(2);
  }

  @Test
  public void classConfigMinSdk() throws Throwable {
    runner = runnerOf(TestClassQAndUp.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(Q, R, S);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingQ = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingQ);
  }

  @Test
  public void classConfigMaxSdk() throws Throwable {
    runner = runnerOf(TestClassUpToAndIncludingQ.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingQ = 2;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingQ);
  }

  @Test
  public void classConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = runnerOf(TestClassBetweenPAndQ.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    // Since test method should only be run once
    int sdksInclusivelyInRange = 2;
    assertThat(runListener.finished).hasSize(sdksInclusivelyInRange);
  }

  @Test
  public void methodConfigMinSdk() throws Throwable {
    runner = runnerOf(TestMethodQAndUp.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(Q, R, S);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksAfterAndIncludingQ = 3;
    assertThat(runListener.finished).hasSize(sdksAfterAndIncludingQ);
  }

  @Test
  public void methodConfigMaxSdk() throws Throwable {
    runner = runnerOf(TestMethodUpToAndIncludingQ.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksUpToAndIncludingQ = 2;
    assertThat(runListener.finished).hasSize(sdksUpToAndIncludingQ);
  }

  @Test
  public void methodConfigWithMinSdkAndMaxSdk() throws Throwable {
    runner = runnerOf(TestMethodBetweenPAndQ.class);
    assertThat(apisFor(runner.getChildren())).containsExactly(P, Q);

    runner.run(runNotifier);

    assertThat(runListener.ignored).isEmpty();
    int sdksInclusivelyInRange = 2;
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

  @Config(sdk = {P, Q})
  public static class TestClassConfigWithSdkGroup {
    @Test
    public void testShouldRunApi28() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(P, Q));
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodConfigWithSdkGroup {
    @Config(sdk = {P, Q})
    @Test
    public void testShouldRunApi28() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(P, Q));
    }
  }

  @Config(minSdk = Q)
  public static class TestClassQAndUp {
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtLeast(Q);
    }
  }

  @Config(maxSdk = Q)
  public static class TestClassUpToAndIncludingQ {
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtMost(Q);
    }
  }

  @Config(minSdk = P, maxSdk = Q)
  public static class TestClassBetweenPAndQ {
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(P, Q));
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodQAndUp {
    @Config(minSdk = Q)
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtLeast(Q);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodUpToAndIncludingQ {
    @Config(maxSdk = Q)
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isAtMost(Q);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodBetweenPAndQ {
    @Config(minSdk = P, maxSdk = Q)
    @Test
    public void testSomeApiLevel() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(P, Q));
    }
  }

  public static class TestMethodWithNewestSdk {
    @Config(sdk = Config.NEWEST_SDK)
    @Test
    public void testWithLatest() {
      assertThat(Build.VERSION.SDK_INT).isEqualTo(S);
    }
  }

  @Config(sdk = Config.ALL_SDKS)
  public static class TestMethodWithSdkAndMinMax {
    @Config(sdk = P, minSdk = P, maxSdk = Q)
    @Test
    public void testWithSdkRange() {
      assertThat(Build.VERSION.SDK_INT).isIn(Range.closed(P, Q));
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
    private final List<String> started = new ArrayList<>();
    private final List<String> finished = new ArrayList<>();
    private final List<String> ignored = new ArrayList<>();

    @Override
    public void testStarted(Description description) {
      started.add(description.getDisplayName());
    }

    @Override
    public void testFinished(Description description) {
      finished.add(description.getDisplayName());
    }

    @Override
    public void testIgnored(Description description) {
      ignored.add(description.getDisplayName());
    }
  }

  private List<Sdk> map(int... sdkInts) {
    SdkCollection allSdks = TestUtil.getSdkCollection();
    return Arrays.stream(sdkInts).mapToObj(allSdks::getSdk).collect(Collectors.toList());
  }
}
