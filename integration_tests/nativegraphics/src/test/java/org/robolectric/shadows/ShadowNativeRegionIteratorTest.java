/*
 * Copyright (C) 2008 The Android Open Source Project
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
 *
 * These tests are taken from
 * https://cs.android.com/android/platform/superproject/+/master:cts/tests/tests/graphics/src/android/graphics/cts/RegionTest.java
 */

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeRegionIteratorTest {
  @Test
  public void testIterateRegion() {
    final Region region = new Region(1, 2, 3, 4);
    final RegionIterator it = new RegionIterator(region);
    final Rect rect = new Rect();
    while (it.next(rect)) {
      // Unused
    }
  }
}
