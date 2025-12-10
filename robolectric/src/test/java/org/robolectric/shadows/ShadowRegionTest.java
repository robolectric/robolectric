package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowRegionTest {
  private final Region regionA = new Region(0, 0, 10, 10);
  private final Region regionB = new Region(5, 5, 15, 15);

  @Test
  public void testEquals() {
    Region region = new Region(new Rect(0, 0, 100, 100));
    assertThat(region.equals(region)).isTrue();
  }

  @Test
  public void constructor_empty() {
    assertThat(new Region().isEmpty()).isTrue();
  }

  @Test
  public void constructor_points() {
    Region r = new Region(1, 2, 11, 12);
    Rect expectedBounds = new Rect(1, 2, 11, 12);
    assertThat(r.isEmpty()).isFalse();
    assertThat(r.getBounds()).isEqualTo(expectedBounds);
  }

  @Test
  public void constructor_copy() {
    Region copy = new Region(regionA);
    assertThat(copy.isEmpty()).isFalse();
    assertThat(copy.getBounds()).isEqualTo(regionA.getBounds());
    assertThat(copy.equals(regionA)).isTrue();
  }

  @Test
  public void setEmpty() {
    assertThat(regionA.isEmpty()).isFalse();
    regionA.setEmpty();
    assertThat(regionA.isEmpty()).isTrue();
  }

  @Test
  public void set_rect() {
    Region r = new Region();
    Rect newRect = new Rect(20, 20, 40, 40);
    assertThat(r.set(newRect)).isTrue();
    assertThat(r.getBounds()).isEqualTo(newRect);
  }

  @Test
  public void set_points() {
    Region r = new Region();
    assertThat(r.set(1, 1, 5, 5)).isTrue();
    assertThat(r.getBounds()).isEqualTo(new Rect(1, 1, 5, 5));
  }

  @Test
  public void set_region() {
    Region r = new Region();
    assertThat(r.set(regionB)).isTrue();
    assertThat(r.getBounds()).isEqualTo(regionB.getBounds());
  }

  @Test
  public void set_path() {
    Region clip = new Region(0, 0, 10, 10);
    Region region = new Region();
    Path path = new Path();
    path.addRect(0, 0, 10, 10, Path.Direction.CW);
    assertThat(region.setPath(path, clip)).isTrue();
    assertThat(region.getBoundaryPath().isEmpty()).isFalse();
  }

  @Test
  public void getBounds() {
    Rect boundsA = regionA.getBounds();
    assertThat(boundsA.left).isEqualTo(0);
    assertThat(boundsA.top).isEqualTo(0);
    assertThat(boundsA.right).isEqualTo(10);
    assertThat(boundsA.bottom).isEqualTo(10);

    Rect boundsB = new Rect();
    assertThat(regionB.getBounds(boundsB)).isTrue();
    assertThat(boundsB).isEqualTo(new Rect(5, 5, 15, 15));
  }

  @Test
  public void getBoundaryPath() {
    assertThat(regionA.getBoundaryPath().isEmpty()).isFalse();
    assertThat(new Region().getBoundaryPath().isEmpty()).isTrue();
  }

  @Test
  public void isEmpty() {
    assertThat(regionA.isEmpty()).isFalse();
    assertThat(new Region().isEmpty()).isTrue();
  }

  @Test
  public void isRect_simple() {
    assertThat(regionA.isRect()).isTrue();
    assertThat(new Region(10, 10, 11, 11).isRect()).isTrue();
  }

  @Test
  public void isRect_complex() {
    regionA.op(regionB, Op.INTERSECT);
    assertThat(regionA.isRect()).isTrue();

    Region region = new Region(0, 0, 10, 10);
    region.op(new Rect(2, 2, 8, 8), Op.DIFFERENCE);
    assertThat(region.isRect()).isFalse();
  }

  @Test
  public void contains() {
    assertThat(regionA.contains(5, 5)).isTrue();
    assertThat(regionA.contains(10, 10)).isFalse();
    assertThat(regionA.contains(-1, 5)).isFalse();
  }

  @Test
  public void quickContains() {
    assertThat(regionA.quickContains(1, 1, 9, 9)).isTrue();
    assertThat(regionA.quickContains(1, 1, 11, 11)).isFalse();
  }

  @Test
  public void quickReject() {
    Rect noOverlap = new Rect(11, 11, 15, 15);
    assertThat(regionA.quickReject(noOverlap)).isTrue();
    assertThat(regionA.quickReject(regionB)).isFalse();
    assertThat(regionA.quickReject(5, 5, 7, 7)).isFalse();
  }

  @Test
  public void op_union() {
    // A(0,0,10,10) UNION B(5,5,15,15) should be (0,0,15,15)
    assertThat(regionA.op(regionB, Op.UNION)).isTrue();
    Rect expectedBounds = new Rect(0, 0, 15, 15);
    assertThat(regionA.getBounds()).isEqualTo(expectedBounds);
  }

  @Test
  public void op_intersect() {
    // A(0,0,10,10) INTERSECT B(5,5,15,15) should be (5,5,10,10)
    assertThat(regionA.op(regionB, Op.INTERSECT)).isTrue();
    Rect expectedBounds = new Rect(5, 5, 10, 10);
    assertThat(regionA.getBounds()).isEqualTo(expectedBounds);
    assertThat(regionA.contains(6, 6)).isTrue();
    assertThat(regionA.contains(4, 4)).isFalse();
  }

  @Test
  public void op_difference() {
    // A(0,0,10,10) DIFFERENCE B(5,5,15,15). Result is L-shaped.
    assertThat(regionA.op(regionB, Op.DIFFERENCE)).isTrue();
    // Still bounded by (0, 0, 10, 10).
    assertThat(regionA.getBounds()).isEqualTo(new Rect(0, 0, 10, 10));
    assertThat(regionA.contains(1, 1)).isTrue();
    assertThat(regionA.contains(6, 6)).isFalse();
    assertThat(regionA.isRect()).isFalse();
  }

  @Test
  public void op_reverse_difference() {
    // A(0,0,10,10) becomes B(5,5,15,15) DIFFERENCE A(0,0,10,10). Result is the corner of B.
    assertThat(regionA.op(regionB, Op.REVERSE_DIFFERENCE)).isTrue();
    // Bounded by (10, 10, 15, 15).
    assertThat(regionA.getBounds()).isEqualTo(new Rect(5, 5, 15, 15));
    assertThat(regionA.contains(11, 11)).isTrue();
    assertThat(regionA.contains(9, 9)).isFalse();
  }

  @Test
  public void op_replace() {
    assertThat(regionA.op(regionB, Op.REPLACE)).isTrue();
    assertThat(regionA.getBounds()).isEqualTo(regionB.getBounds());
  }

  @Test
  public void op_xor() {
    // A XOR B is the non-overlapping parts (two L-shapes)
    assertThat(regionA.op(regionB, Op.XOR)).isTrue();
    assertThat(regionA.getBounds()).isEqualTo(new Rect(0, 0, 15, 15));
    assertThat(regionA.contains(1, 1)).isTrue(); // In non-overlapping part of A
    assertThat(regionA.contains(11, 11)).isTrue(); // In non-overlapping part of B
    assertThat(regionA.contains(6, 6)).isFalse(); // In the intersection (which is excluded)
    assertThat(regionA.isRect()).isFalse();
  }

  @Test
  public void translate_inplace() {
    regionA.translate(10, 20);
    Rect expectedBounds = new Rect(10, 20, 20, 30);
    assertThat(regionA.getBounds()).isEqualTo(expectedBounds);
    assertThat(regionA.contains(15, 25)).isTrue();
  }

  @Test
  public void translate_to_dst() {
    Region r = new Region();
    regionA.translate(10, 20, r);
    assertThat(regionA.getBounds()).isEqualTo(new Rect(0, 0, 10, 10));
    Rect expectedBounds = new Rect(10, 20, 20, 30);
    assertThat(r.getBounds()).isEqualTo(expectedBounds);
    assertThat(r.contains(15, 25)).isTrue();
  }

  @Test
  public void region_fromPath_contains() {
    Path squarePath = new Path();
    squarePath.addRect(5.0f, 5.0f, 15.0f, 15.0f, Path.Direction.CW);
    RectF pathBounds = new RectF();
    squarePath.computeBounds(pathBounds, true);
    Rect clipRect =
        new Rect(
            (int) Math.floor(pathBounds.left),
            (int) Math.floor(pathBounds.top),
            (int) Math.ceil(pathBounds.right),
            (int) Math.ceil(pathBounds.bottom));
    Region clipRegion = new Region(clipRect);
    Region squareRegion = new Region();
    squareRegion.setPath(squarePath, clipRegion);
    assertThat(squareRegion.getBounds()).isEqualTo(clipRect);
    assertThat(squareRegion.contains(10, 10)).isTrue();
    assertThat(squareRegion.contains(20, 20)).isFalse();
    assertThat(squareRegion.contains(0, 0)).isFalse();
  }
}
