package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.platform.ui.InjectEventSecurityException;
import androidx.test.platform.ui.UiController;
import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowPausedLooper;
import org.robolectric.shadows.ShadowUiAutomation;

/** A {@link UiController} that runs on a local JVM with Robolectric. */
public class LocalUiController implements UiController {

  private static final String TAG = "LocalUiController";

  private static long idlingResourceErrorTimeoutMs = SECONDS.toMillis(26);
  private final HashSet<IdlingResourceProxyImpl> syncedIdlingResources = new HashSet<>();
  private final ExecutorService looperIdlingExecutor = Executors.newCachedThreadPool();

  /**
   * Sets the error timeout for idling resources.
   *
   * <p>See {@link androidx.test.espresso.IdlingPolicies#setIdlingResourceTimeout(long, TimeUnit)}.
   *
   * <p>Note: This API may be removed in the future in favor of using IdlingPolicies directly.
   */
  @Beta
  public static void setIdlingResourceTimeout(long timeout, TimeUnit unit) {
    idlingResourceErrorTimeoutMs = unit.toMillis(timeout);
  }

  @Override
  public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
    loopMainThreadUntilIdle();
    ShadowUiAutomation.injectInputEvent(event);
    loopMainThreadUntilIdle();
    return true;
  }

  @Override
  public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
    loopMainThreadUntilIdle();
    ShadowUiAutomation.injectInputEvent(event);
    loopMainThreadUntilIdle();
    return true;
  }

  // TODO: implementation copied from espresso's UIControllerImpl. Refactor code into common
  // location
  @Override
  public boolean injectString(String str) throws InjectEventSecurityException {
    checkNotNull(str);
    checkState(Looper.myLooper() == Looper.getMainLooper(), "Expecting to be on main thread!");

    // No-op if string is empty.
    if (str.isEmpty()) {
      Log.w(TAG, "Supplied string is empty resulting in no-op (nothing is typed).");
      return true;
    }

    boolean eventInjected = false;
    KeyCharacterMap keyCharacterMap = getKeyCharacterMap();

    // TODO: Investigate why not use (as suggested in javadoc of keyCharacterMap.getEvents):
    // http://developer.android.com/reference/android/view/KeyEvent.html#KeyEvent(long,
    // java.lang.String, int, int)
    KeyEvent[] events = keyCharacterMap.getEvents(str.toCharArray());
    if (events == null) {
      throw new RuntimeException(
          String.format(
              "Failed to get key events for string %s (i.e. current IME does not understand how to"
                  + " translate the string into key events). As a workaround, you can use"
                  + " replaceText action to set the text directly in the EditText field.",
              str));
    }

    Log.d(TAG, String.format("Injecting string: \"%s\"", str));

    for (KeyEvent event : events) {
      checkNotNull(
          event,
          String.format(
              "Failed to get event for character (%c) with key code (%s)",
              event.getKeyCode(), event.getUnicodeChar()));

      eventInjected = false;
      for (int attempts = 0; !eventInjected && attempts < 4; attempts++) {
        // We have to change the time of an event before injecting it because
        // all KeyEvents returned by KeyCharacterMap.getEvents() have the same
        // time stamp and the system rejects too old events. Hence, it is
        // possible for an event to become stale before it is injected if it
        // takes too long to inject the preceding ones.
        event = KeyEvent.changeTimeRepeat(event, SystemClock.uptimeMillis(), 0);
        eventInjected = injectKeyEvent(event);
      }

      if (!eventInjected) {
        Log.e(
            TAG,
            String.format(
                "Failed to inject event for character (%c) with key code (%s)",
                event.getUnicodeChar(), event.getKeyCode()));
        break;
      }
    }

    return eventInjected;
  }

  @SuppressLint("InlinedApi")
  @VisibleForTesting
  @SuppressWarnings("deprecation")
  static KeyCharacterMap getKeyCharacterMap() {
    KeyCharacterMap keyCharacterMap = null;

    // KeyCharacterMap.VIRTUAL_KEYBOARD is present from API11.
    // For earlier APIs we use KeyCharacterMap.BUILT_IN_KEYBOARD
    if (Build.VERSION.SDK_INT < 11) {
      keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
    } else {
      keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
    }
    return keyCharacterMap;
  }

  @Override
  public void loopMainThreadUntilIdle() {
    if (!ShadowLooper.looperMode().equals(LooperMode.Mode.PAUSED)) {
      shadowMainLooper().idle();
    } else {
      ImmutableSet<IdlingResourceProxy> idlingResources = syncIdlingResources();
      if (idlingResources.isEmpty()) {
        shadowMainLooper().idle();
      } else {
        loopMainThreadUntilIdlingResourcesIdle(idlingResources);
      }
    }
  }

  private void loopMainThreadUntilIdlingResourcesIdle(
      ImmutableSet<IdlingResourceProxy> idlingResources) {
    Looper mainLooper = Looper.myLooper();
    ShadowPausedLooper shadowMainLooper = Shadow.extract(mainLooper);
    Handler handler = new Handler(mainLooper);
    Set<IdlingResourceProxy> activeResources = new HashSet<>();
    long startTimeNanos = System.nanoTime();

    shadowMainLooper.idle();
    while (true) {
      // Gather the list of resources that are not idling.
      for (IdlingResourceProxy resource : idlingResources) {
        // Add the resource as active and check if it's idle, if it is already is will be removed
        // synchronously. The idle callback is synchronized in the resource which avoids a race
        // between registering the idle callback and checking the idle state.
        activeResources.add(resource);
        resource.notifyOnIdle(
            () -> {
              if (Looper.myLooper() == mainLooper) {
                activeResources.remove(resource);
              } else {
                // Post to restart the main thread.
                handler.post(() -> activeResources.remove(resource));
              }
            });
      }
      // If all are idle then just return, we're done.
      if (activeResources.isEmpty()) {
        break;
      }
      // While the resources that weren't idle haven't transitioned to idle continue to loop the
      // main looper waiting for any new messages. Once all resources have transitioned to idle loop
      // around again to make sure all resources are idle at the same time.
      while (!activeResources.isEmpty()) {
        long elapsedTimeMs = NANOSECONDS.toMillis(System.nanoTime() - startTimeNanos);
        if (elapsedTimeMs >= idlingResourceErrorTimeoutMs) {
          throw new IdlingResourceTimeoutException(idlingResourceNames(activeResources));
        }
        // Poll the queue and suspend the thread until we get new messages or the idle transition.
        shadowMainLooper.poll(idlingResourceErrorTimeoutMs - elapsedTimeMs);
        shadowMainLooper.idle();
      }
    }
  }

  private ImmutableSet<IdlingResourceProxy> syncIdlingResources() {
    // Collect unique registered idling resources.
    HashMap<String, IdlingResource> registeredResourceByName = new HashMap<>();
    for (IdlingResource resource : IdlingRegistry.getInstance().getResources()) {
      String name = resource.getName();
      if (registeredResourceByName.containsKey(name)) {
        logDuplicate(name, registeredResourceByName.get(name), resource);
      } else {
        registeredResourceByName.put(name, resource);
      }
    }
    Iterator<IdlingResourceProxyImpl> iterator = syncedIdlingResources.iterator();
    while (iterator.hasNext()) {
      IdlingResourceProxyImpl proxy = iterator.next();
      if (registeredResourceByName.get(proxy.name) == proxy.resource) {
        // Already registered, don't need to add.
        registeredResourceByName.remove(proxy.name);
      } else {
        // Previously registered, but no longer registered, remove.
        iterator.remove();
      }
    }
    // Add new idling resources that weren't previously registered.
    for (Map.Entry<String, IdlingResource> entry : registeredResourceByName.entrySet()) {
      syncedIdlingResources.add(new IdlingResourceProxyImpl(entry.getKey(), entry.getValue()));
    }

    return ImmutableSet.<IdlingResourceProxy>builder()
        .addAll(syncedIdlingResources)
        .addAll(
            IdlingRegistry.getInstance().getLoopers().stream()
                .map(LooperIdlingResource::new)
                .iterator())
        .build();
  }

  private static void logDuplicate(String name, IdlingResource a, IdlingResource b) {
    Log.e(
        TAG,
        String.format(
            "Attempted to register resource with same names:"
                + " %s. R1: %s R2: %s.\nDuplicate resource registration will be ignored.",
            name, a, b));
  }

  private static List<String> idlingResourceNames(Set<IdlingResourceProxy> idlingResources) {
    return idlingResources.stream().map(IdlingResourceProxy::getName).collect(toList());
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public void loopMainThreadForAtLeast(long millisDelay) {
    shadowMainLooper().idleFor(Duration.ofMillis(millisDelay));
  }

  private interface IdlingResourceProxy {
    String getName();

    void notifyOnIdle(Runnable idleCallback);
  }

  private static final class IdlingResourceProxyImpl implements IdlingResourceProxy {
    private final String name;
    private final IdlingResource resource;

    private Runnable idleCallback;

    IdlingResourceProxyImpl(String name, IdlingResource resource) {
      this.name = name;
      this.resource = resource;
      resource.registerIdleTransitionCallback(this::onIdle);
    }

    @Override
    public String getName() {
      return this.name;
    }

    @Override
    public synchronized void notifyOnIdle(Runnable idleCallback) {
      if (resource.isIdleNow()) {
        this.idleCallback = null;
        idleCallback.run();
      } else {
        this.idleCallback = idleCallback;
      }
    }

    private synchronized void onIdle() {
      if (idleCallback != null) {
        idleCallback.run();
        idleCallback = null;
      }
    }
  }

  private final class LooperIdlingResource implements IdlingResourceProxy {
    private final Looper looper;
    private final ShadowLooper shadowLooper;
    private Runnable idleCallback;

    LooperIdlingResource(Looper looper) {
      this.looper = looper;
      this.shadowLooper = shadowOf(looper);
    }

    @Override
    public String getName() {
      return looper.toString();
    }

    @Override
    public synchronized void notifyOnIdle(Runnable idleCallback) {
      if (shadowLooper.isIdle()) {
        this.idleCallback = null;
        idleCallback.run();
      } else {
        this.idleCallback = idleCallback;
        // Note idle() doesn't throw an exception if called from another thread, the looper would
        // die with an unhandled exception.
        // TODO(paulsowden): It's not technically necessary to idle the looper from another thread,
        //  it can be idled from its own thread, however we'll need API access to do this and
        //  observe the idle state--the idle() api blocks the calling thread by default. Perhaps a
        //  ListenableFuture idleAsync() variant?
        looperIdlingExecutor.execute(this::idleLooper);
      }
    }

    private void idleLooper() {
      shadowLooper.idle();
      synchronized (this) {
        if (idleCallback != null) {
          idleCallback.run();
          idleCallback = null;
        }
      }
    }
  }
}
