package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.Iterables;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
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
  public void instrumentNativeMethod_legacy() {
    ClassNode classNode = createClassWithNativeMethod();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    String someFunctionName = Shadow.directMethodName("org.example.MyClass", "someFunction");
    MethodNode methodNode = findMethodNode(classNode, someFunctionName);

    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: instructions have been rewritten to return 0.
    assertThat(methodNode.instructions.size()).isEqualTo(2);
    assertThat(methodNode.instructions.get(0).getOpcode()).isEqualTo(Opcodes.ICONST_0);
    assertThat(methodNode.instructions.get(1).getOpcode()).isEqualTo(Opcodes.IRETURN);
  }

  @Test
  public void instrumentNativeMethod_withoutExemption_generatesThrowException() throws IOException {
    File exemptionsFile = tempFolder.newFile("natives.txt");
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(exemptionsFile.getPath(), UTF_8))) {
      writer.write("org.example.MyClass#someOtherMethod()V\n");
    }

    NativeCallHandler nativeCallHandler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ true);
    instrumentor.setNativeCallHandler(nativeCallHandler);

    ClassNode classNode = createClassWithNativeMethod();
    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    String someFunctionName = Shadow.directMethodName("org.example.MyClass", "someFunction");
    MethodNode methodNode = findMethodNode(classNode, someFunctionName);
    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: instructions have been rewritten to throw and return.
    assertThat(methodNode.instructions.size()).isEqualTo(7);
    assertThat(methodNode.instructions.get(0).getOpcode()).isEqualTo(Opcodes.NEW);
    assertThat(methodNode.instructions.get(1).getOpcode()).isEqualTo(Opcodes.DUP);
    assertThat(methodNode.instructions.get(2).getOpcode()).isEqualTo(Opcodes.LDC);
    assertThat(methodNode.instructions.get(3).getOpcode()).isEqualTo(Opcodes.INVOKESPECIAL);
    assertThat(methodNode.instructions.get(4).getOpcode()).isEqualTo(Opcodes.ATHROW);
    assertThat(methodNode.instructions.get(5).getOpcode()).isEqualTo(Opcodes.ICONST_0);
    assertThat(methodNode.instructions.get(6).getOpcode()).isEqualTo(Opcodes.IRETURN);
  }

  @Test
  public void instrumentNativeMethod_withExemption_generatesNoOpReturn() throws IOException {
    File exemptionsFile = tempFolder.newFile("natives.txt");
    try (BufferedWriter writer =
        new BufferedWriter(new FileWriter(exemptionsFile.getPath(), UTF_8))) {
      writer.write("org.example.MyClass#someOtherMethod()V\n");
      writer.write("org.example.MyClass#someFunction()I\n");
    }

    NativeCallHandler nativeCallHandler =
        new NativeCallHandler(
            exemptionsFile, /* writeExemptions= */ false, /* throwOnNatives= */ true);
    instrumentor.setNativeCallHandler(nativeCallHandler);

    ClassNode classNode = createClassWithNativeMethod();

    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    String someFunctionName = Shadow.directMethodName("org.example.MyClass", "someFunction");
    MethodNode methodNode = findMethodNode(classNode, someFunctionName);

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

    assertThat(methodNode.access & Opcodes.ACC_NATIVE).isNotEqualTo(0);
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    assertThat(methodNode.access & Opcodes.ACC_SYNTHETIC).isNotEqualTo(0);
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
}
