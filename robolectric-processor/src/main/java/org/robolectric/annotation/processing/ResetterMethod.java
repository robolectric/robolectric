package org.robolectric.annotation.processing;

public class ResetterMethod {
  public final ClassRef classRef;
  public final String methodName;
  public final int minSdk;
  public final int maxSdk;

  public ResetterMethod(ClassRef classRef, String methodName, int minSdk, int maxSdk) {
    this.classRef = classRef;
    this.methodName = methodName;
    this.minSdk = minSdk;
    this.maxSdk = maxSdk;
  }

  public String getClassName() {
    return classRef.toString();
  }
}
