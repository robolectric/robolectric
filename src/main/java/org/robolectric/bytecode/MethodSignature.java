package org.robolectric.bytecode;

import org.objectweb.asm.Type;
import org.robolectric.util.Join;

public class MethodSignature {
  public final String className;
  public final String methodName;
  public final String[] paramTypes;

  public MethodSignature(String className, String methodName, String[] paramTypes) {
    this.className = className;
    this.methodName = methodName;
    this.paramTypes = paramTypes;
  }

  public static MethodSignature parse(String internalString) {
    int parenStart = internalString.indexOf('(');
    int methodStart = internalString.lastIndexOf('/', parenStart);
    String className = internalString.substring(0, methodStart).replace('/', '.');
    String methodName = internalString.substring(methodStart + 1, parenStart);
    Type[] argumentTypes = Type.getArgumentTypes(internalString.substring(parenStart));
    String[] paramTypes = new String[argumentTypes.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      paramTypes[i] = argumentTypes[i].getClassName();
    }

    return new MethodSignature(className, methodName, paramTypes);
  }

  @Override public String toString() {
    return className + "." + methodName + "(" + Join.join(", ", paramTypes) + ")";
  }

  boolean matches(String className, String methodName) {
    return this.className.equals(className) && this.methodName.equals(methodName);
  }
}
