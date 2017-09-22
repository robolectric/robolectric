package org.robolectric.internal.bytecode;

/**
 * Reference to a specific method on a class.
 */
public class MethodRef {
  public final String className;
  public final String methodName;

  public MethodRef(Class<?> clazz, String methodName) {
    this(clazz.getName(), methodName);
  }

  public MethodRef(String className, String methodName) {
    this.className = className;
    this.methodName = methodName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MethodRef methodRef = (MethodRef) o;

    return className.equals(methodRef.className) && methodName.equals(methodRef.methodName);
  }

  @Override public int hashCode() {
    int result = className.hashCode();
    result = 31 * result + methodName.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "MethodRef{" +
        "className='" + className + '\'' +
        ", methodName='" + methodName + '\'' +
        '}';
  }
}
