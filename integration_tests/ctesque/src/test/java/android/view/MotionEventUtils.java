package android.view;

import static org.junit.Assert.assertEquals;

import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;

/**
 * Utils for testing MotionEvent.
 *
 * <p>Copied from Android cts/tests/tests/view/src/android/view/cts/MotionEventUtils.java
 */
public class MotionEventUtils {
  private static final float DELTA = 0.01f;

  private MotionEventUtils() {}

  public static PointerCoordsBuilder withCoords(float x, float y) {
    final PointerCoordsBuilder builder = new PointerCoordsBuilder();
    builder.x = x;
    builder.y = y;
    return builder;
  }

  public static PointerPropertiesBuilder withProperties(int id, int toolType) {
    final PointerPropertiesBuilder builder = new PointerPropertiesBuilder();
    builder.id = id;
    builder.toolType = toolType;
    return builder;
  }

  public static class PointerPropertiesBuilder {
    private int id;
    private int toolType;

    public PointerProperties build() {
      final PointerProperties pointerProperties = new PointerProperties();
      pointerProperties.id = id;
      pointerProperties.toolType = toolType;
      return pointerProperties;
    }

    public void verifyMatches(MotionEvent that, int pointerIndex) {
      assertEquals("Pointer ID should be the same", that.getPointerId(pointerIndex), this.id);
      assertEquals("Tool type should be the same", that.getToolType(pointerIndex), this.toolType);
    }

    public void verifyMatchesPointerProperties(PointerProperties that) {
      assertEquals("Pointer ID should be the same", that.id, this.id);
      assertEquals("Tool type should be the same", that.toolType, this.toolType);
    }

    public void verifyMatchesPointerProperties(MotionEvent motionEvent, int pointerIndex) {
      final PointerProperties that = new PointerProperties();
      motionEvent.getPointerProperties(pointerIndex, that);

      verifyMatchesPointerProperties(that);
    }
  }

  public static class PointerCoordsBuilder {
    private float x;
    private float y;
    private float pressure = 1.0f;
    private float size = 1.0f;
    private float touchMajor;
    private float touchMinor;
    private float toolMajor;
    private float toolMinor;
    private float orientation;

    public PointerCoordsBuilder withPressure(float pressure) {
      this.pressure = pressure;
      return this;
    }

    public PointerCoordsBuilder withSize(float size) {
      this.size = size;
      return this;
    }

    public PointerCoordsBuilder withTouch(float touchMajor, float touchMinor) {
      this.touchMajor = touchMajor;
      this.touchMinor = touchMinor;
      return this;
    }

    public PointerCoordsBuilder withTool(float toolMajor, float toolMinor) {
      this.toolMajor = toolMajor;
      this.toolMinor = toolMinor;
      return this;
    }

    public PointerCoordsBuilder withOrientation(float orientation) {
      this.orientation = orientation;
      return this;
    }

    public PointerCoords build() {
      final PointerCoords pointerCoords = new PointerCoords();
      pointerCoords.x = x;
      pointerCoords.y = y;
      pointerCoords.pressure = pressure;
      pointerCoords.size = size;
      pointerCoords.touchMajor = touchMajor;
      pointerCoords.touchMinor = touchMinor;
      pointerCoords.toolMajor = toolMajor;
      pointerCoords.toolMinor = toolMinor;
      pointerCoords.orientation = orientation;
      return pointerCoords;
    }

    public void verifyMatches(MotionEvent that) {
      assertEquals("X coordinates should be the same", that.getX(), this.x, DELTA);
      assertEquals(
          "X coordinates should be the same", that.getAxisValue(MotionEvent.AXIS_X), this.x, DELTA);

      assertEquals("Y coordinates should be the same", that.getY(), this.y, DELTA);
      assertEquals(
          "Y coordinates should be the same", that.getAxisValue(MotionEvent.AXIS_Y), this.y, DELTA);

      assertEquals("Pressure should be the same", that.getPressure(), this.pressure, DELTA);
      assertEquals(
          "Pressure should be the same",
          that.getAxisValue(MotionEvent.AXIS_PRESSURE),
          this.pressure,
          DELTA);

      assertEquals("Size should be the same", that.getSize(), this.size, DELTA);
      assertEquals(
          "Size should be the same", that.getAxisValue(MotionEvent.AXIS_SIZE), this.size, DELTA);

      assertEquals("Touch major should be the same", that.getTouchMajor(), this.touchMajor, DELTA);
      assertEquals(
          "Touch major should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOUCH_MAJOR),
          this.touchMajor,
          DELTA);

      assertEquals("Touch minor should be the same", that.getTouchMinor(), this.touchMinor, DELTA);
      assertEquals(
          "Touch minor should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOUCH_MINOR),
          this.touchMinor,
          DELTA);

      assertEquals("Tool major should be the same", that.getToolMajor(), this.toolMajor, DELTA);
      assertEquals(
          "Tool major should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOOL_MAJOR),
          this.toolMajor,
          DELTA);

      assertEquals("Tool minor should be the same", that.getToolMinor(), this.toolMinor, DELTA);
      assertEquals(
          "Tool minor should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOOL_MINOR),
          this.toolMinor,
          DELTA);

      assertEquals(
          "Orientation should be the same", that.getOrientation(), this.orientation, DELTA);
      assertEquals(
          "Orientation should be the same",
          that.getAxisValue(MotionEvent.AXIS_ORIENTATION),
          this.orientation,
          DELTA);
    }

    public void verifyMatches(MotionEvent that, int pointerIndex) {
      assertEquals("X coordinates should be the same", that.getX(pointerIndex), this.x, DELTA);
      assertEquals(
          "X coordinates should be the same",
          that.getAxisValue(MotionEvent.AXIS_X, pointerIndex),
          this.x,
          DELTA);

      assertEquals("Y coordinates should be the same", that.getY(pointerIndex), this.y, DELTA);
      assertEquals(
          "Y coordinates should be the same",
          that.getAxisValue(MotionEvent.AXIS_Y, pointerIndex),
          this.y,
          DELTA);

      assertEquals(
          "Pressure should be the same", that.getPressure(pointerIndex), this.pressure, DELTA);
      assertEquals(
          "Pressure should be the same",
          that.getAxisValue(MotionEvent.AXIS_PRESSURE, pointerIndex),
          this.pressure,
          DELTA);

      assertEquals("Size should be the same", that.getSize(pointerIndex), this.size, DELTA);
      assertEquals(
          "Size should be the same",
          that.getAxisValue(MotionEvent.AXIS_SIZE, pointerIndex),
          this.size,
          DELTA);

      assertEquals(
          "Touch major should be the same",
          that.getTouchMajor(pointerIndex),
          this.touchMajor,
          DELTA);
      assertEquals(
          "Touch major should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOUCH_MAJOR, pointerIndex),
          this.touchMajor,
          DELTA);

      assertEquals(
          "Touch minor should be the same",
          that.getTouchMinor(pointerIndex),
          this.touchMinor,
          DELTA);
      assertEquals(
          "Touch minor should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOUCH_MINOR, pointerIndex),
          this.touchMinor,
          DELTA);

      assertEquals(
          "Tool major should be the same", that.getToolMajor(pointerIndex), this.toolMajor, DELTA);
      assertEquals(
          "Tool major should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOOL_MAJOR, pointerIndex),
          this.toolMajor,
          DELTA);

      assertEquals(
          "Tool minor should be the same", that.getToolMinor(pointerIndex), this.toolMinor, DELTA);
      assertEquals(
          "Tool minor should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOOL_MINOR, pointerIndex),
          this.toolMinor,
          DELTA);

      assertEquals(
          "Orientation should be the same",
          that.getOrientation(pointerIndex),
          this.orientation,
          DELTA);
      assertEquals(
          "Orientation should be the same",
          that.getAxisValue(MotionEvent.AXIS_ORIENTATION, pointerIndex),
          this.orientation,
          DELTA);
    }

    public void verifyMatchesHistorical(MotionEvent that, int position) {
      assertEquals(
          "X coordinates should be the same", that.getHistoricalX(position), this.x, DELTA);
      assertEquals(
          "X coordinates should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_X, position),
          this.x,
          DELTA);

      assertEquals(
          "Y coordinates should be the same", that.getHistoricalY(position), this.y, DELTA);
      assertEquals(
          "Y coordinates should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_Y, position),
          this.y,
          DELTA);

      assertEquals(
          "Pressure should be the same",
          that.getHistoricalPressure(position),
          this.pressure,
          DELTA);
      assertEquals(
          "Pressure should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_PRESSURE, position),
          this.pressure,
          DELTA);

      assertEquals("Size should be the same", that.getHistoricalSize(position), this.size, DELTA);
      assertEquals(
          "Size should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_SIZE, position),
          this.size,
          DELTA);

      assertEquals(
          "Touch major should be the same",
          that.getHistoricalTouchMajor(position),
          this.touchMajor,
          DELTA);
      assertEquals(
          "Touch major should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOUCH_MAJOR, position),
          this.touchMajor,
          DELTA);

      assertEquals(
          "Touch minor should be the same",
          that.getHistoricalTouchMinor(position),
          this.touchMinor,
          DELTA);
      assertEquals(
          "Touch minor should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOUCH_MINOR, position),
          this.touchMinor,
          DELTA);

      assertEquals(
          "Tool major should be the same",
          that.getHistoricalToolMajor(position),
          this.toolMajor,
          DELTA);
      assertEquals(
          "Tool major should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOOL_MAJOR, position),
          this.toolMajor,
          DELTA);

      assertEquals(
          "Tool minor should be the same",
          that.getHistoricalToolMinor(position),
          this.toolMinor,
          DELTA);
      assertEquals(
          "Tool minor should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOOL_MINOR, position),
          this.toolMinor,
          DELTA);

      assertEquals(
          "Orientation should be the same",
          that.getHistoricalOrientation(position),
          this.orientation,
          DELTA);
      assertEquals(
          "Orientation should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_ORIENTATION, position),
          this.orientation,
          DELTA);
    }

    public void verifyMatchesHistorical(MotionEvent that, int pointerIndex, int position) {
      assertEquals(
          "X coordinates should be the same",
          that.getHistoricalX(pointerIndex, position),
          this.x,
          DELTA);
      assertEquals(
          "X coordinates should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_X, pointerIndex, position),
          this.x,
          DELTA);

      assertEquals(
          "Y coordinates should be the same",
          that.getHistoricalY(pointerIndex, position),
          this.y,
          DELTA);
      assertEquals(
          "Y coordinates should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_Y, pointerIndex, position),
          this.y,
          DELTA);

      assertEquals(
          "Pressure should be the same",
          that.getHistoricalPressure(pointerIndex, position),
          this.pressure,
          DELTA);
      assertEquals(
          "Pressure should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_PRESSURE, pointerIndex, position),
          this.pressure,
          DELTA);

      assertEquals(
          "Size should be the same",
          that.getHistoricalSize(pointerIndex, position),
          this.size,
          DELTA);
      assertEquals(
          "Size should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_SIZE, pointerIndex, position),
          this.size,
          DELTA);

      assertEquals(
          "Touch major should be the same",
          that.getHistoricalTouchMajor(pointerIndex, position),
          this.touchMajor,
          DELTA);
      assertEquals(
          "Touch major should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOUCH_MAJOR, pointerIndex, position),
          this.touchMajor,
          DELTA);

      assertEquals(
          "Touch minor should be the same",
          that.getHistoricalTouchMinor(pointerIndex, position),
          this.touchMinor,
          DELTA);
      assertEquals(
          "Touch minor should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOUCH_MINOR, pointerIndex, position),
          this.touchMinor,
          DELTA);

      assertEquals(
          "Tool major should be the same",
          that.getHistoricalToolMajor(pointerIndex, position),
          this.toolMajor,
          DELTA);
      assertEquals(
          "Tool major should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOOL_MAJOR, pointerIndex, position),
          this.toolMajor,
          DELTA);

      assertEquals(
          "Tool minor should be the same",
          that.getHistoricalToolMinor(pointerIndex, position),
          this.toolMinor,
          DELTA);
      assertEquals(
          "Tool minor should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_TOOL_MINOR, pointerIndex, position),
          this.toolMinor,
          DELTA);

      assertEquals(
          "Orientation should be the same",
          that.getHistoricalOrientation(pointerIndex, position),
          this.orientation,
          DELTA);
      assertEquals(
          "Orientation should be the same",
          that.getHistoricalAxisValue(MotionEvent.AXIS_ORIENTATION, pointerIndex, position),
          this.orientation,
          DELTA);
    }

    public void verifyMatchesPointerCoords(PointerCoords that) {
      assertEquals("X coordinates should be the same", that.x, this.x, DELTA);
      assertEquals(
          "X coordinates should be the same", that.getAxisValue(MotionEvent.AXIS_X), this.x, DELTA);

      assertEquals("Y coordinates should be the same", that.y, this.y, DELTA);
      assertEquals(
          "Y coordinates should be the same", that.getAxisValue(MotionEvent.AXIS_Y), this.y, DELTA);

      assertEquals("Pressure should be the same", that.pressure, this.pressure, DELTA);
      assertEquals(
          "Pressure should be the same",
          that.getAxisValue(MotionEvent.AXIS_PRESSURE),
          this.pressure,
          DELTA);

      assertEquals("Size should be the same", that.size, this.size, DELTA);
      assertEquals(
          "Size should be the same", that.getAxisValue(MotionEvent.AXIS_SIZE), this.size, DELTA);

      assertEquals("Touch major should be the same", that.touchMajor, this.touchMajor, DELTA);
      assertEquals(
          "Touch major should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOUCH_MAJOR),
          this.touchMajor,
          DELTA);

      assertEquals("Touch minor should be the same", that.touchMinor, this.touchMinor, DELTA);
      assertEquals(
          "Touch minor should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOUCH_MINOR),
          this.touchMinor,
          DELTA);

      assertEquals("Tool major should be the same", that.toolMajor, this.toolMajor, DELTA);
      assertEquals(
          "Tool major should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOOL_MAJOR),
          this.toolMajor,
          DELTA);

      assertEquals("Tool minor should be the same", that.toolMinor, this.toolMinor, DELTA);
      assertEquals(
          "Tool minor should be the same",
          that.getAxisValue(MotionEvent.AXIS_TOOL_MINOR),
          this.toolMinor,
          DELTA);

      assertEquals("Orientation should be the same", that.orientation, this.orientation, DELTA);
      assertEquals(
          "Orientation should be the same",
          that.getAxisValue(MotionEvent.AXIS_ORIENTATION),
          this.orientation,
          DELTA);
    }

    public void verifyMatchesPointerCoords(MotionEvent motionEvent, int pointerIndex) {
      final PointerCoords that = new PointerCoords();
      motionEvent.getPointerCoords(pointerIndex, that);

      verifyMatchesPointerCoords(that);
    }

    public void verifyMatchesHistoricalPointerCoords(
        MotionEvent motionEvent, int pointerIndex, int pos) {
      final PointerCoords that = new PointerCoords();
      motionEvent.getHistoricalPointerCoords(pointerIndex, pos, that);

      verifyMatchesPointerCoords(that);
    }
  }
}
