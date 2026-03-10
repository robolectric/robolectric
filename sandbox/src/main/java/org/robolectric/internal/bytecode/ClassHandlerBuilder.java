package org.robolectric.internal.bytecode;

import java.util.List;
import org.robolectric.pluginapi.MethodHandleDecorator;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.inject.AutoFactory;

/**
 * Factory interface for {@link ClassHandler}.
 *
 * <p>To inject your own ClassHandler, annotate a subclass with {@link
 * com.google.auto.service.AutoService}(ClassHandler).
 *
 * <p>Robolectric's default ClassHandler is {@link ShadowWrangler}.
 */
@AutoFactory
public interface ClassHandlerBuilder {
  /** Builds a {@link ClassHandler instance}. */
  ClassHandler build(
      ShadowMap shadowMap,
      ShadowMatcher shadowMatcher,
      Interceptors interceptors,
      List<MethodHandleDecorator> decorators);
}
