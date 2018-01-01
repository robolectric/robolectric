package org.robolectric.internal.bytecode;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;
import static org.objectweb.asm.Type.VOID;

import java.util.ListIterator;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class OldClassInstrumentor extends ClassInstrumentor {
  private SandboxClassLoader sandboxClassLoader;
  private final Type PLAN_TYPE = Type.getType(ClassHandler.Plan.class);
  private final Type THROWABLE_TYPE = Type.getType(Throwable.class);
  private final Method INITIALIZING_METHOD = new Method("initializing", "(Ljava/lang/Object;)Ljava/lang/Object;");
  private final Method METHOD_INVOKED_METHOD = new Method("methodInvoked", "(Ljava/lang/String;ZLjava/lang/Class;)L" + PLAN_TYPE.getInternalName() + ";");
  private final Method PLAN_RUN_METHOD = new Method("run", OBJECT_TYPE, new Type[]{OBJECT_TYPE, OBJECT_TYPE, Type.getType(Object[].class)});
  private final Method HANDLE_EXCEPTION_METHOD = new Method("cleanStackTrace", THROWABLE_TYPE, new Type[]{THROWABLE_TYPE});
  private final String DIRECT_OBJECT_MARKER_TYPE_DESC = Type.getObjectType(DirectObjectMarker.class.getName().replace('.', '/')).getDescriptor();
  private final Type ROBOLECTRIC_INTERNALS_TYPE = Type.getType(RobolectricInternals.class);

  OldClassInstrumentor(SandboxClassLoader sandboxClassLoader, ClassNode classNode, boolean containsStubs) {
    super(sandboxClassLoader, classNode, containsStubs);
    this.sandboxClassLoader = sandboxClassLoader;
  }

  @Override
  protected void addDirectCallConstructor() {
    MethodNode directCallConstructor = new MethodNode(Opcodes.ACC_PUBLIC,
        "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + classType.getDescriptor() + ")V", null, null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(directCallConstructor);
    generator.loadThis();
    if (classNode.superName.equals("java/lang/Object")) {
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false);
    } else {
      generator.loadArgs();
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName,
          "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + "L" + classNode.superName + ";)V", false);
    }
    generator.loadThis();
    generator.loadArg(1);
    generator.putField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
    generator.returnValue();
    classNode.methods.add(directCallConstructor);
  }

  @Override
  protected void writeCallToInitializing(RobolectricGeneratorAdapter generator) {
    generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, INITIALIZING_METHOD);
  }

  @Override
  protected void generateShadowCall(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator) {
    generateCallToClassHandler(originalMethod, originalMethodName, generator);
  }

  //TODO clean up & javadocs
  private void generateCallToClassHandler(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator) {
    int planLocalVar = generator.newLocal(PLAN_TYPE);
    int exceptionLocalVar = generator.newLocal(THROWABLE_TYPE);
    Label directCall = new Label();
    Label doReturn = new Label();

    boolean isNormalInstanceMethod = !generator.isStatic && !originalMethodName.equals(ShadowConstants.CONSTRUCTOR_METHOD_NAME);

    // maybe perform proxy call...
    if (isNormalInstanceMethod) {
      Label notInstanceOfThis = new Label();

      generator.loadThis();                                         // this
      generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
      generator.instanceOf(classType);                              // __robo_data__, is instance of same class?
      generator.visitJumpInsn(Opcodes.IFEQ, notInstanceOfThis);             // jump if no (is not instance)

      SandboxClassLoader.TryCatch tryCatchForProxyCall = generator.tryStart(THROWABLE_TYPE);
      generator.loadThis();                                         // this
      generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
      generator.checkCast(classType);                               // __robo_data__ but cast to my class
      generator.loadArgs();                                         // __robo_data__ instance, [args]

      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, internalClassName, originalMethod.name, originalMethod.desc, false);
      tryCatchForProxyCall.end();

      generator.returnValue();

      // catch(Throwable)
      tryCatchForProxyCall.handler();
      generator.storeLocal(exceptionLocalVar);
      generator.loadLocal(exceptionLocalVar);
      generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
      generator.throwException();

      // callClassHandler...
      generator.mark(notInstanceOfThis);
    }

    // prepare for call to classHandler.methodInvoked(String signature, boolean isStatic)
    generator.push(classType.getInternalName() + "/" + originalMethodName + originalMethod.desc);
    generator.push(generator.isStatic());
    generator.push(classType);                                         // my class
    generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, METHOD_INVOKED_METHOD);
    generator.storeLocal(planLocalVar);

    generator.loadLocal(planLocalVar); // plan
    generator.ifNull(directCall);

    // prepare for call to plan.run(Object instance, Object[] params)
    SandboxClassLoader.TryCatch tryCatchForHandler = generator.tryStart(THROWABLE_TYPE);
    generator.loadLocal(planLocalVar); // plan
    generator.loadThisOrNull();        // instance
    if (generator.isStatic()) {        // roboData
      generator.loadNull();
    } else {
      generator.loadThis();
      generator.invokeVirtual(classType, new Method(ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE));
    }
    generator.loadArgArray();          // params
    generator.invokeInterface(PLAN_TYPE, PLAN_RUN_METHOD);

    Type returnType = generator.getReturnType();
    int sort = returnType.getSort();
    switch (sort) {
      case VOID:
        generator.pop();
        break;
      case OBJECT:
        /* falls through */
      case ARRAY:
        generator.checkCast(returnType);
        break;
      default:
        int unboxLocalVar = generator.newLocal(OBJECT_TYPE);
        generator.storeLocal(unboxLocalVar);
        generator.loadLocal(unboxLocalVar);
        Label notNull = generator.newLabel();
        Label afterward = generator.newLabel();
        generator.ifNonNull(notNull);
        generator.pushDefaultReturnValueToStack(returnType); // return zero, false, whatever
        generator.goTo(afterward);

        generator.mark(notNull);
        generator.loadLocal(unboxLocalVar);
        generator.unbox(returnType);
        generator.mark(afterward);
        break;
    }
    tryCatchForHandler.end();
    generator.goTo(doReturn);

    // catch(Throwable)
    tryCatchForHandler.handler();
    generator.storeLocal(exceptionLocalVar);
    generator.loadLocal(exceptionLocalVar);
    generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
    generator.throwException();


    if (!originalMethod.name.equals("<init>")) {
      generator.mark(directCall);
      SandboxClassLoader.TryCatch tryCatchForDirect = generator.tryStart(THROWABLE_TYPE);
      generator.invokeMethod(classType.getInternalName(), originalMethod.name, originalMethod.desc);
      tryCatchForDirect.end();
      generator.returnValue();

      // catch(Throwable)
      tryCatchForDirect.handler();
      generator.storeLocal(exceptionLocalVar);
      generator.loadLocal(exceptionLocalVar);
      generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, HANDLE_EXCEPTION_METHOD);
      generator.throwException();
    }

    generator.mark(doReturn);
    generator.returnValue();
  }

  /**
   * Decides to call through the appropriate method to intercept the method with an INVOKEVIRTUAL Opcode,
   * depending if the invokedynamic bytecode instruction is available (Java 7+)
   */
  @Override
  protected void interceptInvokeVirtualMethod(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    interceptInvokeVirtualMethodWithoutInvokeDynamic(instructions, targetMethod);
  }

  /**
   * Intercepts the method without using the invokedynamic bytecode instruction.
   * Should be called through interceptInvokeVirtualMethod, not directly
   */
  private void interceptInvokeVirtualMethodWithoutInvokeDynamic(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    boolean isStatic = targetMethod.getOpcode() == Opcodes.INVOKESTATIC;

    instructions.remove(); // remove the method invocation

    Type[] argumentTypes = Type.getArgumentTypes(targetMethod.desc);

    instructions.add(new LdcInsnNode(argumentTypes.length));
    instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));

    // first, move any arguments into an Object[] in reverse order
    for (int i = argumentTypes.length - 1; i >= 0; i--) {
      Type type = argumentTypes[i];
      int argWidth = type.getSize();

      if (argWidth == 1) {                       // A B C []
        instructions.add(new InsnNode(Opcodes.DUP_X1));  // A B [] C []
        instructions.add(new InsnNode(Opcodes.SWAP));    // A B [] [] C
        instructions.add(new LdcInsnNode(i));    // A B [] [] C 2
        instructions.add(new InsnNode(Opcodes.SWAP));    // A B [] [] 2 C
        SandboxClassLoader.box(type, instructions);                 // A B [] [] 2 (C)
        instructions.add(new InsnNode(Opcodes.AASTORE)); // A B [(C)]
      } else if (argWidth == 2) {                // A B _C_ []
        instructions.add(new InsnNode(Opcodes.DUP_X2));  // A B [] _C_ []
        instructions.add(new InsnNode(Opcodes.DUP_X2));  // A B [] [] _C_ []
        instructions.add(new InsnNode(Opcodes.POP));     // A B [] [] _C_
        SandboxClassLoader.box(type, instructions);                 // A B [] [] (C)
        instructions.add(new LdcInsnNode(i));    // A B [] [] (C) 2
        instructions.add(new InsnNode(Opcodes.SWAP));    // A B [] [] 2 (C)
        instructions.add(new InsnNode(Opcodes.AASTORE)); // A B [(C)]
      }
    }

    if (isStatic) { // []
      instructions.add(new InsnNode(Opcodes.ACONST_NULL)); // [] null
      instructions.add(new InsnNode(Opcodes.SWAP));        // null []
    }

    // instance []
    instructions.add(new LdcInsnNode(targetMethod.owner + "/" + targetMethod.name + targetMethod.desc)); // target method signature
    // instance [] signature
    instructions.add(new InsnNode(Opcodes.DUP_X2));       // signature instance [] signature
    instructions.add(new InsnNode(Opcodes.POP));          // signature instance []

    instructions.add(new LdcInsnNode(classType)); // signature instance [] class
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
        Type.getType(RobolectricInternals.class).getInternalName(), "intercept",
        "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
        false));

    final Type returnType = Type.getReturnType(targetMethod.desc);
    switch (returnType.getSort()) {
      case ARRAY:
        /* falls through */
      case OBJECT:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, sandboxClassLoader.remapType(returnType.getInternalName())));
        break;
      case VOID:
        instructions.add(new InsnNode(Opcodes.POP));
        break;
      case Type.LONG:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Long.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Long.class), "longValue", Type.getMethodDescriptor(Type.LONG_TYPE), false));
        break;
      case Type.FLOAT:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Float.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Float.class), "floatValue", Type.getMethodDescriptor(Type.FLOAT_TYPE), false));
        break;
      case Type.DOUBLE:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Double.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Double.class), "doubleValue", Type.getMethodDescriptor(Type.DOUBLE_TYPE), false));
        break;
      case Type.BOOLEAN:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Boolean.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Boolean.class), "booleanValue", Type.getMethodDescriptor(Type.BOOLEAN_TYPE), false));
        break;
      case Type.INT:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Integer.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Integer.class), "intValue", Type.getMethodDescriptor(Type.INT_TYPE), false));
        break;
      case Type.SHORT:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Short.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Short.class), "shortValue", Type.getMethodDescriptor(Type.SHORT_TYPE), false));
        break;
      case Type.BYTE:
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, Type.getInternalName(Byte.class)));
        instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, Type.getInternalName(Byte.class), "byteValue", Type.getMethodDescriptor(Type.BYTE_TYPE), false));
        break;
      default:
        throw new RuntimeException("Not implemented: " + getClass().getName() + " cannot intercept methods with return type " + returnType.getClassName());
    }
  }
}
