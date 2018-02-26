package org.robolectric.shadows;

import static org.robolectric.Shadows.shadowOf;

import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import java.lang.reflect.Constructor;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * Shadow of MotionEvent.
 *
 * Original Android implementation stores motion events in a pool of native objects.
 * This shadow bypasses that mechanism to store each motion event in a unique java object.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(MotionEvent.class)
public class ShadowMotionEvent {
  @RealObject private MotionEvent realObject;

  private int action;
  private int pointerCount = 1;
  private long downTime;
  private long eventTime;
  private int[] pointerIds = {0, 1};
  private int pointerIndex;
  private int source;
  private PointerProperties[] pointerProperties;
  private PointerCoords[] pointerCoords;
  private int metaState;
  private int buttonState;
  private float xPrecision;
  private float yPrecision;
  private int deviceId;
  private int edgeFlags;
  private int flags;
  private float pressure = 1.0f;
  private float size;

  @Implementation
  protected static MotionEvent obtain(long downTime, long eventTime,
      int action, int pointerCount, PointerProperties[] pointerProperties,
      PointerCoords[] pointerCoords, int metaState, int buttonState,
      float xPrecision, float yPrecision, int deviceId,
      int edgeFlags, int source, int flags) {
    try {
      Constructor<MotionEvent> constructor = MotionEvent.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      MotionEvent motionEvent = constructor.newInstance();
      ShadowMotionEvent shadowMotionEvent = shadowOf(motionEvent);
      shadowMotionEvent.downTime = downTime;
      shadowMotionEvent.eventTime = eventTime;
      shadowMotionEvent.action = action;
      shadowMotionEvent.pointerCount = pointerCount;
      shadowMotionEvent.pointerProperties = pointerProperties;
      shadowMotionEvent.pointerCoords = pointerCoords;
      shadowMotionEvent.metaState = metaState;
      shadowMotionEvent.buttonState = buttonState;
      shadowMotionEvent.xPrecision = xPrecision;
      shadowMotionEvent.yPrecision = yPrecision;
      shadowMotionEvent.deviceId = deviceId;
      shadowMotionEvent.edgeFlags = edgeFlags;
      shadowMotionEvent.source = source;
      shadowMotionEvent.flags = flags;
      return motionEvent;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected static MotionEvent obtain(long downTime, long eventTime, int action,
      float x, float y, float pressure, float size, int metaState,
      float xPrecision, float yPrecision, int deviceId, int edgeFlags) {

    final PointerProperties[] pp = new PointerProperties[1];
    pp[0].clear();
    pp[0].id = 0;

    final PointerCoords pc[] = new PointerCoords[1];
    pc[0].x = x;
    pc[0].y = y;
    pc[0].pressure = pressure;
    pc[0].size = size;

    return obtain(downTime, eventTime, action, 1, pp, pc, metaState, 0,
        xPrecision, yPrecision, deviceId, edgeFlags, InputDevice.SOURCE_UNKNOWN, 0);
  }

  @Implementation
  public static MotionEvent obtain(MotionEvent motionEvent) {
    ShadowMotionEvent shadowMotionEvent = shadowOf(motionEvent);
    return obtain(motionEvent.getDownTime(),
        motionEvent.getEventTime(),
        motionEvent.getAction(),
        motionEvent.getPointerCount(),
        shadowMotionEvent.pointerProperties,
        shadowMotionEvent.pointerCoords,
        motionEvent.getMetaState(),
        motionEvent.getButtonState(),
        motionEvent.getXPrecision(),
        motionEvent.getYPrecision(),
        motionEvent.getDeviceId(),
        motionEvent.getEdgeFlags(),
        motionEvent.getSource(),
        motionEvent.getFlags());
  }

  @Implementation
  public static MotionEvent obtainNoHistory(MotionEvent motionEvent) {
    return obtain(motionEvent);
  }

  @Implementation
  protected void scale(float scale) {
    throw new UnsupportedOperationException();
  }

  @Implementation
  protected int getDeviceId() {
    return deviceId;
  }

  @Implementation
  public int getSource() {
    return source;
  }

  @Implementation
  public void setSource(int source) {
    this.source = source;
  }

  @Implementation
  public int getAction() {
    return action | (pointerIndex << MotionEvent.ACTION_POINTER_ID_SHIFT);
  }

  @Implementation
  public int getActionMasked() {
    return action & MotionEvent.ACTION_MASK;
  }

  @Implementation
  public final int getActionIndex() {
    return pointerIndex;
  }

  @Implementation
  public void setAction(int action ) {
    this.action = action;
  }

  @Implementation
  protected int getFlags() {
    return flags;
  }

  @Implementation
  public final long getDownTime() {
    return downTime;
  }

  @Implementation
  public final long getEventTime() {
    return eventTime;
  }

  @Implementation
  public final float getX() {
    return getX(0);
  }

  @Implementation
  public final float getY() {
    return getY(0);
  }

  @Implementation
  public final float getPressure(int pointerIndex) {
    return pressure;
  }

  @Implementation
  protected final float getSize() {
    return size;
  }

  @Implementation
  protected final float getTouchMajor() {
    return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MAJOR, 0, HISTORY_CURRENT);
  }

  /**
   * {@link #getTouchMinor(int)} for the first pointer index (may be an
   * arbitrary pointer identifier).
   *
   * @see #AXIS_TOUCH_MINOR
   */
  public final float getTouchMinor() {
    return nativeGetAxisValue(mNativePtr, AXIS_TOUCH_MINOR, 0, HISTORY_CURRENT);
  }

  /**
   * {@link #getToolMajor(int)} for the first pointer index (may be an
   * arbitrary pointer identifier).
   *
   * @see #AXIS_TOOL_MAJOR
   */
  public final float getToolMajor() {
    return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MAJOR, 0, HISTORY_CURRENT);
  }

  /**
   * {@link #getToolMinor(int)} for the first pointer index (may be an
   * arbitrary pointer identifier).
   *
   * @see #AXIS_TOOL_MINOR
   */
  public final float getToolMinor() {
    return nativeGetAxisValue(mNativePtr, AXIS_TOOL_MINOR, 0, HISTORY_CURRENT);
  }

  /**
   * {@link #getOrientation(int)} for the first pointer index (may be an
   * arbitrary pointer identifier).
   *
   * @see #AXIS_ORIENTATION
   */
  public final float getOrientation() {
    return nativeGetAxisValue(mNativePtr, AXIS_ORIENTATION, 0, HISTORY_CURRENT);
  }

  @Implementation
  public float getRawX() {
    return getX();
  }

  @Implementation
  public float getRawY() {
    return getY();
  }


  @Implementation
  public final float getX(int pointerIndex) {
    return pointerCoords[pointerIndex].x;
  }

  @Implementation
  public final float getY(int pointerIndex) {
    return pointerCoords[pointerIndex].y;
  }

  @Implementation
  public final int getPointerCount() {
    return pointerCount;
  }

  @Implementation
  public final int getPointerId(int index) {
    return pointerIds[index];
  }

  @Implementation
  public final int getPointerIdBits() {
    int idBits = 0;
    for (int i = 0; i < pointerCount; i++) {
      idBits |= 1 << pointerIds[i];
    }
    return idBits;
  }

  @Implementation
  public final int findPointerIndex(int id) {
    for (int i = 0; i < pointerIds.length; i++) {
      int pointerId = pointerIds[i];

      if (pointerId == id) {
        return i;
      }
    }
    return -1;
  }

  @Implementation
  public final void setLocation(float x, float y) {
    this.x[0] = x;
    this.y[0] = y;
  }

  public MotionEvent setPointer2(float x, float y) {
    this.x[1] = x;
    this.y[1] = y;
    pointerCount = 2;
    return realObject;
  }

  public void setPointerIndex(int pointerIndex) {
    this.pointerIndex = pointerIndex;
  }

  public void setPointerIds(int index0PointerId, int index1PointerId) {
    pointerIds[0] = index0PointerId;
    pointerIds[1] = index1PointerId;
  }

  @Implementation
  protected int getButtonState() {
    return buttonState;
  }

  @Implementation
  protected float getXPrecision() {
    return xPrecision;
  }

  @Implementation
  protected float getYPrecision() {
    return yPrecision;
  }


  @Implementation
  protected int getMetaState() {
    return metaState;
  }

  @Implementation
  protected  int getEdgeFlags() {
    return edgeFlags;
  }

  @Implementation
  protected void getPointerProperties(int pointerIndex,
      PointerProperties outPointerProperties) {
    outPointerProperties.id = pointerProperties[pointerIndex].id;
    outPointerProperties.toolType = pointerProperties[pointerIndex].toolType;
  }

  @Implementation
  protected void getPointerCoords(int pointerIndex, PointerCoords outPointerCoords) {
    outPointerCoords.pressure = pointerCoords[pointerIndex].pressure;
    outPointerCoords.size = pointerCoords[pointerIndex].size;
    outPointerCoords.x = pointerCoords[pointerIndex].x;
    outPointerCoords.y = pointerCoords[pointerIndex].y;
    outPointerCoords.orientation = pointerCoords[pointerIndex].orientation;
    outPointerCoords.toolMajor = pointerCoords[pointerIndex].toolMajor;
    outPointerCoords.toolMinor = pointerCoords[pointerIndex].toolMinor;
    outPointerCoords.touchMajor = pointerCoords[pointerIndex].touchMajor;
    outPointerCoords.touchMinor = pointerCoords[pointerIndex].touchMinor;
  }
}
