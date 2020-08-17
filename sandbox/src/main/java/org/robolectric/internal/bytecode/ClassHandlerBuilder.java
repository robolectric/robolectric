package org.robolectric.internal.bytecode;

import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.inject.AutoFactory;

/**
 * Factory interface for {@link ClassHandler}.
 *
 * To inject your own ClassHandler, annotate a subclass with {@link com.google.auto.service.AutoService}(ClassHandler).
 *
 * Robolectric's default ClassHandler is {@link ShadowWrangler}.
 */
@AutoFactory
public interface ClassHandlerBuilder {
  /** Builds a {@link ClassHandler instance}. */
  ClassHandler build(ShadowMap shadowMap, ShadowMatcher shadowMatcher, Interceptors interceptors);
}
