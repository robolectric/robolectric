package org.robolectric.internal.bytecode;

import com.google.common.collect.Iterables;
import java.util.ServiceLoader;

/**
 * Interface for a builder for {@link ShadowWrangler}. Annotate a implementation of
 * {@link ShadowWranglerBuilder} with {@link com.google.auto.service.AutoService} to register
 * the service.
 */
public interface ShadowWranglerBuilder {
  /**
   * Builds a {@link ShadowWrangler instance}.
   */
  ShadowWrangler buildShadowWrangler(
      ShadowMap shadowMap, int sandbox, Interceptors interceptors);

  /**
   * Builds a {@link ShadowWrangler} using the a {@link ShadowWranglerBuilder} loaded from
   * {@link ServiceLoader}. Will throw if more than one service is specified. Returns the default
   * {@link ShadowWrangler} implementation if no services are available.
   */
  static ShadowWrangler build(ShadowMap shadowMap, int sandbox, Interceptors interceptors) {
    ServiceLoader<ShadowWranglerBuilder> shadowWranglerFactoryServices =
        ServiceLoader.load(ShadowWranglerBuilder.class);

    if (Iterables.isEmpty(shadowWranglerFactoryServices)) {
      return new ShadowWrangler(shadowMap, sandbox, interceptors);
    }

    return Iterables.getOnlyElement(shadowWranglerFactoryServices)
        .buildShadowWrangler(shadowMap, sandbox, interceptors);
  }
}
