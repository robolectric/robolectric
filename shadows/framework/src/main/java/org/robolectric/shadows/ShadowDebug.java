package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import android.os.Debug;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

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
