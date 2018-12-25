package org.robolectric.res;

import java.nio.file.Path;

/** @deprecated Use {@link Path} instead. */
@Deprecated
@SuppressWarnings("NewApi")
public interface FsFile extends Path {

  /** @deprecated use {@link Fs#externalize(Path)} instead. */
  @Deprecated
  default String getPath() {
    return Fs.externalize(this);
  }

  /** @deprecated use {@link Path#resolve(Path)} instead. */
  @Deprecated
  default Path join(String name) {
    return this.resolve(name);
  }
}
