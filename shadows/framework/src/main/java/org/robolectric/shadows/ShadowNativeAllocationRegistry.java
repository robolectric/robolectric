package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import libcore.util.NativeAllocationRegistry;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.nativeruntime.NativeAllocationRegistryNatives;
import org.robolectric.shadows.ShadowNativeAllocationRegistry.Picker;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link NativeAllocationRegistry} that is backed by native code */
@Implements(
    value = NativeAllocationRegistry.class,
    minSdk = O,
    isInAndroidSdk = false,
    shadowPicker = Picker.class)
public class ShadowNativeAllocationRegistry {

  @RealObject protected NativeAllocationRegistry realNativeAllocationRegistry;

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, long nativePtr) {
    // Avoid registering native allocations for classes where native methods are no-ops (like
    // Binder), or for classes that simulate native pointers (like binary resources) but don't
    // actually use native libraries.
    if (nativePtr != 0 && hasValidFreeFunction()) {
      return reflector(NativeAllocationRegistryReflector.class, realNativeAllocationRegistry)
          .registerNativeAllocation(referent, nativePtr);
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
    NativeAllocationRegistryNatives.applyFreeFunction(freeFunction, nativePtr);
  }

  @ForType(NativeAllocationRegistry.class)
  interface NativeAllocationRegistryReflector {
    @Direct
    Runnable registerNativeAllocation(Object referent, long nativePtr);

    @Accessor("freeFunction")
    long getFreeFunction();
  }

  /** Shadow picker for {@link NativeAllocationRegistry}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowNoopNativeAllocationRegistry.class, ShadowNativeAllocationRegistry.class);
    }
  }
}
