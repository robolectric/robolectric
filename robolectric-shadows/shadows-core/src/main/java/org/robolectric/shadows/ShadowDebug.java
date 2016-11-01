package org.robolectric.shadows;

import android.os.Debug;

import com.google.common.collect.ImmutableMap;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.Map;

import static android.os.Build.VERSION_CODES;
import static android.os.Build.VERSION_CODES.M;

/**
 * Shadow for {@link android.os.Debug}.
 */
@Implements(Debug.class)
public class ShadowDebug {
  @Implementation
  public static void __staticInitializer__() {
    // Avoid calling Environment.getLegacyExternalStorageDirectory()
  }

  @Implementation
  public static long getNativeHeapAllocatedSize() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  @Implementation(minSdk = M)
  public static Map<String, String> getRuntimeStats() {
    return ImmutableMap.<String, String>builder().build();
  }
}
