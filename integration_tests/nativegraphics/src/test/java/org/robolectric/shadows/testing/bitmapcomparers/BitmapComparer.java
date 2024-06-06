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

/** This abstract class can be used by the tester to implement their own comparison methods */
public abstract class BitmapComparer {
  /**
   * Compares the two bitmaps given using Java.
   *
   * @param offset where in the bitmaps to start
   * @param stride how much to skip between two different rows
   * @param width the width of the subsection being tested
   * @param height the height of the subsection being tested
   */
  public abstract boolean verifySame(
      int[] ideal, int[] given, int offset, int stride, int width, int height);

  /**
   * This calculates the position in an array that would represent a bitmap given the parameters.
   */
  protected static int indexFromXAndY(int x, int y, int stride, int offset) {
    return x + (y * stride) + offset;
  }
}
