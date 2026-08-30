package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(RobolectricTestRunner.class)
public final class DefaultNativeRuntimeLoaderTest {
  ExecutorService executor = Executors.newSingleThreadExecutor();

  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

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
    // Check that extraction of some key files worked. Each group is checked through the property
    // that the runtime reads it through, because a group being copied somewhere the property does
    // not point at is exactly the failure worth catching.
    assertThat(new File(System.getProperty("icu.data.path")).exists()).isTrue();
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(fontsXml().exists()).isTrue();
      assertThat(
              defaultNativeRuntimeLoader
                  .getHyphenDataDirectory()
                  .resolve("hyphen-data")
                  .toFile()
                  .isDirectory())
          .isTrue();
    }
  }

  @Test
  public void extracts_fontsAndIcuData_whenAssetsAreNotShared() {
    assume().that(hasResource("fonts")).isTrue();
    assume().that(hasResource("icu/icudt68l.dat")).isTrue();
    setSystemPropertyRule.set("robolectric.nativeruntime.cacheAssets", "false");

    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    loader.ensureLoaded();

    // Without sharing, the assets land in this instance's own temporary directory.
    assertThat(new File(System.getProperty("icu.data.path")).exists()).isTrue();
    assertThat(System.getProperty("icu.data.path"))
        .startsWith(loader.getDirectory().toAbsolutePath().toString());
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(fontsXml().exists()).isTrue();
      assertThat((Object) loader.getHyphenDataDirectory()).isEqualTo(loader.getDirectory());
    }
  }

  @Test
  public void sharesAssetDirectory_acrossLoaders() {
    assume().that(hasResource("fonts")).isTrue();
    assume().that(RuntimeEnvironment.getApiLevel()).isAtLeast(O);
    DefaultNativeRuntimeLoader first = new DefaultNativeRuntimeLoader();
    first.ensureLoaded();
    File firstFontsXml = fontsXml();
    Path firstHyphenData = first.getHyphenDataDirectory();
    assume().that(firstHyphenData).isNotEqualTo(first.getDirectory());

    DefaultNativeRuntimeLoader.resetLoaded();
    DefaultNativeRuntimeLoader second = new DefaultNativeRuntimeLoader();
    second.ensureLoaded();

    // The assets are reused rather than extracted again, while the native library stays per
    // instance because System.load() rejects the same path from a second ClassLoader.
    assertThat(fontsXml()).isEqualTo(firstFontsXml);
    assertThat((Object) second.getHyphenDataDirectory()).isEqualTo(firstHyphenData);
    assertThat((Object) second.getDirectory()).isNotEqualTo(first.getDirectory());
    assertThat(firstHyphenData.resolve(".complete").toFile().exists()).isTrue();
  }

  /**
   * The fonts come from the same archive whatever the SDK under test, while the ICU data does not,
   * so they are shared through directories of their own rather than through one directory for every
   * group.
   */
  @Test
  public void sharesEachAssetGroup_inItsOwnDirectory() {
    assume().that(hasResource("fonts")).isTrue();
    assume().that(RuntimeEnvironment.getApiLevel()).isAtLeast(O);
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    loader.ensureLoaded();
    Path hyphenData = loader.getHyphenDataDirectory();
    assume().that(hyphenData).isNotEqualTo(loader.getDirectory());

    Path fonts = fontsXml().toPath().getParent().getParent();
    Path icu = new File(System.getProperty("icu.data.path")).toPath().getParent().getParent();
    assertThat((Object) fonts).isNotEqualTo(icu);
    assertThat((Object) fonts).isNotEqualTo(hyphenData);
    assertThat((Object) icu).isNotEqualTo(hyphenData);
  }

  private static File fontsXml() {
    return new File(System.getProperty("robolectric.nativeruntime.fontdir"), "fonts.xml");
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
