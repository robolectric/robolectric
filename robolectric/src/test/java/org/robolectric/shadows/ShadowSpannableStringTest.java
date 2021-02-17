package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.text.SpannableString;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowSpannableStringTest {
  private static final String TEST_STRING = "Visit us at http://www.foobar.com for more selections";

  private SpannableString spanStr;

  @Before
  public void setUp() throws Exception {
    spanStr = new SpannableString(TEST_STRING);
  }

  @Test
  public void testToString() {
    assertThat(spanStr.toString()).isSameInstanceAs(TEST_STRING);
  }

  @Test
  public void testSetSpan() {
    URLSpan s1 = new URLSpan("http://www.foobar.com");
    UnderlineSpan s2 = new UnderlineSpan();
    spanStr.setSpan(s1, 12, 33, 0);
    spanStr.setSpan(s2, 1, 10, 0);

    assertBothSpans(s1, s2);
  }

  @Test
  public void testRemoveSpan() {
    URLSpan s1 = new URLSpan("http://www.foobar.com");
    UnderlineSpan s2 = new UnderlineSpan();
    spanStr.setSpan(s1, 12, 33, 0);
    spanStr.setSpan(s2, 1, 10, 0);
    spanStr.removeSpan(s1);

    Object[] spans = spanStr.getSpans(0, TEST_STRING.length(), Object.class);
    assertThat(spans).isNotNull();
    assertThat(spans.length).isEqualTo(1);
    assertThat(spans[0]).isSameInstanceAs(s2);
  }

  @Test
  public void testGetSpans() {
    URLSpan s1 = new URLSpan("http://www.foobar.com");
    UnderlineSpan s2 = new UnderlineSpan();
    spanStr.setSpan(s1, 1, 10, 0);
    spanStr.setSpan(s2, 20, 30, 0);

    Object[] spans = spanStr.getSpans(0, TEST_STRING.length(), Object.class);
    assertThat(spans).isNotNull();
    assertThat(spans.length).isEqualTo(2);
    assertBothSpans(s1, s2);

    spans = spanStr.getSpans(0, TEST_STRING.length(), URLSpan.class);
    assertThat(spans).isNotNull();
    assertThat(spans.length).isEqualTo(1);
    assertThat(spans[0]).isSameInstanceAs(s1);

    spans = spanStr.getSpans(11, 35, Object.class);
    assertThat(spans).isNotNull();
    assertThat(spans.length).isEqualTo(1);
    assertThat(spans[0]).isSameInstanceAs(s2);

    spans = spanStr.getSpans(21, 35, Object.class);
    assertThat(spans).isNotNull();
    assertThat(spans.length).isEqualTo(1);
    assertThat(spans[0]).isSameInstanceAs(s2);

    spans = spanStr.getSpans(5, 15, Object.class);
    assertThat(spans).isNotNull();
    assertThat(spans.length).isEqualTo(1);
    assertThat(spans[0]).isSameInstanceAs(s1);
  }

  private void assertBothSpans(URLSpan s1, UnderlineSpan s2) {
    Object[] spans = spanStr.getSpans(0, TEST_STRING.length(), Object.class);
    if (spans[0] instanceof URLSpan) {
      assertThat(spans[0]).isSameInstanceAs(s1);
    } else {
      assertThat(spans[0]).isSameInstanceAs(s2);
    }
    if (spans[1] instanceof UnderlineSpan) {
      assertThat(spans[1]).isSameInstanceAs(s2);
    } else {
      assertThat(spans[1]).isSameInstanceAs(s1);
    }
  }

}
