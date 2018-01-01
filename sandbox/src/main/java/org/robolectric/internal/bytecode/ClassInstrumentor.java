package org.robolectric.internal.bytecode;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.annotation.Nonnull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

abstract class ClassInstrumentor {
  private static final String ROBO_INIT_METHOD_NAME = "$$robo$init";
  static final String GET_ROBO_DATA_SIGNATURE = "()Ljava/lang/Object;";
  private SandboxClassLoader sandboxClassLoader;
  final Type OBJECT_TYPE = Type.getType(Object.class);
  private final String OBJECT_DESC = Type.getDescriptor(Object.class);

  final ClassNode classNode;
  private final boolean containsStubs;
  final String internalClassName;
  private final String className;
  final Type classType;

  ClassInstrumentor(SandboxClassLoader sandboxClassLoader, ClassNode classNode, boolean containsStubs) {
    this.sandboxClassLoader = sandboxClassLoader;
    this.classNode = classNode;
    this.containsStubs = containsStubs;

    this.internalClassName = classNode.name;
    this.className = classNode.name.replace('/', '.');
    this.classType = Type.getObjectType(internalClassName);
  }

  //todo javadoc. Extract blocks to separate methods.
  public void instrument() {
    makeClassPublic(classNode);
    classNode.access = classNode.access & ~Opcodes.ACC_FINAL;

    // Need Java version >=7 to allow invokedynamic
    classNode.version = Math.max(classNode.version, Opcodes.V1_7);

    classNode.fields.add(0, new FieldNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
        ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_DESC, OBJECT_DESC, null));

    Set<String> foundMethods = instrumentMethods();

    // If there is no constructor, adds one
    addNoArgsConstructor(foundMethods);

    addDirectCallConstructor();

    // Do not override final #equals, #hashCode, and #toString for all classes
    instrumentInheritedObjectMethod(classNode, foundMethods, "equals", "(Ljava/lang/Object;)Z");
    instrumentInheritedObjectMethod(classNode, foundMethods, "hashCode", "()I");
    instrumentInheritedObjectMethod(classNode, foundMethods, "toString", "()Ljava/lang/String;");

    addRoboInitMethod();

    addRoboGetDataMethod();

    doSpecialHandling();
  }

  @Nonnull
  private Set<String> instrumentMethods() {
    Set<String> foundMethods = new HashSet<>();
    List<MethodNode> methods = new ArrayList<>(classNode.methods);
    for (MethodNode method : methods) {
      foundMethods.add(method.name + method.desc);

      filterSpecialMethods(method);

      if (method.name.equals("<clinit>")) {
        method.name = ShadowConstants.STATIC_INITIALIZER_METHOD_NAME;
        classNode.methods.add(generateStaticInitializerNotifierMethod());
      } else if (method.name.equals("<init>")) {
        instrumentConstructor(method);
      } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
        instrumentNormalMethod(method);
      }
    }
    return foundMethods;
  }

  private void addNoArgsConstructor(Set<String> foundMethods) {
    if (!foundMethods.contains("<init>()V")) {
      MethodNode defaultConstructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", "()V", null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(defaultConstructor);
      generator.loadThis();
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false);
      generator.loadThis();
      generator.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
      generator.returnValue();
      classNode.methods.add(defaultConstructor);
    }
  }

  abstract protected void addDirectCallConstructor();

  private void addRoboInitMethod() {
    MethodNode initMethodNode = new MethodNode(Opcodes.ACC_PROTECTED, ROBO_INIT_METHOD_NAME, "()V", null, null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    Label alreadyInitialized = new Label();
    generator.loadThis();                                         // this
    generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
    generator.ifNonNull(alreadyInitialized);
    generator.loadThis();                                         // this
    generator.loadThis();                                         // this, this
    writeCallToInitializing(generator);
    // this, __robo_data__
    generator.putField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
    generator.mark(alreadyInitialized);
    generator.returnValue();
    classNode.methods.add(initMethodNode);
  }

  abstract protected void writeCallToInitializing(RobolectricGeneratorAdapter generator);

  private void addRoboGetDataMethod() {
    MethodNode initMethodNode = new MethodNode(Opcodes.ACC_PUBLIC, ShadowConstants.GET_ROBO_DATA_METHOD_NAME, GET_ROBO_DATA_SIGNATURE, null, null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    generator.loadThis();                                         // this
    generator.getField(classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
    generator.returnValue();
    generator.endMethod();
    classNode.methods.add(initMethodNode);
  }

  private void doSpecialHandling() {
    if (className.equals("android.os.Build$VERSION")) {
      for (Object field : classNode.fields) {
        FieldNode fieldNode = (FieldNode) field;
        fieldNode.access &= ~(Modifier.FINAL);
      }
    }
  }

  /**
   * Checks if the given method in the class if overriding, at some point of it's
   * inheritance tree, a final method
   */
  private boolean isOverridingFinalMethod(ClassNode classNode, String methodName, String methodSignature) {
    while (true) {
      List<MethodNode> methods = new ArrayList<>(classNode.methods);

      for (MethodNode method : methods) {
        if (method.name.equals(methodName) && method.desc.equals(methodSignature)) {
          if ((method.access & Opcodes.ACC_FINAL) != 0) {
            return true;
          }
        }
      }

      if (classNode.superName == null) {
        return false;
      }

      try {
        byte[] byteCode = sandboxClassLoader.getByteCode(classNode.superName);
        ClassReader classReader = new ClassReader(byteCode);
        classNode = new ClassNode();
        classReader.accept(classNode, 0);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }

    }
  }

  private boolean isSyntheticAccessorMethod(MethodNode method) {
    return (method.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  /**
   * To be used to instrument methods inherited from the Object class,
   * such as hashCode, equals, and toString.
   * Adds the methods directly to the class.
   */
  private void instrumentInheritedObjectMethod(ClassNode classNode, Set<String> foundMethods, final String methodName, String methodDesc) {
    // Won't instrument if method is overriding a final method
    if (isOverridingFinalMethod(classNode, methodName, methodDesc)) {
      return;
    }

    // if the class doesn't directly override the method, it adds it as a direct invocation and instruments it
    if (!foundMethods.contains(methodName + methodDesc)) {
      MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, methodDesc, null, null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
      generator.invokeMethod("java/lang/Object", methodNode);
      generator.returnValue();
      generator.endMethod();
      this.classNode.methods.add(methodNode);
      instrumentNormalMethod(methodNode);
    }
  }

  private void instrumentConstructor(MethodNode method) {
    makeMethodPrivate(method);

    if (containsStubs) {
      method.instructions.clear();

      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(method);
      generator.loadThis();
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, classNode.superName, "<init>", "()V", false);
      generator.returnValue();
      generator.endMethod();
    }

    InsnList removedInstructions = extractCallToSuperConstructor(method);
    method.name = new ShadowImpl().directMethodName(ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    classNode.methods.add(redirectorMethod(method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

    String[] exceptions = exceptionArray(method);
    MethodNode methodNode = new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
    makeMethodPublic(methodNode);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);

    methodNode.instructions = removedInstructions;

    generator.loadThis();
    generator.invokeVirtual(classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
    generateShadowCall(method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, generator);

    generator.endMethod();
    classNode.methods.add(methodNode);
  }

  private InsnList extractCallToSuperConstructor(MethodNode ctor) {
    InsnList removedInstructions = new InsnList();
    int startIndex = 0;

    AbstractInsnNode[] insns = ctor.instructions.toArray();
    for (int i = 0; i < insns.length; i++) {
      AbstractInsnNode node = insns[i];

      switch (node.getOpcode()) {
        case Opcodes.ALOAD:
          VarInsnNode vnode = (VarInsnNode) node;
          if (vnode.var == 0) {
            startIndex = i;
          }
          break;

        case Opcodes.INVOKESPECIAL:
          MethodInsnNode mnode = (MethodInsnNode) node;
          if (mnode.owner.equals(internalClassName) || mnode.owner.equals(classNode.superName)) {
            assert mnode.name.equals("<init>");

            // remove all instructions in the range startIndex..i, from aload_0 to invokespecial <init>
            while (startIndex <= i) {
              ctor.instructions.remove(insns[startIndex]);
              removedInstructions.add(insns[startIndex]);
              startIndex++;
            }
            return removedInstructions;
          }
          break;

        case Opcodes.ATHROW:
          ctor.visitCode();
          ctor.visitInsn(Opcodes.RETURN);
          ctor.visitEnd();
          return removedInstructions;
      }
    }

    throw new RuntimeException("huh? " + ctor.name + ctor.desc);
  }

  //TODO javadocs
  private void instrumentNormalMethod(MethodNode method) {
    // if not abstract, set a final modifier
    if ((method.access & Opcodes.ACC_ABSTRACT) == 0) {
      method.access = method.access | Opcodes.ACC_FINAL;
    }
    // if a native method, remove native modifier and force return a default value
    if ((method.access & Opcodes.ACC_NATIVE) != 0) {
      method.access = method.access & ~Opcodes.ACC_NATIVE;

      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(method);
      Type returnType = generator.getReturnType();
      generator.pushDefaultReturnValueToStack(returnType);
      generator.returnValue();
    }

    // todo figure out
    String originalName = method.name;
    method.name = new ShadowImpl().directMethodName(originalName);

    MethodNode delegatorMethodNode = new MethodNode(method.access, originalName, method.desc, method.signature, exceptionArray(method));
    delegatorMethodNode.visibleAnnotations = method.visibleAnnotations;
    delegatorMethodNode.access &= ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);

    makeMethodPrivate(method);

    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(delegatorMethodNode);

    generateShadowCall(method, originalName, generator);

    generator.endMethod();

    classNode.methods.add(delegatorMethodNode);
  }

  //todo rename
  private MethodNode redirectorMethod(MethodNode method, String newName) {
    MethodNode redirector = new MethodNode(Opcodes.ASM4, newName, method.desc, method.signature, exceptionArray(method));
    redirector.access = method.access & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);
    makeMethodPrivate(redirector);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(redirector);
    generator.invokeMethod(internalClassName, method);
    generator.returnValue();
    return redirector;
  }

  private String[] exceptionArray(MethodNode method) {
    return ((List<String>) method.exceptions).toArray(new String[method.exceptions.size()]);
  }

  /**
   * Filters methods that might need special treatment because of various reasons
   */
  private void filterSpecialMethods(MethodNode callingMethod) {
    ListIterator<AbstractInsnNode> instructions = callingMethod.instructions.iterator();
    while (instructions.hasNext()) {
      AbstractInsnNode node = instructions.next();

      switch (node.getOpcode()) {
        case Opcodes.NEW:
          TypeInsnNode newInsnNode = (TypeInsnNode) node;
          newInsnNode.desc = sandboxClassLoader.remapType(newInsnNode.desc);
          break;

        case Opcodes.GETFIELD:
          /* falls through */
        case Opcodes.PUTFIELD:
          /* falls through */
        case Opcodes.GETSTATIC:
          /* falls through */
        case Opcodes.PUTSTATIC:
          FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
          fieldInsnNode.desc = sandboxClassLoader.remapType(fieldInsnNode.desc); // todo test
          break;

        case Opcodes.INVOKESTATIC:
          /* falls through */
        case Opcodes.INVOKEINTERFACE:
          /* falls through */
        case Opcodes.INVOKESPECIAL:
          /* falls through */
        case Opcodes.INVOKEVIRTUAL:
          MethodInsnNode targetMethod = (MethodInsnNode) node;
          targetMethod.desc = sandboxClassLoader.remapParams(targetMethod.desc);
          if (isGregorianCalendarBooleanConstructor(targetMethod)) {
            replaceGregorianCalendarBooleanConstructor(instructions, targetMethod);
          } else if (sandboxClassLoader.shouldIntercept(targetMethod)) {
            interceptInvokeVirtualMethod(instructions, targetMethod);
          }
          break;

        case Opcodes.INVOKEDYNAMIC:
          /* no unusual behavior */
          break;

        default:
          break;
      }
    }
  }

  /**
   * Verifies if the @targetMethod is a <init>(boolean) constructor for {@link java.util.GregorianCalendar}
   */
  private boolean isGregorianCalendarBooleanConstructor(MethodInsnNode targetMethod) {
    return targetMethod.owner.equals("java/util/GregorianCalendar") &&
        targetMethod.name.equals("<init>") &&
        targetMethod.desc.equals("(Z)V");
  }

  /**
   * Replaces the void <init> (boolean) constructor for a call to the void <init> (int, int, int) one
   */
  private void replaceGregorianCalendarBooleanConstructor(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    // Remove the call to GregorianCalendar(boolean)
    instructions.remove();

    // Discard the already-pushed parameter for GregorianCalendar(boolean)
    instructions.add(new InsnNode(Opcodes.POP));

    // Add parameters values for calling GregorianCalendar(int, int, int)
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new InsnNode(Opcodes.ICONST_0));

    // Call GregorianCalendar(int, int, int)
    instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetMethod.owner, targetMethod.name, "(III)V", targetMethod.itf));
  }

  /**
   * Decides to call through the appropriate method to intercept the method with an INVOKEVIRTUAL Opcode,
   * depending if the invokedynamic bytecode instruction is available (Java 7+)
   */
  abstract protected void interceptInvokeVirtualMethod(ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod);

  /**
   * Replaces protected and private class modifiers with public
   */
  private void makeClassPublic(ClassNode clazz) {
    clazz.access = (clazz.access | Opcodes.ACC_PUBLIC) & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  /**
   * Replaces protected and private method modifiers with public
   */
  private void makeMethodPublic(MethodNode method) {
    method.access = (method.access | Opcodes.ACC_PUBLIC) & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  /**
   * Replaces protected and public class modifiers with private
   */
  private void makeMethodPrivate(MethodNode method) {
    method.access = (method.access | Opcodes.ACC_PRIVATE) & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
  }

  private MethodNode generateStaticInitializerNotifierMethod() {
    MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", "()V", null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
    generator.push(classType);
    generator.invokeStatic(Type.getType(RobolectricInternals.class), new Method("classInitializing", "(Ljava/lang/Class;)V"));
    generator.returnValue();
    generator.endMethod();
    return methodNode;
  }

  // todo javadocs
  protected abstract void generateShadowCall(MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator);

  int getTag(MethodNode m) {
    return Modifier.isStatic(m.access) ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKESPECIAL;
  }
}
