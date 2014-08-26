package org.robolectric.bytecode;

import java.util.Arrays;

class InvocationProfile {
  final Class clazz;
  final String methodName;
  final boolean isStatic;
  final String[] paramTypes;
  private final boolean isDeclaredOnObject;

  public InvocationProfile(String methodSignatureString, boolean isStatic, ClassLoader classLoader) {
    MethodSignature methodSignature = MethodSignature.parse(methodSignatureString);
    this.clazz = loadClass(classLoader, methodSignature.className);
    this.methodName = methodSignature.methodName;
    this.paramTypes = methodSignature.paramTypes;
    this.isStatic = isStatic;

    this.isDeclaredOnObject = methodSignatureString.endsWith("/equals(Ljava/lang/Object;)Z")
        || methodSignatureString.endsWith("/hashCode()I")
        || methodSignatureString.endsWith("/toString()Ljava/lang/String;");
  }

  public Class<?>[] getParamClasses(ClassLoader classLoader) throws ClassNotFoundException {
    Class[] classes = new Class[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      String paramType = paramTypes[i];
      classes[i] = ShadowWrangler.loadClass(paramType, classLoader);
    }
    return classes;
  }

  private Class<?> loadClass(ClassLoader classLoader, String className) {
    try {
      return classLoader.loadClass(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InvocationProfile that = (InvocationProfile) o;

    if (isDeclaredOnObject != that.isDeclaredOnObject) return false;
    if (isStatic != that.isStatic) return false;
    if (clazz != null ? !clazz.equals(that.clazz) : that.clazz != null) return false;
    if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
    if (!Arrays.equals(paramTypes, that.paramTypes)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clazz != null ? clazz.hashCode() : 0;
    result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
    result = 31 * result + (isStatic ? 1 : 0);
    result = 31 * result + (paramTypes != null ? Arrays.hashCode(paramTypes) : 0);
    result = 31 * result + (isDeclaredOnObject ? 1 : 0);
    return result;
  }

  public boolean isDeclaredOnObject() {
    return isDeclaredOnObject;
  }
}
