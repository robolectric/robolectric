package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class DefaultNativeRuntimeLoaderTest {
  ExecutorService executor = Executors.newSingleThreadExecutor();

  @Before
  public void setUp() {
    DefaultNativeRuntimeLoader.resetLoaded();
  }

  @Test
  public void concurrentLoad() {
    //noinspection resource
    executor.execute(() -> SQLiteDatabase.create(null));
    CursorWindow cursorWindow = new CursorWindow("sdfsdf");
    cursorWindow.close();
  }

  @Test
  public void extracts_fontsAndIcuData() {
    assume().that(hasResource("fonts")).isTrue();
    assume().that(hasResource("icu/icudt68l.dat")).isTrue();
    DefaultNativeRuntimeLoader defaultNativeRuntimeLoader = new DefaultNativeRuntimeLoader();
    defaultNativeRuntimeLoader.ensureLoaded();
    // Check that extraction of some key files worked.
    Path root = defaultNativeRuntimeLoader.getAssetDirectory();
    assertThat(root.resolve("icu/icudt68l.dat").toFile().exists()).isTrue();
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(root.resolve("fonts/fonts.xml").toFile().exists()).isTrue();
    }
  }

  @Test
  public void extracts_fontsAndIcuData_whenAssetsAreNotShared() {
    assume().that(hasResource("fonts")).isTrue();
    assume().that(hasResource("icu/icudt68l.dat")).isTrue();
    String previous = System.getProperty("robolectric.nativeruntime.cacheAssets");
    System.setProperty("robolectric.nativeruntime.cacheAssets", "false");
    try {
      DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
      loader.ensureLoaded();
      // Without sharing, the assets land in this instance's own temporary directory.
      assertThat((Object) loader.getAssetDirectory()).isEqualTo(loader.getDirectory());
      assertThat(loader.getAssetDirectory().resolve("icu/icudt68l.dat").toFile().exists()).isTrue();
    } finally {
      if (previous == null) {
        System.clearProperty("robolectric.nativeruntime.cacheAssets");
      } else {
        System.setProperty("robolectric.nativeruntime.cacheAssets", previous);
      }
    }
  }

  @Test
  public void sharesAssetDirectory_acrossLoaders() {
    assume().that(hasResource("fonts")).isTrue();
    DefaultNativeRuntimeLoader first = new DefaultNativeRuntimeLoader();
    first.ensureLoaded();
    Path firstAssets = first.getAssetDirectory();
    assume().that(firstAssets).isNotEqualTo(first.getDirectory());

    DefaultNativeRuntimeLoader.resetLoaded();
    DefaultNativeRuntimeLoader second = new DefaultNativeRuntimeLoader();
    second.ensureLoaded();

    // The assets are reused rather than extracted again, while the native library stays per
    // instance because System.load() rejects the same path from a second ClassLoader.
    assertThat((Object) second.getAssetDirectory()).isEqualTo(firstAssets);
    assertThat((Object) second.getDirectory()).isNotEqualTo(first.getDirectory());
    assertThat(firstAssets.resolve(".complete").toFile().exists()).isTrue();
  }

  @Test
  public void tempDirectory() {
    DefaultNativeRuntimeLoader defaultNativeRuntimeLoader = new DefaultNativeRuntimeLoader();
    assertThat((Object) defaultNativeRuntimeLoader.getDirectory()).isNull();
    defaultNativeRuntimeLoader.ensureLoaded();
    assertThat((Object) defaultNativeRuntimeLoader.getDirectory()).isNotNull();
  }

  private static boolean hasResource(String name) {
    return Thread.currentThread().getContextClassLoader().getResource(name) != null;
  }
}
