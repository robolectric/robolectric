package com.xtremelabs.robolectric.shadows;

import android.graphics.Path;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;
import static com.xtremelabs.robolectric.shadows.ShadowPath.Point.Type.MOVE_TO;


/**
 * Shadow of {@code Path} that contains a simplified implementation of the original class that only supports
 * straight-line {@code Path}s.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Path.class)
public class ShadowPath {
    private List<Point> points = new ArrayList<Point>();
    private Point wasMovedTo;
    private String quadDescription = "";

    public void __constructor__(Path path) {
        points = new ArrayList<Point>(shadowOf(path).getPoints());
        wasMovedTo = shadowOf(path).wasMovedTo;
        quadDescription = shadowOf(path).quadDescription;
    }

    @Implementation
    public void moveTo(float x, float y) {
        Point p = new Point(x, y, MOVE_TO);
        points.add(p);
        wasMovedTo = p;
    }

    @Implementation
    public void lineTo(float x, float y) {
        Point point = new Point(x, y, LINE_TO);
        points.add(point);
    }

    @Implementation
    public void quadTo(float x1, float y1, float x2, float y2) {
        quadDescription = "Add a quadratic bezier from last point, approaching (" + x1 + "," + y1 + "), " +
                "ending at (" + x2 + "," + y2 + ")";
    }

    @Implementation
    public void reset() {
        points.clear();
        wasMovedTo = null;
        quadDescription = "";
    }

    public String getQuadDescription() {
        return quadDescription;
    }

    /**
     * Non-Android accessor.
     *
     * @return all the points that have been added to the {@code Path}
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * Non-Android accessor.
     *
     * @return whether the {@link #moveTo(float, float)} method was called
     */
    public Point getWasMovedTo() {
        return wasMovedTo;
    }

    public static class Point {
        float x, y;
        private Type type;

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
}
