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
    Path root = defaultNativeRuntimeLoader.getDirectory();
    assertThat(root.resolve("icu/icudt68l.dat").toFile().exists()).isTrue();
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(root.resolve("fonts/fonts.xml").toFile().exists()).isTrue();
    }
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
