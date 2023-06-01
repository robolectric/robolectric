package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
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
    ClassNode classNode = new ClassNode();
    classNode.name = "org/example/MyClass";

    MethodNode methodNode = new MethodNode();
    methodNode.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_NATIVE;
    methodNode.name = "someFunction";
    methodNode.desc = "()I";
    methodNode.signature = "()";
    methodNode.exceptions = ImmutableList.of();
    methodNode.visibleAnnotations = ImmutableList.of();

    classNode.methods.add(methodNode);

    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: original method has been renamed to a robolectric delegate
    assertThat(methodNode.name).isEqualTo("$$robo$$org_example_MyClass$someFunction");
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

    ClassNode classNode = new ClassNode();
    classNode.name = "org/example/MyClass";

    MethodNode methodNode = new MethodNode();
    methodNode.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_NATIVE;
    methodNode.name = "someFunction";
    methodNode.desc = "()I";
    methodNode.signature = "()";
    methodNode.exceptions = ImmutableList.of();
    methodNode.visibleAnnotations = ImmutableList.of();

    classNode.methods.add(methodNode);

    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: original method has been renamed to a robolectric delegate
    assertThat(methodNode.name).isEqualTo("$$robo$$org_example_MyClass$someFunction");
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

    ClassNode classNode = new ClassNode();
    classNode.name = "org/example/MyClass";

    MethodNode methodNode = new MethodNode();
    methodNode.access = Opcodes.ACC_PUBLIC + Opcodes.ACC_NATIVE;
    methodNode.name = "someFunction";
    methodNode.desc = "()I";
    methodNode.signature = "()";
    methodNode.exceptions = ImmutableList.of();
    methodNode.visibleAnnotations = ImmutableList.of();

    classNode.methods.add(methodNode);

    MutableClass clazz =
        new MutableClass(
            classNode, InstrumentationConfiguration.newBuilder().build(), classNodeProvider);
    instrumentor.instrument(clazz);

    // Side effect: original method has been made private.
    assertThat(methodNode.access & Opcodes.ACC_PRIVATE).isNotEqualTo(0);
    // Side effect: original method has been renamed to a robolectric delegate
    assertThat(methodNode.name).isEqualTo("$$robo$$org_example_MyClass$someFunction");
    // Side effect: instructions have been rewritten to return 0.
    assertThat(methodNode.instructions.size()).isEqualTo(2);
    assertThat(methodNode.instructions.get(0).getOpcode()).isEqualTo(Opcodes.ICONST_0);
    assertThat(methodNode.instructions.get(1).getOpcode()).isEqualTo(Opcodes.IRETURN);
  }
}
