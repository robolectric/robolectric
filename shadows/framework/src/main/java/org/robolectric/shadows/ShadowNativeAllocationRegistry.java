package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import libcore.util.NativeAllocationRegistry;
import org.robolectric.annotation.Implements;

@Implements(value = NativeAllocationRegistry.class, callThroughByDefault = false, minSdk = N, isInAndroidSdk = false)
public class ShadowNativeAllocationRegistry {
}
