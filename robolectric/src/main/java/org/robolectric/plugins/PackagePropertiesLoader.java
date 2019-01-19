package org.robolectric.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.Join;

@SuppressWarnings("NewApi")
public class PackagePropertiesLoader {

  /**
   * Return a {@link Properties} file for the given package name, or {@code null} if none is
   * available.
   *
   * @since 3.2
   */
  protected Properties getConfigProperties(String packageName) {
    return cache(packageName);
  }

  private Properties cache(String packageName) {
    List<String> packageParts = new ArrayList<>(Arrays.asList(packageName.split("\\.")));
    packageParts.add(RobolectricTestRunner.CONFIG_PROPERTIES);
    final String resourceName = Join.join("/", packageParts);
    try (InputStream resourceAsStream = getResourceAsStream(resourceName)) {
      if (resourceAsStream == null) {
        return null;
      }
      Properties properties = new Properties();
      properties.load(resourceAsStream);
      return properties;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  // visible for testing
  InputStream getResourceAsStream(String resourceName) {
    return getClass().getClassLoader().getResourceAsStream(resourceName);
  }
}
