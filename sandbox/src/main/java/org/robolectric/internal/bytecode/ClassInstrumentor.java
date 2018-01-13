package org.robolectric.internal.bytecode;

import com.google.common.collect.ImmutableSet;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
  static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final ShadowImpl SHADOW_IMPL = new ShadowImpl();
  final Decorator decorator;

  protected ClassInstrumentor(Decorator decorator) {
    this.decorator = decorator;
  }

  class Subject {
    final SandboxClassLoader sandboxClassLoader;
    final TypeMapper typeMapper;
    final ClassNode classNode;
    private final boolean containsStubs;
    final String internalClassName;
    private final String className;
    final Type classType;
    final ImmutableSet<String> foundMethods;

    Subject(SandboxClassLoader sandboxClassLoader, TypeMapper typeMapper, ClassNode classNode, boolean containsStubs) {
      this.sandboxClassLoader = sandboxClassLoader;
      this.typeMapper = typeMapper;
      this.classNode = classNode;
      this.containsStubs = containsStubs;

      this.internalClassName = classNode.name;
      this.className = classNode.name.replace('/', '.');
      this.classType = Type.getObjectType(internalClassName);

      List<String> foundMethods = new ArrayList<>(classNode.methods.size());
      for (MethodNode methodNode : getMethods()) {
        foundMethods.add(methodNode.name + methodNode.desc);
      }
      this.foundMethods = ImmutableSet.copyOf(foundMethods);
    }

    public void addMethod(MethodNode methodNode) {
      classNode.methods.add(methodNode);
    }

    public void addField(int index, FieldNode fieldNode) {
      classNode.fields.add(index, fieldNode);
    }

    public Iterable<? extends MethodNode> getMethods() {
      return new ArrayList<>(classNode.methods);
    }

    public void addInterface(String internalName) {
      classNode.interfaces.add(internalName);
    }
  }

  public void instrument(SandboxClassLoader sandboxClassLoader, TypeMapper typeMapper, ClassNode classNode, boolean containsStubs) {
    instrument(new Subject(sandboxClassLoader, typeMapper, classNode, containsStubs));
  }

  //todo javadoc. Extract blocks to separate methods.
  public void instrument(Subject subject) {
    makeClassPublic(subject.classNode);
    subject.classNode.access = subject.classNode.access & ~Opcodes.ACC_FINAL;

    // Need Java version >=7 to allow invokedynamic
    subject.classNode.version = Math.max(subject.classNode.version, Opcodes.V1_7);

    instrumentMethods(subject);

    // If there is no constructor, adds one
    addNoArgsConstructor(subject);

    addDirectCallConstructor(subject);

    // Do not override final #equals, #hashCode, and #toString for all classes
    instrumentInheritedObjectMethod(subject, "equals", "(Ljava/lang/Object;)Z");
    instrumentInheritedObjectMethod(subject, "hashCode", "()I");
    instrumentInheritedObjectMethod(subject, "toString", "()Ljava/lang/String;");

    addRoboInitMethod(subject);

    decorator.decorate(subject);

    doSpecialHandling(subject);
  }

  private void instrumentMethods(Subject subject) {
    for (MethodNode method : subject.getMethods()) {
      rewriteMethodBody(subject, method);

      if (method.name.equals("<clinit>")) {
        method.name = ShadowConstants.STATIC_INITIALIZER_METHOD_NAME;
        subject.addMethod(generateStaticInitializerNotifierMethod(subject));
      } else if (method.name.equals("<init>")) {
        instrumentConstructor(subject, method);
      } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
        instrumentNormalMethod(subject, method);
      }
    }
  }

  private void addNoArgsConstructor(Subject subject) {
    if (!subject.foundMethods.contains("<init>()V")) {
      MethodNode defaultConstructor = new MethodNode(Opcodes.ACC_PUBLIC, "<init>", "()V", "()V", null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(defaultConstructor);
      generator.loadThis();
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, subject.classNode.superName, "<init>", "()V", false);
      generator.loadThis();
      generator.invokeVirtual(subject.classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
      generator.returnValue();
      subject.addMethod(defaultConstructor);
    }
  }

  abstract protected void addDirectCallConstructor(Subject subject);

  private void addRoboInitMethod(Subject subject) {
    MethodNode initMethodNode = new MethodNode(Opcodes.ACC_PROTECTED, ROBO_INIT_METHOD_NAME, "()V", null, null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    Label alreadyInitialized = new Label();
    generator.loadThis();                                         // this
    generator.getField(subject.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
    generator.ifNonNull(alreadyInitialized);
    generator.loadThis();                                         // this
    generator.loadThis();                                         // this, this
    writeCallToInitializing(subject, generator);
    // this, __robo_data__
    generator.putField(subject.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
    generator.mark(alreadyInitialized);
    generator.returnValue();
    subject.addMethod(initMethodNode);
  }

  abstract protected void writeCallToInitializing(Subject subject, RobolectricGeneratorAdapter generator);

  private void doSpecialHandling(Subject subject) {
    if (subject.className.equals("android.os.Build$VERSION")) {
      for (Object field : subject.classNode.fields) {
        FieldNode fieldNode = (FieldNode) field;
        fieldNode.access &= ~(Modifier.FINAL);
      }
    }
  }

  /**
   * Checks if the given method in the class if overriding, at some point of it's
   * inheritance tree, a final method
   */
  private boolean isOverridingFinalMethod(Subject subject, String methodName, String methodSignature) {
    ClassNode classNode = subject.classNode;
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
        byte[] byteCode = subject.sandboxClassLoader.getByteCode(classNode.superName);
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
  private void instrumentInheritedObjectMethod(Subject subject, final String methodName, String methodDesc) {
    // Won't instrument if method is overriding a final method
    if (isOverridingFinalMethod(subject, methodName, methodDesc)) {
      return;
    }

    // if the class doesn't directly override the method, it adds it as a direct invocation and instruments it
    if (!subject.foundMethods.contains(methodName + methodDesc)) {
      MethodNode methodNode = new MethodNode(Opcodes.ACC_PUBLIC, methodName, methodDesc, null, null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
      generator.invokeMethod("java/lang/Object", methodNode);
      generator.returnValue();
      generator.endMethod();
      subject.addMethod(methodNode);
      instrumentNormalMethod(subject, methodNode);
    }
  }

  /**
   * Constructors are instrumented as follows:
   * # Code other than a call to the superclass constructor is moved to a new method named
   *   `__constructor__` with the same signature.
   * # The constructor is modified to call {@link ClassHandler#initializing(Object)} (or
   *   {@link ClassHandler#getShadowCreator(Class)} for `invokedynamic` JVMs).
   * # The constructor is modified to then call
   *   {@link ClassHandler#methodInvoked(String, boolean, Class)} (or
   *   {@link ClassHandler#findShadowMethodHandle(Class, String, MethodType, boolean)} for
   *   `invokedynamic` JVMs) with the method name `__constructor__` and the same parameter types.
   *
   * Note that most code in the constructor will not be executed unless the {@link ClassHandler}
   * arranges for it to happen.
   *
   * @param method the constructor to instrument
   */
  private void instrumentConstructor(Subject subject, MethodNode method) {
    makeMethodPrivate(method);

    if (subject.containsStubs) {
      // method.instructions just throws a `stub!` exception, replace it with something anodyne...
      method.instructions.clear();

      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(method);
      generator.loadThis();
      generator.visitMethodInsn(Opcodes.INVOKESPECIAL, subject.classNode.superName, "<init>", "()V", false);
      generator.returnValue();
      generator.endMethod();
    }

    InsnList callSuper = extractCallToSuperConstructor(subject, method);
    method.name = directMethodName(ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    subject.addMethod(redirectorMethod(subject, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

    String[] exceptions = exceptionArray(method);
    MethodNode methodNode = new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
    makeMethodPublic(methodNode);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);

    methodNode.instructions = callSuper;

    generator.loadThis();
    generator.invokeVirtual(subject.classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
    generateClassHandlerCall(subject, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, generator);

    generator.endMethod();
    subject.addMethod(methodNode);
  }

  private InsnList extractCallToSuperConstructor(Subject subject, MethodNode ctor) {
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
          if (mnode.owner.equals(subject.internalClassName) || mnode.owner.equals(subject.classNode.superName)) {
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

  /**
   * # Rename the method from `methodName` to `$$robo$$methodName`.
   * # Make it private so we can invoke it directly without subclass overrides taking precedence.
   * # Remove `final` and `native` modifiers, if present.
   * # Create a delegator method named `methodName` which delegates to the {@link ClassHandler}.
   */
  private void instrumentNormalMethod(Subject subject, MethodNode method) {
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
    method.name = directMethodName(originalName);

    MethodNode delegatorMethodNode = new MethodNode(method.access, originalName, method.desc, method.signature, exceptionArray(method));
    delegatorMethodNode.visibleAnnotations = method.visibleAnnotations;
    delegatorMethodNode.access &= ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);

    makeMethodPrivate(method);

    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(delegatorMethodNode);
    generateClassHandlerCall(subject, method, originalName, generator);
    generator.endMethod();
    subject.addMethod(delegatorMethodNode);
  }

  private String directMethodName(String originalName) {
    return SHADOW_IMPL.directMethodName(originalName);
  }

  //todo rename
  private MethodNode redirectorMethod(Subject subject, MethodNode method, String newName) {
    MethodNode redirector = new MethodNode(Opcodes.ASM4, newName, method.desc, method.signature, exceptionArray(method));
    redirector.access = method.access & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);
    makeMethodPrivate(redirector);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(redirector);
    generator.invokeMethod(subject.internalClassName, method);
    generator.returnValue();
    return redirector;
  }

  private String[] exceptionArray(MethodNode method) {
    return ((List<String>) method.exceptions).toArray(new String[method.exceptions.size()]);
  }

  /**
   * Filters methods that might need special treatment because of various reasons
   */
  private void rewriteMethodBody(Subject subject, MethodNode callingMethod) {
    ListIterator<AbstractInsnNode> instructions = callingMethod.instructions.iterator();
    while (instructions.hasNext()) {
      AbstractInsnNode node = instructions.next();

      switch (node.getOpcode()) {
        case Opcodes.NEW:
          TypeInsnNode newInsnNode = (TypeInsnNode) node;
          newInsnNode.desc = subject.typeMapper.mappedTypeName(newInsnNode.desc);
          break;

        case Opcodes.GETFIELD:
          /* falls through */
        case Opcodes.PUTFIELD:
          /* falls through */
        case Opcodes.GETSTATIC:
          /* falls through */
        case Opcodes.PUTSTATIC:
          FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
          fieldInsnNode.desc = subject.typeMapper.mappedTypeName(fieldInsnNode.desc); // todo test
          break;

        case Opcodes.INVOKESTATIC:
          /* falls through */
        case Opcodes.INVOKEINTERFACE:
          /* falls through */
        case Opcodes.INVOKESPECIAL:
          /* falls through */
        case Opcodes.INVOKEVIRTUAL:
          MethodInsnNode targetMethod = (MethodInsnNode) node;
          targetMethod.desc = subject.typeMapper.remapParams(targetMethod.desc);
          if (isGregorianCalendarBooleanConstructor(targetMethod)) {
            replaceGregorianCalendarBooleanConstructor(instructions, targetMethod);
          } else if (subject.sandboxClassLoader.shouldIntercept(targetMethod)) {
            interceptInvokeVirtualMethod(subject, instructions, targetMethod);
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
  abstract protected void interceptInvokeVirtualMethod(Subject subject, ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod);

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

  private MethodNode generateStaticInitializerNotifierMethod(Subject subject) {
    MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", "()V", null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
    generator.push(subject.classType);
    generator.invokeStatic(Type.getType(RobolectricInternals.class), new Method("classInitializing", "(Ljava/lang/Class;)V"));
    generator.returnValue();
    generator.endMethod();
    return methodNode;
  }

  // todo javadocs
  protected abstract void generateClassHandlerCall(Subject subject, MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator);

  int getTag(MethodNode m) {
    return Modifier.isStatic(m.access) ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKESPECIAL;
  }

  public interface Decorator {
    void decorate(Subject subject);

    void decorateMethodPreClassHandler(Subject subject, MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator);
  }
}
