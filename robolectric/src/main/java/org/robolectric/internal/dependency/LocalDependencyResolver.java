package org.robolectric.internal.dependency;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class LocalDependencyResolver implements DependencyResolver {
  private File offlineJarDir;

  public LocalDependencyResolver(File offlineJarDir) {
    super();
    this.offlineJarDir = offlineJarDir;
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    StringBuilder filenameBuilder = new StringBuilder();
    filenameBuilder.append(dependency.getArtifactId())
        .append("-")
        .append(dependency.getVersion());

    if (dependency.getClassifier() != null) {
      filenameBuilder.append("-")
          .append(dependency.getClassifier());
    }

    filenameBuilder.append(".")
        .append(dependency.getType());

    return fileToUrl(validateFile(new File(offlineJarDir, filenameBuilder.toString())));
  }

  /**
   * Validates {@code file} is an existing file that is readable.
   *
   * @param file the File to test
   * @return the provided file, if all validation passes
   * @throws IllegalArgumentException if validation fails
   */
  private static File validateFile(File file) throws IllegalArgumentException {
    if (!file.isFile()) {
      throw new IllegalArgumentException("Path is not a file: " + file);
    }
    if (!file.canRead()) {
      throw new IllegalArgumentException("Unable to read file: " + file);
    }
    return file;
  }

  /** Returns the given file as a {@link URL}. */
  private static URL fileToUrl(File file) {
    try {
      return file.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
          String.format("File \"%s\" cannot be represented as a URL: %s", file, e));
    }
  }
}
