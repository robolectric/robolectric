package org.robolectric;

import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.SandboxTestRunner;
import org.robolectric.internal.bytecode.SandboxConfig;

/* Tests for native method instrumentation. */
@RunWith(SandboxTestRunner.class)
public class NativeMethodInvocationTest {
  @SandboxConfig(shadows = ShadowClassWithNativeMethods.class)
  @Test
  public void callNativeMethodsByDefault_unsatisfiedLinkError() {
    ClassWithNativeMethods classWithNativeMethods = new ClassWithNativeMethods();
    assertThrows(UnsatisfiedLinkError.class, ClassWithNativeMethods::staticNativeMethod);
    assertThrows(UnsatisfiedLinkError.class, classWithNativeMethods::instanceNativeMethod);
  }

  @Instrument
  static class ClassWithNativeMethods {
    static native void staticNativeMethod();

    native void instanceNativeMethod();
  }

  /** Shadow for {@link NativeMethodInvocationTest.ClassWithNativeMethods} */
  @Implements(value = ClassWithNativeMethods.class, callNativeMethodsByDefault = true)
  public static class ShadowClassWithNativeMethods {}
}
