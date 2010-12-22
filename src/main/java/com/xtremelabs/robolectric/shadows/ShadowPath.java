package com.xtremelabs.robolectric.shadows;

import android.graphics.Path;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

/**
 * Shadow of {@code Path} that contains a simplified implementation of the original class that only supports
 * straight-line {@code Path}s.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Path.class)
public class ShadowPath {
    private List<Point> points = new ArrayList<Point>();
    private Point wasMovedTo;

    @Implementation
    public void moveTo(float x, float y) {
        Point p = new Point(x, y);
        points.add(p);
        wasMovedTo = p;
    }

    @Implementation
    public void lineTo(float x, float y) {
        points.add(new Point(x, y));
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

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (Float.compare(point.x, x) != 0) return false;
            if (Float.compare(point.y, y) != 0) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
            result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Point(" + x + "," + y + ")";
        }
    }
}
