package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.util.TypedValue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Unit tests for {@link ResourceHelper2}. */
@RunWith(AndroidJUnit4.class)
@Config(sdk = Config.NEWEST_SDK)
public class ResourceHelper2Test {
  @Test
  public void parseFloatAttribute_attributeNull_unitRequired() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "0.16", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "0.16dp", TypedValue.TYPE_DIMENSION, 343597361, 1.2666186e-26f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_emptyValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_blankValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "  ", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_nonDigitValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "hello", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_nonAsciiValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "12🍏34", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_validValue_endingWithSpaces() {
    String input = "-1.23  ";
    TypedValue out = new TypedValue();
    float result = Float.parseFloat(input);
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_validValue_invalidEnding() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "-1.23 px", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_validValue_invalidUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "-1.23zz", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeFloatValue() {
    String input = "-1.23";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "-1.23px", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23px", TypedValue.TYPE_DIMENSION, -10317792, -3.0115387e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeSmallFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "-.23", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "-.23dp", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveFloatValue() {
    String input = "1.23";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "1.23sp", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23sp", TypedValue.TYPE_DIMENSION, 10317858, 1.4458399e-38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveSmallFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, ".23", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, ".23pt", out, false);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeIntValue() {
    String input = "-123";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_negativeIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "-123in", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "123.0in", TypedValue.TYPE_DIMENSION, -31484, Float.NaN);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveIntValue() {
    String input = "123";
    float result = Float.parseFloat(input);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(result), result);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitNotRequired_positiveIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, "123mm", out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "123.0mm", TypedValue.TYPE_DIMENSION, 31493, 4.4131e-41f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_emptyValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_blankValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "  ", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_nonDigitValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "hello", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_nonAsciiValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "12🍏34", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_validValue_endingWithSpaces() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-1.23  ", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23dp", TypedValue.TYPE_DIMENSION, -10317791, -3.0115389e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_validValue_invalidEnding() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-1.23 px", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_validValue_invalidUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-1.23zz", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-1.23", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23dp", TypedValue.TYPE_DIMENSION, -10317791, -3.0115389e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-1.23px", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23px", TypedValue.TYPE_DIMENSION, -10317792, -3.0115387e38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeSmallFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-.23", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-.23dp", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "1.23", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23dp", TypedValue.TYPE_DIMENSION, 10317857, 1.4458397e-38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "1.23sp", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "1.23sp", TypedValue.TYPE_DIMENSION, 10317858, 1.4458399e-38f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveSmallFloatValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", ".23", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveSmallFloatValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", ".23pt", out, true);

    assertThat(parseResult).isFalse();
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeIntValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-123", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "123.0dp", TypedValue.TYPE_DIMENSION, -31487, Float.NaN);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_negativeIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "-123in", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "123.0in", TypedValue.TYPE_DIMENSION, -31484, Float.NaN);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveIntValue() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "123", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "123.0dp", TypedValue.TYPE_DIMENSION, 31489, 4.4125e-41f);
  }

  @Test
  public void parseFloatAttribute_attributeNull_unitRequired_positiveIntValue_withUnit() {
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute("", "123mm", out, true);

    assertThat(parseResult).isTrue();
    validateTypedValue(out, "123.0mm", TypedValue.TYPE_DIMENSION, 31493, 4.4131e-41f);
  }

  @Test
  public void parseFloatAttribute_lengthEquals1000_parseSucceed() {
    String input = generateTestFloatAttribute("0.16", 1000);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(
        out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(Float.parseFloat(input)), 0.16f);
  }

  @Test
  public void parseFloatAttribute_lengthLargerThan1000_parseSucceed() {
    String input = generateTestFloatAttribute("0.17", 1001);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(
        out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(Float.parseFloat(input)), 0.17f);
  }

  @Test
  public void parseFloatAttribute_lengthLessThan1000_parseSucceed() {
    String input = generateTestFloatAttribute("0.18", 999);
    TypedValue out = new TypedValue();
    boolean parseResult = ResourceHelper2.parseFloatAttribute(null, input, out, false);

    assertThat(parseResult).isTrue();
    validateTypedValue(
        out, null, TypedValue.TYPE_FLOAT, Float.floatToIntBits(Float.parseFloat(input)), 0.18f);
  }

  private static String generateTestFloatAttribute(String prefixAttribute, int length) {
    StringBuilder builder = new StringBuilder(prefixAttribute);
    int usedLength = builder.length();
    for (int i = 0; i < length - usedLength; i++) {
      builder.append("0");
    }
    return builder.toString();
  }

  private void validateTypedValue(TypedValue out, String string, int type, int data, float result) {
    assertThat(out.assetCookie).isEqualTo(0);
    assertThat(out.string).isEqualTo(string);
    assertThat(out.type).isEqualTo(type);
    assertThat(out.data).isEqualTo(data);
    assertThat(out.getFloat()).isEqualTo(result);
  }
}
