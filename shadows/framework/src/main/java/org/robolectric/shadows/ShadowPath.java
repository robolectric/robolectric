package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.shadow.api.Shadow.extract;
import static org.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;
import static org.robolectric.shadows.ShadowPath.Point.Type.MOVE_TO;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.util.Log;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * The shadow only supports straight-line paths.
 */
@SuppressWarnings({"UnusedDeclaration"})
@Implements(Path.class)
public class ShadowPath {
  private static final String TAG = ShadowPath.class.getSimpleName();
  private static final float EPSILON = 1e-4f;

  @RealObject private Path realObject;

  private List<Point> points = new ArrayList<>();
  private Point wasMovedTo;

  private float mLastX = 0;
  private float mLastY = 0;
  private Path2D mPath = new Path2D.Double();
  private boolean mCachedIsEmpty = true;
  private Path.FillType mFillType = Path.FillType.WINDING;
  protected boolean isSimplePath;

  @Implementation
  protected void __constructor__(Path path) {
    ShadowPath shadowPath = extract(path);
    points = new ArrayList<>(shadowPath.getPoints());
  }

  Path2D getJavaShape() {
    return mPath;
  }

  @Implementation
  protected void moveTo(float x, float y) {
    mPath.moveTo(mLastX = x, mLastY = y);

    // Legacy recording behavior
    Point p = new Point(x, y, MOVE_TO);
    points.add(p);
  }

  @Implementation
  protected void lineTo(float x, float y) {
    if (!hasPoints()) {
      mPath.moveTo(mLastX = 0, mLastY = 0);
    }
    mPath.lineTo(mLastX = x, mLastY = y);

    // Legacy recording behavior
    Point point = new Point(x, y, LINE_TO);
    points.add(point);
  }

  @Implementation
  protected void quadTo(float x1, float y1, float x2, float y2) {
    isSimplePath = false;
    if (!hasPoints()) {
      moveTo(0, 0);
    }
    mPath.quadTo(x1, y1, mLastX = x2, mLastY = y2);
  }

  @Implementation
  protected void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
    if (!hasPoints()) {
      mPath.moveTo(0, 0);
    }
    mPath.curveTo(x1, y1, x2, y2, mLastX = x3, mLastY = y3);
  }

  private boolean hasPoints() {
    return !mPath.getPathIterator(null).isDone();
  }

  @Implementation
  protected void reset() {
    mPath.reset();
    mLastX = 0;
    mLastY = 0;

    // Legacy recording behavior
    points.clear();
  }

  @Implementation(minSdk = LOLLIPOP)
  protected float[] approximate(float acceptableError) {
    PathIterator iterator = mPath.getPathIterator(null, acceptableError);

    float segment[] = new float[6];
    float totalLength = 0;
    ArrayList<Point2D.Float> points = new ArrayList<Point2D.Float>();
    Point2D.Float previousPoint = null;
    while (!iterator.isDone()) {
      int type = iterator.currentSegment(segment);
      Point2D.Float currentPoint = new Point2D.Float(segment[0], segment[1]);
      // MoveTo shouldn't affect the length
      if (previousPoint != null && type != PathIterator.SEG_MOVETO) {
        totalLength += (float) currentPoint.distance(previousPoint);
      }
      previousPoint = currentPoint;
      points.add(currentPoint);
      iterator.next();
    }

    int nPoints = points.size();
    float[] result = new float[nPoints * 3];
    previousPoint = null;
    // Distance that we've covered so far. Used to calculate the fraction of the path that
    // we've covered up to this point.
    float walkedDistance = .0f;
    for (int i = 0; i < nPoints; i++) {
      Point2D.Float point = points.get(i);
      float distance = previousPoint != null ? (float) previousPoint.distance(point) : .0f;
      walkedDistance += distance;
      result[i * 3] = walkedDistance / totalLength;
      result[i * 3 + 1] = point.x;
      result[i * 3 + 2] = point.y;

      previousPoint = point;
    }

    return result;
  }

  /**
   * @return all the points that have been added to the {@code Path}
   */
  public List<Point> getPoints() {
    return points;
  }

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

  @Implementation
  protected void rewind() {
    // call out to reset since there's nothing to optimize in
    // terms of data structs.
    reset();
  }

  @Implementation
  protected void set(Path src) {
    mPath.reset();

    ShadowPath shadowSrc = extract(src);
    setFillType(shadowSrc.mFillType);
    mPath.append(shadowSrc.mPath, false /*connect*/);
  }

  @Implementation(minSdk = KITKAT)
  protected boolean op(Path path1, Path path2, Path.Op op) {
    Log.w(TAG, "android.graphics.Path#op() not supported yet.");
    return false;
  }

  @Implementation(minSdk = LOLLIPOP)
  protected boolean isConvex() {
    Log.w(TAG, "android.graphics.Path#isConvex() not supported yet.");
    return true;
  }

  @Implementation
  protected Path.FillType getFillType() {
    return mFillType;
  }

  @Implementation
  protected void setFillType(Path.FillType fillType) {
    mFillType = fillType;
    mPath.setWindingRule(getWindingRule(fillType));
  }

  /**
   * Returns the Java2D winding rules matching a given Android {@link FillType}.
   *
   * @param type the android fill type
   * @return the matching java2d winding rule.
   */
  private static int getWindingRule(Path.FillType type) {
    switch (type) {
      case WINDING:
      case INVERSE_WINDING:
        return GeneralPath.WIND_NON_ZERO;
      case EVEN_ODD:
      case INVERSE_EVEN_ODD:
        return GeneralPath.WIND_EVEN_ODD;

      default:
        assert false;
        return GeneralPath.WIND_NON_ZERO;
    }
  }

  @Implementation
  protected boolean isInverseFillType() {
    throw new UnsupportedOperationException("isInverseFillType");
  }

  @Implementation
  protected void toggleInverseFillType() {
    throw new UnsupportedOperationException("toggleInverseFillType");
  }

  @Implementation
  protected boolean isEmpty() {
    if (!mCachedIsEmpty) {
      return false;
    }

    float[] coords = new float[6];
    mCachedIsEmpty = Boolean.TRUE;
    for (PathIterator it = mPath.getPathIterator(null); !it.isDone(); it.next()) {
      int type = it.currentSegment(coords);
      // if (type != PathIterator.SEG_MOVETO) {
      // Once we know that the path is not empty, we do not need to check again unless
      // Path#reset is called.
      mCachedIsEmpty = false;
      return false;
      // }
    }

    return true;
  }

  @Implementation
  protected boolean isRect(RectF rect) {
    // create an Area that can test if the path is a rect
    Area area = new Area(mPath);
    if (area.isRectangular()) {
      if (rect != null) {
        fillBounds(rect);
      }

      return true;
    }

    return false;
  }

  @Implementation
  protected void computeBounds(RectF bounds, boolean exact) {
    fillBounds(bounds);
  }

  @Implementation
  protected void incReserve(int extraPtCount) {
    throw new UnsupportedOperationException("incReserve");
  }

  @Implementation
  protected void rMoveTo(float dx, float dy) {
    dx += mLastX;
    dy += mLastY;
    mPath.moveTo(mLastX = dx, mLastY = dy);
  }

  @Implementation
  protected void rLineTo(float dx, float dy) {
    if (!hasPoints()) {
      mPath.moveTo(mLastX = 0, mLastY = 0);
    }

    if (Math.abs(dx) < EPSILON && Math.abs(dy) < EPSILON) {
      // The delta is so small that this shouldn't generate a line
      return;
    }

    dx += mLastX;
    dy += mLastY;
    mPath.lineTo(mLastX = dx, mLastY = dy);
  }

  @Implementation
  protected void rQuadTo(float dx1, float dy1, float dx2, float dy2) {
    if (!hasPoints()) {
      mPath.moveTo(mLastX = 0, mLastY = 0);
    }
    dx1 += mLastX;
    dy1 += mLastY;
    dx2 += mLastX;
    dy2 += mLastY;
    mPath.quadTo(dx1, dy1, mLastX = dx2, mLastY = dy2);
  }

  @Implementation
  protected void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
    if (!hasPoints()) {
      mPath.moveTo(mLastX = 0, mLastY = 0);
    }
    x1 += mLastX;
    y1 += mLastY;
    x2 += mLastX;
    y2 += mLastY;
    x3 += mLastX;
    y3 += mLastY;
    mPath.curveTo(x1, y1, x2, y2, mLastX = x3, mLastY = y3);
  }

  @Implementation
  protected void arcTo(RectF oval, float startAngle, float sweepAngle) {
    arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, false);
  }

  @Implementation
  protected void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) {
    arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void arcTo(
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweepAngle,
      boolean forceMoveTo) {
    isSimplePath = false;
    Arc2D arc =
        new Arc2D.Float(
            left, top, right - left, bottom - top, -startAngle, -sweepAngle, Arc2D.OPEN);
    mPath.append(arc, true /*connect*/);

    resetLastPointFromPath();
  }

  @Implementation
  protected void close() {
    if (!hasPoints()) {
      mPath.moveTo(mLastX = 0, mLastY = 0);
    }
    mPath.closePath();
  }

  @Implementation
  protected void addRect(RectF rect, Direction dir) {
    addRect(rect.left, rect.top, rect.right, rect.bottom, dir);
  }

  @Implementation
  protected void addRect(float left, float top, float right, float bottom, Path.Direction dir) {
    moveTo(left, top);

    switch (dir) {
      case CW:
        lineTo(right, top);
        lineTo(right, bottom);
        lineTo(left, bottom);
        break;
      case CCW:
        lineTo(left, bottom);
        lineTo(right, bottom);
        lineTo(right, top);
        break;
    }

    close();

    resetLastPointFromPath();
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void addOval(float left, float top, float right, float bottom, Path.Direction dir) {
    mPath.append(new Ellipse2D.Float(left, top, right - left, bottom - top), false);
  }

  @Implementation
  protected void addCircle(float x, float y, float radius, Path.Direction dir) {
    mPath.append(new Ellipse2D.Float(x - radius, y - radius, radius * 2, radius * 2), false);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void addArc(
      float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
    mPath.append(
        new Arc2D.Float(
            left, top, right - left, bottom - top, -startAngle, -sweepAngle, Arc2D.OPEN),
        false);
  }

  @Implementation(minSdk = JELLY_BEAN)
  protected void addRoundRect(RectF rect, float rx, float ry, Direction dir) {
    addRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, dir);
  }

  @Implementation(minSdk = JELLY_BEAN)
  protected void addRoundRect(RectF rect, float[] radii, Direction dir) {
    if (rect == null) {
      throw new NullPointerException("need rect parameter");
    }
    addRoundRect(rect.left, rect.top, rect.right, rect.bottom, radii, dir);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void addRoundRect(
      float left, float top, float right, float bottom, float rx, float ry, Path.Direction dir) {
    mPath.append(
        new RoundRectangle2D.Float(left, top, right - left, bottom - top, rx * 2, ry * 2), false);
  }

  @Implementation(minSdk = LOLLIPOP)
  protected void addRoundRect(
      float left, float top, float right, float bottom, float[] radii, Path.Direction dir) {
    if (radii.length < 8) {
      throw new ArrayIndexOutOfBoundsException("radii[] needs 8 values");
    }
    isSimplePath = false;

    float[] cornerDimensions = new float[radii.length];
    for (int i = 0; i < radii.length; i++) {
      cornerDimensions[i] = 2 * radii[i];
    }
    mPath.append(
        new RoundRectangle(left, top, right - left, bottom - top, cornerDimensions), false);
  }

  @Implementation
  protected void addPath(Path src, float dx, float dy) {
    isSimplePath = false;
    ShadowPath.addPath(realObject, src, AffineTransform.getTranslateInstance(dx, dy));
  }

  @Implementation
  protected void addPath(Path src) {
    isSimplePath = false;
    ShadowPath.addPath(realObject, src, null);
  }

  @Implementation
  protected void addPath(Path src, Matrix matrix) {
    if (matrix == null) {
      return;
    }
    ShadowPath shadowSrc = extract(src);
    if (!shadowSrc.isSimplePath) isSimplePath = false;

    ShadowMatrix shadowMatrix = extract(matrix);
    ShadowPath.addPath(realObject, src, shadowMatrix.getAffineTransform());
  }

  private static void addPath(Path destPath, Path srcPath, AffineTransform transform) {
    if (destPath == null) {
      return;
    }

    if (srcPath == null) {
      return;
    }

    ShadowPath shadowDestPath = extract(destPath);
    ShadowPath shadowSrcPath = extract(srcPath);
    if (transform != null) {
      shadowDestPath.mPath.append(shadowSrcPath.mPath.getPathIterator(transform), false);
    } else {
      shadowDestPath.mPath.append(shadowSrcPath.mPath, false);
    }
  }

  @Implementation
  protected void offset(float dx, float dy, Path dst) {
    if (dst != null) {
      dst.set(realObject);
    } else {
      dst = realObject;
    }
    dst.offset(dx, dy);
  }

  @Implementation
  protected void offset(float dx, float dy) {
    GeneralPath newPath = new GeneralPath();

    PathIterator iterator = mPath.getPathIterator(new AffineTransform(0, 0, dx, 0, 0, dy));

    newPath.append(iterator, false /*connect*/);
    mPath = newPath;
  }

  @Implementation
  protected void setLastPoint(float dx, float dy) {
    mLastX = dx;
    mLastY = dy;
  }

  @Implementation
  protected void transform(Matrix matrix, Path dst) {
    ShadowMatrix shadowMatrix = extract(matrix);

    if (shadowMatrix.hasPerspective()) {
      Log.w(TAG, "android.graphics.Path#transform() only supports affine transformations.");
    }

    GeneralPath newPath = new GeneralPath();

    PathIterator iterator = mPath.getPathIterator(shadowMatrix.getAffineTransform());
    newPath.append(iterator, false /*connect*/);

    if (dst != null) {
      ShadowPath shadowPath = extract(dst);
      shadowPath.mPath = newPath;
    } else {
      mPath = newPath;
    }
  }

  @Implementation
  protected void transform(Matrix matrix) {
    transform(matrix, null);
  }

  /**
   * Fills the given {@link RectF} with the path bounds.
   *
   * @param bounds the RectF to be filled.
   */
  public void fillBounds(RectF bounds) {
    Rectangle2D rect = mPath.getBounds2D();
    bounds.left = (float) rect.getMinX();
    bounds.right = (float) rect.getMaxX();
    bounds.top = (float) rect.getMinY();
    bounds.bottom = (float) rect.getMaxY();
  }

  private void resetLastPointFromPath() {
    Point2D last = mPath.getCurrentPoint();
    mLastX = (float) last.getX();
    mLastY = (float) last.getY();
  }
}
