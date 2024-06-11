package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Path;
import android.util.PathParser;
import android.util.PathParser.PathData;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativePathParserTest {

  @Test
  public void testCreatePathFromPathString() {
    Path path = new Path();
    PathData data = new PathData("M 275 80");
    PathParser.createPathFromPathData(path, data);

    assertFalse(path.isEmpty());
  }

  @Test
  public void testCreatePathFromPathData() {
    Path path = new Path();
    PathData original = new PathData("M 275 80");
    PathData data = new PathData(original);
    PathParser.createPathFromPathData(path, data);

    assertFalse(path.isEmpty());
  }

  @Test
  public void testCreatePathFromEmptyPathData() {
    Path path = new Path();
    PathData data = new PathData();
    PathParser.createPathFromPathData(path, data);

    assertTrue(path.isEmpty());
  }

  @Test
  public void testCreatePathFromEmptyPathDataWithSetPathData() {
    Path path = new Path();
    PathData original = new PathData("M 275 80");
    PathData data = new PathData();
    data.setPathData(original);
    PathParser.createPathFromPathData(path, data);

    assertFalse(path.isEmpty());
  }

  @Test
  public void testCreatePathFromPathParserPathString() {
    Path path = PathParser.createPathFromPathData("M 275 80");

    assertFalse(path.isEmpty());
  }

  @Test
  public void testInterpolatePathData() {
    Path path = new Path();
    PathData pathData = new PathData();
    PathData from = new PathData("M 100 100");
    PathData to = new PathData("M 200 200");
    PathParser.interpolatePathData(pathData, from, to, 0.5f);

    PathParser.createPathFromPathData(path, pathData);

    assertFalse(path.isEmpty());
  }

  @Test
  public void testCanMorph() {
    PathData data1 = new PathData("M 275 80");
    PathData data2 = new PathData("M 275 80");

    assertTrue(PathParser.canMorph(data1, data2));
  }
}
