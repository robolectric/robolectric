package org.robolectric.integrationtests.nativegraphics.testing.util;

import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import java.util.HashSet;
import java.util.Set;

public class DrawCountDown implements OnPreDrawListener {
  private static Set<DrawCountDown> pendingCallbacks = new HashSet<>();

  private int drawCount;
  private View targetView;
  private Runnable runnable;

  private DrawCountDown(View targetView, int countFrames, Runnable countReachedListener) {
    this.targetView = targetView;
    drawCount = countFrames;
    runnable = countReachedListener;
  }

  @Override
  public boolean onPreDraw() {
    if (drawCount <= 0) {
      synchronized (pendingCallbacks) {
        pendingCallbacks.remove(this);
      }
      targetView.getViewTreeObserver().removeOnPreDrawListener(this);
      runnable.run();
    } else {
      drawCount--;
      targetView.postInvalidate();
    }
    return true;
  }

  public static void countDownDraws(
      View targetView, int countFrames, Runnable onDrawCountReachedListener) {
    DrawCountDown counter = new DrawCountDown(targetView, countFrames, onDrawCountReachedListener);
    synchronized (pendingCallbacks) {
      pendingCallbacks.add(counter);
    }
    targetView.getViewTreeObserver().addOnPreDrawListener(counter);
  }

  public static void cancelPending() {
    synchronized (pendingCallbacks) {
      for (DrawCountDown counter : pendingCallbacks) {
        counter.targetView.getViewTreeObserver().removeOnPreDrawListener(counter);
      }
      pendingCallbacks.clear();
    }
  }
}
