package org.robolectric.shadows;

import static android.graphics.text.LineBreaker.BREAK_STRATEGY_BALANCED;
import static android.graphics.text.LineBreaker.BREAK_STRATEGY_HIGH_QUALITY;
import static android.graphics.text.LineBreaker.BREAK_STRATEGY_SIMPLE;
import static android.graphics.text.LineBreaker.HYPHENATION_FREQUENCY_FULL;
import static android.graphics.text.LineBreaker.HYPHENATION_FREQUENCY_NONE;
import static android.graphics.text.LineBreaker.HYPHENATION_FREQUENCY_NORMAL;
import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;
import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_NONE;
import static android.os.Build.VERSION_CODES.Q;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.text.LineBreaker;
import android.graphics.text.LineBreaker.ParagraphConstraints;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(minSdk = Q)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeLineBreakerTest {

  @Before
  public void setup() {
    Paint paint = new Paint();
    Context context = RuntimeEnvironment.getApplication();
    AssetManager am = context.getAssets();
    Typeface tf = new Typeface.Builder(am, "fonts/layout/linebreak.ttf").build();
    paint.setTypeface(tf);
    paint.setTextSize(10.0f); // Make 1em = 10px
  }

  @Test
  public void testLineBreak_construct() {
    assertNotNull(new LineBreaker.Builder().build());
  }

  @Test
  public void testSetBreakStrategy_shouldNotThrowExceptions() {
    assertNotNull(new LineBreaker.Builder().setBreakStrategy(BREAK_STRATEGY_SIMPLE).build());
    assertNotNull(new LineBreaker.Builder().setBreakStrategy(BREAK_STRATEGY_HIGH_QUALITY).build());
    assertNotNull(new LineBreaker.Builder().setBreakStrategy(BREAK_STRATEGY_BALANCED).build());
  }

  @Test
  public void testSetHyphenationFrequency_shouldNotThrowExceptions() {
    assertNotNull(
        new LineBreaker.Builder().setHyphenationFrequency(HYPHENATION_FREQUENCY_NORMAL).build());
    assertNotNull(
        new LineBreaker.Builder().setHyphenationFrequency(HYPHENATION_FREQUENCY_FULL).build());
    assertNotNull(
        new LineBreaker.Builder().setHyphenationFrequency(HYPHENATION_FREQUENCY_NONE).build());
  }

  @Test
  public void testSetJustification_shouldNotThrowExceptions() {
    assertNotNull(new LineBreaker.Builder().setJustificationMode(JUSTIFICATION_MODE_NONE).build());
    assertNotNull(
        new LineBreaker.Builder().setJustificationMode(JUSTIFICATION_MODE_INTER_WORD).build());
  }

  @Test
  public void testSetIntent_shouldNotThrowExceptions() {
    assertNotNull(new LineBreaker.Builder().setIndents(null).build());
    assertNotNull(new LineBreaker.Builder().setIndents(new int[] {}).build());
    assertNotNull(new LineBreaker.Builder().setIndents(new int[] {100}).build());
  }

  @Test
  public void testSetGetWidth() {
    ParagraphConstraints c = new ParagraphConstraints();
    assertEquals(0, c.getWidth(), 0.0f); // 0 by default
    c.setWidth(100);
    assertEquals(100, c.getWidth(), 0.0f);
    c.setWidth(200);
    assertEquals(200, c.getWidth(), 0.0f);
  }

  @Test
  public void testSetGetIndent() {
    ParagraphConstraints c = new ParagraphConstraints();
    assertEquals(0.0f, c.getFirstWidth(), 0.0f); // 0 by default
    assertEquals(0, c.getFirstWidthLineCount()); // 0 by default
    c.setIndent(100.0f, 1);
    assertEquals(100.0f, c.getFirstWidth(), 0.0f);
    assertEquals(1, c.getFirstWidthLineCount());
    c.setIndent(200.0f, 5);
    assertEquals(200.0f, c.getFirstWidth(), 0.0f);
    assertEquals(5, c.getFirstWidthLineCount());
  }

  @Test
  public void testSetGetTabStops() {
    ParagraphConstraints c = new ParagraphConstraints();
    assertNull(c.getTabStops()); // null by default
    assertEquals(0, c.getDefaultTabStop(), 0.0); // 0 by default
    c.setTabStops(new float[] {120}, 240);
    assertEquals(1, c.getTabStops().length);
    assertEquals(120, c.getTabStops()[0], 0.0);
    assertEquals(240, c.getDefaultTabStop(), 0.0);
  }
}
