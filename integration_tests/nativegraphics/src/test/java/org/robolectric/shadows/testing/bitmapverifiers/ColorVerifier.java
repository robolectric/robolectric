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

import androidx.annotation.ColorInt;

/** Checks to see if a bitmap is entirely a single color */
public class ColorVerifier extends PerPixelBitmapVerifier {
  @ColorInt private final int color;

  public ColorVerifier(@ColorInt int color) {
    this(color, DEFAULT_THRESHOLD);
  }

  public ColorVerifier(@ColorInt int color, int colorTolerance) {
    super(colorTolerance);
    this.color = color;
  }

  public ColorVerifier(@ColorInt int color, int colorThreshold, float spatialTolerance) {
    super(colorThreshold, spatialTolerance);
    this.color = color;
  }

  @Override
  @ColorInt
  protected int getExpectedColor(int x, int y) {
    return color;
  }
}
