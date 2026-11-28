package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Looper;
import android.os.TestLooperManager;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Remembers which {@link TestLooperManager} holds each looper, so Robolectric can use it to drive
 * the looper instead of failing to acquire a second one. The framework allows one holder per looper
 * from Baklava, and Espresso holds the main looper's while it runs a message.
 */
@Implements(value = TestLooperManager.class, minSdk = BAKLAVA, isInAndroidSdk = false)
public class ShadowTestLooperManager {

  private static final Object lock = new Object();

  @GuardedBy("lock")
  private static final Map<Looper, TestLooperManager> holders = new HashMap<>();

  @RealObject private TestLooperManager realManager;
  private Looper looper;

  @Implementation
  protected void __constructor__(Looper looper) {
    Shadow.invokeConstructor(
        TestLooperManager.class, realManager, ClassParameter.from(Looper.class, looper));
    this.looper = looper;
    synchronized (lock) {
      holders.put(looper, realManager);
    }
  }

  @Implementation
  protected void release() {
    reflector(TestLooperManagerReflector.class, realManager).release();
    synchronized (lock) {
      holders.remove(looper, realManager);
    }
  }

  /** Returns the manager that holds {@code looper}, or null if none does. */
  @Nullable
  static TestLooperManager getHolder(Looper looper) {
    synchronized (lock) {
      return holders.get(looper);
    }
  }

  @Resetter
  public static void reset() {
    synchronized (lock) {
      holders.clear();
    }
  }

  @ForType(TestLooperManager.class)
  interface TestLooperManagerReflector {
    @Direct
    void release();
  }
}
