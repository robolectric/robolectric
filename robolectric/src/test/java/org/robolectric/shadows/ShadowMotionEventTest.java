package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.view.MotionEvent;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowMotionEventTest {
  private MotionEvent event;
  private ShadowMotionEvent shadowMotionEvent;

  @Before
  public void setUp() throws Exception {
    event = MotionEvent.obtain(100, 200, MotionEvent.ACTION_MOVE, 5.0f, 10.0f, 0);
    shadowMotionEvent = shadowOf(event);
  }

  @Test
  public void addingSecondPointerSetsCount() {
    assertThat(event.getX(0)).isEqualTo(5.0f);
    assertThat(event.getY(0)).isEqualTo(10.0f);
    assertThat(event.getPointerCount()).isEqualTo(1);

    shadowMotionEvent.setPointer2(20.0f, 30.0f);

    assertThat(event.getX(1)).isEqualTo(20.0f);
    assertThat(event.getY(1)).isEqualTo(30.0f);
    assertThat(event.getPointerCount()).isEqualTo(2);
  }

  @Test
  public void canSetPointerIdsByIndex() {
    shadowMotionEvent.setPointer2(20.0f, 30.0f);
    shadowMotionEvent.setPointerIds(2, 5);
    assertEquals(2, event.getPointerId(0));
    assertEquals(5, event.getPointerId(1));
  }

  @Test
  public void indexShowsUpInAction() {
    shadowMotionEvent.setPointerIndex(1);
    assertEquals(1 << MotionEvent.ACTION_POINTER_ID_SHIFT | MotionEvent.ACTION_MOVE, event.getAction());
  }

  @Test
  public void canGetActionIndex() {
    assertEquals(0, event.getActionIndex());
    shadowMotionEvent.setPointerIndex(1);
    assertEquals(1, event.getActionIndex());
  }

  @Test
  public void getActionMaskedStripsPointerIndexFromAction() {
    assertEquals(MotionEvent.ACTION_MOVE, event.getActionMasked());
    shadowMotionEvent.setPointerIndex(1);
    assertEquals(MotionEvent.ACTION_MOVE, event.getActionMasked());
  }

  @Test
  public void canFindPointerIndexFromId() {
    shadowMotionEvent.setPointer2(20.0f, 30.0f);
    shadowMotionEvent.setPointerIds(2, 1);
    assertEquals(0, event.findPointerIndex(2));
    assertEquals(1, event.findPointerIndex(1));
    assertEquals(-1, event.findPointerIndex(3));
  }
}
