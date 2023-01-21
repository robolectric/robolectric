package org.robolectric.res

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ThemeStyleSetTest {

  private lateinit var themeStyleSet: ThemeStyleSet

  @Before
  @Throws(Exception::class)
  fun setUp() {
    themeStyleSet = ThemeStyleSet()
  }

  @Test
  @Throws(Exception::class)
  fun shouldFindAttributesFromAnAppliedStyle() {
    themeStyleSet = ThemeStyleSet()
    themeStyleSet.apply(
      createStyle(
        "style1",
        createAttribute("string1", "string1 value from style1"),
        createAttribute("string2", "string2 value from style1")
      ),
      false
    )
    themeStyleSet.apply(
      createStyle("style2", createAttribute("string2", "string2 value from style2")),
      false
    )
    assertThat(themeStyleSet.getAttrValue(attrName("string1")).value)
      .isEqualTo("string1 value from style1")
    assertThat(themeStyleSet.getAttrValue(attrName("string2")).value)
      .isEqualTo("string2 value from style1")
  }

  @Test
  @Throws(Exception::class)
  fun shouldFindAttributesFromAnAppliedFromForcedStyle() {
    themeStyleSet.apply(
      createStyle(
        "style1",
        createAttribute("string1", "string1 value from style1"),
        createAttribute("string2", "string2 value from style1")
      ),
      false
    )
    themeStyleSet.apply(
      createStyle("style2", createAttribute("string1", "string1 value from style2")),
      true
    )
    assertThat(themeStyleSet.getAttrValue(attrName("string1")).value)
      .isEqualTo("string1 value from style2")
    assertThat(themeStyleSet.getAttrValue(attrName("string2")).value)
      .isEqualTo("string2 value from style1")
  }

  private fun createStyle(
    styleName: String,
    vararg attributeResources: AttributeResource
  ): StyleData {
    return StyleData("package", styleName, null, listOf(*attributeResources))
  }

  private fun createAttribute(attrName: String, value: String): AttributeResource {
    return AttributeResource(attrName(attrName), value, "package")
  }

  private fun attrName(attrName: String): ResName {
    return ResName("package", "attr", attrName)
  }
}
