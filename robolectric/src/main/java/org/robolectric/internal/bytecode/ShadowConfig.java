package org.robolectric.internal.bytecode;

import org.robolectric.annotation.Implements;

public class ShadowConfig {
  public final String shadowClassName;
  public final boolean callThroughByDefault;
  public final boolean inheritImplementationMethods;
  public final boolean looseSignatures;

  ShadowConfig(String shadowClassName, boolean callThroughByDefault, boolean inheritImplementationMethods, boolean looseSignatures) {
    this.shadowClassName = shadowClassName;
    this.callThroughByDefault = callThroughByDefault;
    this.inheritImplementationMethods = inheritImplementationMethods;
    this.looseSignatures = looseSignatures;
  }

  ShadowConfig(String shadowClassName, Implements annotation) {
    this(shadowClassName, annotation.callThroughByDefault(), annotation.inheritImplementationMethods(), annotation.looseSignatures());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShadowConfig that = (ShadowConfig) o;

    if (callThroughByDefault != that.callThroughByDefault) return false;
    if (inheritImplementationMethods != that.inheritImplementationMethods) return false;
    if (looseSignatures != that.looseSignatures) return false;
    if (shadowClassName != null ? !shadowClassName.equals(that.shadowClassName) : that.shadowClassName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = shadowClassName != null ? shadowClassName.hashCode() : 0;
    result = 31 * result + (callThroughByDefault ? 1 : 0);
    result = 31 * result + (inheritImplementationMethods ? 1 : 0);
    result = 31 * result + (looseSignatures ? 1 : 0);
    return result;
  }
}
