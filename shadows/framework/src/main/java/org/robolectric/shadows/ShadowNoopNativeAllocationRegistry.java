package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import libcore.util.NativeAllocationRegistry;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link NativeAllocationRegistry} that is a no-op. */
@Implements(
    value = NativeAllocationRegistry.class,
    minSdk = N,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowNoopNativeAllocationRegistry {

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, Object allocator) {
    return () -> {};
  }

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, long nativePtr) {
    return () -> {};
  }
}
