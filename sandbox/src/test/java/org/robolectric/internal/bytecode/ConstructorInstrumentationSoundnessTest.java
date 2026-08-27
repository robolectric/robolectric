package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.robolectric.shadow.api.Shadow;

/**
 * Constructor prologues that {@link ClassInstrumentor} must not split.
 *
 * <p>Splitting a constructor at its {@code super()}/{@code this()} call assumes the prologue leaves
 * {@code this} in slot 0, the parameters in their slots, and nothing on the operand stack. javac
 * satisfies that; optimizers do not. Every shape below was found by running R8, ProGuard or Jacoco
 * over android-all and instrumenting the result, and each one produced a {@link VerifyError} before
 * it was handled.
 *
 * <p>Each test instruments the class, checks it with ASM's verifier, loads it so HotSpot checks it
 * too, and asserts how far instrumentation got.
 */
@RunWith(JUnit4.class)
public class ConstructorInstrumentationSoundnessTest {

  /** How far instrumentation got, in decreasing order of shadow support. */
  private enum Outcome {
    SPLIT,
    SHADOW_OBJECT_ONLY,
    UNTOUCHED
  }

  private static final String STRING = "Ljava/lang/String;";

  @Test
  public void plainConstructor() {
    assertInstrumented(
        Outcome.SPLIT,
        "(" + STRING + ")V",
        mv -> {
          loadThisAndCallSuper(mv);
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitVarInsn(Opcodes.ALOAD, 1);
          mv.visitFieldInsn(Opcodes.PUTFIELD, OWNER, "f", STRING);
        });
  }

  /**
   * A parameter's slot reused for another type, as R8 did to {@code
   * com.google.android.gms.common.logging.Logger(String, String...)} (#7269): the {@code String[]}
   * in slot 2 becomes a {@code String} before {@code super()}.
   */
  @Test
  public void parameterSlotReused() {
    assertInstrumented(
        Outcome.SHADOW_OBJECT_ONLY,
        "(" + STRING + "[" + STRING + ")V",
        mv -> {
          mv.visitVarInsn(Opcodes.ALOAD, 2);
          mv.visitInsn(Opcodes.ARRAYLENGTH);
          mv.visitInsn(Opcodes.POP);
          mv.visitLdcInsn("");
          mv.visitVarInsn(Opcodes.ASTORE, 2); // slot 2 is a String from here
          loadThisAndCallSuper(mv);
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitVarInsn(Opcodes.ALOAD, 2);
          mv.visitFieldInsn(Opcodes.PUTFIELD, OWNER, "f", STRING);
        });
  }

  /** A local above the parameter slots, computed before {@code super()} and read after it. */
  @Test
  public void nonParameterLocalLiveAcrossSuper() {
    assertInstrumented(
        Outcome.SHADOW_OBJECT_ONLY,
        "(I)V",
        mv -> {
          mv.visitVarInsn(Opcodes.ILOAD, 1);
          mv.visitVarInsn(Opcodes.ISTORE, 2);
          loadThisAndCallSuper(mv);
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitVarInsn(Opcodes.ILOAD, 2);
          mv.visitFieldInsn(Opcodes.PUTFIELD, OWNER, "i", "I");
        });
  }

  /**
   * {@code this} kept on the operand stack across the call rather than reloaded. R8 emits this for
   * {@code super(); this.f = x;} and it was by far the commonest breakage over android-all.
   */
  @Test
  public void operandStackLiveAcrossSuper() {
    assertInstrumented(
        Outcome.SHADOW_OBJECT_ONLY,
        "(" + STRING + ")V",
        mv -> {
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitInsn(Opcodes.DUP);
          mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          mv.visitVarInsn(Opcodes.ALOAD, 1);
          mv.visitFieldInsn(Opcodes.PUTFIELD, OWNER, "f", STRING);
        });
  }

  /**
   * Slot 0 reused as scratch while the real {@code this} sits on the stack, as R8 did to {@code
   * android.accounts.Account}. Legal, because the verifier tracks {@code uninitializedThis} on the
   * stack. Nothing can be instrumented: both forms reload {@code this} with {@code Opcodes.ALOAD
   * 0}.
   */
  @Test
  public void thisSlotReused() {
    assertInstrumented(
        Outcome.UNTOUCHED,
        "(" + STRING + ")V",
        mv -> {
          mv.visitVarInsn(Opcodes.ALOAD, 0); // this stays on the stack
          mv.visitVarInsn(Opcodes.ALOAD, 1);
          mv.visitVarInsn(Opcodes.ASTORE, 0); // slot 0 is a String from here
          mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        });
  }

  /** {@code this(new Same(null))} - the argument's constructor matches, but is not the call. */
  @Test
  public void newInstanceOfSameClassAsArgument() {
    assertInstrumented(Outcome.SPLIT, "()V", ConstructorInstrumentationSoundnessTest::delegate);
  }

  /** {@code throw} before {@code super()}, which javac emits under JEP 447/482. */
  @Test
  public void throwBeforeSuper() {
    assertInstrumented(
        Outcome.SPLIT,
        "()V",
        mv -> {
          org.objectweb.asm.Label ok = new org.objectweb.asm.Label();
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitJumpInsn(Opcodes.IFNONNULL, ok);
          newAndThrow(mv);
          mv.visitLabel(ok);
          loadThisAndCallSuper(mv);
        });
  }

  /** A constructor that never chains anywhere, which only a bytecode generator produces. */
  @Test
  public void alwaysThrows() {
    assertInstrumented(
        Outcome.UNTOUCHED, "()V", ConstructorInstrumentationSoundnessTest::newAndThrow);
  }

  // Jacoco keeps its probe array in a local from before super() to the end of the constructor, so
  // its constructors get the shadow object only.

  @Test
  public void jacoco_plainConstructor() {
    assertInstrumented(Outcome.SHADOW_OBJECT_ONLY, "()V", jacoco(mv -> loadThisAndCallSuper(mv)));
  }

  /** Used to fail outright with a {@link NullPointerException} while looking for the call. */
  @Test
  public void jacoco_alwaysThrows() {
    assertInstrumented(
        Outcome.UNTOUCHED, "()V", jacoco(ConstructorInstrumentationSoundnessTest::newAndThrow));
  }

  /**
   * Used to add the shadow call after the argument's constructor, on an uninitialized {@code this}.
   */
  @Test
  public void jacoco_newInstanceOfSameClassAsArgument() {
    assertInstrumented(
        Outcome.SHADOW_OBJECT_ONLY,
        "()V",
        jacoco(ConstructorInstrumentationSoundnessTest::delegate));
  }

  @Test
  public void jacoco_thisSlotReused() {
    assertInstrumented(
        Outcome.UNTOUCHED,
        "(" + STRING + ")V",
        jacoco(mv -> {
          mv.visitVarInsn(Opcodes.ALOAD, 0);
          mv.visitVarInsn(Opcodes.ALOAD, 1);
          mv.visitVarInsn(Opcodes.ASTORE, 0);
          mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        }));
  }

  // ---------------------------------------------------------------------------------------------

  private static final String OWNER = "org/example/Ctor";
  private static final String DELEGATE_DESC = "(Lorg/example/Ctor;)V";
  private static final String NAME = "org.example.Ctor";

  private static void loadThisAndCallSuper(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
  }

  private static void newAndThrow(MethodVisitor mv) {
    mv.visitTypeInsn(Opcodes.NEW, "java/lang/UnsupportedOperationException");
    mv.visitInsn(Opcodes.DUP);
    mv.visitMethodInsn(
        Opcodes.INVOKESPECIAL, "java/lang/UnsupportedOperationException", "<init>", "()V", false);
    mv.visitInsn(Opcodes.ATHROW);
  }

  /** {@code Ctor() { this(new Ctor(null)); }}, whose two INVOKESPECIALs both match on owner. */
  private static void delegate(MethodVisitor mv) {
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitTypeInsn(Opcodes.NEW, OWNER);
    mv.visitInsn(Opcodes.DUP);
    mv.visitInsn(Opcodes.ACONST_NULL);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OWNER, "<init>", DELEGATE_DESC, false);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, OWNER, "<init>", DELEGATE_DESC, false);
  }

  /**
   * Instruments a prologue the way Jacoco's offline instrumentation does: the probe array is stored
   * in a local before the prologue, and a probe after it is set through that local.
   */
  private static Consumer<MethodVisitor> jacoco(Consumer<MethodVisitor> body) {
    // Past the parameter slots of every constructor in these tests.
    int probesSlot = 2;
    return mv -> {
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, OWNER, "$jacocoInit", "()[Z", false);
      mv.visitVarInsn(Opcodes.ASTORE, probesSlot);
      body.accept(mv);
      mv.visitVarInsn(Opcodes.ALOAD, probesSlot);
      mv.visitInsn(Opcodes.ICONST_0);
      mv.visitInsn(Opcodes.ICONST_1);
      mv.visitInsn(Opcodes.BASTORE);
    };
  }

  /**
   * Builds {@code org.example.Ctor} with one constructor of the given descriptor, instruments it,
   * and asserts that it verifies, loads, and reached the expected outcome.
   */
  private static void assertInstrumented(
      Outcome expected, String desc, Consumer<MethodVisitor> body) {
    byte[] instrumented = instrument(constructorClass(desc, body));

    assertThat(verify(instrumented)).isEmpty();
    assertThat(outcomeOf(instrumented, desc)).isEqualTo(expected);
    load(instrumented);
  }

  private static byte[] constructorClass(String desc, Consumer<MethodVisitor> body) {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, OWNER, null, "java/lang/Object", null);
    cw.visitField(Opcodes.ACC_PRIVATE, "f", STRING, null, null).visitEnd();
    cw.visitField(Opcodes.ACC_PRIVATE, "i", "I", null, null).visitEnd();

    // A real $jacocoInit, so a Jacoco-shaped class still links.
    MethodVisitor probe =
        cw.visitMethod(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, "$jacocoInit", "()[Z", null, null);
    probe.visitCode();
    probe.visitInsn(Opcodes.ICONST_1);
    probe.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_BOOLEAN);
    probe.visitInsn(Opcodes.ARETURN);
    probe.visitMaxs(0, 0);
    probe.visitEnd();

    // The delegate target, for this(new Ctor(null)).
    if (!desc.equals("(" + STRING + ")V")) {
      MethodVisitor other =
          cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + STRING + ")V", null, null);
      other.visitCode();
      loadThisAndCallSuper(other);
      other.visitInsn(Opcodes.RETURN);
      other.visitMaxs(0, 0);
      other.visitEnd();
    }

    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", desc, null, null);
    mv.visitCode();
    body.accept(mv);
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();
    cw.visitEnd();
    return cw.toByteArray();
  }

  private static byte[] instrument(byte[] classBytes) {
    ClassNode classNode = new ClassNode();
    new ClassReader(classBytes).accept(classNode, 0);
    ClassNodeProvider provider =
        new ClassNodeProvider() {
          @Override
          protected byte[] getClassBytes(String name) throws ClassNotFoundException {
            throw new ClassNotFoundException(name);
          }
        };
    return new ClassInstrumentor()
        .instrumentToBytes(
            new MutableClass(
                classNode, InstrumentationConfiguration.newBuilder().build(), provider));
  }

  /** What happened to the one constructor with this descriptor; the class holds others. */
  private static Outcome outcomeOf(byte[] classBytes, String desc) {
    ClassNode classNode = new ClassNode();
    new ClassReader(classBytes).accept(classNode, 0);
    String directName = Shadow.directMethodName(NAME, ShadowConstants.CONSTRUCTOR_METHOD_NAME);
    for (MethodNode method : classNode.methods) {
      if (!method.desc.equals(desc)) {
        continue;
      }
      if (method.name.equals(directName)) {
        return Outcome.SPLIT;
      }
      if (method.name.equals("<init>")) {
        for (AbstractInsnNode node : method.instructions) {
          if (node instanceof MethodInsnNode
              && ((MethodInsnNode) node).name.equals("$$robo$init")) {
            return Outcome.SHADOW_OBJECT_ONLY;
          }
        }
      }
    }
    return Outcome.UNTOUCHED;
  }

  /** Every constructor-related method that ASM's type-checking verifier rejects. */
  private static List<String> verify(byte[] classBytes) {
    ClassNode classNode = new ClassNode();
    new ClassReader(classBytes).accept(classNode, 0);
    List<Type> interfaces = new ArrayList<>();
    for (String itf : classNode.interfaces) {
      interfaces.add(Type.getObjectType(itf));
    }
    List<String> errors = new ArrayList<>();
    for (MethodNode method : classNode.methods) {
      if (!method.name.equals("<init>") && !method.name.contains("__constructor__")) {
        continue;
      }
      SimpleVerifier verifier =
          new SimpleVerifier(
              Type.getObjectType(classNode.name),
              Type.getObjectType(classNode.superName),
              interfaces,
              /* isInterface= */ false);
      verifier.setClassLoader(loaderFor(classBytes));
      try {
        new Analyzer<BasicValue>(verifier).analyze(classNode.name, method);
      } catch (AnalyzerException e) {
        errors.add(method.name + method.desc + ": " + e.getMessage());
      }
    }
    return errors;
  }

  /** Links the class for real, so HotSpot's own verifier runs over it. */
  private static void load(byte[] classBytes) {
    try {
      Class<?> unused =
          MethodHandles.lookup().ensureInitialized(loaderFor(classBytes).loadClass(NAME));
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static ClassLoader loaderFor(byte[] classBytes) {
    return new ClassLoader(ConstructorInstrumentationSoundnessTest.class.getClassLoader()) {
      @Override
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(NAME)) {
          return defineClass(name, classBytes, 0, classBytes.length);
        }
        throw new ClassNotFoundException(name);
      }
    };
  }
}
