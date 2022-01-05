package org.robolectric.nativeruntime;

import static com.google.common.base.StandardSystemProperty.OS_ARCH;
import static com.google.common.base.StandardSystemProperty.OS_NAME;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.util.PerfStatsCollector;

/** Loads the Robolectric native runtime. */
public final class NativeRuntimeLoader {
  private static final AtomicBoolean loaded = new AtomicBoolean(false);

  static {
    if (isSupported()) {
      ensureLoaded();
    } else {
      String errorMessage =
          String.format(
              "The Robolectric native runtime is not supported on %s (%s)",
              OS_NAME.value(), OS_ARCH.value());
      throw new AssertionError(errorMessage);
    }
  }

  private NativeRuntimeLoader() {}

  static void ensureLoaded() {
    if (loaded.compareAndSet(false, true)) {
      try {
        PerfStatsCollector.getInstance()
            .measure(
                "loadNativeRuntime",
                () -> {
                  String libraryName = System.mapLibraryName("robolectric-nativeruntime");
                  File tmpLibraryFile =
                      java.nio.file.Files.createTempFile("", libraryName).toFile();
                  tmpLibraryFile.deleteOnExit();
                  URL resource = Resources.getResource(nativeLibraryPath());
                  Resources.asByteSource(resource).copyTo(Files.asByteSink(tmpLibraryFile));
                  System.load(tmpLibraryFile.getAbsolutePath());
                });
      } catch (IOException e) {
        throw new AssertionError("Unable to load Robolectric native runtime library", e);
      }
    }
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
}
