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
  protected static void __staticInitializer__() {
    // Avoid calling Environment.getLegacyExternalStorageDirectory()
  }

  @Implementation
  protected static long getNativeHeapAllocatedSize() {
    return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
  }

  @Implementation(minSdk = M)
  protected static Map<String, String> getRuntimeStats() {
    return ImmutableMap.<String, String>builder().build();
  }
}
