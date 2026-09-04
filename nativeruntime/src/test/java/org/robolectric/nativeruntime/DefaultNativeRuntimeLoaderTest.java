package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.collect.ImmutableMap;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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
    // Check that extraction of some key files worked.
    Path root = defaultNativeRuntimeLoader.getDirectory();
    assertThat(root.resolve("icu/icudt68l.dat").toFile().exists()).isTrue();
    if (RuntimeEnvironment.getApiLevel() >= O) {
      assertThat(root.resolve("fonts/fonts.xml").toFile().exists()).isTrue();
    }
  }

  @Test
  @Config(minSdk = O)
  public void extracts_fonts_whenFontsJarFileSystemIsAlreadyOpen() throws Exception {
    // Issue #10116: another sandbox in the same JVM, or org.robolectric.res.Fs, may already
    // hold a zip filesystem for the jar containing the fonts. Extraction must reuse it
    // instead of failing with FileSystemAlreadyExistsException, and must leave it open.
    assume().that(hasResource("fonts")).isTrue();
    URI fontsUri = Thread.currentThread().getContextClassLoader().getResource("fonts/").toURI();
    assume().that(fontsUri.getScheme()).isEqualTo("jar");

    FileSystem alreadyOpen;
    boolean ownsFileSystem;
    try {
      alreadyOpen = FileSystems.newFileSystem(fontsUri, ImmutableMap.of("create", "true"));
      ownsFileSystem = true;
    } catch (FileSystemAlreadyExistsException e) {
      alreadyOpen = FileSystems.getFileSystem(fontsUri);
      ownsFileSystem = false;
    }
    try {
      DefaultNativeRuntimeLoader defaultNativeRuntimeLoader = new DefaultNativeRuntimeLoader();
      defaultNativeRuntimeLoader.ensureLoaded();

      Path root = defaultNativeRuntimeLoader.getDirectory();
      assertThat(root.resolve("fonts/fonts.xml").toFile().exists()).isTrue();
      assertThat(alreadyOpen.isOpen()).isTrue();
    } finally {
      if (ownsFileSystem) {
        alreadyOpen.close();
      }
    }
  }

  @Test
  @Config(minSdk = O)
  public void closes_jarFileSystemItOpened_onceFontsAndHyphenDataAreExtracted() throws Exception {
    // Issue #10116: a zip filesystem opened by the loader must not outlive the extraction,
    // otherwise it leaks into the next sandbox of the same JVM.
    assume().that(hasResource("fonts")).isTrue();
    assume().that(hasResource("hyphen-data")).isTrue();
    URI fontsUri = resourceUri("fonts/");
    assume().that(fontsUri.getScheme()).isEqualTo("jar");
    assume().that(isJarFileSystemOpen(fontsUri)).isFalse();

    DefaultNativeRuntimeLoader defaultNativeRuntimeLoader = new DefaultNativeRuntimeLoader();
    defaultNativeRuntimeLoader.ensureLoaded();

    Path root = defaultNativeRuntimeLoader.getDirectory();
    assertThat(Files.exists(root.resolve("fonts/fonts.xml"))).isTrue();
    assertThat(Files.exists(root.resolve("hyphen-data/hyph-af.hyb"))).isTrue();
    assertThat(isJarFileSystemOpen(fontsUri)).isFalse();
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

  private static URI resourceUri(String name) throws Exception {
    return Thread.currentThread().getContextClassLoader().getResource(name).toURI();
  }

  private static boolean isJarFileSystemOpen(URI jarUri) {
    try {
      return FileSystems.getFileSystem(jarUri).isOpen();
    } catch (FileSystemNotFoundException e) {
      return false;
    }
  }
}
