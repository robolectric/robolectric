package org.robolectric.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Reserves native library paths across sandboxes for the lifetime of the JVM. */
public final class NativeRuntimeLibraryClaims {
  private static final Set<Path> claimed = ConcurrentHashMap.newKeySet();

  private NativeRuntimeLibraryClaims() {}

  public static boolean claim(Path library) throws IOException {
    return claimed.add(library.toRealPath());
  }
}
