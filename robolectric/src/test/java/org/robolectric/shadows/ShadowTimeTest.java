package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;

import android.os.SystemClock;
import android.text.format.Time;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = JELLY_BEAN_MR2)
public class ShadowTimeTest {

  @Test
  public void shouldFormatAllFormats() {
    Time t = new Time("Asia/Tokyo");
    t.set(1407496560000L);

    // Don't check for %c (the docs state not to use it, and it doesn't work correctly).
    assertEquals("Fri", t.format("%a"));
    assertEquals("Friday", t.format("%A"));
    assertEquals("Aug", t.format("%b"));
    assertEquals("August", t.format("%B"));
    assertEquals("20", t.format("%C"));
    assertEquals("08", t.format("%d"));
    assertEquals("08/08/14", t.format("%D"));
    assertEquals(" 8", t.format("%e"));
    assertEquals("2014-08-08", t.format("%F"));
    assertEquals("14", t.format("%g"));
    assertEquals("2014", t.format("%G"));
    assertEquals("Aug", t.format("%h"));
    assertEquals("20", t.format("%H"));
    assertEquals("08", t.format("%I"));
    assertEquals("220", t.format("%j"));
    assertEquals("20", t.format("%k"));
    assertEquals(" 8", t.format("%l"));
    assertEquals("08", t.format("%m"));
    assertEquals("16", t.format("%M"));
    assertEquals("\n", t.format("%n"));
    assertEquals("PM", t.format("%p"));
    assertEquals("pm", t.format("%P"));
    assertEquals("08:16:00 PM", t.format("%r"));
    assertEquals("20:16", t.format("%R"));
    assertEquals("1407496560", t.format("%s"));
    assertEquals("00", t.format("%S"));
    assertEquals("\t", t.format("%t"));
    assertEquals("20:16:00", t.format("%T"));
    assertEquals("5", t.format("%u"));
    assertEquals("32", t.format("%V"));
    assertEquals("5", t.format("%w"));
    assertEquals("14", t.format("%y"));
    assertEquals("2014", t.format("%Y"));
    assertEquals("+0900", t.format("%z"));
    assertEquals("JST", t.format("%Z"));

    // Padding.
    assertEquals("8", t.format("%-l"));
    assertEquals(" 8", t.format("%_l"));
    assertEquals("08", t.format("%0l"));

    // Escape.
    assertEquals("%", t.format("%%"));
  }

  @Test
  @Config(maxSdk = KITKAT_WATCH)
  // these fail on LOLLIPOP+; is the shadow impl of format correct for pre-LOLLIPOP?
  public void shouldFormatAllFormats_withQuestionableResults() {
    Time t = new Time("Asia/Tokyo");
    t.set(1407496560000L);

    assertEquals("08/08/2014", t.format("%x"));
    assertEquals("08:16:00 PM", t.format("%X"));

    // Case.
    assertEquals("PM", t.format("%^P"));
    assertEquals("PM", t.format("%#P"));
  }

  @Test
  public void shouldSetToNow() {
    Time t = new Time();
    SystemClock.setCurrentTimeMillis(1000);
    t.setToNow();
    assertThat(t.toMillis(false)).isEqualTo(1000);
  }
}
