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
package org.robolectric.shadows.testing.differencevisualizers;

import android.graphics.Color;

/** This class creates difference maps that show which pixels were correct, and which weren't */
public class PassFailVisualizer extends DifferenceVisualizer {
  /**
   * This method will return a bitmap where white is same red is different
   *
   * @param ideal the desired result
   * @param given the produced result
   */
  @Override
  public int[] getDifferences(int[] ideal, int[] given) {
    int[] output = new int[ideal.length];
    for (int y = 0; y < output.length; y++) {
      if (ideal[y] == given[y]) {
        output[y] = Color.WHITE;
      } else {
        output[y] = Color.RED;
      }
    }
    return output;
  }
}
