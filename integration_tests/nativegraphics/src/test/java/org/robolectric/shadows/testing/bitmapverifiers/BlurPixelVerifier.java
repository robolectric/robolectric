/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.graphics.Color;

public class BlurPixelVerifier extends BitmapVerifier {

  private final int dstColor;
  private final int srcColor;

  /**
   * Create a BitmapVerifier that compares pixel values relative to the provided source and
   * destination colors. Pixels closer to the center of the test bitmap are expected to match closer
   * to the source color, while pixels on the exterior of the test bitmap are expected to match the
   * destination color more closely
   */
  public BlurPixelVerifier(int srcColor, int dstColor) {
    this.srcColor = srcColor;
    this.dstColor = dstColor;
  }

  @Override
  public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {

    float dstRedChannel = Color.red(dstColor);
    float dstGreenChannel = Color.green(dstColor);
    float dstBlueChannel = Color.blue(dstColor);

    float srcRedChannel = Color.red(srcColor);
    float srcGreenChannel = Color.green(srcColor);
    float srcBlueChannel = Color.blue(srcColor);

    // Calculate the largest rgb color difference between the source and destination
    // colors
    double maxDifference =
        Math.pow(srcRedChannel - dstRedChannel, 2.0f)
            + Math.pow(srcGreenChannel - dstGreenChannel, 2.0f)
            + Math.pow(srcBlueChannel - dstBlueChannel, 2.0f);

    // Calculate the maximum distance between pixels to the center of the test image
    double maxPixelDistance = Math.sqrt(Math.pow(width / 2.0, 2.0) + Math.pow(height / 2.0, 2.0));

    // Additional tolerance applied to comparisons
    float threshold = .05f;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        double pixelDistance =
            Math.sqrt(Math.pow(x - width / 2.0, 2.0) + Math.pow(y - height / 2.0, 2.0));
        // Calculate the threshold of the destination color expected based on the
        // pixels position relative to the center
        double dstPercentage = pixelDistance / maxPixelDistance + threshold;

        int pixelColor = bitmap[indexFromXAndY(x, y, stride, offset)];
        double pixelRedChannel = Color.red(pixelColor);
        double pixelGreenChannel = Color.green(pixelColor);
        double pixelBlueChannel = Color.blue(pixelColor);
        // Compare the RGB color distance between the current pixel and the destination
        // color
        double dstDistance =
            Math.sqrt(
                Math.pow(pixelRedChannel - dstRedChannel, 2.0)
                    + Math.pow(pixelGreenChannel - dstGreenChannel, 2.0)
                    + Math.pow(pixelBlueChannel - dstBlueChannel, 2.0));

        // Compare the RGB color distance between the current pixel and the source
        // color
        double srcDistance =
            Math.sqrt(
                Math.pow(pixelRedChannel - srcRedChannel, 2.0)
                    + Math.pow(pixelGreenChannel - srcGreenChannel, 2.0)
                    + Math.pow(pixelBlueChannel - srcBlueChannel, 2.0));

        // calculate the ratio between the destination color to the current pixel
        // color relative to the maximum distance between source and destination colors
        // If this value exceeds the threshold expected for the pixel distance from
        // center then we are rendering an unexpected color
        double dstFraction = dstDistance / maxDifference;
        if (dstFraction > dstPercentage) {
          return false;
        }

        // similarly compute the ratio between the source color to the current pixel
        // color relative to the maximum distance between source and destination colors
        // If this value exceeds the threshold expected for the pixel distance from
        // center then we are rendering an unexpected source color
        double srcFraction = srcDistance / maxDifference;
        if (srcFraction > dstPercentage) {
          return false;
        }
      }
    }
    return true;
  }
}
