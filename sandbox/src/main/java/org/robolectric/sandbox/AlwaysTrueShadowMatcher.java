package org.robolectric.sandbox;

import org.robolectric.internal.bytecode.ShadowInfo;

import java.lang.reflect.Method;

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
