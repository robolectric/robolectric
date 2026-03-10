package org.robolectric.pluginapi;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * A plugin interface that allows modifying the {@link MethodHandle} returned by ShadowWrangler.
 *
 * <p>Implementations are loaded via {@link java.util.ServiceLoader}.
 */
public interface MethodHandleDecorator {
  MethodHandle decorate(
      Class<?> definingClass,
      String methodName,
      MethodType methodType,
      boolean isStatic,
      MethodHandle original);
}
