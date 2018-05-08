package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.SystemClock;
import android.view.MotionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link MotionEventBuilder}. */
@RunWith(RobolectricTestRunner.class)
public final class MotionEventBuilderTest {

  @Test
  public void emptyBuilder() throws Exception {
    MotionEvent event = new MotionEventBuilder().build();
    assertThat(event.getDownTime()).isEqualTo(0);
    assertThat(event.getEventTime()).isEqualTo(SystemClock.uptimeMillis());
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_DOWN);
    assertThat(event.getPointerCount()).isEqualTo(1);
    assertThat(event.getX()).isEqualTo(0f);
    assertThat(event.getY()).isEqualTo(0f);
    assertThat(event.getRawX()).isEqualTo(0f);
    assertThat(event.getRawY()).isEqualTo(0f);
    assertThat(event.getMetaState()).isEqualTo(0);
    assertThat(event.getButtonState()).isEqualTo(0);
    assertThat(event.getXPrecision()).isEqualTo(0.0f);
    assertThat(event.getYPrecision()).isEqualTo(0.0f);
    assertThat(event.getDeviceId()).isEqualTo(0);
    assertThat(event.getEdgeFlags()).isEqualTo(0);
  }

  @Test
  public void buildAllFields() {
    MotionEvent event =
        new MotionEventBuilder()
            .withDownTime(1)
            .withEventTime(2)
            .withAction(MotionEvent.ACTION_CANCEL)
            .withPointer(3f, 4f)
            .withPointer(5f, 6f)
            .withMetaState(7)
            .withButtonState(8)
            .withXPrecision(9f)
            .withYPrecision(10f)
            .withDeviceId(11)
            .withEdgeFlags(12)
            .withSource(13)
            .withFlags(14)
            .build();

    assertThat(event.getDownTime()).isEqualTo(1);
    assertThat(event.getEventTime()).isEqualTo(2);
    assertThat(event.getAction()).isEqualTo(MotionEvent.ACTION_CANCEL);
    assertThat(event.getPointerCount()).isEqualTo(2);
    assertThat(event.getX()).isEqualTo(3f);
    assertThat(event.getY()).isEqualTo(4f);
    assertThat(event.getRawX()).isEqualTo(3f);
    assertThat(event.getRawY()).isEqualTo(4f);
    assertThat(event.getX(1)).isEqualTo(5f);
    assertThat(event.getY(1)).isEqualTo(6f);
    assertThat(event.getMetaState()).isEqualTo(7);
    assertThat(event.getButtonState()).isEqualTo(8);
    assertThat(event.getXPrecision()).isEqualTo(9f);
    assertThat(event.getYPrecision()).isEqualTo(10f);
    assertThat(event.getDeviceId()).isEqualTo(11);
    assertThat(event.getEdgeFlags()).isEqualTo(12);
  }

  @Test
  public void withActionIndex() throws Exception {
    MotionEvent event = new MotionEventBuilder().withAction(MotionEvent.ACTION_POINTER_UP).build();
    assertThat(event.getActionMasked()).isEqualTo(MotionEvent.ACTION_POINTER_UP);
    assertThat(event.getActionIndex()).isEqualTo(0);

    event =
        new MotionEventBuilder()
            .withAction(MotionEvent.ACTION_POINTER_UP)
            .withActionIndex(1)
            .build();
    assertThat(event.getActionMasked()).isEqualTo(MotionEvent.ACTION_POINTER_UP);
    assertThat(event.getActionIndex()).isEqualTo(1);
  }
}
