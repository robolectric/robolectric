package org.robolectric.android;

import java.lang.reflect.Method;
import org.robolectric.annotation.Implementation;
import org.robolectric.internal.bytecode.ShadowInfo;
import org.robolectric.sandbox.ShadowMatcher;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/**
 * Android-specific rules for matching shadow classes and methods by SDK level.
 */
public class AndroidSdkShadowMatcher implements ShadowMatcher {
  private static final Implementation IMPLEMENTATION_DEFAULTS =
      ReflectionHelpers.defaultsFor(Implementation.class);

  private final int sdkLevel;

  public AndroidSdkShadowMatcher(int sdkLevel) {
    this.sdkLevel = sdkLevel;
  }

  @Override
  public boolean matches(ShadowInfo shadowInfo) {
    return shadowInfo.supportsSdk(sdkLevel);
  }

  @Override
  public boolean matches(Method method) {
    Implementation implementation = getImplementationAnnotation(method);
    return implementation.minSdk() <= sdkLevel &&
        (implementation.maxSdk() == -1 || implementation.maxSdk() >= sdkLevel);
  }

  private static Implementation getImplementationAnnotation(Method method) {
    if (method == null) {
      return null;
    }
    Implementation implementation = method.getAnnotation(Implementation.class);
    if (implementation == null) {
      Logger.warn("No @Implementation annotation on " + method);
    }
    return implementation == null
        ? IMPLEMENTATION_DEFAULTS
        : implementation;
  }
}
