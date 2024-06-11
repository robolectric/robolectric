/*
 * Copyright (C) 2017 The Android Open Source Project
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
 */

package org.robolectric.shadows.testing.bitmapverifiers;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import org.junit.Assert;

public class SamplePointWideGamutVerifier extends BitmapVerifier {
  private static final String TAG = "SamplePointWideGamut";

  private final Point[] points;
  private final Color[] colors;
  private final float eps;

  public SamplePointWideGamutVerifier(Point[] points, Color[] colors, float eps) {
    this.points = points;
    this.colors = colors;
    this.eps = eps;
  }

  @Override
  public boolean verify(Bitmap bitmap) {
    Assert.assertTrue(
        "You cannot use this verifier with a bitmap whose ColorSpace is not "
            + "wide gamut: "
            + bitmap.getColorSpace(),
        bitmap.getColorSpace().isWideGamut());

    boolean success = true;
    for (int i = 0; i < points.length; i++) {
      Point p = points[i];
      Color expected = colors[i];

      Color actual = bitmap.getColor(p.x, p.y).convert(expected.getColorSpace());

      boolean localSuccess = true;
      if (!floatCompare(expected.red(), actual.red(), eps)) {
        localSuccess = false;
      }
      if (!floatCompare(expected.green(), actual.green(), eps)) {
        localSuccess = false;
      }
      if (!floatCompare(expected.blue(), actual.blue(), eps)) {
        localSuccess = false;
      }
      if (!floatCompare(expected.alpha(), actual.alpha(), eps)) {
        localSuccess = false;
      }

      if (!localSuccess) {
        success = false;
        Log.w(TAG, "Expected " + expected + " at " + p + ", got " + actual);
      }
    }
    return success;
  }

  @Override
  public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {
    Assert.fail("This verifier requires more info than can be encoded in sRGB (int) values");
    return false;
  }

  private static boolean floatCompare(float a, float b, float eps) {
    return Float.compare(a, b) == 0 || Math.abs(a - b) <= eps;
  }
}
