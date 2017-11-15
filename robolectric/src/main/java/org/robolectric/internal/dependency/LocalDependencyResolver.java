package org.robolectric.internal.dependency;

import static org.robolectric.internal.dependency.FileUtil.fileToUrl;
import static org.robolectric.internal.dependency.FileUtil.validateFile;

import java.io.File;
import java.net.URL;
import org.robolectric.res.FsFile;

public class LocalDependencyResolver implements DependencyResolver {
  private final DependencyProperties depsProp;

  /**
   * Creates a LocalDependencyResolver
   *
   * @param depsProp the robolectric-deps.properties, containing a mapping of api level to jar file
   */
  public LocalDependencyResolver(DependencyProperties depsProp) {
    this.depsProp = depsProp;
  }

  @Override
  public URL getLocalArtifactUrl(int apiLevel) {
    String filePath = depsProp.getDependencyName(apiLevel);
    File localFile = new File(filePath);
    if (!localFile.isAbsolute()) {
      FsFile parentDir = depsProp.getPropertyFile().getParent();
      if (depsProp.getPropertyFile().getParent().isDirectory()) {
        localFile = new File(parentDir.getPath(), filePath);
      } else {
        throw new IllegalStateException(
            "Could not find base directory for dependency jars for properties "
                + depsProp.getPropertyFile().getPath());
      }
    }
    return fileToUrl(validateFile(localFile));
  }
}
