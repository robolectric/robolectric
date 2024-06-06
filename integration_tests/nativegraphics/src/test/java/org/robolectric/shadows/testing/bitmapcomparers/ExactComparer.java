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

import android.util.Log;

/** This class does an exact comparison of the pixels in a bitmap. */
public class ExactComparer extends BitmapComparer {
  private static final String TAG = "ExactComparer";

  /** This method does an exact 1 to 1 comparison of the two bitmaps */
  @Override
  public boolean verifySame(
      int[] ideal, int[] given, int offset, int stride, int width, int height) {
    int count = 0;

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int index = indexFromXAndY(x, y, stride, offset);
        if (ideal[index] != given[index]) {
          if (count < 50) {
            Log.d(TAG, "Failure on position x = " + x + " y = " + y);
            Log.d(
                TAG,
                "Expected color : "
                    + Integer.toHexString(ideal[index])
                    + " given color : "
                    + Integer.toHexString(given[index]));
          }
          count++;
        }
      }
    }
    Log.d(TAG, "Number of different pixels : " + count);

    return (count == 0);
  }
}
