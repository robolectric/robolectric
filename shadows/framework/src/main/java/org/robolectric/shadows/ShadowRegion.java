package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import com.google.common.base.Preconditions;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(Region.class)
public class ShadowRegion {
  @RealObject Region realRegion;

  private Area area;

  public static long nextId = 1;

  @Implementation
  protected void __constructor__() {
    area = new Area();
    reflector(RegionReflector.class, realRegion).__constructor__();
  }

  @Implementation
  protected void __constructor__(Region src) {
    area = new Area(getAwtArea(src));
    reflector(RegionReflector.class, realRegion).__constructor__(src);
  }

  @Implementation
  protected void __constructor__(Rect r) {
    area = new Area(new Rectangle2D.Double(r.left, r.top, r.width(), r.height()));
    reflector(RegionReflector.class, realRegion).__constructor__(r);
  }

  @Implementation
  protected void __constructor__(int left, int top, int right, int bottom) {
    area = new Area(new Rectangle2D.Double(left, top, right - left, bottom - top));
    reflector(RegionReflector.class, realRegion).__constructor__(left, top, right, bottom);
  }

  /**
   * The real {@link Region#equals(Object)} calls into native code, which is a no-op in Robolectric.
   */
  @Override
  @Implementation
  @SuppressWarnings("EqualsHashCode")
  public boolean equals(Object obj) {
    if (obj == realRegion) {
      return true;
    }
    if (!(obj instanceof Region)) {
      return false;
    }
    Region other = (Region) obj;
    return area.equals(getAwtArea(other));
  }

  @HiddenApi
  @Implementation
  protected static long nativeConstructor() {
    return nextId++;
  }

  @Implementation
  protected boolean isEmpty() {
    return area.isEmpty();
  }

  @Implementation
  protected Rect getBounds() {
    Rectangle2D bounds = area.getBounds2D();
    return new Rect(
        (int) Math.floor(bounds.getX()),
        (int) Math.floor(bounds.getY()),
        (int) Math.ceil(bounds.getMaxX()),
        (int) Math.ceil(bounds.getMaxY()));
  }

  @Implementation
  protected boolean getBounds(Rect r) {
    Rectangle2D bounds = area.getBounds2D();
    r.left = (int) Math.floor(bounds.getX());
    r.top = (int) Math.floor(bounds.getY());
    r.right = (int) Math.ceil(bounds.getMaxX());
    r.bottom = (int) Math.ceil(bounds.getMaxY());
    return !area.isEmpty();
  }

  @Implementation
  protected Path getBoundaryPath() {
    Path2D path2d = new Path2D.Double(area);
    return createAndroidPath(path2d);
  }

  @Implementation
  protected void setEmpty() {
    area.reset();
  }

  @Implementation
  protected boolean set(Region src) {
    area.reset();
    area.add(getAwtArea(src));
    return true;
  }

  @Implementation
  protected boolean set(Rect r) {
    area.reset();
    area.add(new Area(new Rectangle2D.Double(r.left, r.top, r.width(), r.height())));
    return true;
  }

  @Implementation
  protected boolean set(int left, int top, int right, int bottom) {
    area.reset();
    area.add(new Area(new Rectangle2D.Double(left, top, right - left, bottom - top)));
    return true;
  }

  @Implementation
  protected boolean setPath(Path path, Region clip) {
    Preconditions.checkNotNull(clip);
    Path2D pathShape = getAwtPath(path);
    Area newArea = new Area(pathShape);
    Area clipArea = getAwtArea(clip);

    newArea.intersect(clipArea);

    this.area = newArea;
    return !area.isEmpty();
  }

  @Implementation
  protected boolean isRect() {
    Rectangle bounds = area.getBounds();
    Area boundsArea = new Area(bounds);
    return boundsArea.equals(area);
  }

  @Implementation
  protected boolean contains(int x, int y) {
    return area.contains(x, y);
  }

  @Implementation
  protected boolean quickContains(int left, int top, int right, int bottom) {
    return area.contains(new Rectangle2D.Double(left, top, right - left, bottom - top));
  }

  @Implementation
  protected boolean quickReject(Rect r) {
    Rectangle2D rect = new Rectangle2D.Double(r.left, r.top, r.width(), r.height());
    return !area.intersects(rect);
  }

  @Implementation
  protected boolean quickReject(int left, int top, int right, int bottom) {
    Rectangle2D rect = new Rectangle2D.Double(left, top, right - left, bottom - top);
    return !area.intersects(rect);
  }

  @Implementation
  protected boolean quickReject(Region r) {
    Rect thisBounds = getBounds();
    Rect otherBounds = r.getBounds();

    if (!Rect.intersects(thisBounds, otherBounds)) {
      return true;
    }

    Area otherArea = getAwtArea(r);
    Area intersection = new Area(area);
    intersection.intersect(otherArea);

    return intersection.isEmpty();
  }

  @Implementation
  protected boolean op(Rect rect, Op op) {
    Area other = new Area(new Rectangle2D.Double(rect.left, rect.top, rect.width(), rect.height()));
    return applyOp(other, op);
  }

  @Implementation
  protected boolean op(Region region, Op op) {
    Area other = getAwtArea(region);
    return applyOp(other, op);
  }

  @Implementation
  protected boolean op(Rect rect, Region region, Op op) {
    Area rectArea =
        new Area(new Rectangle2D.Double(rect.left, rect.top, rect.width(), rect.height()));
    Area regionArea = getAwtArea(region);

    this.area = new Area(rectArea);
    return applyOp(regionArea, op);
  }

  @Implementation
  protected boolean op(Region region1, Region region2, Op op) {
    Area area1 = getAwtArea(region1);
    Area area2 = getAwtArea(region2);

    this.area = new Area(area1);
    return applyOp(area2, op);
  }

  private boolean applyOp(Area other, Op op) {
    Area sourceArea;

    switch (op) {
      case DIFFERENCE:
        area.subtract(other);
        break;
      case INTERSECT:
        area.intersect(other);
        break;
      case UNION:
        area.add(other);
        break;
      case XOR:
        area.exclusiveOr(other);
        break;
      case REPLACE:
        area.reset();
        area.add(other);
        break;
      case REVERSE_DIFFERENCE:
        sourceArea = new Area(other);
        sourceArea.subtract(area);
        this.area = sourceArea;
        break;
    }
    return !area.isEmpty();
  }

  @Implementation
  protected void translate(int dx, int dy) {
    area.transform(AffineTransform.getTranslateInstance(dx, dy));
  }

  @Implementation
  protected void translate(int dx, int dy, Region dst) {
    Area translatedArea = new Area(area);
    translatedArea.transform(AffineTransform.getTranslateInstance(dx, dy));
    ((ShadowRegion) Shadow.extract(dst)).area = translatedArea;
  }

  private static Area getAwtArea(Region region) {
    return ((ShadowRegion) Shadow.extract(region)).area;
  }

  private static Path2D getAwtPath(Path path) {
    return ((ShadowLegacyPath) Shadow.extract(path)).getJavaShape();
  }

  private static Path createAndroidPath(Path2D path2d) {
    Path androidPath = new Path();
    PathIterator pi = path2d.getPathIterator(null); // 'null' for no transform
    float[] coords = new float[6]; // Max coordinates needed is 6 (for cubic curve)

    while (!pi.isDone()) {
      int segmentType = pi.currentSegment(coords);

      switch (segmentType) {
        case PathIterator.SEG_MOVETO:
          androidPath.moveTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_LINETO:
          androidPath.lineTo(coords[0], coords[1]);
          break;
        case PathIterator.SEG_QUADTO:
          androidPath.quadTo(coords[0], coords[1], coords[2], coords[3]);
          break;
        case PathIterator.SEG_CUBICTO:
          androidPath.cubicTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          break;
        case PathIterator.SEG_CLOSE:
          androidPath.close();
          break;
      }
      pi.next();
    }
    return androidPath;
  }

  @ForType(Region.class)
  interface RegionReflector {
    @Override
    @Direct
    boolean equals(Object obj);

    @Direct
    void __constructor__();

    @Direct
    void __constructor__(Region src);

    @Direct
    void __constructor__(Rect r);

    @Direct
    void __constructor__(int left, int top, int right, int bottom);
  }

}
