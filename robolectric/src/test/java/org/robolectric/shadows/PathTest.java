package org.robolectric.shadows;

import android.graphics.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.shadows.ShadowPath.Point.Type.LINE_TO;
import static org.robolectric.shadows.ShadowPath.Point.Type.MOVE_TO;


@RunWith(TestRunners.WithDefaults.class)
public class PathTest {

  @Test
  public void testGradTo() {
    Path path = Robolectric.newInstanceOf(Path.class);
    path.quadTo(0, 5, 10, 15);
    ShadowPath shadowPath = shadowOf(path);
    assertEquals(shadowPath.getQuadDescription(), "Add a quadratic bezier from last point, approaching (0.0,5.0), ending at (10.0,15.0)");
  }

  @Test
  public void testMoveTo() throws Exception {
    Path path = Robolectric.newInstanceOf(Path.class);
    path.moveTo(2, 3);
    path.moveTo(3, 4);

    List<ShadowPath.Point> moveToPoints = shadowOf(path).getPoints();
    assertEquals(2, moveToPoints.size());
    assertEquals(new ShadowPath.Point(2, 3, MOVE_TO), moveToPoints.get(0));
    assertEquals(new ShadowPath.Point(3, 4, MOVE_TO), moveToPoints.get(1));
  }

  @Test
  public void testLineTo() throws Exception {
    Path path = Robolectric.newInstanceOf(Path.class);
    path.lineTo(2, 3);
    path.lineTo(3, 4);

    List<ShadowPath.Point> lineToPoints = shadowOf(path).getPoints();
    assertEquals(2, lineToPoints.size());
    assertEquals(new ShadowPath.Point(2, 3, LINE_TO), lineToPoints.get(0));
    assertEquals(new ShadowPath.Point(3, 4, LINE_TO), lineToPoints.get(1));
  }

  @Test
  public void testReset() throws Exception {
    Path path = Robolectric.newInstanceOf(Path.class);
    path.moveTo(0, 3);
    path.lineTo(2, 3);
    path.quadTo(2, 3, 4, 5);
    path.reset();

    ShadowPath shadowPath = shadowOf(path);
    List<ShadowPath.Point> points = shadowPath.getPoints();
    assertEquals(0, points.size());
    assertNull(shadowPath.getWasMovedTo());
    assertEquals("", shadowPath.getQuadDescription());
  }

  @Test
  public void test_copyConstructor() throws Exception {
    Path path = Robolectric.newInstanceOf(Path.class);
    path.moveTo(0, 3);
    path.lineTo(2, 3);
    path.quadTo(2, 3, 4, 5);

    Path copiedPath = new Path(path);
    assertEquals(shadowOf(path).getPoints(), shadowOf(copiedPath).getPoints());
    assertEquals(shadowOf(path).getWasMovedTo(), shadowOf(copiedPath).getWasMovedTo());
    assertEquals(shadowOf(path).getQuadDescription(), shadowOf(copiedPath).getQuadDescription());
  }
}
