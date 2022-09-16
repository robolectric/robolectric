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
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;

import android.os.MessageQueue;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.DisplayEventReceiver;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.time.Duration;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;

/**
 * Shadow of {@link DisplayEventReceiver}. The {@link Choreographer} is a subclass of {@link
 * DisplayEventReceiver}, and receives vsync events from the display indicating the frequency that
 * frames should be generated.
 *
 * <p>The {@code ShadowDisplayEventReceiver} can run in either a paused mode or a non-paused mode,
 * see {@link ShadowChoreographer#isPaused()} and {@link ShadowChoreographer#setPaused(boolean)}. By
 * default it runs unpaused, and each time a frame callback is scheduled with the {@link
 * Choreographer} the clock is advanced to the next frame, configured by {@link
 * ShadowChoreographer#setFrameDelay(Duration)}. In paused mode the clock is not auto advanced and
 * the next frame will only trigger when the clock is advance manually or via the {@link
 * ShadowLooper}.
 */
@Implements(
    className = "android.view.DisplayEventReceiver",
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowDisplayEventReceiver {

  private static NativeObjRegistry<NativeDisplayEventReceiver> nativeObjRegistry =
      new NativeObjRegistry<>(NativeDisplayEventReceiver.class);

  @RealObject protected DisplayEventReceiver realReceiver;
  @ReflectorObject private DisplayEventReceiverReflector displayEventReceiverReflector;

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
    CloseGuard closeGuard = displayEventReceiverReflector.getCloseGuard();
    // Suppresses noisy CloseGuard warning
    if (closeGuard != null) {
      closeGuard.close();
    }
    displayEventReceiverReflector.dispose(finalized);
  }

  protected void onVsync() {
    if (RuntimeEnvironment.getApiLevel() <= JELLY_BEAN) {
      displayEventReceiverReflector.onVsync(ShadowSystem.nanoTime(), 1);
    } else if (RuntimeEnvironment.getApiLevel() < Q) {
      displayEventReceiverReflector.onVsync(
          ShadowSystem.nanoTime(), 0, /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */ 1);
    } else if (RuntimeEnvironment.getApiLevel() < S) {
      displayEventReceiverReflector.onVsync(
          ShadowSystem.nanoTime(), 0L, /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */ 1);
    } else if (RuntimeEnvironment.getApiLevel() < TIRAMISU) {
      try {
        // onVsync takes a package-private VSyncData class as a parameter, thus reflection
        // needs to be used
        Object vsyncData =
            ReflectionHelpers.callConstructor(
                Class.forName("android.view.DisplayEventReceiver$VsyncEventData"),
                ClassParameter.from(long.class, 1), /* id */
                ClassParameter.from(long.class, 10), /* frameDeadline */
                ClassParameter.from(long.class, 1)); /* frameInterval */

        displayEventReceiverReflector.onVsync(
            ShadowSystem.nanoTime(),
            0L, /* physicalDisplayId currently ignored */
            /* frame= */ 1,
            vsyncData /* VsyncEventData */);
      } catch (ClassNotFoundException e) {
        throw new LinkageError("Unable to construct VsyncEventData", e);
      }
    } else {
      displayEventReceiverReflector.onVsync(
          ShadowSystem.nanoTime(),
          0L, /* physicalDisplayId currently ignored */
          1, /* frame */
          newVsyncEventData() /* VsyncEventData */);
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
    private final ShadowPausedSystemClock.Listener clockListener = this::onClockAdvanced;

    @GuardedBy("this")
    private long nextVsyncTime = 0;

    public NativeDisplayEventReceiver(WeakReference<DisplayEventReceiver> receiverRef) {
      this.receiverRef = receiverRef;
      // register a clock listener for the async mode
      ShadowPausedSystemClock.addListener(clockListener);
    }

    private void onClockAdvanced() {
      synchronized (this) {
        if (nextVsyncTime == 0 || ShadowPausedSystemClock.uptimeMillis() < nextVsyncTime) {
          return;
        }
        nextVsyncTime = 0;
      }
      doVsync();
    }

    void dispose() {
      ShadowPausedSystemClock.removeListener(clockListener);
    }

    public void scheduleVsync() {
      Duration frameDelay = ShadowChoreographer.getFrameDelay();
      if (ShadowChoreographer.isPaused()) {
        synchronized (this) {
          nextVsyncTime = SystemClock.uptimeMillis() + frameDelay.toMillis();
        }
      } else {
        // simulate an immediate callback
        ShadowSystemClock.advanceBy(frameDelay);
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

  @Implementation(minSdk = TIRAMISU)
  protected Object getLatestVsyncEventData() {
    return newVsyncEventData();
  }

  private Object newVsyncEventData() {
    try {
      // onVsync on T takes a package-private VsyncEventData class, which is itself composed of a
      // package private VsyncEventData.FrameTimeline  class. So use reflection to build these up
      Class<?> frameTimelineClass =
          Class.forName("android.view.DisplayEventReceiver$VsyncEventData$FrameTimeline");
      Object timeline =
          ReflectionHelpers.callConstructor(
              frameTimelineClass,
              ClassParameter.from(long.class, 1) /* vsync id */,
              ClassParameter.from(long.class, 1) /* expectedPresentTime */,
              ClassParameter.from(long.class, 10) /* deadline */);

      Object timelineArray = Array.newInstance(frameTimelineClass, 1);
      Array.set(timelineArray, 0, timeline);

      // get FrameTimeline[].class
      Class<?> frameTimeLineArrayClass =
          Class.forName("[Landroid.view.DisplayEventReceiver$VsyncEventData$FrameTimeline;");
      return ReflectionHelpers.callConstructor(
          Class.forName("android.view.DisplayEventReceiver$VsyncEventData"),
          ClassParameter.from(frameTimeLineArrayClass, timelineArray),
          ClassParameter.from(int.class, 0), /* frameDeadline */
          ClassParameter.from(long.class, 1)); /* frameInterval */
    } catch (ClassNotFoundException e) {
      throw new LinkageError("Unable to construct VsyncEventData", e);
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
