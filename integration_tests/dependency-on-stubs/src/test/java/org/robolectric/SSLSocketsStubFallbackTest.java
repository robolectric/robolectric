package org.robolectric;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.net.ssl.SSLSocket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Reproduces the gRPC/Firestore "SSLSockets not mocked" failure.
 *
 * <p>{@code android.net.ssl.SSLSockets} was added in API 29, so it is absent from android-all below
 * 29. In a project that depends on the compile-time stubs jar (as Skip/AGP unit tests do — this
 * module does too), Robolectric's class loader falls back to that stub when a class is missing from
 * android-all, and the stub's method bodies throw {@code "Method ... not mocked"}. gRPC-okhttp's
 * {@code OkHttpProtocolNegotiator$AndroidNegotiator} reflectively resolves {@code SSLSockets} for
 * TLS feature-detection and invokes it, which is exactly what broke Firebase Firestore in
 * skip-firebase.
 */
@RunWith(RobolectricTestRunner.class)
public class SSLSocketsStubFallbackTest {

  @Test
  @Config(sdk = P)
  public void isSupportedSocket_belowApi29_resolvesToNotMockedStub() throws Exception {
    // Mirror the reflective lookup gRPC-okhttp does in its static initializer.
    Class<?> sslSockets = Class.forName("android.net.ssl.SSLSockets");
    Method isSupportedSocket = sslSockets.getMethod("isSupportedSocket", SSLSocket.class);

    // At API 28, SSLSockets is not in android-all, so it resolves to the compileSdk-36 mockable
    // android.jar stub, whose body throws the "not mocked" RuntimeException.
    InvocationTargetException e =
        assertThrows(
            InvocationTargetException.class,
            () -> isSupportedSocket.invoke(null, new Object[] {null}));

    Throwable cause = e.getCause();
    assertNotNull(cause);
    assertTrue(
        "expected a 'not mocked' stub error but was: " + cause,
        cause instanceof RuntimeException
            && cause.getMessage() != null
            && cause.getMessage().contains("not mocked")
            && cause.getMessage().contains("android.net.ssl.SSLSockets"));
  }

  @Test
  @Config(sdk = Q)
  public void isSupportedSocket_atApi29_resolvesToRealAndroidAll() throws Exception {
    // At API 29, SSLSockets exists in android-all, so it resolves to the real (instrumented)
    // implementation, which returns false for a non-conscrypt socket — no "not mocked".
    Class<?> sslSockets = Class.forName("android.net.ssl.SSLSockets");
    Method isSupportedSocket = sslSockets.getMethod("isSupportedSocket", SSLSocket.class);

    Object result = isSupportedSocket.invoke(null, new Object[] {null});

    assertEquals(false, result);
  }
}
