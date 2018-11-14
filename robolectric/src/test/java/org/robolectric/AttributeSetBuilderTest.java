package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.robolectric.res.AttributeResource.ANDROID_NS;
import static org.robolectric.res.AttributeResource.ANDROID_RES_NS_PREFIX;
import static org.robolectric.res.AttributeResource.RES_AUTO_NS_URI;

import android.util.AttributeSet;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.res.AttributeResource;

/** Tests for {@link Robolectric#buildAttributeSet()} */
@RunWith(AndroidJUnit4.class)
public class AttributeSetBuilderTest {

  private static final String APP_NS = RES_AUTO_NS_URI;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void getAttributeResourceValue_shouldReturnTheResourceValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, "@android:string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "text", 0))
        .isEqualTo(android.R.string.ok);
  }

  @Test
  public void getAttributeResourceValueWithLeadingWhitespace_shouldReturnTheResourceValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, " @android:string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "text", 0))
        .isEqualTo(android.R.string.ok);
  }

  @Test
  public void getSystemAttributeResourceValue_shouldReturnDefaultValueForNullResourceId() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, AttributeResource.NULL_VALUE)
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_RES_NS_PREFIX + "com.some.namespace", "text", 0))
        .isEqualTo(0);
  }

  @Test
  public void getSystemAttributeResourceValue_shouldReturnDefaultValueForNonMatchingNamespaceId() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.id, "@+id/text1")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_RES_NS_PREFIX + "com.some.other.namespace", "id", 0))
        .isEqualTo(0);
  }

  @Test
  public void shouldCopeWithDefiningLocalIds() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.id, "@+id/text1")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "id", 0))
        .isEqualTo(R.id.text1);
  }

  @Test
  public void getAttributeResourceValue_withNamespace_shouldReturnTheResourceValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.message, "@string/howdy")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(APP_NS, "message", 0))
        .isEqualTo(R.string.howdy);
  }

  @Test
  public void getAttributeResourceValue_shouldReturnDefaultValueWhenAttributeIsNull() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, AttributeResource.NULL_VALUE)
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(APP_NS, "message", -1))
        .isEqualTo(-1);
  }

  @Test
  public void getAttributeResourceValue_shouldReturnDefaultValueWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(APP_NS, "message", -1))
        .isEqualTo(-1);
  }

  @Test
  public void getAttributeBooleanValue_shouldGetBooleanValuesFromAttributes() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "true")
        .build();

    assertThat(roboAttributeSet.getAttributeBooleanValue(APP_NS, "isSugary", false))
        .isTrue();
  }

  @Test
  public void getAttributeBooleanValue_withNamespace_shouldGetBooleanValuesFromAttributes() throws Exception {
    // org.robolectric.lib1.R values should be reconciled to match org.robolectric.R values.
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "true")
        .build();

    assertThat(roboAttributeSet.getAttributeBooleanValue(APP_NS, "isSugary", false)).isTrue();
  }

  @Test
  public void getAttributeBooleanValue_shouldReturnDefaultBooleanValueWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet =  Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeBooleanValue(ANDROID_RES_NS_PREFIX + "com.some.namespace", "isSugary", true))
        .isTrue();
  }

  @Test
  public void getAttributeValue_byName_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "oh heck yeah")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(APP_NS, "isSugary"))
        .isEqualTo("false");
    assertThat(roboAttributeSet.getAttributeBooleanValue(APP_NS, "isSugary", true))
        .isEqualTo(false);
    assertThat(roboAttributeSet.getAttributeBooleanValue(APP_NS, "animalStyle", true))
        .isEqualTo(true);
  }

  @Test
  public void getAttributeValue_byNameWithReference_shouldReturnFullyQualifiedValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "@string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(APP_NS, "isSugary"))
        .isEqualTo("@" + R.string.ok);
  }

  @Test
  public void getAttributeValue_byId_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "oh heck yeah")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(0))
        .isEqualTo("false");
  }

  @Test
  public void getAttributeValue_byIdWithReference_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "@string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(0))
        .isEqualTo("@" + R.string.ok);
  }

  @Test
  public void getAttributeIntValue_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.sugarinessPercent, "100")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "sugarinessPercent", 0))
        .isEqualTo(100);
  }

  @Test
  public void getAttributeIntValue_shouldReturnHexValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.sugarinessPercent, "0x10")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "sugarinessPercent", 0))
        .isEqualTo(16);
  }

  @Test
  public void getAttributeIntValue_whenTypeAllowsIntOrEnum_withInt_shouldReturnInt() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.numColumns, "3")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "numColumns", 0))
        .isEqualTo(3);
  }

  @Test
  public void getAttributeIntValue_shouldReturnValueFromAttributeWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "sugarinessPercent", 42))
        .isEqualTo(42);
  }

  @Test
  public void getAttributeIntValue_shouldReturnEnumValuesForEnumAttributesWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "itemType", 24))
        .isEqualTo(24);
  }

  @Test
  public void getAttributeIntValue_shouldReturnEnumValuesForEnumAttributesInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.itemType, "ungulate")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "itemType", 24))
        .isEqualTo(1);

    AttributeSet roboAttributeSet2 = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.itemType, "marsupial")
        .build();

    assertThat(roboAttributeSet2.getAttributeIntValue(APP_NS, "itemType", 24))
        .isEqualTo(0);
  }

  @Test
  public void shouldFailOnMissingEnumValue() throws Exception {
    try {
      Robolectric.buildAttributeSet()
          .addAttribute(R.attr.itemType, "simian")
          .build();
      fail("should fail");
    } catch (Exception e) {
      // expected
      assertThat(e.getMessage()).contains("no value found for simian");
    }
  }

  @Test
  public void shouldFailOnMissingFlagValue() throws Exception {
    try {
      Robolectric.buildAttributeSet()
          .addAttribute(R.attr.scrollBars, "temporal")
          .build();
      fail("should fail");
    } catch (Exception e) {
      // expected
      assertThat(e.getMessage()).contains("no value found for temporal");
    }
  }

  @Test
  public void getAttributeIntValue_shouldReturnFlagValuesForFlagAttributesInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.scrollBars, "horizontal|vertical")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "scrollBars", 24))
        .isEqualTo(0x100 | 0x200);
  }

  @Test
  public void getAttributeFloatValue_shouldGetFloatValuesFromAttributes() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.aspectRatio, "1234.456")
        .build();

    assertThat(roboAttributeSet.getAttributeFloatValue(APP_NS, "aspectRatio", 78.9f))
        .isEqualTo(1234.456f);
  }

  @Test
  public void getAttributeFloatValue_shouldReturnDefaultFloatValueWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeFloatValue(APP_NS, "aspectRatio", 78.9f))
        .isEqualTo(78.9f);
  }

  @Test
  public void getClassAndIdAttribute_returnsZeroWhenNotSpecified() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet().build();
    assertThat(roboAttributeSet.getClassAttribute()).isNull();
    assertThat(roboAttributeSet.getIdAttribute()).isNull();
  }

  @Test
  public void getClassAndIdAttribute_returnsAttr() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .setIdAttribute("the id")
        .setClassAttribute("the class")
        .build();
    assertThat(roboAttributeSet.getClassAttribute()).isEqualTo("the class");
    assertThat(roboAttributeSet.getIdAttribute()).isEqualTo("the id");
  }

  @Test
  public void getStyleAttribute_returnsZeroWhenNoStyle() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getStyleAttribute())
        .isEqualTo(0);
  }

  @Test
  public void getStyleAttribute_returnsCorrectValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .setStyleAttribute("@style/Gastropod")
        .build();

    assertThat(roboAttributeSet.getStyleAttribute())
        .isEqualTo(R.style.Gastropod);
  }

  @Test
  public void getStyleAttribute_whenStyleIsBogus() throws Exception {
    try {
      Robolectric.buildAttributeSet()
            .setStyleAttribute("@style/non_existent_style")
            .build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .contains("no such resource @style/non_existent_style while resolving value for style");
    }
  }

  @Test
  public void getAttributeNameResource() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.aspectRatio, "1")
        .build();

    assertThat(roboAttributeSet.getAttributeNameResource(0))
        .isEqualTo(R.attr.aspectRatio);
  }

  @Test
  public void shouldReturnAttributesInOrderOfNameResId() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.height, "1px")
        .addAttribute(R.attr.animalStyle, "meow")
        .addAttribute(android.R.attr.width, "1px")
        .build();

    assertThat(asList(
        roboAttributeSet.getAttributeName(0),
        roboAttributeSet.getAttributeName(1),
        roboAttributeSet.getAttributeName(2)
    )).containsExactly("height", "width", "animalStyle");

    assertThat(asList(
        roboAttributeSet.getAttributeNameResource(0),
        roboAttributeSet.getAttributeNameResource(1),
        roboAttributeSet.getAttributeNameResource(2)
    )).containsExactly(android.R.attr.height, android.R.attr.width, R.attr.animalStyle);
  }

  @Test
  public void whenAttrSetAttrSpecifiesUnknownStyle_throwsException() throws Exception {
    try {
      Robolectric.buildAttributeSet()
          .addAttribute(R.attr.string2, "?org.robolectric:attr/noSuchAttr")
          .build();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).contains("no such attr ?org.robolectric:attr/noSuchAttr");
      assertThat(e.getMessage()).contains("while resolving value for org.robolectric:attr/string2");
    }
  }

  @Test
  public void whenAttrSetAttrSpecifiesUnknownReference_throwsException() throws Exception {
    try {
      Robolectric.buildAttributeSet()
          .addAttribute(R.attr.string2, "@org.robolectric:attr/noSuchRes")
          .build();
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).contains("no such resource @org.robolectric:attr/noSuchRes");
      assertThat(e.getMessage()).contains("while resolving value for org.robolectric:attr/string2");
    }
  }

}
