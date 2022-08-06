package org.robolectric.manifest

import com.google.common.collect.ImmutableList
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.res.ResourceTable
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Tests for {@link MetaData}
 */
@RunWith(JUnit4::class)
class MetaDataTest {
    @Mock private val resourceProvider: ResourceTable? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    @Throws(Exception::class)
    fun testNonExistantResource_throwsResourceNotFoundException() {
        val metaDataElement = createMetaDataNode("aName", "@xml/non_existant_resource")

        val metaData = MetaData(ImmutableList.of<Node>(metaDataElement))

        assertThrows(RoboNotFoundException::class.java) { metaData.init(resourceProvider, "a.package") }
    }

    companion object {
        private fun createMetaDataNode(name: String, value: String): Element {
            val dbf = DocumentBuilderFactory.newInstance()
            val metaDataElement: Element
            try {
                val db = dbf.newDocumentBuilder()
                metaDataElement = db.newDocument().createElement("meta-data")
                metaDataElement.setAttribute("android:name", name)
                metaDataElement.setAttribute("android:value", value)
            } catch (e: ParserConfigurationException) {
                throw RuntimeException(e)
            }
            return metaDataElement
        }
    }
}