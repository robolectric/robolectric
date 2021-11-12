package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import androidx.test.platform.ui.InjectEventSecurityException;
import androidx.test.platform.ui.UiController;
import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/** A {@link UiController} that runs on a local JVM with Robolectric. */
public class LocalUiController implements UiController {

  private static final String TAG = "LocalUiController";

  @Override
  public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == Looper.getMainLooper(), "Expecting to be on main thread!");
    loopMainThreadUntilIdle();

    getViewRoot().dispatchTouchEvent(event);

    loopMainThreadUntilIdle();

    return true;
  }

  @Override
  public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == Looper.getMainLooper(), "Expecting to be on main thread!");

    loopMainThreadUntilIdle();
    getViewRoot().dispatchKeyEvent(event);

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
    shadowMainLooper().idle();
  }

  @Override
  @SuppressWarnings("AndroidJdkLibsChecker")
  public void loopMainThreadForAtLeast(long millisDelay) {
    shadowMainLooper().idleFor(Duration.ofMillis(millisDelay));
  }

  private View getViewRoot() {
    List<ViewRootImpl> viewRoots = getViewRoots();
    if (viewRoots.isEmpty()) {
      throw new IllegalStateException("no view roots!");
    }
    List<LayoutParams> params = getRootLayoutParams();
    if (params.size() != viewRoots.size()) {
      throw new IllegalStateException("number params is not consistent with number of view roots!");
    }

    int topMostRootIndex = 0;
    for (int i = 0; i < params.size(); i++) {
      LayoutParams param = params.get(i);
      if (param.type > params.get(topMostRootIndex).type) {
        topMostRootIndex = i;
      }
    }
    return viewRoots.get(topMostRootIndex).getView();
  }

  private static List<ViewRootImpl> getViewRoots() {
    Object windowManager = getViewRootsContainer();
    Object viewRootsObj = ReflectionHelpers.getField(windowManager, "mRoots");
    Class<?> viewRootsClass = viewRootsObj.getClass();
    if (ViewRootImpl[].class.isAssignableFrom(viewRootsClass)) {
      return Arrays.asList((ViewRootImpl[]) viewRootsObj);
    } else if (List.class.isAssignableFrom(viewRootsClass)) {
      return (List<ViewRootImpl>) viewRootsObj;
    } else {
      throw new IllegalStateException(
          "WindowManager.mRoots is an unknown type " + viewRootsClass.getName());
    }
  }

  private static List<LayoutParams> getRootLayoutParams() {
    Object windowManager = getViewRootsContainer();
    Object paramsObj = ReflectionHelpers.getField(windowManager, "mParams");
    Class<?> paramsClass = paramsObj.getClass();
    if (LayoutParams[].class.isAssignableFrom(paramsClass)) {
      return Arrays.asList((LayoutParams[]) paramsObj);
    } else if (List.class.isAssignableFrom(paramsClass)) {
      return (List<LayoutParams>) paramsObj;
    } else {
      throw new IllegalStateException(
          "WindowManager.mParams is an unknown type " + paramsClass.getName());
    }
  }

  private static Object getViewRootsContainer() {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN) {
      return ReflectionHelpers.callStaticMethod(WindowManagerImpl.class, "getDefault");
    } else {
      return WindowManagerGlobal.getInstance();
    }
  }
}
