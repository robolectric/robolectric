package android.text;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.graphics.Paint;
import android.text.TextUtils.TruncateAt;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test file for {@link TextUtils}.
 *
 * <p>Copied from <a
 * href="https://cs.android.com/android/platform/superproject/main/+/main:cts/tests/tests/text/src/android/text/cts/TextUtilsTest.java">...</a>.
 */
@RunWith(AndroidJUnit4.class)
public class TextUtilsTest {
  private String mEllipsis;

  @Before
  public void setup() {
    mEllipsis = getEllipsis();
  }

  /**
   * Get the ellipsis from system.
   *
   * @return the string of ellipsis.
   */
  private static String getEllipsis() {
    String text = "xxxxx";
    TextPaint p = new TextPaint();
    float width = p.measureText(text.substring(1));
    String re = TextUtils.ellipsize(text, p, width, TruncateAt.START).toString();
    return re.substring(0, re.indexOf("x"));
  }

  @Test
  public void testEllipsize() {
    TextPaint p = new TextPaint();

    // turn off kerning. with kerning enabled, different methods of measuring the same text
    // produce different results.
    p.setFlags(p.getFlags() & ~Paint.DEV_KERN_TEXT_FLAG);

    CharSequence text = "long string to truncate";

    float textWidth = p.measureText(mEllipsis) + p.measureText("uncate");
    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.START).toString())
        .isEqualTo(mEllipsis + "uncate");

    textWidth = p.measureText("long str") + p.measureText(mEllipsis);
    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.END).toString())
        .isEqualTo("long str" + mEllipsis);

    textWidth = p.measureText("long") + p.measureText(mEllipsis) + p.measureText("ate");
    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.MIDDLE).toString())
        .isEqualTo("long" + mEllipsis + "ate");

    // issue 1688347, ellipsize() is not defined for TruncateAt.MARQUEE.
    // In the code it looks like this does the same as MIDDLE.
    // In other methods, MARQUEE is equivalent to END, except for the first line.
    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.MARQUEE).toString())
        .isEqualTo("long" + mEllipsis + "ate");

    textWidth = p.measureText(mEllipsis);
    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.END).toString()).isEqualTo("");
    assertThat(TextUtils.ellipsize(text, p, textWidth - 1, TruncateAt.END).toString())
        .isEqualTo("");
    assertThat(TextUtils.ellipsize(text, p, -1f, TruncateAt.END).toString()).isEqualTo("");
    assertThat(TextUtils.ellipsize(text, p, Float.MAX_VALUE, TruncateAt.END).toString())
        .isEqualTo(text);

    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.START).toString()).isEqualTo("");
    assertThat(TextUtils.ellipsize(text, p, textWidth, TruncateAt.MIDDLE).toString()).isEqualTo("");

    try {
      TextUtils.ellipsize(text, null, textWidth, TruncateAt.MIDDLE);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }

    try {
      TextUtils.ellipsize(null, p, textWidth, TruncateAt.MIDDLE);
      fail("Should throw NullPointerException");
    } catch (NullPointerException e) {
      // expected
    }
  }
}
