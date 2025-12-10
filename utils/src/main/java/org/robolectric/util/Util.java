package org.robolectric.util;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Generic collection of utility methods. */
public class Util {
  @SuppressWarnings("NewApi")
  public static Path pathFrom(URL localArtifactUrl) {
    try {
      return Paths.get(localArtifactUrl.toURI());
    } catch (URISyntaxException e) {
      throw new RuntimeException("huh? " + localArtifactUrl, e);
    }
  }

  /**
   * Re-throw {@code t} (even if it's a checked exception) without requiring a {@code throws}
   * declaration.
   *
   * <p>This function declares a return type of {@link RuntimeException} but will never actually
   * return a value. This allows you to use it with a {@code throw} statement to convince the
   * compiler that the current branch will not complete.
   *
   * <pre>{@code
   * throw Util.sneakyThrow(new IOException());
   * }</pre>
   *
   * <p>Adapted from https://www.mail-archive.com/javaposse@googlegroups.com/msg05984.html
   */
  @SuppressWarnings("unchecked")
  public static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
    throw (T) t;
  }
}
