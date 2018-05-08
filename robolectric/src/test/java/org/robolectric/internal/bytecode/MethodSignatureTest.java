package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MethodSignatureTest {

  @Test
  public void parse_shouldHandlePrimitiveReturnTypes() {
    final MethodSignature signature = MethodSignature.parse("java/lang/Long/foo(Ljava/lang/Integer;)Z");
    assertThat(signature.className).isEqualTo("java.lang.Long");
    assertThat(signature.methodName).isEqualTo("foo");
    assertThat(signature.paramTypes).asList().contains("java.lang.Integer");
    assertThat(signature.returnType).isEqualTo("boolean");
  }

  @Test
  public void parse_shouldHandleObjectReturnTypes() {
    final MethodSignature signature = MethodSignature.parse("java/lang/Long/foo(Ljava/lang/Integer;)Ljava/lang/Long;");
    assertThat(signature.className).isEqualTo("java.lang.Long");
    assertThat(signature.methodName).isEqualTo("foo");
    assertThat(signature.paramTypes).asList().contains("java.lang.Integer");
    assertThat(signature.returnType).isEqualTo("java.lang.Long");
  }
}