package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;
import static org.robolectric.shadows.ShadowPath.Point.Type.MOVE_TO;

import android.graphics.Path;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowPathTest {

  private static final float ERROR_TOLERANCE = 0.5f;

  @Test
  public void testMoveTo() {
    Path path = new Path();
    path.moveTo(2, 3);
    path.moveTo(3, 4);

    List<ShadowPath.Point> moveToPoints = shadowOf(path).getPoints();
    assertEquals(2, moveToPoints.size());
    assertEquals(new ShadowPath.Point(2, 3, MOVE_TO), moveToPoints.get(0));
    assertEquals(new ShadowPath.Point(3, 4, MOVE_TO), moveToPoints.get(1));
  }

  @Test
  public void testLineTo() {
    Path path = new Path();
    path.lineTo(2, 3);
    path.lineTo(3, 4);

    List<ShadowPath.Point> lineToPoints = shadowOf(path).getPoints();
    assertEquals(2, lineToPoints.size());
    assertEquals(new ShadowPath.Point(2, 3, LINE_TO), lineToPoints.get(0));
    assertEquals(new ShadowPath.Point(3, 4, LINE_TO), lineToPoints.get(1));
  }

  @Test
  public void testReset() {
    Path path = new Path();
    path.moveTo(0, 3);
    path.lineTo(2, 3);
    path.quadTo(2, 3, 4, 5);
    path.reset();

    ShadowPath shadowPath = shadowOf(path);
    List<ShadowPath.Point> points = shadowPath.getPoints();
    assertEquals(0, points.size());
  }

  @Test
  public void copyConstructor_copiesShadowPoints() {
    Path path = new Path();
    path.moveTo(0, 3);
    path.lineTo(2, 3);
    path.quadTo(2, 3, 4, 5);

    Path copiedPath = new Path(path);

    assertEquals(shadowOf(path).getPoints(), shadowOf(copiedPath).getPoints());
  }

  @Test
  @Config(minSdk = O)
  public void copyConstructor_copiesPathSegments() {
    Path path = new Path();
    path.moveTo(9, 3);
    path.lineTo(2, 3);
    path.quadTo(2, 3, 4, 5);
    float[] segments = path.approximate(ERROR_TOLERANCE);

    Path copiedPath = new Path(path);

    assertArrayEquals(segments, copiedPath.approximate(ERROR_TOLERANCE), ERROR_TOLERANCE);
  }

  @Test
  public void copyConstructor_copiesFillType() {
    Path.FillType fillType = Path.FillType.INVERSE_EVEN_ODD;
    Path path = new Path();
    path.setFillType(fillType);

    Path copiedPath = new Path(path);

    assertEquals(fillType, copiedPath.getFillType());
  }

  @Test
  public void copyConstructor_emptyPath_isEmpty() {
    Path emptyPath = new Path();

    Path copiedEmptyPath = new Path(emptyPath);

    assertTrue(copiedEmptyPath.isEmpty());
  }

  @Test
  public void emptyConstructor_isEmpty() {
    Path emptyPath = new Path();

    assertTrue(emptyPath.isEmpty());
  }
}
