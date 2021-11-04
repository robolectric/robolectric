package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.MessageQueue;
import android.os.SystemClock;
import android.view.DisplayEventReceiver;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.time.Duration;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Reflector;
import org.robolectric.util.reflector.WithType;

@Implements(
    className = "android.view.DisplayEventReceiver",
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowDisplayEventReceiver {

  private static NativeObjRegistry<NativeDisplayEventReceiver> nativeObjRegistry =
      new NativeObjRegistry<>(NativeDisplayEventReceiver.class);
  private static int asyncVsyncDelay;

  @RealObject protected DisplayEventReceiver realReceiver;

  private static final Duration VSYNC_DELAY = Duration.ofMillis(1);

  @Implementation(minSdk = O, maxSdk = Q)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver, MessageQueue msgQueue, int vsyncSource) {
    return nativeObjRegistry.register(new NativeDisplayEventReceiver(receiver));
  }

  @Implementation(minSdk = M, maxSdk = N_MR1)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver, MessageQueue msgQueue) {
    return nativeObjRegistry.register(new NativeDisplayEventReceiver(receiver));
  }

  @Implementation(minSdk = KITKAT_WATCH, maxSdk = LOLLIPOP_MR1)
  protected static long nativeInit(DisplayEventReceiver receiver, MessageQueue msgQueue) {
    return nativeObjRegistry.register(
        new NativeDisplayEventReceiver(new WeakReference<>(receiver)));
  }

  @Implementation(maxSdk = KITKAT)
  protected static int nativeInit(Object receiver, Object msgQueue) {
    return (int)
        nativeObjRegistry.register(
            new NativeDisplayEventReceiver(new WeakReference<>((DisplayEventReceiver) receiver)));
  }

  @Implementation(minSdk = R)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver,
      MessageQueue msgQueue,
      int vsyncSource,
      int configChanged) {
    return nativeInit(receiver, msgQueue);
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeDispose(long receiverPtr) {
    NativeDisplayEventReceiver receiver = nativeObjRegistry.unregister(receiverPtr);
    if (receiver != null) {
      receiver.dispose();
    }
  }

  @Implementation(maxSdk = KITKAT)
  protected static void nativeDispose(int receiverPtr) {
    NativeDisplayEventReceiver receiver = nativeObjRegistry.unregister(receiverPtr);
    if (receiver != null) {
      receiver.dispose();
    }
  }

  @Implementation(minSdk = KITKAT_WATCH)
  protected static void nativeScheduleVsync(long receiverPtr) {
    nativeObjRegistry.getNativeObject(receiverPtr).scheduleVsync();
  }

  @Implementation(maxSdk = KITKAT)
  protected static void nativeScheduleVsync(int receiverPtr) {
    nativeObjRegistry.getNativeObject(receiverPtr).scheduleVsync();
  }

  @Implementation(minSdk = JELLY_BEAN_MR1, maxSdk = R)
  protected void dispose(boolean finalized) {
    CloseGuard closeGuard =
        Reflector.reflector(DisplayEventReceiverReflector.class, realReceiver).getCloseGuard();
    // Suppresses noisy CloseGuard warning
    if (closeGuard != null) {
      closeGuard.close();
    }
    reflector(DisplayEventReceiverReflector.class, realReceiver).dispose(finalized);
  }

  static void setAsyncVsync(int delayMillis) {
    asyncVsyncDelay = delayMillis;
  }

  @Resetter
  public static void reset() {
    asyncVsyncDelay = 0;
  }

  protected void onVsync() {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      reflector(DisplayEventReceiverReflector.class, realReceiver)
          .onVsync(ShadowSystem.nanoTime(), 1);
    } else if (RuntimeEnvironment.getApiLevel() < Q) {
      reflector(DisplayEventReceiverReflector.class, realReceiver)
          .onVsync(ShadowSystem.nanoTime(), 0, /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */ 1);
    } else if (RuntimeEnvironment.getApiLevel() <= R) {
      reflector(DisplayEventReceiverReflector.class, realReceiver)
          .onVsync(ShadowSystem.nanoTime(), 0L, /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */ 1);
    } else {
      try {
        // onVsync takes a package-private VSyncData class as a parameter, thus reflection
        // needs to be used
        Object vsyncData =
            ReflectionHelpers.callConstructor(
                Class.forName("android.view.DisplayEventReceiver$VsyncEventData"),
                ClassParameter.from(long.class, 1), /* id */
                ClassParameter.from(long.class, 10), /* frameDeadline */
                ClassParameter.from(long.class, 1)); /* frameInterval */

        reflector(DisplayEventReceiverReflector.class, realReceiver)
            .onVsync(
                ShadowSystem.nanoTime(),
                0L, /* physicalDisplayId currently ignored */
                1, /* frame */
                vsyncData /* VsyncEventData */);
      } catch (ClassNotFoundException e) {
        throw new LinkageError("Unable to construct VsyncEventData", e);
      }
    }
  }

  /**
   * A simulation of the native code that provides synchronization with the display hardware frames
   * (aka vsync), that attempts to provide relatively accurate behavior, while adjusting for
   * Robolectric's fixed system clock.
   *
   * <p>In the default mode, requests for a vsync callback will be processed immediately inline. The
   * system clock is also auto advanced by VSYNC_DELAY to appease the calling Choreographer that
   * expects an advancing system clock. This mode allows seamless view layout / traversal operations
   * with a simple {@link ShadowLooper#idle()} call.
   *
   * <p>However, the default mode can cause problems with animations which continually request vsync
   * callbacks, leading to timeouts and hamper attempts to verify animations in progress. For those
   * use cases, an 'async' callback mode is provided (via the {@link
   * ShadowChoreographer#setPostFrameCallbackDelay(int)} API. In this mode, vsync requests will be
   * scheduled asynchronously by listening to clock updates.
   */
  private static class NativeDisplayEventReceiver {

    private final WeakReference<DisplayEventReceiver> receiverRef;
    private final ShadowPausedSystemClock.Listener clockListener;
    private long nextVsyncTime = 0;

    public NativeDisplayEventReceiver(WeakReference<DisplayEventReceiver> receiverRef) {
      this.receiverRef = receiverRef;
      // register a clock listener for the async mode
      this.clockListener =
          new ShadowPausedSystemClock.Listener() {
            @Override
            public void clockUpdated(long newCurrentTimeMillis) {
              if (nextVsyncTime > 0 && newCurrentTimeMillis >= nextVsyncTime) {
                nextVsyncTime = 0;
                doVsync();
              }
            }
          };
      ShadowPausedSystemClock.addListener(clockListener);
    }

    void dispose() {
      ShadowPausedSystemClock.removeListener(clockListener);
    }

    public void scheduleVsync() {
      if (asyncVsyncDelay > 0 && nextVsyncTime == 0) {
        nextVsyncTime = SystemClock.uptimeMillis() + asyncVsyncDelay;
      } else {
        // simulate an immediate callback
        ShadowSystemClock.advanceBy(VSYNC_DELAY);
        doVsync();
      }
    }

    private void doVsync() {
      DisplayEventReceiver receiver = receiverRef.get();
      if (receiver != null) {
        ShadowDisplayEventReceiver shadowReceiver = Shadow.extract(receiver);
        shadowReceiver.onVsync();
      }
    }
  }

  /** Reflector interface for {@link DisplayEventReceiver}'s internals. */
  @ForType(DisplayEventReceiver.class)
  protected interface DisplayEventReceiverReflector {

    @Direct
    void dispose(boolean finalized);

    void onVsync(long timestampNanos, int frame);

    void onVsync(long timestampNanos, int physicalDisplayId, int frame);

    void onVsync(long timestampNanos, long physicalDisplayId, int frame);

    void onVsync(
        long timestampNanos,
        long physicalDisplayId,
        int frame,
        @WithType("android.view.DisplayEventReceiver$VsyncEventData") Object vsyncEventData);

    @Accessor("mCloseGuard")
    CloseGuard getCloseGuard();
  }
}
