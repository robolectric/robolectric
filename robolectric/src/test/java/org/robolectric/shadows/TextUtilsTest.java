package org.robolectric.shadows;

import android.text.TextPaint;
import android.text.TextUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;

@RunWith(TestRunners.WithDefaults.class)
public class TextUtilsTest {
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
    assertThat(TextUtils.ellipsize("apples", p, 100, TextUtils.TruncateAt.END).toString()).isEqualTo("apples");
    assertThat(TextUtils.ellipsize("", p, 100, TextUtils.TruncateAt.END).toString()).isEqualTo("");
  }
}
