package org.robolectric.interceptors;

import static com.google.common.truth.Truth.assertThat;

import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.Socket;
import java.time.Duration;
import java.util.Arrays;
import java.util.regex.Pattern;
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
  public void systemLog_shouldWriteToStderr() throws Throwable {
    PrintStream stderr = System.err;
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    PrintStream printStream = new PrintStream(stream);
    System.setErr(printStream);
    try {
      for (String methodName : new String[] {"logE", "logW"}) {
        stream.reset();
        // One parameter: [message]
        invokeDynamic(
            System.class, methodName, void.class, ClassParameter.from(String.class, "hello"));
        String expected1 = String.format("System\\.%s: hello", methodName);
        // Two parameters: [message, throwable]
        // We verify that the stack trace is dumped by looking for the name of this method.
        invokeDynamic(
            System.class,
            methodName,
            void.class,
            ClassParameter.from(String.class, "world"),
            ClassParameter.from(Throwable.class, new Throwable("message")));
        String expected2 =
            String.format(
                "System.%s: world.*java\\.lang\\.Throwable: message.*"
                    + "at .*AndroidInterceptorsIntegrationTest\\.systemLog_shouldWriteToStderr",
                methodName);
        // Due to the possibility of running tests in Parallel, assertions checking stderr contents
        // should not assert equality.
        assertThat(stream.toString())
            .matches(Pattern.compile(".*" + expected1 + ".*" + expected2 + ".*", Pattern.DOTALL));
      }
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

  /* Creates a FileDescriptor-object using reflection. Note that the "fd"-field is present for
   * both the Windows and Unix implementations of FileDescriptor in OpenJDK.
   */
  private static FileDescriptor createFd(int fd) throws Throwable {
    FileDescriptor ret = new FileDescriptor();
    Field accessor = FileDescriptor.class.getDeclaredField("fd");
    accessor.setAccessible(true);
    accessor.set(ret, fd);
    return ret;
  }

  @Test
  public void fileDescriptorRelease_isValid_correctResultsAfterRelease() throws Throwable {
    FileDescriptor original = createFd(42);
    assertThat(original.valid()).isTrue();
    FileDescriptor copy =
        invokeDynamic(
            FileDescriptor.class,
            "release$",
            FileDescriptor.class,
            ClassParameter.from(FileDescriptor.class, original));
    assertThat(copy.valid()).isTrue();
    assertThat(original.valid()).isFalse();
  }

  @Test
  public void fileDescriptorRelease_allowsReleaseOnInvalidFd() throws Throwable {
    FileDescriptor original = new FileDescriptor();
    assertThat(original.valid()).isFalse();
    FileDescriptor copy =
        invokeDynamic(
            FileDescriptor.class,
            "release$",
            FileDescriptor.class,
            ClassParameter.from(FileDescriptor.class, original));
    assertThat(copy.valid()).isFalse();
    assertThat(original.valid()).isFalse();
  }

  @Test
  public void fileDescriptorRelease_doubleReleaseReturnsInvalidFd() throws Throwable {
    FileDescriptor original = createFd(42);
    assertThat(original.valid()).isTrue();
    FileDescriptor copy =
        invokeDynamic(
            FileDescriptor.class,
            "release$",
            FileDescriptor.class,
            ClassParameter.from(FileDescriptor.class, original));
    FileDescriptor copy2 =
        invokeDynamic(
            FileDescriptor.class,
            "release$",
            FileDescriptor.class,
            ClassParameter.from(FileDescriptor.class, original));
    assertThat(copy.valid()).isTrue();
    assertThat(copy2.valid()).isFalse();
    assertThat(original.valid()).isFalse();
  }

  @Test
  public void fileDescriptorRelease_releaseFdCorrect() throws Throwable {
    FileDescriptor original = createFd(42);
    FileDescriptor copy =
        invokeDynamic(
            FileDescriptor.class,
            "release$",
            FileDescriptor.class,
            ClassParameter.from(FileDescriptor.class, original));
    Field accessor = FileDescriptor.class.getDeclaredField("fd");
    accessor.setAccessible(true);
    assertThat(accessor.get(copy)).isEqualTo(42);
  }

  @Test
  public void socketFileDescriptor_returnsNullFileDescriptor() throws Throwable {
    FileDescriptor fd =
        invokeDynamic(
            Socket.class,
            "getFileDescriptor$",
            void.class,
            ClassParameter.from(Socket.class, new Socket()));
    assertThat(fd).isNull();
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
