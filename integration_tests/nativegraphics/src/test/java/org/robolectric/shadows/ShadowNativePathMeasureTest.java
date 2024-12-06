/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This test is created from Android CTS tests:
 *
 * https://cs.android.com/android/platform/superproject/+/master:cts/tests/tests/graphics/src/android/graphics/cts/PathMeasureTest.java
 */

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathMeasure;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativePathMeasureTest {
  private PathMeasure pathMeasure;
  private Path path;

  @Before
  public void setup() {
    path = new Path();
    pathMeasure = new PathMeasure();
  }

  @Test
  public void testConstructor() {
    pathMeasure = new PathMeasure();

    // new the PathMeasure instance
    Path path = new Path();
    pathMeasure = new PathMeasure(path, true);

    // new the PathMeasure instance
    pathMeasure = new PathMeasure(path, false);
  }

  @Test
  public void testGetPosTanArraysTooSmall() {
    float distance = 1f;
    float[] pos = {1f};
    float[] tan = {1f};

    assertThrows(
        ArrayIndexOutOfBoundsException.class, () -> pathMeasure.getPosTan(distance, pos, tan));
  }

  @Test
  public void testGetPosTan() {
    float distance = 1f;
    float[] pos2 = {1f, 2f};
    float[] tan2 = {1f, 3f};
    assertFalse(pathMeasure.getPosTan(distance, pos2, tan2));

    pathMeasure.setPath(path, true);
    path.addRect(1f, 2f, 3f, 4f, Path.Direction.CW);
    pathMeasure.setPath(path, true);
    float[] pos3 = {1f, 2f, 3f, 4f};
    float[] tan3 = {1f, 2f, 3f, 4f};
    assertTrue(pathMeasure.getPosTan(0f, pos3, tan3));
  }

  @Test
  public void testNextContour() {
    assertFalse(pathMeasure.nextContour());
    path.addRect(1, 2, 3, 4, Path.Direction.CW);
    path.addRect(1, 2, 3, 4, Path.Direction.CW);
    pathMeasure.setPath(path, true);
    assertTrue(pathMeasure.nextContour());
    assertFalse(pathMeasure.nextContour());
  }

  @Test
  public void testGetLength() {
    assertEquals(0f, pathMeasure.getLength(), 0.0f);
    path.addRect(1, 2, 3, 4, Path.Direction.CW);
    pathMeasure.setPath(path, true);
    assertEquals(8.0f, pathMeasure.getLength(), 0.0f);
  }

  @Test
  public void testIsClosed() {
    Path circle = new Path();
    circle.addCircle(0, 0, 1, Direction.CW);

    PathMeasure measure = new PathMeasure(circle, false);
    assertTrue(measure.isClosed());
    measure.setPath(circle, true);
    assertTrue(measure.isClosed());

    Path line = new Path();
    line.lineTo(5, 5);

    measure.setPath(line, false);
    assertFalse(measure.isClosed());
    measure.setPath(line, true);
    assertTrue(measure.isClosed());
  }

  @Test
  public void testSetPath() {
    pathMeasure.setPath(path, true);
    // There is no getter and we can't obtain any status about it.
  }

  @Test
  public void testGetSegment() {
    assertEquals(0f, pathMeasure.getLength(), 0.0f);
    path.addRect(1, 2, 3, 4, Path.Direction.CW);
    pathMeasure.setPath(path, true);
    assertEquals(8f, pathMeasure.getLength(), 0.0f);
    Path dst = new Path();
    assertTrue(pathMeasure.getSegment(0, pathMeasure.getLength(), dst, true));
    assertFalse(pathMeasure.getSegment(pathMeasure.getLength(), 0, dst, true));
  }

  @Test
  public void testGetMatrix() {
    Matrix matrix = new Matrix();
    assertFalse(pathMeasure.getMatrix(1f, matrix, PathMeasure.POSITION_MATRIX_FLAG));
    matrix.setScale(1f, 2f);
    path.addRect(1f, 2f, 3f, 4f, Path.Direction.CW);
    pathMeasure.setPath(path, true);
    assertTrue(pathMeasure.getMatrix(0f, matrix, PathMeasure.TANGENT_MATRIX_FLAG));
  }
}
