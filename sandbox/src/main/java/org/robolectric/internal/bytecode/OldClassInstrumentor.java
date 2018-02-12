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
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class OldClassInstrumentor extends ClassInstrumentor {
  private static final Type PLAN_TYPE = Type.getType(ClassHandler.Plan.class);
  static final Type THROWABLE_TYPE = Type.getType(Throwable.class);
  static final Type ROBOLECTRIC_INTERNALS_TYPE = Type.getType(RobolectricInternals.class);
  private static final Method INITIALIZING_METHOD = new Method("initializing", "(Ljava/lang/Object;)Ljava/lang/Object;");
  private static final Method METHOD_INVOKED_METHOD = new Method("methodInvoked", "(Ljava/lang/String;ZLjava/lang/Class;)L" + PLAN_TYPE.getInternalName() + ";");
  private static final Method PLAN_RUN_METHOD = new Method("run", OBJECT_TYPE, new Type[]{OBJECT_TYPE, Type.getType(Object[].class)});
  static final Method HANDLE_EXCEPTION_METHOD = new Method("cleanStackTrace", THROWABLE_TYPE, new Type[]{THROWABLE_TYPE});
  private static final String DIRECT_OBJECT_MARKER_TYPE_DESC = Type.getObjectType(DirectObjectMarker.class.getName().replace('.', '/')).getDescriptor();

  public OldClassInstrumentor(ClassInstrumentor.Decorator decorator) {
    super(decorator);
  }

  /**
   * Generates code like this:
   * ```java
   * public ThisClass(DirectObjectMarker dom, ThisClass domInstance) {
   *   super(dom, domInstance);
   *   __robo_data__ = domInstance;
   * }
   * ```
   */
  @Override
  protected void addDirectCallConstructor(MutableClass mutableClass) {
    MethodNode directCallConstructor = new MethodNode(Opcodes.ACC_PUBLIC,
        "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + mutableClass.classType.getDescriptor() + ")V", null, null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(directCallConstructor);
    generator.loadThis();
    String superName = mutableClass.classNode.superName;
    if (superName.equals("java/lang/Object")) {
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "()V", false);
    } else {
      generator.loadArgs();
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, superName,
          "<init>", "(" + DIRECT_OBJECT_MARKER_TYPE_DESC + "L" + superName + ";)V", false);
    }
    generator.loadThis();
    generator.loadArg(1);
    generator.putField(mutableClass.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
    generator.returnValue();
    mutableClass.addMethod(directCallConstructor);
  }

  @Override
  protected void writeCallToInitializing(MutableClass mutableClass,
      RobolectricGeneratorAdapter generator) {
    generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, INITIALIZING_METHOD);
  }

  @Override
  protected void generateClassHandlerCall(MutableClass mutableClass, MethodNode originalMethod,
      String originalMethodName, RobolectricGeneratorAdapter generator) {
    generateCallToClassHandler(mutableClass, originalMethod, originalMethodName, generator);
  }

  /**
   * Generates codelike this:
   * ```java
   * // decorator-specific code...
   *
   * Plan plan = RobolectricInternals.methodInvoked(
   *     "pkg/ThisClass/thisMethod(Ljava/lang/String;Z)V", isStatic, ThisClass.class);
   * if (plan != null) {
   *   try {
   *     return plan.run(this, args);
   *   } catch (Throwable t) {
   *     throw RobolectricInternals.cleanStackTrace(t);
   *   }
   * } else {
   *   return $$robo$$thisMethod(*args);
   * }
   * ```
   */
  private void generateCallToClassHandler(MutableClass mutableClass, MethodNode originalMethod,
      String originalMethodName, RobolectricGeneratorAdapter generator) {
    decorator.decorateMethodPreClassHandler(mutableClass, originalMethod, originalMethodName, generator);

    int planLocalVar = generator.newLocal(PLAN_TYPE);
    int exceptionLocalVar = generator.newLocal(THROWABLE_TYPE);
    Label directCall = new Label();
    Label doReturn = new Label();

    // prepare for call to classHandler.methodInvoked(String signature, boolean isStatic)
    generator.push(mutableClass.classType.getInternalName() + "/" + originalMethodName + originalMethod.desc);
    generator.push(generator.isStatic());
    generator.push(mutableClass.classType);                                         // my class
    generator.invokeStatic(ROBOLECTRIC_INTERNALS_TYPE, METHOD_INVOKED_METHOD);
    generator.storeLocal(planLocalVar);

    generator.loadLocal(planLocalVar); // plan
    generator.ifNull(directCall);

    // prepare for call to plan.run(Object instance, Object[] params)
    TryCatch tryCatchForHandler = generator.tryStart(THROWABLE_TYPE);
    generator.loadLocal(planLocalVar); // plan
    generator.loadThisOrNull();        // instance
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
      TryCatch tryCatchForDirect = generator.tryStart(THROWABLE_TYPE);
      generator.invokeMethod(mutableClass.classType.getInternalName(), originalMethod.name, originalMethod.desc);
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
   * Decides to call through the appropriate method to intercept the method with an INVOKEVIRTUAL
   * Opcode, depending if the invokedynamic bytecode instruction is available (Java 7+).
   */
  @Override
  protected void interceptInvokeVirtualMethod(MutableClass mutableClass,
      ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    interceptInvokeVirtualMethodWithoutInvokeDynamic(mutableClass, instructions, targetMethod);
  }

  /**
   * Intercepts the method without using the invokedynamic bytecode instruction.
   * Should be called through interceptInvokeVirtualMethod, not directly.
   */
  private void interceptInvokeVirtualMethodWithoutInvokeDynamic(MutableClass mutableClass,
      ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    boolean isStatic = targetMethod.getOpcode() == Opcodes.INVOKESTATIC;

    instructions.remove(); // remove the method invocation

    Type[] argumentTypes = Type.getArgumentTypes(targetMethod.desc);

    instructions.add(new LdcInsnNode(argumentTypes.length));
    instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));

    // first, move any arguments into an Object[] in reverse order
    for (int i = argumentTypes.length - 1; i >= 0; i--) {
      Type type = argumentTypes[i];
      int argWidth = type.getSize();

      if (argWidth == 1) {                               // A B C []
        instructions.add(new InsnNode(Opcodes.DUP_X1));  // A B [] C []
        instructions.add(new InsnNode(Opcodes.SWAP));    // A B [] [] C
        instructions.add(new LdcInsnNode(i));            // A B [] [] C 2
        instructions.add(new InsnNode(Opcodes.SWAP));    // A B [] [] 2 C
        box(type, instructions);                         // A B [] [] 2 (C)
        instructions.add(new InsnNode(Opcodes.AASTORE)); // A B [(C)]
      } else if (argWidth == 2) {                        // A B _C_ []
        instructions.add(new InsnNode(Opcodes.DUP_X2));  // A B [] _C_ []
        instructions.add(new InsnNode(Opcodes.DUP_X2));  // A B [] [] _C_ []
        instructions.add(new InsnNode(Opcodes.POP));     // A B [] [] _C_
        box(type, instructions);                         // A B [] [] (C)
        instructions.add(new LdcInsnNode(i));            // A B [] [] (C) 2
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

    instructions.add(new LdcInsnNode(mutableClass.classType)); // signature instance [] class
    instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
        Type.getType(RobolectricInternals.class).getInternalName(), "intercept",
        "(Ljava/lang/String;Ljava/lang/Object;[Ljava/lang/Object;Ljava/lang/Class;)Ljava/lang/Object;",
        false));

    final Type returnType = Type.getReturnType(targetMethod.desc);
    switch (returnType.getSort()) {
      case ARRAY:
        /* falls through */
      case OBJECT:
        String remappedType = mutableClass.config.mappedTypeName(returnType.getInternalName());
        instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, remappedType));
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

  static void box(final Type type, ListIterator<AbstractInsnNode> instructions) {
    if (type.getSort() == OBJECT || type.getSort() == ARRAY) {
      return;
    }

    if (Type.VOID_TYPE.equals(type)) {
      instructions.add(new InsnNode(Opcodes.ACONST_NULL));
    } else {
      Type boxed = getBoxedType(type);
      instructions.add(new TypeInsnNode(Opcodes.NEW, boxed.getInternalName()));
      if (type.getSize() == 2) {
        // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
        instructions.add(new InsnNode(Opcodes.DUP_X2));
        instructions.add(new InsnNode(Opcodes.DUP_X2));
        instructions.add(new InsnNode(Opcodes.POP));
      } else {
        // p -> po -> opo -> oop -> o
        instructions.add(new InsnNode(Opcodes.DUP_X1));
        instructions.add(new InsnNode(Opcodes.SWAP));
      }
      instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, boxed.getInternalName(),
          "<init>", "(" + type.getDescriptor() + ")V", false));
    }
  }

  private static Type getBoxedType(final Type type) {
    switch (type.getSort()) {
      case Type.BYTE:
        return Type.getObjectType("java/lang/Byte");
      case Type.BOOLEAN:
        return Type.getObjectType("java/lang/Boolean");
      case Type.SHORT:
        return Type.getObjectType("java/lang/Short");
      case Type.CHAR:
        return Type.getObjectType("java/lang/Character");
      case Type.INT:
        return Type.getObjectType("java/lang/Integer");
      case Type.FLOAT:
        return Type.getObjectType("java/lang/Float");
      case Type.LONG:
        return Type.getObjectType("java/lang/Long");
      case Type.DOUBLE:
        return Type.getObjectType("java/lang/Double");
      default:
        // no boxing required
        return type;
    }
  }
}
