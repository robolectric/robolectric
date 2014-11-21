package org.robolectric.shadows;

import android.os.Looper;
import android.view.Choreographer;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ShadowThingy;
import org.robolectric.util.SoftThreadLocal;

@Implements(Choreographer.class)
public class ShadowChoreographer {
  @RealObject private Choreographer realObject;
  private static final Thread MAIN_THREAD = Thread.currentThread();
  private static SoftThreadLocal<Choreographer> sThreadInstance = makeThreadLocal();

  private static SoftThreadLocal<Choreographer> makeThreadLocal() {
    return new SoftThreadLocal<Choreographer>() {
      @Override protected Choreographer create() {
        Looper looper = Looper.myLooper();
        if (looper == null) {
          throw new IllegalStateException("The current thread must have a looper!");
        }
        return ShadowThingy.newInstance(Choreographer.class, new Class[]{Looper.class}, new Object[]{looper});
      }
    };
  }

  @Implementation
  public static Choreographer getInstance() {
    return sThreadInstance.get();
  }

  @Resetter
  public static synchronized void resetThreadLoopers() {
    // Blech. We need to share the main looper because somebody might refer to it in a static
    // field. We also need to keep it in a soft reference so we don't max out permgen.

    if (Thread.currentThread() != MAIN_THREAD) {
      throw new RuntimeException("you should only be calling this from the main thread!");
    }

    sThreadInstance = makeThreadLocal();
  }
}

