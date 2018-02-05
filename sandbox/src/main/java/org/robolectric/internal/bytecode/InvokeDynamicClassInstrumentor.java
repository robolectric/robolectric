package org.robolectric.internal.bytecode;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ListIterator;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class InvokeDynamicClassInstrumentor extends ClassInstrumentor {
  private static final Handle BOOTSTRAP_INIT;
  private static final Handle BOOTSTRAP;
  private static final Handle BOOTSTRAP_STATIC;
  private static final Handle BOOTSTRAP_INTRINSIC;

  static {
    String className = Type.getInternalName(InvokeDynamicSupport.class);

    MethodType bootstrap =
        methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
    String bootstrapMethod =
        bootstrap.appendParameterTypes(MethodHandle.class).toMethodDescriptorString();
    String bootstrapIntrinsic =
        bootstrap.appendParameterTypes(String.class).toMethodDescriptorString();

    BOOTSTRAP_INIT = new Handle(Opcodes.H_INVOKESTATIC, className, "bootstrapInit", bootstrap.toMethodDescriptorString());
    BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, className, "bootstrap", bootstrapMethod);
    BOOTSTRAP_STATIC = new Handle(Opcodes.H_INVOKESTATIC, className, "bootstrapStatic", bootstrapMethod);
    BOOTSTRAP_INTRINSIC = new Handle(Opcodes.H_INVOKESTATIC, className, "bootstrapIntrinsic", bootstrapIntrinsic);
  }

  public InvokeDynamicClassInstrumentor(Decorator decorator) {
    super(decorator);
  }

  @Override
  protected void addDirectCallConstructor(MutableClass mutableClass) {
    // not needed, for reasons.
  }

  @Override
  protected void writeCallToInitializing(MutableClass mutableClass,
      RobolectricGeneratorAdapter generator) {
    generator.invokeDynamic("initializing",
        Type.getMethodDescriptor(OBJECT_TYPE, mutableClass.classType), BOOTSTRAP_INIT);
  }

  @Override
  protected void generateClassHandlerCall(MutableClass mutableClass, MethodNode originalMethod,
      String originalMethodName, RobolectricGeneratorAdapter generator) {
    generateInvokeDynamic(mutableClass, originalMethod, originalMethodName, generator);
  }

  // todo javadocs
  private void generateInvokeDynamic(MutableClass mutableClass, MethodNode originalMethod,
      String originalMethodName, RobolectricGeneratorAdapter generator) {
    Handle original =
        new Handle(getTag(originalMethod), mutableClass.classType.getInternalName(),
            originalMethod.name, originalMethod.desc);

    if (generator.isStatic()) {
      generator.loadArgs();
      generator.invokeDynamic(originalMethodName, originalMethod.desc, BOOTSTRAP_STATIC, original);
    } else {
      String desc = "(" + mutableClass.classType.getDescriptor() + originalMethod.desc.substring(1);
      generator.loadThis();
      generator.loadArgs();
      generator.invokeDynamic(originalMethodName, desc, BOOTSTRAP, original);
    }

    generator.returnValue();
  }

  @Override
  protected void interceptInvokeVirtualMethod(MutableClass mutableClass,
      ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    interceptInvokeVirtualMethodWithInvokeDynamic(instructions, targetMethod);
  }

  /**
   * Intercepts the method using the invokedynamic bytecode instruction available in Java 7+.
   * Should be called through interceptInvokeVirtualMethod, not directly.
   */
  private void interceptInvokeVirtualMethodWithInvokeDynamic(
      ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    instructions.remove();  // remove the method invocation

    Type type = Type.getObjectType(targetMethod.owner);
    String description = targetMethod.desc;
    String owner = type.getClassName();

    if (targetMethod.getOpcode() != Opcodes.INVOKESTATIC) {
      String thisType = type.getDescriptor();
      description = "(" + thisType + description.substring(1);
    }

    instructions.add(new InvokeDynamicInsnNode(targetMethod.name, description, BOOTSTRAP_INTRINSIC, owner));
  }
}
