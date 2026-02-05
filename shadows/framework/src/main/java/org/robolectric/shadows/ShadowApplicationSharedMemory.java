package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.util.reflector.Reflector.reflector;

import com.android.internal.os.ApplicationSharedMemory;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link ApplicationSharedMemory}. This gets initialized in {@link
 * AndroidTestEnvironment}.
 */
@Implements(value = ApplicationSharedMemory.class, isInAndroidSdk = false, minSdk = BAKLAVA)
public class ShadowApplicationSharedMemory {

  @Implementation
  protected static long nativeMap(int fd, boolean isMutable) {
    return 1;
  }

  @Resetter
  public static void reset() {
    reflector(ApplicationSharedMemoryReflector.class).setInstance(null);
  }

  @ForType(ApplicationSharedMemory.class)
  interface ApplicationSharedMemoryReflector {
    @Accessor("sInstance")
    void setInstance(ApplicationSharedMemory instance);
  }
}
