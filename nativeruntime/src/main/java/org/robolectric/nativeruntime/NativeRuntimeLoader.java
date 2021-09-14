package org.robolectric.nativeruntime;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/** Loads the Roboelctric native runtime. */
public final class NativeRuntimeLoader {
  private static final AtomicBoolean loaded = new AtomicBoolean(false);

  static {
    ensureLoaded();
  }

  private NativeRuntimeLoader() {}

  static void ensureLoaded() {
    if (loaded.compareAndSet(false, true)) {
      try {
        File tmpLibraryFile =
            java.nio.file.Files.createTempFile("", "robolectric-nativeruntime.so").toFile();
        tmpLibraryFile.deleteOnExit();
        URL resource = Resources.getResource("native/linux/64/robolectric-nativeruntime.so");
        Resources.asByteSource(resource).copyTo(Files.asByteSink(tmpLibraryFile));
        System.load(tmpLibraryFile.getAbsolutePath());
      } catch (IOException e) {
        throw new AssertionError("Unable to load Robolectric nativeruntime library", e);
      }
    }
  }
}
