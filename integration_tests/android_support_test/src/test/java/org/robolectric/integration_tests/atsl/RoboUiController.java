package org.robolectric.integration_tests.atsl;

import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import android.view.ViewRootImpl;
import android.view.WindowManagerGlobal;

public class RoboUiController implements android.support.test.espresso.UiController {

  @Override
  public boolean injectMotionEvent(MotionEvent event) throws android.support.test.espresso.InjectEventSecurityException {
    loopMainThreadUntilIdle();

    WindowManagerGlobal wmGlobal = WindowManagerGlobal.getInstance();
    ArrayList<ViewRootImpl> viewRoots = org.robolectric.util.ReflectionHelpers
        .getField(wmGlobal, "mRoots");

    if (viewRoots.size() != 1) {
      org.robolectric.util.Logger.warn("Unexpected # of view roots!: " + viewRoots.size());
      return false;
    }
    ViewRootImpl viewRoot = viewRoots.get(0);
    viewRoot.getView().dispatchTouchEvent(event);
    return true;
  }

  @Override
  public boolean injectKeyEvent(KeyEvent event) throws android.support.test.espresso.InjectEventSecurityException {


    return true;
  }

  @Override
  public boolean injectString(String str) throws android.support.test.espresso.InjectEventSecurityException {
    return true;
  }

  @Override
  public void loopMainThreadUntilIdle() {
    org.robolectric.shadows.ShadowApplication.getInstance().getForegroundThreadScheduler().advanceToLastPostedRunnable();
  }

  @Override
  public void loopMainThreadForAtLeast(long millisDelay) {
    org.robolectric.shadows.ShadowApplication.getInstance().getForegroundThreadScheduler().advanceBy(millisDelay,
        TimeUnit.MILLISECONDS);
  }
}

