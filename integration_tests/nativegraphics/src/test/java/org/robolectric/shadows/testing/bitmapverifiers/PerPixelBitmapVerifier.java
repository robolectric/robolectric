/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.util.Log;
import androidx.annotation.ColorInt;
import org.robolectric.shadows.testing.util.CompareUtils;

/** This class looks at every pixel in a given bitmap and verifies that it is correct. */
public abstract class PerPixelBitmapVerifier extends BitmapVerifier {
  private static final String TAG = "PerPixelBitmapVerifer";
  public static final int DEFAULT_THRESHOLD = 48;

  // total color difference tolerated without the pixel failing
  private int colorTolerance;

  // portion of bitmap allowed to fail pixel check
  private float spatialTolerance;

  public PerPixelBitmapVerifier() {
    this(DEFAULT_THRESHOLD, 0);
  }

  public PerPixelBitmapVerifier(int colorTolerance) {
    this(colorTolerance, 0);
  }

  public PerPixelBitmapVerifier(int colorTolerance, float spatialTolerance) {
    this.colorTolerance = colorTolerance;
    this.spatialTolerance = spatialTolerance;
  }

  @ColorInt
  protected int getExpectedColor(int x, int y) {
    return Color.WHITE;
  }

  @Override
  public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {
    int failures = 0;
    int[] differenceMap = new int[bitmap.length];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int index = indexFromXAndY(x, y, stride, offset);
        if (!verifyPixel(x, y, bitmap[index])) {
          if (failures < 50) {
            Log.d(
                TAG,
                "Expected : "
                    + Integer.toHexString(getExpectedColor(x, y))
                    + " received : "
                    + Integer.toHexString(bitmap[index])
                    + " at position ("
                    + x
                    + ","
                    + y
                    + ")");
          }
          failures++;
          differenceMap[index] = FAIL_COLOR;
        } else {
          differenceMap[index] = PASS_COLOR;
        }
      }
    }
    int toleratedFailures = (int) (spatialTolerance * width * height);
    boolean success = failures <= toleratedFailures;
    Log.d(TAG, failures + " failures observed out of " + toleratedFailures + " tolerated failures");
    if (!success) {
      differenceBitmapBase = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      differenceBitmapBase.setPixels(differenceMap, offset, stride, 0, 0, width, height);
    }
    return success;
  }

  protected boolean verifyPixel(int x, int y, int observedColor) {
    int expectedColor = getExpectedColor(x, y);
    return CompareUtils.verifyPixelWithThreshold(observedColor, expectedColor, colorTolerance);
  }
}
