package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;

import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.internal.bytecode.InvokeDynamicSupport;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Integration tests for Android interceptors. */
@RunWith(AndroidJUnit4.class)
public class AndroidInterceptorsIntegrationTest {

  @Test
  public void systemLogE_shouldWriteToStderr() throws Throwable {
    PrintStream stderr = System.err;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(stream);
    System.setErr(printStream);
    try {
      invokeDynamic(System.class, "logE", void.class, ClassParameter.from(String.class, "hello"));
      invokeDynamic(
          System.class,
          "logE",
          void.class,
          ClassParameter.from(String.class, "world"),
          ClassParameter.from(Throwable.class, new Throwable("throw")));
      assertThat(stream.toString())
          .isEqualTo(String.format("System.logE: hello%n"
              + "System.logE: worldjava.lang.Throwable: throw%n"));
    } finally {
      System.setErr(stderr);
    }
  }

  @Test
  public void systemNanoTime_shouldReturnShadowClockTime() throws Throwable {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      SystemClock.setCurrentTimeMillis(200);
    } else {
      ShadowSystemClock.setNanoTime(Duration.ofMillis(200).toNanos());
    }

    long nanoTime = invokeDynamic(System.class, "nanoTime", long.class);
    assertThat(nanoTime).isEqualTo(Duration.ofMillis(200).toNanos());
  }

  @Test
  public void systemCurrentTimeMillis_shouldReturnShadowClockTime() throws Throwable {
    if (ShadowLooper.looperMode() == LooperMode.Mode.PAUSED) {
      SystemClock.setCurrentTimeMillis(200);
    } else {
      ShadowSystemClock.setNanoTime(Duration.ofMillis(200).toNanos());
    }

    long currentTimeMillis = invokeDynamic(System.class, "currentTimeMillis", long.class);
    assertThat(currentTimeMillis).isEqualTo(200);
  }

  @SuppressWarnings({"unchecked", "TypeParameterUnusedInFormals"})
  private static <T> T invokeDynamic(
      Class<?> cls, String methodName, Class<?> returnType, ClassParameter<?>... params)
      throws Throwable {
    MethodType methodType =
        MethodType.methodType(
            returnType, Arrays.stream(params).map(param -> param.clazz).toArray(Class[]::new));
    CallSite callsite =
        InvokeDynamicSupport.bootstrapIntrinsic(
            MethodHandles.lookup(), methodName, methodType, cls.getName());
    return (T)
        callsite
            .dynamicInvoker()
            .invokeWithArguments(
                Arrays.stream(params).map(param -> param.val).collect(Collectors.toList()));
  }
}
