package org.robolectric.simulator;

import android.app.UiAutomation;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.MotionEvent;
import androidx.test.platform.app.InstrumentationRegistry;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;

/** A {@link MouseHandler} that posts triggers {@link MotionEvent}. */
public class MouseHandler extends MouseAdapter {
  private final UiAutomation uiAutomation =
      InstrumentationRegistry.getInstrumentation().getUiAutomation();

  private boolean isPressed;
  private Duration androidSystemClockTimeDelta;
  private Instant downTime;

  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  public void mousePressed(MouseEvent mouseEvent) {
    if (shouldHandle(mouseEvent)) {
      isPressed = true;
      androidSystemClockTimeDelta =
          Duration.ofMillis(SystemClock.uptimeMillis()).minus(Duration.ofNanos(System.nanoTime()));
      downTime = Instant.ofEpochMilli(mouseEvent.getWhen());
      postMotionEvent(mouseEvent, MotionEvent.ACTION_DOWN);
    }
  }

  @Override
  public void mouseDragged(MouseEvent mouseEvent) {
    if (isPressed) {
      postMotionEvent(mouseEvent, MotionEvent.ACTION_MOVE);
    }
  }

  @Override
  public void mouseReleased(MouseEvent mouseEvent) {
    if (shouldHandle(mouseEvent)) {
      isPressed = false;
      postMotionEvent(mouseEvent, MotionEvent.ACTION_UP);
    }
  }

  private boolean shouldHandle(MouseEvent mouseEvent) {
    return !mouseEvent.isPopupTrigger() && mouseEvent.getButton() == MouseEvent.BUTTON1;
  }

  private void postMotionEvent(MouseEvent mouseEvent, int action) {
    MotionEvent androidEvent = obtainMotionEvent(mouseEvent, action);
    handler.post(() -> uiAutomation.injectInputEvent(androidEvent, true));
    mouseEvent.consume();
  }

  private MotionEvent obtainMotionEvent(MouseEvent mouseEvent, int action) {
    return MotionEvent.obtain(
        toAndroidTime(downTime),
        toAndroidTime(Instant.ofEpochMilli(mouseEvent.getWhen())),
        action,
        mouseEvent.getX(),
        mouseEvent.getY(),
        /* metaState= */ 0);
  }

  private long toAndroidTime(Instant instant) {
    return instant.minus(androidSystemClockTimeDelta).toEpochMilli();
  }
}
