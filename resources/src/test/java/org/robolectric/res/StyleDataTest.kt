package org.robolectric.res

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class StyleDataTest {

    private val androidSearchViewStyle = ResName("android", "attr", "searchViewStyle")
    private val myLibSearchViewStyle = ResName("library.resource", "attr", "searchViewStyle")
    private val myAppSearchViewStyle = ResName("my.app", "attr", "searchViewStyle")

    @Test
    fun getAttrValue_willFindLibraryResourcesWithSameName(){
        val styleData = StyleData("library.resource", "Theme_MyApp", "Theme_Material", Arrays.asList(
                    AttributeResource(myLibSearchViewStyle, "lib_value", "library.resource")
        ))

        assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("lib_value")
        assertThat(styleData.getAttrValue(myLibSearchViewStyle).value).isEqualTo("lib_value")

        assertThat(styleData.getAttrValue(androidSearchViewStyle)).isNull()
        }

    @Test
    fun attrValue_willNotFindFrameworkResourcesWithSameName() {
            val styleData = StyleData("android", "Theme_Material", "Theme", Arrays.asList(
                    AttributeResource(androidSearchViewStyle, "android_value", "android")
            ))

            assertThat(styleData.getAttrValue(androidSearchViewStyle).value).isEqualTo("android_value")
            assertThat(styleData.getAttrValue(myAppSearchViewStyle)).isNull()

            assertThat(styleData.getAttrValue(myLibSearchViewStyle)).isNull()
        }

    // todo: any packageNames that aren't 'android' should be treated as equivalent
//    assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("lib_value");
    @Test
    fun attrValue_willChooseBetweenAmbiguousAttributes(){
            val styleData = StyleData("android", "Theme_Material", "Theme", Arrays.asList(
                    AttributeResource(myLibSearchViewStyle, "lib_value", "library.resource"),
                    AttributeResource(androidSearchViewStyle, "android_value", "android")
            ))

            assertThat(styleData.getAttrValue(androidSearchViewStyle).value).isEqualTo("android_value")
            assertThat(styleData.getAttrValue(myLibSearchViewStyle).value).isEqualTo("lib_value")

            // todo: any packageNames that aren't 'android' should be treated as equivalent
//    assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("lib_value");
        }

    @Throws(Exception::class)
    @Test
    fun attrValue_willReturnTrimmedAttributeValues() {
            val styleData = StyleData("library.resource", "Theme_MyApp", "Theme_Material", Arrays.asList(
                    AttributeResource(myLibSearchViewStyle, "\n lib_value ", "library.resource")
            ))

            assertThat(styleData.getAttrValue(myAppSearchViewStyle).value).isEqualTo("\n lib_value ")
            assertThat(styleData.getAttrValue(myLibSearchViewStyle).trimmedValue).isEqualTo("lib_value")
        }
}