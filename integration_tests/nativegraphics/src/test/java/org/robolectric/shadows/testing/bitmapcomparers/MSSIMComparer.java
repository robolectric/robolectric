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
package org.robolectric.shadows.testing.bitmapcomparers;

import android.graphics.Color;
import android.util.Log;

/**
 * Image comparison using Structural Similarity Index, developed by Wang, Bovik, Sheikh, and
 * Simoncelli. Details can be read in their paper :
 *
 * <p>https://ece.uwaterloo.ca/~z70wang/publications/ssim.pdf
 */
public class MSSIMComparer extends BitmapComparer {
  // These values were taken from the publication
  public static final String TAG_NAME = "MSSIM";
  public static final double CONSTANT_L = 254;
  public static final double CONSTANT_K1 = 0.00001;
  public static final double CONSTANT_K2 = 0.00003;
  public static final double CONSTANT_C1 = Math.pow(CONSTANT_L * CONSTANT_K1, 2);
  public static final double CONSTANT_C2 = Math.pow(CONSTANT_L * CONSTANT_K2, 2);
  public static final int WINDOW_SIZE = 10;

  private double threshold;

  public MSSIMComparer(double threshold) {
    this.threshold = threshold;
  }

  /**
   * Compute the size of the window. The window defaults to WINDOW_SIZE, but must be contained
   * within dimension.
   */
  private int computeWindowSize(int coordinateStart, int dimension) {
    if (coordinateStart + WINDOW_SIZE <= dimension) {
      return WINDOW_SIZE;
    }
    return dimension - coordinateStart;
  }

  @Override
  public boolean verifySame(
      int[] ideal, int[] given, int offset, int stride, int width, int height) {
    double ssimTotal = 0;
    int windows = 0;

    for (int currentWindowY = 0; currentWindowY < height; currentWindowY += WINDOW_SIZE) {
      int windowHeight = computeWindowSize(currentWindowY, height);
      for (int currentWindowX = 0; currentWindowX < width; currentWindowX += WINDOW_SIZE) {
        int windowWidth = computeWindowSize(currentWindowX, width);
        int start = indexFromXAndY(currentWindowX, currentWindowY, stride, offset);
        if (isWindowWhite(ideal, start, stride, windowWidth, windowHeight)
            && isWindowWhite(given, start, stride, windowWidth, windowHeight)) {
          continue;
        }
        windows++;
        double[] means = getMeans(ideal, given, start, stride, windowWidth, windowHeight);
        double meanX = means[0];
        double meanY = means[1];
        double[] variances =
            getVariances(ideal, given, meanX, meanY, start, stride, windowWidth, windowHeight);
        double varX = variances[0];
        double varY = variances[1];
        double stdBoth = variances[2];
        double ssim = ssim(meanX, meanY, varX, varY, stdBoth);
        ssimTotal += ssim;
      }
    }

    if (windows == 0) {
      return true;
    }

    ssimTotal /= windows;

    Log.d(TAG_NAME, "MSSIM = " + ssimTotal);

    return (ssimTotal >= threshold);
  }

  private boolean isWindowWhite(
      int[] colors, int start, int stride, int windowWidth, int windowHeight) {
    for (int y = 0; y < windowHeight; y++) {
      for (int x = 0; x < windowWidth; x++) {
        if (colors[indexFromXAndY(x, y, stride, start)] != Color.WHITE) {
          return false;
        }
      }
    }
    return true;
  }

  private double ssim(double muX, double muY, double sigX, double sigY, double sigXY) {
    double ssimDouble = (((2 * muX * muY) + CONSTANT_C1) * ((2 * sigXY) + CONSTANT_C2));
    double denom = ((muX * muX) + (muY * muY) + CONSTANT_C1) * (sigX + sigY + CONSTANT_C2);
    ssimDouble /= denom;
    return ssimDouble;
  }

  /**
   * This method will find the mean of a window in both sets of pixels. The return is an array where
   * the first double is the mean of the first set and the second double is the mean of the second
   * set.
   */
  private double[] getMeans(
      int[] pixels0, int[] pixels1, int start, int stride, int windowWidth, int windowHeight) {
    double avg0 = 0;
    double avg1 = 0;
    for (int y = 0; y < windowHeight; y++) {
      for (int x = 0; x < windowWidth; x++) {
        int index = indexFromXAndY(x, y, stride, start);
        avg0 += getIntensity(pixels0[index]);
        avg1 += getIntensity(pixels1[index]);
      }
    }
    avg0 /= windowWidth * windowHeight;
    avg1 /= windowWidth * windowHeight;
    return new double[] {avg0, avg1};
  }

  /**
   * Finds the variance of the two sets of pixels, as well as the covariance of the windows. The
   * return value is an array of doubles, the first is the variance of the first set of pixels, the
   * second is the variance of the second set of pixels, and the third is the covariance.
   */
  private double[] getVariances(
      int[] pixels0,
      int[] pixels1,
      double mean0,
      double mean1,
      int start,
      int stride,
      int windowWidth,
      int windowHeight) {
    double var0 = 0;
    double var1 = 0;
    double varBoth = 0;
    for (int y = 0; y < windowHeight; y++) {
      for (int x = 0; x < windowWidth; x++) {
        int index = indexFromXAndY(x, y, stride, start);
        double v0 = getIntensity(pixels0[index]) - mean0;
        double v1 = getIntensity(pixels1[index]) - mean1;
        var0 += v0 * v0;
        var1 += v1 * v1;
        varBoth += v0 * v1;
      }
    }
    var0 /= (windowWidth * windowHeight) - 1;
    var1 /= (windowWidth * windowHeight) - 1;
    varBoth /= (windowWidth * windowHeight) - 1;
    return new double[] {var0, var1, varBoth};
  }

  /**
   * Gets the intensity of a given pixel in RGB using luminosity formula
   *
   * <p>l = 0.21R' + 0.72G' + 0.07B'
   *
   * <p>The prime symbols dictate a gamma correction of 1.
   */
  private double getIntensity(int pixel) {
    final double gamma = 1;
    double l = 0;
    l += (0.21f * Math.pow(Color.red(pixel) / 255f, gamma));
    l += (0.72f * Math.pow(Color.green(pixel) / 255f, gamma));
    l += (0.07f * Math.pow(Color.blue(pixel) / 255f, gamma));
    return l;
  }
}
