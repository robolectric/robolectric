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
  public void getColor_invalidValueLength6() {
    int color = ResourceHelper.getColor("#12345");

    // TODO This case should probably throw an exception instead
    assertThat(color).isEqualTo(74565);
  }

  @Test
  public void getColor_invalidValueLength8() {
    int color = ResourceHelper.getColor("#1234567");

    // TODO This case should probably throw an exception instead
    assertThat(color).isEqualTo(19088743);
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
  public void getColorType_nullValue() {
    assertThat(ResourceHelper.getColorType(null)).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getColorType_emptyValue() {
    assertThat(ResourceHelper.getColorType("")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getColorType_valueDoesNotStartWithHashtag() {
    assertThat(ResourceHelper.getColorType("FF0000")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getColorType_invalidValueLength6() {
    assertThat(ResourceHelper.getColorType("#12345")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getColorType_invalidValueLength8() {
    assertThat(ResourceHelper.getColorType("#1234567")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getColorType_RGBValue() {
    assertThat(ResourceHelper.getColorType("#abc")).isEqualTo(TypedValue.TYPE_INT_COLOR_RGB4);
  }

  @Test
  public void getColorType_invalidRGBValue() {
    assertThat(ResourceHelper.getColorType("#zzz")).isEqualTo(TypedValue.TYPE_INT_COLOR_RGB4);
  }

  @Test
  public void getColorType_ARGBValue() {
    assertThat(ResourceHelper.getColorType("#abcd")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB4);
  }

  @Test
  public void getColorType_invalidARGBValue() {
    assertThat(ResourceHelper.getColorType("#zzzz")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB4);
  }

  @Test
  public void getColorType_RRGGBBValue() {
    assertThat(ResourceHelper.getColorType("#a1b2c3")).isEqualTo(TypedValue.TYPE_INT_COLOR_RGB8);
  }

  @Test
  public void getColorType_invalidRRGGBBValue() {
    assertThat(ResourceHelper.getColorType("#zzzzzz")).isEqualTo(TypedValue.TYPE_INT_COLOR_RGB8);
  }

  @Test
  public void getColorType_AARRGGBBValue() {
    assertThat(ResourceHelper.getColorType("#a1b2c3d4")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getColorType_invalidAARRGGBBValue() {
    assertThat(ResourceHelper.getColorType("#zzzzzzzz")).isEqualTo(TypedValue.TYPE_INT_COLOR_ARGB8);
  }

  @Test
  public void getInternalResourceId_nullIdName() {
    assertThrows(NullPointerException.class, () -> ResourceHelper.getInternalResourceId(null));
  }

  @Test
  public void getInternalResourceId_emptyIdName() {
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> ResourceHelper.getInternalResourceId(""));
    assertThat(exception.getCause()).isInstanceOf(NoSuchFieldException.class);
  }

  @Test
  public void getInternalResourceId_invalidIdName() {
    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> ResourceHelper.getInternalResourceId("foo_bar"));
    assertThat(exception.getCause()).isInstanceOf(NoSuchFieldException.class);
  }

  @Test
  public void getInternalResourceId_validIdName() {
    int id = ResourceHelper.getInternalResourceId("content");

    assertThat(id).isEqualTo(com.android.internal.R.id.content);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired() {
    TypedValue out = new TypedValue();

    assertThrows(
        AssertionError.class, () -> ResourceHelper.parseFloatAttribute(null, "0.16", out, true));
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_emptyValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_blankValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "  ", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_nonDigitValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "hello", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_nonAsciiValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "12🍏34", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_validValue_endingWithSpaces() {
    String input = "-1.23  ";
    TypedValue out = new TypedValue();
    float result = Float.parseFloat(input);
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_validValue_invalidEnding() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "-1.23 px", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_validValue_invalidUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "-1.23zz", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeFloatValue() {
    String input = "-1.23";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "-1.23px", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -10317792, -3.0115387e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeSmallFloatValue() {
    String input = "-.23";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "-.23dp", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -493921231, -1.3222637e21f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveFloatValue() {
    String input = "1.23";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "1.23sp", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 10317858, 1.4458399e-38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveSmallFloatValue() {
    String input = ".23";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, ".23pt", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 493921331, 3.1848625e-21f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeIntValue() {
    String input = "-123";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "-123in", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -31484, Float.NaN);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveIntValue() {
    String input = "123";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, "123mm", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 31493, 4.4131e-41f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_emptyValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_blankValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "  ", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_nonDigitValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "hello", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_nonAsciiValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "12🍏34", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_validValue_endingWithSpaces() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-1.23  ", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -10317791, -3.0115389e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_validValue_invalidEnding() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-1.23 px", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_validValue_invalidUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-1.23zz", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-1.23", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -10317791, -3.0115389e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-1.23px", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -10317792, -3.0115387e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeSmallFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-.23", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -493921231, -1.3222637e21f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-.23dp", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -493921231, -1.3222637e21f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "1.23", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 10317857, 1.4458397e-38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "1.23sp", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 10317858, 1.4458399e-38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveSmallFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", ".23", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 493921329, 3.184862e-21f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", ".23pt", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 493921331, 3.1848625e-21f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeIntValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-123", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -31487, Float.NaN);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "-123in", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, -31484, Float.NaN);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveIntValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "123", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 31489, 4.4125e-41f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute("", "123mm", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, TypedValue.TYPE_DIMENSION, 31493, 4.4131e-41f);
  }

  @Test
  public void parseFloatAttribute_lengthEquals1000_parseSucceed() {
    String input = generateTestFloatAttribute("0.16", 1000);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(
        out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(Float.parseFloat(input)), 0.16f);
  }

  @Test
  public void parseFloatAttribute_lengthLargerThan1000_returnsFalse() {
    String input = generateTestFloatAttribute("0.17", 1001);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_lengthLessThan1000_parseSucceed() {
    String input = generateTestFloatAttribute("0.18", 999);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(
        out, TypedValue.TYPE_FLOAT, Float.floatToIntBits(Float.parseFloat(input)), 0.18f);
  }

  private static String generateTestFloatAttribute(String prefixAttribute, int length) {
    StringBuilder builder = new StringBuilder(prefixAttribute);
    int usedLength = builder.length();
    for (int i = 0; i < length - usedLength; i++) {
      builder.append("0");
    }
    return builder.toString();
  }

  private void validateTypedValue(TypedValue out, int type, int data, float result) {
    assertThat(out.assetCookie).isEqualTo(0);
    assertThat(out.string).isNull();
    assertThat(out.type).isEqualTo(type);
    assertThat(out.data).isEqualTo(data);
    assertThat(out.getFloat()).isEqualTo(result);
  }
}
