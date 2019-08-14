package org.robolectric.shadows;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link MediaScannerConnection} */
@Implements(value = MediaScannerConnection.class)
public class ShadowMediaScannerConnection {
  private static final Set<String> savedPaths = new HashSet<>();
  private static final Set<String> savedMimeTypes = new HashSet<>();

  @Implementation
  protected static void scanFile(
      Context context, String[] paths, String[] mimeTypes, OnScanCompletedListener callback) {
    if (paths != null) {
      Collections.addAll(savedPaths, paths);
    }
    if (mimeTypes != null) {
      Collections.addAll(savedMimeTypes, mimeTypes);
    }
  }

  @Resetter
  public static void reset() {
    savedPaths.clear();
    savedMimeTypes.clear();
  }

  /** Return the set of file paths scanned by scanFile() */
  public static Set<String> getSavedPaths() {
    return new HashSet<>(savedPaths);
  }

  /** Return the set of file mimeTypes scanned by scanFile() */
  public static Set<String> getSavedMimeTypes() {
    return new HashSet<>(savedMimeTypes);
  }
}
