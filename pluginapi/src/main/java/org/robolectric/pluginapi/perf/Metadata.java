package org.robolectric.pluginapi.perf;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata for perf stats collection.
 */
public class Metadata {
  private final Map<Class<?>, Object> metadata;

  public Metadata(Map<Class<?>, Object> metadata) {
    this.metadata = new HashMap<>(metadata);
  }

  public <T> T get(Class<T> metadataClass) {
    return metadataClass.cast(metadata.get(metadataClass));
  }
}
