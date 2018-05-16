package org.robolectric.android.internal;

import android.os.Build.VERSION_CODES;
import androidx.test.platform.ui.InjectEventSecurityException;
import androidx.test.platform.ui.UiController;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerImpl;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.ReflectionHelpers;

/** Custom implementation of {@link UiController} for Robolectric. */
public class RoboUiController implements UiController {

  @Override
  public boolean injectMotionEvent(MotionEvent event) throws InjectEventSecurityException {
    loopMainThreadUntilIdle();

    Preconditions.checkArgument(getViewRoots().size() == 1);
    // is it valid/proper to inject touch event for each view root?
    for (ViewRootImpl viewRoot : getViewRoots()) {
      viewRoot.getView().dispatchTouchEvent(event);
    }

    return true;
  }

  @Override
  public boolean injectKeyEvent(KeyEvent event) throws InjectEventSecurityException {
    throw new UnsupportedOperationException("injectKeyEvent not supported, yet");
  }

  @Override
  public boolean injectString(String str) throws InjectEventSecurityException {
    throw new UnsupportedOperationException("injectString not supported, yet");
  }

  @Override
  public void loopMainThreadUntilIdle() {
    ShadowLooper.getShadowMainLooper().idle();
  }

  @Override
  public void loopMainThreadForAtLeast(long millisDelay) {
    ShadowLooper.getShadowMainLooper().idle(millisDelay, TimeUnit.MILLISECONDS);
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

  private static Object getViewRootsContainer() {
    if (RuntimeEnvironment.getApiLevel() <= VERSION_CODES.JELLY_BEAN) {
      return ReflectionHelpers.callStaticMethod(WindowManagerImpl.class, "getDefault");
    } else {
      return WindowManagerGlobal.getInstance();
    }
  }
}
