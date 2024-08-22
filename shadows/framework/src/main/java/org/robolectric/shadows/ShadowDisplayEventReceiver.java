package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.MessageQueue;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.DisplayEventReceiver;
import dalvik.system.CloseGuard;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.time.Duration;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.ReflectorObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.WithType;
import org.robolectric.versioning.AndroidVersions.U;

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

  @Implementation(maxSdk = LOLLIPOP_MR1)
  protected static long nativeInit(DisplayEventReceiver receiver, MessageQueue msgQueue) {
    return nativeObjRegistry.register(
        new NativeDisplayEventReceiver(new WeakReference<>(receiver)));
  }

  @Implementation(minSdk = R, maxSdk = TIRAMISU)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver,
      MessageQueue msgQueue,
      int vsyncSource,
      int configChanged) {
    return nativeInit(receiver, msgQueue);
  }

  @Implementation(minSdk = U.SDK_INT)
  protected static long nativeInit(
      WeakReference<DisplayEventReceiver> receiver,
      WeakReference<Object> vsyncEventData,
      MessageQueue msgQueue,
      int vsyncSource,
      int eventRegistration,
      long layerHandle) {
    return nativeInit(receiver, msgQueue);
  }

  @Implementation(maxSdk = TIRAMISU)
  protected static void nativeDispose(long receiverPtr) {
    NativeDisplayEventReceiver receiver = nativeObjRegistry.unregister(receiverPtr);
    if (receiver != null) {
      receiver.dispose();
    }
  }

  @Implementation
  protected static void nativeScheduleVsync(long receiverPtr) {
    nativeObjRegistry.getNativeObject(receiverPtr).scheduleVsync();
  }

  @Implementation(maxSdk = R)
  protected void dispose(boolean finalized) {
    CloseGuard closeGuard = displayEventReceiverReflector.getCloseGuard();
    // Suppresses noisy CloseGuard warning
    if (closeGuard != null) {
      closeGuard.close();
    }
    displayEventReceiverReflector.dispose(finalized);
  }

  protected void onVsync() {
    if (RuntimeEnvironment.getApiLevel() < Q) {
      displayEventReceiverReflector.onVsync(
          ShadowSystem.nanoTime(), 0, /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */ 1);
    } else if (RuntimeEnvironment.getApiLevel() < S) {
      displayEventReceiverReflector.onVsync(
          ShadowSystem.nanoTime(), 0L, /* SurfaceControl.BUILT_IN_DISPLAY_ID_MAIN */ 1);
    } else if (RuntimeEnvironment.getApiLevel() < TIRAMISU) {
      displayEventReceiverReflector.onVsync(
          ShadowSystem.nanoTime(),
          0L, /* physicalDisplayId currently ignored */
          /* frame= */ 1,
          newVsyncEventData() /* VsyncEventData */);
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

    public NativeDisplayEventReceiver(WeakReference<DisplayEventReceiver> receiverRef) {
      this.receiverRef = receiverRef;
      // register a clock listener for the async mode
      ShadowPausedSystemClock.addListener(clockListener);
    }

    private void onClockAdvanced() {
      synchronized (this) {
        long nextVsyncTime = ShadowChoreographer.getNextVsyncTime();
        if (nextVsyncTime == 0 || ShadowPausedSystemClock.uptimeMillis() < nextVsyncTime) {
          return;
        }
        ShadowChoreographer.setNextVsyncTime(0);
      }

      doVsync();
    }

    void dispose() {
      ShadowPausedSystemClock.removeListener(clockListener);
    }

    public void scheduleVsync() {
      Duration frameDelay = ShadowChoreographer.getFrameDelay();
      if (ShadowChoreographer.isPaused()) {
        if (ShadowChoreographer.getNextVsyncTime() < SystemClock.uptimeMillis()) {
          ShadowChoreographer.setNextVsyncTime(SystemClock.uptimeMillis() + frameDelay.toMillis());
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

  private static Object /* VsyncEventData */ newVsyncEventData() {
    VsyncEventDataReflector vsyncEventDataReflector = reflector(VsyncEventDataReflector.class);
    if (RuntimeEnvironment.getApiLevel() < TIRAMISU) {
      return vsyncEventDataReflector.newVsyncEventData(
          /* id= */ 1, /* frameDeadline= */ 10, /* frameInterval= */ 1);
    }
    try {
      // onVsync on T takes a package-private VsyncEventData class, which is itself composed of a
      // package private VsyncEventData.FrameTimeline  class. So use reflection to build these up
      Class<?> frameTimelineClass =
          Class.forName("android.view.DisplayEventReceiver$VsyncEventData$FrameTimeline");

      int timelineArrayLength = RuntimeEnvironment.getApiLevel() == TIRAMISU ? 1 : 7;
      FrameTimelineReflector frameTimelineReflector = reflector(FrameTimelineReflector.class);
      Object timelineArray = Array.newInstance(frameTimelineClass, timelineArrayLength);
      for (int i = 0; i < timelineArrayLength; i++) {
        Array.set(timelineArray, i, frameTimelineReflector.newFrameTimeline(1, 1, 10));
      }
      if (RuntimeEnvironment.getApiLevel() <= TIRAMISU) {
        return vsyncEventDataReflector.newVsyncEventData(
            timelineArray, /* preferredFrameTimelineIndex= */ 0, /* frameInterval= */ 1);
      } else {
        return vsyncEventDataReflector.newVsyncEventData(
            timelineArray,
            /* preferredFrameTimelineIndex= */ 0,
            timelineArrayLength,
            /* frameInterval= */ 1);
      }
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

    @Accessor("mReceiverPtr")
    long getReceiverPtr();
  }

  @ForType(className = "android.view.DisplayEventReceiver$VsyncEventData")
  interface VsyncEventDataReflector {
    @Constructor
    Object newVsyncEventData(long id, long frameDeadline, long frameInterval);

    @Constructor
    Object newVsyncEventData(
        @WithType("[Landroid.view.DisplayEventReceiver$VsyncEventData$FrameTimeline;")
            Object frameTimelineArray,
        int preferredFrameTimelineIndex,
        long frameInterval);

    @Constructor
    Object newVsyncEventData(
        @WithType("[Landroid.view.DisplayEventReceiver$VsyncEventData$FrameTimeline;")
            Object frameTimelineArray,
        int preferredFrameTimelineIndex,
        int timelineArrayLength,
        long frameInterval);
  }

  @ForType(className = "android.view.DisplayEventReceiver$VsyncEventData$FrameTimeline")
  interface FrameTimelineReflector {
    @Constructor
    Object newFrameTimeline(long id, long expectedPresentTime, long deadline);
  }
}
