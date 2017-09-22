package org.robolectric.internal.bytecode;

import org.robolectric.annotation.Implements;

public class ShadowConfig {
  public final String shadowClassName;
  public final boolean callThroughByDefault;
  public final boolean inheritImplementationMethods;
  public final boolean looseSignatures;
  private final int minSdk;
  private final int maxSdk;

  ShadowConfig(String shadowClassName, boolean callThroughByDefault, boolean inheritImplementationMethods,
               boolean looseSignatures, int minSdk, int maxSdk) {
    this.shadowClassName = shadowClassName;
    this.callThroughByDefault = callThroughByDefault;
    this.inheritImplementationMethods = inheritImplementationMethods;
    this.looseSignatures = looseSignatures;
    this.minSdk = minSdk;
    this.maxSdk = maxSdk;
  }

  ShadowConfig(String shadowClassName, Implements annotation) {
    this(shadowClassName,
        annotation.callThroughByDefault(),
        annotation.inheritImplementationMethods(),
        annotation.looseSignatures(),
        annotation.minSdk(),
        annotation.maxSdk());
  }

  public boolean supportsSdk(int sdkInt) {
    return minSdk <= sdkInt && (maxSdk == -1 || maxSdk >= sdkInt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShadowConfig that = (ShadowConfig) o;

    if (callThroughByDefault != that.callThroughByDefault) return false;
    if (inheritImplementationMethods != that.inheritImplementationMethods) return false;
    if (looseSignatures != that.looseSignatures) return false;
    if (minSdk != that.minSdk) return false;
    if (maxSdk != that.maxSdk) return false;
    return shadowClassName != null ? shadowClassName.equals(that.shadowClassName) : that.shadowClassName == null;

  }

  @Override
  public int hashCode() {
    int result = shadowClassName != null ? shadowClassName.hashCode() : 0;
    result = 31 * result + (callThroughByDefault ? 1 : 0);
    result = 31 * result + (inheritImplementationMethods ? 1 : 0);
    result = 31 * result + (looseSignatures ? 1 : 0);
    result = 31 * result + minSdk;
    result = 31 * result + maxSdk;
    return result;
  }
}
