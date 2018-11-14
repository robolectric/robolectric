package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StyleDataTest {

  private final ResName androidSearchViewStyle = new ResName("android", "attr", "searchViewStyle");
  private final ResName myLibSearchViewStyle = new ResName("library.resource", "attr", "searchViewStyle");
  private final ResName myAppSearchViewStyle = new ResName("my.app", "attr", "searchViewStyle");

  @Test
  public void getAttrValue_willFindLibraryResourcesWithSameName() {
    StyleData styleData = new StyleData("library.resource", "Theme_MyApp", "Theme_Material", asList(
        new AttributeResource(myLibSearchViewStyle, "lib_value", "library.resource")
    ));

    assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("lib_value");
    assertThat(styleData.getAttrValue(myLibSearchViewStyle).value).isEqualTo("lib_value");

    assertThat(styleData.getAttrValue(androidSearchViewStyle)).isNull();
  }

  @Test
  public void getAttrValue_willNotFindFrameworkResourcesWithSameName() {
    StyleData styleData = new StyleData("android", "Theme_Material", "Theme", asList(
        new AttributeResource(androidSearchViewStyle, "android_value", "android")
    ));

    assertThat(styleData.getAttrValue(androidSearchViewStyle).value).isEqualTo("android_value");

    assertThat(styleData.getAttrValue(myAppSearchViewStyle)).isNull();
    assertThat(styleData.getAttrValue(myLibSearchViewStyle)).isNull();
  }

  @Test
  public void getAttrValue_willChooseBetweenAmbiguousAttributes() {
    StyleData styleData = new StyleData("android", "Theme_Material", "Theme", asList(
        new AttributeResource(myLibSearchViewStyle, "lib_value", "library.resource"),
        new AttributeResource(androidSearchViewStyle, "android_value", "android")
    ));

    assertThat(styleData.getAttrValue(androidSearchViewStyle).value).isEqualTo("android_value");
    assertThat(styleData.getAttrValue(myLibSearchViewStyle).value).isEqualTo("lib_value");

    // todo: any packageNames that aren't 'android' should be treated as equivalent
//    assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("lib_value");
  }

  @Test
  public void getAttrValue_willReturnTrimmedAttributeValues() throws Exception {
    StyleData styleData = new StyleData("library.resource", "Theme_MyApp", "Theme_Material", asList(
            new AttributeResource(myLibSearchViewStyle, "\n lib_value ", "library.resource")
    ));

    assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("\n lib_value ");
    assertThat(styleData.getAttrValue(myLibSearchViewStyle).trimmedValue).isEqualTo("lib_value");
  }

}
