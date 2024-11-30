/*
 * Copyright (C) 2018 The Android Open Source Project
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

import static org.junit.Assert.assertFalse;

import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.List;

public class RegionVerifier extends BitmapVerifier {
  private static class SubRegionVerifiers {
    public Region region;
    public BitmapVerifier verifier;

    SubRegionVerifiers(Region region, BitmapVerifier verifier) {
      this.region = region;
      this.verifier = verifier;
    }
  }

  private final List<SubRegionVerifiers> regionVerifiers = new ArrayList<>();

  @Override
  public boolean verify(int[] bitmap, int offset, int stride, int width, int height) {
    assertFalse(regionVerifiers.isEmpty());
    boolean isVerified = true;
    for (SubRegionVerifiers subRegionVerifier : regionVerifiers) {
      if (subRegionVerifier.region.isRect()) {
        Rect area = subRegionVerifier.region.getBounds();
        isVerified &= verifySubRect(bitmap, offset, stride, subRegionVerifier.verifier, area);
      } else {
        RegionIterator iterator = new RegionIterator(subRegionVerifier.region);
        Rect area = new Rect();
        while (iterator.next(area)) {
          isVerified &= verifySubRect(bitmap, offset, stride, subRegionVerifier.verifier, area);
        }
      }
    }
    return isVerified;
  }

  private boolean verifySubRect(
      int[] bitmap, int offset, int stride, BitmapVerifier subVerifier, Rect rect) {
    final int newOffset = rect.top * stride + rect.left + offset;
    return subVerifier.verify(bitmap, newOffset, stride, rect.width(), rect.height());
  }

  @CanIgnoreReturnValue
  public RegionVerifier addVerifier(Rect area, BitmapVerifier verifier) {
    return addVerifier(new Region(area), verifier);
  }

  @CanIgnoreReturnValue
  public RegionVerifier addVerifier(Region area, BitmapVerifier verifier) {
    regionVerifiers.add(new SubRegionVerifiers(area, verifier));
    return this;
  }
}
