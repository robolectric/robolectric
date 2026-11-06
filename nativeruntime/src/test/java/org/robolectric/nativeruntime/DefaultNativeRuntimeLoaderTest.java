package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

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
  public void tempDirectory() {
    DefaultNativeRuntimeLoader defaultNativeRuntimeLoader = new DefaultNativeRuntimeLoader();
    assertThat((Object) defaultNativeRuntimeLoader.getDirectory()).isNull();
    defaultNativeRuntimeLoader.ensureLoaded();
    assertThat((Object) defaultNativeRuntimeLoader.getDirectory()).isNotNull();
  }

  @Test
  @Config(sdk = VANILLA_ICE_CREAM)
  public void getCoreClassNatives_vanillaIceCream_excludesResourceNatives() {
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    assertThat(loader.getCoreClassNatives())
        .containsNoneOf(
            "android.content.res.ApkAssets",
            "android.content.res.AssetManager",
            "android.content.res.StringBlock",
            "android.content.res.XmlBlock");
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void getCoreClassNatives_baklavaAndAbove_includesResourceNatives() {
    DefaultNativeRuntimeLoader loader = new DefaultNativeRuntimeLoader();
    assertThat(loader.getCoreClassNatives())
        .containsAtLeast(
            "android.content.res.ApkAssets",
            "android.content.res.AssetManager",
            "android.content.res.StringBlock",
            "android.content.res.XmlBlock");
  }

  private static File fontsXml() {
    return new File(System.getProperty("robolectric.nativeruntime.fontdir"), "fonts.xml");
  }

  private static boolean hasResource(String name) {
    return Thread.currentThread().getContextClassLoader().getResource(name) != null;
  }
}
