package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.P;

import libcore.util.NativeAllocationRegistry;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link NativeAllocationRegistry} that is a no-op. */
@Implements(value = NativeAllocationRegistry.class, minSdk = N, isInAndroidSdk = false)
public class ShadowNoopNativeAllocationRegistry {

  @Implementation(maxSdk = P)
  protected Runnable registerNativeAllocation(
      Object referent,
      @ClassName("libcore.util.NativeAllocationRegistry$Allocator") Object allocator) {
    return () -> {};
  }

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, long nativePtr) {
    return () -> {};
  }
}
