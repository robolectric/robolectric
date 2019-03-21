package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import libcore.util.NativeAllocationRegistry;
import libcore.util.NativeAllocationRegistry.Allocator;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = NativeAllocationRegistry.class, minSdk = N, isInAndroidSdk = false)
public class ShadowNativeAllocationRegistry {

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, Allocator allocator) {
    return null;
  }

  @Implementation
  protected Runnable registerNativeAllocation(Object referent, long nativePtr) {
    return null;
  }
}
