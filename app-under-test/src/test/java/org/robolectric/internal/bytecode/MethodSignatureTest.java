package org.robolectric.internal.bytecode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodSignatureTest {

  @Test
  public void parse_shouldHandlePrimitiveReturnTypes() {
    final MethodSignature signature = MethodSignature.parse("java/lang/Long/foo(Ljava/lang/Integer;)Z");
    assertThat(signature.className).isEqualTo("java.lang.Long");
    assertThat(signature.methodName).isEqualTo("foo");
    assertThat(signature.paramTypes).contains("java.lang.Integer");
    assertThat(signature.returnType).isEqualTo("boolean");
  }

  @Test
  public void parse_shouldHandleObjectReturnTypes() {
    final MethodSignature signature = MethodSignature.parse("java/lang/Long/foo(Ljava/lang/Integer;)Ljava/lang/Long;");
    assertThat(signature.className).isEqualTo("java.lang.Long");
    assertThat(signature.methodName).isEqualTo("foo");
    assertThat(signature.paramTypes).contains("java.lang.Integer");
    assertThat(signature.returnType).isEqualTo("java.lang.Long");
  }
}