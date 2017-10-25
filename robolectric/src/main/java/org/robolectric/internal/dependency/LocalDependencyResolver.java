package org.robolectric.internal.dependency;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import static org.robolectric.internal.dependency.FileUtil.fileToUrl;
import static org.robolectric.internal.dependency.FileUtil.validateFile;

public class LocalDependencyResolver implements DependencyResolver {
  private final DependencyProperties depsProp;

  /**
   * Creates a LocalDependencyResolver
   *  @param depsProp the robolectric-deps.properties, containing a mapping of api level to jar file
   * @param baseDir the base directory for the jar files
   */
  public LocalDependencyResolver(DependencyProperties depsProp) {
    this.depsProp = depsProp;
  }

  @Override
  public URL getLocalArtifactUrl(int apiLevel) {
    String filePath = depsProp.getDependencyName(apiLevel);
    File localFile = new File(filePath);
    if (!localFile.isAbsolute()) {
      localFile = new File(depsProp.getDirectoryPath(), filePath);
    }
    return fileToUrl(validateFile(localFile));
  }
}
