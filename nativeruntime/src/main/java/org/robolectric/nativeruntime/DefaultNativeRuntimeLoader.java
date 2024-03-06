package org.robolectric.nativeruntime;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.base.StandardSystemProperty.OS_ARCH;
import static com.google.common.base.StandardSystemProperty.OS_NAME;

import android.database.CursorWindow;
import android.os.Build;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import javax.annotation.Priority;
import org.robolectric.pluginapi.NativeRuntimeLoader;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.TempDirectory;
import org.robolectric.util.inject.Injector;

/** Loads the Robolectric native runtime. */
@AutoService(NativeRuntimeLoader.class)
@Priority(Integer.MIN_VALUE)
public class DefaultNativeRuntimeLoader implements NativeRuntimeLoader {
  protected static final AtomicBoolean loaded = new AtomicBoolean(false);

  private static final AtomicReference<NativeRuntimeLoader> nativeRuntimeLoader =
      new AtomicReference<>();

  private TempDirectory extractDirectory;

  public static void injectAndLoad() {
    // Ensure a single instance.
    synchronized (nativeRuntimeLoader) {
      if (nativeRuntimeLoader.get() == null) {
        Injector injector = new Injector.Builder(CursorWindow.class.getClassLoader()).build();
        NativeRuntimeLoader loader = injector.getInstance(NativeRuntimeLoader.class);
        nativeRuntimeLoader.set(loader);
      }
    }
    nativeRuntimeLoader.get().ensureLoaded();
  }

  @Override
  public synchronized void ensureLoaded() {
    if (loaded.get()) {
      return;
    }

    if (!isSupported()) {
      String errorMessage =
          String.format(
              "The Robolectric native runtime is not supported on %s (%s)",
              OS_NAME.value(), OS_ARCH.value());
      throw new AssertionError(errorMessage);
    }
    loaded.set(true);

    try {
      PerfStatsCollector.getInstance()
          .measure(
              "loadNativeRuntime",
              () -> {
                extractDirectory = new TempDirectory("nativeruntime");
                System.setProperty("icu.locale.default", Locale.getDefault().toLanguageTag());
                if (Build.VERSION.SDK_INT >= O) {
                  maybeCopyFonts(extractDirectory);
                }
                maybeCopyIcuData(extractDirectory);
                loadLibrary(extractDirectory);
              });
    } catch (IOException e) {
      throw new AssertionError("Unable to load Robolectric native runtime library", e);
    }
  }

  /** Attempts to load the ICU dat file. This is only relevant for native graphics. */
  private void maybeCopyIcuData(TempDirectory tempDirectory) throws IOException {
    URL icuDatUrl;
    try {
      icuDatUrl = Resources.getResource("icu/icudt68l.dat");
    } catch (IllegalArgumentException e) {
      return;
    }
    Path icuPath = tempDirectory.create("icu");
    Path icuDatPath = icuPath.resolve("icudt68l.dat");
    Resources.asByteSource(icuDatUrl).copyTo(Files.asByteSink(icuDatPath.toFile()));
    System.setProperty("icu.data.path", icuDatPath.toAbsolutePath().toString());
  }

  /**
   * Attempts to copy the system fonts to a temporary directory. This is only relevant for native
   * graphics.
   */
  private void maybeCopyFonts(TempDirectory tempDirectory) throws IOException {
    URI fontsUri = null;
    try {
      fontsUri = Resources.getResource("fonts/").toURI();
    } catch (IllegalArgumentException | URISyntaxException e) {
      return;
    }

    FileSystem zipfs = null;

    if ("jar".equals(fontsUri.getScheme())) {
      zipfs = FileSystems.newFileSystem(fontsUri, ImmutableMap.of("create", "true"));
    }

    Path fontsInputPath = Paths.get(fontsUri);
    Path fontsOutputPath = tempDirectory.create("fonts");

    try (Stream<Path> pathStream = java.nio.file.Files.walk(fontsInputPath)) {
      Iterator<Path> fileIterator = pathStream.iterator();
      while (fileIterator.hasNext()) {
        Path path = fileIterator.next();
        // Avoid copying parent directory.
        if ("fonts".equals(path.getFileName().toString())) {
          continue;
        }
        String fontPath = "fonts/" + path.getFileName();
        URL resource = Resources.getResource(fontPath);
        Path outputPath = tempDirectory.getBasePath().resolve(fontPath);
        Resources.asByteSource(resource).copyTo(Files.asByteSink(outputPath.toFile()));
      }
    }
    System.setProperty(
        "robolectric.nativeruntime.fontdir",
        // Android's FontListParser expects a trailing slash for the base font directory.
        fontsOutputPath.toAbsolutePath() + File.separator);
    if (zipfs != null) {
      zipfs.close();
    }
  }

  private void loadLibrary(TempDirectory tempDirectory) throws IOException {
    String libraryName = System.mapLibraryName("robolectric-nativeruntime");
    Path libraryPath = tempDirectory.getBasePath().resolve(libraryName);
    URL libraryResource = Resources.getResource(nativeLibraryPath());
    Resources.asByteSource(libraryResource).copyTo(Files.asByteSink(libraryPath.toFile()));
    System.load(libraryPath.toAbsolutePath().toString());
  }

  private static boolean isSupported() {
    return ("mac".equals(osName()) && ("aarch64".equals(arch()) || "x86_64".equals(arch())))
        || ("linux".equals(osName()) && "x86_64".equals(arch()))
        || ("windows".equals(osName()) && "x86_64".equals(arch()));
  }

  private static String nativeLibraryPath() {
    String os = osName();
    String arch = arch();
    return String.format(
        "native/%s/%s/%s", os, arch, System.mapLibraryName("robolectric-nativeruntime"));
  }

  private static String osName() {
    String osName = OS_NAME.value().toLowerCase(Locale.US);
    if (osName.contains("linux")) {
      return "linux";
    } else if (osName.contains("mac")) {
      return "mac";
    } else if (osName.contains("win")) {
      return "windows";
    }
    return "unknown";
  }

  private static String arch() {
    String arch = OS_ARCH.value().toLowerCase(Locale.US);
    if (arch.equals("x86_64") || arch.equals("amd64")) {
      return "x86_64";
    }
    return arch;
  }

  @VisibleForTesting
  static boolean isLoaded() {
    return loaded.get();
  }

  @VisibleForTesting
  Path getDirectory() {
    return extractDirectory == null ? null : extractDirectory.getBasePath();
  }

  @VisibleForTesting
  static void resetLoaded() {
    loaded.set(false);
  }
}
