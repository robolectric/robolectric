package org.robolectric.shadows;

import static com.google.common.base.Preconditions.checkState;

import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper builder for creating {@link MotionEvent}'s.
 *
 * Default values for unspecified attributes are 0 unless otherwise noted.
 *
 * TODO: move this class to another package, since its not related to shadows
 */
public class MotionEventBuilder {

  private long downTime = 0;
  private long eventTime = SystemClock.uptimeMillis();
  private int action = MotionEvent.ACTION_DOWN;
  private int actionIndex = -1;
  private List<PointerProperties> pointerPropertiesList = new ArrayList<>();
  private List<PointerCoords> pointerCoordsList = new ArrayList<>();
  private int metaState = 0;
  private int buttonState = 0;
  private float xPrecision = 0f;
  private float yPrecision = 0f;
  private int deviceId = 0;
  private int edgeFlags = 0;
  private int source = 0;
  private int flags = 0;

  /**
   * Start building a new MotionEvent.
   *
   * @return a new MotionEventBuilder.
   */
  public static MotionEventBuilder buildMotionEvent() {
    return new MotionEventBuilder();
  }

  /**
   * Sets the down time.
   *
   * @see MotionEvent#getDownTime()
   */
  public MotionEventBuilder withDownTime(long downTime) {
    this.downTime = downTime;
    return this;
  }

  /**
   * Sets the event time. Default is SystemClock.uptimeMillis().
   *
   * @see MotionEvent#getEventTime()
   */
  public MotionEventBuilder withEventTime(long eventTime) {
    this.eventTime = eventTime;
    return this;
  }

  /**
   * Sets the action. Default is {@link MotionEvent.ACTION_DOWN}.
   *
   * @see MotionEvent#getAction()
   */
  public MotionEventBuilder withAction(int action) {
    this.action = action;
    return this;
  }

  /**
   * Sets the pointer index associated with the action.
   *
   * @see MotionEvent#getActionIndex()
   */
  public MotionEventBuilder withActionIndex(int pointerIndex) {
    checkState(pointerIndex <= 0xFF, "pointerIndex must be less than 0xff");
    this.actionIndex = pointerIndex;
    return this;
  }

  /**
   * Sets the metaState.
   *
   * @see MotionEvent#getMetaState()
   */
  public MotionEventBuilder withMetaState(int metastate) {
    this.metaState = metastate;
    return this;
  }

  /**
   * Sets the button state.
   *
   * @see MotionEvent#getButtonState()
   */
  public MotionEventBuilder withButtonState(int buttonState) {
    this.buttonState = buttonState;
    return this;
  }

  /**
   * Sets the x precision.
   *
   * @see MotionEvent#getXPrecision()
   */
  public MotionEventBuilder withXPrecision(float xPrecision) {
    this.xPrecision = xPrecision;
    return this;
  }

  /**
   * Sets the y precision.
   *
   * @see MotionEvent#getYPrecision()
   */
  public MotionEventBuilder withYPrecision(float yPrecision) {
    this.yPrecision = yPrecision;
    return this;
  }

  /**
   * Sets the device id.
   *
   * @see MotionEvent#getDeviceId()
   */
  public MotionEventBuilder withDeviceId(int deviceId) {
    this.deviceId = deviceId;
    return this;
  }

  /**
   * Sets the edge flags.
   *
   * @see MotionEvent#getEdgeFlags()
   */
  public MotionEventBuilder withEdgeFlags(int edgeFlags) {
    this.edgeFlags = edgeFlags;
    return this;
  }

  /**
   * Sets the source.
   *
   * @see MotionEvent#getSource()
   */
  public MotionEventBuilder withSource(int source) {
    this.source = source;
    return this;
  }

  /**
   * Sets the flags.
   *
   * @see MotionEvent#getFlags()
   */
  public MotionEventBuilder withFlags(int flags) {
    this.flags = flags;
    return this;
  }

  /**
   * Simple mechanism to add a pointer to the MotionEvent.
   *
   * <p>Can be called multiple times to add multiple pointers to the event.
   */
  public MotionEventBuilder withPointer(float x, float y) {
    PointerProperties pointerProperties = new PointerProperties();
    pointerProperties.id = pointerPropertiesList.size();
    PointerCoords pointerCoords = new PointerCoords();
    pointerCoords.x = x;
    pointerCoords.y = y;
    return withPointer(pointerProperties, pointerCoords);
  }

  /**
   * An expanded variant of {@link #withPointer(float, float)} that supports specifying all pointer
   * properties and coords data.
   */
  public MotionEventBuilder withPointer(
      PointerProperties pointerProperties, PointerCoords pointerCoords) {
    pointerPropertiesList.add(pointerProperties);
    pointerCoordsList.add(pointerCoords);
    return this;
  }

  /** Returns a MotionEvent with the provided data or reasonable defaults. */
  public MotionEvent build() {
    if (pointerPropertiesList.size() == 0) {
      withPointer(0, 0);
    }
    if (actionIndex != -1) {
      action = action | (actionIndex << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
    }
    return MotionEvent.obtain(
        downTime,
        eventTime,
        action,
        pointerPropertiesList.size(),
        pointerPropertiesList.toArray(new PointerProperties[pointerPropertiesList.size()]),
        pointerCoordsList.toArray(new MotionEvent.PointerCoords[pointerCoordsList.size()]),
        metaState,
        buttonState,
        xPrecision,
        yPrecision,
        deviceId,
        edgeFlags,
        source,
        flags);
  }
}
