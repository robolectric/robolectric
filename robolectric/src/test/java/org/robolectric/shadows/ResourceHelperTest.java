package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.util.TypedValue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ResourceHelper}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.NEWEST_SDK)
public class ResourceHelperTest {
  @Test
  public void getColor_nullValue() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor(null));

    assertThat(exception.getMessage()).isEqualTo("Color value cannot be null");
  }

  @Test
  public void getColor_emptyValue() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor(""));

    assertThat(exception.getMessage()).isEqualTo("Color value '' must start with #");
  }

  @Test
  public void getColor_valueDoesNotStartWithHashtag() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor("FF0000"));

    assertThat(exception.getMessage()).isEqualTo("Color value 'FF0000' must start with #");
  }

  @Test
  public void getColor_valueTooLong() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor("#AABBCCDDEE"));

    assertThat(exception.getMessage())
        .isEqualTo(
            "Color value 'AABBCCDDEE' is too long. Format is either #AARRGGBB, #RRGGBB, #RGB, or"
                + " #ARGB");
  }

  @Test
  public void getColor_RGBValue() {
    int color = ResourceHelper.getColor("#abc");

    assertThat(color).isEqualTo(-5588020);
  }

  @Test
  public void getColor_invalidRGBValue() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor("#zzz"));

    assertThat(exception.getMessage()).isEqualTo("For input string: \"FFzzzzzz\" under radix 16");
  }

  @Test
  public void getColor_ARGBValue() {
    int color = ResourceHelper.getColor("#abcd");

    assertThat(color).isEqualTo(-1430532899);
  }

  @Test
  public void getColor_invalidARGBValue() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor("#zzzz"));

    assertThat(exception.getMessage()).isEqualTo("For input string: \"zzzzzzzz\" under radix 16");
  }

  @Test
  public void getColor_RRGGBBValue() {
    int color = ResourceHelper.getColor("#a1b2c3");

    assertThat(color).isEqualTo(-6180157);
  }

  @Test
  public void getColor_invalidRRGGBBValue() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor("#zzzzzz"));

    assertThat(exception.getMessage()).isEqualTo("For input string: \"FFzzzzzz\" under radix 16");
  }

  @Test
  public void getColor_AARRGGBBValue() {
    int color = ResourceHelper.getColor("#a1b2c3d4");

    assertThat(color).isEqualTo(-1582119980);
  }

  @Test
  public void getColor_invalidAARRGGBBValue() {
    NumberFormatException exception =
        assertThrows(NumberFormatException.class, () -> ResourceHelper.getColor("#zzzzzzzz"));

    assertThat(exception.getMessage()).isEqualTo("For input string: \"zzzzzzzz\" under radix 16");
  }

  @Test
  public void parseFloatAttribute() {
    TypedValue out = new TypedValue();
    ResourceHelper.parseFloatAttribute(null, "0.16", out, false);
    assertThat(out.getFloat()).isEqualTo(0.16f);

    out = new TypedValue();
    ResourceHelper.parseFloatAttribute(null, ".16", out, false);
    assertThat(out.getFloat()).isEqualTo(0.16f);
  }

  @Test
  public void parseFloatAttribute_lengthEquals1000_parseSucceed() {
    TypedValue out = new TypedValue();
    boolean parseResult =
        ResourceHelper.parseFloatAttribute(
            null, generateTestFloatAttribute("0.16", 1000), out, false);
    assertThat(parseResult).isTrue();
    assertThat(out.getFloat()).isEqualTo(0.16f);
  }

  @Test
  public void parseFloatAttribute_lengthLargerThan1000_returnsFalse() {
    TypedValue out = new TypedValue();
    boolean parseResult =
        ResourceHelper.parseFloatAttribute(
            null, generateTestFloatAttribute("0.17", 1001), out, false);
    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_lengthLessThan1000_parseSucceed() {
    TypedValue out = new TypedValue();
    boolean parseResult =
        ResourceHelper.parseFloatAttribute(
            null, generateTestFloatAttribute("0.18", 999), out, false);
    assertThat(parseResult).isTrue();
    assertThat(out.getFloat()).isEqualTo(0.18f);
  }

  private static String generateTestFloatAttribute(String prefixAttribute, int length) {
    StringBuilder builder = new StringBuilder(prefixAttribute);
    int usedLength = builder.length();
    for (int i = 0; i < length - usedLength; i++) {
      builder.append("0");
    }
    return builder.toString();
  }
}
