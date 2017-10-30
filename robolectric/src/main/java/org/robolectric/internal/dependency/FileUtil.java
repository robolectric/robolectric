package org.robolectric.internal.dependency;

import com.google.common.base.Preconditions;
import org.robolectric.res.FsFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

class FileUtil {

  /**
   * Validates {@code file} is an existing file that is readable.
   *
   * @param file the File to test
   * @return the provided file, if all validation passes
   * @throws IllegalArgumentException if validation fails
   */
  public static File validateFile(File file ) {
    Preconditions.checkState(file.isFile(), "Path is not a file: " + file.getAbsolutePath());
    Preconditions.checkState(file.canRead(), "Unable to read file: " + file.getAbsolutePath());
    return file;
  }

  /** Returns the given file as a {@link URL}. */
  public static URL fileToUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          String.format("File \"%s\" cannot be represented as a URL: %s", file.getAbsolutePath(), e));
    }
  }
}
