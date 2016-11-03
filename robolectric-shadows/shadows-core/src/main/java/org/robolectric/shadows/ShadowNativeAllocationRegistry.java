package org.robolectric.shadows;

import libcore.util.NativeAllocationRegistry;
import org.robolectric.annotation.Implements;

import static android.os.Build.VERSION_CODES.N;

@Implements(value = NativeAllocationRegistry.class, callThroughByDefault = false, minSdk = N)
public class ShadowNativeAllocationRegistry {
}
