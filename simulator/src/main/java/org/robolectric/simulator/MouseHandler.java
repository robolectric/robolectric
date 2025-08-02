package org.robolectric.simulator;

import android.app.UiAutomation;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import androidx.test.platform.app.InstrumentationRegistry;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.robolectric.shadows.ShadowUiAutomation;
import org.robolectric.simulator.pluginapi.MenuCustomizer;
import org.robolectric.util.inject.Injector;

/** A {@link MouseAdapter} that triggers Android {@link MotionEvent}s when the mouse is pressed. */
public class MouseHandler extends MouseAdapter {
  private final UiAutomation uiAutomation =
      InstrumentationRegistry.getInstrumentation().getUiAutomation();

  private boolean isPressed;
  private Instant downTime;

  private final Handler handler = new Handler(Looper.getMainLooper());

  private final JPopupMenu rightClickMenu;
  private Component previouslyFocusedComponent;

  public MouseHandler() {
    rightClickMenu = new JPopupMenu();

    rightClickMenu.addPopupMenuListener(
        new PopupMenuListener() {
          @Override
          public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            previouslyFocusedComponent =
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
          }

          @Override
          public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            restoreFocus();
          }

          @Override
          public void popupMenuCanceled(PopupMenuEvent e) {
            restoreFocus();
          }

          private void restoreFocus() {
            SwingUtilities.invokeLater(
                () -> {
                  previouslyFocusedComponent.requestFocusInWindow();
                });
          }
        });

    rightClickMenu.add(getBackMenuItem());

    // Allow plugins to customize the right click menu.
    Injector injector = new Injector.Builder(Looper.class.getClassLoader()).build();
    MenuCustomizer menuCustomizer = injector.getInstance(MenuCustomizer.class);
    menuCustomizer.customizePopupMenu(rightClickMenu);
  }

  private JMenuItem getBackMenuItem() {
    JMenuItem sendBackMenuItem = new JMenuItem("Press back");
    sendBackMenuItem.addActionListener(
        e ->
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
                }));

    return sendBackMenuItem;
  }

  @Override
  public void mousePressed(MouseEvent mouseEvent) {
    if (shouldHandle(mouseEvent)) {
      isPressed = true;
      downTime = Instant.ofEpochMilli(SystemClock.uptimeMillis());
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
        downTime.toEpochMilli(),
        SystemClock.uptimeMillis(),
        action,
        mouseEvent.getX(),
        mouseEvent.getY(),
        /* metaState= */ 0);
  }
}
