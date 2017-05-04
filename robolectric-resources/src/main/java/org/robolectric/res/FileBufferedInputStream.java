package org.robolectric.res;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class FileBufferedInputStream extends BufferedInputStream {
  private final FileInputStream fileInputStream;

  public FileBufferedInputStream(FileInputStream in) {
    super(in);
    this.fileInputStream = in;
  }

  public FileBufferedInputStream(FileInputStream in, int size) {
    super(in, size);
    this.fileInputStream = in;
  }

  public FileInputStream getFileInputStream() {
    return fileInputStream;
  }
}
