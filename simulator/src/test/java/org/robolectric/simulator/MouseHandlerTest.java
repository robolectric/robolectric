package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class MouseHandlerTest {
  private MouseHandler mouseHandler;
  private TestView testView;

  @Before
  public void setUp() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    testView = new TestView(activity);
    activity.setContentView(testView);
    testView.layout(0, 0, 1000, 1000);
    mouseHandler = new MouseHandler();
  }

  @Test
  public void mousePressed_dispatchesTouchEvent() {
    MouseEvent awtEvent =
        new MouseEvent(
            new DummyComponent(),
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            MouseEvent.BUTTON1_DOWN_MASK,
            10,
            100,
            1,
            false,
            MouseEvent.BUTTON1);

    mouseHandler.mousePressed(awtEvent);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(testView.events).hasSize(1);
    MotionEvent event = testView.events.get(0);
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_DOWN);
    assertThat(event.getSource()).isEqualTo(InputDevice.SOURCE_TOUCHSCREEN);
    // Mainly check that the event was dispatched and the coordinates were set.
    assertThat(event.getX()).isWithin(5f).of(10f);
  }

  @Test
  public void mouseReleased_dispatchesTouchEvent() {
    // Need a pressed event first
    MouseEvent pressEvent =
        new MouseEvent(
            new DummyComponent(),
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            MouseEvent.BUTTON1_DOWN_MASK,
            10,
            100,
            1,
            false,
            MouseEvent.BUTTON1);
    mouseHandler.mousePressed(pressEvent);
    shadowOf(Looper.getMainLooper()).idle();
    testView.events.clear();

    MouseEvent releaseEvent =
        new MouseEvent(
            new DummyComponent(),
            MouseEvent.MOUSE_RELEASED,
            System.currentTimeMillis(),
            0,
            10,
            100,
            1,
            false,
            MouseEvent.BUTTON1);

    mouseHandler.mouseReleased(releaseEvent);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(testView.events).hasSize(1);
    MotionEvent event = testView.events.get(0);
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_UP);
    assertThat(event.getSource()).isEqualTo(InputDevice.SOURCE_TOUCHSCREEN);
  }

  @Test
  public void mouseDragged_dispatchesTouchEvent() {
    MouseEvent pressEvent =
        new MouseEvent(
            new DummyComponent(),
            MouseEvent.MOUSE_PRESSED,
            System.currentTimeMillis(),
            MouseEvent.BUTTON1_DOWN_MASK,
            10,
            100,
            1,
            false,
            MouseEvent.BUTTON1);
    mouseHandler.mousePressed(pressEvent);
    shadowOf(Looper.getMainLooper()).idle();
    testView.events.clear();

    MouseEvent dragEvent =
        new MouseEvent(
            new DummyComponent(),
            MouseEvent.MOUSE_DRAGGED,
            System.currentTimeMillis(),
            MouseEvent.BUTTON1_DOWN_MASK,
            15,
            105,
            1,
            false,
            MouseEvent.BUTTON1);

    mouseHandler.mouseDragged(dragEvent);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(testView.events).hasSize(1);
    MotionEvent event = testView.events.get(0);
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_MOVE);
    assertThat(event.getSource()).isEqualTo(InputDevice.SOURCE_TOUCHSCREEN);
  }

  @Test
  public void mouseWheelMoved_dispatchesGenericMotionEvent() {
    MouseWheelEvent wheelEvent =
        new MouseWheelEvent(
            new DummyComponent(),
            MouseEvent.MOUSE_WHEEL,
            System.currentTimeMillis(),
            0,
            50,
            100,
            0,
            false,
            MouseWheelEvent.WHEEL_UNIT_SCROLL,
            3,
            1);

    mouseHandler.mouseWheelMoved(wheelEvent);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(testView.events).hasSize(1);
    MotionEvent event = testView.events.get(0);
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_SCROLL);
    assertThat(event.getSource()).isEqualTo(InputDevice.SOURCE_MOUSE);
  }

  static class TestView extends View {
    final List<MotionEvent> events = new ArrayList<>();

    public TestView(Context context) {
      super(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      events.add(MotionEvent.obtain(event));
      return true;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
      events.add(MotionEvent.obtain(event));
      return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      setMeasuredDimension(1000, 1000);
    }
  }

  private static class DummyComponent extends Component {}
}
