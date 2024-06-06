/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package org.robolectric.shadows;

import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class BitmapUtils {
  private static final String TAG = "BitmapUtils";

  private BitmapUtils() {}

  private static Boolean compareBasicBitmapsInfo(Bitmap bmp1, Bitmap bmp2) {
    if (bmp1 == bmp2) {
      return Boolean.TRUE;
    }

    if (bmp1 == null) {
      Log.d(TAG, "compareBitmaps() failed because bmp1 is null");
      return Boolean.FALSE;
    }

    if (bmp2 == null) {
      Log.d(TAG, "compareBitmaps() failed because bmp2 is null");
      return Boolean.FALSE;
    }

    if ((bmp1.getWidth() != bmp2.getWidth()) || (bmp1.getHeight() != bmp2.getHeight())) {
      Log.d(
          TAG,
          "compareBitmaps() failed because sizes don't match "
              + "bmp1=("
              + bmp1.getWidth()
              + "x"
              + bmp1.getHeight()
              + "), "
              + "bmp2=("
              + bmp2.getWidth()
              + "x"
              + bmp2.getHeight()
              + ")");
      return Boolean.FALSE;
    }

    if (bmp1.getConfig() != bmp2.getConfig()) {
      Log.d(
          TAG,
          "compareBitmaps() failed because configs don't match "
              + "bmp1=("
              + bmp1.getConfig()
              + "), "
              + "bmp2=("
              + bmp2.getConfig()
              + ")");
      return Boolean.FALSE;
    }

    return null;
  }

  /** Compares two bitmaps by pixels. */
  public static boolean compareBitmaps(Bitmap bmp1, Bitmap bmp2) {
    final Boolean basicComparison = compareBasicBitmapsInfo(bmp1, bmp2);
    if (basicComparison != null) {
      return basicComparison.booleanValue();
    }

    for (int i = 0; i < bmp1.getWidth(); i++) {
      for (int j = 0; j < bmp1.getHeight(); j++) {
        if (bmp1.getPixel(i, j) != bmp2.getPixel(i, j)) {
          Log.d(TAG, "compareBitmaps(): pixels (" + i + ", " + j + ") don't match");
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Compares two bitmaps by pixels, with a buffer for mismatches.
   *
   * <p>For example, if {@code minimumPrecision} is 0.99, at least 99% of the pixels should match.
   */
  public static boolean compareBitmaps(Bitmap bmp1, Bitmap bmp2, double minimumPrecision) {
    final Boolean basicComparison = compareBasicBitmapsInfo(bmp1, bmp2);
    if (basicComparison != null) {
      return basicComparison.booleanValue();
    }

    final int width = bmp1.getWidth();
    final int height = bmp1.getHeight();

    final long numberPixels = (long) width * height;
    long numberMismatches = 0;

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        if (bmp1.getPixel(i, j) != bmp2.getPixel(i, j)) {
          numberMismatches++;
          if (numberMismatches <= 10) {
            // Let's not spam logcat...
            Log.w(TAG, "compareBitmaps(): pixels (" + i + ", " + j + ") don't match");
          }
        }
      }
    }
    final double actualPrecision = ((double) numberPixels - numberMismatches) / numberPixels;
    Log.v(
        TAG,
        "compareBitmaps(): numberPixels="
            + numberPixels
            + ", numberMismatches="
            + numberMismatches
            + ", minimumPrecision="
            + minimumPrecision
            + ", actualPrecision="
            + actualPrecision);
    return actualPrecision >= minimumPrecision;
  }

  public static Bitmap generateRandomBitmap(int width, int height) {
    final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    final Random generator = new Random();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        bmp.setPixel(x, y, generator.nextInt(Integer.MAX_VALUE));
      }
    }
    return bmp;
  }

  public static Bitmap generateWhiteBitmap(int width, int height) {
    final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    bmp.eraseColor(Color.WHITE);
    return bmp;
  }

  public static ByteArrayInputStream bitmapToInputStream(Bitmap bmp) {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bmp.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
    byte[] bitmapData = bos.toByteArray();
    return new ByteArrayInputStream(bitmapData);
  }

  private static void logIfBitmapSolidColor(String fileName, Bitmap bitmap) {
    int firstColor = bitmap.getPixel(0, 0);
    for (int x = 0; x < bitmap.getWidth(); x++) {
      for (int y = 0; y < bitmap.getHeight(); y++) {
        if (bitmap.getPixel(x, y) != firstColor) {
          return;
        }
      }
    }

    Log.w(TAG, String.format("%s entire bitmap color is %x", fileName, firstColor));
  }

  public static void saveBitmap(Bitmap bitmap, String directoryName, String fileName) {
    new File(directoryName).mkdirs(); // create dirs if needed

    Log.d(TAG, "Saving file: " + fileName + " in directory: " + directoryName);

    if (bitmap == null) {
      Log.d(TAG, "File not saved, bitmap was null");
      return;
    }

    logIfBitmapSolidColor(fileName, bitmap);

    File file = new File(directoryName, fileName);
    try (FileOutputStream fileStream = new FileOutputStream(file)) {
      bitmap.compress(Bitmap.CompressFormat.PNG, 0 /* ignored for PNG */, fileStream);
      fileStream.flush();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Compare expected to actual to see if their diff is less than mseMargin.
  // lessThanMargin is to indicate whether we expect the diff to be
  // "less than" or "no less than".
  public static boolean compareBitmapsMse(
      Bitmap expected,
      Bitmap actual,
      int mseMargin,
      boolean lessThanMargin,
      boolean isPremultiplied) {
    final Boolean basicComparison = compareBasicBitmapsInfo(expected, actual);
    if (basicComparison != null) {
      return basicComparison.booleanValue();
    }

    double mse = 0;
    int width = expected.getWidth();
    int height = expected.getHeight();

    // Bitmap.getPixels() returns colors with non-premultiplied ARGB values.
    int[] expColors = new int[width * height];
    expected.getPixels(expColors, 0, width, 0, 0, width, height);

    int[] actualColors = new int[width * height];
    actual.getPixels(actualColors, 0, width, 0, 0, width, height);

    for (int row = 0; row < height; ++row) {
      for (int col = 0; col < width; ++col) {
        int idx = row * width + col;
        mse += distance(expColors[idx], actualColors[idx], isPremultiplied);
      }
    }
    mse /= width * height;

    Log.i(TAG, "MSE: " + mse);
    if (lessThanMargin) {
      if (mse > mseMargin) {
        Log.d(TAG, "MSE too large for normal case: " + mse);
        return false;
      }
      return true;
    } else {
      if (mse <= mseMargin) {
        Log.d(TAG, "MSE too small for abnormal case: " + mse);
        return false;
      }
      return true;
    }
  }

  // Same as above, but asserts compareBitmapsMse's return value.
  public static void assertBitmapsMse(
      Bitmap expected,
      Bitmap actual,
      int mseMargin,
      boolean lessThanMargin,
      boolean isPremultiplied) {
    assertTrue(compareBitmapsMse(expected, actual, mseMargin, lessThanMargin, isPremultiplied));
  }

  private static int multiplyAlpha(int color, int alpha) {
    return (color * alpha + 127) / 255;
  }

  // For the Bitmap with Alpha, multiply the Alpha values to get the effective
  // RGB colors and then compute the color-distance.
  private static double distance(int expect, int actual, boolean isPremultiplied) {
    if (isPremultiplied) {
      final int a1 = Color.alpha(actual);
      final int a2 = Color.alpha(expect);
      final int r = multiplyAlpha(Color.red(actual), a1) - multiplyAlpha(Color.red(expect), a2);
      final int g = multiplyAlpha(Color.green(actual), a1) - multiplyAlpha(Color.green(expect), a2);
      final int b = multiplyAlpha(Color.blue(actual), a1) - multiplyAlpha(Color.blue(expect), a2);
      return r * r + g * g + b * b;
    } else {
      int r = Color.red(actual) - Color.red(expect);
      int g = Color.green(actual) - Color.green(expect);
      int b = Color.blue(actual) - Color.blue(expect);
      return r * r + g * g + b * b;
    }
  }
}
