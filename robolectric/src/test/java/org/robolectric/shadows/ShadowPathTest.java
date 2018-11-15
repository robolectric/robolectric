package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;
import static org.robolectric.shadows.ShadowPath.Point.Type.MOVE_TO;

import android.graphics.Path;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowPathTest {

  @Test
  public void testMoveTo() throws Exception {
    Path path = new Path();
    path.moveTo(2, 3);
    path.moveTo(3, 4);

    List<ShadowPath.Point> moveToPoints = shadowOf(path).getPoints();
    assertEquals(2, moveToPoints.size());
    assertEquals(new ShadowPath.Point(2, 3, MOVE_TO), moveToPoints.get(0));
    assertEquals(new ShadowPath.Point(3, 4, MOVE_TO), moveToPoints.get(1));
  }

  @Test
  public void testLineTo() throws Exception {
    Path path = new Path();
    path.lineTo(2, 3);
    path.lineTo(3, 4);

    List<ShadowPath.Point> lineToPoints = shadowOf(path).getPoints();
    assertEquals(2, lineToPoints.size());
    assertEquals(new ShadowPath.Point(2, 3, LINE_TO), lineToPoints.get(0));
    assertEquals(new ShadowPath.Point(3, 4, LINE_TO), lineToPoints.get(1));
  }

  @Test
  public void testReset() throws Exception {
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
  public void test_copyConstructor() throws Exception {
    Path path = new Path();
    path.moveTo(0, 3);
    path.lineTo(2, 3);
    path.quadTo(2, 3, 4, 5);

    Path copiedPath = new Path(path);
    assertEquals(shadowOf(path).getPoints(), shadowOf(copiedPath).getPoints());
  }
}
