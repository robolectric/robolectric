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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = O)
public class ShadowNativeRegionTest {
  // DIFFERENCE
  private static final int[][] DIFFERENCE_WITH1 = {
    {0, 0}, {4, 4}, {10, 10}, {19, 19}, {19, 0}, {10, 4}, {4, 10}, {0, 19}
  };
  private static final int[][] DIFFERENCE_WITHOUT1 = {{5, 5}, {9, 9}, {9, 5}, {5, 9}};

  private static final int[][] DIFFERENCE_WITH2 = {
    {0, 0}, {19, 0}, {9, 9}, {19, 9}, {0, 19}, {9, 19}
  };
  private static final int[][] DIFFERENCE_WITHOUT2 = {
    {10, 10}, {19, 10}, {10, 19}, {19, 19}, {29, 10}, {29, 29}, {10, 29}
  };

  private static final int[][] DIFFERENCE_WITH3 = {{0, 0}, {19, 0}, {0, 19}, {19, 19}};
  private static final int[][] DIFFERENCE_WITHOUT3 = {{40, 40}, {40, 59}, {59, 40}, {59, 59}};

  // INTERSECT
  private static final int[][] INTERSECT_WITH1 = {{5, 5}, {9, 9}, {9, 5}, {5, 9}};
  private static final int[][] INTERSECT_WITHOUT1 = {
    {0, 0}, {2, 2}, {4, 4}, {10, 10}, {19, 19}, {19, 0}, {10, 4}, {4, 10}, {0, 19}
  };

  private static final int[][] INTERSECT_WITH2 = {{10, 10}, {19, 10}, {10, 19}, {19, 19}};
  private static final int[][] INTERSECT_WITHOUT2 = {
    {0, 0}, {19, 0}, {9, 9}, {19, 9}, {0, 19}, {9, 19}, {29, 10}, {29, 29}, {10, 29}
  };

  // UNION
  private static final int[][] UNION_WITH1 = {
    {0, 0}, {2, 2}, {4, 4}, {6, 6}, {10, 10}, {19, 19}, {19, 0}, {10, 4}, {4, 10}, {0, 19}, {5, 5},
    {9, 9}, {9, 5}, {5, 9}
  };
  private static final int[][] UNION_WITHOUT1 = {{0, 20}, {20, 20}, {20, 0}};

  private static final int[][] UNION_WITH2 = {
    {0, 0}, {2, 2}, {19, 0}, {9, 9}, {19, 9}, {0, 19}, {9, 19}, {21, 21}, {10, 10}, {19, 10},
    {10, 19}, {19, 19}, {29, 10}, {29, 29}, {10, 29}
  };
  private static final int[][] UNION_WITHOUT2 = {
    {0, 29}, {0, 20}, {9, 29}, {9, 20},
    {29, 0}, {20, 0}, {29, 9}, {20, 9}
  };

  private static final int[][] UNION_WITH3 = {
    {0, 0}, {2, 2}, {19, 0}, {0, 19}, {19, 19},
    {40, 40}, {41, 41}, {40, 59}, {59, 40}, {59, 59}
  };
  private static final int[][] UNION_WITHOUT3 = {{20, 20}, {39, 39}};

  // XOR
  private static final int[][] XOR_WITH1 = {
    {0, 0}, {2, 2}, {4, 4}, {10, 10}, {19, 19}, {19, 0}, {10, 4}, {4, 10}, {0, 19}
  };
  private static final int[][] XOR_WITHOUT1 = {{5, 5}, {6, 6}, {9, 9}, {9, 5}, {5, 9}};

  private static final int[][] XOR_WITH2 = {
    {0, 0}, {2, 2}, {19, 0}, {9, 9}, {19, 9}, {0, 19}, {9, 19}, {21, 21}, {29, 10}, {10, 29},
    {20, 10}, {10, 20}, {20, 20}, {29, 29}
  };
  private static final int[][] XOR_WITHOUT2 = {{10, 10}, {11, 11}, {19, 10}, {10, 19}, {19, 19}};

  private static final int[][] XOR_WITH3 = {
    {0, 0}, {2, 2}, {19, 0}, {0, 19}, {19, 19},
    {40, 40}, {41, 41}, {40, 59}, {59, 40}, {59, 59}
  };
  private static final int[][] XOR_WITHOUT3 = {{20, 20}, {39, 39}};

  // REVERSE_DIFFERENCE
  private static final int[][] REVERSE_DIFFERENCE_WITH2 = {
    {29, 10}, {10, 29}, {20, 10}, {10, 20}, {20, 20}, {29, 29}, {21, 21}
  };
  private static final int[][] REVERSE_DIFFERENCE_WITHOUT2 = {
    {0, 0}, {19, 0}, {0, 19}, {19, 19}, {2, 2}, {11, 11}
  };

  private static final int[][] REVERSE_DIFFERENCE_WITH3 = {
    {40, 40}, {40, 59}, {59, 40}, {59, 59}, {41, 41}
  };
  private static final int[][] REVERSE_DIFFERENCE_WITHOUT3 = {
    {0, 0}, {19, 0}, {0, 19}, {19, 19}, {20, 20}, {39, 39}, {2, 2}
  };

  private Region region;

  private void verifyPointsInsideRegion(int[][] area) {
    for (int[] ints : area) {
      assertTrue(region.contains(ints[0], ints[1]));
    }
  }

  private void verifyPointsOutsideRegion(int[][] area) {
    for (int[] ints : area) {
      assertFalse(region.contains(ints[0], ints[1]));
    }
  }

  @Before
  public void setup() {
    region = new Region();
  }

  @Test
  public void testConstructor() {
    // We don't actually care about the result of quickContains in this function (tested later).
    // We call it because in robolectric native runtime, it's not static and so if the constructor
    // is set up incorrectly, it will crash trying to get the instance from mNativeRegion.
    Rect rect = new Rect();

    // Test Region()
    Region defaultRegion = new Region();
    defaultRegion.quickContains(rect);

    // Test Region(Region)
    Region oriRegion = new Region();
    Region copyRegion = new Region(oriRegion);
    copyRegion.quickContains(rect);

    // Test Region(Rect)
    Region rectRegion = new Region(rect);
    rectRegion.quickContains(rect);

    // Test Region(int, int, int, int)
    Region intRegion = new Region(0, 0, 100, 100);
    intRegion.quickContains(rect);
  }

  @Test
  public void testSet1() {
    Rect rect = new Rect(1, 2, 3, 4);
    Region oriRegion = new Region(rect);
    assertTrue(region.set(oriRegion));
    assertEquals(1, region.getBounds().left);
    assertEquals(2, region.getBounds().top);
    assertEquals(3, region.getBounds().right);
    assertEquals(4, region.getBounds().bottom);
  }

  @Test
  public void testSet2() {
    Rect rect = new Rect(1, 2, 3, 4);
    assertTrue(region.set(rect));
    assertEquals(1, region.getBounds().left);
    assertEquals(2, region.getBounds().top);
    assertEquals(3, region.getBounds().right);
    assertEquals(4, region.getBounds().bottom);
  }

  @Test
  public void testSet3() {
    assertTrue(region.set(1, 2, 3, 4));
    assertEquals(1, region.getBounds().left);
    assertEquals(2, region.getBounds().top);
    assertEquals(3, region.getBounds().right);
    assertEquals(4, region.getBounds().bottom);
  }

  @Test
  public void testIsRect() {
    assertFalse(region.isRect());
    region = new Region(1, 2, 3, 4);
    assertTrue(region.isRect());
  }

  @Test
  public void testIsComplex() {
    // Region is empty
    assertFalse(region.isComplex());

    // Only one rectangle
    region = new Region();
    region.set(1, 2, 3, 4);
    assertFalse(region.isComplex());

    // More than one rectangle
    region = new Region();
    region.set(1, 1, 2, 2);
    region.union(new Rect(3, 3, 5, 5));
    assertTrue(region.isComplex());
  }

  @Test
  public void testQuickContains1() {
    Rect rect = new Rect(1, 2, 3, 4);
    // This region not contains expected rectangle.
    assertFalse(region.quickContains(rect));
    region.set(rect);
    // This region contains only one rectangle and it is the expected one.
    assertTrue(region.quickContains(rect));
    region.set(5, 6, 7, 8);
    // This region contains more than one rectangle.
    assertFalse(region.quickContains(rect));
  }

  @Test
  public void testQuickContains2() {
    // This region not contains expected rectangle.
    assertFalse(region.quickContains(1, 2, 3, 4));
    region.set(1, 2, 3, 4);
    // This region contains only one rectangle and it is the expected one.
    assertTrue(region.quickContains(1, 2, 3, 4));
    region.set(5, 6, 7, 8);
    // This region contains more than one rectangle.
    assertFalse(region.quickContains(1, 2, 3, 4));
  }

  @Test
  public void testUnion() {
    Rect rect1 = new Rect();
    Rect rect2 = new Rect(0, 0, 20, 20);
    Rect rect3 = new Rect(5, 5, 10, 10);
    Rect rect4 = new Rect(10, 10, 30, 30);
    Rect rect5 = new Rect(40, 40, 60, 60);

    // union (inclusive-or) the two regions
    region.set(rect2);
    // union null rectangle
    assertTrue(region.contains(6, 6));
    assertTrue(region.union(rect1));
    assertTrue(region.contains(6, 6));

    // 1. union rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.union(rect3));
    verifyPointsInsideRegion(UNION_WITH1);
    verifyPointsOutsideRegion(UNION_WITHOUT1);

    // 2. union rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(21, 21));
    assertTrue(region.union(rect4));
    verifyPointsInsideRegion(UNION_WITH2);
    verifyPointsOutsideRegion(UNION_WITHOUT2);

    // 3. union rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.union(rect5));
    verifyPointsInsideRegion(UNION_WITH3);
    verifyPointsOutsideRegion(UNION_WITHOUT3);
  }

  @Test
  public void testContains() {
    region.set(2, 2, 5, 5);
    // Not contain (1, 1).
    assertFalse(region.contains(1, 1));

    // Test point inside this region.
    assertTrue(region.contains(3, 3));

    // Test left-top corner.
    assertTrue(region.contains(2, 2));

    // Test left-bottom corner.
    assertTrue(region.contains(2, 4));

    // Test right-top corner.
    assertTrue(region.contains(4, 2));

    // Test right-bottom corner.
    assertTrue(region.contains(4, 4));

    // Though you set 5, but 5 is not contained by this region.
    assertFalse(region.contains(5, 5));
    assertFalse(region.contains(2, 5));
    assertFalse(region.contains(5, 2));

    // Set a new rectangle.
    region.set(6, 6, 8, 8);
    assertFalse(region.contains(3, 3));
    assertTrue(region.contains(7, 7));
  }

  @Test
  public void testEmpty() {
    assertTrue(region.isEmpty());
    region = null;
    region = new Region(1, 2, 3, 4);
    assertFalse(region.isEmpty());
    region.setEmpty();
    assertTrue(region.isEmpty());
  }

  @Test
  public void testGetBoundsNull() {
    assertThrows(NullPointerException.class, () -> region.getBounds(null));
  }

  @Test
  public void testGetBounds() {
    // Normal, return true.
    Rect rect1 = new Rect(1, 2, 3, 4);
    region = new Region(rect1);
    assertTrue(region.getBounds(rect1));

    region.setEmpty();
    Rect rect2 = new Rect(5, 6, 7, 8);
    assertFalse(region.getBounds(rect2));
  }

  @Test
  public void testOp1() {
    Rect rect1 = new Rect();
    Rect rect2 = new Rect(0, 0, 20, 20);
    Rect rect3 = new Rect(5, 5, 10, 10);
    Rect rect4 = new Rect(10, 10, 30, 30);
    Rect rect5 = new Rect(40, 40, 60, 60);

    verifyNullRegionOp1(rect1);
    verifyDifferenceOp1(rect1, rect2, rect3, rect4, rect5);
    verifyIntersectOp1(rect1, rect2, rect3, rect4, rect5);
    verifyUnionOp1(rect1, rect2, rect3, rect4, rect5);
    verifyXorOp1(rect1, rect2, rect3, rect4, rect5);
    verifyReverseDifferenceOp1(rect1, rect2, rect3, rect4, rect5);
    verifyReplaceOp1(rect1, rect2, rect3, rect4, rect5);
  }

  private void verifyNullRegionOp1(Rect rect1) {
    // Region without rectangle
    region = new Region();
    assertFalse(region.op(rect1, Region.Op.DIFFERENCE));
    assertFalse(region.op(rect1, Region.Op.INTERSECT));
    assertFalse(region.op(rect1, Region.Op.UNION));
    assertFalse(region.op(rect1, Region.Op.XOR));
    assertFalse(region.op(rect1, Region.Op.REVERSE_DIFFERENCE));
    assertFalse(region.op(rect1, Region.Op.REPLACE));
  }

  private void verifyDifferenceOp1(Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // DIFFERENCE, Region with rectangle
    // subtract the op region from the first region
    region = new Region();
    // subtract null rectangle
    region.set(rect2);
    assertTrue(region.op(rect1, Region.Op.DIFFERENCE));

    // 1. subtract rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(rect3, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH1);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT1);

    // 2. subtract rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(11, 11));
    assertTrue(region.op(rect4, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT2);

    // 3. subtract rectangle out of this region
    region.set(rect2);
    assertTrue(region.op(rect5, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT3);
  }

  private void verifyIntersectOp1(Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // INTERSECT, Region with rectangle
    // intersect the two regions
    region = new Region();
    // intersect null rectangle
    region.set(rect2);
    assertFalse(region.op(rect1, Region.Op.INTERSECT));

    // 1. intersect rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.op(rect3, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH1);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT1);

    // 2. intersect rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(9, 9));
    assertTrue(region.op(rect4, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH2);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT2);

    // 3. intersect rectangle out of this region
    region.set(rect2);
    assertFalse(region.op(rect5, Region.Op.INTERSECT));
  }

  private void verifyUnionOp1(Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // UNION, Region with rectangle
    // union (inclusive-or) the two regions
    region = new Region();
    region.set(rect2);
    // union null rectangle
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(rect1, Region.Op.UNION));
    assertTrue(region.contains(6, 6));

    // 1. union rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(rect3, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH1);
    verifyPointsOutsideRegion(UNION_WITHOUT1);

    // 2. union rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(rect4, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH2);
    verifyPointsOutsideRegion(UNION_WITHOUT2);

    // 3. union rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(rect5, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH3);
    verifyPointsOutsideRegion(UNION_WITHOUT3);
  }

  private void verifyXorOp1(Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // XOR, Region with rectangle
    // exclusive-or the two regions
    region = new Region();
    // xor null rectangle
    region.set(rect2);
    assertTrue(region.op(rect1, Region.Op.XOR));

    // 1. xor rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(rect3, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH1);
    verifyPointsOutsideRegion(XOR_WITHOUT1);

    // 2. xor rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(11, 11));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(rect4, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH2);
    verifyPointsOutsideRegion(XOR_WITHOUT2);

    // 3. xor rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(rect5, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH3);
    verifyPointsOutsideRegion(XOR_WITHOUT3);
  }

  private void verifyReverseDifferenceOp1(
      Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // REVERSE_DIFFERENCE, Region with rectangle
    // reverse difference the first region from the op region
    region = new Region();
    region.set(rect2);
    // reverse difference null rectangle
    assertFalse(region.op(rect1, Region.Op.REVERSE_DIFFERENCE));

    // 1. reverse difference rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertFalse(region.op(rect3, Region.Op.REVERSE_DIFFERENCE));

    // 2. reverse difference rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(11, 11));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(rect4, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT2);

    // 3. reverse difference rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(rect5, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT3);
  }

  private void verifyReplaceOp1(Rect rect1, Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // REPLACE, Region with rectangle
    // replace the dst region with the op region
    region = new Region();
    region.set(rect2);
    // subtract null rectangle
    assertFalse(region.op(rect1, Region.Op.REPLACE));
    // subtract rectangle inside this region
    region.set(rect2);
    assertEquals(rect2, region.getBounds());
    assertTrue(region.op(rect3, Region.Op.REPLACE));
    assertNotSame(rect2, region.getBounds());
    assertEquals(rect3, region.getBounds());
    // subtract rectangle overlap this region
    region.set(rect2);
    assertEquals(rect2, region.getBounds());
    assertTrue(region.op(rect4, Region.Op.REPLACE));
    assertNotSame(rect2, region.getBounds());
    assertEquals(rect4, region.getBounds());
    // subtract rectangle out of this region
    region.set(rect2);
    assertEquals(rect2, region.getBounds());
    assertTrue(region.op(rect5, Region.Op.REPLACE));
    assertNotSame(rect2, region.getBounds());
    assertEquals(rect5, region.getBounds());
  }

  @Test
  public void testOp2() {
    Rect rect2 = new Rect(0, 0, 20, 20);
    Rect rect3 = new Rect(5, 5, 10, 10);
    Rect rect4 = new Rect(10, 10, 30, 30);
    Rect rect5 = new Rect(40, 40, 60, 60);

    verifyNullRegionOp2();
    verifyDifferenceOp2(rect2);
    verifyIntersectOp2(rect2);
    verifyUnionOp2(rect2);
    verifyXorOp2(rect2);
    verifyReverseDifferenceOp2(rect2);
    verifyReplaceOp2(rect2, rect3, rect4, rect5);
  }

  private void verifyNullRegionOp2() {
    // Region without rectangle
    region = new Region();
    assertFalse(region.op(0, 0, 0, 0, Region.Op.DIFFERENCE));
    assertFalse(region.op(0, 0, 0, 0, Region.Op.INTERSECT));
    assertFalse(region.op(0, 0, 0, 0, Region.Op.UNION));
    assertFalse(region.op(0, 0, 0, 0, Region.Op.XOR));
    assertFalse(region.op(0, 0, 0, 0, Region.Op.REVERSE_DIFFERENCE));
    assertFalse(region.op(0, 0, 0, 0, Region.Op.REPLACE));
  }

  private void verifyDifferenceOp2(Rect rect2) {
    // DIFFERENCE, Region with rectangle
    // subtract the op region from the first region
    region = new Region();
    // subtract null rectangle
    region.set(rect2);
    assertTrue(region.op(0, 0, 0, 0, Region.Op.DIFFERENCE));

    // 1. subtract rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(5, 5, 10, 10, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH1);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT1);

    // 2. subtract rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(11, 11));
    assertTrue(region.op(10, 10, 30, 30, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT2);

    // 3. subtract rectangle out of this region
    region.set(rect2);
    assertTrue(region.op(40, 40, 60, 60, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT3);
  }

  private void verifyIntersectOp2(Rect rect2) {
    // INTERSECT, Region with rectangle
    // intersect the two regions
    region = new Region();
    // intersect null rectangle
    region.set(rect2);
    assertFalse(region.op(0, 0, 0, 0, Region.Op.INTERSECT));

    // 1. intersect rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.op(5, 5, 10, 10, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH1);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT1);

    // 2. intersect rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(9, 9));
    assertTrue(region.op(10, 10, 30, 30, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH2);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT2);

    // 3. intersect rectangle out of this region
    region.set(rect2);
    assertFalse(region.op(40, 40, 60, 60, Region.Op.INTERSECT));
  }

  private void verifyUnionOp2(Rect rect2) {
    // UNION, Region with rectangle
    // union (inclusive-or) the two regions
    region = new Region();
    region.set(rect2);
    // union null rectangle
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(0, 0, 0, 0, Region.Op.UNION));
    assertTrue(region.contains(6, 6));

    // 1. union rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(5, 5, 10, 10, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH1);
    verifyPointsOutsideRegion(UNION_WITHOUT1);

    // 2. union rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(10, 10, 30, 30, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH2);
    verifyPointsOutsideRegion(UNION_WITHOUT2);

    // 3. union rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(40, 40, 60, 60, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH3);
    verifyPointsOutsideRegion(UNION_WITHOUT3);
  }

  private void verifyXorOp2(Rect rect2) {
    // XOR, Region with rectangle
    // exclusive-or the two regions
    region = new Region();
    region.set(rect2);
    // xor null rectangle
    assertTrue(region.op(0, 0, 0, 0, Region.Op.XOR));

    // 1. xor rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(5, 5, 10, 10, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH1);
    verifyPointsOutsideRegion(XOR_WITHOUT1);

    // 2. xor rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(11, 11));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(10, 10, 30, 30, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH2);
    verifyPointsOutsideRegion(XOR_WITHOUT2);

    // 3. xor rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(40, 40, 60, 60, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH3);
    verifyPointsOutsideRegion(XOR_WITHOUT3);
  }

  private void verifyReverseDifferenceOp2(Rect rect2) {
    // REVERSE_DIFFERENCE, Region with rectangle
    // reverse difference the first region from the op region
    region = new Region();
    region.set(rect2);
    // reverse difference null rectangle
    assertFalse(region.op(0, 0, 0, 0, Region.Op.REVERSE_DIFFERENCE));
    // reverse difference rectangle inside this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertFalse(region.op(5, 5, 10, 10, Region.Op.REVERSE_DIFFERENCE));
    // reverse difference rectangle overlap this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(11, 11));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(10, 10, 30, 30, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT2);
    // reverse difference rectangle out of this region
    region.set(rect2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(40, 40, 60, 60, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT3);
  }

  private void verifyReplaceOp2(Rect rect2, Rect rect3, Rect rect4, Rect rect5) {
    // REPLACE, Region w1ith rectangle
    // replace the dst region with the op region
    region = new Region();
    region.set(rect2);
    // subtract null rectangle
    assertFalse(region.op(0, 0, 0, 0, Region.Op.REPLACE));
    // subtract rectangle inside this region
    region.set(rect2);
    assertEquals(rect2, region.getBounds());
    assertTrue(region.op(5, 5, 10, 10, Region.Op.REPLACE));
    assertNotSame(rect2, region.getBounds());
    assertEquals(rect3, region.getBounds());
    // subtract rectangle overlap this region
    region.set(rect2);
    assertEquals(rect2, region.getBounds());
    assertTrue(region.op(10, 10, 30, 30, Region.Op.REPLACE));
    assertNotSame(rect2, region.getBounds());
    assertEquals(rect4, region.getBounds());
    // subtract rectangle out of this region
    region.set(rect2);
    assertEquals(rect2, region.getBounds());
    assertTrue(region.op(40, 40, 60, 60, Region.Op.REPLACE));
    assertNotSame(rect2, region.getBounds());
    assertEquals(rect5, region.getBounds());
  }

  @Test
  public void testOp3() {
    Region region1 = new Region();
    Region region2 = new Region(0, 0, 20, 20);
    Region region3 = new Region(5, 5, 10, 10);
    Region region4 = new Region(10, 10, 30, 30);
    Region region5 = new Region(40, 40, 60, 60);

    verifyNullRegionOp3(region1);
    verifyDifferenceOp3(region1, region2, region3, region4, region5);
    verifyIntersectOp3(region1, region2, region3, region4, region5);
    verifyUnionOp3(region1, region2, region3, region4, region5);
    verifyXorOp3(region1, region2, region3, region4, region5);
    verifyReverseDifferenceOp3(region1, region2, region3, region4, region5);
    verifyReplaceOp3(region1, region2, region3, region4, region5);
  }

  private void verifyNullRegionOp3(Region region1) {
    // Region without rectangle
    region = new Region();
    assertFalse(region.op(region1, Region.Op.DIFFERENCE));
    assertFalse(region.op(region1, Region.Op.INTERSECT));
    assertFalse(region.op(region1, Region.Op.UNION));
    assertFalse(region.op(region1, Region.Op.XOR));
    assertFalse(region.op(region1, Region.Op.REVERSE_DIFFERENCE));
    assertFalse(region.op(region1, Region.Op.REPLACE));
  }

  private void verifyDifferenceOp3(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // DIFFERENCE, Region with rectangle
    // subtract the op region from the first region
    region = new Region();
    // subtract null rectangle
    region.set(region2);
    assertTrue(region.op(region1, Region.Op.DIFFERENCE));

    // 1. subtract rectangle inside this region
    region.set(region2);
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(region3, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH1);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT1);

    // 2. subtract rectangle overlap this region
    region.set(region2);
    assertTrue(region.contains(11, 11));
    assertTrue(region.op(region4, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT2);

    // 3. subtract rectangle out of this region
    region.set(region2);
    assertTrue(region.op(region5, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT3);
  }

  private void verifyIntersectOp3(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // INTERSECT, Region with rectangle
    // intersect the two regions
    region = new Region();
    region.set(region2);
    // intersect null rectangle
    assertFalse(region.op(region1, Region.Op.INTERSECT));

    // 1. intersect rectangle inside this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.op(region3, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH1);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT1);

    // 2. intersect rectangle overlap this region
    region.set(region2);
    assertTrue(region.contains(9, 9));
    assertTrue(region.op(region4, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH2);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT2);

    // 3. intersect rectangle out of this region
    region.set(region2);
    assertFalse(region.op(region5, Region.Op.INTERSECT));
  }

  private void verifyUnionOp3(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // UNION, Region with rectangle
    // union (inclusive-or) the two regions
    region = new Region();
    // union null rectangle
    region.set(region2);
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(region1, Region.Op.UNION));
    assertTrue(region.contains(6, 6));

    // 1. union rectangle inside this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(region3, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH1);
    verifyPointsOutsideRegion(UNION_WITHOUT1);

    // 2. union rectangle overlap this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(region4, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH2);
    verifyPointsOutsideRegion(UNION_WITHOUT2);

    // 3. union rectangle out of this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(region5, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH3);
    verifyPointsOutsideRegion(UNION_WITHOUT3);
  }

  private void verifyXorOp3(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // XOR, Region with rectangle
    // exclusive-or the two regions
    region = new Region();
    // xor null rectangle
    region.set(region2);
    assertTrue(region.op(region1, Region.Op.XOR));

    // 1. xor rectangle inside this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertTrue(region.op(region3, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH1);
    verifyPointsOutsideRegion(XOR_WITHOUT1);

    // 2. xor rectangle overlap this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(11, 11));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(region4, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH2);
    verifyPointsOutsideRegion(XOR_WITHOUT2);

    // 3. xor rectangle out of this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(region5, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH3);
    verifyPointsOutsideRegion(XOR_WITHOUT3);
  }

  private void verifyReverseDifferenceOp3(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // REVERSE_DIFFERENCE, Region with rectangle
    // reverse difference the first region from the op region
    region = new Region();
    // reverse difference null rectangle
    region.set(region2);
    assertFalse(region.op(region1, Region.Op.REVERSE_DIFFERENCE));

    // 1. reverse difference rectangle inside this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(6, 6));
    assertFalse(region.op(region3, Region.Op.REVERSE_DIFFERENCE));

    // 2. reverse difference rectangle overlap this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertTrue(region.contains(11, 11));
    assertFalse(region.contains(21, 21));
    assertTrue(region.op(region4, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT2);

    // 3. reverse difference rectangle out of this region
    region.set(region2);
    assertTrue(region.contains(2, 2));
    assertFalse(region.contains(41, 41));
    assertTrue(region.op(region5, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT3);
  }

  private void verifyReplaceOp3(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // REPLACE, Region with rectangle
    // replace the dst region with the op region
    region = new Region();
    region.set(region2);
    // subtract null rectangle
    assertFalse(region.op(region1, Region.Op.REPLACE));
    // subtract rectangle inside this region
    region.set(region2);
    assertEquals(region2.getBounds(), region.getBounds());
    assertTrue(region.op(region3, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region3.getBounds(), region.getBounds());
    // subtract rectangle overlap this region
    region.set(region2);
    assertEquals(region2.getBounds(), region.getBounds());
    assertTrue(region.op(region4, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region4.getBounds(), region.getBounds());
    // subtract rectangle out of this region
    region.set(region2);
    assertEquals(region2.getBounds(), region.getBounds());
    assertTrue(region.op(region5, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region5.getBounds(), region.getBounds());
  }

  @Test
  public void testOp4() {
    Rect rect1 = new Rect();
    Rect rect2 = new Rect(0, 0, 20, 20);

    Region region1 = new Region();
    Region region2 = new Region(0, 0, 20, 20);
    Region region3 = new Region(5, 5, 10, 10);
    Region region4 = new Region(10, 10, 30, 30);
    Region region5 = new Region(40, 40, 60, 60);

    verifyNullRegionOp4(rect1, region1);
    verifyDifferenceOp4(rect1, rect2, region1, region3, region4, region5);
    verifyIntersectOp4(rect1, rect2, region1, region3, region4, region5);
    verifyUnionOp4(rect1, rect2, region1, region3, region4, region5);
    verifyXorOp4(rect1, rect2, region1, region3, region4, region5);
    verifyReverseDifferenceOp4(rect1, rect2, region1, region3, region4, region5);
    verifyReplaceOp4(rect1, rect2, region1, region2, region3, region4, region5);
  }

  private void verifyNullRegionOp4(Rect rect1, Region region1) {
    // Region without rectangle
    region = new Region();
    assertFalse(region.op(rect1, region1, Region.Op.DIFFERENCE));
    assertFalse(region.op(rect1, region1, Region.Op.INTERSECT));
    assertFalse(region.op(rect1, region1, Region.Op.UNION));

    assertFalse(region.op(rect1, region1, Region.Op.XOR));
    assertFalse(region.op(rect1, region1, Region.Op.REVERSE_DIFFERENCE));
    assertFalse(region.op(rect1, region1, Region.Op.REPLACE));
  }

  private void verifyDifferenceOp4(
      Rect rect1, Rect rect2, Region region1, Region region3, Region region4, Region region5) {
    // DIFFERENCE, Region with rectangle
    // subtract the op region from the first region
    region = new Region();
    // subtract null rectangle
    assertTrue(region.op(rect2, region1, Region.Op.DIFFERENCE));

    // 1. subtract rectangle inside this region
    region.set(rect1);
    assertTrue(region.op(rect2, region3, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH1);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT1);

    // 2. subtract rectangle overlap this region
    region.set(rect1);
    assertTrue(region.op(rect2, region4, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT2);

    // 3. subtract rectangle out of this region
    region.set(rect1);
    assertTrue(region.op(rect2, region5, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT3);
  }

  private void verifyIntersectOp4(
      Rect rect1, Rect rect2, Region region1, Region region3, Region region4, Region region5) {
    // INTERSECT, Region with rectangle
    // intersect the two regions
    region = new Region();
    // intersect null rectangle
    region.set(rect1);
    assertFalse(region.op(rect2, region1, Region.Op.INTERSECT));

    // 1. intersect rectangle inside this region
    region.set(rect1);
    assertTrue(region.op(rect2, region3, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH1);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT1);

    // 2. intersect rectangle overlap this region
    region.set(rect1);
    assertTrue(region.op(rect2, region4, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH2);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT2);

    // 3. intersect rectangle out of this region
    region.set(rect1);
    assertFalse(region.op(rect2, region5, Region.Op.INTERSECT));
  }

  private void verifyUnionOp4(
      Rect rect1, Rect rect2, Region region1, Region region3, Region region4, Region region5) {
    // UNION, Region with rectangle
    // union (inclusive-or) the two regions
    region = new Region();
    // union null rectangle
    region.set(rect1);
    assertTrue(region.op(rect2, region1, Region.Op.UNION));
    assertTrue(region.contains(6, 6));

    // 1. union rectangle inside this region
    region.set(rect1);
    assertTrue(region.op(rect2, region3, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH1);
    verifyPointsOutsideRegion(UNION_WITHOUT1);

    // 2. union rectangle overlap this region
    region.set(rect1);
    assertTrue(region.op(rect2, region4, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH2);
    verifyPointsOutsideRegion(UNION_WITHOUT2);

    // 3. union rectangle out of this region
    region.set(rect1);
    assertTrue(region.op(rect2, region5, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH3);
    verifyPointsOutsideRegion(UNION_WITHOUT3);
  }

  private void verifyXorOp4(
      Rect rect1, Rect rect2, Region region1, Region region3, Region region4, Region region5) {
    // XOR, Region with rectangle
    // exclusive-or the two regions
    region = new Region();
    // xor null rectangle
    region.set(rect1);
    assertTrue(region.op(rect2, region1, Region.Op.XOR));

    // 1. xor rectangle inside this region
    region.set(rect1);
    assertTrue(region.op(rect2, region3, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH1);
    verifyPointsOutsideRegion(XOR_WITHOUT1);

    // 2. xor rectangle overlap this region
    region.set(rect1);
    assertTrue(region.op(rect2, region4, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH2);
    verifyPointsOutsideRegion(XOR_WITHOUT2);

    // 3. xor rectangle out of this region
    region.set(rect1);
    assertTrue(region.op(rect2, region5, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH3);
    verifyPointsOutsideRegion(XOR_WITHOUT3);
  }

  private void verifyReverseDifferenceOp4(
      Rect rect1, Rect rect2, Region region1, Region region3, Region region4, Region region5) {
    // REVERSE_DIFFERENCE, Region with rectangle
    // reverse difference the first region from the op region
    region = new Region();
    // reverse difference null rectangle
    region.set(rect1);
    assertFalse(region.op(rect2, region1, Region.Op.REVERSE_DIFFERENCE));

    // 1. reverse difference rectangle inside this region
    region.set(rect1);
    assertFalse(region.op(rect2, region3, Region.Op.REVERSE_DIFFERENCE));

    // 2. reverse difference rectangle overlap this region
    region.set(rect1);
    assertTrue(region.op(rect2, region4, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT2);

    // 3. reverse difference rectangle out of this region
    region.set(rect1);
    assertTrue(region.op(rect2, region5, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT3);
  }

  private void verifyReplaceOp4(
      Rect rect1,
      Rect rect2,
      Region region1,
      Region region2,
      Region region3,
      Region region4,
      Region region5) {
    // REPLACE, Region with rectangle
    // replace the dst region with the op region
    region = new Region();
    // subtract null rectangle
    region.set(rect1);
    assertFalse(region.op(rect2, region1, Region.Op.REPLACE));
    // subtract rectangle inside this region
    region.set(rect1);
    assertTrue(region.op(rect2, region3, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region3.getBounds(), region.getBounds());
    // subtract rectangle overlap this region
    region.set(rect1);
    assertTrue(region.op(rect2, region4, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region4.getBounds(), region.getBounds());
    // subtract rectangle out of this region
    region.set(rect1);
    assertTrue(region.op(rect2, region5, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region5.getBounds(), region.getBounds());
  }

  @Test
  public void testOp5() {
    Region region1 = new Region();
    Region region2 = new Region(0, 0, 20, 20);
    Region region3 = new Region(5, 5, 10, 10);
    Region region4 = new Region(10, 10, 30, 30);
    Region region5 = new Region(40, 40, 60, 60);

    verifyNullRegionOp5(region1);
    verifyDifferenceOp5(region1, region2, region3, region4, region5);
    verifyIntersectOp5(region1, region2, region3, region4, region5);
    verifyUnionOp5(region1, region2, region3, region4, region5);
    verifyXorOp5(region1, region2, region3, region4, region5);
    verifyReverseDifferenceOp5(region1, region2, region3, region4, region5);
    verifyReplaceOp5(region1, region2, region3, region4, region5);
  }

  private void verifyNullRegionOp5(Region region1) {
    // Region without rectangle
    region = new Region();
    assertFalse(region.op(region, region1, Region.Op.DIFFERENCE));
    assertFalse(region.op(region, region1, Region.Op.INTERSECT));
    assertFalse(region.op(region, region1, Region.Op.UNION));
    assertFalse(region.op(region, region1, Region.Op.XOR));
    assertFalse(region.op(region, region1, Region.Op.REVERSE_DIFFERENCE));
    assertFalse(region.op(region, region1, Region.Op.REPLACE));
  }

  private void verifyDifferenceOp5(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // DIFFERENCE, Region with rectangle
    // subtract the op region from the first region
    region = new Region();
    // subtract null rectangle
    region.set(region1);
    assertTrue(region.op(region2, region1, Region.Op.DIFFERENCE));

    // 1. subtract rectangle inside this region
    region.set(region1);
    assertTrue(region.op(region2, region3, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH1);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT1);

    // 2. subtract rectangle overlap this region
    region.set(region1);
    assertTrue(region.op(region2, region4, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT2);

    // 3. subtract rectangle out of this region
    region.set(region1);
    assertTrue(region.op(region2, region5, Region.Op.DIFFERENCE));
    verifyPointsInsideRegion(DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(DIFFERENCE_WITHOUT3);
  }

  private void verifyIntersectOp5(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // INTERSECT, Region with rectangle
    // intersect the two regions
    region = new Region();
    // intersect null rectangle
    region.set(region1);
    assertFalse(region.op(region2, region1, Region.Op.INTERSECT));

    // 1. intersect rectangle inside this region
    region.set(region1);
    assertTrue(region.op(region2, region3, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH1);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT1);

    // 2. intersect rectangle overlap this region
    region.set(region1);
    assertTrue(region.op(region2, region4, Region.Op.INTERSECT));
    verifyPointsInsideRegion(INTERSECT_WITH2);
    verifyPointsOutsideRegion(INTERSECT_WITHOUT2);

    // 3. intersect rectangle out of this region
    region.set(region1);
    assertFalse(region.op(region2, region5, Region.Op.INTERSECT));
  }

  private void verifyUnionOp5(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // UNION, Region with rectangle
    // union (inclusive-or) the two regions
    region = new Region();
    // union null rectangle
    region.set(region1);
    assertTrue(region.op(region2, region1, Region.Op.UNION));
    assertTrue(region.contains(6, 6));

    // 1. union rectangle inside this region
    region.set(region1);
    assertTrue(region.op(region2, region3, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH1);
    verifyPointsOutsideRegion(UNION_WITHOUT1);

    // 2. union rectangle overlap this region
    region.set(region1);
    assertTrue(region.op(region2, region4, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH2);
    verifyPointsOutsideRegion(UNION_WITHOUT2);

    // 3. union rectangle out of this region
    region.set(region1);
    assertTrue(region.op(region2, region5, Region.Op.UNION));
    verifyPointsInsideRegion(UNION_WITH3);
    verifyPointsOutsideRegion(UNION_WITHOUT3);
  }

  private void verifyXorOp5(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // XOR, Region with rectangle
    // exclusive-or the two regions
    region = new Region();
    // xor null rectangle
    region.set(region1);
    assertTrue(region.op(region2, region1, Region.Op.XOR));

    // 1. xor rectangle inside this region
    region.set(region1);
    assertTrue(region.op(region2, region3, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH1);
    verifyPointsOutsideRegion(XOR_WITHOUT1);

    // 2. xor rectangle overlap this region
    region.set(region1);
    assertTrue(region.op(region2, region4, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH2);
    verifyPointsOutsideRegion(XOR_WITHOUT2);

    // 3. xor rectangle out of this region
    region.set(region1);
    assertTrue(region.op(region2, region5, Region.Op.XOR));
    verifyPointsInsideRegion(XOR_WITH3);
    verifyPointsOutsideRegion(XOR_WITHOUT3);
  }

  private void verifyReverseDifferenceOp5(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // REVERSE_DIFFERENCE, Region with rectangle
    // reverse difference the first region from the op region
    region = new Region();
    // reverse difference null rectangle
    region.set(region1);
    assertFalse(region.op(region2, region1, Region.Op.REVERSE_DIFFERENCE));

    // 1. reverse difference rectangle inside this region
    region.set(region1);
    assertFalse(region.op(region2, region3, Region.Op.REVERSE_DIFFERENCE));

    // 2. reverse difference rectangle overlap this region
    region.set(region1);
    assertTrue(region.op(region2, region4, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH2);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT2);

    // 3. reverse difference rectangle out of this region
    region.set(region1);
    assertTrue(region.op(region2, region5, Region.Op.REVERSE_DIFFERENCE));
    verifyPointsInsideRegion(REVERSE_DIFFERENCE_WITH3);
    verifyPointsOutsideRegion(REVERSE_DIFFERENCE_WITHOUT3);
  }

  private void verifyReplaceOp5(
      Region region1, Region region2, Region region3, Region region4, Region region5) {
    // REPLACE, Region with rectangle
    // replace the dst region with the op region
    region = new Region();
    // subtract null rectangle
    region.set(region1);
    assertFalse(region.op(region2, region1, Region.Op.REPLACE));
    // subtract rectangle inside this region
    region.set(region1);
    assertTrue(region.op(region2, region3, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region3.getBounds(), region.getBounds());
    // subtract rectangle overlap this region
    region.set(region1);
    assertTrue(region.op(region2, region4, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region4.getBounds(), region.getBounds());
    // subtract rectangle out of this region
    region.set(region1);
    assertTrue(region.op(region2, region5, Region.Op.REPLACE));
    assertNotSame(region2.getBounds(), region.getBounds());
    assertEquals(region5.getBounds(), region.getBounds());
  }

  @Test
  public void testGetBoundaryPath1() {
    assertTrue(region.getBoundaryPath().isEmpty());

    // Both clip and path are non-null.
    Region clip = new Region(0, 0, 10, 10);
    Path path = new Path();
    path.addRect(0, 0, 10, 10, Path.Direction.CW);
    assertTrue(region.setPath(path, clip));
    assertFalse(region.getBoundaryPath().isEmpty());
  }

  @Test
  public void testGetBoundaryPath2() {
    Path path = new Path();
    assertFalse(region.getBoundaryPath(path));

    // path is null
    region = new Region(0, 0, 10, 10);
    path = new Path();
    assertTrue(region.getBoundaryPath(path));

    // region is null
    region = new Region();
    path = new Path();
    path.addRect(0, 0, 10, 10, Path.Direction.CW);
    assertFalse(region.getBoundaryPath(path));

    // both path and region are non-null
    region = new Region(0, 0, 10, 10);
    path = new Path();
    path.addRect(0, 0, 5, 5, Path.Direction.CW);
    assertTrue(region.getBoundaryPath(path));
  }

  @Test
  public void testSetPath() {
    // Both clip and path are null.
    Region clip = new Region();
    Path path = new Path();
    assertFalse(region.setPath(path, clip));

    // Only path is null.
    path = new Path();
    clip = new Region(0, 0, 10, 10);
    assertFalse(region.setPath(path, clip));

    // Only clip is null.
    clip = new Region();
    path = new Path();
    path.addRect(0, 0, 10, 10, Path.Direction.CW);
    assertFalse(region.setPath(path, clip));

    // Both clip and path are non-null.
    path = new Path();
    clip = new Region(0, 0, 10, 10);
    path.addRect(0, 0, 10, 10, Path.Direction.CW);
    assertTrue(region.setPath(path, clip));

    // Both clip and path are non-null.
    path = new Path();
    clip = new Region(0, 0, 5, 5);
    path.addRect(0, 0, 10, 10, Path.Direction.CW);
    assertTrue(region.setPath(path, clip));
    Rect expected = new Rect(0, 0, 5, 5);
    Rect unexpected = new Rect(0, 0, 10, 10);
    Rect actual = region.getBounds();
    assertEquals(expected.right, actual.right);
    assertNotSame(unexpected.right, actual.right);

    // Both clip and path are non-null.
    path = new Path();
    clip = new Region(0, 0, 10, 10);
    path.addRect(0, 0, 5, 5, Path.Direction.CW);
    assertTrue(region.setPath(path, clip));
    expected = new Rect(0, 0, 5, 5);
    unexpected = new Rect(0, 0, 10, 10);
    actual = region.getBounds();
    assertEquals(expected.right, actual.right);
    assertNotSame(unexpected.right, actual.right);
  }

  @Test
  public void testTranslate1() {
    Rect rect1 = new Rect(0, 0, 20, 20);
    Rect rect2 = new Rect(10, 10, 30, 30);
    region = new Region(0, 0, 20, 20);
    region.translate(10, 10);
    assertNotSame(rect1, region.getBounds());
    assertEquals(rect2, region.getBounds());
  }

  @Test
  public void testTranslate2() {
    Region dst = new Region();
    Rect rect1 = new Rect(0, 0, 20, 20);
    Rect rect2 = new Rect(10, 10, 30, 30);
    region = new Region(0, 0, 20, 20);
    region.translate(10, 10, dst);
    assertEquals(rect1, region.getBounds());
    assertNotSame(rect2, region.getBounds());
    assertNotSame(rect1, dst.getBounds());
    assertEquals(rect2, dst.getBounds());
  }

  @Test
  public void testDescribeContents() {
    int actual = region.describeContents();
    assertEquals(0, actual);
  }

  @Test
  public void testQuickReject1() {
    Rect oriRect = new Rect(0, 0, 20, 20);
    Rect rect1 = new Rect();
    Rect rect2 = new Rect(40, 40, 60, 60);
    Rect rect3 = new Rect(0, 0, 10, 10);
    Rect rect4 = new Rect(10, 10, 30, 30);

    // Return true if the region is empty
    assertTrue(region.quickReject(rect1));
    region.set(oriRect);
    assertTrue(region.quickReject(rect2));
    region.set(oriRect);
    assertFalse(region.quickReject(rect3));
    region.set(oriRect);
    assertFalse(region.quickReject(rect4));
  }

  @Test
  public void testQuickReject2() {
    // Return true if the region is empty
    assertTrue(region.quickReject(0, 0, 0, 0));
    region.set(0, 0, 20, 20);
    assertTrue(region.quickReject(40, 40, 60, 60));
    region.set(0, 0, 20, 20);
    assertFalse(region.quickReject(0, 0, 10, 10));
    region.set(0, 0, 20, 20);
    assertFalse(region.quickReject(10, 10, 30, 30));
  }

  @Test
  public void testQuickReject3() {
    Region oriRegion = new Region(0, 0, 20, 20);
    Region region1 = new Region();
    Region region2 = new Region(40, 40, 60, 60);
    Region region3 = new Region(0, 0, 10, 10);
    Region region4 = new Region(10, 10, 30, 30);

    // Return true if the region is empty
    assertTrue(region.quickReject(region1));
    region.set(oriRegion);
    assertTrue(region.quickReject(region2));
    region.set(oriRegion);
    assertFalse(region.quickReject(region3));
    region.set(oriRegion);
    assertFalse(region.quickReject(region4));
  }
}
