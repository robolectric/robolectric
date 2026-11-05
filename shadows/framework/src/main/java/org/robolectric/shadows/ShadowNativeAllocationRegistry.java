package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.reflector.Reflector.reflector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import libcore.util.NativeAllocationRegistry;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.NativeAllocationRegistryNatives;
import org.robolectric.shadows.ShadowNativeAllocationRegistry.Picker;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for {@link NativeAllocationRegistry} that is backed by native code */
@Implements(
    value = NativeAllocationRegistry.class,
    minSdk = O,
    isInAndroidSdk = false,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeAllocationRegistry {

  /**
   * The registered allocations that aren't freed yet, by free function, like the per-class counts
   * of NativeAllocationRegistry.getMetrics() from Baklava.
   */
  private static final Map<Long, Long> liveAllocations = new ConcurrentHashMap<>();

  @RealObject protected NativeAllocationRegistry realNativeAllocationRegistry;

  /**
   * The newly introduced constructor (in version V) now performs a null check on the classLoader
   * argument. We intercept the constructor calls so as to pass a non-null classloader. The
   * classloader would be Robolectric's SandboxClassloader, but the value itself does not affect the
   * behavior of actual class.
   */
  @Implementation(minSdk = BAKLAVA)
  protected void __constructor__(
      ClassLoader classLoader,
      Class<?> clazz,
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

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, long nativePtr) {
    // Avoid registering native allocations for classes where native methods are no-ops (like
    // Binder), or for classes that simulate native pointers (like binary resources) but don't
    // actually use native libraries.
    if (nativePtr != 0 && hasValidFreeFunction()) {
      NativeAllocationRegistryReflector registry =
          reflector(NativeAllocationRegistryReflector.class, realNativeAllocationRegistry);
      Runnable freer = registry.registerNativeAllocation(referent, nativePtr);
      liveAllocations.merge(registry.getFreeFunction(), 1L, Long::sum);
      return freer;
    } else {
      return () -> {};
    }
  }

  private boolean hasValidFreeFunction() {
    return reflector(NativeAllocationRegistryReflector.class, realNativeAllocationRegistry)
            .getFreeFunction()
        != 0;
  }

  @Implementation
  protected static void applyFreeFunction(long freeFunction, long nativePtr) {
    if (RuntimeEnvironment.getApiLevel() <= UPSIDE_DOWN_CAKE) {
      NativeAllocationRegistryNatives.applyFreeFunction(freeFunction, nativePtr);
    } else {
      reflector(NativeAllocationRegistryReflector.class).applyFreeFunction(freeFunction, nativePtr);
    }
    liveAllocations.merge(freeFunction, -1L, Long::sum);
  }

  /**
   * Returns the number of registered native allocations that aren't freed yet, by free function. A
   * compat version of NativeAllocationRegistry.getMetrics(), which is only in Baklava and later.
   */
  static Map<Long, Long> getMetricsCompat() {
    return new HashMap<>(liveAllocations);
  }

  @ForType(NativeAllocationRegistry.class)
  interface NativeAllocationRegistryReflector {
    @Direct
    Runnable registerNativeAllocation(Object referent, long nativePtr);

    @Accessor("freeFunction")
    long getFreeFunction();

    @Static
    @Direct
    void applyFreeFunction(long freeFunction, long nativePtr);
  }

  /** Shadow picker for {@link NativeAllocationRegistry}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowNoopNativeAllocationRegistry.class, ShadowNativeAllocationRegistry.class);
    }
  }
}
