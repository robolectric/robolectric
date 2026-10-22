package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.res.android.ResourceTypes.Res_value;
import org.robolectric.util.inject.Injector;

/**
 * Benchmark for {@link CppAssetManager2#ResolveReference} with and without its resolved-value
 * cache.
 */
@RunWith(JUnit4.class)
public class ResolveReferenceBenchmarkTest {
  private static final int ROUNDS = 200;

  private CppAssetManager2 assetManager;
  private int[] refResIds;

  @Before
  public void setUp() {
    Sdk sdk =
        new Injector.Builder()
                .bind(Properties.class, System.getProperties())
                .build()
                .getInstance(SdkProvider.class)
                .getSdks()
                .stream()
                .filter(Sdk::isSupported)
                .max(Comparator.naturalOrder())
                .get();

    File jar = sdk.getJarPath().toFile();
    assetManager = new CppAssetManager2();
    assetManager.SetApkAssets(
        ImmutableList.of(CppApkAssets.Load(jar.getAbsolutePath(), true)), true);

    // Collect all framework (0x01) resources whose value is a reference to another resource.
    List<Integer> refs = new ArrayList<>();
    Ref<Res_value> value = new Ref<>(null);
    Ref<ResTable_config> config = new Ref<>(null);
    Ref<Integer> flags = new Ref<>(0);
    for (int typeId = 1; typeId <= 0xff; typeId++) {
      int consecutiveMisses = 0;
      for (int entryId = 0; entryId <= 0xffff && consecutiveMisses < 256; entryId++) {
        int resId = 0x01000000 | (typeId << 16) | entryId;
        if (assetManager.GetResource(resId, true, (short) 0, value, config, flags).intValue() < 0) {
          consecutiveMisses++;
          continue;
        }
        consecutiveMisses = 0;
        if (value.get().dataType == Res_value.TYPE_REFERENCE
            && value.get().data != 0
            && value.get().data != resId) {
          refs.add(resId);
        }
      }
    }
    refResIds = refs.stream().mapToInt(Integer::intValue).toArray();
    assertThat(refResIds).isNotEmpty();
  }

  @Ignore("Benchmark; run manually")
  @Test
  public void benchmarkResolveReference() {
    // Warmup JIT
    for (int i = 0; i < 50; i++) {
      resolveAll(false);
      resolveAll(true);
    }

    long before = time(() -> resolveAll(false));
    long after = time(() -> resolveAll(true));

    System.out.printf(
        "%n[ResolveReference Benchmark] %d framework references, %d rounds%n",
        refResIds.length, ROUNDS);
    System.out.printf(
        "  Before (cacheValue=false): %8.3f ms (%d ns/resolve)%n",
        before / 1e6, before / (ROUNDS * refResIds.length));
    System.out.printf(
        "  After  (cacheValue=true):  %8.3f ms (%d ns/resolve)%n",
        after / 1e6, after / (ROUNDS * refResIds.length));
    System.out.printf("  Speedup: %.1fx%n%n", (double) before / after);
  }

  private long time(Runnable work) {
    long[] samples = new long[ROUNDS];
    for (int i = 0; i < ROUNDS; i++) {
      long t0 = System.nanoTime();
      work.run();
      samples[i] = System.nanoTime() - t0;
    }
    Arrays.sort(samples);
    return samples[ROUNDS / 2];
  }

  private void resolveAll(boolean cacheValue) {
    Ref<Res_value> value = new Ref<>(null);
    Ref<ResTable_config> config = new Ref<>(new ResTable_config());
    for (int resId : refResIds) {
      value.set(new Res_value((byte) Res_value.TYPE_REFERENCE, resId));
      assetManager.resolveReference(
          ApkAssetsCookie.forInt(0), value, config, null, null, cacheValue);
    }
  }
}
