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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.robolectric.shadows.testing.bitmapcomparers.BitmapComparer;
import org.robolectric.shadows.testing.differencevisualizers.PassFailVisualizer;

public class GoldenImageVerifier extends BitmapVerifier {
  private final BitmapComparer bitmapComparer;
  private final int[] goldenBitmapArray;
  private final int width;
  private final int height;

  public GoldenImageVerifier(Bitmap goldenBitmap, BitmapComparer bitmapComparer) {
    width = goldenBitmap.getWidth();
    height = goldenBitmap.getHeight();
    goldenBitmapArray = new int[width * height];
    goldenBitmap.getPixels(goldenBitmapArray, 0, width, 0, 0, width, height);
    this.bitmapComparer = bitmapComparer;
  }

  public GoldenImageVerifier(Context context, int goldenResId, BitmapComparer bitmapComparer) {
    this(BitmapFactory.decodeResource(context.getResources(), goldenResId), bitmapComparer);
  }

  @Override
  public boolean verify(Bitmap bitmap) {
    // Clip to the size of the golden image.
    if (bitmap.getWidth() > width || bitmap.getHeight() > height) {
      bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
    }
    return super.verify(bitmap);
  }

  @Override
  public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {
    boolean success =
        bitmapComparer.verifySame(goldenBitmapArray, bitmap, offset, stride, width, height);
    if (!success) {
      differenceBitmapBase = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
      int[] differences = new PassFailVisualizer().getDifferences(goldenBitmapArray, bitmap);
      differenceBitmapBase.setPixels(differences, 0, width, 0, 0, width, height);
    }
    return success;
  }
}
