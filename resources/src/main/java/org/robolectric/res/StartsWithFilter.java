package org.robolectric.res;

import java.nio.file.Path;
import java.util.function.Predicate;

@SuppressWarnings("NewApi")
class StartsWithFilter implements Predicate<Path> {
  private final String prefix;

  public StartsWithFilter(String prefix) {
    this.prefix = prefix;
  }

  @Override
  public boolean test(Path file) {
    return file.getFileName().toString().startsWith(prefix);
  }
}
