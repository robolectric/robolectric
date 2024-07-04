package org.robolectric.sandbox;

import java.lang.reflect.Method;
import org.robolectric.internal.bytecode.ShadowInfo;

/**
 * ShadowMatcher is used by {@link org.robolectric.internal.bytecode.ShadowWrangler} to provide
 * library-specific rules about whether shadow classes and methods should be considered matches.
 */
public interface ShadowMatcher {
  ShadowMatcher MATCH_ALL = new AlwaysTrueShadowMatcher();

  boolean matches(ShadowInfo shadowInfo);

  boolean matches(Method method);
}
