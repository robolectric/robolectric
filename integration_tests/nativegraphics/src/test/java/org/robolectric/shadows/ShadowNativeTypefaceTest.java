package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(minSdk = O)
@RunWith(RobolectricTestRunner.class)
public class ShadowNativeTypefaceTest {
  // generic family name for monospaced fonts
  private static final String MONO = "monospace";
  private static final String DEFAULT = null;
  private static final String INVALID = "invalid-family-name";

  private static float measureText(String text, Typeface typeface) {
    final Paint paint = new Paint();
    // Fix the locale so that fix the locale based fallback.
    paint.setTextLocale(Locale.US);
    paint.setTypeface(typeface);
    return paint.measureText(text);
  }

  // list of family names to try when attempting to find a typeface with a given style
  private static final String[] FAMILIES = {
    null, "monospace", "serif", "sans-serif", "cursive", "arial", "times"
  };

  private final Context context = RuntimeEnvironment.getApplication();

  /**
   * Create a typeface of the given style. If the default font does not support the style, a number
   * of generic families are tried.
   *
   * @return The typeface or null, if no typeface with the given style can be found.
   */
  private static Typeface createTypeface(int style) {
    for (String family : FAMILIES) {
      Typeface tf = Typeface.create(family, style);
      if (tf.getStyle() == style) {
        return tf;
      }
    }
    return null;
  }

  @Test
  public void typeface_notDefault() {
    Typeface typeface1 =
        Typeface.createFromAsset(context.getAssets(), "fonts/others/samplefont.ttf");
    assertThat(typeface1).isNotEqualTo(Typeface.DEFAULT);
  }

  @Test
  public void testIsBold() {
    Typeface typeface = createTypeface(Typeface.BOLD);
    if (typeface != null) {
      assertEquals(Typeface.BOLD, typeface.getStyle());
      assertTrue(typeface.isBold());
      assertFalse(typeface.isItalic());
    }

    typeface = createTypeface(Typeface.ITALIC);
    if (typeface != null) {
      assertEquals(Typeface.ITALIC, typeface.getStyle());
      assertFalse(typeface.isBold());
      assertTrue(typeface.isItalic());
    }

    typeface = createTypeface(Typeface.BOLD_ITALIC);
    if (typeface != null) {
      assertEquals(Typeface.BOLD_ITALIC, typeface.getStyle());
      assertTrue(typeface.isBold());
      assertTrue(typeface.isItalic());
    }

    typeface = createTypeface(Typeface.NORMAL);
    if (typeface != null) {
      assertEquals(Typeface.NORMAL, typeface.getStyle());
      assertFalse(typeface.isBold());
      assertFalse(typeface.isItalic());
    }
  }

  @Test
  public void testCreate() {
    Typeface typeface = Typeface.create(DEFAULT, Typeface.NORMAL);
    assertNotNull(typeface);
    typeface = Typeface.create(MONO, Typeface.BOLD);
    assertNotNull(typeface);
    typeface = Typeface.create(INVALID, Typeface.ITALIC);
    assertNotNull(typeface);

    typeface = Typeface.create(typeface, Typeface.NORMAL);
    assertNotNull(typeface);
    typeface = Typeface.create(typeface, Typeface.BOLD);
    assertNotNull(typeface);
  }

  @Test
  public void testDefaultFromStyle() {
    Typeface typeface = Typeface.defaultFromStyle(Typeface.NORMAL);
    assertNotNull(typeface);
    typeface = Typeface.defaultFromStyle(Typeface.BOLD);
    assertNotNull(typeface);
    typeface = Typeface.defaultFromStyle(Typeface.ITALIC);
    assertNotNull(typeface);
    typeface = Typeface.defaultFromStyle(Typeface.BOLD_ITALIC);
    assertNotNull(typeface);
  }

  @Test
  public void testConstants() {
    assertNotNull(Typeface.DEFAULT);
    assertNotNull(Typeface.DEFAULT_BOLD);
    assertNotNull(Typeface.MONOSPACE);
    assertNotNull(Typeface.SANS_SERIF);
    assertNotNull(Typeface.SERIF);
  }

  @Test
  public void testCreateFromAssetNull() {
    // input abnormal params.
    assertThrows(NullPointerException.class, () -> Typeface.createFromAsset(null, null));
  }

  @Test
  public void testCreateFromAssetNullPath() {
    // input abnormal params.
    assertThrows(
        NullPointerException.class, () -> Typeface.createFromAsset(context.getAssets(), null));
  }

  @Test
  public void testCreateFromAssetInvalidPath() {
    // input abnormal params.
    assertThrows(
        RuntimeException.class,
        () -> Typeface.createFromAsset(context.getAssets(), "invalid path"));
  }

  @Test
  public void testCreateFromAsset() {
    Typeface typeface =
        Typeface.createFromAsset(context.getAssets(), "fonts/others/samplefont.ttf");
    assertNotNull(typeface);
  }

  @Test
  public void testCreateFromFileByFileReferenceNull() {
    // input abnormal params.
    assertThrows(NullPointerException.class, () -> Typeface.createFromFile((File) null));
  }

  @Test
  public void testCreateFromFileByFileReference() throws IOException {
    File file = new File(obtainPath());
    Typeface typeface = Typeface.createFromFile(file);
    assertNotNull(typeface);
  }

  @Test
  public void testCreateFromFileWithInvalidPath() throws IOException {
    File file = new File("/invalid/path");
    assertThrows(RuntimeException.class, () -> Typeface.createFromFile(file));
  }

  @Test
  public void testCreateFromFileByFileNameNull() throws IOException {
    // input abnormal params.
    assertThrows(NullPointerException.class, () -> Typeface.createFromFile((String) null));
  }

  @Test
  public void testCreateFromFileByInvalidFileName() throws IOException {
    // input abnormal params.
    assertThrows(RuntimeException.class, () -> Typeface.createFromFile("/invalid/path"));
  }

  @Test
  public void testCreateFromFileByFileName() throws IOException {
    Typeface typeface = Typeface.createFromFile(obtainPath());
    assertNotNull(typeface);
  }

  private String obtainPath() throws IOException {
    File dir = context.getFilesDir();
    dir.mkdirs();
    File file = new File(dir, "test.jpg");
    if (!file.createNewFile()) {
      if (!file.exists()) {
        fail("Failed to create new File!");
      }
    }
    InputStream is = context.getAssets().open("fonts/others/samplefont.ttf");
    FileOutputStream fOutput = new FileOutputStream(file);
    ByteStreams.copy(is, fOutput);
    is.close();
    fOutput.close();
    return file.getPath();
  }

  @Test
  @Config(minSdk = P)
  public void testInvalidCmapFont() {
    Typeface typeface =
        Typeface.createFromAsset(context.getAssets(), "fonts/security/bombfont.ttf");
    assertNotNull(typeface);
    final String testString = "abcde";
    float widthDefaultTypeface = measureText(testString, Typeface.DEFAULT);
    float widthCustomTypeface = measureText(testString, typeface);
    assertEquals(widthDefaultTypeface, widthCustomTypeface, 1.0f);
  }

  @Test
  @Config(minSdk = P)
  public void testInvalidCmapFont2() {
    Typeface typeface =
        Typeface.createFromAsset(context.getAssets(), "fonts/security/bombfont2.ttf");
    assertNotNull(typeface);
    final String testString = "abcde";
    float widthDefaultTypeface = measureText(testString, Typeface.DEFAULT);
    float widthCustomTypeface = measureText(testString, typeface);
    assertEquals(widthDefaultTypeface, widthCustomTypeface, 1.0f);
  }

  @Test
  @Config(minSdk = O, maxSdk = O_MR1)
  public void testInvalidCmapFont_o_omr1() {
    Typeface typeface =
        Typeface.createFromAsset(context.getAssets(), "fonts/security/bombfont.ttf");
    assertNotNull(typeface);
    Paint p = new Paint();
    final String testString = "abcde";
    float widthDefaultTypeface = p.measureText(testString);
    p.setTypeface(typeface);
    float widthCustomTypeface = p.measureText(testString);
    assertEquals(widthDefaultTypeface, widthCustomTypeface, 1.0f);
  }

  @Test
  @Config(minSdk = O, maxSdk = O_MR1)
  public void testInvalidCmapFont2_o_omr1() {
    Typeface typeface =
        Typeface.createFromAsset(context.getAssets(), "fonts/security/bombfont2.ttf");
    assertNotNull(typeface);
    Paint p = new Paint();
    final String testString = "abcde";
    float widthDefaultTypeface = p.measureText(testString);
    p.setTypeface(typeface);
    float widthCustomTypeface = p.measureText(testString);
    assertEquals(widthDefaultTypeface, widthCustomTypeface, 1.0f);
  }

  @Test
  @Config(minSdk = P)
  public void testInvalidCmapFont_tooLargeCodePoints() {
    // Following three font doen't have any coverage between U+0000..U+10FFFF. Just make sure
    // they don't crash us.
    final String[] invalidCMAPFonts = {
      "fonts/security/out_of_unicode_start_cmap12.ttf",
      "fonts/security/out_of_unicode_end_cmap12.ttf",
      "fonts/security/too_large_start_cmap12.ttf",
      "fonts/security/too_large_end_cmap12.ttf",
    };
    for (final String file : invalidCMAPFonts) {
      final Typeface typeface = Typeface.createFromAsset(context.getAssets(), file);
      assertNotNull(typeface);
    }
  }

  @Test
  @Config(minSdk = P)
  public void testInvalidCmapFont_unsortedEntries() {
    // Following two font files have glyph for U+0400 and U+0100 but the fonts must not be used
    // due to invalid cmap data. For more details, see each ttx source file.
    final String[] invalidCMAPFonts = {
      "fonts/security/unsorted_cmap4.ttf", "fonts/security/unsorted_cmap12.ttf"
    };
    for (final String file : invalidCMAPFonts) {
      final Typeface typeface = Typeface.createFromAsset(context.getAssets(), file);
      assertNotNull(typeface);
      final String testString = "\u0100\u0400";
      final float widthDefaultTypeface = measureText(testString, Typeface.DEFAULT);
      final float widthCustomTypeface = measureText(testString, typeface);
      assertEquals(widthDefaultTypeface, widthCustomTypeface, 0.0f);
    }

    // Following two font files have glyph for U+0400 U+FE00 and U+0100 U+FE00 but the fonts
    // must not be used due to invalid cmap data. For more details, see each ttx source file.
    final String[] invalidCMAPVSFonts = {
      "fonts/security/unsorted_cmap14_default_uvs.ttf",
      "fonts/security/unsorted_cmap14_non_default_uvs.ttf"
    };
    for (final String file : invalidCMAPVSFonts) {
      final Typeface typeface = Typeface.createFromAsset(context.getAssets(), file);
      assertNotNull(typeface);
      final String testString = "\u0100\uFE00\u0400\uFE00";
      final float widthDefaultTypeface = measureText(testString, Typeface.DEFAULT);
      final float widthCustomTypeface = measureText(testString, typeface);
      assertEquals(widthDefaultTypeface, widthCustomTypeface, 0.0f);
    }
  }

  @Test
  @Config(sdk = P)
  public void testCreateFromAsset_cachesTypeface() {
    Typeface typeface1 =
        Typeface.createFromAsset(context.getAssets(), "fonts/others/samplefont.ttf");
    assertNotNull(typeface1);

    Typeface typeface2 =
        Typeface.createFromAsset(context.getAssets(), "fonts/others/samplefont.ttf");
    assertNotNull(typeface2);
    assertSame("Same font asset should return same Typeface object", typeface1, typeface2);

    Typeface typeface3 =
        Typeface.createFromAsset(context.getAssets(), "fonts/others/samplefont2.ttf");
    assertNotNull(typeface3);
    assertNotSame(
        "Different font asset should return different Typeface object", typeface2, typeface3);

    Typeface typeface4 =
        Typeface.createFromAsset(context.getAssets(), "fonts/others/samplefont3.ttf");
    assertNotNull(typeface4);
    assertNotSame(
        "Different font asset should return different Typeface object", typeface2, typeface4);
    assertNotSame(
        "Different font asset should return different Typeface object", typeface3, typeface4);
  }
}
