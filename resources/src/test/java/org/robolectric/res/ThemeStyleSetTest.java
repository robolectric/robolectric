package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ThemeStyleSetTest {

  private ThemeStyleSet themeStyleSet;

  @Before
  public void setUp() throws Exception {
    themeStyleSet = new ThemeStyleSet();
  }

  @Test
  public void shouldFindAttributesFromAnAppliedStyle() throws Exception {
    themeStyleSet = new ThemeStyleSet();
    themeStyleSet.apply(createStyle("style1",
        createAttribute("string1", "string1 value from style1"),
        createAttribute("string2", "string2 value from style1")
    ), false);
    themeStyleSet.apply(createStyle("style2", createAttribute("string2", "string2 value from style2")), false);
    assertThat(themeStyleSet.getAttrValue(attrName("string1")).value).isEqualTo("string1 value from style1");
    assertThat(themeStyleSet.getAttrValue(attrName("string2")).value).isEqualTo("string2 value from style1");
  }

  @Test
  public void shouldFindAttributesFromAnAppliedFromForcedStyle() throws Exception {
    themeStyleSet.apply(createStyle("style1",
        createAttribute("string1", "string1 value from style1"),
        createAttribute("string2", "string2 value from style1")
    ), false);
    themeStyleSet.apply(createStyle("style2", createAttribute("string1", "string1 value from style2")), true);
    assertThat(themeStyleSet.getAttrValue(attrName("string1")).value).isEqualTo("string1 value from style2");
    assertThat(themeStyleSet.getAttrValue(attrName("string2")).value).isEqualTo("string2 value from style1");
  }

  private StyleData createStyle(String styleName, AttributeResource... attributeResources) {
    return new StyleData("package", styleName, null, asList(attributeResources));
  }

  private AttributeResource createAttribute(String attrName, String value) {
    return new AttributeResource(attrName(attrName), value, "package");
  }

  private ResName attrName(String attrName) {
    return new ResName("package", "attr", attrName);
  }
}