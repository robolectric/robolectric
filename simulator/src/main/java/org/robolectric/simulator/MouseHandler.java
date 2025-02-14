package org.robolectric.simulator;

import android.app.UiAutomation;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.test.platform.app.InstrumentationRegistry;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.Instant;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import org.robolectric.shadows.ShadowUiAutomation;

/** A {@link MouseHandler} that posts triggers {@link MotionEvent}. */
public class MouseHandler extends MouseAdapter {
  private final UiAutomation uiAutomation =
      InstrumentationRegistry.getInstrumentation().getUiAutomation();

  private boolean isPressed;
  private Duration androidSystemClockTimeDelta;
  private Instant downTime;

  private final Handler handler = new Handler(Looper.getMainLooper());

  private final JPopupMenu rightClickMenu;

  public MouseHandler() {
    rightClickMenu = new JPopupMenu();
    JMenuItem sendBackEvent = new JMenuItem("Press back");
    sendBackEvent.addActionListener(
        e -> {
          handler.post(
              () -> {
                long eventTime = SystemClock.uptimeMillis();
                KeyEvent backKeyDown =
                    new KeyEvent(
                        eventTime,
                        eventTime,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_BACK,
                        /* repeat= */ 0,
                        0);
                ShadowUiAutomation.injectInputEvent(backKeyDown);

                KeyEvent backKeyUp =
                    new KeyEvent(
                        eventTime,
                        eventTime,
                        KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_BACK,
                        /* repeat= */ 0,
                        0);
                ShadowUiAutomation.injectInputEvent(backKeyUp);
              });
        });
    rightClickMenu.add(sendBackEvent);
  }

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
    } else if (SwingUtilities.isRightMouseButton(mouseEvent)) {
      rightClickMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
    }
  }

  private boolean shouldHandle(MouseEvent mouseEvent) {
    return !mouseEvent.isPopupTrigger() && SwingUtilities.isLeftMouseButton(mouseEvent);
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
