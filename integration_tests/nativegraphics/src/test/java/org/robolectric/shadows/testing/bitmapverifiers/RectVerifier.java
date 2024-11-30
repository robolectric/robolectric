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

import android.graphics.Rect;

/** Tests to see if there is rectangle of a certain color, with a background given */
public class RectVerifier extends PerPixelBitmapVerifier {
  private final int outerColor;
  private final int innerColor;
  private final Rect innerRect;

  public RectVerifier(int outerColor, int innerColor, Rect innerRect) {
    this(outerColor, innerColor, innerRect, DEFAULT_THRESHOLD);
  }

  public RectVerifier(int outerColor, int innerColor, Rect innerRect, int tolerance) {
    super(tolerance);
    this.outerColor = outerColor;
    this.innerColor = innerColor;
    this.innerRect = innerRect;
  }

  @Override
  protected int getExpectedColor(int x, int y) {
    return innerRect.contains(x, y) ? innerColor : outerColor;
  }
}
