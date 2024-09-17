package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;

import libcore.util.NativeAllocationRegistry;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.InDevelopment;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.versioning.AndroidVersions.V;

/** Shadow for {@link NativeAllocationRegistry} that is a no-op. */
@Implements(
    value = NativeAllocationRegistry.class,
    minSdk = N,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowNoopNativeAllocationRegistry {

  @RealObject protected NativeAllocationRegistry realNativeAllocationRegistry;

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, Object allocator) {
    return () -> {};
  }

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, long nativePtr) {
    return () -> {};
  }

  /**
   * The newly introduced constructor (in version V) now performs a null check on the classLoader
   * argument. We intercept the constructor calls so as to pass a non-null classloader. The
   * classloader would be Robolectric's SandboxClassloader, but the value itself does not affect the
   * behavior of actual class.
   */
  @InDevelopment
  @Implementation(minSdk = V.SDK_INT)
  protected void __constructor__(
      ClassLoader classLoader,
      Class clazz,
      long freeFunction,
      long size,
      boolean mallocAllocation) {
    ClassLoader loader =
        (classLoader == null) ? Thread.currentThread().getContextClassLoader() : classLoader;
    invokeConstructor(
        NativeAllocationRegistry.class,
        realNativeAllocationRegistry,
        ClassParameter.from(ClassLoader.class, loader),
        ClassParameter.from(Class.class, clazz),
        ClassParameter.from(long.class, freeFunction),
        ClassParameter.from(long.class, size),
        ClassParameter.from(boolean.class, mallocAllocation));
  }
}
