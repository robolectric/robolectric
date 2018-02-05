package org.robolectric.internal.bytecode;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;

import java.lang.reflect.Modifier;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.tree.MethodNode;
import org.robolectric.internal.bytecode.ClassInstrumentor.TryCatch;

/**
 * GeneratorAdapter implementation specific to generate code for Robolectric purposes
 */
class RobolectricGeneratorAdapter extends GeneratorAdapter {
  final boolean isStatic;
  private final String desc;

  public RobolectricGeneratorAdapter(MethodNode methodNode) {
    super(Opcodes.ASM4, methodNode, methodNode.access, methodNode.name, methodNode.desc);
    this.isStatic = Modifier.isStatic(methodNode.access);
    this.desc = methodNode.desc;
  }

  public void loadThisOrNull() {
    if (isStatic) {
      loadNull();
    } else {
      loadThis();
    }
  }

  public boolean isStatic() {
    return isStatic;
  }

  public void loadNull() {
    visitInsn(Opcodes.ACONST_NULL);
  }

  public Type getReturnType() {
    return Type.getReturnType(desc);
  }

  /**
   * Forces a return of a default value, depending on the method's return type
   *
   * @param type The method's return type
   */
  public void pushDefaultReturnValueToStack(Type type) {
    if (type.equals(Type.BOOLEAN_TYPE)) {
      push(false);
    } else if (type.equals(Type.INT_TYPE) || type.equals(Type.SHORT_TYPE) || type.equals(Type.BYTE_TYPE) || type.equals(Type.CHAR_TYPE)) {
      push(0);
    } else if (type.equals(Type.LONG_TYPE)) {
      push(0L);
    } else if (type.equals(Type.FLOAT_TYPE)) {
      push(0f);
    } else if (type.equals(Type.DOUBLE_TYPE)) {
      push(0d);
    } else if (type.getSort() == ARRAY || type.getSort() == OBJECT) {
      loadNull();
    }
  }

  void invokeMethod(String internalClassName, MethodNode method) {
    invokeMethod(internalClassName, method.name, method.desc);
  }

  void invokeMethod(String internalClassName, String methodName, String methodDesc) {
    if (isStatic()) {
      loadArgs();                                             // this, [args]
      visitMethodInsn(Opcodes.INVOKESTATIC, internalClassName, methodName, methodDesc, false);
    } else {
      loadThisOrNull();                                       // this
      loadArgs();                                             // this, [args]
      visitMethodInsn(Opcodes.INVOKESPECIAL, internalClassName, methodName, methodDesc, false);
    }
  }

  public TryCatch tryStart(Type exceptionType) {
    return new TryCatch(this, exceptionType);
  }
}
