package org.robolectric.interceptors;

import static com.google.common.truth.Truth.assertThat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodSignature;
import org.robolectric.util.Function;

@RunWith(JUnit4.class)
public class AndroidInterceptorsTest {
  @Test
  public void testNioUtilsFreeDirectBufferInterceptor()
      throws NoSuchMethodException, IllegalAccessException {

    Interceptors interceptors = new Interceptors(AndroidInterceptors.all());
    Interceptor interceptor = interceptors.findInterceptor("java.nio.NioUtils", "freeDirectBuffer");
    assertThat(interceptor).isNotNull();

    MethodHandle methodHandle =
        interceptor.getMethodHandle(
            "freeDirectBuffer",
            MethodType.methodType(void.class, new Class<?>[] {ByteBuffer.class}));
    assertThat(methodHandle).isNotNull();

    Function<Object, Object> function =
        interceptor.handle(
            MethodSignature.parse("java.nio.NioUtils/freeDirectBuffer(Ljava.nio.ByteBuffer;)V"));
    assertThat(function).isNotNull();
    // Actual invocation is a no-op.
    function.call(/* theClass= */ null, ByteBuffer.allocate(0), /* params= */ null);
  }
}
