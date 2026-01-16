package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.HandlerThread;
import android.window.SurfaceSyncGroup;
import com.google.common.util.concurrent.Uninterruptibles;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/** Shadow for new SurfaceSyncGroup introduced in android U. */
@Implements(
    value = SurfaceSyncGroup.class,
    minSdk = UPSIDE_DOWN_CAKE,
    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowSurfaceSyncGroup {

  @Resetter
  public static void reset() {
    if (RuntimeEnvironment.getApiLevel() > TIRAMISU) {
      HandlerThread hThread = reflector(SurfaceSyncGroupReflector.class).getHandlerThread();
      if (hThread != null) {
        hThread.quit();
        Uninterruptibles.joinUninterruptibly(hThread);
        reflector(SurfaceSyncGroupReflector.class).setHandlerThread(null);
      }
    }
  }

  // The real implementation will add post a delayed-by-1s Runnable to its HandlerThread.
  // This Runnable will have hard references to SurfaceSyncGroup.mAddedToSyncListener ->
  // ViewRootImpl which will prevent garbage collection
  @Implementation
  protected void addTimeout() {}

  @ForType(SurfaceSyncGroup.class)
  private interface SurfaceSyncGroupReflector {
    @Accessor("sHandlerThread")
    @Static
    HandlerThread getHandlerThread();

    @Accessor("sHandlerThread")
    @Static
    void setHandlerThread(HandlerThread t);
  }
}
