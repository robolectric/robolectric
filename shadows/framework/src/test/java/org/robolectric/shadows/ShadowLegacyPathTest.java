package org.robolectric.shadows;

import android.graphics.Path;
import com.google.common.truth.Truth;
import java.awt.geom.Area;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLegacyPath.class})
public class ShadowLegacyPathTest {

  private Path p1 = new Path();
  private Path p2 = new Path();

  @Before
  public void setUp() throws Exception {
    /*
           ┌─────┐
        p1 │  ┌──┼──┐
           └──┼──┘  │ p2
              └─────┘
    */
    p1.addRect(0, 0, 100, 100, Path.Direction.CW);
    p2.addRect(50, 50, 150, 150, Path.Direction.CW);
  }

  @Test
  public void testOpDifference() {
    Path actualPath = new Path();
    actualPath.op(p1, p2, Path.Op.DIFFERENCE);

    Path expectedPath = new Path();
    expectedPath.addRect(0, 0, 100, 50, Path.Direction.CW);
    expectedPath.addRect(0, 50, 50, 100, Path.Direction.CW);

    assertPath(actualPath, expectedPath);
  }

  @Test
  public void testOpReverseDifference() {
    Path actualPath = new Path();
    actualPath.op(p1, p2, Path.Op.REVERSE_DIFFERENCE);

    Path expectedPath = new Path();
    expectedPath.addRect(100, 50, 150, 100, Path.Direction.CW);
    expectedPath.addRect(50, 100, 150, 150, Path.Direction.CW);

    assertPath(actualPath, expectedPath);
  }

  @Test
  public void testOpIntersect() {
    Path actualPath = new Path();
    actualPath.op(p1, p2, Path.Op.INTERSECT);

    Path expectedPath = new Path();
    expectedPath.addRect(50, 50, 100, 100, Path.Direction.CW);

    assertPath(actualPath, expectedPath);
  }

  @Test
  public void testOpUnion() {
    Path actualPath = new Path();
    actualPath.op(p1, p2, Path.Op.UNION);

    Path expectedPath = new Path();
    expectedPath.addRect(0, 0, 100, 50, Path.Direction.CW);
    expectedPath.addRect(0, 50, 150, 100, Path.Direction.CW);
    expectedPath.addRect(50, 100, 150, 150, Path.Direction.CW);

    assertPath(actualPath, expectedPath);
  }

  @Test
  public void testOpXor() {
    Path actualPath = new Path();
    actualPath.op(p1, p2, Path.Op.XOR);

    Path expectedPath = new Path();
    expectedPath.addRect(0, 0, 100, 50, Path.Direction.CW);
    expectedPath.addRect(0, 50, 50, 100, Path.Direction.CW);
    expectedPath.addRect(100, 50, 150, 100, Path.Direction.CW);
    expectedPath.addRect(50, 100, 150, 150, Path.Direction.CW);

    assertPath(actualPath, expectedPath);
  }

  private void assertPath(Path actual, Path expected) {
    ShadowLegacyPath actualShadow = Shadow.extract(actual);
    ShadowLegacyPath expectedShadow = Shadow.extract(expected);

    Area actualArea = new Area(actualShadow.getJavaShape());
    Area expectedArea = new Area(expectedShadow.getJavaShape());

    Truth.assertThat(actualArea.equals(expectedArea)).isTrue();
  }
}
