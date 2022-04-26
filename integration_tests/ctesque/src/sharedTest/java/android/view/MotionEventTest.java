package android.view;

import static android.os.Build.VERSION_CODES.N;
import static androidx.test.ext.truth.view.MotionEventSubject.assertThat;
import static androidx.test.ext.truth.view.PointerCoordsSubject.assertThat;
import static androidx.test.ext.truth.view.PointerPropertiesSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.graphics.Matrix;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import androidx.test.core.view.PointerCoordsBuilder;
import androidx.test.core.view.PointerPropertiesBuilder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Test {@link android.view.MotionEvent}.
 *
 * <p>Baselined from Android cts/tests/tests/view/src/android/view/cts/MotionEventTest.java
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class MotionEventTest {
  private MotionEvent motionEvent1;
  private MotionEvent motionEvent2;
  private MotionEvent motionEventDynamic;
  private long downTime;
  private long eventTime;
  private static final float X_3F = 3.0f;
  private static final float Y_4F = 4.0f;
  private static final int META_STATE = KeyEvent.META_SHIFT_ON;
  private static final float PRESSURE_1F = 1.0f;
  private static final float SIZE_1F = 1.0f;
  private static final float X_PRECISION_3F = 3.0f;
  private static final float Y_PRECISION_4F = 4.0f;
  private static final int DEVICE_ID_1 = 1;
  private static final int EDGE_FLAGS = MotionEvent.EDGE_TOP;
  private static final float TOLERANCE = 0.01f;

  @Before
  public void setup() {
    downTime = SystemClock.uptimeMillis();
    eventTime = SystemClock.uptimeMillis();
    motionEvent1 =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_MOVE, X_3F, Y_4F, META_STATE);
    motionEvent2 =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            X_3F,
            Y_4F,
            PRESSURE_1F,
            SIZE_1F,
            META_STATE,
            X_PRECISION_3F,
            Y_PRECISION_4F,
            DEVICE_ID_1,
            EDGE_FLAGS);
  }

  @After
  public void teardown() {
    if (null != motionEvent1) {
      motionEvent1.recycle();
    }
    if (null != motionEvent2) {
      motionEvent2.recycle();
    }
    if (null != motionEventDynamic) {
      motionEventDynamic.recycle();
    }
  }

  @Test
  public void obtainBasic() {
    motionEvent1 =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, X_3F, Y_4F, META_STATE);
    assertThat(motionEvent1).isNotNull();
    assertThat(motionEvent1).hasDownTime(downTime);
    assertThat(motionEvent1).hasEventTime(eventTime);
    assertThat(motionEvent1).hasAction(MotionEvent.ACTION_DOWN);
    assertThat(motionEvent1).x().isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEvent1).y().isWithin(TOLERANCE).of(Y_4F);
    assertThat(motionEvent1).rawX().isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEvent1).rawY().isWithin(TOLERANCE).of(Y_4F);
    assertThat(motionEvent1).hasMetaState(META_STATE);
    assertThat(motionEvent1).hasDeviceId(0);
    assertThat(motionEvent1).hasEdgeFlags(0);
    assertThat(motionEvent1).pressure().isWithin(TOLERANCE).of(PRESSURE_1F);
    assertThat(motionEvent1).size().isWithin(TOLERANCE).of(SIZE_1F);
    assertThat(motionEvent1).xPrecision().isWithin(TOLERANCE).of(1.0f);
    assertThat(motionEvent1).yPrecision().isWithin(TOLERANCE).of(1.0f);
  }

  @Test
  public void testObtainFromMotionEvent() {
    motionEventDynamic = MotionEvent.obtain(motionEvent2);
    assertThat(motionEventDynamic).isNotNull();
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .isEqualToWithinTolerance(motionEvent2, TOLERANCE);
  }

  @Test
  public void testObtainAllFields() {
    motionEventDynamic =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_DOWN,
            X_3F,
            Y_4F,
            PRESSURE_1F,
            SIZE_1F,
            META_STATE,
            X_PRECISION_3F,
            Y_PRECISION_4F,
            DEVICE_ID_1,
            EDGE_FLAGS);
    assertThat(motionEventDynamic).isNotNull();
    assertThat(motionEventDynamic).hasButtonState(0);
    assertThat(motionEventDynamic).hasDownTime(downTime);
    assertThat(motionEventDynamic).hasEventTime(eventTime);
    assertThat(motionEventDynamic).hasAction(MotionEvent.ACTION_DOWN);
    assertThat(motionEventDynamic).x().isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEventDynamic).y().isWithin(TOLERANCE).of(Y_4F);
    assertThat(motionEventDynamic).rawX().isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEventDynamic).rawY().isWithin(TOLERANCE).of(Y_4F);
    assertThat(motionEventDynamic).hasMetaState(META_STATE);
    assertThat(motionEventDynamic).hasDeviceId(DEVICE_ID_1);
    assertThat(motionEventDynamic).hasEdgeFlags(EDGE_FLAGS);
    assertThat(motionEventDynamic).pressure().isWithin(TOLERANCE).of(PRESSURE_1F);
    assertThat(motionEventDynamic).size().isWithin(TOLERANCE).of(SIZE_1F);
    assertThat(motionEventDynamic).xPrecision().isWithin(TOLERANCE).of(X_PRECISION_3F);
    assertThat(motionEventDynamic).yPrecision().isWithin(TOLERANCE).of(Y_PRECISION_4F);
  }

  @Test
  public void actionButton() {
    MotionEvent event =
        MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, X_3F, Y_4F, META_STATE);
    if (Build.VERSION.SDK_INT < VERSION_CODES.M) {
      try {
        assertThat(event).hasActionButton(0);
        fail("IllegalStateException not thrown");
      } catch (IllegalStateException e) {
        // expected
      }
    } else {
      assertThat(event).hasActionButton(0);
    }
  }

  @Test
  public void testObtainFromRecycledEvent() {
    PointerCoords coords0 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(X_3F, Y_4F)
            .setPressure(PRESSURE_1F)
            .setSize(SIZE_1F)
            .setTool(1.2f, 1.4f)
            .build();
    PointerProperties properties0 =
        PointerPropertiesBuilder.newBuilder()
            .setId(0)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();
    motionEventDynamic =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            1,
            new PointerProperties[] {properties0},
            new PointerCoords[] {coords0},
            META_STATE,
            0,
            X_PRECISION_3F,
            Y_PRECISION_4F,
            DEVICE_ID_1,
            EDGE_FLAGS,
            InputDevice.SOURCE_TOUCHSCREEN,
            0);
    MotionEvent motionEventDynamicCopy = MotionEvent.obtain(motionEventDynamic);
    assertThat(motionEventDynamic.getToolType(0)).isEqualTo(MotionEvent.TOOL_TYPE_FINGER);
    assertThat(motionEventDynamicCopy.getToolType(0)).isEqualTo(MotionEvent.TOOL_TYPE_FINGER);
    motionEventDynamic.recycle();

    PointerCoords coords1 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(X_3F + 1.0f, Y_4F - 2.0f)
            .setPressure(PRESSURE_1F + 0.2f)
            .setSize(SIZE_1F + 0.5f)
            .setTouch(2.2f, 0.6f)
            .build();
    PointerProperties properties1 =
        PointerPropertiesBuilder.newBuilder()
            .setId(0)
            .setToolType(MotionEvent.TOOL_TYPE_MOUSE)
            .build();
    motionEventDynamic =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            1,
            new PointerProperties[] {properties1},
            new PointerCoords[] {coords1},
            META_STATE,
            0,
            X_PRECISION_3F,
            Y_PRECISION_4F,
            DEVICE_ID_1,
            EDGE_FLAGS,
            InputDevice.SOURCE_TOUCHSCREEN,
            0);
    assertThat(motionEventDynamicCopy.getToolType(0)).isEqualTo(MotionEvent.TOOL_TYPE_FINGER);
    assertThat(motionEventDynamic.getToolType(0)).isEqualTo(MotionEvent.TOOL_TYPE_MOUSE);
  }

  @Test
  public void testObtainFromPropertyArrays() {
    PointerCoords coords0 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(X_3F, Y_4F)
            .setPressure(PRESSURE_1F)
            .setSize(SIZE_1F)
            .setTool(1.2f, 1.4f)
            .build();
    PointerCoords coords1 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(X_3F + 1.0f, Y_4F - 2.0f)
            .setPressure(PRESSURE_1F + 0.2f)
            .setSize(SIZE_1F + 0.5f)
            .setTouch(2.2f, 0.6f)
            .build();

    PointerProperties properties0 =
        PointerPropertiesBuilder.newBuilder()
            .setId(0)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();
    PointerProperties properties1 =
        PointerPropertiesBuilder.newBuilder()
            .setId(1)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();

    motionEventDynamic =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            2,
            new PointerProperties[] {properties0, properties1},
            new PointerCoords[] {coords0, coords1},
            META_STATE,
            0,
            X_PRECISION_3F,
            Y_PRECISION_4F,
            DEVICE_ID_1,
            EDGE_FLAGS,
            InputDevice.SOURCE_TOUCHSCREEN,
            0);

    // We expect to have data for two pointers
    assertThat(motionEventDynamic).hasPointerCount(2);
    assertThat(motionEventDynamic).hasFlags(0);
    assertThat(motionEventDynamic).pointerId(0).isEqualTo(0);
    assertThat(motionEventDynamic).pointerId(1).isEqualTo(1);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(0)
        .isEqualToWithinTolerance(coords0, TOLERANCE);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(1)
        .isEqualToWithinTolerance(coords1, TOLERANCE);
    assertThat(motionEventDynamic).pointerProperties(0).isEqualTo(properties0);
    assertThat(motionEventDynamic).pointerProperties(1).isEqualTo(properties1);
  }

  @Test
  public void testObtainNoHistory() {
    // Add two batch to one of our events
    motionEvent2.addBatch(eventTime + 10, X_3F + 5.0f, Y_4F + 5.0f, 0.5f, 0.5f, 0);
    motionEvent2.addBatch(eventTime + 20, X_3F + 10.0f, Y_4F + 15.0f, 2.0f, 3.0f, 0);
    // The newly added batch should be the "new" values of the event
    assertThat(motionEvent2).x().isWithin(TOLERANCE).of(X_3F + 10.0f);
    assertThat(motionEvent2).y().isWithin(TOLERANCE).of(Y_4F + 15.0f);
    assertThat(motionEvent2).pressure().isWithin(TOLERANCE).of(2.0f);
    assertThat(motionEvent2).size().isWithin(TOLERANCE).of(3.0f);
    assertThat(motionEvent2).hasEventTime(eventTime + 20);

    // We should have history with 2 entries
    assertThat(motionEvent2).hasHistorySize(2);

    // The previous data should be history at index 1
    assertThat(motionEvent2).historicalX(1).isWithin(TOLERANCE).of(X_3F + 5.0f);
    assertThat(motionEvent2).historicalY(1).isWithin(TOLERANCE).of(Y_4F + 5.0f);
    assertThat(motionEvent2).historicalPressure(1).isWithin(TOLERANCE).of(0.5f);
    assertThat(motionEvent2).historicalSize(1).isWithin(TOLERANCE).of(0.5f);
    assertThat(motionEvent2).historicalEventTime(1).isEqualTo(eventTime + 10);

    // And the original data should be history at index 0
    assertThat(motionEvent2).historicalX(0).isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEvent2).historicalY(0).isWithin(TOLERANCE).of(Y_4F);
    assertThat(motionEvent2).historicalPressure(0).isWithin(TOLERANCE).of(1.0f);
    assertThat(motionEvent2).historicalSize(0).isWithin(TOLERANCE).of(1.0f);
    assertThat(motionEvent2).historicalEventTime(0).isEqualTo(eventTime);

    motionEventDynamic = MotionEvent.obtainNoHistory(motionEvent2);
    // The newly obtained event should have the matching current content and no history
    assertThat(motionEventDynamic).x().isWithin(TOLERANCE).of(X_3F + 10.0f);
    assertThat(motionEventDynamic).y().isWithin(TOLERANCE).of(Y_4F + 15.0f);
    assertThat(motionEventDynamic).pressure().isWithin(TOLERANCE).of(2.0f);
    assertThat(motionEventDynamic).size().isWithin(TOLERANCE).of(3.0f);
    assertThat(motionEventDynamic).hasHistorySize(0);
  }

  @Test
  public void testAccessAction() {
    assertThat(motionEvent1).hasAction(MotionEvent.ACTION_MOVE);

    motionEvent1.setAction(MotionEvent.ACTION_UP);
    assertThat(motionEvent1).hasAction(MotionEvent.ACTION_UP);
  }

  @Test
  public void testDescribeContents() {
    // make sure this method never throw any exception.
    motionEvent2.describeContents();
  }

  @Test
  public void testAccessEdgeFlags() {
    assertThat(motionEvent2).hasEdgeFlags(EDGE_FLAGS);

    motionEvent2.setEdgeFlags(10);
    assertThat(motionEvent2).hasEdgeFlags(10);
  }

  @Test
  public void testWriteToParcel() {
    Parcel parcel = Parcel.obtain();
    motionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
    parcel.setDataPosition(0);

    MotionEvent motionEvent = MotionEvent.CREATOR.createFromParcel(parcel);
    assertThat(motionEvent).rawY().isWithin(TOLERANCE).of(motionEvent2.getRawY());
    assertThat(motionEvent).rawX().isWithin(TOLERANCE).of(motionEvent2.getRawX());
    assertThat(motionEvent).y().isWithin(TOLERANCE).of(motionEvent2.getY());
    assertThat(motionEvent).x().isWithin(TOLERANCE).of(motionEvent2.getX());
    assertThat(motionEvent).hasAction(motionEvent2.getAction());
    assertThat(motionEvent).hasDownTime(motionEvent2.getDownTime());
    assertThat(motionEvent).hasEventTime(motionEvent2.getEventTime());
    assertThat(motionEvent).hasEdgeFlags(motionEvent2.getEdgeFlags());
    assertThat(motionEvent).hasDeviceId(motionEvent2.getDeviceId());
  }

  @Test
  public void testReadFromParcelWithInvalidPointerCountSize() {
    Parcel parcel = Parcel.obtain();
    motionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

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
  @SdkSuppress(minSdkVersion = N)
  public void testReadFromParcelWithInvalidSampleSize() {
    Parcel parcel = Parcel.obtain();
    motionEvent2.writeToParcel(parcel, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

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
    motionEvent2.toString();
  }

  @Test
  public void testOffsetLocationForPointerSource() {
    assertThat(motionEvent2).x().isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEvent2).y().isWithin(TOLERANCE).of(Y_4F);
    motionEvent2.setSource(InputDevice.SOURCE_TOUCHSCREEN);

    float offsetX = 1.0f;
    float offsetY = 1.0f;
    motionEvent2.offsetLocation(offsetX, offsetY);

    assertThat(motionEvent2).x().isWithin(TOLERANCE).of(X_3F + offsetX);
    assertThat(motionEvent2).y().isWithin(TOLERANCE).of(Y_4F + offsetY);
  }

  @Test
  public void testSetLocationForPointerSource() {
    assertThat(motionEvent2).x().isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEvent2).y().isWithin(TOLERANCE).of(Y_4F);
    motionEvent2.setSource(InputDevice.SOURCE_TOUCHSCREEN);

    motionEvent2.setLocation(2.0f, 2.0f);

    assertThat(motionEvent2).x().isWithin(TOLERANCE).of(2.0f);
    assertThat(motionEvent2).y().isWithin(TOLERANCE).of(2.0f);
  }

  @Test
  public void testGetHistoricalData() {
    assertThat(motionEvent2).hasHistorySize(0);

    motionEvent2.addBatch(eventTime + 10, X_3F + 5.0f, Y_4F + 5.0f, 0.5f, 0.5f, 0);
    // The newly added batch should be the "new" values of the event
    assertThat(motionEvent2).x().isWithin(TOLERANCE).of(X_3F + 5.0f);
    assertThat(motionEvent2).y().isWithin(TOLERANCE).of(Y_4F + 5.0f);
    assertThat(motionEvent2).pressure().isWithin(TOLERANCE).of(0.5f);
    assertThat(motionEvent2).size().isWithin(TOLERANCE).of(0.5f);
    assertThat(motionEvent2).hasEventTime(eventTime + 10);

    // We should have history with 1 entry
    assertThat(motionEvent2).hasHistorySize(1);
    // And the previous / original data should be history at index 0
    assertThat(motionEvent2).historicalEventTime(0).isEqualTo(eventTime);
    assertThat(motionEvent2).historicalX(0).isWithin(TOLERANCE).of(X_3F);
    assertThat(motionEvent2).historicalY(0).isWithin(TOLERANCE).of(Y_4F);
    assertThat(motionEvent2).historicalPressure(0).isWithin(TOLERANCE).of(1.0f);
    assertThat(motionEvent2).historicalSize(0).isWithin(TOLERANCE).of(1.0f);
  }

  @Test
  public void testGetCurrentDataWithTwoPointers() {
    PointerCoords coords0 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(10.0f, 20.0f)
            .setPressure(1.2f)
            .setSize(2.0f)
            .setTool(1.2f, 1.4f)
            .build();
    PointerCoords coords1 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(30.0f, 40.0f)
            .setPressure(1.4f)
            .setSize(3.0f)
            .setTouch(2.2f, 0.6f)
            .build();

    PointerProperties properties0 =
        PointerPropertiesBuilder.newBuilder()
            .setId(0)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();
    PointerProperties properties1 =
        PointerPropertiesBuilder.newBuilder()
            .setId(1)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();

    motionEventDynamic =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            2,
            new PointerProperties[] {properties0, properties1},
            new PointerCoords[] {coords0, coords1},
            0,
            0,
            1.0f,
            1.0f,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0);

    // We expect to have data for two pointers
    assertThat(motionEventDynamic).pointerId(0).isEqualTo(0);
    assertThat(motionEventDynamic).pointerId(1).isEqualTo(1);

    assertThat(motionEventDynamic).hasPointerCount(2);
    assertThat(motionEventDynamic).hasFlags(0);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(0)
        .isEqualToWithinTolerance(coords0, TOLERANCE);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(1)
        .isEqualToWithinTolerance(coords1, TOLERANCE);
    assertThat(motionEventDynamic).pointerProperties(0).isEqualTo(properties0);
    assertThat(motionEventDynamic).pointerProperties(1).isEqualTo(properties1);
  }

  @Test
  public void testGetHistoricalDataWithTwoPointers() {
    // PHASE 1 - construct the initial data for the event
    PointerCoords coordsInitial0 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(10.0f, 20.0f)
            .setPressure(1.2f)
            .setSize(2.0f)
            .setTool(1.2f, 1.4f)
            .setTouch(0.7f, 0.6f)
            .setOrientation(2.0f)
            .build();
    PointerCoords coordsInitial1 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(30.0f, 40.0f)
            .setPressure(1.4f)
            .setSize(3.0f)
            .setTool(1.3f, 1.7f)
            .setTouch(2.7f, 3.6f)
            .setOrientation(1.0f)
            .build();

    PointerProperties properties0 =
        PointerPropertiesBuilder.newBuilder()
            .setId(0)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();
    PointerProperties properties1 =
        PointerPropertiesBuilder.newBuilder()
            .setId(1)
            .setToolType(MotionEvent.TOOL_TYPE_FINGER)
            .build();

    motionEventDynamic =
        MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_MOVE,
            2,
            new PointerProperties[] {properties0, properties1},
            new PointerCoords[] {coordsInitial0, coordsInitial1},
            0,
            0,
            1.0f,
            1.0f,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0);

    // We expect to have data for two pointers
    assertThat(motionEventDynamic).hasPointerCount(2);
    assertThat(motionEventDynamic).pointerId(0).isEqualTo(0);
    assertThat(motionEventDynamic).pointerId(1).isEqualTo(1);
    assertThat(motionEventDynamic).hasFlags(0);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(0)
        .isEqualToWithinTolerance(coordsInitial0, TOLERANCE);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(1)
        .isEqualToWithinTolerance(coordsInitial1, TOLERANCE);
    assertThat(motionEventDynamic).pointerProperties(0).isEqualTo(properties0);
    assertThat(motionEventDynamic).pointerProperties(1).isEqualTo(properties1);

    // PHASE 2 - add a new batch of data to our event
    PointerCoords coordsNext0 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(15.0f, 25.0f)
            .setPressure(1.6f)
            .setSize(2.2f)
            .setTool(1.2f, 1.4f)
            .setTouch(1.0f, 0.9f)
            .setOrientation(2.2f)
            .build();
    PointerCoords coordsNext1 =
        PointerCoordsBuilder.newBuilder()
            .setCoords(35.0f, 45.0f)
            .setPressure(1.8f)
            .setSize(3.2f)
            .setTool(1.2f, 1.4f)
            .setTouch(0.7f, 0.6f)
            .setOrientation(2.9f)
            .build();

    motionEventDynamic.addBatch(eventTime + 10, new PointerCoords[] {coordsNext0, coordsNext1}, 0);
    // We still expect to have data for two pointers
    assertThat(motionEventDynamic).hasPointerCount(2);
    assertThat(motionEventDynamic).pointerId(0).isEqualTo(0);
    assertThat(motionEventDynamic).pointerId(1).isEqualTo(1);
    assertThat(motionEventDynamic).hasFlags(0);

    // The newly added batch should be the "new" values of the event
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(0)
        .isEqualToWithinTolerance(coordsNext0, TOLERANCE);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .pointerCoords(1)
        .isEqualToWithinTolerance(coordsNext1, TOLERANCE);
    assertThat(motionEventDynamic).pointerProperties(0).isEqualTo(properties0);
    assertThat(motionEventDynamic).pointerProperties(1).isEqualTo(properties1);
    assertThat(motionEventDynamic).hasEventTime(eventTime + 10);

    // We should have history with 1 entry
    assertThat(motionEventDynamic).hasHistorySize(1);
    // And the previous / original data should be history at position 0
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .historicalPointerCoords(0, 0)
        .isEqualToWithinTolerance(coordsInitial0, TOLERANCE);
    MotionEventEqualitySubject.assertThat(motionEventDynamic)
        .historicalPointerCoords(1, 0)
        .isEqualToWithinTolerance(coordsInitial1, TOLERANCE);
  }

  @Test
  public void testGetHistorySize() {
    long eventTime = SystemClock.uptimeMillis();
    float x = 10.0f;
    float y = 20.0f;
    float pressure = 1.0f;
    float size = 1.0f;

    motionEvent2.setAction(MotionEvent.ACTION_DOWN);
    assertThat(motionEvent2).hasHistorySize(0);

    motionEvent2.addBatch(eventTime, x, y, pressure, size, 0);
    assertThat(motionEvent2).hasHistorySize(1);
  }

  @Test
  public void testRecycle() {
    motionEvent2.recycle();

    try {
      motionEvent2.recycle();
      fail("recycle() should throw an exception when the event has already been recycled.");
    } catch (RuntimeException ex) {
      // expected
    }
    motionEvent2 = null; // since it was recycled, don't try to recycle again in tear down
  }

  @Test
  public void testTransformShouldApplyMatrixToPointsAndPreserveRawPosition() {
    // Generate some points on a circle.
    // Each point 'i' is a point on a circle of radius ROTATION centered at (3,2) at an angle
    // of ARC * i degrees clockwise relative to the Y axis.
    // The geometrical representation is irrelevant to the test, it's just easy to generate
    // and check rotation.  We set the orientation to the same angle.
    // Coordinate system: down is increasing Y, right is increasing X.
    final float pi180 = (float) (Math.PI / 180);
    final float radius = 10;
    final float arc = 36;
    final float rotation = arc * 2;

    final int pointerCount = 11;
    final int[] pointerIds = new int[pointerCount];
    final PointerCoords[] pointerCoords = new PointerCoords[pointerCount];
    for (int i = 0; i < pointerCount; i++) {
      final PointerCoords c = new PointerCoords();
      final float angle = (float) (i * arc * pi180);
      pointerIds[i] = i;
      pointerCoords[i] = c;
      c.x = (float) (Math.sin(angle) * radius + 3);
      c.y = (float) (-Math.cos(angle) * radius + 2);
      c.orientation = angle;
    }
    final MotionEvent event =
        MotionEvent.obtain(
            0,
            0,
            MotionEvent.ACTION_MOVE,
            pointerCount,
            pointerIds,
            pointerCoords,
            0,
            0,
            0,
            0,
            0,
            InputDevice.SOURCE_TOUCHSCREEN,
            0);
    final float originalRawX = 0 + 3;
    final float originalRawY = -radius + 2;

    // Check original raw X and Y assumption.
    assertThat(event).rawX().isWithin(TOLERANCE).of(originalRawX);
    assertThat(event).rawY().isWithin(TOLERANCE).of(originalRawY);

    // Now translate the motion event so the circle's origin is at (0,0).
    event.offsetLocation(-3, -2);

    // Offsetting the location should preserve the raw X and Y of the first point.
    assertThat(event).rawX().isWithin(TOLERANCE).of(originalRawX);
    assertThat(event).rawY().isWithin(TOLERANCE).of(originalRawY);

    // Apply a rotation about the origin by ROTATION degrees clockwise.
    Matrix matrix = new Matrix();
    matrix.setRotate(rotation);
    event.transform(matrix);

    // Check the points.
    for (int i = 0; i < pointerCount; i++) {
      final PointerCoords c = pointerCoords[i];
      event.getPointerCoords(i, c);

      final float angle = (float) ((i * arc + rotation) * pi180);
      assertThat(event)
          .pointerCoords(i)
          .x()
          .isWithin(TOLERANCE)
          .of((float) (Math.sin(angle) * radius));
      assertThat(event)
          .pointerCoords(i)
          .y()
          .isWithin(TOLERANCE)
          .of(-(float) Math.cos(angle) * radius);

      assertThat(Math.tan(c.orientation)).isWithin(0.1f).of(Math.tan(angle));
    }

    // Applying the transformation should preserve the raw X and Y of the first point.
    assertThat(event).rawX().isWithin(TOLERANCE).of(originalRawX);
    assertThat(event).rawY().isWithin(TOLERANCE).of(originalRawY);
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
    assertThat(copy).x().isWithin(TOLERANCE).of(1f);
    assertThat(copy).y().isWithin(TOLERANCE).of(2f);
    assertThat(copy).pressure().isWithin(TOLERANCE).of(3f);
    assertThat(copy).size().isWithin(TOLERANCE).of(4f);
    assertThat(copy).touchMajor().isWithin(TOLERANCE).of(5f);
    assertThat(copy).touchMinor().isWithin(TOLERANCE).of(6f);
    assertThat(copy).toolMajor().isWithin(TOLERANCE).of(7f);
    assertThat(copy).toolMinor().isWithin(TOLERANCE).of(8f);
    assertThat(copy).orientation().isWithin(TOLERANCE).of(9f);
    assertThat(copy).axisValue(MotionEvent.AXIS_GENERIC_1).isWithin(TOLERANCE).of(10f);
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
    assertThat(copy).x().isWithin(TOLERANCE).of(1f);
    assertThat(copy).y().isWithin(TOLERANCE).of(2f);
    assertThat(copy).pressure().isWithin(TOLERANCE).of(3f);
    assertThat(copy).size().isWithin(TOLERANCE).of(4f);
    assertThat(copy).touchMajor().isWithin(TOLERANCE).of(5f);
    assertThat(copy).touchMinor().isWithin(TOLERANCE).of(6f);
    assertThat(copy).toolMajor().isWithin(TOLERANCE).of(7f);
    assertThat(copy).toolMinor().isWithin(TOLERANCE).of(8f);
    assertThat(copy).orientation().isWithin(TOLERANCE).of(9f);
    assertThat(copy).axisValue(MotionEvent.AXIS_GENERIC_1).isWithin(TOLERANCE).of(10f);
  }

  @Test
  public void testPointerPropertiesDefaultConstructor() {
    PointerProperties properties = new PointerProperties();

    assertThat(properties).hasId(MotionEvent.INVALID_POINTER_ID);
    assertThat(properties).hasToolType(MotionEvent.TOOL_TYPE_UNKNOWN);
  }

  @Test
  public void testPointerPropertiesCopyConstructor() {
    PointerProperties properties = new PointerProperties();
    properties.id = 1;
    properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;

    PointerProperties copy = new PointerProperties(properties);
    assertThat(copy).hasId(1);
    assertThat(copy).hasToolType(MotionEvent.TOOL_TYPE_MOUSE);
  }

  @Test
  public void testPointerPropertiesCopyFrom() {
    PointerProperties properties = new PointerProperties();
    properties.id = 1;
    properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;

    PointerProperties copy = new PointerProperties();
    copy.copyFrom(properties);
    assertThat(copy).hasId(1);
    assertThat(properties).hasToolType(MotionEvent.TOOL_TYPE_MOUSE);
  }

  @Test
  public void testAxisFromToString() {
    assertThat(MotionEvent.axisToString(MotionEvent.AXIS_RTRIGGER)).isEqualTo("AXIS_RTRIGGER");
    assertThat(MotionEvent.axisFromString("AXIS_RTRIGGER")).isEqualTo(MotionEvent.AXIS_RTRIGGER);
  }

  private static class MotionEventEqualitySubject extends Subject {
    private final MotionEvent actual;

    private MotionEventEqualitySubject(FailureMetadata metadata, MotionEvent actual) {
      super(metadata, actual);
      this.actual = actual;
    }

    public static MotionEventEqualitySubject assertThat(MotionEvent event) {
      return Truth.assertAbout(motionEvents()).that(event);
    }

    public static Subject.Factory<MotionEventEqualitySubject, MotionEvent> motionEvents() {
      return MotionEventEqualitySubject::new;
    }

    public PointerCoordsEqualitySubject pointerCoords(int pointerIndex) {
      PointerCoords outPointerCoords = new PointerCoords();
      actual.getPointerCoords(pointerIndex, outPointerCoords);
      return check("getPointerCoords(%s)", pointerIndex)
          .about(PointerCoordsEqualitySubject.pointerCoords())
          .that(outPointerCoords);
    }

    public PointerCoordsEqualitySubject historicalPointerCoords(int pointerIndex, int pos) {
      PointerCoords outPointerCoords = new PointerCoords();
      actual.getHistoricalPointerCoords(pointerIndex, pos, outPointerCoords);
      return check("getHistoricalPointerCoords(%s, %s)", pointerIndex, pos)
          .about(PointerCoordsEqualitySubject.pointerCoords())
          .that(outPointerCoords);
    }

    /** Asserts that the given MotionEvent matches the current subject. */
    public void isEqualToWithinTolerance(MotionEvent other, float tolerance) {
      check("getDownTime()").that(actual.getDownTime()).isEqualTo(other.getDownTime());
      check("getEventTime()").that(actual.getEventTime()).isEqualTo(other.getEventTime());
      check("action()").that(actual.getAction()).isEqualTo(other.getAction());
      check("buttonState()").that(actual.getButtonState()).isEqualTo(other.getButtonState());
      check("deviceId()").that(actual.getDeviceId()).isEqualTo(other.getDeviceId());
      check("getFlags()").that(actual.getFlags()).isEqualTo(other.getFlags());
      check("getEdgeFlags()").that(actual.getEdgeFlags()).isEqualTo(other.getEdgeFlags());
      check("getXPrecision()").that(actual.getXPrecision()).isEqualTo(other.getXPrecision());
      check("getYPrecision()").that(actual.getYPrecision()).isEqualTo(other.getYPrecision());

      check("getX()").that(actual.getX()).isWithin(tolerance).of(other.getX());
      check("getY()").that(actual.getY()).isWithin(tolerance).of(other.getY());
      check("getPressure()").that(actual.getPressure()).isWithin(tolerance).of(other.getPressure());
      check("getSize()").that(actual.getSize()).isWithin(tolerance).of(other.getSize());
      check("getTouchMajor()")
          .that(actual.getTouchMajor())
          .isWithin(tolerance)
          .of(other.getTouchMajor());
      check("getTouchMinor()")
          .that(actual.getTouchMinor())
          .isWithin(tolerance)
          .of(other.getTouchMinor());
      check("getToolMajor()")
          .that(actual.getToolMajor())
          .isWithin(tolerance)
          .of(other.getToolMajor());
      check("getToolMinor()")
          .that(actual.getToolMinor())
          .isWithin(tolerance)
          .of(other.getToolMinor());
      check("getOrientation()")
          .that(actual.getOrientation())
          .isWithin(tolerance)
          .of(other.getOrientation());
      check("getPointerCount()").that(actual.getPointerCount()).isEqualTo(other.getPointerCount());

      for (int i = 1; i < actual.getPointerCount(); i++) {
        check("getX(%s)", i).that(actual.getX(i)).isWithin(tolerance).of(other.getX(i));
        check("getY(%s)", i).that(actual.getY(i)).isWithin(tolerance).of(other.getY(i));
        check("getPressure(%s)", i)
            .that(actual.getPressure(i))
            .isWithin(tolerance)
            .of(other.getPressure(i));
        check("getSize(%s)", i).that(actual.getSize(i)).isWithin(tolerance).of(other.getSize(i));
        check("getTouchMajor(%s)", i)
            .that(actual.getTouchMajor(i))
            .isWithin(tolerance)
            .of(other.getTouchMajor(i));
        check("getTouchMinor(%s)", i)
            .that(actual.getTouchMinor(i))
            .isWithin(tolerance)
            .of(other.getTouchMinor(i));
        check("getToolMajor(%s)", i)
            .that(actual.getToolMajor(i))
            .isWithin(tolerance)
            .of(other.getToolMajor(i));
        check("getToolMinor(%s)", i)
            .that(actual.getToolMinor(i))
            .isWithin(tolerance)
            .of(other.getToolMinor(i));
        check("getOrientation(%s)", i)
            .that(actual.getOrientation(i))
            .isWithin(tolerance)
            .of(other.getOrientation(i));
      }
      check("getHistorySize()").that(actual.getHistorySize()).isEqualTo(other.getHistorySize());

      for (int i = 0; i < actual.getHistorySize(); i++) {
        check("getHistoricalX(%s)", i).that(actual.getX(i)).isWithin(tolerance).of(other.getX(i));
        check("getHistoricalY(%s)", i)
            .that(actual.getHistoricalY(i))
            .isWithin(tolerance)
            .of(other.getHistoricalY(i));
        check("getHistoricalPressure(%s)", i)
            .that(actual.getHistoricalPressure(i))
            .isWithin(tolerance)
            .of(other.getHistoricalPressure(i));
        check("getHistoricalSize(%s)", i)
            .that(actual.getHistoricalSize(i))
            .isWithin(tolerance)
            .of(other.getHistoricalSize(i));
        check("getHistoricalTouchMajor(%s)", i)
            .that(actual.getHistoricalTouchMajor(i))
            .isWithin(tolerance)
            .of(other.getHistoricalTouchMajor(i));
        check("getHistoricalTouchMinor(%s)", i)
            .that(actual.getHistoricalTouchMinor(i))
            .isWithin(tolerance)
            .of(other.getHistoricalTouchMinor(i));
        check("getHistoricalToolMajor(%s)", i)
            .that(actual.getHistoricalToolMajor(i))
            .isWithin(tolerance)
            .of(other.getHistoricalToolMajor(i));
        check("getHistoricalToolMinor(%s)", i)
            .that(actual.getHistoricalToolMinor(i))
            .isWithin(tolerance)
            .of(other.getHistoricalToolMinor(i));
        check("getHistoricalOrientation(%s)", i)
            .that(actual.getHistoricalOrientation(i))
            .isWithin(tolerance)
            .of(other.getHistoricalOrientation(i));
      }
    }
  }

  private static class PointerCoordsEqualitySubject extends Subject {
    private final PointerCoords actual;

    private PointerCoordsEqualitySubject(FailureMetadata metadata, PointerCoords actual) {
      super(metadata, actual);
      this.actual = actual;
    }

    public static PointerCoordsEqualitySubject assertThat(PointerCoords coords) {
      return Truth.assertAbout(pointerCoords()).that(coords);
    }

    public static Subject.Factory<PointerCoordsEqualitySubject, PointerCoords> pointerCoords() {
      return PointerCoordsEqualitySubject::new;
    }

    public void isEqualToWithinTolerance(PointerCoords other, float tolerance) {
      check("orientation").that(actual.orientation).isWithin(tolerance).of(other.orientation);
      check("pressure").that(actual.pressure).isWithin(tolerance).of(other.pressure);
      check("size").that(actual.size).isWithin(tolerance).of(other.size);
      check("toolMajor").that(actual.toolMajor).isWithin(tolerance).of(other.toolMajor);
      check("toolMinor").that(actual.toolMinor).isWithin(tolerance).of(other.toolMinor);
      check("touchMajor").that(actual.touchMajor).isWithin(tolerance).of(other.touchMajor);
      check("touchMinor").that(actual.touchMinor).isWithin(tolerance).of(other.touchMinor);
      check("x").that(actual.x).isWithin(tolerance).of(other.x);
      check("y").that(actual.y).isWithin(tolerance).of(other.y);
    }
  }
}
