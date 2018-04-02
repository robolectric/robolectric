package org.robolectric.internal.bytecode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Implements.DefaultShadowFactory;
import org.robolectric.shadow.api.ShadowFactory;

public class ShadowInfo {

  public final String shadowedClassName;
  public final String shadowClassName;
  public final boolean callThroughByDefault;
  /**
   * @deprecated
   */
  @Deprecated
  public final boolean inheritImplementationMethods;
  public final boolean looseSignatures;
  private final int minSdk;
  private final int maxSdk;
  private final Class<? extends ShadowFactory<?>> shadowFactoryClass;

  ShadowInfo(String shadowedClassName, String shadowClassName, boolean callThroughByDefault,
      boolean inheritImplementationMethods, boolean looseSignatures, int minSdk, int maxSdk,
      Class<? extends ShadowFactory<?>> shadowFactoryClass) {
    this.shadowedClassName = shadowedClassName;
    this.shadowClassName = shadowClassName;
    this.callThroughByDefault = callThroughByDefault;
    this.inheritImplementationMethods = inheritImplementationMethods;
    this.looseSignatures = looseSignatures;
    this.minSdk = minSdk;
    this.maxSdk = maxSdk;
    this.shadowFactoryClass =
        DefaultShadowFactory.class.equals(shadowFactoryClass)
            ? null
            : shadowFactoryClass;
  }

  ShadowInfo(String shadowedClassName, String shadowClassName, Implements annotation) {
    this(shadowedClassName,
        shadowClassName,
        annotation.callThroughByDefault(),
        annotation.inheritImplementationMethods(),
        annotation.looseSignatures(),
        annotation.minSdk(),
        annotation.maxSdk(),
        annotation.factory());
  }

  public boolean supportsSdk(int sdkInt) {
    return minSdk <= sdkInt && (maxSdk == -1 || maxSdk >= sdkInt);
  }

  public boolean isShadowOf(Class<?> clazz) {
    return shadowedClassName.equals(clazz.getName());
  }

  public Class<? extends ShadowFactory<?>> getShadowFactoryClass() {
    return shadowFactoryClass;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ShadowInfo that = (ShadowInfo) o;
    return callThroughByDefault == that.callThroughByDefault &&
        inheritImplementationMethods == that.inheritImplementationMethods &&
        looseSignatures == that.looseSignatures &&
        minSdk == that.minSdk &&
        maxSdk == that.maxSdk &&
        Objects.equals(shadowedClassName, that.shadowedClassName) &&
        Objects.equals(shadowClassName, that.shadowClassName) &&
        Objects.equals(shadowFactoryClass, that.shadowFactoryClass);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(shadowedClassName, shadowClassName, callThroughByDefault,
            inheritImplementationMethods, looseSignatures, minSdk, maxSdk, shadowFactoryClass);
  }
}
