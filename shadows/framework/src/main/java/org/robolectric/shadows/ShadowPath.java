package org.robolectric.shadows;


import android.graphics.Path;
import android.graphics.RectF;
import java.util.List;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.ShadowPicker;
import org.robolectric.shadows.ShadowPath.Picker;

/** Base class for {@link ShadowPath} classes. */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(value = Path.class, shadowPicker = Picker.class)
public abstract class ShadowPath {

  /**
   * @return all the points that have been added to the {@code Path}
   */
  public abstract List<Point> getPoints();

  /**
   * Fills the given {@link RectF} with the path bounds.
   *
   * @param bounds the RectF to be filled.
   */
  public abstract void fillBounds(RectF bounds);

  public static class Point {
    private final float x;
    private final float y;
    private final Type type;

    public enum Type {
      MOVE_TO,
      LINE_TO
    }

    public Point(float x, float y, Type type) {
      this.x = x;
      this.y = y;
      this.type = type;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Point)) return false;

      Point point = (Point) o;

      if (Float.compare(point.x, x) != 0) return false;
      if (Float.compare(point.y, y) != 0) return false;
      if (type != point.type) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
      result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
      result = 31 * result + (type != null ? type.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "Point(" + x + "," + y + "," + type + ")";
    }

    public float getX() {
      return x;
    }

    public float getY() {
      return y;
    }

    public Type getType() {
      return type;
    }
  }

  /** A {@link ShadowPicker} that always selects the legacy ShadowPath */
  public static class Picker implements ShadowPicker<ShadowPath> {
    @Override
    public Class<? extends ShadowPath> pickShadowClass() {
      return ShadowLegacyPath.class;
    }
  }
}
