package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
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
    assertThat(methodNode.instructions.size()).isEqualTo(2);
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
