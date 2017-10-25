package org.robolectric.internal.dependency;

import com.google.common.base.Preconditions;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import static org.robolectric.internal.dependency.FileUtil.validateFile;

/**
 * Stores a mapping of SDK API Level to dependency jar name
 */
public class DependencyProperties {

  private final Properties depsProp;
  private final String directoryPath;

  DependencyProperties(Properties properties, String directoryPath) {
    this.depsProp = properties;
    this.directoryPath = directoryPath;
  }

   DependencyProperties(Properties depsProps) {
    this(depsProps, null);
  }

  public String getDependencyName(int apiLevel) {
     return Preconditions.checkNotNull(depsProp.getProperty(Integer.toString(apiLevel)),
         String.format("Could not find entry for api level %d in robolectric-deps.properties", apiLevel));
  }

  public String getDirectoryPath() {
    return directoryPath;
  }

  public static DependencyProperties load() {
    FsFile propFile = getDependencyProperties();
    Properties properties = loadProperties(propFile);
    String dependencyDirPath = System.getProperty("robolectric.dependency.dir", ".");
    if (dependencyDirPath == null) {
      dependencyDirPath = propFile.getParent().getPath();
    }
    return new DependencyProperties(properties, dependencyDirPath);
  }

  private static Properties loadProperties(FsFile propertiesFile) {
    final Properties properties = new Properties();
    try (InputStream stream = propertiesFile.getInputStream()) {
      properties.load(stream);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load " + propertiesFile.getPath(), e);
    }
    return properties;
  }

  private static FsFile getDependencyProperties() {
    // first check if a custom robolectric-deps.properties is being provided
    String propPath = System.getProperty("robolectric-deps.properties");
    if (propPath == null) {
      URL buildPathPropertiesUrl = DependencyResolverFactory.class.getClassLoader().getResource("robolectric-deps.properties");
      Preconditions.checkNotNull(buildPathPropertiesUrl, "cannot find robolectric-deps.properties on classpath");
      propPath = buildPathPropertiesUrl.getPath();
    }
    return Fs.newFile(validateFile(new File(propPath)));
  }
}
