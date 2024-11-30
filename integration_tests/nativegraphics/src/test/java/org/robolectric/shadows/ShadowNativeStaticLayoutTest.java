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

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import android.text.Editable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.PrecomputedText;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.LineBackgroundSpan;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.text.style.StyleSpan;
import android.text.style.TextAppearanceSpan;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.text.EditorState;

/**
 * Tests are derived from the <a
 * http="https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:cts/tests/tests/text/src/android/text/cts/StaticLayoutTest.java">StaticLayoutTest
 * CTS test</a>.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeStaticLayoutTest {

  private static final float SPACE_MULTI = 1.0f;
  private static final float SPACE_ADD = 0.0f;
  private static final int DEFAULT_OUTER_WIDTH = 150;

  private static final int LAST_LINE = 5;
  private static final int LINE_COUNT = 6;
  private static final int LARGER_THAN_LINE_COUNT = 50;

  private static final String LOREM_IPSUM =
      "Lorem ipsum dolor sit amet, consectetur adipiscing "
          + "elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad "
          + "minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea "
          + "commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse "
          + "cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non "
          + "proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

  /* the first line must have one tab. the others not. totally 6 lines
   */
  private static final CharSequence LAYOUT_TEXT =
      "CharSe\tq\nChar" + "Sequence\nCharSequence\nHello\n, world\nLongLongLong";

  private static final CharSequence LAYOUT_TEXT_SINGLE_LINE = "CharSequence";

  private static final int VERTICAL_BELOW_TEXT = 1000;

  private static final Alignment DEFAULT_ALIGN = Alignment.ALIGN_CENTER;

  private static final int ELLIPSIZE_WIDTH = 8;

  private StaticLayout defaultLayout;
  private TextPaint defaultPaint;

  private static class TestingTextPaint extends TextPaint {
    // need to have a subclass to ensure measurement happens in Java and not C++
  }

  @Before
  public void setup() {
    defaultPaint = new TextPaint();
    defaultLayout = createDefaultStaticLayout();
  }

  private StaticLayout createDefaultStaticLayout() {
    return new StaticLayout(
        LAYOUT_TEXT,
        defaultPaint,
        DEFAULT_OUTER_WIDTH,
        DEFAULT_ALIGN,
        SPACE_MULTI,
        SPACE_ADD,
        true);
  }

  private StaticLayout createEllipsizeStaticLayout() {
    return new StaticLayout(
        LAYOUT_TEXT,
        0,
        LAYOUT_TEXT.length(),
        defaultPaint,
        DEFAULT_OUTER_WIDTH,
        DEFAULT_ALIGN,
        SPACE_MULTI,
        SPACE_ADD,
        true,
        TextUtils.TruncateAt.MIDDLE,
        ELLIPSIZE_WIDTH);
  }

  private StaticLayout createEllipsizeStaticLayout(
      CharSequence text, TextUtils.TruncateAt ellipsize) {
    return new StaticLayout(
        text,
        0,
        text.length(),
        defaultPaint,
        DEFAULT_OUTER_WIDTH,
        DEFAULT_ALIGN,
        SPACE_MULTI,
        SPACE_ADD,
        true /* include pad */,
        ellipsize,
        ELLIPSIZE_WIDTH);
  }

  /** Constructor test */
  @Test
  @SuppressWarnings("CheckReturnValue")
  public void testConstructor() {
    new StaticLayout(
        LAYOUT_TEXT,
        defaultPaint,
        DEFAULT_OUTER_WIDTH,
        DEFAULT_ALIGN,
        SPACE_MULTI,
        SPACE_ADD,
        true);

    new StaticLayout(
        LAYOUT_TEXT,
        0,
        LAYOUT_TEXT.length(),
        defaultPaint,
        DEFAULT_OUTER_WIDTH,
        DEFAULT_ALIGN,
        SPACE_MULTI,
        SPACE_ADD,
        true);

    new StaticLayout(
        LAYOUT_TEXT,
        0,
        LAYOUT_TEXT.length(),
        defaultPaint,
        DEFAULT_OUTER_WIDTH,
        DEFAULT_ALIGN,
        SPACE_MULTI,
        SPACE_ADD,
        false,
        null,
        0);
  }

  @Test
  public void testConstructorNull() {
    assertThrows(
        NullPointerException.class, () -> new StaticLayout(null, null, -1, null, 0, 0, true));
  }

  @Test
  public void testBuilder() {
    {
      // Obtain.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      StaticLayout layout = builder.build();
      // Check values passed to obtain().
      assertEquals(LAYOUT_TEXT, layout.getText());
      assertEquals(defaultPaint, layout.getPaint());
      assertEquals(DEFAULT_OUTER_WIDTH, layout.getWidth());
      // Check default values.
      assertEquals(Alignment.ALIGN_NORMAL, layout.getAlignment());
      assertEquals(0.0f, layout.getSpacingAdd(), 0.0f);
      assertEquals(1.0f, layout.getSpacingMultiplier(), 0.0f);
      assertEquals(DEFAULT_OUTER_WIDTH, layout.getEllipsizedWidth());
    }
    {
      // Obtain with null objects.
      StaticLayout.Builder builder = StaticLayout.Builder.obtain(null, 0, 0, null, 0);
      assertThrows(NullPointerException.class, builder::build);
    }
    {
      // setText.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      builder.setText(LAYOUT_TEXT_SINGLE_LINE);
      StaticLayout layout = builder.build();
      assertEquals(LAYOUT_TEXT_SINGLE_LINE, layout.getText());
    }
    {
      // setAlignment.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      builder.setAlignment(DEFAULT_ALIGN);
      StaticLayout layout = builder.build();
      assertEquals(DEFAULT_ALIGN, layout.getAlignment());
    }
    {
      // setLineSpacing.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      builder.setLineSpacing(1.0f, 2.0f);
      StaticLayout layout = builder.build();
      assertEquals(1.0f, layout.getSpacingAdd(), 0.0f);
      assertEquals(2.0f, layout.getSpacingMultiplier(), 0.0f);
    }
    {
      // setEllipsizedWidth and setEllipsize.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      builder.setEllipsize(TruncateAt.END);
      builder.setEllipsizedWidth(ELLIPSIZE_WIDTH);
      StaticLayout layout = builder.build();
      assertEquals(ELLIPSIZE_WIDTH, layout.getEllipsizedWidth());
      assertEquals(DEFAULT_OUTER_WIDTH, layout.getWidth());
      assertEquals(0, layout.getEllipsisCount(0));
      assertTrue(layout.getEllipsisCount(5) > 0);
    }
    {
      // setMaxLines.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      builder.setMaxLines(1);
      builder.setEllipsize(TruncateAt.END);
      StaticLayout layout = builder.build();
      assertTrue(layout.getEllipsisCount(0) > 0);
      assertEquals(1, layout.getLineCount());
    }
    {
      // Setter methods that cannot be directly tested.
      // setBreakStrategy, setHyphenationFrequency, setIncludePad, and setIndents.
      StaticLayout.Builder builder =
          StaticLayout.Builder.obtain(
              LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
      builder.setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY);
      builder.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);
      builder.setIncludePad(true);
      builder.setIndents(null, null);
      StaticLayout layout = builder.build();
      assertNotNull(layout);
    }
  }

  @Config(minSdk = P) // TODO(hoisie): Fix in Android O/O_MR1
  @Test
  public void testSetLineSpacing_whereLineEndsWithNextLine() {
    final float spacingAdd = 10f;
    final float spacingMult = 3f;

    // two lines of text, with line spacing, first line will have the spacing, but last line
    // won't have the spacing
    final String tmpText = "a\nb";
    StaticLayout.Builder builder =
        StaticLayout.Builder.obtain(
            tmpText, 0, tmpText.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
    builder.setLineSpacing(spacingAdd, spacingMult).setIncludePad(false);
    final StaticLayout comparisonLayout = builder.build();

    assertEquals(2, comparisonLayout.getLineCount());
    final int heightWithLineSpacing =
        comparisonLayout.getLineBottom(0) - comparisonLayout.getLineTop(0);
    final int heightWithoutLineSpacing =
        comparisonLayout.getLineBottom(1) - comparisonLayout.getLineTop(1);
    assertTrue(heightWithLineSpacing > heightWithoutLineSpacing);

    final String text = "a\n";
    // build the layout to be tested
    builder =
        StaticLayout.Builder.obtain("a\n", 0, text.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
    builder.setLineSpacing(spacingAdd, spacingMult).setIncludePad(false);
    final StaticLayout layout = builder.build();

    assertEquals(comparisonLayout.getLineCount(), layout.getLineCount());
    assertEquals(heightWithLineSpacing, layout.getLineBottom(0) - layout.getLineTop(0));
    assertEquals(heightWithoutLineSpacing, layout.getLineBottom(1) - layout.getLineTop(1));
  }

  @Test
  public void testBuilder_setJustificationMode() {
    StaticLayout.Builder builder =
        StaticLayout.Builder.obtain(
            LAYOUT_TEXT, 0, LAYOUT_TEXT.length(), defaultPaint, DEFAULT_OUTER_WIDTH);
    builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
    StaticLayout layout = builder.build();
    // Hard to expect the justification result. Just make sure the final layout is created
    // without causing any exceptions.
    assertNotNull(layout);
  }

  /*
   * Get the line number corresponding to the specified vertical position.
   *  If you ask for a position above 0, you get 0. above 0 means pixel above the fire line
   *  if you ask for a position in the range of the height, return the pixel in line
   *  if you ask for a position below the bottom of the text, you get the last line.
   *  Test 4 values containing -1, 0, normal number and > count
   */
  @Test
  public void testGetLineForVertical() {
    assertEquals(0, defaultLayout.getLineForVertical(-1));
    assertEquals(0, defaultLayout.getLineForVertical(0));
    assertTrue(defaultLayout.getLineForVertical(50) > 0);
    assertEquals(LAST_LINE, defaultLayout.getLineForVertical(VERTICAL_BELOW_TEXT));
  }

  /** Return the number of lines of text in this layout. */
  @Test
  public void testGetLineCount() {
    assertEquals(LINE_COUNT, defaultLayout.getLineCount());
  }

  /*
   * Return the vertical position of the top of the specified line.
   * If the specified line is one beyond the last line, returns the bottom of the last line.
   * A line of text contains top and bottom in height. this method just get the top of a line
   * Test 4 values containing -1, 0, normal number and > count
   */
  @Test
  public void testGetLineTop() {
    assertTrue(defaultLayout.getLineTop(0) >= 0);
    assertTrue(defaultLayout.getLineTop(1) > defaultLayout.getLineTop(0));
  }

  @Test
  public void testGetLineTopBeforeFirst() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getLineTop(-1));
  }

  @Test
  public void testGetLineTopAfterLast() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getLineTop(LARGER_THAN_LINE_COUNT));
  }

  /**
   * Return the descent of the specified line. This method just like getLineTop, descent means the
   * bottom pixel of the line Test 4 values containing -1, 0, normal number and > count
   */
  @Test
  public void testGetLineDescent() {
    assertTrue(defaultLayout.getLineDescent(0) > 0);
    assertTrue(defaultLayout.getLineDescent(1) > 0);
  }

  @Test
  public void testGetLineDescentBeforeFirst() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getLineDescent(-1));
  }

  @Test
  public void testGetLineDescentAfterLast() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getLineDescent(LARGER_THAN_LINE_COUNT));
  }

  /**
   * Returns the primary directionality of the paragraph containing the specified line. By default,
   * each line should be same
   */
  @Test
  public void testGetParagraphDirection() {
    assertEquals(defaultLayout.getParagraphDirection(0), defaultLayout.getParagraphDirection(1));
  }

  @Test
  public void testGetParagraphDirectionBeforeFirst() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getParagraphDirection(-1));
  }

  @Test
  public void testGetParagraphDirectionAfterLast() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getParagraphDirection(LARGER_THAN_LINE_COUNT));
  }

  /**
   * Return the text offset of the beginning of the specified line. If the specified line is one
   * beyond the last line, returns the end of the last line. Test 4 values containing -1, 0, normal
   * number and > count Each line's offset must >= 0
   */
  @Test
  public void testGetLineStart() {
    assertTrue(defaultLayout.getLineStart(0) >= 0);
    assertTrue(defaultLayout.getLineStart(1) >= 0);
  }

  @Test
  public void testGetLineStartBeforeFirst() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getLineStart(-1));
  }

  @Test
  public void testGetLineStartAfterLast() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getLineStart(LARGER_THAN_LINE_COUNT));
  }

  /*
   * Returns whether the specified line contains one or more tabs.
   */
  @Test
  public void testGetContainsTab() {
    assertTrue(defaultLayout.getLineContainsTab(0));
    assertFalse(defaultLayout.getLineContainsTab(1));
  }

  @Test
  public void testGetContainsTabBeforeFirst() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getLineContainsTab(-1));
  }

  @Test
  public void testGetContainsTabAfterLast() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getLineContainsTab(LARGER_THAN_LINE_COUNT));
  }

  /**
   * Returns an array of directionalities for the specified line. The array alternates counts of
   * characters in left-to-right and right-to-left segments of the line. We can not check the return
   * value, for Directions' field is package private So only check it not null
   */
  @Test
  public void testGetLineDirections() {
    assertNotNull(defaultLayout.getLineDirections(0));
    assertNotNull(defaultLayout.getLineDirections(1));
  }

  @Test
  public void testGetLineDirectionsBeforeFirst() {
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getLineDirections(-1));
  }

  @Test
  public void testGetLineDirectionsAfterLast() {
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getLineDirections(LARGER_THAN_LINE_COUNT));
  }

  /**
   * Returns the (negative) number of extra pixels of ascent padding in the top line of the Layout.
   */
  @Test
  public void testGetTopPadding() {
    assertTrue(defaultLayout.getTopPadding() < 0);
  }

  /** Returns the number of extra pixels of descent padding in the bottom line of the Layout. */
  @Test
  public void testGetBottomPadding() {
    assertTrue(defaultLayout.getBottomPadding() > 0);
  }

  /*
   * Returns the number of characters to be ellipsized away, or 0 if no ellipsis is to take place.
   * So each line must >= 0
   */
  @Test
  public void testGetEllipsisCount() {
    // Multilines (6 lines) and TruncateAt.START so no ellipsis at all
    defaultLayout = createEllipsizeStaticLayout(LAYOUT_TEXT, TextUtils.TruncateAt.MIDDLE);

    assertEquals(0, defaultLayout.getEllipsisCount(0));
    assertEquals(0, defaultLayout.getEllipsisCount(1));
    assertEquals(0, defaultLayout.getEllipsisCount(2));
    assertEquals(0, defaultLayout.getEllipsisCount(3));
    assertEquals(0, defaultLayout.getEllipsisCount(4));
    assertEquals(0, defaultLayout.getEllipsisCount(5));

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getEllipsisCount(-1));

    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getEllipsisCount(LARGER_THAN_LINE_COUNT));

    // Multilines (6 lines) and TruncateAt.MIDDLE so no ellipsis at all
    defaultLayout = createEllipsizeStaticLayout(LAYOUT_TEXT, TextUtils.TruncateAt.MIDDLE);

    assertEquals(0, defaultLayout.getEllipsisCount(0));
    assertEquals(0, defaultLayout.getEllipsisCount(1));
    assertEquals(0, defaultLayout.getEllipsisCount(2));
    assertEquals(0, defaultLayout.getEllipsisCount(3));
    assertEquals(0, defaultLayout.getEllipsisCount(4));
    assertEquals(0, defaultLayout.getEllipsisCount(5));

    // Multilines (6 lines) and TruncateAt.END so ellipsis only on the last line
    defaultLayout = createEllipsizeStaticLayout(LAYOUT_TEXT, TextUtils.TruncateAt.END);

    assertEquals(0, defaultLayout.getEllipsisCount(0));
    assertEquals(0, defaultLayout.getEllipsisCount(1));
    assertEquals(0, defaultLayout.getEllipsisCount(2));
    assertEquals(0, defaultLayout.getEllipsisCount(3));
    assertEquals(0, defaultLayout.getEllipsisCount(4));
    assertTrue(defaultLayout.getEllipsisCount(5) > 0);

    // Multilines (6 lines) and TruncateAt.MARQUEE so ellipsis only on the last line
    defaultLayout = createEllipsizeStaticLayout(LAYOUT_TEXT, TextUtils.TruncateAt.END);

    assertEquals(0, defaultLayout.getEllipsisCount(0));
    assertEquals(0, defaultLayout.getEllipsisCount(1));
    assertEquals(0, defaultLayout.getEllipsisCount(2));
    assertEquals(0, defaultLayout.getEllipsisCount(3));
    assertEquals(0, defaultLayout.getEllipsisCount(4));
    assertTrue(defaultLayout.getEllipsisCount(5) > 0);
  }

  /*
   * Return the offset of the first character to be ellipsized away
   * relative to the start of the line.
   * (So 0 if the beginning of the line is ellipsized, not getLineStart().)
   */
  @Test
  public void testGetEllipsisStart() {
    defaultLayout = createEllipsizeStaticLayout();
    assertTrue(defaultLayout.getEllipsisStart(0) >= 0);
    assertTrue(defaultLayout.getEllipsisStart(1) >= 0);

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> defaultLayout.getEllipsisStart(-1));
    assertThrows(
        ArrayIndexOutOfBoundsException.class,
        () -> defaultLayout.getEllipsisStart(LARGER_THAN_LINE_COUNT));
  }

  /*
   * Return the width to which this Layout is ellipsizing
   * or getWidth() if it is not doing anything special.
   * The constructor's Argument TextUtils.TruncateAt defines which EllipsizedWidth to use
   * ellipsizedWidth if argument is not null
   * outerWidth if argument is null
   */
  @Test
  public void testGetEllipsizedWidth() {
    int ellipsizedWidth = 60;
    int outerWidth = 100;
    StaticLayout layout =
        new StaticLayout(
            LAYOUT_TEXT,
            0,
            LAYOUT_TEXT.length(),
            defaultPaint,
            outerWidth,
            DEFAULT_ALIGN,
            SPACE_MULTI,
            SPACE_ADD,
            false,
            TextUtils.TruncateAt.END,
            ellipsizedWidth);
    assertEquals(ellipsizedWidth, layout.getEllipsizedWidth());

    layout =
        new StaticLayout(
            LAYOUT_TEXT,
            0,
            LAYOUT_TEXT.length(),
            defaultPaint,
            outerWidth,
            DEFAULT_ALIGN,
            SPACE_MULTI,
            SPACE_ADD,
            false,
            null,
            ellipsizedWidth);
    assertEquals(outerWidth, layout.getEllipsizedWidth());
  }

  /**
   * scenario description: 1. set the text. 2. change the text 3. Check the text won't change to the
   * StaticLayout
   */
  @Test
  public void testImmutableStaticLayout() {
    Editable editable = Editable.Factory.getInstance().newEditable("123\t\n555");
    StaticLayout layout =
        new StaticLayout(
            editable,
            defaultPaint,
            DEFAULT_OUTER_WIDTH,
            DEFAULT_ALIGN,
            SPACE_MULTI,
            SPACE_ADD,
            true);

    assertEquals(2, layout.getLineCount());
    assertTrue(defaultLayout.getLineContainsTab(0));

    // change the text
    editable.delete(0, editable.length() - 1);

    assertEquals(2, layout.getLineCount());
    assertTrue(layout.getLineContainsTab(0));
  }

  // String wrapper for testing not well known implementation of CharSequence.
  private static class FakeCharSequence implements CharSequence {
    private final String str;

    public FakeCharSequence(String str) {
      this.str = str;
    }

    @Override
    public char charAt(int index) {
      return str.charAt(index);
    }

    @Override
    public int length() {
      return str.length();
    }

    @Override
    @Nonnull
    public CharSequence subSequence(int start, int end) {
      return str.subSequence(start, end);
    }

    @Override
    @Nonnull
    public String toString() {
      return str;
    }
  }

  private List<CharSequence> buildTestCharSequences(String testString, Normalizer.Form[] forms) {
    List<CharSequence> result = new ArrayList<>();

    List<String> normalizedStrings = new ArrayList<>();
    for (Normalizer.Form form : forms) {
      normalizedStrings.add(Normalizer.normalize(testString, form));
    }

    for (String str : normalizedStrings) {
      result.add(str);
      result.add(new SpannedString(str));
      result.add(new SpannableString(str));
      result.add(new SpannableStringBuilder(str)); // as a GraphicsOperations implementation.
      result.add(new FakeCharSequence(str)); // as a not well known implementation.
    }
    return result;
  }

  private String buildTestMessage(CharSequence seq) {
    String normalized;
    if (Normalizer.isNormalized(seq, Normalizer.Form.NFC)) {
      normalized = "NFC";
    } else if (Normalizer.isNormalized(seq, Normalizer.Form.NFD)) {
      normalized = "NFD";
    } else if (Normalizer.isNormalized(seq, Normalizer.Form.NFKC)) {
      normalized = "NFKC";
    } else if (Normalizer.isNormalized(seq, Normalizer.Form.NFKD)) {
      normalized = "NFKD";
    } else {
      throw new IllegalStateException("Normalized form is not NFC/NFD/NFKC/NFKD");
    }

    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < seq.length(); ++i) {
      builder.append(String.format("0x%04X ", (int) seq.charAt(i)));
    }

    return "testString: \""
        + seq
        + "\"["
        + builder
        + "]"
        + ", class: "
        + seq.getClass().getName()
        + ", Normalization: "
        + normalized;
  }

  @Test
  public void testGetOffset_ascii() {
    String[] testStrings = {"abcde", "ab\ncd", "ab\tcd", "ab\n\nc", "ab\n\tc"};

    for (String testString : testStrings) {
      for (CharSequence seq : buildTestCharSequences(testString, Normalizer.Form.values())) {
        StaticLayout layout =
            new StaticLayout(
                seq,
                defaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACE_MULTI,
                SPACE_ADD,
                true);

        String testLabel = buildTestMessage(seq);

        assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
        assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
        assertEquals(testLabel, 1, layout.getOffsetToLeftOf(2));
        assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
        assertEquals(testLabel, 3, layout.getOffsetToLeftOf(4));
        assertEquals(testLabel, 4, layout.getOffsetToLeftOf(5));

        assertEquals(testLabel, 1, layout.getOffsetToRightOf(0));
        assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
        assertEquals(testLabel, 3, layout.getOffsetToRightOf(2));
        assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
        assertEquals(testLabel, 5, layout.getOffsetToRightOf(4));
        assertEquals(testLabel, 5, layout.getOffsetToRightOf(5));
      }
    }

    String testString = "ab\r\nde";
    for (CharSequence seq : buildTestCharSequences(testString, Normalizer.Form.values())) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 1, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 5, layout.getOffsetToLeftOf(6));

      assertEquals(testLabel, 1, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(6));
    }
  }

  @Test
  public void testGetOffset_unicode() {
    String[] testStrings =
        new String[] {
          // Cyrillic alphabets.
          "\u0410\u0411\u0412\u0413\u0414",
          // Japanese Hiragana Characters.
          "\u3042\u3044\u3046\u3048\u304A",
        };

    for (String testString : testStrings) {
      for (CharSequence seq : buildTestCharSequences(testString, Normalizer.Form.values())) {
        StaticLayout layout =
            new StaticLayout(
                seq,
                defaultPaint,
                DEFAULT_OUTER_WIDTH,
                DEFAULT_ALIGN,
                SPACE_MULTI,
                SPACE_ADD,
                true);

        String testLabel = buildTestMessage(seq);

        assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
        assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
        assertEquals(testLabel, 1, layout.getOffsetToLeftOf(2));
        assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
        assertEquals(testLabel, 3, layout.getOffsetToLeftOf(4));
        assertEquals(testLabel, 4, layout.getOffsetToLeftOf(5));

        assertEquals(testLabel, 1, layout.getOffsetToRightOf(0));
        assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
        assertEquals(testLabel, 3, layout.getOffsetToRightOf(2));
        assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
        assertEquals(testLabel, 5, layout.getOffsetToRightOf(4));
        assertEquals(testLabel, 5, layout.getOffsetToRightOf(5));
      }
    }
  }

  @Test
  public void testGetOffset_unicode_normalization() {
    // "A" with acute, circumflex, tilde, diaeresis, ring above.
    String testString = "\u00C1\u00C2\u00C3\u00C4\u00C5";
    Normalizer.Form[] oneUnicodeForms = {Normalizer.Form.NFC, Normalizer.Form.NFKC};
    for (CharSequence seq : buildTestCharSequences(testString, oneUnicodeForms)) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 1, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 3, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(5));

      assertEquals(testLabel, 1, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 3, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(5));
    }

    Normalizer.Form[] twoUnicodeForms = {Normalizer.Form.NFD, Normalizer.Form.NFKD};
    for (CharSequence seq : buildTestCharSequences(testString, twoUnicodeForms)) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(6));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(7));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(8));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(9));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(10));

      assertEquals(testLabel, 2, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 8, layout.getOffsetToRightOf(6));
      assertEquals(testLabel, 8, layout.getOffsetToRightOf(7));
      assertEquals(testLabel, 10, layout.getOffsetToRightOf(8));
      assertEquals(testLabel, 10, layout.getOffsetToRightOf(9));
      assertEquals(testLabel, 10, layout.getOffsetToRightOf(10));
    }
  }

  @Test
  public void testGetOffset_unicode_surrogatePairs() {
    // Emoticons for surrogate pairs tests.
    String testString = "\uD83D\uDE00\uD83D\uDE01\uD83D\uDE02\uD83D\uDE03\uD83D\uDE04";
    for (CharSequence seq : buildTestCharSequences(testString, Normalizer.Form.values())) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(6));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(7));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(8));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(9));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(10));

      assertEquals(testLabel, 2, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 8, layout.getOffsetToRightOf(6));
      assertEquals(testLabel, 8, layout.getOffsetToRightOf(7));
      assertEquals(testLabel, 10, layout.getOffsetToRightOf(8));
      assertEquals(testLabel, 10, layout.getOffsetToRightOf(9));
      assertEquals(testLabel, 10, layout.getOffsetToRightOf(10));
    }
  }

  @Test
  public void testGetOffset_unicode_thai() {
    // Thai Characters. The expected cursorable boundary is
    // | \u0E02 | \u0E2D | \u0E1A | \u0E04\u0E38 | \u0E13 |
    String testString = "\u0E02\u0E2D\u0E1A\u0E04\u0E38\u0E13";
    for (CharSequence seq : buildTestCharSequences(testString, Normalizer.Form.values())) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 1, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 3, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 3, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 5, layout.getOffsetToLeftOf(6));

      assertEquals(testLabel, 1, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 3, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(6));
    }
  }

  @Config(minSdk = P) // TODO(hoisie): Fix in Android O/O_MR1
  @Test
  public void testGetOffset_unicode_arabic() {
    // Arabic Characters. The expected cursorable boundary is
    // | \u0623 \u064F | \u0633 \u0652 | \u0631 \u064E | \u0629 \u064C |";
    String testString = "\u0623\u064F\u0633\u0652\u0631\u064E\u0629\u064C";

    Normalizer.Form[] oneUnicodeForms = {Normalizer.Form.NFC, Normalizer.Form.NFKC};
    for (CharSequence seq : buildTestCharSequences(testString, oneUnicodeForms)) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(6));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(7));
      assertEquals(testLabel, 8, layout.getOffsetToLeftOf(8));

      assertEquals(testLabel, 0, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 0, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(6));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(7));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(8));
    }
  }

  @Config(minSdk = P) // TODO(hoisie): Fix in Android O/O_MR1
  @Test
  public void testGetOffset_unicode_bidi() {
    // String having RTL characters and LTR characters

    // LTR Context
    // The first and last two characters are LTR characters.
    String testString = "\u0061\u0062\u05DE\u05E1\u05E2\u0063\u0064";
    // Logical order: [L1] [L2] [R1] [R2] [R3] [L3] [L4]
    //               0    1    2    3    4    5    6    7
    // Display order: [L1] [L2] [R3] [R2] [R1] [L3] [L4]
    //               0    1    2    4    3    5    6    7
    // [L?] means ?th LTR character and [R?] means ?th RTL character.
    for (CharSequence seq : buildTestCharSequences(testString, Normalizer.Form.values())) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 1, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 3, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 5, layout.getOffsetToLeftOf(6));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(7));

      assertEquals(testLabel, 1, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 3, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 7, layout.getOffsetToRightOf(6));
      assertEquals(testLabel, 7, layout.getOffsetToRightOf(7));
    }

    // RTL Context
    // The first and last two characters are RTL characters.
    String testString2 = "\u05DE\u05E1\u0063\u0064\u0065\u05DE\u05E1";
    // Logical order: [R1] [R2] [L1] [L2] [L3] [R3] [R4]
    //               0    1    2    3    4    5    6    7
    // Display order: [R4] [R3] [L1] [L2] [L3] [R2] [R1]
    //               7    6    5    3    4    2    1    0
    // [L?] means ?th LTR character and [R?] means ?th RTL character.
    for (CharSequence seq : buildTestCharSequences(testString2, Normalizer.Form.values())) {
      StaticLayout layout =
          new StaticLayout(
              seq, defaultPaint, DEFAULT_OUTER_WIDTH, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);

      String testLabel = buildTestMessage(seq);

      assertEquals(testLabel, 1, layout.getOffsetToLeftOf(0));
      assertEquals(testLabel, 2, layout.getOffsetToLeftOf(1));
      assertEquals(testLabel, 4, layout.getOffsetToLeftOf(2));
      assertEquals(testLabel, 5, layout.getOffsetToLeftOf(3));
      assertEquals(testLabel, 3, layout.getOffsetToLeftOf(4));
      assertEquals(testLabel, 6, layout.getOffsetToLeftOf(5));
      assertEquals(testLabel, 7, layout.getOffsetToLeftOf(6));
      assertEquals(testLabel, 7, layout.getOffsetToLeftOf(7));

      assertEquals(testLabel, 0, layout.getOffsetToRightOf(0));
      assertEquals(testLabel, 0, layout.getOffsetToRightOf(1));
      assertEquals(testLabel, 1, layout.getOffsetToRightOf(2));
      assertEquals(testLabel, 4, layout.getOffsetToRightOf(3));
      assertEquals(testLabel, 2, layout.getOffsetToRightOf(4));
      assertEquals(testLabel, 3, layout.getOffsetToRightOf(5));
      assertEquals(testLabel, 5, layout.getOffsetToRightOf(6));
      assertEquals(testLabel, 6, layout.getOffsetToRightOf(7));
    }
  }

  private void moveCursorToRightCursorableOffset(EditorState state) {
    assertEquals("The editor has selection", state.selectionStart, state.selectionEnd);
    StaticLayout layout =
        StaticLayout.Builder.obtain(
                state.text, 0, state.text.length(), defaultPaint, DEFAULT_OUTER_WIDTH)
            .build();
    final int newOffset = layout.getOffsetToRightOf(state.selectionStart);
    state.selectionStart = state.selectionEnd = newOffset;
  }

  private void moveCursorToLeftCursorableOffset(EditorState state) {
    assertEquals("The editor has selection", state.selectionStart, state.selectionEnd);
    StaticLayout layout =
        StaticLayout.Builder.obtain(
                state.text, 0, state.text.length(), defaultPaint, DEFAULT_OUTER_WIDTH)
            .build();
    final int newOffset = layout.getOffsetToLeftOf(state.selectionStart);
    state.selectionStart = state.selectionEnd = newOffset;
  }

  @Test
  public void testGetOffset_emoji() {
    EditorState state = new EditorState();

    // Emojis
    // U+00A9 is COPYRIGHT SIGN.
    state.setByString("| U+00A9 U+00A9 U+00A9");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 | U+00A9 U+00A9");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+00A9 | U+00A9");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+00A9 U+00A9 |");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+00A9 U+00A9 |");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+00A9 U+00A9 | U+00A9");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+00A9 | U+00A9 U+00A9");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+00A9 U+00A9 U+00A9");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+00A9 U+00A9 U+00A9");

    // Surrogate pairs
    // U+1F468 is MAN.
    state.setByString("| U+1F468 U+1F468 U+1F468");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F468 | U+1F468 U+1F468");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F468 U+1F468 | U+1F468");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F468 U+1F468 U+1F468 |");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F468 U+1F468 U+1F468 |");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+1F468 U+1F468 | U+1F468");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+1F468 | U+1F468 U+1F468");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+1F468 U+1F468 U+1F468");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+1F468 U+1F468 U+1F468");

    // Keycaps
    // U+20E3 is COMBINING ENCLOSING KEYCAP.
    state.setByString("| '1' U+20E3 '1' U+20E3 '1' U+20E3");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("'1' U+20E3 | '1' U+20E3 '1' U+20E3");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("'1' U+20E3 '1' U+20E3 | '1' U+20E3");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("'1' U+20E3 '1' U+20E3 '1' U+20E3 |");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("'1' U+20E3 '1' U+20E3 '1' U+20E3 |");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("'1' U+20E3 '1' U+20E3 | '1' U+20E3");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("'1' U+20E3 | '1' U+20E3 '1' U+20E3");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| '1' U+20E3 '1' U+20E3 '1' U+20E3");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| '1' U+20E3 '1' U+20E3 '1' U+20E3");

    // Variation selectors
    // U+00A9 is COPYRIGHT SIGN, U+FE0E is VARIATION SELECTOR-15. U+FE0F is VARIATION
    // SELECTOR-16.
    state.setByString("| U+00A9 U+FE0E U+00A9 U+FE0F U+00A9 U+FE0E");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+FE0E | U+00A9 U+FE0F U+00A9 U+FE0E");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+FE0E U+00A9 U+FE0F | U+00A9 U+FE0E");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+FE0E U+00A9 U+FE0F U+00A9 U+FE0E |");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+00A9 U+FE0E U+00A9 U+FE0F U+00A9 U+FE0E |");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+00A9 U+FE0E U+00A9 U+FE0F | U+00A9 U+FE0E");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+00A9 U+FE0E | U+00A9 U+FE0F U+00A9 U+FE0E");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+00A9 U+FE0E U+00A9 U+FE0F U+00A9 U+FE0E");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+00A9 U+FE0E U+00A9 U+FE0F U+00A9 U+FE0E");

    // TODO(hoisie): Investigate why this fails in P
    if (RuntimeEnvironment.getApiLevel() > P) {
      // Keycap + variation selector
      state.setByString("| '1' U+FE0F U+20E3 '1' U+FE0F U+20E3 '1' U+FE0F U+20E3");
      moveCursorToRightCursorableOffset(state);
      state.assertEquals("'1' U+FE0F U+20E3 | '1' U+FE0F U+20E3 '1' U+FE0F U+20E3");
      moveCursorToRightCursorableOffset(state);
      state.assertEquals("'1' U+FE0F U+20E3 '1' U+FE0F U+20E3 | '1' U+FE0F U+20E3");
      moveCursorToRightCursorableOffset(state);
      state.assertEquals("'1' U+FE0F U+20E3 '1' U+FE0F U+20E3 '1' U+FE0F U+20E3 |");
      moveCursorToRightCursorableOffset(state);
      state.assertEquals("'1' U+FE0F U+20E3 '1' U+FE0F U+20E3 '1' U+FE0F U+20E3 |");
      moveCursorToLeftCursorableOffset(state);
      state.assertEquals("'1' U+FE0F U+20E3 '1' U+FE0F U+20E3 | '1' U+FE0F U+20E3");
      moveCursorToLeftCursorableOffset(state);
      state.assertEquals("'1' U+FE0F U+20E3 | '1' U+FE0F U+20E3 '1' U+FE0F U+20E3");
      moveCursorToLeftCursorableOffset(state);
      state.assertEquals("| '1' U+FE0F U+20E3 '1' U+FE0F U+20E3 '1' U+FE0F U+20E3");
      moveCursorToLeftCursorableOffset(state);
      state.assertEquals("| '1' U+FE0F U+20E3 '1' U+FE0F U+20E3 '1' U+FE0F U+20E3");
    }

    // Flags
    // U+1F1E6 U+1F1E8 is Ascension Island flag.
    state.setByString("| U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F1E6 U+1F1E8 | U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 | U+1F1E6 U+1F1E8");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 |");
    moveCursorToRightCursorableOffset(state);
    state.assertEquals("U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 |");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 | U+1F1E6 U+1F1E8");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("U+1F1E6 U+1F1E8 | U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8");
    moveCursorToLeftCursorableOffset(state);
    state.assertEquals("| U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8 U+1F1E6 U+1F1E8");
  }

  @Test
  public void testGetOffsetForHorizontal_multilines() {
    // Emoticons for surrogate pairs tests.
    String testString = "\uD83D\uDE00\uD83D\uDE01\uD83D\uDE02\uD83D\uDE03\uD83D\uDE04";
    final float width = defaultPaint.measureText(testString, 0, 6);
    StaticLayout layout =
        new StaticLayout(
            testString, defaultPaint, (int) width, DEFAULT_ALIGN, SPACE_MULTI, SPACE_ADD, true);
    // We expect the line break to be after the third emoticon, but we allow flexibility of the
    // line break algorithm as long as the break is within the string. These other cases might
    // happen if for example the font has kerning between emoticons.
    final int lineBreakOffset = layout.getOffsetForHorizontal(1, 0.0f);
    assertEquals(0, layout.getLineForOffset(lineBreakOffset - 1));

    assertEquals(0, layout.getOffsetForHorizontal(0, 0.0f));
    assertEquals(lineBreakOffset - 2, layout.getOffsetForHorizontal(0, width));
    assertEquals(lineBreakOffset - 2, layout.getOffsetForHorizontal(0, width * 2));

    final int lineCount = layout.getLineCount();
    assertEquals(testString.length(), layout.getOffsetForHorizontal(lineCount - 1, width));
    assertEquals(testString.length(), layout.getOffsetForHorizontal(lineCount - 1, width * 2));
  }

  @Config(minSdk = P) // TODO(hoisie): Fix in Android O/O_MR1
  @Test
  public void testIsRtlCharAt() {
    {
      String testString = "ab(\u0623\u0624)c\u0625";
      StaticLayout layout =
          new StaticLayout(
              testString,
              defaultPaint,
              DEFAULT_OUTER_WIDTH,
              DEFAULT_ALIGN,
              SPACE_MULTI,
              SPACE_ADD,
              true);

      assertFalse(layout.isRtlCharAt(0));
      assertFalse(layout.isRtlCharAt(1));
      assertFalse(layout.isRtlCharAt(2));
      assertTrue(layout.isRtlCharAt(3));
      assertTrue(layout.isRtlCharAt(4));
      assertFalse(layout.isRtlCharAt(5));
      assertFalse(layout.isRtlCharAt(6));
      assertTrue(layout.isRtlCharAt(7));
    }
    {
      String testString = "\u0623\u0624(ab)\u0625c";
      StaticLayout layout =
          new StaticLayout(
              testString,
              defaultPaint,
              DEFAULT_OUTER_WIDTH,
              DEFAULT_ALIGN,
              SPACE_MULTI,
              SPACE_ADD,
              true);

      assertTrue(layout.isRtlCharAt(0));
      assertTrue(layout.isRtlCharAt(1));
      assertTrue(layout.isRtlCharAt(2));
      assertFalse(layout.isRtlCharAt(3));
      assertFalse(layout.isRtlCharAt(4));
      assertTrue(layout.isRtlCharAt(5));
      assertTrue(layout.isRtlCharAt(6));
      assertFalse(layout.isRtlCharAt(7));
      assertFalse(layout.isRtlCharAt(8));
    }
  }

  @Config(minSdk = P) // TODO(hoisie): Fix in Android O/O_MR1
  @Test
  public void testGetHorizontal() {
    String testString = "abc\u0623\u0624\u0625def";
    StaticLayout layout =
        new StaticLayout(
            testString,
            defaultPaint,
            DEFAULT_OUTER_WIDTH,
            DEFAULT_ALIGN,
            SPACE_MULTI,
            SPACE_ADD,
            true);

    assertEquals(layout.getPrimaryHorizontal(0), layout.getSecondaryHorizontal(0), 0.0f);
    assertTrue(layout.getPrimaryHorizontal(0) < layout.getPrimaryHorizontal(3));
    assertTrue(layout.getPrimaryHorizontal(3) < layout.getSecondaryHorizontal(3));
    assertTrue(layout.getPrimaryHorizontal(4) < layout.getSecondaryHorizontal(3));
    assertEquals(layout.getPrimaryHorizontal(4), layout.getSecondaryHorizontal(4), 0.0f);
    assertEquals(layout.getPrimaryHorizontal(3), layout.getSecondaryHorizontal(6), 0.0f);
    assertEquals(layout.getPrimaryHorizontal(6), layout.getSecondaryHorizontal(3), 0.0f);
    assertEquals(layout.getPrimaryHorizontal(7), layout.getSecondaryHorizontal(7), 0.0f);
  }

  @Test
  public void testVeryLargeString() {
    final int maxCount = 1 << 20;
    final int wordSize = 32;
    char[] longText = new char[maxCount];
    for (int n = 0; n < maxCount; n++) {
      longText[n] = (n % wordSize) == 0 ? ' ' : 'm';
    }
    String longTextString = new String(longText);
    TextPaint paint = new TestingTextPaint();
    StaticLayout layout =
        new StaticLayout(
            longTextString,
            paint,
            DEFAULT_OUTER_WIDTH,
            DEFAULT_ALIGN,
            SPACE_MULTI,
            SPACE_ADD,
            true);
    assertNotNull(layout);
    // In Android O and Android O_MR1, StaticLayout.addMeasuredRun will get called for this very
    // long line, which will result in the recycle mechanism being triggered, as the number
    // of lines is greater than 16.
    assertThat(layout.getLineCount()).isGreaterThan(16);
  }

  @Test
  public void testNoCrashWhenWordStyleOverlap() {
    // test case where word boundary overlaps multiple style spans
    SpannableStringBuilder text = new SpannableStringBuilder("word boundaries, overlap style");
    // span covers "boundaries"
    text.setSpan(
        new StyleSpan(Typeface.BOLD),
        "word ".length(),
        "word boundaries".length(),
        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    defaultPaint.setTextLocale(Locale.US);
    StaticLayout layout =
        StaticLayout.Builder.obtain(text, 0, text.length(), defaultPaint, DEFAULT_OUTER_WIDTH)
            .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY) // enable hyphenation
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL)
            .build();
    assertNotNull(layout);
  }

  @Test
  public void testRespectingIndentsOnEllipsizedText() {
    // test case where word boundary overlaps multiple style spans
    final String text = "words with indents";

    // +1 to ensure that we won't wrap in the normal case
    int textWidth = (int) (defaultPaint.measureText(text) + 1);
    StaticLayout layout =
        StaticLayout.Builder.obtain(text, 0, text.length(), defaultPaint, textWidth)
            .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY) // enable hyphenation
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NORMAL)
            .setEllipsize(TruncateAt.END)
            .setEllipsizedWidth(textWidth)
            .setMaxLines(1)
            .setIndents(null, new int[] {20})
            .build();
    assertTrue(layout.getEllipsisStart(0) != 0);
  }

  @Test
  public void testGetPrimary_shouldFail_whenOffsetIsOutOfBounds_withSpannable() {
    final String text = "1\n2\n3";
    final SpannableString spannable = new SpannableString(text);
    spannable.setSpan(new Object(), 0, text.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    final Layout layout =
        StaticLayout.Builder.obtain(
                spannable, 0, spannable.length(), defaultPaint, Integer.MAX_VALUE - 1)
            .setMaxLines(2)
            .setEllipsize(TruncateAt.END)
            .build();
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> layout.getPrimaryHorizontal(layout.getText().length()));
  }

  @Test
  public void testGetPrimary_shouldFail_whenOffsetIsOutOfBounds_withString() {
    final String text = "1\n2\n3";
    final Layout layout =
        StaticLayout.Builder.obtain(text, 0, text.length(), defaultPaint, Integer.MAX_VALUE - 1)
            .setMaxLines(2)
            .setEllipsize(TruncateAt.END)
            .build();
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> layout.getPrimaryHorizontal(layout.getText().length()));
  }

  @Test
  public void testNegativeWidth() {
    StaticLayout.Builder.obtain("a", 0, 1, new TextPaint(), 5)
        .setIndents(new int[] {10}, new int[] {10})
        .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
        .build();
    StaticLayout.Builder.obtain("a", 0, 1, new TextPaint(), 5)
        .setIndents(new int[] {10}, new int[] {10})
        .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
        .build();
    StaticLayout.Builder.obtain("a", 0, 1, new TextPaint(), 5)
        .setIndents(new int[] {10}, new int[] {10})
        .setBreakStrategy(Layout.BREAK_STRATEGY_BALANCED)
        .build();
  }

  @Test
  public void testGetLineMax() {
    final float wholeWidth = defaultPaint.measureText(LOREM_IPSUM);
    final int lineWidth = (int) (wholeWidth / 10.0f); // Make 10 lines per paragraph.
    final String multiParaTestString =
        LOREM_IPSUM + "\n" + LOREM_IPSUM + "\n" + LOREM_IPSUM + "\n" + LOREM_IPSUM;
    final Layout layout =
        StaticLayout.Builder.obtain(
                multiParaTestString, 0, multiParaTestString.length(), defaultPaint, lineWidth)
            .build();
    for (int i = 0; i < layout.getLineCount(); i++) {
      assertTrue(layout.getLineMax(i) <= lineWidth);
    }
  }

  @Test
  public void testIndent() {
    final float wholeWidth = defaultPaint.measureText(LOREM_IPSUM);
    final int lineWidth = (int) (wholeWidth / 10.0f); // Make 10 lines per paragraph.
    final int indentWidth = (int) (lineWidth * 0.3f); // Make 30% indent.
    final String multiParaTestString =
        LOREM_IPSUM + "\n" + LOREM_IPSUM + "\n" + LOREM_IPSUM + "\n" + LOREM_IPSUM;
    final Layout layout =
        StaticLayout.Builder.obtain(
                multiParaTestString, 0, multiParaTestString.length(), defaultPaint, lineWidth)
            .setIndents(new int[] {indentWidth}, null)
            .build();
    for (int i = 0; i < layout.getLineCount(); i++) {
      assertTrue(layout.getLineMax(i) <= lineWidth - indentWidth);
    }
  }

  @CanIgnoreReturnValue
  private static Bitmap drawToBitmap(Layout l) {
    final Bitmap bmp = Bitmap.createBitmap(l.getWidth(), l.getHeight(), Bitmap.Config.RGB_565);
    final Canvas c = new Canvas(bmp);

    c.save();
    c.translate(0, 0);
    l.draw(c);
    c.restore();
    return bmp;
  }

  private static String textPaintToString(TextPaint p) {
    return "{"
        + "mTextSize="
        + p.getTextSize()
        + ", "
        + "mTextSkewX="
        + p.getTextSkewX()
        + ", "
        + "mTextScaleX="
        + p.getTextScaleX()
        + ", "
        + "mLetterSpacing="
        + p.getLetterSpacing()
        + ", "
        + "mFlags="
        + p.getFlags()
        + ", "
        + "mTextLocales="
        + p.getTextLocales()
        + ", "
        + "mFontVariationSettings="
        + p.getFontVariationSettings()
        + ", "
        + "mTypeface="
        + p.getTypeface()
        + ", "
        + "mFontFeatureSettings="
        + p.getFontFeatureSettings()
        + "}";
  }

  private static String directionToString(TextDirectionHeuristic dir) {
    if (dir == TextDirectionHeuristics.LTR) {
      return "LTR";
    } else if (dir == TextDirectionHeuristics.RTL) {
      return "RTL";
    } else if (dir == TextDirectionHeuristics.FIRSTSTRONG_LTR) {
      return "FIRSTSTRONG_LTR";
    } else if (dir == TextDirectionHeuristics.FIRSTSTRONG_RTL) {
      return "FIRSTSTRONG_RTL";
    } else if (dir == TextDirectionHeuristics.ANYRTL_LTR) {
      return "ANYRTL_LTR";
    } else {
      throw new RuntimeException("Unknown Direction");
    }
  }

  static class LayoutParam {
    final int strategy;
    final int frequency;
    final TextPaint paint;
    final TextDirectionHeuristic dir;

    LayoutParam(int strategy, int frequency, TextPaint paint, TextDirectionHeuristic dir) {
      this.strategy = strategy;
      this.frequency = frequency;
      this.paint = new TextPaint(paint);
      this.dir = dir;
    }

    @Override
    @Nonnull
    public String toString() {
      return "{"
          + "mStrategy="
          + strategy
          + ", "
          + "mFrequency="
          + frequency
          + ", "
          + "mPaint="
          + textPaintToString(paint)
          + ", "
          + "mDir="
          + directionToString(dir)
          + "}";
    }

    Layout getLayout(CharSequence text, int width) {
      return StaticLayout.Builder.obtain(text, 0, text.length(), paint, width)
          .setBreakStrategy(strategy)
          .setHyphenationFrequency(frequency)
          .setTextDirection(dir)
          .build();
    }

    PrecomputedText getPrecomputedText(CharSequence text) {
      PrecomputedText.Params param =
          new PrecomputedText.Params.Builder(paint)
              .setBreakStrategy(strategy)
              .setHyphenationFrequency(frequency)
              .setTextDirection(dir)
              .build();
      return PrecomputedText.create(text, param);
    }
  }

  void assertSameStaticLayout(
      CharSequence text, LayoutParam measuredTextParam, LayoutParam staticLayoutParam) {
    String msg =
        "StaticLayout for "
            + staticLayoutParam
            + " with PrecomputedText"
            + " created with "
            + measuredTextParam
            + " must output the same BMP.";

    final float wholeWidth = defaultPaint.measureText(text.toString());
    final int lineWidth = (int) (wholeWidth / 10.0f); // Make 10 lines per paragraph.

    // Static layout parameter should be used for the final output.
    final Layout expectedLayout = staticLayoutParam.getLayout(text, lineWidth);

    final PrecomputedText mt = measuredTextParam.getPrecomputedText(text);
    final Layout resultLayout =
        StaticLayout.Builder.obtain(mt, 0, mt.length(), staticLayoutParam.paint, lineWidth)
            .setBreakStrategy(staticLayoutParam.strategy)
            .setHyphenationFrequency(staticLayoutParam.frequency)
            .setTextDirection(staticLayoutParam.dir)
            .build();

    assertEquals(msg, expectedLayout.getHeight(), resultLayout.getHeight(), 0.0f);

    final Bitmap expectedBMP = drawToBitmap(expectedLayout);
    final Bitmap resultBMP = drawToBitmap(resultLayout);

    assertTrue(msg, resultBMP.sameAs(expectedBMP));
  }

  @Config(minSdk = P) // PrecomputedText was added in P
  @Test
  public void testPrecomputedText() {
    int[] breaks = {
      Layout.BREAK_STRATEGY_SIMPLE,
      Layout.BREAK_STRATEGY_HIGH_QUALITY,
      Layout.BREAK_STRATEGY_BALANCED,
    };

    int[] frequencies = {
      Layout.HYPHENATION_FREQUENCY_NORMAL,
      Layout.HYPHENATION_FREQUENCY_FULL,
      Layout.HYPHENATION_FREQUENCY_NONE,
    };

    TextDirectionHeuristic[] dirs = {
      TextDirectionHeuristics.LTR,
      TextDirectionHeuristics.RTL,
      TextDirectionHeuristics.FIRSTSTRONG_LTR,
      TextDirectionHeuristics.FIRSTSTRONG_RTL,
      TextDirectionHeuristics.ANYRTL_LTR,
    };

    float[] textSizes = {8.0f, 16.0f, 32.0f};

    LocaleList[] locales = {
      LocaleList.forLanguageTags("en-US"),
      LocaleList.forLanguageTags("ja-JP"),
      LocaleList.forLanguageTags("en-US,ja-JP"),
    };

    TextPaint paint = new TextPaint();

    // If the PrecomputedText is created with the same argument of the StaticLayout, generate
    // the same bitmap.
    for (int b : breaks) {
      for (int f : frequencies) {
        for (TextDirectionHeuristic dir : dirs) {
          for (float textSize : textSizes) {
            for (LocaleList locale : locales) {
              paint.setTextSize(textSize);
              paint.setTextLocales(locale);

              assertSameStaticLayout(
                  LOREM_IPSUM,
                  new LayoutParam(b, f, paint, dir),
                  new LayoutParam(b, f, paint, dir));
            }
          }
        }
      }
    }

    // If the parameters are different, the output of the static layout must be
    // same bitmap.
    for (int bi = 0; bi < breaks.length; bi++) {
      for (int fi = 0; fi < frequencies.length; fi++) {
        for (int diri = 0; diri < dirs.length; diri++) {
          for (int sizei = 0; sizei < textSizes.length; sizei++) {
            for (int localei = 0; localei < locales.length; localei++) {
              TextPaint p1 = new TextPaint();
              TextPaint p2 = new TextPaint();

              p1.setTextSize(textSizes[sizei]);
              p2.setTextSize(textSizes[(sizei + 1) % textSizes.length]);

              p1.setTextLocales(locales[localei]);
              p2.setTextLocales(locales[(localei + 1) % locales.length]);

              int b1 = breaks[bi];
              int b2 = breaks[(bi + 1) % breaks.length];

              int f1 = frequencies[fi];
              int f2 = frequencies[(fi + 1) % frequencies.length];

              TextDirectionHeuristic dir1 = dirs[diri];
              TextDirectionHeuristic dir2 = dirs[(diri + 1) % dirs.length];

              assertSameStaticLayout(
                  LOREM_IPSUM,
                  new LayoutParam(b1, f1, p1, dir1),
                  new LayoutParam(b2, f2, p2, dir2));
            }
          }
        }
      }
    }
  }

  @Config(minSdk = P) // TODO(hoisie): Fix in Android O/O_MR1
  @Test
  public void testReplacementFontMetricsTest() {
    Context context = RuntimeEnvironment.getApplication();

    Typeface tf = new Typeface.Builder(context.getAssets(), "fonts/samplefont.ttf").build();
    assertNotNull(tf);
    TextPaint paint = new TextPaint();
    paint.setTypeface(tf);

    ReplacementSpan firstReplacement = mock(ReplacementSpan.class);
    ArgumentCaptor<FontMetricsInt> fm1Captor = ArgumentCaptor.forClass(FontMetricsInt.class);
    when(firstReplacement.getSize(
            any(Paint.class), any(CharSequence.class), anyInt(), anyInt(), fm1Captor.capture()))
        .thenReturn(0);
    TextAppearanceSpan firstStyleSpan =
        new TextAppearanceSpan(
            null /* family */,
            Typeface.NORMAL /* style */,
            100 /* text size, 1em = 100px */,
            null /* text color */,
            null /* link color */);

    ReplacementSpan secondReplacement = mock(ReplacementSpan.class);
    ArgumentCaptor<FontMetricsInt> fm2Captor = ArgumentCaptor.forClass(FontMetricsInt.class);
    when(secondReplacement.getSize(
            any(Paint.class),
            any(CharSequence.class),
            any(Integer.class),
            any(Integer.class),
            fm2Captor.capture()))
        .thenReturn(0);
    TextAppearanceSpan secondStyleSpan =
        new TextAppearanceSpan(
            null /* family */,
            Typeface.NORMAL /* style */,
            200 /* text size, 1em = 200px */,
            null /* text color */,
            null /* link color */);

    SpannableStringBuilder ssb = new SpannableStringBuilder("Hello, World\nHello, Android");
    ssb.setSpan(firstStyleSpan, 0, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    ssb.setSpan(firstReplacement, 0, 13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    ssb.setSpan(secondStyleSpan, 13, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    ssb.setSpan(secondReplacement, 13, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    StaticLayout.Builder.obtain(ssb, 0, ssb.length(), paint, Integer.MAX_VALUE).build();

    FontMetricsInt firstMetrics = fm1Captor.getValue();
    FontMetricsInt secondMetrics = fm2Captor.getValue();

    // The samplefont.ttf has 0.8em ascent and 0.2em descent.
    assertEquals(-100, firstMetrics.ascent);
    assertEquals(20, firstMetrics.descent);

    assertEquals(-200, secondMetrics.ascent);
    assertEquals(40, secondMetrics.descent);
  }

  @Config(minSdk = Q)
  @Test
  public void testChangeFontMetricsLineHeightBySpanTest() {
    final TextPaint paint = new TextPaint();
    paint.setTextSize(50);
    final SpannableString spanStr0 = new SpannableString(LOREM_IPSUM);
    // Make sure the final layout contain multiple lines.
    final int width = (int) paint.measureText(spanStr0.toString()) / 5;
    final int expectedHeight0 = 25;

    spanStr0.setSpan(
        new LineHeightSpan.Standard(expectedHeight0),
        0,
        spanStr0.length(),
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    StaticLayout layout0 =
        StaticLayout.Builder.obtain(spanStr0, 0, spanStr0.length(), paint, width).build();

    // We need at least 3 lines for testing.
    assertTrue(layout0.getLineCount() > 2);
    // Omit the first and last line, because their line hight might be different due to
    // padding.
    for (int i = 1; i < layout0.getLineCount() - 1; ++i) {
      assertEquals(expectedHeight0, layout0.getLineBottom(i) - layout0.getLineTop(i));
    }

    final SpannableString spanStr1 = new SpannableString(LOREM_IPSUM);
    int expectedHeight1 = 100;

    spanStr1.setSpan(
        new LineHeightSpan.Standard(expectedHeight1),
        0,
        spanStr1.length(),
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    StaticLayout layout1 =
        StaticLayout.Builder.obtain(spanStr1, 0, spanStr1.length(), paint, width).build();

    for (int i = 1; i < layout1.getLineCount() - 1; ++i) {
      assertEquals(expectedHeight1, layout1.getLineBottom(i) - layout1.getLineTop(i));
    }
  }

  @Config(minSdk = Q)
  @Test
  public void testChangeFontMetricsLineHeightBySpanMultipleTimesTest() {
    final TextPaint paint = new TextPaint();
    paint.setTextSize(50);
    final SpannableString spanStr = new SpannableString(LOREM_IPSUM);
    final int width = (int) paint.measureText(spanStr.toString()) / 5;
    final int expectedHeight = 100;

    spanStr.setSpan(
        new LineHeightSpan.Standard(25),
        0,
        spanStr.length(),
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    // Only the last span is effective.
    spanStr.setSpan(
        new LineHeightSpan.Standard(expectedHeight),
        0,
        spanStr.length(),
        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
    StaticLayout layout =
        StaticLayout.Builder.obtain(spanStr, 0, spanStr.length(), paint, width).build();

    assertTrue(layout.getLineCount() > 2);
    for (int i = 1; i < layout.getLineCount() - 1; ++i) {
      assertEquals(expectedHeight, layout.getLineBottom(i) - layout.getLineTop(i));
    }
  }

  private static class FakeLineBackgroundSpan implements LineBackgroundSpan {
    // Whenever drawBackground() is called, the start and end of
    // the line will be stored into history as an array in the
    // format of [start, end].
    private final List<int[]> history;

    FakeLineBackgroundSpan() {
      history = new ArrayList<>();
    }

    @Override
    public void drawBackground(
        @Nonnull Canvas c,
        @Nonnull Paint p,
        int left,
        int right,
        int top,
        int baseline,
        int bottom,
        @Nonnull CharSequence text,
        int start,
        int end,
        int lnum) {
      history.add(new int[] {start, end});
    }

    List<int[]> getHistory() {
      return history;
    }
  }

  private void testLineBackgroundSpanInRange(String text, int start, int end) {
    final SpannableString spanStr = new SpannableString(text);
    final FakeLineBackgroundSpan span = new FakeLineBackgroundSpan();
    spanStr.setSpan(span, start, end, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

    final TextPaint paint = new TextPaint();
    paint.setTextSize(50);
    final int width = (int) paint.measureText(spanStr.toString()) / 5;
    final StaticLayout layout =
        StaticLayout.Builder.obtain(spanStr, 0, spanStr.length(), paint, width).build();

    // One line is too simple, need more to test.
    assertTrue(layout.getLineCount() > 1);
    drawToBitmap(layout);
    List<int[]> history = span.getHistory();

    if (history.isEmpty()) {
      // drawBackground() of FakeLineBackgroundSpan was never called.
      // This only happens when the length of the span is zero.
      assertTrue(start >= end);
      return;
    }

    // Check if drawBackground() is corrected called for each affected line.
    int lastLineEnd = history.get(0)[0];
    for (int[] lineRange : history) {
      // The range of line must intersect with the span.
      assertTrue(lineRange[0] < end && lineRange[1] > start);
      // Check:
      // 1. drawBackground() is called in the correct sequence.
      // 2. drawBackground() is called only once for each affected line.
      assertEquals(lastLineEnd, lineRange[0]);
      lastLineEnd = lineRange[1];
    }

    int[] firstLineRange = history.get(0);
    int[] lastLineRange = history.get(history.size() - 1);

    // Check if affected lines match the span coverage.
    assertTrue(firstLineRange[0] <= start && end <= lastLineRange[1]);
  }

  @Test
  public void testDrawWithLineBackgroundSpanCoverWholeText() {
    testLineBackgroundSpanInRange(LOREM_IPSUM, 0, LOREM_IPSUM.length());
  }

  @Test
  public void testDrawWithLineBackgroundSpanCoverNothing() {
    int i = 0;
    // Zero length Spans.
    testLineBackgroundSpanInRange(LOREM_IPSUM, i, i);
    i = LOREM_IPSUM.length() / 2;
    testLineBackgroundSpanInRange(LOREM_IPSUM, i, i);
  }

  @Test
  @Config(minSdk = Q)
  public void testDrawWithLineBackgroundSpanCoverPart() {
    int start = 0;
    int end = LOREM_IPSUM.length() / 2;
    testLineBackgroundSpanInRange(LOREM_IPSUM, start, end);

    start = LOREM_IPSUM.length() / 2;
    end = LOREM_IPSUM.length();
    testLineBackgroundSpanInRange(LOREM_IPSUM, start, end);
  }

  @Config(minSdk = VERSION_CODES.R)
  @Test
  public void testBidiVisibleEnd() {
    TextPaint paint = new TextPaint();
    // The default text size is too small and not useful for handling line breaks.
    // Make it bigger.
    paint.setTextSize(32);

    final String input = "\u05D0aaaaaa\u3000 aaaaaa";
    // To make line break happen, pass slightly shorter width from the full text width.
    final int lineBreakWidth = (int) (paint.measureText(input) * 0.8);
    final StaticLayout layout =
        StaticLayout.Builder.obtain(input, 0, input.length(), paint, lineBreakWidth).build();

    // Make sure getLineMax won't cause crashes.
    // getLineMax eventually calls TextLine.measure which was the problematic method.
    layout.getLineMax(0);

    final Bitmap bmp =
        Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.RGB_565);
    final Canvas c = new Canvas(bmp);
    // Make sure draw won't cause crashes.
    // draw eventually calls TextLine.draw which was the problematic method.
    layout.draw(c);
  }
}
