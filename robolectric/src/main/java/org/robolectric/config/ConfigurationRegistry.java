package org.robolectric.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.pluginapi.ConfigurationStrategy.Configuration;

/**
 * Holds configuration objects for the current test, computed using
 * {@link org.robolectric.pluginapi.Configurer}.
 *
 * Configuration is computed before tests run, outside of their sandboxes. If the configuration
 * is needed from within a sandbox (when a test is executing), we need to transfer it to a class
 * that the SandboxClassLoader recognizes. We do this by serializing and deserializing in
 * {@link #reloadInSandboxClassLoader(Object)}.
 */
public class ConfigurationRegistry {

  public static ConfigurationRegistry instance;

  /**
   * Returns the configuration object of the specified class, computed using
   * {@link org.robolectric.pluginapi.Configurer}.
   */
  public static <T> T get(Class<T> configClass) {
    return instance.getInSandboxClassLoader(configClass);
  }

  private final Map<String, Object> configurations = new HashMap<>();

  public ConfigurationRegistry(Configuration configuration) {
    for (Class<?> classInParentLoader : configuration.keySet()) {
      Object configInParentLoader = configuration.get(classInParentLoader);
      configurations.put(classInParentLoader.getName(), configInParentLoader);
    }
  }

  private <T> T getInSandboxClassLoader(Class<T> someConfigClass) {
    Object configInParentLoader = configurations.get(someConfigClass.getName());
    Object configInSandboxLoader = reloadInSandboxClassLoader(configInParentLoader);
    return someConfigClass.cast(configInSandboxLoader);
  }

  private static Object reloadInSandboxClassLoader(Object configInParentLoader) {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
      out.writeObject(configInParentLoader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    byte[] bytes = buf.toByteArray();

    // ObjectInputStream loads classes in the current classloader by magic
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
