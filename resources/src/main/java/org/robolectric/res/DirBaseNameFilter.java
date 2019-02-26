package org.robolectric.res;

import java.nio.file.Path;
import java.util.function.Predicate;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
class DirBaseNameFilter implements Predicate<Path> {
  private final String prefix;
  private final String prefixDash;

  DirBaseNameFilter(String prefix) {
    this.prefix = prefix;
    this.prefixDash = prefix + "-";
  }

  @Override
  public boolean test(Path file) {
    String fileName = nameWithoutTrailingSeparator(file);
    return fileName.equals(prefix) || fileName.startsWith(prefixDash);
  }

  /**
   * It sure seems like a bug that Path#getFileName() returns "name/" for paths inside a jar, but
   * "name" for paths on a regular filesystem. It's always a normal slash, even on Windows. :-p
   */
  private String nameWithoutTrailingSeparator(Path file) {
    String fileName = file.getFileName().toString();
    int trailingSlash = fileName.indexOf('/');
    if (trailingSlash != -1) {
      fileName = fileName.substring(0, trailingSlash);
    }
    return fileName;
  }
}
