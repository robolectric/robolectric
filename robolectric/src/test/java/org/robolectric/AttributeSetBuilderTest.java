package org.robolectric;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.res.AttributeResource.ANDROID_NS;
import static org.robolectric.res.AttributeResource.ANDROID_RES_NS_PREFIX;
import static org.robolectric.res.AttributeResource.RES_AUTO_NS_URI;

import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.res.AttributeResource;

/**
 * Tests for {@link Robolectric#buildAttributeSet()}
 */
@RunWith(TestRunners.SelfTest.class)
public class AttributeSetBuilderTest {

  private static final String APP_NS = RES_AUTO_NS_URI;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void getAttributeResourceValue_shouldReturnTheResourceValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, "@android:string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "text", 0)).isEqualTo(android.R.string.ok);
  }

  @Test
  public void getAttributeResourceValueWithLeadingWhitespace_shouldReturnTheResourceValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, " @android:string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "text", 0)).isEqualTo(android.R.string.ok);
  }

  @Test
  public void getSystemAttributeResourceValue_shouldReturnDefaultValueForNullResourceId() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, AttributeResource.NULL_VALUE)
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_RES_NS_PREFIX + "com.some.namespace", "text", 0)).isEqualTo(0);
  }

  @Test
  public void getSystemAttributeResourceValue_shouldReturnDefaultValueForNonMatchingNamespaceId() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.id, "@+id/text1")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_RES_NS_PREFIX + "com.some.other.namespace", "id", 0)).isEqualTo(0);
  }

  @Test
  public void shouldCopeWithDefiningLocalIds() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.id, "@+id/text1")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(ANDROID_NS, "id", 0)).isEqualTo(R.id.text1);
  }

  @Test
  public void getAttributeResourceValue_withNamespace_shouldReturnTheResourceValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.message, "@string/howdy")
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(APP_NS, "message", 0)).isEqualTo(R.string.howdy);
  }

  @Test
  public void getAttributeResourceValue_shouldReturnDefaultValueWhenAttributeIsNull() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.text, AttributeResource.NULL_VALUE)
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(APP_NS, "message", -1)).isEqualTo(-1);
  }

  @Test
  public void getAttributeResourceValue_shouldReturnDefaultValueWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeResourceValue(APP_NS, "message", -1)).isEqualTo(-1);
  }

  @Test
  public void getAttributeBooleanValue_shouldGetBooleanValuesFromAttributes() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "true")
        .build();

    assertThat(roboAttributeSet.getAttributeBooleanValue(APP_NS, "isSugary", false)).isTrue();
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

    assertThat(roboAttributeSet.getAttributeBooleanValue(ANDROID_RES_NS_PREFIX + "com.some.namespace", "isSugary", true)).isTrue();
  }

  @Test
  public void getAttributeValue_byName_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "oh heck yeah")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(APP_NS, "isSugary")).isEqualTo("oh heck yeah");
  }

  @Test
  public void getAttributeValue_byNameWithReference_shouldReturnFullyQualifiedValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "@string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(APP_NS, "isSugary")).isEqualTo("@" + R.string.ok);
  }

  @Test
  public void getAttributeValue_byId_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "oh heck yeah")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(0)).isEqualTo("oh heck yeah");
  }

  @Test
  public void getAttributeValue_byIdWithReference_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.isSugary, "@string/ok")
        .build();

    assertThat(roboAttributeSet.getAttributeValue(0)).isEqualTo("@" + R.string.ok);
  }

  @Test
  public void getAttributeIntValue_shouldReturnValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.sugarinessPercent, "100")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "sugarinessPercent", 0)).isEqualTo(100);
  }

  @Test
  public void getAttributeIntValue_shouldReturnHexValueFromAttribute() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.sugarinessPercent, "0x10")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "sugarinessPercent", 0)).isEqualTo(16);
  }

  @Test
  public void getAttributeIntValue_whenTypeAllowsIntOrEnum_withInt_shouldReturnInt() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.numColumns, "3")
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "numColumns", 0)).isEqualTo(3);
  }

  @Test
  public void getAttributeIntValue_shouldReturnValueFromAttributeWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "sugarinessPercent", 42)).isEqualTo(42);
  }

  @Test
  public void getAttributeIntValue_shouldReturnEnumValuesForEnumAttributesWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeIntValue(APP_NS, "itemType", 24)).isEqualTo(24);
  }

  @Test
  public void getAttributeFloatValue_shouldGetFloatValuesFromAttributes() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.aspectRatio, "1234.456")
        .build();

    assertThat(roboAttributeSet.getAttributeFloatValue(APP_NS, "aspectRatio", 78.9f)).isEqualTo(1234.456f);
  }

  @Test
  public void getAttributeFloatValue_shouldReturnDefaultFloatValueWhenNotInAttributeSet() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getAttributeFloatValue(APP_NS, "aspectRatio", 78.9f)).isEqualTo(78.9f);
  }

  @Test
  public void getStyleAttribute_doesNotThrowException() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    roboAttributeSet.getStyleAttribute();
  }

  @Test
  public void getStyleAttribute_returnsZeroWhenNoStyle() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .build();

    assertThat(roboAttributeSet.getStyleAttribute()).isEqualTo(0);
  }

  @Test
  public void getStyleAttribute_returnsCorrectValue() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .setStyleAttribute("@style/Gastropod")
        .build();

    assertThat(roboAttributeSet.getStyleAttribute()).isEqualTo(R.style.Gastropod);
  }

  @Test
  public void getStyleAttribute_whenStyleIsBogus() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .setStyleAttribute("@style/non_existent_style")
        .build();

    assertThat(roboAttributeSet.getStyleAttribute()).isEqualTo(0);
  }

  @Test
  public void getAttributeNameResource() throws Exception {
    AttributeSet roboAttributeSet = Robolectric.buildAttributeSet()
        .addAttribute(R.attr.aspectRatio, "1")
        .build();

    assertThat(roboAttributeSet.getAttributeNameResource(0)).isEqualTo(R.attr.aspectRatio);
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
}
