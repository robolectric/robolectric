/*
 * Copyright (C) 2022 The Android Open Source Project
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

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.MeasuredParagraph;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = P)
public class ShadowNativeMeasuredParagraphTest {
  private static final TextDirectionHeuristic LTR = TextDirectionHeuristics.LTR;
  private static final TextDirectionHeuristic RTL = TextDirectionHeuristics.RTL;

  private final TextPaint paint = new TextPaint();

  @Before
  public void setUp() { // The test font has following coverage and width.
    // U+0020: 10em
    // U+002E (.): 10em
    // U+0043 (C): 100em
    // U+0049 (I): 1em
    // U+004C (L): 50em
    // U+0056 (V): 5em
    // U+0058 (X): 10em
    // U+005F (_): 0em
    // U+FFFD (invalid surrogate will be replaced to this): 7em
    // U+10331 (\uD800\uDF31): 10em
    Context context = RuntimeEnvironment.getApplication();
    paint.setTypeface(
        Typeface.createFromAsset(
            context.getAssets(), "fonts/StaticLayoutLineBreakingTestFont.ttf"));
    paint.setTextSize(1.0f); // Make 1em == 1px.
  }

  private String charsToString(char[] chars) {
    return String.valueOf(chars);
  }

  @Test
  public void buildForBidi() {

    MeasuredParagraph mt = MeasuredParagraph.buildForBidi("XXX", 0, 3, LTR, null);
    assertNotNull(mt);
    assertNotNull(mt.getChars());
    assertEquals("XXX", charsToString(mt.getChars()));
    assertEquals(Layout.DIR_LEFT_TO_RIGHT, mt.getParagraphDir());
    assertNotNull(mt.getDirections(0, 3));
    assertEquals(0, mt.getWholeWidth(), 0);
    assertEquals(0, mt.getWidths().size());
    assertEquals(0, mt.getSpanEndCache().size());
    assertEquals(0, mt.getFontMetrics().size());
    assertEquals(0, reflector(MeasuredParagraphReflector.class, mt).getNativePtr());

    // Recycle it
    MeasuredParagraph mt2 = MeasuredParagraph.buildForBidi("_VVV_", 1, 4, RTL, mt);
    assertEquals(mt2, mt);
    assertNotNull(mt2.getChars());
    assertEquals("VVV", charsToString(mt.getChars()));
    assertNotNull(mt2.getDirections(0, 3));
    assertEquals(0, mt2.getWholeWidth(), 0);
    assertEquals(0, mt2.getWidths().size());
    assertEquals(0, mt2.getSpanEndCache().size());
    assertEquals(0, mt2.getFontMetrics().size());
    assertEquals(0, reflector(MeasuredParagraphReflector.class, mt2).getNativePtr());

    mt2.recycle();
  }

  @Test
  public void buildForMeasurement() {

    MeasuredParagraph mt = MeasuredParagraph.buildForMeasurement(paint, "XXX", 0, 3, LTR, null);
    assertNotNull(mt);
    assertNotNull(mt.getChars());
    assertEquals("XXX", charsToString(mt.getChars()));
    assertEquals(Layout.DIR_LEFT_TO_RIGHT, mt.getParagraphDir());
    assertNotNull(mt.getDirections(0, 3));
    assertEquals(30, mt.getWholeWidth(), 0);
    assertEquals(3, mt.getWidths().size());
    assertEquals(10, mt.getWidths().get(0), 0);
    assertEquals(10, mt.getWidths().get(1), 0);
    assertEquals(10, mt.getWidths().get(2), 0);
    assertEquals(0, mt.getSpanEndCache().size());
    assertEquals(0, mt.getFontMetrics().size());
    assertEquals(0, reflector(MeasuredParagraphReflector.class, mt).getNativePtr());

    // Recycle it
    MeasuredParagraph mt2 = MeasuredParagraph.buildForMeasurement(paint, "_VVV_", 1, 4, RTL, mt);
    assertEquals(mt2, mt);
    assertNotNull(mt2.getChars());
    assertEquals("VVV", charsToString(mt.getChars()));
    assertEquals(Layout.DIR_RIGHT_TO_LEFT, mt2.getParagraphDir());
    assertNotNull(mt2.getDirections(0, 3));
    assertEquals(15, mt2.getWholeWidth(), 0);
    assertEquals(3, mt2.getWidths().size());
    assertEquals(5, mt2.getWidths().get(0), 0);
    assertEquals(5, mt2.getWidths().get(1), 0);
    assertEquals(5, mt2.getWidths().get(2), 0);
    assertEquals(0, mt2.getSpanEndCache().size());
    assertEquals(0, mt2.getFontMetrics().size());
    assertEquals(0, reflector(MeasuredParagraphReflector.class, mt2).getNativePtr());

    mt2.recycle();
  }

  @Test
  public void buildForStaticLayout() {

    MeasuredParagraph mt =
        (MeasuredParagraph)
            reflector(MeasuredParagraphReflector.class)
                .buildForStaticLayout(paint, "XXX", 0, 3, LTR, false, false, null);
    assertNotNull(mt);
    assertNotNull(mt.getChars());
    assertEquals("XXX", charsToString(mt.getChars()));
    assertEquals(Layout.DIR_LEFT_TO_RIGHT, mt.getParagraphDir());
    assertNotNull(mt.getDirections(0, 3));
    assertEquals(0, mt.getWholeWidth(), 0);
    assertEquals(0, mt.getWidths().size());
    assertEquals(1, mt.getSpanEndCache().size());
    assertEquals(3, mt.getSpanEndCache().get(0));
    assertNotEquals(0, mt.getFontMetrics().size());
    assertNotEquals(0, reflector(MeasuredParagraphReflector.class, mt).getNativePtr());

    // Recycle it
    MeasuredParagraph mt2 =
        (MeasuredParagraph)
            reflector(MeasuredParagraphReflector.class)
                .buildForStaticLayout(paint, "_VVV_", 1, 4, RTL, false, false, mt);
    assertEquals(mt2, mt);
    assertNotNull(mt2.getChars());
    assertEquals("VVV", charsToString(mt.getChars()));
    assertEquals(Layout.DIR_RIGHT_TO_LEFT, mt2.getParagraphDir());
    assertNotNull(mt2.getDirections(0, 3));
    assertEquals(0, mt2.getWholeWidth(), 0);
    assertEquals(0, mt2.getWidths().size());
    assertEquals(1, mt2.getSpanEndCache().size());
    assertEquals(4, mt2.getSpanEndCache().get(0));
    assertNotEquals(0, mt2.getFontMetrics().size());
    assertNotEquals(0, reflector(MeasuredParagraphReflector.class, mt2).getNativePtr());

    mt2.recycle();
  }

  @Test
  public void testFor70146381() {
    MeasuredParagraph.buildForMeasurement(paint, "X…", 0, 2, RTL, null);
  }

  @ForType(MeasuredParagraph.class)
  interface MeasuredParagraphReflector {
    long getNativePtr();

    @Static
    MeasuredParagraph buildForStaticLayout(
        TextPaint paint,
        CharSequence text,
        int start,
        int end,
        TextDirectionHeuristic textDir,
        boolean computeHyphenation,
        boolean computeLayout,
        MeasuredParagraph recycle);
  }
}
