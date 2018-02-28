package android.view;

import static android.view.MotionEventUtils.withCoords;
import static android.view.MotionEventUtils.withProperties;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Matrix;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.MotionEventUtils.PointerCoordsBuilder;
import android.view.MotionEventUtils.PointerPropertiesBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Test {@link MotionEvent}.
 *
 * Copied from Android cts/tests/tests/view/src/android/view/cts/MotionEventTest.java
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class MotionEventTest {
  private MotionEvent mMotionEvent1;
  private MotionEvent mMotionEvent2;
  private MotionEvent mMotionEventDynamic;
  private long mDownTime;
  private long mEventTime;
  private static final float X_3F           = 3.0f;
  private static final float Y_4F           = 4.0f;
  private static final int META_STATE       = KeyEvent.META_SHIFT_ON;
  private static final float PRESSURE_1F    = 1.0f;
  private static final float SIZE_1F        = 1.0f;
  private static final float X_PRECISION_3F  = 3.0f;
  private static final float Y_PRECISION_4F  = 4.0f;
  private static final int DEVICE_ID_1      = 1;
  private static final int EDGE_FLAGS       = MotionEvent.EDGE_TOP;
  private static final float DELTA          = 0.01f;

  @Before
  public void setup() {
    mDownTime = SystemClock.uptimeMillis();
    mEventTime = SystemClock.uptimeMillis();
    mMotionEvent1 = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_MOVE, X_3F, Y_4F, META_STATE);
    mMotionEvent2 = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_MOVE, X_3F, Y_4F, PRESSURE_1F, SIZE_1F, META_STATE,
        X_PRECISION_3F, Y_PRECISION_4F, DEVICE_ID_1, EDGE_FLAGS);
  }

  @After
  public void teardown() {
    if (null != mMotionEvent1) {
      mMotionEvent1.recycle();
    }
    if (null != mMotionEvent2) {
      mMotionEvent2.recycle();
    }
    if (null != mMotionEventDynamic) {
      mMotionEventDynamic.recycle();
    }
  }

  @Test
  public void testObtainBasic() {
    mMotionEvent1 = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_DOWN, X_3F, Y_4F, META_STATE);
    assertNotNull(mMotionEvent1);
    assertEquals(mDownTime, mMotionEvent1.getDownTime());
    assertEquals(mEventTime, mMotionEvent1.getEventTime());
    assertEquals(MotionEvent.ACTION_DOWN, mMotionEvent1.getAction());
    assertEquals(X_3F, mMotionEvent1.getX(), DELTA);
    assertEquals(Y_4F, mMotionEvent1.getY(), DELTA);
    assertEquals(X_3F, mMotionEvent1.getRawX(), DELTA);
    assertEquals(Y_4F, mMotionEvent1.getRawY(), DELTA);
    assertEquals(META_STATE, mMotionEvent1.getMetaState());
    assertEquals(0, mMotionEvent1.getDeviceId());
    assertEquals(0, mMotionEvent1.getEdgeFlags());
    assertEquals(PRESSURE_1F, mMotionEvent1.getPressure(), DELTA);
    assertEquals(SIZE_1F, mMotionEvent1.getSize(), DELTA);
    assertEquals(1.0f, mMotionEvent1.getXPrecision(), DELTA);
    assertEquals(1.0f, mMotionEvent1.getYPrecision(), DELTA);
  }

  @Test
  public void testObtainFromMotionEvent() {
    mMotionEventDynamic = MotionEvent.obtain(mMotionEvent2);
    assertNotNull(mMotionEventDynamic);
    assertEquals(mMotionEvent2.getDownTime(), mMotionEventDynamic.getDownTime());
    assertEquals(mMotionEvent2.getEventTime(), mMotionEventDynamic.getEventTime());
    assertEquals(mMotionEvent2.getAction(), mMotionEventDynamic.getAction());
    assertEquals(mMotionEvent2.getX(), mMotionEventDynamic.getX(), DELTA);
    assertEquals(mMotionEvent2.getY(), mMotionEventDynamic.getY(), DELTA);
    assertEquals(mMotionEvent2.getX(), mMotionEventDynamic.getRawX(), DELTA);
    assertEquals(mMotionEvent2.getY(), mMotionEventDynamic.getRawY(), DELTA);
    assertEquals(mMotionEvent2.getMetaState(), mMotionEventDynamic.getMetaState());
    assertEquals(mMotionEvent2.getDeviceId(), mMotionEventDynamic.getDeviceId());
    assertEquals(mMotionEvent2.getEdgeFlags(), mMotionEventDynamic.getEdgeFlags());
    assertEquals(mMotionEvent2.getPressure(), mMotionEventDynamic.getPressure(), DELTA);
    assertEquals(mMotionEvent2.getSize(), mMotionEventDynamic.getSize(), DELTA);
    assertEquals(mMotionEvent2.getXPrecision(), mMotionEventDynamic.getXPrecision(), DELTA);
    assertEquals(mMotionEvent2.getYPrecision(), mMotionEventDynamic.getYPrecision(), DELTA);
  }

  @Test
  public void testObtainAllFields() {
    mMotionEventDynamic = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_DOWN, X_3F, Y_4F, PRESSURE_1F, SIZE_1F, META_STATE,
        X_PRECISION_3F, Y_PRECISION_4F, DEVICE_ID_1, EDGE_FLAGS);
    assertNotNull(mMotionEventDynamic);
    assertEquals(mDownTime, mMotionEventDynamic.getDownTime());
    assertEquals(mEventTime, mMotionEventDynamic.getEventTime());
    assertEquals(MotionEvent.ACTION_DOWN, mMotionEventDynamic.getAction());
    assertEquals(X_3F, mMotionEventDynamic.getX(), DELTA);
    assertEquals(Y_4F, mMotionEventDynamic.getY(), DELTA);
    assertEquals(X_3F, mMotionEventDynamic.getRawX(), DELTA);
    assertEquals(Y_4F, mMotionEventDynamic.getRawY(), DELTA);
    assertEquals(META_STATE, mMotionEventDynamic.getMetaState());
    assertEquals(DEVICE_ID_1, mMotionEventDynamic.getDeviceId());
    assertEquals(EDGE_FLAGS, mMotionEventDynamic.getEdgeFlags());
    assertEquals(PRESSURE_1F, mMotionEventDynamic.getPressure(), DELTA);
    assertEquals(SIZE_1F, mMotionEventDynamic.getSize(), DELTA);
    assertEquals(X_PRECISION_3F, mMotionEventDynamic.getXPrecision(), DELTA);
    assertEquals(Y_PRECISION_4F, mMotionEventDynamic.getYPrecision(), DELTA);
  }

  @Test
  public void testObtainFromPropertyArrays() {
    PointerCoordsBuilder coordsBuilder0 =
        withCoords(X_3F, Y_4F).withPressure(PRESSURE_1F).withSize(SIZE_1F).
            withTool(1.2f, 1.4f);
    PointerCoordsBuilder coordsBuilder1 =
        withCoords(X_3F + 1.0f, Y_4F - 2.0f).withPressure(PRESSURE_1F + 0.2f).
            withSize(SIZE_1F + 0.5f).withTouch(2.2f, 0.6f);

    PointerPropertiesBuilder propertiesBuilder0 =
        withProperties(0, MotionEvent.TOOL_TYPE_FINGER);
    PointerPropertiesBuilder propertiesBuilder1 =
        withProperties(1, MotionEvent.TOOL_TYPE_FINGER);

    mMotionEventDynamic = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_MOVE, 2,
        new PointerProperties[] { propertiesBuilder0.build(), propertiesBuilder1.build() },
        new PointerCoords[] { coordsBuilder0.build(), coordsBuilder1.build() },
        META_STATE, 0, X_PRECISION_3F, Y_PRECISION_4F, DEVICE_ID_1, EDGE_FLAGS,
        InputDevice.SOURCE_TOUCHSCREEN, 0);

    // We expect to have data for two pointers
    assertEquals(2, mMotionEventDynamic.getPointerCount());
    assertEquals(0, mMotionEventDynamic.getPointerId(0));
    assertEquals(1, mMotionEventDynamic.getPointerId(1));
    assertEquals(0, mMotionEventDynamic.getFlags());
    verifyCurrentPointerData(mMotionEventDynamic,
        new PointerPropertiesBuilder[] { propertiesBuilder0, propertiesBuilder1 },
        new PointerCoordsBuilder[] { coordsBuilder0, coordsBuilder1 });
  }

  @Test
  public void testObtainNoHistory() {
    // Add two batch to one of our events
    mMotionEvent2.addBatch(mEventTime + 10, X_3F + 5.0f, Y_4F + 5.0f, 0.5f, 0.5f, 0);
    mMotionEvent2.addBatch(mEventTime + 20, X_3F + 10.0f, Y_4F + 15.0f, 2.0f, 3.0f, 0);
    // The newly added batch should be the "new" values of the event
    withCoords(X_3F + 10.0f, Y_4F + 15.0f).withPressure(2.0f).withSize(3.0f).
        verifyMatches(mMotionEvent2);
    assertEquals(mEventTime + 20, mMotionEvent2.getEventTime());
    // We should have history with 2 entries
    assertEquals(2, mMotionEvent2.getHistorySize());
    // The previous data should be history at index 1
    withCoords(X_3F + 5.0f, Y_4F + 5.0f).withPressure(0.5f).withSize(0.5f).
        verifyMatchesHistorical(mMotionEvent2, 1);
    assertEquals(mEventTime + 10, mMotionEvent2.getHistoricalEventTime(1));
    // And the original data should be history at index 0
    withCoords(X_3F, Y_4F).withPressure(1.0f).withSize(1.0f).
        verifyMatchesHistorical(mMotionEvent2, 0);
    assertEquals(mEventTime, mMotionEvent2.getHistoricalEventTime(0));

    assertEquals(2, mMotionEvent2.getHistorySize());

    mMotionEventDynamic = MotionEvent.obtainNoHistory(mMotionEvent2);
    // The newly obtained event should have the matching current content
    withCoords(X_3F + 10.0f, Y_4F + 15.0f).withPressure(2.0f).withSize(3.0f).
        verifyMatches(mMotionEvent2);
    // And no history
    assertEquals(0, mMotionEventDynamic.getHistorySize());
  }

  @Test
  public void testAccessAction() {
    assertEquals(MotionEvent.ACTION_MOVE, mMotionEvent1.getAction());

    mMotionEvent1.setAction(MotionEvent.ACTION_UP);
    assertEquals(MotionEvent.ACTION_UP, mMotionEvent1.getAction());

    mMotionEvent1.setAction(MotionEvent.ACTION_MOVE);
    assertEquals(MotionEvent.ACTION_MOVE, mMotionEvent1.getAction());

    mMotionEvent1.setAction(MotionEvent.ACTION_CANCEL);
    assertEquals(MotionEvent.ACTION_CANCEL, mMotionEvent1.getAction());

    mMotionEvent1.setAction(MotionEvent.ACTION_DOWN);
    assertEquals(MotionEvent.ACTION_DOWN, mMotionEvent1.getAction());
  }

  @Test
  public void testDescribeContents() {
    // make sure this method never throw any exception.
    mMotionEvent2.describeContents();
  }

  @Test
  public void testAccessEdgeFlags() {
    assertEquals(EDGE_FLAGS, mMotionEvent2.getEdgeFlags());

    int edgeFlags = 10;
    mMotionEvent2.setEdgeFlags(edgeFlags);
    assertEquals(edgeFlags, mMotionEvent2.getEdgeFlags());
  }

  @Test
  public void testWriteToParcel() {
    Parcel parcel = Parcel.obtain();
    mMotionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
    parcel.setDataPosition(0);

    MotionEvent motionEvent = MotionEvent.CREATOR.createFromParcel(parcel);
    assertEquals(mMotionEvent2.getRawY(), motionEvent.getRawY(), DELTA);
    assertEquals(mMotionEvent2.getRawX(), motionEvent.getRawX(), DELTA);
    assertEquals(mMotionEvent2.getY(), motionEvent.getY(), DELTA);
    assertEquals(mMotionEvent2.getX(), motionEvent.getX(), DELTA);
    assertEquals(mMotionEvent2.getAction(), motionEvent.getAction());
    assertEquals(mMotionEvent2.getDownTime(), motionEvent.getDownTime());
    assertEquals(mMotionEvent2.getEventTime(), motionEvent.getEventTime());
    assertEquals(mMotionEvent2.getEdgeFlags(), motionEvent.getEdgeFlags());
    assertEquals(mMotionEvent2.getDeviceId(), motionEvent.getDeviceId());
  }

  @Test
  public void testReadFromParcelWithInvalidPointerCountSize() {
    Parcel parcel = Parcel.obtain();
    mMotionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

    // Move to pointer id count.
    parcel.setDataPosition(4);
    parcel.writeInt(17);

    parcel.setDataPosition(0);
    try {
      MotionEvent.CREATOR.createFromParcel(parcel);
      fail("deserialized invalid parcel");
    } catch (RuntimeException e) {
      // Expected.
    }
  }

  @Test
  @Ignore // doesn't actually fail when expected on emulator API 23
  public void testReadFromParcelWithInvalidSampleSize() {
    Parcel parcel = Parcel.obtain();
    mMotionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

    // Move to sample count.
    parcel.setDataPosition(2 * 4);
    parcel.writeInt(0x000f0000);

    parcel.setDataPosition(0);
    try {
      MotionEvent.CREATOR.createFromParcel(parcel);
      fail("deserialized invalid parcel");
    } catch (RuntimeException e) {
      // Expected.
    }
  }

  @Test
  public void testToString() {
    // make sure this method never throw exception.
    mMotionEvent2.toString();
  }

  @Test
  public void testOffsetLocation() {
    assertEquals(X_3F, mMotionEvent2.getX(), DELTA);
    assertEquals(Y_4F, mMotionEvent2.getY(), DELTA);

    float offsetX = 1.0f;
    float offsetY = 1.0f;
    mMotionEvent2.offsetLocation(offsetX, offsetY);
    withCoords(X_3F + offsetX, Y_4F + offsetY).withPressure(PRESSURE_1F).withSize(SIZE_1F).
        verifyMatches(mMotionEvent2);
  }

  @Test
  public void testSetLocation() {
    assertEquals(X_3F, mMotionEvent2.getX(), DELTA);
    assertEquals(Y_4F, mMotionEvent2.getY(), DELTA);

    mMotionEvent2.setLocation(0.0f, 0.0f);
    withCoords(0.0f, 0.0f).withPressure(PRESSURE_1F).withSize(SIZE_1F).
        verifyMatches(mMotionEvent2);

    mMotionEvent2.setLocation(2.0f, 2.0f);
    withCoords(2.0f, 2.0f).withPressure(PRESSURE_1F).withSize(SIZE_1F).
        verifyMatches(mMotionEvent2);
  }

  @Test
  public void testGetHistoricalData() {
    assertEquals(0, mMotionEvent2.getHistorySize());

    mMotionEvent2.addBatch(mEventTime + 10, X_3F + 5.0f, Y_4F + 5.0f, 0.5f, 0.5f, 0);
    // The newly added batch should be the "new" values of the event
    withCoords(X_3F + 5.0f, Y_4F + 5.0f).withPressure(0.5f).withSize(0.5f).
        verifyMatches(mMotionEvent2);
    assertEquals(mEventTime + 10, mMotionEvent2.getEventTime());
    // We should have history with 1 entry
    assertEquals(1, mMotionEvent2.getHistorySize());
    // And the previous / original data should be history at index 0
    assertEquals(1, mMotionEvent2.getHistorySize());
    withCoords(X_3F, Y_4F).withPressure(1.0f).withSize(1.0f).
        verifyMatchesHistorical(mMotionEvent2, 0);
    assertEquals(mEventTime, mMotionEvent2.getHistoricalEventTime(0));

    // Add another update batch to our event
    mMotionEvent2.addBatch(mEventTime + 20, X_3F + 10.0f, Y_4F + 15.0f, 2.0f, 3.0f, 0);
    // The newly added batch should be the "new" values of the event
    withCoords(X_3F + 10.0f, Y_4F + 15.0f).withPressure(2.0f).withSize(3.0f).
        verifyMatches(mMotionEvent2);
    assertEquals(mEventTime + 20, mMotionEvent2.getEventTime());
    // We should have history with 2 entries
    assertEquals(2, mMotionEvent2.getHistorySize());
    // The previous data should be history at index 1
    withCoords(X_3F + 5.0f, Y_4F + 5.0f).withPressure(0.5f).withSize(0.5f).
        verifyMatchesHistorical(mMotionEvent2, 1);
    assertEquals(mEventTime + 10, mMotionEvent2.getHistoricalEventTime(1));
    // And the original data should be history at index 0
    withCoords(X_3F, Y_4F).withPressure(1.0f).withSize(1.0f).
        verifyMatchesHistorical(mMotionEvent2, 0);
    assertEquals(mEventTime, mMotionEvent2.getHistoricalEventTime(0));
  }

  private static void verifyCurrentPointerData(MotionEvent motionEvent,
      PointerPropertiesBuilder[] pointerPropertiesBuilders,
      PointerCoordsBuilder[] pointerCoordsBuilders) {
    assertNotNull(motionEvent);
    assertNotNull(pointerPropertiesBuilders);
    assertNotNull(pointerCoordsBuilders);
    final int pointerCount = motionEvent.getPointerCount();
    assertEquals(pointerCount, pointerPropertiesBuilders.length);
    assertEquals(pointerCount, pointerCoordsBuilders.length);

    // Test that we have the expected data fetched via MotionEvent.getPointerCoords API
    for (int i = 0; i < pointerCount; i++) {
      pointerCoordsBuilders[i].verifyMatchesPointerCoords(motionEvent, i);
    }

    // Test that we have the expected data fetched via per-field MotionEvent getter APIs
    for (int i = 0; i < pointerCount; i++) {
      pointerCoordsBuilders[i].verifyMatches(motionEvent, i);
    }

    // Test that we have the expected data fetched via MotionEvent.getPointerProperties API
    for (int i = 0; i < pointerCount; i++) {
      pointerPropertiesBuilders[i].verifyMatchesPointerProperties(motionEvent, i);
    }

    // Test that we have the expected data fetched via per-field MotionEvent getter APIs
    for (int i = 0; i < pointerCount; i++) {
      pointerPropertiesBuilders[i].verifyMatches(motionEvent, i);
    }
  }

  private static void verifyHistoricalPointerData(MotionEvent motionEvent,
      PointerCoordsBuilder[] pointerCoordsBuilders, int pos) {
    assertNotNull(motionEvent);
    assertNotNull(pointerCoordsBuilders);
    final int pointerCount = motionEvent.getPointerCount();
    assertEquals(pointerCount, pointerCoordsBuilders.length);

    // Test that we have the expected data fetched via MotionEvent.getHistoricalPointerCoords
    // API
    for (int i = 0; i < pointerCount; i++) {
      pointerCoordsBuilders[i].verifyMatchesHistoricalPointerCoords(motionEvent, i, pos);
    }

    // Test that we have the expected data fetched via per-field MotionEvent getter APIs
    for (int i = 0; i < pointerCount; i++) {
      pointerCoordsBuilders[i].verifyMatchesHistorical(motionEvent, i, pos);
    }
  }

  @Test
  public void testGetCurrentDataWithTwoPointers() {
    PointerCoordsBuilder coordsBuilder0 =
        withCoords(10.0f, 20.0f).withPressure(1.2f).withSize(2.0f).withTool(1.2f, 1.4f);
    PointerCoordsBuilder coordsBuilder1 =
        withCoords(30.0f, 40.0f).withPressure(1.4f).withSize(3.0f).withTouch(2.2f, 0.6f);

    PointerPropertiesBuilder propertiesBuilder0 =
        withProperties(0, MotionEvent.TOOL_TYPE_FINGER);
    PointerPropertiesBuilder propertiesBuilder1 =
        withProperties(1, MotionEvent.TOOL_TYPE_FINGER);

    mMotionEventDynamic = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_MOVE, 2,
        new PointerProperties[] { propertiesBuilder0.build(), propertiesBuilder1.build() },
        new PointerCoords[] { coordsBuilder0.build(), coordsBuilder1.build() },
        0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);

    // We expect to have data for two pointers
    assertEquals(2, mMotionEventDynamic.getPointerCount());
    assertEquals(0, mMotionEventDynamic.getPointerId(0));
    assertEquals(1, mMotionEventDynamic.getPointerId(1));
    assertEquals(0, mMotionEventDynamic.getFlags());
    verifyCurrentPointerData(mMotionEventDynamic,
        new PointerPropertiesBuilder[] { propertiesBuilder0, propertiesBuilder1 },
        new PointerCoordsBuilder[] { coordsBuilder0, coordsBuilder1 });
  }

  @Test
  public void testGetHistoricalDataWithTwoPointers() {
    // PHASE 1 - construct the initial data for the event
    PointerCoordsBuilder coordsBuilderInitial0 =
        withCoords(10.0f, 20.0f).withPressure(1.2f).withSize(2.0f).withTool(1.2f, 1.4f).
            withTouch(0.7f, 0.6f).withOrientation(2.0f);
    PointerCoordsBuilder coordsBuilderInitial1 =
        withCoords(30.0f, 40.0f).withPressure(1.4f).withSize(3.0f).withTool(1.3f, 1.7f).
            withTouch(2.7f, 3.6f).withOrientation(1.0f);

    PointerPropertiesBuilder propertiesBuilder0 =
        withProperties(0, MotionEvent.TOOL_TYPE_FINGER);
    PointerPropertiesBuilder propertiesBuilder1 =
        withProperties(1, MotionEvent.TOOL_TYPE_FINGER);

    mMotionEventDynamic = MotionEvent.obtain(mDownTime, mEventTime,
        MotionEvent.ACTION_MOVE, 2,
        new PointerProperties[] { propertiesBuilder0.build(), propertiesBuilder1.build() },
        new PointerCoords[] {
            coordsBuilderInitial0.build(), coordsBuilderInitial1.build() },
        0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);

    // We expect to have data for two pointers
    assertEquals(2, mMotionEventDynamic.getPointerCount());
    assertEquals(0, mMotionEventDynamic.getPointerId(0));
    assertEquals(1, mMotionEventDynamic.getPointerId(1));
    assertEquals(0, mMotionEventDynamic.getFlags());
    verifyCurrentPointerData(mMotionEventDynamic,
        new PointerPropertiesBuilder[] { propertiesBuilder0, propertiesBuilder1 },
        new PointerCoordsBuilder[] { coordsBuilderInitial0, coordsBuilderInitial1 });

    // PHASE 2 - add a new batch of data to our event
    PointerCoordsBuilder coordsBuilderNext0 =
        withCoords(15.0f, 25.0f).withPressure(1.6f).withSize(2.2f).withTool(1.2f, 1.4f).
            withTouch(1.0f, 0.9f).withOrientation(2.2f);
    PointerCoordsBuilder coordsBuilderNext1 =
        withCoords(35.0f, 45.0f).withPressure(1.8f).withSize(3.2f).withTool(1.2f, 1.4f).
            withTouch(0.7f, 0.6f).withOrientation(2.9f);

    mMotionEventDynamic.addBatch(mEventTime + 10,
        new PointerCoords[] { coordsBuilderNext0.build(), coordsBuilderNext1.build() }, 0);
    // We still expect to have data for two pointers
    assertEquals(2, mMotionEventDynamic.getPointerCount());
    assertEquals(0, mMotionEventDynamic.getPointerId(0));
    assertEquals(1, mMotionEventDynamic.getPointerId(1));
    assertEquals(0, mMotionEventDynamic.getFlags());

    // The newly added batch should be the "new" values of the event
    verifyCurrentPointerData(mMotionEventDynamic,
        new PointerPropertiesBuilder[] { propertiesBuilder0, propertiesBuilder1 },
        new PointerCoordsBuilder[] { coordsBuilderNext0, coordsBuilderNext1 });
    assertEquals(mEventTime + 10, mMotionEventDynamic.getEventTime());
    // We should have history with 1 entry
    assertEquals(1, mMotionEventDynamic.getHistorySize());
    // And the previous / original data should be history at index 0
    assertEquals(1, mMotionEventDynamic.getHistorySize());
    verifyHistoricalPointerData(mMotionEventDynamic,
        new PointerCoordsBuilder[] { coordsBuilderInitial0, coordsBuilderInitial1 },
        0);

    // PHASE 3 - add one more new batch of data to our event
    PointerCoordsBuilder coordsBuilderLast0 =
        withCoords(18.0f, 28.0f).withPressure(1.1f).withSize(2.9f).withTool(1.5f, 1.9f).
            withTouch(1.2f, 5.0f).withOrientation(3.2f);
    PointerCoordsBuilder coordsBuilderLast1 =
        withCoords(38.0f, 48.0f).withPressure(1.2f).withSize(2.5f).withTool(0.2f, 0.4f).
            withTouch(2.7f, 4.6f).withOrientation(0.2f);

    mMotionEventDynamic.addBatch(mEventTime + 20,
        new PointerCoords[] { coordsBuilderLast0.build(), coordsBuilderLast1.build() }, 0);
    // We still expect to have data for two pointers
    assertEquals(2, mMotionEventDynamic.getPointerCount());
    assertEquals(0, mMotionEventDynamic.getPointerId(0));
    assertEquals(1, mMotionEventDynamic.getPointerId(1));
    assertEquals(0, mMotionEventDynamic.getFlags());

    // The newly added batch should be the "new" values of the event
    verifyCurrentPointerData(mMotionEventDynamic,
        new PointerPropertiesBuilder[] { propertiesBuilder0, propertiesBuilder1 },
        new PointerCoordsBuilder[] { coordsBuilderLast0, coordsBuilderLast1 });
    assertEquals(mEventTime + 20, mMotionEventDynamic.getEventTime());
    // We should have history with 2 entries
    assertEquals(2, mMotionEventDynamic.getHistorySize());
    // The previous data should be history at index 1
    verifyHistoricalPointerData(mMotionEventDynamic,
        new PointerCoordsBuilder[] { coordsBuilderNext0, coordsBuilderNext1 },
        1);
    assertEquals(mEventTime + 10, mMotionEventDynamic.getHistoricalEventTime(1));
    // And the original data should be history at index 0
    verifyHistoricalPointerData(mMotionEventDynamic,
        new PointerCoordsBuilder[] { coordsBuilderInitial0, coordsBuilderInitial1 },
        0);
    assertEquals(mEventTime, mMotionEventDynamic.getHistoricalEventTime(0));
  }

  @Test
  public void testGetHistorySize() {
    long eventTime = SystemClock.uptimeMillis();
    float x = 10.0f;
    float y = 20.0f;
    float pressure = 1.0f;
    float size = 1.0f;

    mMotionEvent2.setAction(MotionEvent.ACTION_DOWN);
    assertEquals(0, mMotionEvent2.getHistorySize());

    mMotionEvent2.setAction(MotionEvent.ACTION_MOVE);
    mMotionEvent2.addBatch(eventTime, x, y, pressure, size, 0);
    assertEquals(1, mMotionEvent2.getHistorySize());
  }

  @Test
  public void testRecycle() {
    mMotionEvent2.setAction(MotionEvent.ACTION_MOVE);
    assertEquals(0, mMotionEvent2.getHistorySize());
    mMotionEvent2.addBatch(mEventTime, 10.0f, 5.0f, 1.0f, 0.0f, 0);
    assertEquals(1, mMotionEvent2.getHistorySize());

    mMotionEvent2.recycle();

    try {
      mMotionEvent2.recycle();
      fail("recycle() should throw an exception when the event has already been recycled.");
    } catch (RuntimeException ex) {
    }

    mMotionEvent2 = null; // since it was recycled, don't try to recycle again in tear down
  }

  @Test
  public void testTransformShouldApplyMatrixToPointsAndPreserveRawPosition() {
    // Generate some points on a circle.
    // Each point 'i' is a point on a circle of radius ROTATION centered at (3,2) at an angle
    // of ARC * i degrees clockwise relative to the Y axis.
    // The geometrical representation is irrelevant to the test, it's just easy to generate
    // and check rotation.  We set the orientation to the same angle.
    // Coordinate system: down is increasing Y, right is increasing X.
    final float PI_180 = (float) (Math.PI / 180);
    final float RADIUS = 10;
    final float ARC = 36;
    final float ROTATION = ARC * 2;

    final int pointerCount = 11;
    final int[] pointerIds = new int[pointerCount];
    final PointerCoords[] pointerCoords = new PointerCoords[pointerCount];
    for (int i = 0; i < pointerCount; i++) {
      final PointerCoords c = new PointerCoords();
      final float angle = (float) (i * ARC * PI_180);
      pointerIds[i] = i;
      pointerCoords[i] = c;
      c.x = (float) (Math.sin(angle) * RADIUS + 3);
      c.y = (float) (- Math.cos(angle) * RADIUS + 2);
      c.orientation = angle;
    }
    final MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE,
        pointerCount, pointerIds, pointerCoords, 0, 0, 0, 0, 0, 0, 0);
    final float originalRawX = 0 + 3;
    final float originalRawY = - RADIUS + 2;
    dump("Original points.", event);

    // Check original raw X and Y assumption.
    assertEquals(originalRawX, event.getRawX(), 0.001);
    assertEquals(originalRawY, event.getRawY(), 0.001);

    // Now translate the motion event so the circle's origin is at (0,0).
    event.offsetLocation(-3, -2);
    dump("Translated points.", event);

    // Offsetting the location should preserve the raw X and Y of the first point.
    assertEquals(originalRawX, event.getRawX(), 0.001);
    assertEquals(originalRawY, event.getRawY(), 0.001);

    // Apply a rotation about the origin by ROTATION degrees clockwise.
    Matrix matrix = new Matrix();
    matrix.setRotate(ROTATION);
    event.transform(matrix);
    dump("Rotated points.", event);

    // Check the points.
    for (int i = 0; i < pointerCount; i++) {
      final PointerCoords c = pointerCoords[i];
      event.getPointerCoords(i, c);

      final float angle = (float) ((i * ARC + ROTATION) * PI_180);
      assertEquals(Math.sin(angle) * RADIUS, c.x, 0.001);
      assertEquals(- Math.cos(angle) * RADIUS, c.y, 0.001);
      assertEquals(Math.tan(angle), Math.tan(c.orientation), 0.1);
    }

    // Applying the transformation should preserve the raw X and Y of the first point.
    assertEquals(originalRawX, event.getRawX(), 0.001);
    assertEquals(originalRawY, event.getRawY(), 0.001);
  }

  private void dump(String label, MotionEvent ev) {
    if (false) {
      StringBuilder msg = new StringBuilder();
      msg.append(label).append("\n");

      msg.append("  Raw: (").append(ev.getRawX()).append(",").append(ev.getRawY()).append(")\n");
      int pointerCount = ev.getPointerCount();
      for (int i = 0; i < pointerCount; i++) {
        msg.append("  Pointer[").append(i).append("]: (")
            .append(ev.getX(i)).append(",").append(ev.getY(i)).append("), orientation=")
            .append(ev.getOrientation(i) * 180 / Math.PI).append(" deg\n");
      }

      android.util.Log.i("TEST", msg.toString());
    }
  }

  @Test
  public void testPointerCoordsDefaultConstructor() {
    PointerCoords coords = new PointerCoords();

    assertEquals(0f, coords.x, 0.0f);
    assertEquals(0f, coords.y, 0.0f);
    assertEquals(0f, coords.pressure, 0.0f);
    assertEquals(0f, coords.size, 0.0f);
    assertEquals(0f, coords.touchMajor, 0.0f);
    assertEquals(0f, coords.touchMinor, 0.0f);
    assertEquals(0f, coords.toolMajor, 0.0f);
    assertEquals(0f, coords.toolMinor, 0.0f);
    assertEquals(0f, coords.orientation, 0.0f);
  }

  @Test
  public void testPointerCoordsCopyConstructor() {
    PointerCoords coords = new PointerCoords();
    coords.x = 1;
    coords.y = 2;
    coords.pressure = 3;
    coords.size = 4;
    coords.touchMajor = 5;
    coords.touchMinor = 6;
    coords.toolMajor = 7;
    coords.toolMinor = 8;
    coords.orientation = 9;
    coords.setAxisValue(MotionEvent.AXIS_GENERIC_1, 10);

    PointerCoords copy = new PointerCoords(coords);
    assertEquals(1f, copy.x, 0.0f);
    assertEquals(2f, copy.y, 0.0f);
    assertEquals(3f, copy.pressure, 0.0f);
    assertEquals(4f, copy.size, 0.0f);
    assertEquals(5f, copy.touchMajor, 0.0f);
    assertEquals(6f, copy.touchMinor, 0.0f);
    assertEquals(7f, copy.toolMajor, 0.0f);
    assertEquals(8f, copy.toolMinor, 0.0f);
    assertEquals(9f, copy.orientation, 0.0f);
    assertEquals(10f, coords.getAxisValue(MotionEvent.AXIS_GENERIC_1), 0.0f);
  }

  @Test
  public void testPointerCoordsCopyFrom() {
    PointerCoords coords = new PointerCoords();
    coords.x = 1;
    coords.y = 2;
    coords.pressure = 3;
    coords.size = 4;
    coords.touchMajor = 5;
    coords.touchMinor = 6;
    coords.toolMajor = 7;
    coords.toolMinor = 8;
    coords.orientation = 9;
    coords.setAxisValue(MotionEvent.AXIS_GENERIC_1, 10);

    PointerCoords copy = new PointerCoords();
    copy.copyFrom(coords);
    assertEquals(1f, copy.x, 0.0f);
    assertEquals(2f, copy.y, 0.0f);
    assertEquals(3f, copy.pressure, 0.0f);
    assertEquals(4f, copy.size, 0.0f);
    assertEquals(5f, copy.touchMajor, 0.0f);
    assertEquals(6f, copy.touchMinor, 0.0f);
    assertEquals(7f, copy.toolMajor, 0.0f);
    assertEquals(8f, copy.toolMinor, 0.0f);
    assertEquals(9f, copy.orientation, 0.0f);
    assertEquals(10f, coords.getAxisValue(MotionEvent.AXIS_GENERIC_1), 0.0f);
  }

  @Test
  public void testPointerPropertiesDefaultConstructor() {
    PointerProperties properties = new PointerProperties();

    assertEquals(MotionEvent.INVALID_POINTER_ID, properties.id);
    assertEquals(MotionEvent.TOOL_TYPE_UNKNOWN, properties.toolType);
  }

  @Test
  public void testPointerPropertiesCopyConstructor() {
    PointerProperties properties = new PointerProperties();
    properties.id = 1;
    properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;

    PointerProperties copy = new PointerProperties(properties);
    assertEquals(1, copy.id);
    assertEquals(MotionEvent.TOOL_TYPE_MOUSE, copy.toolType);
  }

  @Test
  public void testPointerPropertiesCopyFrom() {
    PointerProperties properties = new PointerProperties();
    properties.id = 1;
    properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;

    PointerProperties copy = new PointerProperties();
    copy.copyFrom(properties);
    assertEquals(1, copy.id);
    assertEquals(MotionEvent.TOOL_TYPE_MOUSE, copy.toolType);
  }

  @Test
  public void testAxisFromToString() {
    assertThat(MotionEvent.axisToString(MotionEvent.AXIS_RTRIGGER)).isEqualTo("AXIS_RTRIGGER");
    assertThat(MotionEvent.axisFromString("AXIS_RTRIGGER")).isEqualTo(MotionEvent.AXIS_RTRIGGER);
  }
}