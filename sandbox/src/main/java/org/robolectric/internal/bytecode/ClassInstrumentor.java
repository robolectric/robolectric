package org.robolectric.internal.bytecode;

import static java.lang.invoke.MethodType.methodType;

import com.google.common.collect.Iterables;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ListIterator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
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
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.robolectric.util.PerfStatsCollector;

/**
 * Instruments (i.e. modifies the bytecode) of classes to place the scaffolding necessary to use
 * Robolectric's shadows.
 */
public class ClassInstrumentor {
  private static final Handle BOOTSTRAP_INIT;
  private static final Handle BOOTSTRAP;
  private static final Handle BOOTSTRAP_STATIC;
  private static final Handle BOOTSTRAP_INTRINSIC;
  private static final String ROBO_INIT_METHOD_NAME = "$$robo$init";
  static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final ShadowImpl SHADOW_IMPL = new ShadowImpl();
  final Decorator decorator;

  static {
    String className = Type.getInternalName(InvokeDynamicSupport.class);

    MethodType bootstrap =
        methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);
    String bootstrapMethod =
        bootstrap.appendParameterTypes(MethodHandle.class).toMethodDescriptorString();
    String bootstrapIntrinsic =
        bootstrap.appendParameterTypes(String.class).toMethodDescriptorString();

    BOOTSTRAP_INIT =
        new Handle(
            Opcodes.H_INVOKESTATIC,
            className,
            "bootstrapInit",
            bootstrap.toMethodDescriptorString(),
            false);
    BOOTSTRAP = new Handle(Opcodes.H_INVOKESTATIC, className, "bootstrap", bootstrapMethod, false);
    BOOTSTRAP_STATIC =
        new Handle(Opcodes.H_INVOKESTATIC, className, "bootstrapStatic", bootstrapMethod, false);
    BOOTSTRAP_INTRINSIC =
        new Handle(
            Opcodes.H_INVOKESTATIC, className, "bootstrapIntrinsic", bootstrapIntrinsic, false);
  }

  public ClassInstrumentor() {
    this(new ShadowDecorator());
  }

  protected ClassInstrumentor(Decorator decorator) {
    this.decorator = decorator;
  }

  private MutableClass analyzeClass(
      byte[] origClassBytes,
      final InstrumentationConfiguration config,
      ClassNodeProvider classNodeProvider) {
    ClassNode classNode =
        new ClassNode(Opcodes.ASM4) {
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

  public byte[] instrument(
      ClassDetails classDetails,
      InstrumentationConfiguration config,
      ClassNodeProvider classNodeProvider) {
    PerfStatsCollector perfStats = PerfStatsCollector.getInstance();
    MutableClass mutableClass =
        perfStats.measure(
            "analyze class",
            () -> analyzeClass(classDetails.getClassBytes(), config, classNodeProvider));
    byte[] instrumentedBytes =
        perfStats.measure("instrument class", () -> instrumentToBytes(mutableClass));
    recordPackageStats(perfStats, mutableClass);
    return instrumentedBytes;
  }

  private void recordPackageStats(PerfStatsCollector perfStats, MutableClass mutableClass) {
    String className = mutableClass.getName();
    for (int i = className.indexOf('.'); i != -1; i = className.indexOf('.', i + 1)) {
      perfStats.incrementCount("instrument package " + className.substring(0, i));
    }
  }

  public void instrument(MutableClass mutableClass) {
    try {
      // Need Java version >=7 to allow invokedynamic
      mutableClass.classNode.version = Math.max(mutableClass.classNode.version, Opcodes.V1_7);

      if (mutableClass.getName().equals("android.util.SparseArray")) {
        addSetToSparseArray(mutableClass);
      }

      instrumentMethods(mutableClass);

      if (mutableClass.isInterface()) {
        mutableClass.addInterface(Type.getInternalName(InstrumentedInterface.class));
      } else {
        makeClassPublic(mutableClass.classNode);
        if ((mutableClass.classNode.access & Opcodes.ACC_FINAL) == Opcodes.ACC_FINAL) {
          mutableClass
              .classNode
              .visitAnnotation("Lcom/google/errorprone/annotations/DoNotMock;", true)
              .visit(
                  "value",
                  "This class is final. Consider using the real thing, or "
                      + "adding/enhancing a Robolectric shadow for it.");
        }
        mutableClass.classNode.access = mutableClass.classNode.access & ~Opcodes.ACC_FINAL;

        // If there is no constructor, adds one
        addNoArgsConstructor(mutableClass);

        addDirectCallConstructor(mutableClass);

        addRoboInitMethod(mutableClass);

        removeFinalFromFields(mutableClass);

        decorator.decorate(mutableClass);
      }
    } catch (Exception e) {
      throw new RuntimeException("failed to instrument " + mutableClass.getName(), e);
    }
  }

  // See https://github.com/robolectric/robolectric/issues/6840
  // Adds Set(int, object) to android.util.SparseArray.
  private void addSetToSparseArray(MutableClass mutableClass) {
    for (MethodNode method : mutableClass.getMethods()) {
      if ("set".equals(method.name)) {
        return;
      }
    }

    MethodNode setFunction =
        new MethodNode(
            Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
            "set",
            "(ILjava/lang/Object;)V",
            "(ITE;)V",
            null);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(setFunction);
    generator.loadThis();
    generator.loadArg(0);
    generator.loadArg(1);
    generator.invokeVirtual(mutableClass.classType, new Method("put", "(ILjava/lang/Object;)V"));
    generator.returnValue();
    mutableClass.addMethod(setFunction);
  }

  /**
   * Checks if the first instruction is a Jacoco load instructions. Robolectric is not capable at
   * the moment of re-instrumenting Jacoco-instrumented constructors.
   *
   * @param ctor constructor method node
   * @return whether or not the constructor can be instrumented
   */
  private boolean isJacocoInstrumented(MethodNode ctor) {
    AbstractInsnNode[] insns = ctor.instructions.toArray();
    if (insns.length > 0) {
      if (insns[0] instanceof LdcInsnNode
          && ((LdcInsnNode) insns[0]).cst instanceof ConstantDynamic) {
        ConstantDynamic cst = (ConstantDynamic) ((LdcInsnNode) insns[0]).cst;
        return cst.getName().equals("$jacocoData");
      }
    }
    return false;
  }

  /**
   * Adds a call $$robo$init, which instantiates a shadow object if required. This is to support
   * custom shadows for Jacoco-instrumented classes (except cnstructor shadows).
   */
  private void addCallToRoboInit(MutableClass mutableClass, MethodNode ctor) {
    AbstractInsnNode returnNode =
        Iterables.find(
            ctor.instructions,
            node -> node instanceof InsnNode && node.getOpcode() == Opcodes.RETURN,
            null);
    ctor.instructions.insertBefore(returnNode, new VarInsnNode(Opcodes.ALOAD, 0));
    ctor.instructions.insertBefore(
        returnNode,
        new MethodInsnNode(
            Opcodes.INVOKEVIRTUAL,
            mutableClass.classType.getInternalName(),
            ROBO_INIT_METHOD_NAME,
            "()V"));
  }

  private void instrumentMethods(MutableClass mutableClass) {
    if (mutableClass.isInterface()) {
      for (MethodNode method : mutableClass.getMethods()) {
        rewriteMethodBody(mutableClass, method);
      }
    } else {
      for (MethodNode method : mutableClass.getMethods()) {
        rewriteMethodBody(mutableClass, method);

        if (method.name.equals("<clinit>")) {
          method.name = ShadowConstants.STATIC_INITIALIZER_METHOD_NAME;
          mutableClass.addMethod(generateStaticInitializerNotifierMethod(mutableClass));
        } else if (method.name.equals("<init>")) {
          if (isJacocoInstrumented(method)) {
            addCallToRoboInit(mutableClass, method);
          } else {
            instrumentConstructor(mutableClass, method);
          }
        } else if (!isSyntheticAccessorMethod(method) && !Modifier.isAbstract(method.access)) {
          instrumentNormalMethod(mutableClass, method);
        }
      }
    }
  }

  private static void addNoArgsConstructor(MutableClass mutableClass) {
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

  protected void addDirectCallConstructor(MutableClass mutableClass) {}

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
    generator.loadThis(); // this
    generator.getField(
        mutableClass.classType,
        ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME,
        OBJECT_TYPE); // contents of __robo_data__
    generator.ifNonNull(alreadyInitialized);
    generator.loadThis(); // this
    generator.loadThis(); // this, this
    writeCallToInitializing(mutableClass, generator);
    // this, __robo_data__
    generator.putField(
        mutableClass.classType, ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME, OBJECT_TYPE);
    generator.mark(alreadyInitialized);
    generator.returnValue();
    mutableClass.addMethod(initMethodNode);
  }

  protected void writeCallToInitializing(
      MutableClass mutableClass, RobolectricGeneratorAdapter generator) {
    generator.invokeDynamic(
        "initializing",
        Type.getMethodDescriptor(OBJECT_TYPE, mutableClass.classType),
        BOOTSTRAP_INIT);
  }

  private static void removeFinalFromFields(MutableClass mutableClass) {
    for (FieldNode fieldNode : mutableClass.getFields()) {
      fieldNode.access &= ~Modifier.FINAL;
    }
  }

  private static boolean isSyntheticAccessorMethod(MethodNode method) {
    return (method.access & Opcodes.ACC_SYNTHETIC) != 0;
  }

  /**
   * Constructors are instrumented as follows: TODO(slliu): Fill in constructor instrumentation
   * directions
   *
   * @param method the constructor to instrument
   */
  private void instrumentConstructor(MutableClass mutableClass, MethodNode method) {
    makeMethodPrivate(method);

    InsnList callSuper = extractCallToSuperConstructor(mutableClass, method);
    method.name = directMethodName(mutableClass, ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    mutableClass.addMethod(
        redirectorMethod(mutableClass, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

    String[] exceptions = exceptionArray(method);
    MethodNode initMethodNode =
        new MethodNode(method.access, "<init>", method.desc, method.signature, exceptions);
    makeMethodPublic(initMethodNode);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    initMethodNode.instructions.add(callSuper);
    generator.loadThis();
    generator.invokeVirtual(mutableClass.classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
    generateClassHandlerCall(
        mutableClass, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, generator);

    generator.endMethod();

    InsnList postamble = extractInstructionsAfterReturn(method, initMethodNode);
    if (postamble.size() > 0) {
      initMethodNode.instructions.add(postamble);
    }
    mutableClass.addMethod(initMethodNode);
  }

  /**
   * Checks to see if there are instructions after RETURN. If there are, it will check to see if
   * they belong in the call-to-super, or the shadowable part of the constructor.
   */
  private InsnList extractInstructionsAfterReturn(MethodNode method, MethodNode initMethodNode) {
    InsnList removedInstructions = new InsnList();
    AbstractInsnNode returnNode =
        Iterables.find(
            method.instructions,
            node -> node instanceof InsnNode && node.getOpcode() == Opcodes.RETURN,
            null);
    if (returnNode == null) {
      return removedInstructions;
    }
    if (returnNode.getNext() instanceof LabelNode) {
      // There are instructions after the return, check where they belong. Note this is a very rare
      // edge case and only seems to happen with desugared+proguarded classes such as
      // play-services-basement's ApiException.
      LabelNode labelAfterReturn = (LabelNode) returnNode.getNext();
      boolean inInitMethodNode =
          Iterables.any(
              initMethodNode.instructions,
              input ->
                  input instanceof JumpInsnNode
                      && ((JumpInsnNode) input).label == labelAfterReturn);

      if (inInitMethodNode) {
        while (returnNode.getNext() != null) {
          AbstractInsnNode node = returnNode.getNext();
          method.instructions.remove(node);
          removedInstructions.add(node);
        }
      }
    }
    return removedInstructions;
  }

  private static InsnList extractCallToSuperConstructor(
      MutableClass mutableClass, MethodNode ctor) {
    InsnList removedInstructions = new InsnList();
    // Start removing instructions at the beginning of the method. The first instructions of
    // constructors may vary.
    int startIndex = 0;

    AbstractInsnNode[] insns = ctor.instructions.toArray();
    for (int i = 0; i < insns.length; i++) {
      AbstractInsnNode node = insns[i];

      switch (node.getOpcode()) {
        case Opcodes.INVOKESPECIAL:
          MethodInsnNode mnode = (MethodInsnNode) node;
          if (mnode.owner.equals(mutableClass.internalClassName)
              || mnode.owner.equals(mutableClass.classNode.superName)) {
            if (!"<init>".equals(mnode.name)) {
              throw new AssertionError("Invalid MethodInsnNode name");
            }

            // remove all instructions in the range 0 (the start) to invokespecial
            // <init>
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
    boolean isNativeMethod = (method.access & Opcodes.ACC_NATIVE) != 0;
    if (isNativeMethod) {
      instrumentNativeMethod(mutableClass, method);
    }

    // todo figure out
    String originalName = method.name;
    method.name = directMethodName(mutableClass, originalName);

    MethodNode delegatorMethodNode =
        new MethodNode(
            method.access, originalName, method.desc, method.signature, exceptionArray(method));
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

  protected static String directMethodName(MutableClass mutableClass, String originalName) {
    return SHADOW_IMPL.directMethodName(mutableClass.getName(), originalName);
  }

  // todo rename
  private MethodNode redirectorMethod(
      MutableClass mutableClass, MethodNode method, String newName) {
    MethodNode redirector =
        new MethodNode(
            Opcodes.ASM4, newName, method.desc, method.signature, exceptionArray(method));
    redirector.access =
        method.access & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);
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

  /** Filters methods that might need special treatment because of various reasons */
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
  private static boolean isGregorianCalendarBooleanConstructor(MethodInsnNode targetMethod) {
    return targetMethod.owner.equals("java/util/GregorianCalendar")
        && targetMethod.name.equals("<init>")
        && targetMethod.desc.equals("(Z)V");
  }

  /**
   * Replaces the void {@code <init>(boolean)} constructor for a call to the {@code void <init>(int,
   * int, int)} one.
   */
  private static void replaceGregorianCalendarBooleanConstructor(
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
  protected void interceptInvokeVirtualMethod(
      MutableClass mutableClass,
      ListIterator<AbstractInsnNode> instructions,
      MethodInsnNode targetMethod) {
    instructions.remove(); // remove the method invocation

    Type type = Type.getObjectType(targetMethod.owner);
    String description = targetMethod.desc;
    String owner = type.getClassName();

    if (targetMethod.getOpcode() != Opcodes.INVOKESTATIC) {
      String thisType = type.getDescriptor();
      description = "(" + thisType + description.substring(1);
    }

    instructions.add(
        new InvokeDynamicInsnNode(targetMethod.name, description, BOOTSTRAP_INTRINSIC, owner));
  }

  /** Replaces protected and private class modifiers with public. */
  private static void makeClassPublic(ClassNode clazz) {
    clazz.access =
        (clazz.access | Opcodes.ACC_PUBLIC) & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  /** Replaces protected and private method modifiers with public. */
  protected void makeMethodPublic(MethodNode method) {
    method.access =
        (method.access | Opcodes.ACC_PUBLIC) & ~(Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE);
  }

  /** Replaces protected and public class modifiers with private. */
  protected void makeMethodPrivate(MethodNode method) {
    method.access =
        (method.access | Opcodes.ACC_PRIVATE) & ~(Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED);
  }

  private static MethodNode generateStaticInitializerNotifierMethod(MutableClass mutableClass) {
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
  protected void generateClassHandlerCall(
      MutableClass mutableClass,
      MethodNode originalMethod,
      String originalMethodName,
      RobolectricGeneratorAdapter generator) {
    Handle original =
        new Handle(
            getTag(originalMethod),
            mutableClass.classType.getInternalName(),
            originalMethod.name,
            originalMethod.desc,
            getTag(originalMethod) == Opcodes.H_INVOKEINTERFACE);

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

  int getTag(MethodNode m) {
    return Modifier.isStatic(m.access) ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKESPECIAL;
  }

  public interface Decorator {
    void decorate(MutableClass mutableClass);
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
