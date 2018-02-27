package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 27)
public class ShadowMotionEventTest {
  private MotionEvent event;
  private ShadowMotionEvent shadowMotionEvent;

  // @Before
  // public void setUp() throws Exception {
  //   event = MotionEvent.obtain(100, 200, MotionEvent.ACTION_MOVE, 5.0f, 10.0f, 0);
  //   shadowMotionEvent = shadowOf(event);
  // }
  //
  // @Test
  // public void addingSecondPointerSetsCount() {
  //   assertThat(event.getX(0)).isEqualTo(5.0f);
  //   assertThat(event.getY(0)).isEqualTo(10.0f);
  //   assertThat(event.getPointerCount()).isEqualTo(1);
  //
  //   shadowOf(event).setPointer2( 20.0f, 30.0f );
  //
  //   assertThat(event.getX(1)).isEqualTo(20.0f);
  //   assertThat(event.getY(1)).isEqualTo(30.0f);
  //   assertThat(event.getPointerCount()).isEqualTo(2);
  //   assertThat(event.getPointerIdBits()).isEqualTo(0x3);
  // }
  //
  // @Test
  // public void canSetPointerIdsByIndex() {
  //   shadowMotionEvent.setPointer2(20.0f, 30.0f);
  //   shadowMotionEvent.setPointerIds(2, 5);
  //   assertEquals(2, event.getPointerId(0));
  //   assertEquals(5, event.getPointerId(1));
  //   assertThat(event.getPointerIdBits()).isEqualTo(0x24);
  // }
  //
  // @Test
  // public void indexShowsUpInAction() {
  //   shadowMotionEvent.setPointerIndex(1);
  //   assertEquals(1 << MotionEvent.ACTION_POINTER_ID_SHIFT | MotionEvent.ACTION_MOVE, event.getAction());
  // }
  //
  // @Test
  // public void canGetActionIndex() {
  //   assertEquals(0, event.getActionIndex());
  //   shadowMotionEvent.setPointerIndex(1);
  //   assertEquals(1, event.getActionIndex());
  // }
  //
  // @Test
  // public void getActionMaskedStripsPointerIndexFromAction() {
  //   assertEquals(MotionEvent.ACTION_MOVE, event.getActionMasked());
  //   shadowMotionEvent.setPointerIndex(1);
  //   assertEquals(MotionEvent.ACTION_MOVE, event.getActionMasked());
  // }
  //
  // @Test
  // public void canFindPointerIndexFromId() {
  //   shadowMotionEvent.setPointer2(20.0f, 30.0f);
  //   shadowMotionEvent.setPointerIds(2, 1);
  //   assertEquals(0, event.findPointerIndex(2));
  //   assertEquals(1, event.findPointerIndex(1));
  //   assertEquals(-1, event.findPointerIndex(3));
  // }
  //
  // @Test
  // public void canSetMotionEventLocation() throws Exception {
  //   assertEquals(5.0f, event.getX(), 0.0f);
  //   assertEquals(10.0f, event.getY(), 0.0f);
  //   shadowMotionEvent.setLocation(10.0f, 20.0f);
  //   assertEquals(10.0f, event.getX(), 0.0f);
  //   assertEquals(20.0f, event.getY(), 0.0f);
  // }

  /** Test the expanded obtain method */
  @Test
  public void obtain_expanded() {
    MotionEvent.PointerCoords[] pointerCoords = {new MotionEvent.PointerCoords()};
    pointerCoords[0].clear();
    pointerCoords[0].x = 25;
    pointerCoords[0].y = 50;
    pointerCoords[0].pressure = 0;
    pointerCoords[0].size = 1;

    MotionEvent.PointerProperties[] pointerProperties = {new PointerProperties()};
    pointerProperties[0].id = 1;
    pointerProperties[0].toolType = 3;

    MotionEvent motionEvent =  MotionEvent.obtain(
        50, // downTime
        100, // evenTime
        MotionEvent.ACTION_DOWN, // action
        1, // pointerCount
        pointerProperties,
        pointerCoords,
        1, // metaState
        MotionEvent.BUTTON_PRIMARY, // buttonState,
        15, // xPrecision,
        15, // yPrecision
        2, // deviceId
        3, // edgeFlags
        InputDevice.SOURCE_TOUCHSCREEN, // source,
        4); // flags

    assertThat(motionEvent.getDownTime()).isEqualTo(50);
    assertThat(motionEvent.getEventTime()).isEqualTo(100);
    assertThat(motionEvent.getAction()).isEqualTo(MotionEvent.ACTION_DOWN);
    assertThat(motionEvent.getPointerCount()).isEqualTo(1);

    PointerProperties actualPointerProps = new PointerProperties();
    motionEvent.getPointerProperties(0, actualPointerProps);
    assertThat(actualPointerProps.id).isEqualTo(pointerProperties[0].id);
    assertThat(actualPointerProps.toolType).isEqualTo(pointerProperties[0].toolType);

    PointerCoords actualPointerCoords = new PointerCoords();
    motionEvent.getPointerCoords(0, actualPointerCoords);
    assertThat(actualPointerCoords.pressure).isEqualTo(pointerCoords[0].pressure);
    assertThat(actualPointerCoords.size).isEqualTo(pointerCoords[0].size);
    assertThat(actualPointerCoords.x).isEqualTo(pointerCoords[0].x);
    assertThat(actualPointerCoords.y).isEqualTo(pointerCoords[0].y);
    assertThat(actualPointerCoords.orientation).isEqualTo(pointerCoords[0].orientation);
    assertThat(actualPointerCoords.toolMajor).isEqualTo(pointerCoords[0].toolMajor);
    assertThat(actualPointerCoords.toolMinor).isEqualTo(pointerCoords[0].toolMinor);
    assertThat(actualPointerCoords.touchMajor).isEqualTo(pointerCoords[0].touchMajor);
    assertThat(actualPointerCoords.touchMinor).isEqualTo(pointerCoords[0].touchMinor);

    assertThat(motionEvent.getX()).isEqualTo(25.0f);


    assertThat(motionEvent.getMetaState()).isEqualTo(1);
    assertThat(motionEvent.getButtonState()).isEqualTo(MotionEvent.BUTTON_PRIMARY);
    assertThat(motionEvent.getXPrecision()).isEqualTo(15);
    assertThat(motionEvent.getYPrecision()).isEqualTo(15);
    assertThat(motionEvent.getDeviceId()).isEqualTo(2);
    assertThat(motionEvent.getEdgeFlags()).isEqualTo(3);
    assertThat(motionEvent.getSource()).isEqualTo(InputDevice.SOURCE_TOUCHSCREEN);
    assertThat(motionEvent.getFlags()).isEqualTo(4);
  }
}
