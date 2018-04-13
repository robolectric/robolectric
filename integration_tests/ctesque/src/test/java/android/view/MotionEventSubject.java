package android.view;

import android.os.Build;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.FloatSubject;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.LongSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import javax.annotation.Nullable;

/** {@link Subject} for {@link MotionEvent}. */
public final class MotionEventSubject extends Subject<MotionEventSubject, MotionEvent> {

  public static MotionEventSubject assertThat(MotionEvent event) {
    return Truth.assertAbout(motionEvents()).that(event);
  }

  public static Subject.Factory<MotionEventSubject, MotionEvent> motionEvents() {
    return MotionEventSubject::new;
  }

  private MotionEventSubject(FailureMetadata failureMetadata, @Nullable MotionEvent motionEvent) {
    super(failureMetadata, motionEvent);
  }

  public void hasAction(int action) {
    check("getAction()").that(actual().getAction()).isEqualTo(action);
  }

  /**
   * Check the value of {@link MotionEvent#getActionButton()}
   *
   * @throws IllegalStateException if called on below Android API 23
   */
  public void hasActionButton(int actionButton) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      throw new IllegalStateException(
          "getActionButton() is only available on Android API 23 and above");
    }
    check("getActionButton()").that(actual().getActionButton()).isEqualTo(actionButton);
  }

  public void hasButtonState(int buttonState) {
    check("getButtonState()").that(actual().getButtonState()).isEqualTo(buttonState);
  }

  public void hasDeviceId(int deviceId) {
    check("getDeviceId()").that(actual().getDeviceId()).isEqualTo(deviceId);
  }

  public void hasDownTime(long downTime) {
    check("getDownTime()").that(actual().getDownTime()).isEqualTo(downTime);
  }

  public void hasEdgeFlags(int edgeFlags) {
    check("getEdgeFlags()").that(actual().getEdgeFlags()).isEqualTo(edgeFlags);
  }

  public void hasEventTime(long eventTime) {
    check("getEventTime()").that(actual().getEventTime()).isEqualTo(eventTime);
  }

  public void hasFlags(int flags) {
    check("getFlags()").that(actual().getFlags()).isEqualTo(flags);
  }

  public void hasHistorySize(int historySize) {
    check("getHistorySize()").that(actual().getHistorySize()).isEqualTo(historySize);
  }

  public LongSubject historicalEventTime(int pos) {
    return check("getHistoricalEventTime(%s)", pos).that(actual().getHistoricalEventTime(pos));
  }

  public PointerCoordsSubject historicalPointerCoords(int pointerIndex, int pos) {
    PointerCoords outPointerCoords = new PointerCoords();
    actual().getHistoricalPointerCoords(pointerIndex, pos, outPointerCoords);
    return check("getHistoricalPointerCoords(%s, %s)", pointerIndex, pos)
        .about(PointerCoordsSubject.pointerCoords())
        .that(outPointerCoords);
  }

  public FloatSubject historicalPressure(int pos) {
    return check("getHistoricalPressure(%s)", pos).that(actual().getHistoricalPressure(pos));
  }

  public FloatSubject historicalOrientation(int pos) {
    return check("getHistoricalOrientation(%s)", pos).that(actual().getHistoricalOrientation(pos));
  }

  public FloatSubject historicalSize(int pos) {
    return check("getHistoricalSize(%s)", pos).that(actual().getHistoricalSize(pos));
  }

  public FloatSubject historicalTouchMajor(int pos) {
    return check("getHistoricalTouchMajor(%s)", pos).that(actual().getHistoricalTouchMajor(pos));
  }

  public FloatSubject historicalTouchMinor(int pos) {
    return check("getHistoricalTouchMinor(%s)", pos).that(actual().getHistoricalTouchMinor(pos));
  }

  public FloatSubject historicalToolMajor(int pos) {
    return check("getHistoricalToolMajor(%s)", pos).that(actual().getHistoricalToolMajor(pos));
  }

  public FloatSubject historicalToolMinor(int pos) {
    return check("getHistoricalToolMinor(%s)", pos).that(actual().getHistoricalToolMinor(pos));
  }

  public FloatSubject historicalX(int pos) {
    return check("getHistoricalX(%s)", pos).that(actual().getHistoricalX(pos));
  }

  public FloatSubject historicalY(int pos) {
    return check("getHistoricalY(%s)", pos).that(actual().getHistoricalY(pos));
  }

  public void hasMetaState(int metaState) {
    check("getMetaState()").that(actual().getMetaState()).isEqualTo(metaState);
  }

  public FloatSubject orientation() {
    return check("getOrientation()").that(actual().getOrientation());
  }

  public FloatSubject orientation(int pointerIndex) {
    return check("getOrientation(%s)", pointerIndex).that(actual().getOrientation(pointerIndex));
  }

  public PointerCoordsSubject pointerCoords(int pointerIndex) {
    PointerCoords outPointerCoords = new PointerCoords();
    actual().getPointerCoords(pointerIndex, outPointerCoords);
    return check("getPointerCoords(%s)", pointerIndex)
        .about(PointerCoordsSubject.pointerCoords())
        .that(outPointerCoords);
  }

  public void hasPointerCount(int pointerCount) {
    check("getPointerCount()").that(actual().getPointerCount()).isEqualTo(pointerCount);
  }

  public IntegerSubject pointerId(int pointerIndex) {
    return check("getPointerId(%s)", pointerIndex).that(actual().getPointerId(pointerIndex));
  }

  public PointerPropertiesSubject pointerProperties(int pointerIndex) {
    PointerProperties outPointerProps = new PointerProperties();
    actual().getPointerProperties(pointerIndex, outPointerProps);
    return check("getPointerProperties(%s)", pointerIndex)
        .about(PointerPropertiesSubject.pointerProperties())
        .that(outPointerProps);
  }

  public FloatSubject pressure() {
    return check("getPressure()").that(actual().getPressure());
  }

  public FloatSubject pressure(int pointerIndex) {
    return check("getPressure(%s)", pointerIndex).that(actual().getPressure(pointerIndex));
  }

  public FloatSubject rawX() {
    return check("getRawX()").that(actual().getRawX());
  }

  public FloatSubject rawY() {
    return check("getRawY()").that(actual().getRawY());
  }

  public FloatSubject size() {
    return check("getSize()").that(actual().getSize());
  }

  public FloatSubject size(int pointerIndex) {
    return check("getSize(%s)", pointerIndex).that(actual().getSize());
  }

  public FloatSubject toolMajor() {
    return check("getToolMajor()").that(actual().getToolMajor());
  }

  public FloatSubject toolMajor(int pointerIndex) {
    return check("getToolMajor(%s)", pointerIndex).that(actual().getToolMajor(pointerIndex));
  }

  public FloatSubject toolMinor() {
    return check("getToolMinor()").that(actual().getToolMinor());
  }

  public FloatSubject toolMinor(int pointerIndex) {
    return check("getToolMinor(%s)", pointerIndex).that(actual().getToolMinor(pointerIndex));
  }

  public FloatSubject touchMajor() {
    return check("getTouchMajor()").that(actual().getTouchMajor());
  }

  public FloatSubject touchMajor(int pointerIndex) {
    return check("getTouchMajor(%s)", pointerIndex).that(actual().getTouchMajor(pointerIndex));
  }

  public FloatSubject touchMinor() {
    return check("getTouchMinor()").that(actual().getTouchMinor());
  }

  public FloatSubject touchMinor(int pointerIndex) {
    return check("getTouchMinor(%s)", pointerIndex).that(actual().getTouchMinor(pointerIndex));
  }

  public FloatSubject x() {
    return check("getX()").that(actual().getX());
  }

  public FloatSubject x(int pointerIndex) {
    return check("getX(%s)", pointerIndex).that(actual().getX(pointerIndex));
  }

  public FloatSubject xPrecision() {
    return check("getXPrecision()").that(actual().getXPrecision());
  }

  public FloatSubject y() {
    return check("getY()").that(actual().getY());
  }

  public FloatSubject y(int pointerIndex) {
    return check("getY(%s)", pointerIndex).that(actual().getY(pointerIndex));
  }

  public FloatSubject yPrecision() {
    return check("getYPrecision()").that(actual().getYPrecision());
  }
}
