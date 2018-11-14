package org.robolectric.res;

import java.io.IOException;
import java.io.InputStream;
import javax.annotation.Nonnull;

public interface FsFile {
  boolean exists();

  boolean isDirectory();

  boolean isFile();

  FsFile[] listFiles();

  FsFile[] listFiles(Filter filter);

  String[] listFileNames();

  FsFile getParent();

  String getName();

  InputStream getInputStream() throws IOException;

  byte[] getBytes() throws IOException;

  FsFile join(String... pathParts);

  @Override String toString();

  @Override boolean equals(Object o);

  @Override int hashCode();

  String getBaseName();

  String getPath();

  long length();

  public interface Filter {
    boolean accept(@Nonnull FsFile fsFile);
  }
}
