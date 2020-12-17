package org.robolectric.internal.bytecode;

import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ListIterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.Remapper;
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

public abstract class ClassInstrumentor {
  private static final String ROBO_INIT_METHOD_NAME = "$$robo$init";
  static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final ShadowImpl SHADOW_IMPL = new ShadowImpl();
  final Decorator decorator;

  protected ClassInstrumentor(Decorator decorator) {
    this.decorator = decorator;
  }

  public MutableClass analyzeClass(
      byte[] origClassBytes,
      final InstrumentationConfiguration config,
      ClassNodeProvider classNodeProvider) {
    ClassNode classNode =
        new ClassNode(Opcodes.ASM4) {
          @Override
          public FieldVisitor visitField(
              int access, String name, String desc, String signature, Object value) {
            desc = config.remapParamType(desc);
            return super.visitField(access & ~Opcodes.ACC_FINAL, name, desc, signature, value);
          }

          @Override
          public MethodVisitor visitMethod(
              int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor =
                super.visitMethod(access, name, config.remapParams(desc), signature, exceptions);
            return new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);
          }
        };

    final ClassReader classReader = new ClassReader(origClassBytes);
    classReader.accept(classNode, 0);
    return new MutableClass(classNode, config, classNodeProvider);
  }

  byte[] instrumentToBytes(MutableClass mutableClass) {
    instrument(mutableClass);

    ClassNode classNode = mutableClass.classNode;
    ClassWriter writer = new InstrumentingClassWriter(mutableClass.classNodeProvider, classNode);
    Remapper remapper =
        new Remapper() {
          @Override
          public String map(final String internalName) {
            return mutableClass.config.mappedTypeName(internalName);
          }
        };
    ClassRemapper visitor = new ClassRemapper(writer, remapper);
    classNode.accept(visitor);
    return writer.toByteArray();
  }

  public byte[] instrument(byte[] origBytes, InstrumentationConfiguration config,
      ClassNodeProvider classNodeProvider) {
    MutableClass mutableClass = analyzeClass(origBytes, config, classNodeProvider);
    return instrumentToBytes(mutableClass);
  }

  public void instrument(MutableClass mutableClass) {
    try {
      // no need to do anything to interfaces
      if (mutableClass.isInterface()) {
        return;
      }

      makeClassPublic(mutableClass.classNode);
      if ((mutableClass.classNode.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
        mutableClass.classNode.visitAnnotation(
            "Lcom/google/errorprone/annotations/DoNotMock;", true)
            .visit("value", "This class is final. Consider using the real thing, or "
                + "adding/enhancing a Robolectric shadow for it.");
      }
      mutableClass.classNode.access = mutableClass.classNode.access & ~Opcodes.ACC_FINAL;

      // Need Java version >=7 to allow invokedynamic
      mutableClass.classNode.version = Math.max(mutableClass.classNode.version, Opcodes.V1_7);

      instrumentMethods(mutableClass);

      // If there is no constructor, adds one
      addNoArgsConstructor(mutableClass);

      addDirectCallConstructor(mutableClass);

      addRoboInitMethod(mutableClass);

      decorator.decorate(mutableClass);

      doSpecialHandling(mutableClass);
    } catch (Exception e) {
      throw new RuntimeException("failed to instrument " + mutableClass.getName(), e);
    }
  }

  private void instrumentMethods(MutableClass mutableClass) {
    for (MethodNode method : mutableClass.getMethods()) {
      rewriteMethodBody(mutableClass, method);

      if (method.name.equals("<clinit>")) {
        method.name = ShadowConstants.STATIC_INITIALIZER_METHOD_NAME;
        mutableClass.addMethod(generateStaticInitializerNotifierMethod(mutableClass));
      } else if (method.name.equals("<init>")) {
        instrumentConstructor(mutableClass, method);
      } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
        instrumentNormalMethod(mutableClass, method);
      }
    }
  }

  private void addNoArgsConstructor(MutableClass mutableClass) {
    if (!mutableClass.foundMethods.contains("<init>()V")) {
      MethodNode defaultConstructor =
          new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, "<init>", "()V", "()V", null);
      RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(defaultConstructor);
      generator.loadThis();
      generator.visitMethodInsn(
          Opcodes.INVOKESPECIAL, mutableClass.classNode.superName, "<init>", "()V", false);
      generator.loadThis();
      generator.invokeVirtual(mutableClass.classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
      generator.returnValue();
      mutableClass.addMethod(defaultConstructor);
    }
  }

  protected abstract void addDirectCallConstructor(MutableClass mutableClass);

  /**
   * Generates code like this:
   *
   * <pre>
   * protected void $$robo$init() {
   *   if (__robo_data__ == null) {
   *     __robo_data__ = RobolectricInternals.initializing(this);
   *   }
   * }
   * </pre>
   */
  private void addRoboInitMethod(MutableClass mutableClass) {
    MethodNode initMethodNode =
        new MethodNode(
            Opcodes.ACC_PROTECTED | Opcodes.ACC_SYNTHETIC,
            ROBO_INIT_METHOD_NAME,
            "()V",
            null,
            null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    Label alreadyInitialized = new Label();
    generator.loadThis();                                         // this
    generator.getField(mutableClass.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);  // contents of __robo_data__
    generator.ifNonNull(alreadyInitialized);
    generator.loadThis();                                         // this
    generator.loadThis();                                         // this, this
    writeCallToInitializing(mutableClass, generator);
    // this, __robo_data__
    generator.putField(mutableClass.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
    generator.mark(alreadyInitialized);
    generator.returnValue();
    mutableClass.addMethod(initMethodNode);
  }

  protected abstract void writeCallToInitializing(MutableClass mutableClass, RobolectricGeneratorAdapter generator);

  private void doSpecialHandling(MutableClass mutableClass) {
    if (mutableClass.getName().equals("android.os.Build$VERSION")) {
      for (FieldNode fieldNode : mutableClass.getFields()) {
        fieldNode.access &= ~(Modifier.FINAL);
      }
    }
  }

  private boolean isSyntheticAccessorMethod(MethodNode method) {
    return (method.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  /**
   * Constructors are instrumented as follows:
   *
   * <ul>
   *   <li>Code other than a call to the superclass constructor is moved to a new method named
   *       {@code __constructor__} with the same signature.
   *   <li>The constructor is modified to call {@link ClassHandler#initializing(Object)} (or {@link
   *       ClassHandler#getShadowCreator(Class)} for {@code invokedynamic} JVMs).
   *   <li>The constructor is modified to then call {@link ClassHandler#methodInvoked(String,
   *       boolean, Class)} (or {@link ClassHandler#findShadowMethodHandle(Class, String,
   *       MethodType, boolean)} for {@code invokedynamic} JVMs) with the method name {@code
   *       __constructor__} and the same parameter types.
   * </ul>
   *
   * Note that most code in the constructor will not be executed unless the {@link ClassHandler}
   * arranges for it to happen.
   *
   * <p>Given a constructor like this:
   *
   * <pre>
   * public ThisClass(String name, int size) {
   *   super(name, someStaticMethod());
   *   this.size = size;
   * }
   * </pre>
   *
   * ... generates code like this:
   *
   * <pre>
   * private $$robo$$__constructor__(String name, int size) {
   *   this.size = size;
   * }
   *
   * private __constructor__(String name, int size) {
   *   Plan plan = RobolectricInternals.methodInvoked(
   *       "pkg/ThisClass/__constructor__(Ljava/lang/String;I)V", true, ThisClass.class);
   *   if (plan != null) {
   *     try {
   *       plan.run(this, new Object[] {name, size});
   *     } catch (Throwable t) {
   *       throw RobolectricInternals.cleanStackTrace(t);
   *     }
   *   } else {
   *     $$robo$$__constructor__(name, size);
   *   }
   * }
   *
   * public ThisClass(String name, int size) {
   *   super(name, someStaticMethod());
   *   $$robo$init();
   * }
   * </pre>
   *
   * @param method the constructor to instrument
   */
  private void instrumentConstructor(MutableClass mutableClass, MethodNode method) {
    makeMethodPrivate(method);

    InsnList callSuper = extractCallToSuperConstructor(mutableClass, method);
    method.name = directMethodName(mutableClass, ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    mutableClass.addMethod(redirectorMethod(mutableClass, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

    String[] exceptions = exceptionArray(method);
    MethodNode initMethodNode =
        new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
    makeMethodPublic(initMethodNode);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);

    initMethodNode.instructions = callSuper;

    generator.loadThis();
    generator.invokeVirtual(mutableClass.classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
    generateClassHandlerCall(mutableClass, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, generator);

    generator.endMethod();
    mutableClass.addMethod(initMethodNode);
  }

  private InsnList extractCallToSuperConstructor(MutableClass mutableClass, MethodNode ctor) {
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
          if (mnode.owner.equals(mutableClass.internalClassName) || mnode.owner.equals(mutableClass.classNode.superName)) {
            if (!"<init>".equals(mnode.name)) {
              throw new AssertionError("Invalid MethodInsnNode name");
            }

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

        default:
          // nothing to do
      }
    }

    throw new RuntimeException("huh? " + ctor.name + ctor.desc);
  }

  /**
   * Instruments a normal method
   *
   * <ul>
   *   <li>Rename the method from {@code methodName} to {@code $$robo$$methodName}.
   *   <li>Make it private so we can invoke it directly without subclass overrides taking
   *       precedence.
   *   <li>Remove {@code final} modifiers, if present.
   *   <li>Create a delegator method named {@code methodName} which delegates to the {@link
   *       ClassHandler}.
   * </ul>
   */
  protected void instrumentNormalMethod(MutableClass mutableClass, MethodNode method) {
    // if not abstract, set a final modifier
    if ((method.access & Opcodes.ACC_ABSTRACT) == 0) {
      method.access = method.access | Opcodes.ACC_FINAL;
    }
    if ((method.access & Opcodes.ACC_NATIVE) != 0) {
      instrumentNativeMethod(mutableClass, method);
    }

    // todo figure out
    String originalName = method.name;
    method.name = directMethodName(mutableClass, originalName);

    MethodNode delegatorMethodNode = new MethodNode(method.access, originalName, method.desc, method.signature, exceptionArray(method));
    delegatorMethodNode.visibleAnnotations = method.visibleAnnotations;
    delegatorMethodNode.access &= ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);

    makeMethodPrivate(method);

    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(delegatorMethodNode);
    generateClassHandlerCall(mutableClass, method, originalName, generator);
    generator.endMethod();
    mutableClass.addMethod(delegatorMethodNode);
  }

  /**
   * Creates native stub which returns the default return value.
   *
   * @param mutableClass Class to be instrumented
   * @param method Method to be instrumented, must be native
   */
  protected void instrumentNativeMethod(MutableClass mutableClass, MethodNode method) {
    method.access = method.access & ~Opcodes.ACC_NATIVE;

    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(method);
    Type returnType = generator.getReturnType();
    generator.pushDefaultReturnValueToStack(returnType);
    generator.returnValue();
  }

  private String directMethodName(MutableClass mutableClass, String originalName) {
    return SHADOW_IMPL.directMethodName(mutableClass.getName(), originalName);
  }

  //todo rename
  private MethodNode redirectorMethod(MutableClass mutableClass, MethodNode method, String newName) {
    MethodNode redirector = new MethodNode(Opcodes.ASM4, newName, method.desc, method.signature, exceptionArray(method));
    redirector.access = method.access & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);
    makeMethodPrivate(redirector);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(redirector);
    generator.invokeMethod(mutableClass.internalClassName, method);
    generator.returnValue();
    return redirector;
  }

  protected String[] exceptionArray(MethodNode method) {
    List<String> exceptions = method.exceptions;
    return exceptions.toArray(new String[exceptions.size()]);
  }

  /**
   * Filters methods that might need special treatment because of various reasons
   */
  private void rewriteMethodBody(MutableClass mutableClass, MethodNode callingMethod) {
    ListIterator<AbstractInsnNode> instructions = callingMethod.instructions.iterator();
    while (instructions.hasNext()) {
      AbstractInsnNode node = instructions.next();

      switch (node.getOpcode()) {
        case Opcodes.NEW:
          TypeInsnNode newInsnNode = (TypeInsnNode) node;
          newInsnNode.desc = mutableClass.config.mappedTypeName(newInsnNode.desc);
          break;

        case Opcodes.GETFIELD:
          /* falls through */
        case Opcodes.PUTFIELD:
          /* falls through */
        case Opcodes.GETSTATIC:
          /* falls through */
        case Opcodes.PUTSTATIC:
          FieldInsnNode fieldInsnNode = (FieldInsnNode) node;
          fieldInsnNode.desc = mutableClass.config.mappedTypeName(fieldInsnNode.desc); // todo test
          break;

        case Opcodes.INVOKESTATIC:
          /* falls through */
        case Opcodes.INVOKEINTERFACE:
          /* falls through */
        case Opcodes.INVOKESPECIAL:
          /* falls through */
        case Opcodes.INVOKEVIRTUAL:
          MethodInsnNode targetMethod = (MethodInsnNode) node;
          targetMethod.desc = mutableClass.config.remapParams(targetMethod.desc);
          if (isGregorianCalendarBooleanConstructor(targetMethod)) {
            replaceGregorianCalendarBooleanConstructor(instructions, targetMethod);
          } else if (mutableClass.config.shouldIntercept(targetMethod)) {
            interceptInvokeVirtualMethod(mutableClass, instructions, targetMethod);
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
   * Verifies if the @targetMethod is a {@code <init>(boolean)} constructor for {@link
   * java.util.GregorianCalendar}.
   */
  private boolean isGregorianCalendarBooleanConstructor(MethodInsnNode targetMethod) {
    return targetMethod.owner.equals("java/util/GregorianCalendar") &&
        targetMethod.name.equals("<init>") &&
        targetMethod.desc.equals("(Z)V");
  }

  /**
   * Replaces the void {@code <init>(boolean)} constructor for a call to the {@code void <init>(int,
   * int, int)} one.
   */
  private void replaceGregorianCalendarBooleanConstructor(
      ListIterator<AbstractInsnNode> instructions, MethodInsnNode targetMethod) {
    // Remove the call to GregorianCalendar(boolean)
    instructions.remove();

    // Discard the already-pushed parameter for GregorianCalendar(boolean)
    instructions.add(new InsnNode(Opcodes.POP));

    // Add parameters values for calling GregorianCalendar(int, int, int)
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new InsnNode(Opcodes.ICONST_0));
    instructions.add(new InsnNode(Opcodes.ICONST_0));

    // Call GregorianCalendar(int, int, int)
    instructions.add(
        new MethodInsnNode(
            Opcodes.INVOKESPECIAL,
            targetMethod.owner,
            targetMethod.name,
            "(III)V",
            targetMethod.itf));
  }

  /**
   * Decides to call through the appropriate method to intercept the method with an INVOKEVIRTUAL
   * Opcode, depending if the invokedynamic bytecode instruction is available (Java 7+).
   */
  protected abstract void interceptInvokeVirtualMethod(
      MutableClass mutableClass, ListIterator<AbstractInsnNode> instructions,
      MethodInsnNode targetMethod);

  /**
   * Replaces protected and private class modifiers with public.
   */
  private void makeClassPublic(ClassNode clazz) {
    clazz.access = (clazz.access | Opcodes.ACC_PUBLIC) & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  /**
   * Replaces protected and private method modifiers with public.
   */
  protected void makeMethodPublic(MethodNode method) {
    method.access = (method.access | Opcodes.ACC_PUBLIC) & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  /**
   * Replaces protected and public class modifiers with private.
   */
  protected void makeMethodPrivate(MethodNode method) {
    method.access = (method.access | Opcodes.ACC_PRIVATE) & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
  }

  private MethodNode generateStaticInitializerNotifierMethod(MutableClass mutableClass) {
    MethodNode methodNode = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", "()V", null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(methodNode);
    generator.push(mutableClass.classType);
    generator.invokeStatic(
        Type.getType(RobolectricInternals.class),
        new Method("classInitializing", "(Ljava/lang/Class;)V"));
    generator.returnValue();
    generator.endMethod();
    return methodNode;
  }

  // todo javadocs
  protected abstract void generateClassHandlerCall(MutableClass mutableClass,
      MethodNode originalMethod, String originalMethodName, RobolectricGeneratorAdapter generator);

  int getTag(MethodNode m) {
    return Modifier.isStatic(m.access) ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKESPECIAL;
  }

  public interface Decorator {
    void decorate(MutableClass mutableClass);

    void decorateMethodPreClassHandler(MutableClass mutableClass, MethodNode originalMethod,
        String originalMethodName, RobolectricGeneratorAdapter generator);
  }

  /**
   * Provides try/catch code generation with a {@link org.objectweb.asm.commons.GeneratorAdapter}.
   */
  static class TryCatch {
    private final Label start;
    private final Label end;
    private final Label handler;
    private final GeneratorAdapter generatorAdapter;

    TryCatch(GeneratorAdapter generatorAdapter, Type type) {
      this.generatorAdapter = generatorAdapter;
      this.start = generatorAdapter.mark();
      this.end = new Label();
      this.handler = new Label();
      generatorAdapter.visitTryCatchBlock(start, end, handler, type.getInternalName());
    }

    void end() {
      generatorAdapter.mark(end);
    }

    void handler() {
      generatorAdapter.mark(handler);
    }
  }
}
