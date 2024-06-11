package org.robolectric.shadows.testing.util;

import android.graphics.Color;

public final class CompareUtils {
  /**
   * @return True if close enough
   */
  public static boolean verifyPixelWithThreshold(int color, int expectedColor, int threshold) {
    int diff =
        Math.abs(Color.red(color) - Color.red(expectedColor))
            + Math.abs(Color.green(color) - Color.green(expectedColor))
            + Math.abs(Color.blue(color) - Color.blue(expectedColor));
    return diff <= threshold;
  }

  /**
   * @param threshold Per channel differences for R / G / B channel against the average of these 3
   *     channels. Should be less than 2 normally.
   * @return True if the color is close enough to be a gray scale color.
   */
  public static boolean verifyPixelGrayScale(int color, int threshold) {
    int average = Color.red(color) + Color.green(color) + Color.blue(color);
    average /= 3;
    return Math.abs(Color.red(color) - average) <= threshold
        && Math.abs(Color.green(color) - average) <= threshold
        && Math.abs(Color.blue(color) - average) <= threshold;
  }

  private CompareUtils() {}
}
