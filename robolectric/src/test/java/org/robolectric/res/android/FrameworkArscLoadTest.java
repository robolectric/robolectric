package org.robolectric.res.android;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Properties;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.util.inject.Injector;

@RunWith(JUnit4.class)
public class FrameworkArscLoadTest {
  private static final boolean RUN_BENCHMARK = false;

  @Test
  public void frameworkArscLoad() throws Exception {
    Assume.assumeTrue(RUN_BENCHMARK);

    Sdk sdk = latestSdk();
    File jar = sdk.getJarPath().toFile();
    System.out.printf("Using SDK level: API %d%n", sdk.getApiLevel());

    String jarPath = jar.getAbsolutePath();
    System.out.printf(
        "number of arsc packages in binary resources: %d%n",
        CppApkAssets.Load(jarPath, true).GetLoadedArsc().GetPackages().size());

    measure(
        "FULL load (CppApkAssets.Load: zip open + inflate + parse)",
        100,
        () -> {
          int unused = CppApkAssets.Load(jarPath, true).GetLoadedArsc().GetPackages().size();
        });
  }

  /** Locates the latest known SDK via Robolectric's SdkProvider plugin. */
  private static Sdk latestSdk() {
    Injector injector =
        new Injector.Builder().bind(Properties.class, System.getProperties()).build();
    SdkProvider sdkProvider = injector.getInstance(SdkProvider.class);
    return sdkProvider.getSdks().stream()
        .filter(Sdk::isSupported)
        .max(Comparator.naturalOrder())
        .orElseThrow(() -> new IllegalStateException("no supported SDKs from " + sdkProvider));
  }

  private static void measure(String label, int iterations, Runnable work) {
    long[] samples = new long[iterations];
    long start = System.nanoTime();
    for (int i = 0; i < iterations; i++) {
      long t0 = System.nanoTime();
      work.run();
      samples[i] = System.nanoTime() - t0;
    }
    long elapsed = System.nanoTime() - start;
    Arrays.sort(samples);
    System.out.printf(
        "%n[%s]%n  %d iters: mean=%.3f ms  p50=%.3f ms  p90=%.3f ms  min=%.3f ms  total=%.1f ms%n",
        label,
        iterations,
        elapsed / 1_000_000.0 / iterations,
        samples[iterations / 2] / 1_000_000.0,
        samples[(int) (iterations * 0.90)] / 1_000_000.0,
        samples[0] / 1_000_000.0,
        elapsed / 1_000_000.0);
  }
}
