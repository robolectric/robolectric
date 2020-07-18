package org.robolectric.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.pluginapi.config.Configurer;

/**
 * Provides cached access to {@code robolectric-properties} files, for all your configuration needs!
 *
 * <p>Used by {@link ConfigConfigurer} to support package configuration (see [Configuring
 * Robolectric](http://robolectric.org/configuring/) but it may be useful for other {@link
 * Configurer}s as well.
 */
@SuppressWarnings({"AndroidJdkLibsChecker", "NewApi"})
public class PackagePropertiesLoader {

  /**
   * We should get very high cache hit rates even with a tiny cache if we're called sequentially
   * by multiple {@link Configurer}s for the same package.
   */
  private final Map<String, Properties> cache = new LinkedHashMap<String, Properties>() {
    @Override
    protected boolean removeEldestEntry(Map.Entry<String, Properties> eldest) {
      return size() > 3;
    }
  };

  /**
   * Return a {@link Properties} file for the given package name, or {@code null} if none is
   * available.
   *
   * @since 3.2
   */
  public Properties getConfigProperties(@Nonnull String packageName) {
    return cache.computeIfAbsent(packageName, s -> {
      StringBuilder buf = new StringBuilder();
      if (!packageName.isEmpty()) {
        buf.append(packageName.replace('.', '/'));
        buf.append('/');
      }
      buf.append(RobolectricTestRunner.CONFIG_PROPERTIES);
      final String resourceName = buf.toString();

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
    });
  }

  // visible for testing
  InputStream getResourceAsStream(String resourceName) {
    return getClass().getClassLoader().getResourceAsStream(resourceName);
  }
}
