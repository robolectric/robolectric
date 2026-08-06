package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.robolectric.shadow.api.Shadow;

/** Test for {@link ClassInstrumentor}. */
@RunWith(JUnit4.class)
public class ClassInstrumentorTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private ClassInstrumentor instrumentor;
  private ClassNodeProvider classNodeProvider;

  @Before
  public void setUp() throws Exception {
    instrumentor = new ClassInstrumentor();
    classNodeProvider =
        new ClassNodeProvider() {
          @Override
          protected byte[] getClassBytes(String className) {
            return new byte[0];
          }
        };
  }

  @Test
  public void instrumentRegularMethod() {
    ClassNode classNode = createClassWithRegularMethod();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    String someFunctionName = Shadow.directMethodName("org.example.MyClass", "someFunction");
    MethodNode methodNode = findMethodNode(classNode, someFunctionName);

    assertThat(clazz.classNode.interfaces).contains(Type.getInternalName(ShadowedObject.class));
    assertRoboDataField(clazz.getFields().get(0));

    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: instructions have been rewritten to return 0.
    assertThat(methodNode.instructions).isEmpty();
  }

  @Test
  public void instrumentNativeMethod_legacy() {
    ClassNode classNode = createClassWithNativeMethod();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    String someFunctionName = Shadow.directMethodName("org.example.MyClass", "someFunction");
    MethodNode methodNode = findMethodNode(classNode, someFunctionName);

    assertThat(clazz.classNode.interfaces).contains(Type.getInternalName(ShadowedObject.class));
    assertRoboDataField(clazz.getFields().get(0));

    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: instructions have been rewritten to return 0.
    assertThat(methodNode.instructions).hasSize(2);
    assertThat(methodNode.instructions.get(0).getOpcode()).isEqualTo(Opcodes.ICONST_0);
    assertThat(methodNode.instructions.get(1).getOpcode()).isEqualTo(Opcodes.IRETURN);
  }

  @Test
  public void instrumentNativeMethod_generatesNativeBindingMethod() {
    ClassNode classNode = createClassWithNativeMethod();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    String nativeMethodName = Shadow.directNativeMethodName("org.example.MyClass", "someFunction");
    MethodNode methodNode = findMethodNode(classNode, nativeMethodName);

    assertThat(clazz.classNode.interfaces).contains(Type.getInternalName(ShadowedObject.class));
    assertRoboDataField(clazz.getFields().get(0));

    assertThat(methodNode.access & Opcodes.ACC_NATIVE).isNotEqualTo(0);
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    assertThat(methodNode.access & Opcodes.ACC_SYNTHETIC).isNotEqualTo(0);
  }

  /**
   * Tests that a constructor which reuses a parameter slot for an incompatible type before super()
   * does not get the constructor-splitting transform (which would produce invalid bytecode).
   * Instead, it should fall back to the addCallToRoboInit path.
   *
   * <p>This models a pattern produced by R8/ProGuard: the String[] parameter in slot 2 is
   * overwritten with a String before super() is called, and the post-super body reads slot 2 as a
   * String. If the constructor were split, the generated __constructor__ would receive String[] in
   * slot 2, producing a VerifyError when it tries to store it into a String field.
   */
  @Test
  public void instrumentConstructor_withParameterSlotReuse_doesNotSplit() {
    ClassNode classNode = createClassWithSlotReusingConstructor();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    // The constructor-splitting transform would rename <init> to $$robo$$...__constructor__.
    // If the fallback path was taken, no such method should exist.
    String directCtorName = Shadow.directMethodName("org.example.SlotReuseCtor", "__constructor__");
    MethodNode directCtor =
        Iterables.tryFind(classNode.methods, m -> m.name.equals(directCtorName)).orNull();
    assertThat(directCtor).isNull();

    // The <init> method should still exist (not renamed).
    MethodNode initMethod =
        Iterables.tryFind(classNode.methods, m -> m.name.equals("<init>")).orNull();
    assertThat(initMethod).isNotNull();
  }

  /**
   * Tests that a normal constructor (no parameter slot reuse) still gets the full
   * constructor-splitting transform.
   */
  @Test
  public void instrumentConstructor_withoutParameterSlotReuse_doesSplit() {
    ClassNode classNode = createClassWithNormalConstructor();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    // The constructor-splitting transform renames the original to $$robo$$...__constructor__.
    String directCtorName = Shadow.directMethodName("org.example.NormalCtor", "__constructor__");
    MethodNode directCtor =
        Iterables.tryFind(classNode.methods, m -> m.name.equals(directCtorName)).orNull();
    assertThat(directCtor).isNotNull();
  }

  private static ClassNode createClassWithRegularMethod() {
    ClassNode classNode = new ClassNode();
    classNode.name = "org/example/MyClass";
    classNode.methods.add(new MethodNode(Opcodes.ACC_PUBLIC, "someFunction", "()I", null, null));
    return classNode;
  }

  private static ClassNode createClassWithNativeMethod() {
    ClassNode classNode = new ClassNode();
    classNode.name = "org/example/MyClass";
    classNode.methods.add(
        new MethodNode(Opcodes.ACC_PUBLIC + Opcodes.ACC_NATIVE, "someFunction", "()I", null, null));
    return classNode;
  }

  /**
   * Creates a class with a constructor that reuses a parameter slot for an incompatible type before
   * super(). This models the R8/ProGuard optimization described in the bug report.
   *
   * <pre>
   * class SlotReuseCtor extends Object {
   *   private String f;
   *   // ctor (String a, String[] b)
   *   // slot 2 (b, String[]) is overwritten with "" (String) before super()
   *   // post-super code reads slot 2 as String
   * }
   * </pre>
   */
  private static ClassNode createClassWithSlotReusingConstructor() {
    ClassWriter cw = new ClassWriter(0);
    cw.visit(
        Opcodes.V1_7,
        Opcodes.ACC_PUBLIC,
        "org/example/SlotReuseCtor",
        null,
        "java/lang/Object",
        null);
    cw.visitField(Opcodes.ACC_PRIVATE, "f", "Ljava/lang/String;", null, null).visitEnd();

    // ctor (String a, String[] b)
    MethodVisitor mv =
        cw.visitMethod(
            Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;[Ljava/lang/String;)V", null, null);
    mv.visitCode();
    mv.visitLdcInsn(""); // push ""
    mv.visitVarInsn(Opcodes.ASTORE, 2); // slot 2 := "" (String), overwrites String[] param
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    mv.visitMethodInsn(
        Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // super()
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    mv.visitVarInsn(Opcodes.ALOAD, 2); // slot 2 read as String
    mv.visitFieldInsn(
        Opcodes.PUTFIELD,
        "org/example/SlotReuseCtor",
        "f",
        "Ljava/lang/String;"); // this.f = (String) slot 2
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(2, 3);
    mv.visitEnd();

    cw.visitEnd();

    // Parse the generated bytecode into a ClassNode (tree API).
    ClassNode classNode = new ClassNode();
    new org.objectweb.asm.ClassReader(cw.toByteArray()).accept(classNode, 0);
    return classNode;
  }

  /**
   * Creates a class with a normal constructor (no parameter slot reuse) that should get the full
   * constructor-splitting transform.
   *
   * <pre>
   * class NormalCtor extends Object {
   *   private String f;
   *   // ctor (String a)
   *   // super() then this.f = a
   * }
   * </pre>
   */
  private static ClassNode createClassWithNormalConstructor() {
    ClassWriter cw = new ClassWriter(0);
    cw.visit(
        Opcodes.V1_7, Opcodes.ACC_PUBLIC, "org/example/NormalCtor", null, "java/lang/Object", null);
    cw.visitField(Opcodes.ACC_PRIVATE, "f", "Ljava/lang/String;", null, null).visitEnd();

    MethodVisitor mv =
        cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/String;)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    mv.visitMethodInsn(
        Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false); // super()
    mv.visitVarInsn(Opcodes.ALOAD, 0); // this
    mv.visitVarInsn(Opcodes.ALOAD, 1); // a (String param, unmodified)
    mv.visitFieldInsn(
        Opcodes.PUTFIELD, "org/example/NormalCtor", "f", "Ljava/lang/String;"); // this.f = a
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(2, 2);
    mv.visitEnd();

    cw.visitEnd();

    ClassNode classNode = new ClassNode();
    new org.objectweb.asm.ClassReader(cw.toByteArray()).accept(classNode, 0);
    return classNode;
  }

  private static MethodNode findMethodNode(ClassNode classNode, String name) {
    return Iterables.find(classNode.methods, input -> input.name.equals(name));
  }

  private static void assertRoboDataField(FieldNode fieldNode) {
    assertThat(fieldNode.access)
        .isEqualTo(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_TRANSIENT);
    assertThat(fieldNode.name).isEqualTo(ShadowConstants.CLASS_HANDLER_DATA_FIELD_NAME);
    assertThat(fieldNode.desc).isEqualTo(Type.getDescriptor(Object.class));
    assertThat(fieldNode.signature).isEqualTo(Type.getDescriptor(Object.class));
    assertThat(fieldNode.value).isNull();
  }
}
