package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertArrayEquals;

import android.text.TextPaint;
import android.text.TextUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowTextUtilsTest {
  @Test
  public void testExpandTemplate() throws Exception {
    assertThat(TextUtils.expandTemplate("a^1b^2c^3d", "A", "B", "C", "D").toString()).isEqualTo("aAbBcCd");
  }

  @Test
  public void testIsEmpty() throws Exception {
    assertThat(TextUtils.isEmpty(null)).isTrue();
    assertThat(TextUtils.isEmpty("")).isTrue();
    assertThat(TextUtils.isEmpty(" ")).isFalse();
    assertThat(TextUtils.isEmpty("123")).isFalse();
  }

  @Test public void testJoin() {
    assertThat(TextUtils.join(",", new String[]{"1"})).isEqualTo("1");
    assertThat(TextUtils.join(",", new String[]{"1", "2", "3"})).isEqualTo("1,2,3");
    assertThat(TextUtils.join(",", Arrays.asList("1", "2", "3"))).isEqualTo("1,2,3");
  }

  @Test
  public void testIsDigitsOnly() throws Exception {
    assertThat(TextUtils.isDigitsOnly("123456")).isTrue();
    assertThat(TextUtils.isDigitsOnly("124a56")).isFalse();
  }

  @Test
  public void testSplit() {
    //empty
    assertThat(TextUtils.split("", ",").length).isEqualTo(0);

    //one value
    assertArrayEquals(TextUtils.split("abc", ","), new String[]{"abc"});

    //two values
    assertArrayEquals(TextUtils.split("abc,def", ","), new String[]{"abc", "def"});

    //two values with space
    assertArrayEquals(TextUtils.split("abc, def", ","), new String[]{"abc", " def"});
  }

  @Test
  public void testEquals() {
    assertThat(TextUtils.equals(null, null)).isTrue();
    assertThat(TextUtils.equals("", "")).isTrue();
    assertThat(TextUtils.equals("a", "a")).isTrue();
    assertThat(TextUtils.equals("ab", "ab")).isTrue();

    assertThat(TextUtils.equals(null, "")).isFalse();
    assertThat(TextUtils.equals("", null)).isFalse();

    assertThat(TextUtils.equals(null, "a")).isFalse();
    assertThat(TextUtils.equals("a", null)).isFalse();

    assertThat(TextUtils.equals(null, "ab")).isFalse();
    assertThat(TextUtils.equals("ab", null)).isFalse();

    assertThat(TextUtils.equals("", "a")).isFalse();
    assertThat(TextUtils.equals("a", "")).isFalse();

    assertThat(TextUtils.equals("", "ab")).isFalse();
    assertThat(TextUtils.equals("ab", "")).isFalse();

    assertThat(TextUtils.equals("a", "ab")).isFalse();
    assertThat(TextUtils.equals("ab", "a")).isFalse();
  }

  @Test public void testEllipsize() {
    TextPaint p = new TextPaint();
    assertThat(TextUtils.ellipsize("apples", p, 0, TextUtils.TruncateAt.END).toString())
        .isEqualTo("");
    assertThat(TextUtils.ellipsize("apples", p, -1, TextUtils.TruncateAt.END).toString())
        .isEqualTo("");
    assertThat(TextUtils.ellipsize("apples", p, 3, TextUtils.TruncateAt.END).toString())
        .isEqualTo("app");
    assertThat(TextUtils.ellipsize("apples", p, 100, TextUtils.TruncateAt.END).toString())
        .isEqualTo("apples");
    assertThat(TextUtils.ellipsize("", p, 100, TextUtils.TruncateAt.END).toString()).isEqualTo("");
  }
}
