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
 * limitations under the License.
 */
package org.robolectric.shadows.testing.bitmapverifiers;

import android.util.Log;
import org.robolectric.shadows.testing.util.CompareUtils;

public class ColorCountVerifier extends BitmapVerifier {
  private int color;
  private int count;
  private int threshold;

  public ColorCountVerifier(int color, int count, int threshold) {
    this.color = color;
    this.count = count;
    this.threshold = threshold;
  }

  public ColorCountVerifier(int color, int count) {
    this(color, count, 0);
  }

  @Override
  public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {
    int count = 0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (CompareUtils.verifyPixelWithThreshold(
            bitmap[indexFromXAndY(x, y, stride, offset)], color, threshold)) {
          count++;
        }
      }
    }
    if (count != this.count) {
      Log.d("ColorCountVerifier", ("Color count mismatch " + count) + " != " + this.count);
    }
    return count == this.count;
  }
}
