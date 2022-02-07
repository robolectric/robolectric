package org.robolectric.android.internal;

import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
import static android.view.WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
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
import android.view.ViewRootImpl;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import androidx.test.platform.ui.InjectEventSecurityException;
import androidx.test.platform.ui.UiController;
import com.google.common.annotations.VisibleForTesting;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

/** A {@link UiController} that runs on a local JVM with Robolectric. */
public class LocalUiController implements UiController {

  private static final String TAG = "LocalUiController";

  private static final Predicate<Root> IS_FOCUSABLE = hasLayoutFlag(FLAG_NOT_FOCUSABLE).negate();
  private static final Predicate<Root> IS_TOUCHABLE = hasLayoutFlag(FLAG_NOT_TOUCHABLE).negate();
  private static final Predicate<Root> IS_TOUCH_MODAL =
      IS_FOCUSABLE.and(hasLayoutFlag(FLAG_NOT_TOUCH_MODAL).negate());
  private static final Predicate<Root> WATCH_TOUCH_OUTSIDE =
      IS_TOUCH_MODAL.negate().and(hasLayoutFlag(FLAG_WATCH_OUTSIDE_TOUCH));

  @Override
  public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == Looper.getMainLooper(), "Expecting to be on main thread!");
    loopMainThreadUntilIdle();

    // TODO(paulsowden): The real implementation will send a full event stream (a touch down
    //  followed by a series of moves, etc) to the same window/root even if the subsequent events
    //  leave the window bounds, and will split pointer down events based on the window flags.
    //  This will be necessary to support more sophisticated multi-window use cases.

    List<Root> touchableRoots = getViewRoots().stream().filter(IS_TOUCHABLE).collect(toList());
    for (int i = 0; i < touchableRoots.size(); i++) {
      Root root = touchableRoots.get(i);
      if (i == touchableRoots.size() - 1 || root.isTouchModal() || root.isTouchInside(event)) {
        event.offsetLocation(-root.params.x, -root.params.y);
        root.impl.getView().dispatchTouchEvent(event);
        event.offsetLocation(root.params.x, root.params.y);
        break;
      } else if (event.getActionMasked() == MotionEvent.ACTION_DOWN && root.watchTouchOutside()) {
        MotionEvent outsideEvent = MotionEvent.obtain(event);
        outsideEvent.setAction(MotionEvent.ACTION_OUTSIDE);
        outsideEvent.offsetLocation(-root.params.x, -root.params.y);
        root.impl.getView().dispatchTouchEvent(outsideEvent);
        outsideEvent.recycle();
      }
    }

    loopMainThreadUntilIdle();

    return true;
  }

  @Override
  public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
    checkNotNull(event);
    checkState(Looper.myLooper() == Looper.getMainLooper(), "Expecting to be on main thread!");
    loopMainThreadUntilIdle();

    getViewRoots().stream()
        .filter(IS_FOCUSABLE)
        .findFirst()
        .ifPresent(root -> root.impl.getView().dispatchKeyEvent(event));

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

  private ArrayList<Root> getViewRoots() {
    List<ViewRootImpl> viewRootImpls = getViewRootImpls();
    if (viewRootImpls.isEmpty()) {
      throw new IllegalStateException("no view roots!");
    }
    List<LayoutParams> params = getRootLayoutParams();
    if (params.size() != viewRootImpls.size()) {
      throw new IllegalStateException("number params is not consistent with number of view roots!");
    }
    ArrayList<Root> roots = new ArrayList<>();
    for (int i = 0; i < viewRootImpls.size(); i++) {
      roots.add(new Root(viewRootImpls.get(i), params.get(i), i));
    }
    roots.sort(comparingInt(Root::getType).reversed().thenComparingInt(Root::getIndex));
    return roots;
  }

  private static List<ViewRootImpl> getViewRootImpls() {
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

  private static Predicate<Root> hasLayoutFlag(int flag) {
    return root -> (root.params.flags & flag) == flag;
  }

  private static final class Root {
    final ViewRootImpl impl;
    final LayoutParams params;
    final int index;

    Root(ViewRootImpl impl, LayoutParams params, int index) {
      this.impl = impl;
      this.params = params;
      this.index = index;
    }

    int getIndex() {
      return index;
    }

    int getType() {
      return params.type;
    }

    boolean isTouchInside(MotionEvent event) {
      int index = event.getActionIndex();
      return event.getX(index) >= params.x
          && event.getX(index) <= params.x + impl.getView().getWidth()
          && event.getY(index) >= params.y
          && event.getY(index) <= params.y + impl.getView().getHeight();
    }

    boolean isTouchModal() {
      return IS_TOUCH_MODAL.test(this);
    }

    boolean watchTouchOutside() {
      return WATCH_TOUCH_OUTSIDE.test(this);
    }
  }
}
