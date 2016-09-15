package org.robolectric.internal.bytecode;

import org.objectweb.asm.Type;
import org.robolectric.util.Join;

public class MethodSignature {
  public final String className;
  public final String methodName;
  public final String[] paramTypes;
  public final String returnType;

  private MethodSignature(String className, String methodName, String[] paramTypes, String returnType) {
    this.className = className;
    this.methodName = methodName;
    this.paramTypes = paramTypes;
    this.returnType = returnType;
  }

  public static MethodSignature parse(String internalString) {
    int parenStart = internalString.indexOf('(');
    int methodStart = internalString.lastIndexOf('/', parenStart);
    String className = internalString.substring(0, methodStart).replace('/', '.');
    String methodName = internalString.substring(methodStart + 1, parenStart);
    String methodDescriptor = internalString.substring(parenStart);
    Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
    String[] paramTypes = new String[argumentTypes.length];
    for (int i = 0; i < argumentTypes.length; i++) {
      paramTypes[i] = argumentTypes[i].getClassName();
    }
    final String returnType = Type.getReturnType(methodDescriptor).getClassName();
    return new MethodSignature(className, methodName, paramTypes, returnType);
  }

  @Override
  public String toString() {
    return className + "." + methodName + "(" + Join.join(", ", (Object[]) paramTypes) + ")";
  }

  boolean matches(String className, String methodName) {
    return this.className.equals(className) && this.methodName.equals(methodName);
  }
}
