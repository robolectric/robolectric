package org.robolectric.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.pluginapi.ConfigurationStrategy.ConfigCollection;

/**
 * Holds configuration objects for the current test, computed using
 * {@link org.robolectric.pluginapi.Configurer}.
 */
public class ConfigRegistry {

  public static ConfigRegistry instance;

  /**
   * Returns the configuration object of the specified class, computed using
   * {@link org.robolectric.pluginapi.Configurer}.
   */
  public static <T> T get(Class<T> someConfigClass) {
    return instance.getInSandboxClassLoader(someConfigClass);
  }

  private final Map<String, Object> configs = new HashMap<>();

  public ConfigRegistry(ConfigCollection configCollection) {
    for (Class<?> classInParentLoader : configCollection.keySet()) {
      Object configInParentLoader = configCollection.get(classInParentLoader);
      configs.put(classInParentLoader.getName(), configInParentLoader);
    }
  }

  private <T> T getInSandboxClassLoader(Class<T> someConfigClass) {
    Object configInParentLoader = configs.get(someConfigClass.getName());
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
