package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.util.reflector.Reflector.reflector;

import com.android.internal.os.ApplicationSharedMemory;
import java.io.FileDescriptor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link ApplicationSharedMemory}. This gets initialized in {@link
 * AndroidTestEnvironment}.
 */
@Implements(value = ApplicationSharedMemory.class, isInAndroidSdk = false, minSdk = BAKLAVA)
public class ShadowApplicationSharedMemory {

  /**
   * The real {@code create()} calls {@code FileDescriptor.setInt$}, which Robolectric routes
   * through {@code jdk.internal.access.SharedSecrets} and fails with {@link IllegalAccessException}
   * on JDK 9+ without an add-opens. Since there is no real shared memory here ({@link #nativeMap}
   * is stubbed), build the inert instance directly instead, mirroring the real result so {@code
   * isMapped()} stays {@code true}.
   */
  @Implementation
  protected static ApplicationSharedMemory create() {
    return ReflectionHelpers.callConstructor(
        ApplicationSharedMemory.class,
        ClassParameter.from(FileDescriptor.class, new FileDescriptor()),
        ClassParameter.from(boolean.class, /* isMutable= */ true),
        ClassParameter.from(long.class, /* mappedAddress= */ nativeMap(0, true)));
  }

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
