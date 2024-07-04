package org.robolectric.sandbox;

import java.lang.reflect.Method;
import org.robolectric.internal.bytecode.ShadowInfo;

class AlwaysTrueShadowMatcher implements ShadowMatcher {
  @Override
  public boolean matches(ShadowInfo shadowInfo) {
    return true;
  }

  @Override
  public boolean matches(Method method) {
    return true;
  }
}
