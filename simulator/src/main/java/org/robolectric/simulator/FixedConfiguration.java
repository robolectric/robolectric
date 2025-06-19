package org.robolectric.simulator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.robolectric.pluginapi.config.ConfigurationStrategy;

/**
 * A {@link ConfigurationStrategy.Configuration} that provides a fixed set of values for the
 * configuration.
 */
public final class FixedConfiguration implements ConfigurationStrategy.Configuration {

  private final ImmutableMap<Class<?>, Object> modes;

  private FixedConfiguration(ImmutableMap<Class<?>, Object> modes) {
    this.modes = modes;
  }

  @Override
  public <T> T get(Class<T> aClass) {
    return aClass.cast(modes.get(aClass));
  }

  @Override
  public ImmutableSet<Class<?>> keySet() {
    return modes.keySet();
  }

  @Override
  public ImmutableMap<Class<?>, Object> map() {
    return modes;
  }

  /** Builder for {@link FixedConfiguration}. */
  public static class Builder {
    private final ImmutableMap.Builder<Class<?>, Object> builder = ImmutableMap.builder();

    @CanIgnoreReturnValue
    public Builder put(Class<?> key, Object value) {
      builder.put(key, value);
      return this;
    }

    public FixedConfiguration build() {
      return new FixedConfiguration(builder.buildOrThrow());
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }
}
