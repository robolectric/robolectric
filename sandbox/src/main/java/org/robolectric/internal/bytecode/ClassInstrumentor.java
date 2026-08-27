package org.robolectric.internal.bytecode;

import static java.lang.invoke.MethodType.methodType;

import com.google.common.collect.Iterables;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
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
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.robolectric.util.Logger;
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
  protected static final Type OBJECT_TYPE = Type.getType(Object.class);
  private static final ShadowImpl SHADOW_IMPL = new ShadowImpl();
  final Decorator decorator;

  static {
    String className = Type.getInternalName(InvokeDynamicSupport.class);

    MethodType bootstrap =
        methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class);

    /*
     * There is an additional int.class argument to the invokedynamic bootstrap method. This conveys
     * whether or not the method invocation represents a native method. A one means the original
     * method was a native method, and a zero means it was not. It should be boolean.class, but
     * that is nt possible due to https://bugs.java.com/bugdatabase/view_bug?bug_id=JDK-8322510.
     */
    String bootstrapMethod =
        bootstrap
            .appendParameterTypes(MethodHandle.class, /* isNative */ int.class)
            .toMethodDescriptorString();
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

  public void instrument(MutableClass mutableClass) {
    if (hasAnyJUnitAnnotation(mutableClass)) {
      // Detected a JUnit related test class, skip instrumentation
      return;
    }
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
                  "This class is final. Consider either:\n"
                      + "1. Using the real class.\n"
                      + "2. If it's a pure data class, adding a Robolectric Builder for it.\n"
                      + "3. If it cannot function on the JVM, adding or enhancing a Robolectric"
                      + " Shadow for it");
        }
        mutableClass.classNode.access = mutableClass.classNode.access & ~Opcodes.ACC_FINAL;

        // If there is no constructor, adds one
        addNoArgsConstructor(mutableClass);

        addRoboInitMethod(mutableClass);

        removeFinalFromFields(mutableClass);

        decorator.decorate(mutableClass);
      }
    } catch (Exception e) {
      throw new RuntimeException("failed to instrument " + mutableClass.getName(), e);
    }
  }

  private void recordPackageStats(PerfStatsCollector perfStats, MutableClass mutableClass) {
    String className = mutableClass.getName();
    for (int i = className.indexOf('.'); i != -1; i = className.indexOf('.', i + 1)) {
      perfStats.incrementCount("instrument package " + className.substring(0, i));
    }
  }

  private static boolean hasAnyJUnitAnnotation(MutableClass mutableClass) {
    for (MethodNode method : mutableClass.getMethods()) {
      if (method.visibleAnnotations == null) {
        continue;
      }
      if ((method.access & Opcodes.ACC_PUBLIC) == 0) {
        // Only check public methods.
        continue;
      }
      // Because robolectric will generate private versions of the method for
      // internal use and any junit annotated method that is private will cause junit to
      // throw exceptions
      if (method.visibleAnnotations.stream()
          .anyMatch(annotation -> annotation.desc.startsWith("Lorg/junit/"))) {
        return true;
      }
    }
    return false;
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
   * Adds a call to $$robo$init after the super()/this() call, which instantiates a shadow object if
   * required. This supports custom shadows, except constructor shadows, for constructors that
   * cannot be split, such as Jacoco-instrumented ones.
   *
   * <p>Does nothing if the constructor has no super()/this() call or does not keep {@code this} in
   * slot 0 up to it, since there is then no instance to initialize or no way to reach it.
   */
  protected void addCallToRoboInit(MutableClass mutableClass, MethodNode ctor) {
    MethodInsnNode delegationCall = findDelegationCall(mutableClass, ctor);
    if (delegationCall != null
        && keepsThisInSlotZero(frameAt(mutableClass, ctor, delegationCall))) {
      addCallToRoboInit(mutableClass, ctor, delegationCall);
    }
  }

  private static void addCallToRoboInit(
      MutableClass mutableClass, MethodNode ctor, MethodInsnNode delegationCall) {
    AbstractInsnNode returnNode = delegationCall;
    ctor.instructions.insert(
        returnNode,
        new MethodInsnNode(
            Opcodes.INVOKEVIRTUAL,
            mutableClass.classType.getInternalName(),
            ROBO_INIT_METHOD_NAME,
            "()V"));
    ctor.instructions.insert(returnNode, new VarInsnNode(Opcodes.ALOAD, 0));
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
          instrumentConstructor(mutableClass, method);
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
   * Constructors are instrumented as follows:
   *
   * <ul>
   *   <li>The original constructor will be stripped of its instructions leading up to, and
   *       including, the call to super() or this(). It is also renamed to $$robo$$__constructor__
   *   <li>A method called __constructor__ is created and its job is to call
   *       $$robo$$__constructor__. The __constructor__ method is what gets shadowed if a Shadow
   *       wants to shadow a constructor.
   *   <li>A new constructor is created and contains the stripped instructions of the original
   *       constructor leading up to, and including, the call to super() or this(). Then, it has a
   *       call to $$robo$init to initialize the Class' Shadow Object. Then, it uses invokedynamic
   *       to call __constructor__. Finally, it contains any instructions that might occur after the
   *       return statement in the original constructor.
   * </ul>
   *
   * @param method the constructor to instrument
   */
  protected void instrumentConstructor(MutableClass mutableClass, MethodNode method) {
    MethodInsnNode delegationCall = findDelegationCall(mutableClass, method);
    if (delegationCall == null) {
      // A constructor that never chains to a superclass constructor is one that always throws.
      // There is no instance to shadow and nothing to split, so leave it exactly as it is.
      return;
    }
    Frame<SourceValue> frame = frameAt(mutableClass, method, delegationCall);
    if (!keepsThisInSlotZero(frame)) {
      Logger.warn(
          "%s%s does not keep `this` in slot 0 up to its call to super()/this();"
              + " leaving it uninstrumented.",
          mutableClass.getName(), method.desc);
      return;
    }
    if (!canSplitAt(frame, delegationCall)) {
      Logger.warn(
          "%s%s does not leave its locals and stack untouched up to its call to super()/this();"
              + " instrumenting it without constructor shadow support.",
          mutableClass.getName(), method.desc);
      addCallToRoboInit(mutableClass, method, delegationCall);
      return;
    }

    int methodAccess = method.access;
    makeMethodPrivate(method);

    InsnList callSuper = extractCallToSuperConstructor(method, delegationCall);
    method.name = directMethodName(mutableClass, ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    mutableClass.addMethod(
        redirectorMethod(mutableClass, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME));

    String[] exceptions = exceptionArray(method);
    MethodNode initMethodNode =
        new MethodNode(methodAccess, "<init>", method.desc, method.signature, exceptions);
    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(initMethodNode);
    initMethodNode.instructions.add(callSuper);
    generator.loadThis();
    generator.invokeVirtual(mutableClass.classType, new Method(ROBO_INIT_METHOD_NAME, "()V"));
    generateClassHandlerCall(
        mutableClass, method, ShadowConstants.CONSTRUCTOR_METHOD_NAME, generator, false);

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
  @SuppressWarnings("NullArgumentForNonNullParameter")
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

  /**
   * Finds the {@code super()}/{@code this()} call that {@code ctor} chains to, or {@code null} if
   * it has none, which means the constructor always throws.
   *
   * <p>Matching on {@code INVOKESPECIAL <init>} owned by this class or its superclass is not
   * enough: {@code this(new Same(...))} constructs its argument with a call that matches just as
   * well and comes first. Every such call initializes either a preceding {@code NEW} or {@code
   * this}, so the outstanding {@code NEW}s are counted off and the first call left over is the
   * delegation.
   */
  private static MethodInsnNode findDelegationCall(MutableClass mutableClass, MethodNode ctor) {
    Map<String, Integer> uninitialized = new HashMap<>();
    for (AbstractInsnNode node : ctor.instructions) {
      if (node.getOpcode() == Opcodes.NEW) {
        uninitialized.merge(((TypeInsnNode) node).desc, 1, Integer::sum);
      } else if (node.getOpcode() == Opcodes.INVOKESPECIAL) {
        MethodInsnNode call = (MethodInsnNode) node;
        if (!"<init>".equals(call.name)) {
          continue;
        }
        Integer pending = uninitialized.get(call.owner);
        if (pending != null && pending > 0) {
          uninitialized.put(call.owner, pending - 1);
        } else if (call.owner.equals(mutableClass.internalClassName)
            || call.owner.equals(mutableClass.classNode.superName)) {
          return call;
        }
      }
    }
    return null;
  }

  /**
   * The dataflow frame at the {@code super()}/{@code this()} call, recording where each value in
   * the frame came from, or {@code null} if the constructor cannot be analyzed.
   */
  private static Frame<SourceValue> frameAt(
      MutableClass mutableClass, MethodNode ctor, MethodInsnNode delegationCall) {
    try {
      Frame<SourceValue>[] frames =
          new Analyzer<>(new SourceInterpreter()).analyze(mutableClass.internalClassName, ctor);
      return frames[ctor.instructions.indexOf(delegationCall)];
    } catch (AnalyzerException | RuntimeException e) {
      return null;
    }
  }

  /**
   * Whether {@code this} is still in slot 0 at the call. Both the split and {@link
   * #addCallToRoboInit} reload it from there with {@code ALOAD 0}, so neither is usable otherwise.
   *
   * <p>R8 reuses slot 0 as scratch while the real {@code this} sits on the operand stack, which is
   * legal because the verifier tracks {@code uninitializedThis} on the stack rather than in a
   * local.
   */
  private static boolean keepsThisInSlotZero(Frame<SourceValue> frame) {
    // A value with no producing instruction is the one the slot held on entry.
    return frame != null && frame.getLocal(0).insns.isEmpty();
  }

  /**
   * Whether the prologue leaves the rest of the frame as it was on entry, so the body after the
   * call can be moved into a method carrying the constructor's own descriptor: every other local
   * must still hold its entry value, since only the parameters exist there, and the call must leave
   * the operand stack empty, since the split ends {@code <init>} at that point.
   *
   * <p>R8 breaks the second constantly, compiling {@code super(); this.f = x;} to {@code aload_0,
   * dup, invokespecial, ..., putfield} so that {@code this} need not be reloaded.
   */
  private static boolean canSplitAt(Frame<SourceValue> frame, MethodInsnNode delegationCall) {
    for (int slot = 1; slot < frame.getLocals(); slot++) {
      if (!frame.getLocal(slot).insns.isEmpty()) {
        return false;
      }
    }
    return frame.getStackSize() == 1 + Type.getArgumentTypes(delegationCall.desc).length;
  }

  /**
   * Removes the instructions from the start of {@code ctor} up to and including {@code
   * delegationCall}, and returns them.
   */
  @SuppressWarnings("ReferenceEquality") // instruction nodes are compared by identity
  private static InsnList extractCallToSuperConstructor(
      MethodNode ctor, MethodInsnNode delegationCall) {
    InsnList removedInstructions = new InsnList();
    AbstractInsnNode node = ctor.instructions.getFirst();
    while (true) {
      AbstractInsnNode next = node.getNext();
      ctor.instructions.remove(node);
      removedInstructions.add(node);
      if (node == delegationCall) {
        return removedInstructions;
      }
      node = next;
    }
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

    // Create delegator method with same name as original method. The delegator method will use
    // invokedynamic to decide at runtime whether to call original method or shadowed method
    String originalName = method.name;
    method.name = directMethodName(mutableClass, originalName);

    MethodNode delegatorMethodNode =
        new MethodNode(
            method.access, originalName, method.desc, method.signature, exceptionArray(method));
    delegatorMethodNode.visibleAnnotations = method.visibleAnnotations;
    delegatorMethodNode.access &= ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_FINAL);

    makeMethodPrivate(method);

    RobolectricGeneratorAdapter generator = new RobolectricGeneratorAdapter(delegatorMethodNode);
    generateClassHandlerCall(mutableClass, method, originalName, generator, isNativeMethod);
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

    String nativeBindingMethodName =
        SHADOW_IMPL.directNativeMethodName(mutableClass.getName(), method.name);

    // Generate native binding method
    MethodNode nativeBindingMethod =
        new MethodNode(
            Opcodes.ASM4,
            nativeBindingMethodName,
            method.desc,
            method.signature,
            exceptionArray(method));
    nativeBindingMethod.access = method.access | Opcodes.ACC_SYNTHETIC;
    makeMethodPrivate(nativeBindingMethod);
    mutableClass.addMethod(nativeBindingMethod);

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
    return exceptions.toArray(new String[0]);
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
      RobolectricGeneratorAdapter generator,
      boolean isNativeMethod) {
    Handle original =
        new Handle(
            getTag(originalMethod),
            mutableClass.classType.getInternalName(),
            originalMethod.name,
            originalMethod.desc,
            getTag(originalMethod) == Opcodes.H_INVOKEINTERFACE);

    if (generator.isStatic()) {
      generator.loadArgs();
      generator.invokeDynamic(
          originalMethodName, originalMethod.desc, BOOTSTRAP_STATIC, original, isNativeMethod);
    } else {
      String desc = "(" + mutableClass.classType.getDescriptor() + originalMethod.desc.substring(1);
      generator.loadThis();
      generator.loadArgs();
      generator.invokeDynamic(originalMethodName, desc, BOOTSTRAP, original, isNativeMethod);
    }

    generator.returnValue();
  }

  int getTag(MethodNode m) {
    return Modifier.isStatic(m.access) ? Opcodes.H_INVOKESTATIC : Opcodes.H_INVOKESPECIAL;
  }

  // implemented in DirectClassInstrumentor
  public void setAndroidJarSDKVersion(int androidJarSDKVersion) {}

  // implemented in DirectClassInstrumentor
  protected int getAndroidJarSDKVersion() {
    return -1;
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
